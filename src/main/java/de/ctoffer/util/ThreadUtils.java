package de.ctoffer.util;

public enum ThreadUtils {
    ;

    public static void sleepNoThrow(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exc) {
            // ignore
        }
    }
}
