package com.xiaonuoclean.cleanup;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CleanupWhitelistTest {
    @Test
    void allowsCleanupWhenItemIsNotWhitelisted() {
        CleanupWhitelist whitelist = new CleanupWhitelist(List.of("minecraft:diamond"));

        assertTrue(whitelist.shouldClean("minecraft:dirt"));
    }

    @Test
    void preventsCleanupWhenItemIsWhitelisted() {
        CleanupWhitelist whitelist = new CleanupWhitelist(List.of("minecraft:diamond"));

        assertFalse(whitelist.shouldClean("minecraft:diamond"));
    }
}
