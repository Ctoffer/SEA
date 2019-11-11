package de.ctoffer.assistance;

import de.ctoffer.assistance.commands.Command;
import de.ctoffer.assistance.commands.CommandParser;
import de.ctoffer.assistance.context.*;
import de.ctoffer.util.Config;
import de.ctoffer.util.Input;
import de.ctoffer.util.pair.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Assistant implements AutoCloseable {
    private static final String INPUT_PREFIX = "SEA";
    private static final Assistant INSTANCE = new Assistant();

    public static Assistant getInstance() {
        return INSTANCE;
    }

    private ActiveStreams streams;
    private boolean active;
    private String subSpaceTag;
    private SubSpaceContext subSpaceContext;

    private Assistant() {
        this.active = true;
        streams = new ActiveStreams(System.out, System.out, System.in);
        subSpaceTag = "";
        subSpaceContext = SubSpaceContext.build(this::openSubSpace, this::closeSubSpace);
    }

    public StreamConfiguration configureStreams() {
        return new StreamConfiguration();
    }

    public void waitForOrder() {
        String userMessage = readMessage(buildInputMessage());
        Pair<Command, Arguments> input = parseUserMessage(userMessage);
        Command command = input.first;
        Arguments args = input.second;

        try {
            if (command.isKnown()) {
                injectContextFor(command);
                command.setArguments(args);
                command.execute();
            } else {
                writeIndentedError(String.format("Command '%s' is unknown.", command.name()));
            }
        } catch (Exception e) {
            writeIndentedError(e.getMessage());
        }
    }

    private String buildInputMessage() {
        return INPUT_PREFIX + subSpaceTag + ": ";
    }

    private Pair<Command, Arguments> parseUserMessage(String userMessage) {
        return CommandParser.parse(userMessage);
    }

    private void injectContextFor(final Command command) {
        final AvailableContexts contexts = new AvailableContexts();
        final Require require = command.getClass().getAnnotation(Require.class);

        for (Context context : require.value()) {
            injectContextIn(contexts, context);

        }
        command.injectContexts(contexts);
    }

    private void injectContextIn(AvailableContexts contexts, Context context) {
        switch (context) {
            case APPLICATION:
                setContext(contexts::setApplicationContext, this::getApplicationContext);
                break;
            case CONSOLE:
                setContext(contexts::setConsoleContext, this::getConsoleContext);
                break;
            case CONFIG:
                setContext(contexts::setConfig, this::getConfig);
                break;
            case SUB_SPACE:
                setContext(contexts::setSubSpaceContext, this::getSubSpaceContext);
                break;
        }
    }

    private static <T> void setContext(Consumer<T> setter, Supplier<T> getter) {
        setter.accept(getter.get());
    }

    public boolean shouldServe() {
        return active;
    }

    @Override
    public void close() {
        this.active = false;
    }

    private ApplicationContext getApplicationContext() {
        return this::close;
    }

    private ConsoleContext getConsoleContext() {
        return ConsoleContext.build(this::writeIndentedMessage, this::writeIndentedError, this::readMessage);
    }

    private void writeIndentedMessage(String message) {
        int numberOfSpace = buildInputMessage().length();
        writeMessage(" ".repeat(numberOfSpace) + message);
    }

    private void writeMessage(String message) {
        streams.out.println(message);
    }

    private void writeIndentedError(String errorMessage) {
        int numberOfSpace = buildInputMessage().length();
        writeError(" ".repeat(numberOfSpace) + errorMessage);
    }

    private void writeError(String errorMessage) {
        streams.err.println(errorMessage);
    }

    private String readMessage(final String message) {
        streams.out.print(message);
        return Input.readNextLine(streams.in);
    }

    private SubSpaceContext getSubSpaceContext() {
        return subSpaceContext;
    }

    private void openSubSpace(SubSpace space) {
        subSpaceTag = " [" + space.name() + "]";
    }

    private void closeSubSpace(SubSpace space) {
        subSpaceTag = "";
    }

    private Config getConfig() {
        try {
            InputStream configStream = Assistant.class.getClassLoader().getResourceAsStream("config.json");
            return new Config(configStream);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    public class StreamConfiguration {
        private PrintStream out;
        private PrintStream err;
        private InputStream in;
        private boolean applied;

        private StreamConfiguration() {
            out = System.out;
            err = System.out;
            in = System.in;
            applied = false;
        }

        public StreamConfiguration setOutput(final OutputStream outputStream) {
            return setOutput(new PrintStream(outputStream));
        }

        public StreamConfiguration setOutput(final PrintStream outputStream) {
            wasAlreadyApplied();
            this.out = outputStream;
            return this;
        }

        private void wasAlreadyApplied() {
            if (applied) {
                throw new IllegalStateException("StreamConfiguration was already applied!");
            }
        }

        public StreamConfiguration setError(final OutputStream errorStream) {
            return setError(new PrintStream(errorStream));
        }

        public StreamConfiguration setError(final PrintStream errorStream) {
            wasAlreadyApplied();
            this.err = errorStream;
            return this;
        }

        public StreamConfiguration setInput(final InputStream inpuStream) {
            wasAlreadyApplied();
            this.in = inpuStream;
            return this;
        }

        public void applyChanges() {
            applied = true;
            Assistant.this.streams = new ActiveStreams(out, err, in);
        }
    }
}

class ActiveStreams {
    final PrintStream out;
    final PrintStream err;
    final InputStream in;

    ActiveStreams(final PrintStream out, final PrintStream err, final InputStream in) {
        this.out = out;
        this.err = err;
        this.in = in;
    }
}