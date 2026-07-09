package com.xiaonuoclean.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public record CleanConfig(String language, int intervalSeconds, List<Integer> warningSeconds, List<String> whitelist) {
    public static final String DEFAULT_LANGUAGE = "zh-CN";
    public static final int DEFAULT_INTERVAL_SECONDS = 15 * 60;
    public static final List<Integer> DEFAULT_WARNING_SECONDS = List.of(10, 5, 4, 3, 2, 1);

    private static final Pattern LANGUAGE_PATTERN = Pattern.compile("[A-Za-z0-9_.-]+");
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    private static final Pattern PATH_PATTERN = Pattern.compile("[a-z0-9_./-]+");

    public static CleanConfig createDefault() {
        return new CleanConfig(DEFAULT_LANGUAGE, DEFAULT_INTERVAL_SECONDS, DEFAULT_WARNING_SECONDS, List.of());
    }

    public CleanConfig normalize() {
        String normalizedLanguage = normalizeLanguage(language);
        int normalizedIntervalSeconds = intervalSeconds > 0 ? intervalSeconds : DEFAULT_INTERVAL_SECONDS;
        List<Integer> normalizedWarnings = normalizeWarnings(normalizedIntervalSeconds, warningSeconds);
        List<String> normalizedWhitelist = normalizeWhitelist(whitelist);

        return new CleanConfig(normalizedLanguage, normalizedIntervalSeconds, normalizedWarnings, normalizedWhitelist);
    }

    public static String normalizeLanguage(String language) {
        if (language == null) {
            return DEFAULT_LANGUAGE;
        }

        String trimmedLanguage = language.trim();
        if (trimmedLanguage.isEmpty()
                || trimmedLanguage.contains("..")
                || !LANGUAGE_PATTERN.matcher(trimmedLanguage).matches()) {
            return DEFAULT_LANGUAGE;
        }

        return trimmedLanguage;
    }

    private static List<Integer> normalizeWarnings(int intervalSeconds, List<Integer> warnings) {
        Set<Integer> normalizedWarnings = new LinkedHashSet<>();

        if (warnings == null) {
            for (Integer warning : DEFAULT_WARNING_SECONDS) {
                if (warning < intervalSeconds) {
                    normalizedWarnings.add(warning);
                }
            }

            return List.copyOf(normalizedWarnings);
        }

        for (Integer warning : warnings) {
            if (warning != null && warning > 0 && warning < intervalSeconds) {
                normalizedWarnings.add(warning);
            }
        }

        return List.copyOf(normalizedWarnings);
    }

    private static List<String> normalizeWhitelist(List<String> whitelist) {
        Set<String> normalizedWhitelist = new LinkedHashSet<>();

        if (whitelist != null) {
            for (String itemId : whitelist) {
                String normalizedItemId = normalizeItemId(itemId);
                if (normalizedItemId != null) {
                    normalizedWhitelist.add(normalizedItemId);
                }
            }
        }

        return List.copyOf(normalizedWhitelist);
    }

    private static String normalizeItemId(String itemId) {
        if (itemId == null) {
            return null;
        }

        if (IDENTIFIER_PATTERN.matcher(itemId).matches()) {
            return itemId;
        }

        if (PATH_PATTERN.matcher(itemId).matches()) {
            return "minecraft:" + itemId;
        }

        return null;
    }

    public List<String> mutableWhitelist() {
        return new ArrayList<>(whitelist);
    }
}
