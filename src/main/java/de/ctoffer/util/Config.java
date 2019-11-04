package de.ctoffer.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Config {
    private JsonObject object;

    public Config(Path path) throws IOException {
        this(Files.newInputStream(path));
    }

    public Config(final InputStream input) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(input)) {
            object = JsonParser.parseReader(reader).getAsJsonObject();
        }
        input.close();
    }

    public Config(final JsonObject obj) {
        this.object = obj;
    }

    public String getString(String path) {
        String[] parts = path.split("/");
        if (parts.length == 1) {
            return object.get(parts[0]).getAsString();
        } else {
            String[] subArray = Arrays.asList(parts).subList(0, parts.length - 1).toArray(new String[]{});
            return sub(subArray).object.get(parts[parts.length - 1]).getAsString();
        }
    }

    public JsonArray getList(String path) {
        String[] parts = path.split("/");
        JsonArray result;

        if (parts.length == 1) {
            result = object.get(parts[0]).getAsJsonArray();
        } else {
            String[] subArray = Arrays.asList(parts).subList(0, parts.length - 1).toArray(new String[]{});
            result = sub(subArray).object.get(parts[parts.length - 1]).getAsJsonArray();
        }

        return result;
    }

    public JsonObject getObject(String path) {
        return sub(path).object;
    }

    public Config sub(String path) {
        return sub(path.split("/"));
    }

    public Config sub(String... path) {
        JsonObject result = object;
        for (String name : path) {
            result = result.getAsJsonObject(name);
        }
        return new Config(result);
    }
}
