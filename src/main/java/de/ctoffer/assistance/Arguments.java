package de.ctoffer.assistance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Arguments {

    private List<String> argumentList;

    public Arguments() {
        argumentList = new ArrayList<>();
    }

    public Arguments(final List<String> input) {
        argumentList = input.stream().filter(s -> !s.contains(" ")).collect(Collectors.toList());
    }

    public void addArgument(String s) {
        if(s.contains(" ")) {
            throw new IllegalArgumentException("flag names mustn't contain space!");
        }
        argumentList.add(s);
    }

    public List<String> getNonHelpFlags() {
        return argumentList.stream()
                .filter(s -> !s.contains("--help"))
                .filter(s -> !s.contains("--h"))
                .filter(s -> !s.contains("?"))
                .collect(Collectors.toList());
    }

    public boolean isFlagPresent(final String flag) {
        if(flag.contains(" ")) {
            throw new IllegalArgumentException("flag names mustn't contain space!");
        }
        return argumentList.contains(flag);
    }

    public Optional<String> hasFlagMatching(final String pattern) {
        return argumentList.stream()
                .filter(arg -> arg.matches(pattern))
                .findFirst();
    }

    public boolean containsHelp() {
        return argumentList.contains("--help") || argumentList.contains("-h") || argumentList.contains("?");
    }
}
