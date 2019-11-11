package de.ctoffer.meta;

import de.ctoffer.moodle.Moodle;
import de.ctoffer.moodle.SubmissionRow;
import de.ctoffer.util.Config;
import de.ctoffer.util.Serial;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.stream.Collectors.*;

public class FolderManager {
    private ExerciseManager exerciseManager;

    public FolderManager(Config config) {
        this.exerciseManager = new ExerciseManager(config);
    }

    public ExerciseManager getExerciseManager() {
        return exerciseManager;
    }

    public static class ExerciseManager {
        private String home;
        private String mainFolder;
        private String sheetFolderFormat;
        private String submissionFolder;
        private String interNameDivider;
        private String intraNameDivider;
        private String fileNameFormat;

        public ExerciseManager(Config config) {
            Config exerciseConfig = config.sub("folder/exercises");
            home = config.getString("home");
            mainFolder = exerciseConfig.getString("main");
            sheetFolderFormat = exerciseConfig.getString("sheet");
            submissionFolder = exerciseConfig.getString("submission");
            interNameDivider = exerciseConfig.getString("group/interNameDivider");
            intraNameDivider = exerciseConfig.getString("group/intraNameDivider");
            fileNameFormat = exerciseConfig.getString("filename");
        }

        public Path getSheetFolder(int sheetNr) {
            return Paths.get(home, mainFolder, String.format(sheetFolderFormat, sheetNr), submissionFolder);
        }

        public Map<Integer, String> createGroupFolders(Map<Integer, List<Student>> groups, int sheetNr) {
            Path submissions = Paths.get(home, mainFolder, String.format(sheetFolderFormat, sheetNr), submissionFolder);
            createDirectories(submissions);
            Map<Integer, String> groupFolderNames = groups.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, this::createGroupFolderName));
            groupFolderNames.values()
                    .stream()
                    .map(submissions::resolve)
                    .forEach(ExerciseManager::createDirectories);
            return groupFolderNames;
        }

        public List<Path> downloadAll(Moodle moodle,
                                Map<Integer, String> groupFolderNames,
                                List<SubmissionRow> rows,
                                int sheetNr,
                                Exercise exercise) throws IOException {
            Path submissions = Paths.get(home, mainFolder, String.format(sheetFolderFormat, sheetNr), submissionFolder);
            Files.createDirectories(submissions);

            Comparator<SubmissionRow> comp = Comparator.comparing(row -> row.getStudent().getGroupId());
            List<Path> downloadedPaths = new ArrayList<>();
            for (SubmissionRow row : rows.stream().sorted(comp).collect(toList())) {
                Student student = row.getStudent();
                int groupId = student.getGroupId();
                String groupName = groupFolderNames.get(groupId);
                Path groupFolder = submissions.resolve(groupName);
                String fileName = String.format(fileNameFormat,
                        exercise.getAlias(),
                        student.getName().replace(" ", "-"),
                        row.getFileName());
                Path saveFile = groupFolder.resolve(fileName);
                String fileUrl = row.getFileURL();
                if (fileUrl != null) {
                    moodle.download(fileUrl, saveFile);
                    downloadedPaths.add(saveFile);
                }
            }

            return downloadedPaths;
        }

        private static void createDirectories(Path p) {
            try {
                Files.createDirectories(p);
            } catch (IOException ioe) {
                throw new IllegalStateException(ioe);
            }
        }

        private String createGroupFolderName(Map.Entry<Integer, List<Student>> entry) {
            return createGroupFolderName(entry.getKey(), entry.getValue());
        }

        private String createGroupFolderName(int groupId, List<Student> students) {
            return String.format("%02d_", groupId) +
                    students.stream()
                            .map(Student::getName)
                            .map(name -> name.replace(" ", intraNameDivider))
                            .collect(Collectors.joining(interNameDivider));
        }


        public void savePathsForDownloadedSubmissions(final int sheetNr,
                                                      final String name,
                                                      final List<Path> downloadedPaths) throws IOException {
            final String fileName = String.format("submissionPaths_%s.ser", name);
            final Path folder = getSheetFolder(sheetNr);
            Files.createDirectories(folder);
            final Path submissionPaths = folder.resolve(fileName);
            final ArrayList<URI> paths = downloadedPaths.stream()
                    .map(Path::toAbsolutePath)
                    .map(Path::toUri)
                    .collect(toCollection(ArrayList::new));
            Serial.write(submissionPaths.toFile(), paths);
        }

        public Optional<List<Path>> loadPathsForDownloadedSubmissions(final int sheetNr, final String name) {
            try {
                final String fileName = String.format("submissionPaths_%s.ser", name);
                final Path folder = getSheetFolder(sheetNr);
                final Path submissionPaths = folder.resolve(fileName);
                List<URI> absolutePaths = Serial.read(submissionPaths.toFile());
                return Optional.of(absolutePaths.stream().map(Paths::get).collect(Collectors.toList()));
            } catch(Exception e) {
                return Optional.empty();
            }
        }

        public void deleteSavedPathsMetadata(final int sheetNr, final String name) throws IOException {
            final String fileName = String.format("submissionPaths_%s.ser", name);
            final Path folder = getSheetFolder(sheetNr);
            final Path submissionPaths = folder.resolve(fileName);
            Files.delete(submissionPaths);
        }
    }
}