package com.xiaonuoclean.i18n;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.xiaonuoclean.config.CleanConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LanguageManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageManager.class);
    private static final Gson GSON = new Gson();
    private static final Type LANGUAGE_FILE_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();
    private static final String RESOURCE_ROOT = "assets/xiaonuoclean/lang/";
    private static final List<String> DEFAULT_LANGUAGE_FILES = List.of("zh-CN", "en-US");

    private final Path languageDirectory;
    private final Map<String, Map<String, String>> cache = new HashMap<>();

    public LanguageManager(Path languageDirectory) {
        this.languageDirectory = Objects.requireNonNull(languageDirectory, "languageDirectory");
    }

    public void reload() {
        cache.clear();
        seedDefaultFiles();
    }

    public void seedDefaultFiles() {
        try {
            Files.createDirectories(languageDirectory);
        } catch (IOException exception) {
            LOGGER.warn("Failed to create XiaoNuoClean language directory: {}", languageDirectory, exception);
            return;
        }

        for (String language : DEFAULT_LANGUAGE_FILES) {
            Path target = languageDirectory.resolve(language + ".json");
            if (Files.exists(target)) {
                continue;
            }

            try (InputStream input = openBundledLanguage(language)) {
                if (input == null) {
                    LOGGER.warn("Bundled XiaoNuoClean language file is missing: {}", language);
                    continue;
                }

                Files.copy(input, target);
            } catch (IOException exception) {
                LOGGER.warn("Failed to seed XiaoNuoClean language file: {}", target, exception);
            }
        }
    }

    public String translate(String language, String key) {
        return translate(language, key, Map.of());
    }

    public String translate(String language, String key, Map<String, ?> placeholders) {
        String normalizedLanguage = CleanConfig.normalizeLanguage(language);
        String template = loadLanguage(normalizedLanguage).get(key);

        if (template == null && !CleanConfig.DEFAULT_LANGUAGE.equals(normalizedLanguage)) {
            template = loadLanguage(CleanConfig.DEFAULT_LANGUAGE).get(key);
        }

        if (template == null) {
            return key;
        }

        return format(template, placeholders);
    }

    private Map<String, String> loadLanguage(String language) {
        return cache.computeIfAbsent(language, this::loadLanguageFile);
    }

    private Map<String, String> loadLanguageFile(String language) {
        Map<String, String> external = loadExternalLanguage(language);
        if (!external.isEmpty()) {
            return external;
        }

        return loadBundledLanguage(language);
    }

    private Map<String, String> loadExternalLanguage(String language) {
        Path languageFile = languageDirectory.resolve(language + ".json");
        if (!Files.exists(languageFile)) {
            return Map.of();
        }

        try (Reader reader = Files.newBufferedReader(languageFile)) {
            Map<String, String> loaded = GSON.fromJson(reader, LANGUAGE_FILE_TYPE);
            return sanitizeTranslations(loaded);
        } catch (IOException | JsonSyntaxException exception) {
            LOGGER.warn("Failed to load XiaoNuoClean language file: {}", languageFile, exception);
            return Map.of();
        }
    }

    private Map<String, String> loadBundledLanguage(String language) {
        try (InputStream input = openBundledLanguage(language)) {
            if (input == null) {
                return Map.of();
            }

            try (Reader reader = new java.io.InputStreamReader(input, java.nio.charset.StandardCharsets.UTF_8)) {
                Map<String, String> loaded = GSON.fromJson(reader, LANGUAGE_FILE_TYPE);
                return sanitizeTranslations(loaded);
            }
        } catch (IOException | JsonSyntaxException exception) {
            LOGGER.warn("Failed to load bundled XiaoNuoClean language file: {}", language, exception);
            return Map.of();
        }
    }

    private InputStream openBundledLanguage(String language) {
        return LanguageManager.class.getClassLoader().getResourceAsStream(RESOURCE_ROOT + language + ".json");
    }

    private Map<String, String> sanitizeTranslations(Map<String, String> loaded) {
        if (loaded == null) {
            return Map.of();
        }

        Map<String, String> sanitized = new HashMap<>();
        for (Map.Entry<String, String> entry : loaded.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                sanitized.put(entry.getKey(), entry.getValue());
            }
        }

        return Map.copyOf(sanitized);
    }

    private String format(String template, Map<String, ?> placeholders) {
        String formatted = template;
        for (Map.Entry<String, ?> placeholder : placeholders.entrySet()) {
            formatted = formatted.replace("{" + placeholder.getKey() + "}", String.valueOf(placeholder.getValue()));
        }

        return formatted;
    }
}
