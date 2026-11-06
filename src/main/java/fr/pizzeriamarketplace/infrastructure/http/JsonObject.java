package fr.pizzeriamarketplace.infrastructure.http;

import java.util.HashMap;
import java.util.Map;

final class JsonObject {
    private final Map<String, String> values;

    private JsonObject(Map<String, String> values) {
        this.values = values;
    }

    static JsonObject parse(String body) {
        String json = body.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new ApiException(400, "Request body must be a JSON object");
        }

        Map<String, String> values = new HashMap<>();
        String content = json.substring(1, json.length() - 1).trim();
        if (content.isEmpty()) {
            return new JsonObject(values);
        }

        for (String entry : content.split(",")) {
            String[] pair = entry.split(":", 2);
            if (pair.length != 2) {
                throw new ApiException(400, "Invalid JSON field");
            }
            String key = unquote(pair[0].trim());
            String value = pair[1].trim();
            values.put(key, value.startsWith("\"") ? unquote(value) : value);
        }
        return new JsonObject(values);
    }

    String requiredString(String name) {
        String value = values.get(name);
        if (value == null || value.isBlank()) {
            throw new ApiException(400, "Missing field: " + name);
        }
        return value;
    }

    int requiredInt(String name) {
        try {
            return Integer.parseInt(requiredString(name));
        } catch (NumberFormatException exception) {
            throw new ApiException(400, "Field must be an integer: " + name);
        }
    }

    private static String unquote(String value) {
        if (value.length() < 2 || value.charAt(0) != '"' || value.charAt(value.length() - 1) != '"') {
            throw new ApiException(400, "JSON strings must use double quotes");
        }
        return value.substring(1, value.length() - 1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
