package model.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JsonUtils {

    private JsonUtils() {
        // Private constructor to prevent instantiation
    }

    public static String listToJson(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        return "[" + list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }

    public static List<Integer> jsonToList(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("[]")) {
            return new ArrayList<>();
        }

        String content = json.replaceAll("[\\[\\]]", "").trim();

        if (content.isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(content.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .toList();
    }
}
