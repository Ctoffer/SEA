package de.ctoffer.meta;

import com.google.gson.JsonObject;

public class Exercise {
    private final String name;
    private final String alias;

    public Exercise(final String name, final String alias) {
        this.name = name;
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public static Exercise fromJson(final JsonObject object) {
        final String name = object.get("name").getAsString();
        final String alias = object.get("alias").getAsString();
        return new Exercise(name, alias);
    }
}
