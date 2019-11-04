package de.ctoffer.moodle;

import de.ctoffer.login.CredentialsAccess;
import de.ctoffer.meta.Student;
import org.apache.commons.math3.util.Pair;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.ctoffer.util.ThreadUtils.sleepNoThrow;
import static java.util.stream.Collectors.toMap;

public class Moodle extends CredentialsAccess<Moodle> {
    private static final int COURSE_ID = 22829;

    public static Moodle getInstance(final WebDriver driver) {
        if (instance == null) {
            instance = new Moodle(driver);
        }

        return instance;
    }

    private static Moodle instance;
    private Map<String, String> cookies = new HashMap<>();

    private Moodle(final WebDriver driver) {
        super(driver);
    }

    @Override
    protected void login(final String name, final String password) {
        /*
        try {
            loginDirectly(name, password);
            System.out.println("Direct login worked");
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to login directly!");
        }
        */


        driver.get("https://elearning2.uni-heidelberg.de/login/index.php");
        driver.findElement(By.cssSelector("input[name='username']")).sendKeys(name);
        driver.findElement(By.cssSelector("input[name='password']")).sendKeys(decipherPassword(password));
        sleepNoThrow(1000);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    private void loginDirectly(final String name, final String password) throws IOException {
        final Map<String, String> loginData = new HashMap<>();
        loginData.put("username", name);
        loginData.put("password", decipherPassword(password));
        String loginTokenId = "logintoken";

        Connection connection = Jsoup.connect("https://elearning2.uni-heidelberg.de/login/index.php");

        final String loginToken = connection.get().getElementsByTag("input")
                .stream()
                .filter(a -> loginTokenId.equals(a.attr("name")))
                .findFirst()
                .orElseThrow(RuntimeException::new)
                .attr(loginTokenId);
        loginData.put(loginTokenId, loginToken);

        final Connection.Response res = connection
                .data(loginData)
                .method(Connection.Method.POST)
                .execute();
        this.cookies = res.cookies();
        if (!res.parse().getElementsByClass("alert-danger").isEmpty()) {
            throw new RuntimeException("Login failed - check your mail, password and internet connection");
        }

    }

    public void selectISW() {
        // driver
        driver.get("https://elearning2.uni-heidelberg.de/course/view.php?id=" + COURSE_ID);

    }

    public List<SubmissionRow> selectExerciseByName(String name, List<Student> myStudents) {
        selectISW();
        clickOnExercise(name);
        sleepNoThrow(3000);
        selectAllSubmissions();
        System.out.println("Collect rows");
        Map<Student, Optional<WebElement>> associatedRows = getRowsOfMyStudents(myStudents);
        Map<Boolean, List<Pair<Student, WebElement>>> accessable = new HashMap<>();
        accessable.computeIfAbsent(true, k -> new ArrayList<>());
        accessable.computeIfAbsent(false, k -> new ArrayList<>());
        associatedRows.forEach((student, opt) -> accessable.get(opt.isPresent()).add(Pair.create(student, opt.orElse(null))));
        System.out.println(String.format("Success: %s, Failure: %s, Total: %s",
                accessable.get(true).size(),
                accessable.get(false).size(),
                myStudents.size())
        );
        return accessable.get(true)
                .stream()
                .map(pair -> SubmissionRow.fromRow(pair.getFirst(), pair.getSecond()))
                .collect(Collectors.toList());
    }

    private void clickOnExercise(String name) {
        driver.findElement(By.xpath("//a/descendant::span[contains(text(), '" + name + "')]")).click();
    }

    private void selectAllSubmissions() {
        showAll();
        adjustPerPage();
    }

    private Map<Student, Optional<WebElement>> getRowsOfMyStudents(List<Student> myStudents) {
        return myStudents.stream().collect(toMap(Function.identity(), this::findStudentRow));
    }

    private Optional<WebElement> findStudentRow(Student student) {
        int id = student.getMoodleId();
        Optional<WebElement> result;
        try {
            WebElement row = driver.findElement(By.cssSelector("tr[class*='user" + id + "']"));
            result = Optional.ofNullable(row);
        } catch (Exception e) {
            result = Optional.empty();
        }

        return result;
    }

    private void showAll() {
        driver.findElement(By.xpath("//a[contains(text(), 'Alle Abgaben anzeigen')]")).click();
    }

    private void adjustPerPage() {
        driver.findElement(By.xpath("//select[@id='id_perpage']")).click();
        driver.findElement(By.xpath("//option[@value='-1']")).click();
    }

    @Override
    protected void logout() {
        driver.findElement(By.cssSelector("a[aria-label='Nutzermenü']")).click();
        driver.findElement(By.cssSelector("a[data-title='logout,moodle']")).click();
        //logoutDirectly();
    }

    private void logoutDirectly() {
        String logoutUrl = "https://elearning2.uni-heidelberg.de/login/logout.php";
        try {
            String sesskey = get(logoutUrl).getElementsByTag("input")
                    .stream()
                    .filter(e -> e.hasAttr("type")
                            && e.hasAttr("name")
                            && e.hasAttr("value"))
                    .findFirst()
                    .orElseThrow(() -> new IOException("No sesskey found"))
                    .attr("value");


            get(logoutUrl + "?sesskey=" + sesskey);
        } catch (IOException ioe) {
            System.err.println("Error occured while performing logout!");
        }
    }

    private Document get(final String url) throws IOException {
        return Jsoup.connect(url)
                .maxBodySize(0)
                .cookies(cookies)
                .get();
    }

    public void download(final String source, final Path destination) throws IOException {
        try {
            Files.write(destination, this.getRaw(source).bodyAsBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Connection.Response getRaw(final String url) throws IOException {
        return Jsoup.connect(url)
                .maxBodySize(0)
                .cookies(driver.manage().getCookies().stream().collect(Collectors.toMap(Cookie::getName, Cookie::getValue)))
                .ignoreContentType(true)
                .execute();
    }
}
