package com.battlecity.map;

/**
 * 用于从 JSON 描述单个地图元素。
 */
public record ObstacleDefinition(
        TileType type,
        double x,
        double y
) {
}

