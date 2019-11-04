package de.ctoffer.muesli;

import de.ctoffer.login.CredentialsAccess;
import de.ctoffer.util.ThreadUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class Muesli extends CredentialsAccess<Muesli> {
    public static Muesli getInstance(final WebDriver driver) {
        if(instance == null) {
            instance = new Muesli(driver);
        }

        return instance;
    }

    private static Muesli instance;

    private Muesli(final WebDriver driver) {
        super(driver);
    }

    @Override
    protected void login(final String name, final String password) {
        driver.get("https://muesli.mathi.uni-heidelberg.de/user/login");
        driver.findElement(By.cssSelector("input[name='email']")).sendKeys(name);
        driver.findElement(By.cssSelector("input[name='password']")).sendKeys(decipherPassword(password));
        ThreadUtils.sleepNoThrow(5000);
        driver.findElement(By.cssSelector("input[type='submit']")).click();
    }

    public void selectISW() {
        // driver
    }

    @Override
    protected void logout() {
        driver.findElement(By.cssSelector("a[href='/user/logout']")).click();
    }
}
