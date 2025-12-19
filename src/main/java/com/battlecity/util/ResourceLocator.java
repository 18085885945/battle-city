package com.battlecity.util;

import java.nio.file.Path;

/**
 * 统一管理资源路径，便于后续改为外部配置或用户目录。
 */
public final class ResourceLocator {

    private ResourceLocator() {
    }

    public static Path defaultConfigPath() {
        return Path.of("src", "main", "resources", "config", "game-config.json");
    }

    public static Path levelsDirectory() {
        return Path.of("src", "main", "resources", "levels");
    }
}

