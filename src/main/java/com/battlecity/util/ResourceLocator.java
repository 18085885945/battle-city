package com.battlecity.util;

import java.nio.file.Path;

/**
 * 统一管理资源路径，便于后续改为外部配置或用户目录。
 * 使用类路径资源路径（以/开头），可在JAR包中正常工作。
 */
public final class ResourceLocator {

    private ResourceLocator() {
    }

    /**
     * 返回配置文件在类路径中的路径（用于从JAR包中加载）
     * @return 类路径资源路径，如 "/config/game-config.json"
     */
    public static String defaultConfigPath() {
        return "/config/game-config.json";
    }

    /**
     * 返回关卡目录在类路径中的路径（用于从JAR包中加载）
     * @return 类路径资源路径，如 "/levels"
     */
    public static String levelsDirectory() {
        return "/levels";
    }

    /**
     * 返回关卡目录在文件系统中的路径（用于关卡编辑器保存文件）
     * @return 文件系统路径，如 "src/main/resources/levels"
     */
    public static Path levelsFileSystemPath() {
        return Path.of("src", "main", "resources", "levels");
    }
}

