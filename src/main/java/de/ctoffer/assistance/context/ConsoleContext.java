package de.ctoffer.assistance.context;

import de.ctoffer.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public interface ConsoleContext {
    default void output(final String message, Object obj, Object... objects) {
        final List<Object> toInject = new ArrayList<>();
        toInject.add(obj);
        toInject.addAll(Arrays.asList(objects));
        output(String.format(message, toInject.toArray()));
    }

    void output(final String message);
    void error(final String message);
    String input(final String message);

    public static ConsoleContext build(Consumer<String> output, Consumer<String> error, UnaryOperator<String> input) {
        ObjectUtils.requireAllNonNull(output, error, input);
        return new ConsoleContext() {
            @Override
            public void output(String message) {
                output.accept(message);
            }

            @Override
            public void error(String message) {
                error.accept(message);
            }

            @Override
            public String input(String message) {
                return input.apply(message);
            }
        };
    }
}
