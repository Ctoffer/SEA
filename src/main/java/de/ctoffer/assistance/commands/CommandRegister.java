package de.ctoffer.assistance.commands;

import java.util.HashMap;
import java.util.Map;

public enum CommandRegister {
    INSTANCE;

    private Map<String, String> aliases = new HashMap<>();
    private Map<String, Command> commands = new HashMap<>();

    CommandRegister() {
        register(new EmptyCommand(), "");
        register(new ExitCommand(), "stop");
        register(new MoodleCommand());
        register(new UnzipCommand(), "uz");
    }

    private void register(Command command, String... aliases) {
        String name = command.name();
        this.aliases.put(name, name);
        for(final String alias : aliases) {
            this.aliases.put(alias, name);
        }
        commands.put(name, command);
    }

    public Command getCommand(String name) {
        return commands.getOrDefault(aliases.getOrDefault(name, name), new UnknownCommand(name));
    }
}
