package de.ctoffer.assistance.commands;

import java.util.Objects;

public class UnknownCommand extends Command {
    private String input;

    public UnknownCommand(String input) {
        this.input = Objects.requireNonNull(input);
    }

    @Override
    public boolean isKnown() {
        return false;
    }

    @Override
    public String name() {
        return input;
    }

    @Override
    public void checkArguments() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void runCore() {
        throw new UnsupportedOperationException();
    }
}
