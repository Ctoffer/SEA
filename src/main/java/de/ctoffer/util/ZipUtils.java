package de.ctoffer.util;

import de.ctoffer.assistance.context.ConsoleContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static de.ctoffer.util.FileUtils.deletePath;
import static de.ctoffer.util.LoopUtils.tryLoopUntilNull;

public enum ZipUtils {
    ;

    public static boolean isZip(Path p) {
        return Files.exists(p) && p.getFileName().toString().endsWith(".zip");
    }

    public static Optional<Path> unzip(Path src, ConsoleContext console) {
        return new Unzipper(src).run(console);
    }
}

class Unzipper {
    private final Path source;
    private final Path destination;
    private static final Logger logger = LogManager.getLogger(Unzipper.class);

    public Unzipper(final Path source) {
        this(source, toDestination(source));
    }

    private static Path toDestination(final Path source) {
        String path = source.getFileName().toString().replace(".zip", "");
        return source.getParent().resolve(path);
    }

    public Unzipper(final Path source, final Path destination) {
        if (!ZipUtils.isZip(source)) {
            throw new IllegalArgumentException("Source must be a zip file!");
        }
        this.source = source;
        this.destination = destination;
    }

    public Optional<Path> run(final ConsoleContext console) {
        Optional<Path> result = Optional.empty();
        try {
            console.output("Unzip from '%s' to '%s'", source.getFileName(), destination.getFileName());
            Files.createDirectories(destination);
            final Charset CP437 = Charset.forName("CP437");

            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(source), CP437)) {
                tryLoopUntilNull(zis::getNextEntry, entry -> handleZipEntry(zis, entry, console));
            }

            result = Optional.of(destination);
        } catch (IOException ioe) {
            console.error(ioe.getMessage());
        }

        return result;
    }

    private void handleZipEntry(final ZipInputStream zis,
                                final ZipEntry zipEntry,
                                final ConsoleContext console) throws IOException {
        final String fileName = zipEntry.getName();
        if (isMacMeta(fileName)) {
            final Path entryPath = destination.resolve(fileName);
            deletePath(entryPath);

            if (zipEntry.isDirectory()) {
                Files.createDirectories(entryPath);
            } else {
                Files.createDirectories(entryPath.getParent());
                Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                if(ZipUtils.isZip(entryPath)) {
                    new Unzipper(entryPath).run(console);
                }
            }
        }
        zis.closeEntry();
    }

    private boolean isMacMeta(final String fileName) {
        return !fileName.contains("__MACOSX");
    }
}
