package com.battlecity.map;

import java.util.List;

/**
 * 单个关卡的完整描述。
 */
public record LevelDefinition(
        String id,
        String name,
        int width,
        int height,
        BaseDefinition base,
        List<ObstacleDefinition> obstacles,
        Integer timeLimitSeconds  // 可选的时间限制（秒），用于限时模式，null表示无限制
) {
    /**
     * 兼容旧代码的构造函数（时间限制为null）
     */
    public LevelDefinition(String id, String name, int width, int height, BaseDefinition base, List<ObstacleDefinition> obstacles) {
        this(id, name, width, height, base, obstacles, null);
    }
}

