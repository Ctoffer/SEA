package de.ctoffer.util.funtional;

public interface ThrowingConsumer <T, E extends Exception> {
    void accept(T obj) throws E;
}
