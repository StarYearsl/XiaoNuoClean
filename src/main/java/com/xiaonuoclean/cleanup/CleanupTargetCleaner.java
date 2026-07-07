package com.xiaonuoclean.cleanup;

import java.util.List;

public class CleanupTargetCleaner {
    public int clean(List<CleanupTarget> targets, CleanupWhitelist whitelist) {
        int cleanedCount = 0;

        for (CleanupTarget target : targets) {
            if (whitelist.shouldClean(target.itemId())) {
                cleanedCount += target.count();
                target.remove();
            }
        }

        return cleanedCount;
    }
}
