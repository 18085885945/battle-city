package com.battlecity.config;

import com.battlecity.engine.state.GameModeType;

/**
 * 不同游戏模式的参数定义。
 */
public record ModeConfig(
        GameModeType modeType,
        boolean endless,
        int timeLimitSeconds,
        int maxEnemyOnField
) {
}

