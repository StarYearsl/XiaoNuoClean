package com.xiaonuoclean.cleanup;

public final class CleanupMessages {
    public static final String PREFIX = "[小诺清理]";

    private CleanupMessages() {
    }

    public static String warning(int remainingSeconds) {
        return PREFIX + " 将在 " + remainingSeconds + " 秒后清理掉落物。";
    }

    public static String cleanedCount(int count) {
        return PREFIX + " 本次共清理了 " + count + " 个掉落物。";
    }
}
