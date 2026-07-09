package com.xiaonuoclean.cleanup;

import com.xiaonuoclean.i18n.LanguageManager;

import java.util.Map;

public final class CleanupMessages {
    private CleanupMessages() {
    }

    public static String warning(LanguageManager languageManager, String language, int remainingSeconds) {
        return languageManager.translate(
                language,
                "cleanup.warning",
                Map.of("seconds", remainingSeconds)
        );
    }

    public static String cleanedCount(LanguageManager languageManager, String language, int count) {
        return languageManager.translate(
                language,
                "cleanup.cleaned",
                Map.of("count", count)
        );
    }
}
