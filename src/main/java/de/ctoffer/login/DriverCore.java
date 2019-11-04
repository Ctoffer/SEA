package de.ctoffer.login;

import de.ctoffer.moodle.Moodle;
import de.ctoffer.muesli.Muesli;
import de.ctoffer.util.Config;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class DriverCore implements AutoCloseable {
    private final WebDriver driver;

    public DriverCore(Config seleniumConfig) {
        final String driverName = seleniumConfig.getString("driver");
        switch (driverName) {
            case "Chrome": {
                System.setProperty("webdriver.chrome.driver", seleniumConfig.getString("path"));
                driver = new ChromeDriver();
                break;
            }
            default:
                throw new IllegalStateException("Unknown driver: " + driverName);
        }

    }

    public Muesli getMuesliInstance() {
        return Muesli.getInstance(driver);
    }

    public Moodle getMoodleInstance() {
        return Moodle.getInstance(driver);
    }

    @Override
    public void close() {
        driver.quit();
    }
}
