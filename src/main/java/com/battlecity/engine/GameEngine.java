package com.battlecity.engine;

import com.battlecity.controller.GameController;
import com.battlecity.engine.loop.GameLoop;
import com.battlecity.engine.state.GameStateManager;
import com.battlecity.model.GameWorld;

/**
 * 游戏引擎核心，协调循环、状态与世界。
 */
public class GameEngine {

    private final GameContext context;
    private final GameController controller;
    private final GameStateManager stateManager;
    private final GameLoop loop;
    private GameWorld world;

    public GameEngine(GameContext context, GameController controller) {
        this.context = context;
        this.controller = controller;
        this.stateManager = new GameStateManager();
        this.loop = new GameLoop(this::tick, context.config().targetFps());
    }

    public void startClassicMode() {
        world = GameWorld.initialWorld(context.levelRepository().defaultClassic());
        stateManager.startClassic(world);
        controller.bindWorld(world);
        loop.start();
    }

    public void startEndlessMode() {
        world = GameWorld.initialWorld(context.levelRepository().endlessPrototype());
        stateManager.startEndless(world);
        controller.bindWorld(world);
        loop.start();
    }

    public void startTimedMode() {
        world = GameWorld.initialWorld(context.levelRepository().timedPrototype());
        stateManager.startTimed(world);
        controller.bindWorld(world);
        loop.start();
    }

    public void pause() {
        loop.pause();
        stateManager.pause();
    }

    public void resume() {
        loop.resume();
        stateManager.resume();
    }

    public boolean isPaused() {
        return stateManager.isPaused();
    }

    public void shutdown() {
        loop.stop();
    }

    private void tick(double deltaSeconds) {
        if (world == null) {
            return;
        }
        // GameController负责更新世界（包括碰撞检测）
        controller.update(deltaSeconds);
    }

    public GameWorld getWorld() {
        return world;
    }
}

