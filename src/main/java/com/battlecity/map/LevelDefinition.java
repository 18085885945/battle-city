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
        List<ObstacleDefinition> obstacles
) {
}

