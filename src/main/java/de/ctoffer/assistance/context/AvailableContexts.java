package de.ctoffer.assistance.context;

import de.ctoffer.login.DriverCore;
import de.ctoffer.util.Config;

import java.util.EnumMap;
import java.util.Map;

public class AvailableContexts {
    private Map<Context, Object> contexts = new EnumMap<>(Context.class);

    public void setApplicationContext(ApplicationContext context) {
        contexts.put(Context.APPLICATION, context);
    }

    public void setConsoleContext(ConsoleContext context) {
        contexts.put(Context.CONSOLE, context);
    }

    public void setSubSpaceContext(SubSpaceContext context) {
        contexts.put(Context.SUB_SPACE, context);
    }

    public void setConfig(Config config) {
        contexts.put(Context.CONFIG, config);
    }

    public <T> T getContext(Context context) {
        return (T) contexts.get(context);
    }
}
