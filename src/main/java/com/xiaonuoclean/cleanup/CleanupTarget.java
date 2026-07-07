package com.xiaonuoclean.cleanup;

public record CleanupTarget(String itemId, int count, Runnable removeAction) {
    public void remove() {
        removeAction.run();
    }
}
