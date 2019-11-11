package de.ctoffer.assistance.commands;

import de.ctoffer.assistance.context.ApplicationContext;
import de.ctoffer.assistance.context.Context;
import de.ctoffer.assistance.context.Require;

@Require({Context.CONSOLE, Context.APPLICATION})
public class ExitCommand extends Command {
    @Override
    public String help() {
        return "Stops the programm";
    }

    @Override
    public String name() {
        return "exit";
    }

    @Override
    public void runCore() {
        ApplicationContext context = contexts.getContext(Context.APPLICATION);
        context.stop();
    }
}
