package com.rhuloe.cbccryocannon.logic;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CleansingMappings {
    private CleansingMappings() {}

    public static Map<String, String> parse(Iterable<String> entries) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String entry : entries) {
            String[] pair = entry.split("\\|", -1);
            if (pair.length == 2 && !pair[0].isBlank() && !pair[1].isBlank()) {
                result.put(pair[0].trim(), pair[1].trim());
            }
        }
        return Map.copyOf(result);
    }
}