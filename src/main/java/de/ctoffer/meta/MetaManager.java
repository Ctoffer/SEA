package de.ctoffer.meta;

import de.ctoffer.util.Config;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetaManager implements AutoCloseable{
    private final XSSFWorkbook workbook;
    private final List<Student> studentList;

    public MetaManager(Path location) throws IOException {
        workbook = new XSSFWorkbook(Files.newInputStream(location));
        studentList = new ArrayList<>();
        readStudentList();
    }

    private void readStudentList() {
        XSSFSheet sheet = workbook.getSheet("Metadaten");
        for(Row row : sheet) {
            if(row.getCell(0).getCellType() == CellType.NUMERIC) {
                studentList.add(Student.fromRow(row));
            }
        }
    }

    public Map<Integer, List<Student>> getGroups() {
        return studentList.stream().collect(Collectors.groupingBy(Student::getGroupId));
    }

    public Stream<Student> students() {
        return studentList.stream();
    }

    public List<Student> studentList() {
        return Collections.unmodifiableList(studentList);
    }

    public void prepareSheet(Config config, String key) {
        String format = config.getString("excel/sheetFormat");
        String sheetName = String.format(format, key);
        XSSFSheet sheet = workbook.getSheet(sheetName);
        if(sheet == null) {
            sheet = workbook.createSheet(sheetName);

        }
    }

    private void createHeaders(XSSFSheet sheet) {
        Row headerRow = sheet.getRow(0);
        int i = 0;
        headerRow.getCell(i++).setCellValue("Start");
        headerRow.getCell(i++).setCellValue("Ende");
        headerRow.getCell(i++).setCellValue("Person 1");
        headerRow.getCell(i++).setCellValue("Person 2");
        headerRow.getCell(i++).setCellValue("Person 3");
        headerRow.getCell(i++).setCellValue("Anmerkung");
    }

    @Override
    public void close() throws IOException {
        workbook.close();
    }
}
