package com.xiaonuoclean.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CleanConfigManagerTest {
    @TempDir
    Path tempDir;

    @Test
    void loadCreatesDefaultConfigWhenFileDoesNotExist() throws Exception {
        Path configPath = tempDir.resolve("xiaonuoclean.json");
        CleanConfigManager manager = new CleanConfigManager(configPath);

        CleanConfig config = manager.load();

        assertEquals(CleanConfig.createDefault(), config);
        assertTrue(Files.exists(configPath));
        String content = Files.readString(configPath);
        assertTrue(content.contains("\"language\": \"zh-CN\""));
        assertTrue(content.contains("\"intervalSeconds\": 900"));
    }

    @Test
    void loadNormalizesExistingConfigWithoutRewritingUserFile() throws Exception {
        Path configPath = tempDir.resolve("xiaonuoclean.json");
        String originalContent = """
                {
                  "intervalSeconds": 4,
                  "warningSeconds": [4, 3, 0, 2],
                  "whitelist": ["minecraft:diamond", "bad id", "minecraft:diamond"],
                  "futureField": true
                }
                """;
        Files.writeString(configPath, originalContent);
        CleanConfigManager manager = new CleanConfigManager(configPath);

        CleanConfig config = manager.load();

        assertEquals("zh-CN", config.language());
        assertEquals(4, config.intervalSeconds());
        assertEquals(List.of(3, 2), config.warningSeconds());
        assertEquals(List.of("minecraft:diamond"), config.whitelist());
        assertEquals(originalContent, Files.readString(configPath));
    }

    @Test
    void malformedConfigUsesDefaultsWithoutOverwritingUserFile() throws Exception {
        Path configPath = tempDir.resolve("xiaonuoclean.json");
        String brokenContent = "{ this is not valid json";
        Files.writeString(configPath, brokenContent);
        CleanConfigManager manager = new CleanConfigManager(configPath);

        CleanConfig config = manager.load();

        assertEquals(CleanConfig.createDefault(), config);
        assertEquals(brokenContent, Files.readString(configPath));
    }

    @Test
    void savePersistsNormalizedConfig() throws Exception {
        Path configPath = tempDir.resolve("nested").resolve("xiaonuoclean.json");
        CleanConfigManager manager = new CleanConfigManager(configPath);

        manager.save(new CleanConfig("en-US", 30, List.of(10, 5), List.of("minecraft:diamond")));
        CleanConfig loaded = manager.load();

        assertEquals("en-US", loaded.language());
        assertEquals(30, loaded.intervalSeconds());
        assertEquals(List.of(10, 5), loaded.warningSeconds());
        assertEquals(List.of("minecraft:diamond"), loaded.whitelist());
    }
}
