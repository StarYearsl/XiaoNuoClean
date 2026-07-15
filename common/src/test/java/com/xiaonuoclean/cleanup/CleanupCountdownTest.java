package com.xiaonuoclean.cleanup;

import com.xiaonuoclean.config.CleanConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CleanupCountdownTest {
    @Test
    void startsAtConfiguredIntervalInTicks() {
        CleanupCountdown countdown = new CleanupCountdown(new CleanConfig("zh-CN", true, 30, List.of(10, 5), List.of()));

        assertEquals(600, countdown.remainingTicks());
        assertEquals(30, countdown.remainingSeconds());
    }

    @Test
    void emitsWarningsAtConfiguredRemainingSeconds() {
        CleanupCountdown countdown = new CleanupCountdown(new CleanConfig("zh-CN", true, 3, List.of(2, 1), List.of()));
        List<Integer> warnings = new ArrayList<>();

        for (int i = 0; i < 40; i++) {
            countdown.tick(event -> {
                if (event instanceof CleanupCountdown.Warning warning) {
                    warnings.add(warning.remainingSeconds());
                }
            });
        }

        assertEquals(List.of(2, 1), warnings);
    }

    @Test
    void emitsCleanupAndResetsWhenTimeReachesZero() {
        CleanupCountdown countdown = new CleanupCountdown(new CleanConfig("zh-CN", true, 1, List.of(), List.of()));
        List<String> events = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            countdown.tick(event -> events.add(event.getClass().getSimpleName()));
        }

        assertEquals(List.of("Cleanup"), events);
        assertEquals(20, countdown.remainingTicks());
    }

    @Test
    void supportsLargeIntervalsWithoutOverflowingTicks() {
        CleanupCountdown countdown = new CleanupCountdown(new CleanConfig("zh-CN", true, 200_000_000, List.of(), List.of()));

        assertEquals(4_000_000_000L, countdown.remainingTicks());
        assertEquals(200_000_000, countdown.remainingSeconds());
    }

    @Test
    void resetUsesLatestConfigAndClearsWarningHistory() {
        CleanupCountdown countdown = new CleanupCountdown(new CleanConfig("zh-CN", true, 10, List.of(5), List.of()));

        countdown.reset(new CleanConfig("zh-CN", true, 2, List.of(1), List.of()));

        assertEquals(40, countdown.remainingTicks());
        assertEquals(2, countdown.remainingSeconds());
    }
}
