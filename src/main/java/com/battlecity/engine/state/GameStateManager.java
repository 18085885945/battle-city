package com.battlecity.engine.state;

import com.battlecity.model.GameWorld;

/**
 * 管理状态流转。
 */
public class GameStateManager {
    private GameState current;

    public void startClassic(GameWorld world) {
        current = new GameState(GameModeType.CLASSIC, world);
    }

    public void startEndless(GameWorld world) {
        current = new GameState(GameModeType.ENDLESS, world);
    }

    public void startTimed(GameWorld world) {
        current = new GameState(GameModeType.TIMED, world);
    }

    public void pause() {
        if (current != null) {
            current.pause();
        }
    }

    public void resume() {
        if (current != null) {
            current.resume();
        }
    }

    public GameState current() {
        return current;
    }

    public boolean isPaused() {
        return current != null && current.isPaused();
    }
}

