package com.battlecity.engine.state;

import com.battlecity.model.GameWorld;

/**
 * 游戏状态聚合，包含当前模式、关卡和时间。
 */
public class GameState {
    private final GameModeType mode;
    private final GameWorld world;
    private double elapsedSeconds;
    private boolean paused;

    public GameState(GameModeType mode, GameWorld world) {
        this.mode = mode;
        this.world = world;
    }

    public void update(double deltaSeconds) {
        if (!paused) {
            elapsedSeconds += deltaSeconds;
        }
    }

    public GameModeType mode() {
        return mode;
    }

    public GameWorld world() {
        return world;
    }

    public double elapsedSeconds() {
        return elapsedSeconds;
    }

    public void pause() {
        this.paused = true;
    }

    public void resume() {
        this.paused = false;
    }

    public boolean isPaused() {
        return paused;
    }
}

