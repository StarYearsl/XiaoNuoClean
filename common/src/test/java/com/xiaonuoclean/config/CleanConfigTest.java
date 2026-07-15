package com.xiaonuoclean.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CleanConfigTest {
    @Test
    void createDefaultUsesFifteenMinuteIntervalAndDefaultWarnings() {
        CleanConfig config = CleanConfig.createDefault();

        assertEquals("zh-CN", config.language());
        assertEquals(true, config.enabled());
        assertEquals(900, config.intervalSeconds());
        assertEquals(List.of(10, 5, 4, 3, 2, 1), config.warningSeconds());
        assertEquals(List.of(), config.whitelist());
    }

    @Test
    void normalizeReplacesInvalidIntervalAndFiltersWarningsAndWhitelist() {
        CleanConfig config = new CleanConfig(
                "en-US",
                false,
                -1,
                List.of(30, 10, 10, 0, -3, 900),
                List.of("minecraft:diamond", "bad id", "minecraft:diamond", "xiaonuoclean:kept_item")
        );

        CleanConfig normalized = config.normalize();

        assertEquals(900, normalized.intervalSeconds());
        assertEquals("en-US", normalized.language());
        assertEquals(false, normalized.enabled());
        assertEquals(List.of(30, 10), normalized.warningSeconds());
        assertEquals(List.of("minecraft:diamond", "xiaonuoclean:kept_item"), normalized.whitelist());
    }

    @Test
    void normalizeUsesDefaultWarningsWhenWarningsAreMissing() {
        CleanConfig config = new CleanConfig("zh-CN", true, 5, null, List.of());

        CleanConfig normalized = config.normalize();

        assertEquals(5, normalized.intervalSeconds());
        assertEquals(List.of(4, 3, 2, 1), normalized.warningSeconds());
    }

    @Test
    void normalizeKeepsExplicitEmptyWarningsToDisableCountdownBroadcasts() {
        CleanConfig config = new CleanConfig("zh-CN", true, 5, List.of(), List.of());

        CleanConfig normalized = config.normalize();

        assertEquals(5, normalized.intervalSeconds());
        assertEquals(List.of(), normalized.warningSeconds());
    }

    @Test
    void normalizeConvertsShorthandWhitelistItemsToMinecraftNamespace() {
        CleanConfig config = new CleanConfig("zh-CN", true, 900, List.of(10), List.of("diamond", "minecraft:netherite_ingot"));

        CleanConfig normalized = config.normalize();

        assertEquals(List.of("minecraft:diamond", "minecraft:netherite_ingot"), normalized.whitelist());
    }

    @Test
    void normalizeUsesDefaultLanguageWhenLanguageIsMissingOrBlank() {
        assertEquals("zh-CN", new CleanConfig(null, true, 900, List.of(10), List.of()).normalize().language());
        assertEquals("zh-CN", new CleanConfig("   ", true, 900, List.of(10), List.of()).normalize().language());
    }

    @Test
    void normalizeRejectsPathLikeLanguageValues() {
        assertEquals("zh-CN", new CleanConfig("../en-US", true, 900, List.of(10), List.of()).normalize().language());
        assertEquals("zh-CN", new CleanConfig("lang\\en-US", true, 900, List.of(10), List.of()).normalize().language());
    }

    @Test
    void normalizeDefaultsMissingEnabledValueToTrueAndPreservesFalse() {
        assertEquals(true, new CleanConfig("zh-CN", null, 900, List.of(10), List.of()).normalize().enabled());
        assertEquals(false, new CleanConfig("zh-CN", false, 900, List.of(10), List.of()).normalize().enabled());
    }

    @Test
    void configUpdatesPreserveDisabledState() {
        CleanConfig config = new CleanConfig("en-US", false, 900, List.of(10), List.of("minecraft:diamond")).normalize();

        assertEquals(false, config.withIntervalSeconds(1800).enabled());
        assertEquals(false, config.withWarningSeconds(List.of(30)).enabled());
        assertEquals(false, config.withWhitelist(List.of("minecraft:emerald")).enabled());
        assertEquals(true, config.withEnabled(true).enabled());
    }
}
