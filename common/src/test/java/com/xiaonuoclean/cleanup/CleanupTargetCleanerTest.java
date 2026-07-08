package com.xiaonuoclean.cleanup;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CleanupTargetCleanerTest {
    @Test
    void returnsTotalItemCountInsteadOfEntityCount() {
        List<CleanupTarget> targets = new ArrayList<>();
        List<Boolean> removed = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            int index = i;
            removed.add(false);
            targets.add(new CleanupTarget("minecraft:item_" + i, 64, () -> removed.set(index, true)));
        }

        int cleanedCount = new CleanupTargetCleaner().clean(targets, new CleanupWhitelist(List.of()));

        assertEquals(576, cleanedCount);
        assertTrue(removed.stream().allMatch(Boolean::booleanValue));
    }

    @Test
    void returnsTotalOfMixedStackCounts() {
        List<CleanupTarget> targets = List.of(
                new CleanupTarget("minecraft:dirt", 1, () -> { }),
                new CleanupTarget("minecraft:stone", 32, () -> { }),
                new CleanupTarget("minecraft:cobblestone", 64, () -> { })
        );

        int cleanedCount = new CleanupTargetCleaner().clean(targets, new CleanupWhitelist(List.of()));

        assertEquals(97, cleanedCount);
    }

    @Test
    void ignoresWhitelistedTargetsAndDoesNotRemoveThem() {
        List<Boolean> removed = new ArrayList<>(List.of(false, false));
        List<CleanupTarget> targets = List.of(
                new CleanupTarget("minecraft:diamond", 64, () -> removed.set(0, true)),
                new CleanupTarget("minecraft:dirt", 32, () -> removed.set(1, true))
        );

        int cleanedCount = new CleanupTargetCleaner().clean(targets, new CleanupWhitelist(List.of("minecraft:diamond")));

        assertEquals(32, cleanedCount);
        assertFalse(removed.get(0));
        assertTrue(removed.get(1));
    }
}
