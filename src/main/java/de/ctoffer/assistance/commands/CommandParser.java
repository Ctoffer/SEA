package de.ctoffer.assistance.commands;

import de.ctoffer.assistance.Arguments;
import de.ctoffer.util.pair.Pair;

import java.util.Arrays;
import java.util.List;

public enum CommandParser {
    ;

    public static Pair<Command, Arguments> parse(final String input) {
        List<String> parts = Arrays.asList(input.split(" "));
        Command command = CommandRegister.INSTANCE.getCommand(parts.get(0));
        Arguments args = new Arguments(parts.subList(1, parts.size()));
        return Pair.paired(command, args);
    }
}
