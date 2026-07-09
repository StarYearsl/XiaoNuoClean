package com.xiaonuoclean.neoforge;

import com.xiaonuoclean.cleanup.CleanupScheduler;
import com.xiaonuoclean.cleanup.ItemCleanupService;
import com.xiaonuoclean.command.XiaoNuoCleanCommand;
import com.xiaonuoclean.config.CleanConfigManager;
import com.xiaonuoclean.config.XiaoNuoCleanPaths;
import com.xiaonuoclean.i18n.LanguageManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(XiaoNuoCleanNeoForge.MOD_ID)
public class XiaoNuoCleanNeoForge {
    public static final String MOD_ID = "xiaonuoclean";

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final CleanupScheduler scheduler;
    private final XiaoNuoCleanCommand command;

    public XiaoNuoCleanNeoForge(IEventBus modBus, ModContainer modContainer) {
        XiaoNuoCleanPaths paths = XiaoNuoCleanPaths.resolve(FMLPaths.CONFIGDIR.get());

        LanguageManager languageManager = new LanguageManager(paths.languageDirectory());
        languageManager.reload();

        CleanConfigManager configManager = new CleanConfigManager(paths.configPath());
        configManager.load();

        ItemCleanupService cleanupService = new ItemCleanupService();
        this.scheduler = new CleanupScheduler(configManager, cleanupService, languageManager);
        this.command = new XiaoNuoCleanCommand(configManager, scheduler, languageManager);

        NeoForge.EVENT_BUS.register(this);

        LOGGER.debug("NeoForge mod bus: {}, container: {}", modBus, modContainer);
        LOGGER.info("XiaoNuoClean initialized for NeoForge. Config path: {}", paths.configPath());
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        command.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    @SubscribeEvent
    public void tick(ServerTickEvent.Post event) {
        scheduler.tick(event.getServer());
    }
}
