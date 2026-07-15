package com.xiaonuoclean.cleanup;

import com.xiaonuoclean.config.CleanConfig;
import com.xiaonuoclean.config.CleanConfigManager;
import com.xiaonuoclean.i18n.LanguageManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class CleanupScheduler {
    private final CleanConfigManager configManager;
    private final ItemCleanupService cleanupService;
    private final LanguageManager languageManager;
    private CleanupCountdown countdown;

    public CleanupScheduler(CleanConfigManager configManager, ItemCleanupService cleanupService, LanguageManager languageManager) {
        this.configManager = configManager;
        this.cleanupService = cleanupService;
        this.languageManager = languageManager;
        this.countdown = new CleanupCountdown(configManager.config());
    }

    public void refresh() {
        this.countdown.reset(configManager.config());
    }

    public int remainingSeconds() {
        return countdown.remainingSeconds();
    }

    public int cleanNow(MinecraftServer server) {
        CleanConfig config = configManager.config();
        int cleanedCount = cleanupService.clean(server, config);
        broadcast(server, CleanupMessages.cleanedCount(languageManager, config.language(), cleanedCount));
        countdown.reset(configManager.config());
        return cleanedCount;
    }

    public void tick(MinecraftServer server) {
        CleanConfig config = configManager.config();
        if (!Boolean.TRUE.equals(config.enabled())) {
            return;
        }

        countdown.tick(event -> {
            if (event instanceof CleanupCountdown.Warning warning) {
                broadcast(server, CleanupMessages.warning(languageManager, config.language(), warning.remainingSeconds()));
            }

            if (event instanceof CleanupCountdown.Cleanup) {
                int cleanedCount = cleanupService.clean(server, config);
                broadcast(server, CleanupMessages.cleanedCount(languageManager, config.language(), cleanedCount));
            }
        });
    }

    private void broadcast(MinecraftServer server, String message) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }
}
