package com.xiaonuoclean.cleanup;

import com.xiaonuoclean.config.CleanConfig;
import com.xiaonuoclean.config.CleanConfigManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class CleanupScheduler {
    private final CleanConfigManager configManager;
    private final ItemCleanupService cleanupService;
    private CleanupCountdown countdown;

    public CleanupScheduler(CleanConfigManager configManager, ItemCleanupService cleanupService) {
        this.configManager = configManager;
        this.cleanupService = cleanupService;
        this.countdown = new CleanupCountdown(configManager.config());
    }

    public void register() {
        ServerTickEvents.END_SERVER_TICK.register(this::tick);
    }

    public void refresh() {
        this.countdown.reset(configManager.config());
    }

    public int remainingSeconds() {
        return countdown.remainingSeconds();
    }

    public int cleanNow(MinecraftServer server) {
        int cleanedCount = cleanupService.clean(server, configManager.config());
        broadcast(server, CleanupMessages.cleanedCount(cleanedCount));
        countdown.reset(configManager.config());
        return cleanedCount;
    }

    private void tick(MinecraftServer server) {
        CleanConfig config = configManager.config();
        countdown.tick(event -> {
            if (event instanceof CleanupCountdown.Warning warning) {
                broadcast(server, CleanupMessages.warning(warning.remainingSeconds()));
            }

            if (event instanceof CleanupCountdown.Cleanup) {
                int cleanedCount = cleanupService.clean(server, config);
                broadcast(server, CleanupMessages.cleanedCount(cleanedCount));
            }
        });
    }

    private void broadcast(MinecraftServer server, String message) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }
}
