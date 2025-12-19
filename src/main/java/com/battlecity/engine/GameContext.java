package com.battlecity.engine;

import com.battlecity.config.GameConfig;
import com.battlecity.map.LevelRepository;

/**
 * 在各系统之间共享的上下文对象。
 */
public class GameContext {
    private final GameConfig config;
    private final LevelRepository levelRepository;

    public GameContext(GameConfig config, LevelRepository levelRepository) {
        this.config = config;
        this.levelRepository = levelRepository;
    }

    public GameConfig config() {
        return config;
    }

    public LevelRepository levelRepository() {
        return levelRepository;
    }
}

