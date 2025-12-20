package com.battlecity.config;

import com.battlecity.engine.state.GameModeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GameConfigLoaderTest {

    @Test
    void shouldLoadDefaultConfig() {
        GameConfig config = new GameConfigLoader()
                .load("/config/game-config.json");

        assertEquals(60, config.targetFps());
        assertNotNull(config.player());
        assertEquals(3, config.player().maxLives());
        assertEquals(4, config.mode(GameModeType.CLASSIC).maxEnemyOnField());
    }
}

