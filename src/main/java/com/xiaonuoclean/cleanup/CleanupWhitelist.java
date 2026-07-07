package com.xiaonuoclean.cleanup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CleanupWhitelist {
    private final Set<String> itemIds;

    public CleanupWhitelist(List<String> itemIds) {
        this.itemIds = new HashSet<>(itemIds);
    }

    public boolean shouldClean(String itemId) {
        return !itemIds.contains(itemId);
    }
}
