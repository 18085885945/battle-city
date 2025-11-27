package com.battlecity.map;

import java.util.List;

/**
 * 关卡定义
 * 包含关卡的基本信息和障碍物配置
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

