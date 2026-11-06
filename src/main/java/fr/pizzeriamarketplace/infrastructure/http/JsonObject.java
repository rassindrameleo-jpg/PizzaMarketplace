package fr.pizzeriamarketplace.infrastructure.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON reader for flat request bodies: string, number and boolean fields,
 * plus arrays of strings (e.g. the ingredients of a custom pizza).
 */
final class JsonObject {
    private final Map<String, Object> values;

    private JsonObject(Map<String, Object> values) {
        this.values = values;
    }

    static JsonObject parse(String body) {
        return new Parser(body).parseObject();
    }

    String requiredString(String name) {
        Object value = values.get(name);
        if (value instanceof List<?>) {
            throw new ApiException(400, "Field must not be an array: " + name);
        }
        if (value == null || ((String) value).isBlank()) {
            throw new ApiException(400, "Missing field: " + name);
        }
        return (String) value;
    }

    int requiredInt(String name) {
        try {
            return Integer.parseInt(requiredString(name));
        } catch (NumberFormatException exception) {
            throw new ApiException(400, "Field must be an integer: " + name);
        }
    }

    List<String> requiredStringList(String name) {
        Object value = values.get(name);
        if (value == null) {
            throw new ApiException(400, "Missing field: " + name);
        }
        if (!(value instanceof List<?> list)) {
            throw new ApiException(400, "Field must be an array: " + name);
        }
        return list.stream().map(String.class::cast).toList();
    }

    private static final class Parser {
        private final String json;
        private int position;

        private Parser(String json) {
            this.json = json;
        }

        private JsonObject parseObject() {
            Map<String, Object> values = new HashMap<>();
            skipWhitespace();
            expect('{', "Request body must be a JSON object");
            skipWhitespace();
            if (peek() == '}') {
                position++;
            } else {
                do {
                    skipWhitespace();
                    String key = parseString();
                    skipWhitespace();
                    expect(':', "Invalid JSON field");
                    skipWhitespace();
                    values.put(key, parseValue());
                    skipWhitespace();
                } while (consume(','));
                expect('}', "Invalid JSON field");
            }
            skipWhitespace();
            if (position != json.length()) {
                throw new ApiException(400, "Request body must be a JSON object");
            }
            return new JsonObject(values);
        }

        private Object parseValue() {
            char current = peek();
            if (current == '"') {
                return parseString();
            }
            if (current == '[') {
                return parseStringArray();
            }
            return parseLiteral();
        }

        private List<String> parseStringArray() {
            expect('[', "Invalid JSON array");
            List<String> items = new ArrayList<>();
            skipWhitespace();
            if (consume(']')) {
                return items;
            }
            do {
                skipWhitespace();
                if (peek() != '"') {
                    throw new ApiException(400, "JSON arrays must contain strings");
                }
                items.add(parseString());
                skipWhitespace();
            } while (consume(','));
            expect(']', "Invalid JSON array");
            return items;
        }

        private String parseString() {
            expect('"', "JSON strings must use double quotes");
            StringBuilder value = new StringBuilder();
            while (position < json.length()) {
                char current = json.charAt(position++);
                if (current == '"') {
                    return value.toString();
                }
                if (current == '\\' && position < json.length()) {
                    value.append(json.charAt(position++));
                } else {
                    value.append(current);
                }
            }
            throw new ApiException(400, "Unterminated JSON string");
        }

        private String parseLiteral() {
            int start = position;
            while (position < json.length() && ",}] \t\r\n".indexOf(json.charAt(position)) < 0) {
                position++;
            }
            if (start == position) {
                throw new ApiException(400, "Invalid JSON field");
            }
            return json.substring(start, position);
        }

        private char peek() {
            if (position >= json.length()) {
                throw new ApiException(400, "Unexpected end of JSON");
            }
            return json.charAt(position);
        }

        private boolean consume(char expected) {
            if (position < json.length() && json.charAt(position) == expected) {
                position++;
                return true;
            }
            return false;
        }

        private void expect(char expected, String message) {
            if (!consume(expected)) {
                throw new ApiException(400, message);
            }
        }

        private void skipWhitespace() {
            while (position < json.length() && Character.isWhitespace(json.charAt(position))) {
                position++;
            }
        }
    }
}
