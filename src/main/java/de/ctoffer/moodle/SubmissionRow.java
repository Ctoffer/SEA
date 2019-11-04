package de.ctoffer.moodle;

import de.ctoffer.meta.Student;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Objects;
import java.util.Optional;

public class SubmissionRow {
    private final Student student;
    private final String name;
    private final String mail;
    private final String state;
    private final String date;
    private final String fileName;
    private final String fileURL;

    public static SubmissionRow fromRow(Student student, WebElement row) {
        Objects.requireNonNull(row);
        final String name = getCellTextAt(row, 2);
        final String mail = getCellTextAt(row, 3);
        final String state = getCellTextAt(row, 4);
        final String date = getCellTextAt(row, 7);
        final Optional<WebElement> file = tryFindLink(getCellAt(row, 8));
        final String fileName = file.map(WebElement::getText).orElse(null);
        final String fileURL = file.map(elem -> elem.getAttribute("href")).orElse(null);

        return new SubmissionRow(student, name, mail, state, date, fileName, fileURL);
    }

    private static String getCellTextAt(WebElement row, int index) {
        return getCellAt(row, index).getText();
    }

    private static WebElement getCellAt(WebElement row, int index) {
        return row.findElement(By.cssSelector("td[class*='cell c" + index + "']"));
    }

    private static Optional<WebElement> tryFindLink(WebElement cell) {
        Optional<WebElement> result;
        try {
            result = Optional.of(cell.findElement(By.cssSelector("a[target='_blank']")));
        } catch(Exception e) {
            result = Optional.empty();
        }

        return result;
    }

    private SubmissionRow(final Student student,
                          final String name,
                          final String mail,
                          final String state,
                          final String date,
                          final String fileName,
                          final String fileURL) {
        this.student = student;
        this.name = name;
        this.mail = mail;
        this.state = state;
        this.date = date;
        this.fileName = fileName;
        this.fileURL = fileURL;
    }

    public Student getStudent() {
        return student;
    }

    public String getName() {
        return name;
    }

    public String getMail() {
        return mail;
    }

    public String getState() {
        return state;
    }

    public String getDate() {
        return date;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileURL() {
        return fileURL;
    }
}
