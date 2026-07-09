package com.xiaonuoclean.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XiaoNuoCleanPathsTest {
    @TempDir
    Path tempDir;

    @Test
    void resolvesConfigAndLanguageFilesUnderSingleModDirectory() {
        XiaoNuoCleanPaths paths = XiaoNuoCleanPaths.resolve(tempDir);

        assertEquals(tempDir.resolve("xiaonuoclean"), paths.configDirectory());
        assertEquals(tempDir.resolve("xiaonuoclean").resolve("config.json"), paths.configPath());
        assertEquals(tempDir.resolve("xiaonuoclean").resolve("lang"), paths.languageDirectory());
    }
}
