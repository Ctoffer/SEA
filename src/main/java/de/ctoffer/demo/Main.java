package de.ctoffer.demo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.ctoffer.login.DriverCore;
import de.ctoffer.meta.Exercise;
import de.ctoffer.meta.FolderManager;
import de.ctoffer.meta.MetaManager;
import de.ctoffer.moodle.Moodle;
import de.ctoffer.moodle.SubmissionRow;
import de.ctoffer.muesli.Muesli;
import de.ctoffer.util.Config;
import de.ctoffer.util.ThreadUtils;
import de.ctoffer.util.ZipUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        InputStream configStream = Main.class.getClassLoader().getResourceAsStream("config.json");
        Config config = new Config(configStream);
        try (DriverCore core = new DriverCore(config.sub("selenium"))) {
            runMoodleDemo(core, config);
        }
    }

    private static void runMuesliDemo(DriverCore core, JsonObject user) {
        try (final Muesli muesli = core.getMuesliInstance().login(user)) {
            ThreadUtils.sleepNoThrow(2000);
        }
    }

    private static void runMoodleDemo(DriverCore core, Config config) throws IOException {
        JsonObject user = config.getObject("user/Moodle");
        int sheetNr = 3; // TODO make that better parameterized

        try (MetaManager manager = new MetaManager(Paths.get(config.getString("home"), config.getString("metafile")))) {
            FolderManager folderManager = new FolderManager(config);

            List<Exercise> exercises = getTestatExercises(config, "" + sheetNr);
            final Map<Exercise, List<SubmissionRow>> rows = new HashMap<>();

            try (final Moodle moodle = core.getMoodleInstance().login(user)) {
                moodle.selectISW();
                final Function<String, List<SubmissionRow>> extractRow = name -> moodle.selectExerciseByName(name, manager.studentList());
                exercises.forEach(exercise -> rows.put(exercise, extractRow.apply(exercise.getName())));

                final Map<Integer, String> groupFolderNames = folderManager
                        .getExerciseManager()
                        .createGroupFolders(manager.getGroups(), sheetNr);
                for (Map.Entry<Exercise, List<SubmissionRow>> entry : rows.entrySet()) {
                    logger.info(entry.getKey().getName());
                    for (SubmissionRow row : entry.getValue()) {
                        logger.info("    " + row.getStudent() + ": " + row.getFileURL());
                    }
                    List<Path> downloadedPaths = folderManager
                            .getExerciseManager()
                            .downloadAll(moodle, groupFolderNames, entry.getValue(), sheetNr, entry.getKey());
                    downloadedPaths.stream().filter(ZipUtils::isZip).forEach(ZipUtils::unzip);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
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
