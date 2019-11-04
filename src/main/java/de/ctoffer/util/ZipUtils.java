package de.ctoffer.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
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

    public static Optional<Path> unzip(Path src) {
        return new Unzipper(src).run();
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

    public Optional<Path> run() {
        Optional<Path> result = Optional.empty();
        try {
            logger.debug("Unzip from '{}' to '{}'", source.getFileName(), destination.getFileName());
            Files.createDirectories(destination);

            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(source))) {
                tryLoopUntilNull(zis::getNextEntry, entry -> handleZipEntry(zis, entry));
            }

            result = Optional.of(destination);
        } catch (IOException ioe) {
            logger.catching(Level.WARN, ioe);
        }

        return result;
    }

    private void handleZipEntry(final ZipInputStream zis, final ZipEntry zipEntry) throws IOException {
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
                    new Unzipper(entryPath).run();
                }
            }
        }
        zis.closeEntry();
    }

    private boolean isMacMeta(final String fileName) {
        return !fileName.contains("__MACOSX");
    }
}
