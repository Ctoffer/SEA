package de.ctoffer.assistance.commands;

import com.google.gson.JsonElement;
import de.ctoffer.assistance.context.ConsoleContext;
import de.ctoffer.assistance.context.Context;
import de.ctoffer.assistance.context.Require;
import de.ctoffer.meta.Exercise;
import de.ctoffer.meta.FolderManager;
import de.ctoffer.meta.MetaManager;
import de.ctoffer.util.Config;
import de.ctoffer.util.ZipUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Require({Context.CONSOLE, Context.CONFIG})
public class UnzipCommand extends Command{
    private static final int UNZIP_SUBMISSIONS_INDEX = 0;
    private List<String> validArguments = Arrays.asList("submissions=(\\d+)", "-d");

    @Override
    public String name() {
        return "unzip";
    }

    @Override
    public List<String> getValidNonHelpArguments() {
        return Collections.unmodifiableList(validArguments);
    }

    @Override
    public void runCore() {
        Config config = contexts.getContext(Context.CONFIG);
        ConsoleContext console = contexts.getContext(Context.CONSOLE);

        try (MetaManager manager = new MetaManager(Paths.get(config.getString("home"), config.getString("metafile")))) {
            FolderManager folderManager = new FolderManager(config);
            Optional<String> unzipSubmissions = arguments.hasFlagMatching(validArguments.get(UNZIP_SUBMISSIONS_INDEX));
            unzipSubmissions.ifPresent(input -> unzipSubmissions(folderManager.getExerciseManager(), input));
        } catch (IOException ioe) {
            console.error(ioe.getMessage());
        }
    }

    private void unzipSubmissions(FolderManager.ExerciseManager exerciseManager, String input) {
        Config config = contexts.getContext(Context.CONFIG);
        ConsoleContext console = contexts.getContext(Context.CONSOLE);
        Matcher regex = Pattern.compile(validArguments.get(UNZIP_SUBMISSIONS_INDEX)).matcher(input);
        boolean deleteAfter = arguments.isFlagPresent("-d");

        if (regex.find()) {
            int sheetNumber = Integer.parseInt(regex.group(1));
            for(Exercise exercise : getTestatExercises(config, "" + sheetNumber)) {
                final Optional<List<Path>> savedPaths = exerciseManager.loadPathsForDownloadedSubmissions(
                        sheetNumber,
                        exercise.getAlias()
                );
                if(savedPaths.isPresent()) {
                    unzipAll(savedPaths.get());
                } else {
                    console.error("No paths saved for sheet " + sheetNumber + " and exercise " + exercise.getAlias() + "!");
                    console.error("Call moodle downloadSubmissions=<sheet number> first!");
                }
                if(deleteAfter) {
                    try {
                        exerciseManager.deleteSavedPathsMetadata(sheetNumber, exercise.getAlias());
                    } catch (IOException e) {
                        console.error(e.getMessage());
                    }
                }
            }
        }
    }

    private static List<Exercise> getTestatExercises(Config config, String sheet) {
        Iterator<JsonElement> iterator = config.getList("excel/exercise/" + sheet + "/Testat").iterator();
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
                .map(JsonElement::getAsJsonObject)
                .map(Exercise::fromJson)
                .collect(Collectors.toList());
    }

    private void unzipAll(final List<Path> paths) {
        ConsoleContext console = contexts.getContext(Context.CONSOLE);
        boolean deleteAfter = arguments.isFlagPresent("-d");
        for (Path path : paths) {
            if (ZipUtils.isZip(path)) {
                Optional<Path> resultPath = ZipUtils.unzip(path, console);
                if(resultPath.isPresent() && deleteAfter) {
                    console.output("Deleting: " + path.getFileName());
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        console.error("Failure deleting: " + e.getMessage());
                    }
                }
            }
        }
    }
}
