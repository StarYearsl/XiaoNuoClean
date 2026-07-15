package com.xiaonuoclean.cleanup;

import com.xiaonuoclean.config.CleanConfig;
import com.xiaonuoclean.config.CleanConfigManager;
import com.xiaonuoclean.i18n.LanguageManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CleanupSchedulerTest {
    @TempDir
    Path tempDir;

    @Test
    void disabledAutomaticCleanupDoesNotAdvanceCountdown() {
        CleanConfigManager configManager = new CleanConfigManager(tempDir.resolve("config.json"));
        configManager.save(new CleanConfig("zh-CN", false, 3, List.of(), List.of()));
        CleanupScheduler scheduler = new CleanupScheduler(
                configManager,
                new ItemCleanupService(),
                new LanguageManager(tempDir.resolve("lang"))
        );

        scheduler.tick(null);

        assertEquals(3, scheduler.remainingSeconds());
    }
}
