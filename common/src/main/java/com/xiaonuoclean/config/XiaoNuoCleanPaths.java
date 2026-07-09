package com.xiaonuoclean.config;

import java.nio.file.Path;

public record XiaoNuoCleanPaths(
        Path configDirectory,
        Path configPath,
        Path languageDirectory
) {
    private static final String MOD_ID = "xiaonuoclean";

    public static XiaoNuoCleanPaths resolve(Path configRoot) {
        Path configDirectory = configRoot.resolve(MOD_ID);
        return new XiaoNuoCleanPaths(
                configDirectory,
                configDirectory.resolve("config.json"),
                configDirectory.resolve("lang")
        );
    }
}
