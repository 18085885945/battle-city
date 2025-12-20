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
        Integer timeLimitSeconds,  // 可选的时间限制（秒），用于限时模式，null表示无限制
        Double enemySpawnInterval,  // 敌人刷新间隔（秒），null表示使用默认值5.0
        Double eliteSpawnRate  // 精英怪出现频率（0.0-1.0），null表示使用默认值0.2（20%）
) {
    /**
     * 兼容旧代码的构造函数（时间限制、刷新间隔、精英频率为null）
     */
    public LevelDefinition(String id, String name, int width, int height, BaseDefinition base, List<ObstacleDefinition> obstacles) {
        this(id, name, width, height, base, obstacles, null, null, null);
    }
    
    /**
     * 兼容旧代码的构造函数（刷新间隔、精英频率为null）
     */
    public LevelDefinition(String id, String name, int width, int height, BaseDefinition base, List<ObstacleDefinition> obstacles, Integer timeLimitSeconds) {
        this(id, name, width, height, base, obstacles, timeLimitSeconds, null, null);
    }
}

