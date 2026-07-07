package com.xiaonuoclean;

import com.xiaonuoclean.cleanup.CleanupScheduler;
import com.xiaonuoclean.cleanup.ItemCleanupService;
import com.xiaonuoclean.command.XiaoNuoCleanCommand;
import com.xiaonuoclean.config.CleanConfigManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class XiaoNuoClean implements ModInitializer {
	public static final String MOD_ID = "xiaonuoclean";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
		CleanConfigManager configManager = new CleanConfigManager(configPath);
		configManager.load();

		ItemCleanupService cleanupService = new ItemCleanupService();
		CleanupScheduler scheduler = new CleanupScheduler(configManager, cleanupService);
		scheduler.register();

		new XiaoNuoCleanCommand(configManager, scheduler).register();

		LOGGER.info("XiaoNuoClean initialized. Config path: {}", configPath);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
