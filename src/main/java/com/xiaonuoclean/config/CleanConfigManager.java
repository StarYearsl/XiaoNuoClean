package com.xiaonuoclean.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xiaonuoclean.XiaoNuoClean;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class CleanConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configPath;
    private CleanConfig config = CleanConfig.createDefault();

    public CleanConfigManager(Path configPath) {
        this.configPath = configPath;
    }

    public CleanConfig load() {
        if (!Files.exists(configPath)) {
            config = CleanConfig.createDefault();
            save(config);
            return config;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            CleanConfig loadedConfig = GSON.fromJson(reader, CleanConfig.class);
            config = loadedConfig == null ? CleanConfig.createDefault() : loadedConfig.normalize();
            return config;
        } catch (IOException | RuntimeException exception) {
            XiaoNuoClean.LOGGER.warn("Failed to load XiaoNuoClean config, using defaults without overwriting the existing file", exception);
            config = CleanConfig.createDefault();
            return config;
        }
    }

    public void save(CleanConfig config) {
        this.config = config.normalize();

        try {
            Path parent = configPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(this.config, writer);
            }
        } catch (IOException exception) {
            XiaoNuoClean.LOGGER.error("Failed to save XiaoNuoClean config", exception);
        }
    }

    public CleanConfig config() {
        return config;
    }

    public Path configPath() {
        return configPath;
    }
}
