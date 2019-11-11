package de.ctoffer.assistance.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.ctoffer.assistance.context.*;
import de.ctoffer.login.DriverCore;
import de.ctoffer.meta.Exercise;
import de.ctoffer.meta.FolderManager;
import de.ctoffer.meta.MetaManager;
import de.ctoffer.moodle.Moodle;
import de.ctoffer.moodle.SubmissionRow;
import de.ctoffer.util.Config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Require({Context.CONSOLE, Context.SUB_SPACE, Context.CONFIG})
public class MoodleCommand extends Command {
    private static final int DOWNLOAD_SUBMISSIONS_INDEX = 1;
    private List<String> validArguments = Arrays.asList(
            "open",
            "downloadSubmissions=(\\d+)",
            "close"
    );
    private Moodle moodle;
    private DriverCore driverCore;

    @Override
    public String name() {
        return "moodle";
    }

    @Override
    protected List<String> getValidNonHelpArguments() {
        return Collections.unmodifiableList(validArguments);
    }

    @Override
    public void runCore() {
        SubSpaceContext context = contexts.getContext(Context.SUB_SPACE);

        for (String argument : arguments.getNonHelpFlags()) {
            String validArg = validArguments.stream()
                    .filter(argument::matches)
                    .findFirst()
                    .orElse("");
            int index = validArguments.indexOf(validArg);
            if (index != -1) {
                selectActionByIndex(context, argument, index);
            }
        }
    }

    private void selectActionByIndex(SubSpaceContext context, String input, int index) {
        Config config = contexts.getContext(Context.CONFIG);

        switch (index) {
            case 0:
                driverCore = new DriverCore(config.sub("selenium"));
                context.openSpace(SubSpace.MOODLE);
                final JsonObject user = config.getObject("user/Moodle");
                moodle = driverCore.getMoodleInstance().login(user);
                break;
            case 1:
                if(moodle == null) {
                    throw new IllegalStateException("Moodle must first be opened, before it can be used!");
                }
                try {
                    downloadSubmissionsOfSheet(input);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                break;
            case 2:
                context.closeSpace(SubSpace.MOODLE);
                moodle.close();
                moodle = null;
                driverCore.close();
                break;
        }

    }

    private void downloadSubmissionsOfSheet(String input) throws Exception {
        Config config = contexts.getContext(Context.CONFIG);
        Matcher regex = Pattern.compile(validArguments.get(DOWNLOAD_SUBMISSIONS_INDEX)).matcher(input);

        if (regex.find()) {
            int sheetNumber = Integer.parseInt(regex.group(1));
            downloadSubmissions(config, sheetNumber);
        }
    }

    private void downloadSubmissions(final Config config, final int sheetNr) throws Exception {
        ConsoleContext console = contexts.getContext(Context.CONSOLE);
        try (MetaManager manager = new MetaManager(Paths.get(config.getString("home"), config.getString("metafile")))) {
            FolderManager folderManager = new FolderManager(config);

            List<Exercise> exercises = getTestatExercises(config, "" + sheetNr);
            final Map<Exercise, List<SubmissionRow>> rows = new HashMap<>();

            moodle.selectISW();
            final Function<String, List<SubmissionRow>> extractRow = name -> moodle.selectExerciseByName(
                    name,
                    manager.studentList(),
                    console
            );
            exercises.forEach(exercise -> rows.put(exercise, extractRow.apply(exercise.getName())));

            final Map<Integer, String> groupFolderNames = folderManager
                    .getExerciseManager()
                    .createGroupFolders(manager.getGroups(), sheetNr);

            FolderManager.ExerciseManager exerciseManager = folderManager.getExerciseManager();

            for (Map.Entry<Exercise, List<SubmissionRow>> entry : rows.entrySet()) {
                console.output(entry.getKey().getName());
                for (SubmissionRow row : entry.getValue()) {
                    console.output("    " + row.getStudent() + ": " + row.getFileURL());
                }

                List<Path> downloadedPaths = exerciseManager.downloadAll(
                        moodle,
                        groupFolderNames,
                        entry.getValue(),
                        sheetNr,
                        entry.getKey()
                );
                console.output("Update metadata.");
                exerciseManager.savePathsForDownloadedSubmissions(sheetNr, entry.getKey().getAlias(), downloadedPaths);
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

}
