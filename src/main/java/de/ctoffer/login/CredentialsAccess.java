package de.ctoffer.login;

import com.google.gson.JsonObject;
import de.ctoffer.util.ThreadUtils;
import org.openqa.selenium.WebDriver;

public abstract class CredentialsAccess <T extends CredentialsAccess<T>> implements AutoCloseable {
    protected final WebDriver driver;
    private boolean loggedIn;

    public CredentialsAccess(final WebDriver driver) {
        this.driver = driver;
        this.loggedIn = false;
    }

    public T login(JsonObject object) {
        if(loggedIn) {
            throw new IllegalStateException("Already logged in!");
        }

        login(object.get("name").getAsString(), object.get("password").getAsString());

        loggedIn = true;
        return (T) this;
    }

    protected abstract void login(String name, String password);

    protected static String decipherPassword(final String password) {
        assert(password.length() % 3 == 0);
        final StringBuilder builder = new StringBuilder();

        for(int i = 0; i < password.length(); i += 3) {
            builder.append(((char) Integer.parseInt(password.substring(i, i + 3))));
        }

        return builder.toString();
    }

    @Override
    public void close() {
        if(!loggedIn) {
            throw new IllegalStateException("Must be logged in to log out!");
        }

        logout();
        loggedIn = false;
        ThreadUtils.sleepNoThrow(5000);
    }

    protected abstract void logout();
}
