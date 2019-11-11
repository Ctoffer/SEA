package de.ctoffer.assistance.commands;

import de.ctoffer.assistance.Arguments;
import de.ctoffer.assistance.context.AvailableContexts;
import de.ctoffer.assistance.context.ConsoleContext;
import de.ctoffer.assistance.context.Context;
import de.ctoffer.assistance.context.Require;
import de.ctoffer.util.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

public abstract class Command {
    private final AtomicInteger counter = new AtomicInteger(0);
    protected Arguments arguments;
    protected AvailableContexts contexts;

    public boolean isKnown() {
        return true;
    }

    public abstract String name();

    public String help() {
        return "No help available.";
    }

    public final void injectContexts(AvailableContexts contexts) {
        this.contexts = Objects.requireNonNull(contexts);
    }

    public void setArguments(Arguments arguments) {
        this.arguments = Objects.requireNonNull(arguments);
    }

    public void execute() {
        ObjectUtils.requireAllNonNull(arguments, contexts);
        checkArguments();
        if(arguments.containsHelp()) {
           ConsoleContext context = contexts.getContext(Context.CONSOLE);
           context.output(help());
        } else {
            runPreCore();
            runCore();
            runPostCore();
            counter.incrementAndGet();
        }
    }

    public int getRunCount() {
        return counter.get();
    }

    public void checkArguments() {
        List<String> args = arguments.getNonHelpFlags();

        for(String argument :  args) {
            if(getValidNonHelpArguments().stream().noneMatch(argument::matches)) {
                throw new IllegalStateException(format("Unknown argument '%s' for command '%s'", argument, name()));
            }
        }
    }

    protected List<String> getValidNonHelpArguments() {
        return Collections.emptyList();
    }

    public void runPreCore() {

    }

    public abstract void runCore();

    public void runPostCore() {

    }
}
