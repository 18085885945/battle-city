package com.battlecity.engine;

import com.battlecity.controller.GameController;
import com.battlecity.engine.loop.GameLoop;
import com.battlecity.engine.state.GameStateManager;
import com.battlecity.map.LevelDefinition;
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
        startCustomLevel(context.levelRepository().defaultClassic());
    }

    public void startEndlessMode() {
        startCustomLevel(context.levelRepository().endlessPrototype());
    }

    public void startTimedMode() {
        startCustomLevel(context.levelRepository().timedPrototype());
    }

    public void startCustomLevel(LevelDefinition levelDefinition) {
        if (levelDefinition == null) {
            throw new IllegalArgumentException("关卡定义为空");
        }
        world = GameWorld.initialWorld(levelDefinition);
        // 自由选关按经典流程进入
        stateManager.startClassic(world);
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

