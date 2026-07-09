package com.xiaonuoclean.cleanup;

import com.xiaonuoclean.i18n.LanguageManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CleanupMessagesTest {
    @TempDir
    Path tempDir;

    @Test
    void formatsChineseWarningMessage() {
        LanguageManager languageManager = new LanguageManager(tempDir.resolve("lang"));

        assertEquals("[小诺清理] 将在 10 秒后清理掉落物。", CleanupMessages.warning(languageManager, "zh-CN", 10));
    }

    @Test
    void formatsEnglishCleanedCountMessage() {
        LanguageManager languageManager = new LanguageManager(tempDir.resolve("lang"));

        assertEquals("[XiaoNuoClean] Cleaned 23 dropped items.", CleanupMessages.cleanedCount(languageManager, "en-US", 23));
    }
}
