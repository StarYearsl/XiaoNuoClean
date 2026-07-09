package com.xiaonuoclean;

import com.xiaonuoclean.cleanup.CleanupScheduler;
import com.xiaonuoclean.cleanup.ItemCleanupService;
import com.xiaonuoclean.command.XiaoNuoCleanCommand;
import com.xiaonuoclean.config.CleanConfigManager;
import com.xiaonuoclean.config.XiaoNuoCleanPaths;
import com.xiaonuoclean.i18n.LanguageManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XiaoNuoClean implements ModInitializer {
	public static final String MOD_ID = "xiaonuoclean";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		XiaoNuoCleanPaths paths = XiaoNuoCleanPaths.resolve(FabricLoader.getInstance().getConfigDir());

		LanguageManager languageManager = new LanguageManager(paths.languageDirectory());
		languageManager.reload();

		CleanConfigManager configManager = new CleanConfigManager(paths.configPath());
		configManager.load();

		ItemCleanupService cleanupService = new ItemCleanupService();
		CleanupScheduler scheduler = new CleanupScheduler(configManager, cleanupService, languageManager);
		ServerTickEvents.END_SERVER_TICK.register(scheduler::tick);

		XiaoNuoCleanCommand command = new XiaoNuoCleanCommand(configManager, scheduler, languageManager);
		CommandRegistrationCallback.EVENT.register(command::register);

		LOGGER.info("XiaoNuoClean initialized. Config path: {}", paths.configPath());
	}
}
