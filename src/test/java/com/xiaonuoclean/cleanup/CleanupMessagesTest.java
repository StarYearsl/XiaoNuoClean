package com.xiaonuoclean.cleanup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CleanupMessagesTest {
    @Test
    void formatsWarningMessage() {
        assertEquals("[小诺清理] 将在 10 秒后清理掉落物。", CleanupMessages.warning(10));
    }

    @Test
    void formatsCleanedCountMessage() {
        assertEquals("[小诺清理] 本次共清理了 23 个掉落物。", CleanupMessages.cleanedCount(23));
    }
}
