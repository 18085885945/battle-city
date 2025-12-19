package com.battlecity.config;

/**
 * 玩家默认属性配置。
 */
public record PlayerConfig(
        int maxLives,
        double baseSpeed,
        double fireCooldownMillis,
        double armor
) {
}

