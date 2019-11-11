package de.ctoffer.assistance.commands;

public class EmptyCommand extends Command {
    @Override
    public String name() {
        return "empty";
    }

    @Override
    public String help() {
        return "Empty command - this command will do nothing.";
    }

    @Override
    public void checkArguments() {
        // nothing to do, because this command will do nothing
    }

    @Override
    public void runCore() {
        // nothing to do, because this command will do nothing
    }
}
