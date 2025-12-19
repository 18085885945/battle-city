package com.battlecity.config;

/**
 * 经典模式波次配置。
 */
public record EnemyWaveConfig(
        int waveIndex,
        int normalEnemies,
        int eliteEnemies
) {
}

