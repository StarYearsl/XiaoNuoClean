package com.xiaonuoclean.i18n;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageManagerTest {
    @TempDir
    Path tempDir;

    @Test
    void seedsBundledLanguageFilesWithoutOverwritingExistingFiles() throws Exception {
        Path languageDirectory = tempDir.resolve("lang");
        LanguageManager manager = new LanguageManager(languageDirectory);

        manager.seedDefaultFiles();

        Path zhCn = languageDirectory.resolve("zh-CN.json");
        Path enUs = languageDirectory.resolve("en-US.json");
        assertTrue(Files.exists(zhCn));
        assertTrue(Files.exists(enUs));

        String customizedContent = """
                {
                  "cleanup.cleaned": "custom"
                }
                """;
        Files.writeString(zhCn, customizedContent);

        manager.seedDefaultFiles();

        assertEquals(customizedContent, Files.readString(zhCn));
    }

    @Test
    void loadsEnglishLanguageAndFormatsNamedPlaceholders() {
        LanguageManager manager = new LanguageManager(tempDir.resolve("lang"));

        String message = manager.translate("en-US", "cleanup.cleaned", Map.of("count", 23));

        assertEquals("[XiaoNuoClean] Cleaned 23 dropped items.", message);
    }

    @Test
    void loadsCustomExternalLanguageFileByLanguageName() throws Exception {
        Path languageDirectory = tempDir.resolve("lang");
        Files.createDirectories(languageDirectory);
        Files.writeString(languageDirectory.resolve("pirate.json"), """
                {
                  "cleanup.cleaned": "Cleaned {count} shiny things."
                }
                """);
        LanguageManager manager = new LanguageManager(languageDirectory);

        String message = manager.translate("pirate", "cleanup.cleaned", Map.of("count", 7));

        assertEquals("Cleaned 7 shiny things.", message);
    }

    @Test
    void fallsBackToChineseWhenSelectedLanguageIsMissing() {
        LanguageManager manager = new LanguageManager(tempDir.resolve("lang"));

        String message = manager.translate("missing", "cleanup.cleaned", Map.of("count", 4));

        assertEquals("[小诺清理] 本次共清理了 4 个掉落物。", message);
    }

    @Test
    void fallsBackToChineseKeyWhenSelectedLanguageMissesKey() throws Exception {
        Path languageDirectory = tempDir.resolve("lang");
        Files.createDirectories(languageDirectory);
        Files.writeString(languageDirectory.resolve("partial.json"), """
                {
                  "cleanup.warning": "soon"
                }
                """);
        LanguageManager manager = new LanguageManager(languageDirectory);

        String message = manager.translate("partial", "cleanup.cleaned", Map.of("count", 2));

        assertEquals("[小诺清理] 本次共清理了 2 个掉落物。", message);
    }

    @Test
    void returnsKeyWhenNoTranslationExists() {
        LanguageManager manager = new LanguageManager(tempDir.resolve("lang"));

        String message = manager.translate("zh-CN", "unknown.key", Map.of());

        assertEquals("unknown.key", message);
    }
}
