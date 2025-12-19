package com.battlecity.config;

import com.battlecity.engine.state.GameModeType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 全局配置，包含渲染、模式、玩家与敌人默认参数。
 */
public record GameConfig(
        int targetFps,
        int virtualWidth,
        int virtualHeight,
        Map<GameModeType, ModeConfig> modeConfigs,
        PlayerConfig player,
        List<EnemyWaveConfig> enemyWaves
) {

    public GameConfig {
        if (modeConfigs == null || modeConfigs.isEmpty()) {
            throw new IllegalArgumentException("modeConfigs 不能为空");
        }
    }

    public ModeConfig mode(GameModeType type) {
        return Optional.ofNullable(modeConfigs.get(type))
                .orElseThrow(() -> new IllegalArgumentException("未配置模式: " + type));
    }

    public GameConfig withModeConfigs(Map<GameModeType, ModeConfig> newModes) {
        return new GameConfig(
                targetFps,
                virtualWidth,
                virtualHeight,
                new EnumMap<>(newModes),
                player,
                enemyWaves
        );
    }
}

