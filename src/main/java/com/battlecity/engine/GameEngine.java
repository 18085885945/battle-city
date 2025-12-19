package com.battlecity.engine;

import com.battlecity.controller.GameController;
import com.battlecity.engine.loop.GameLoop;
import com.battlecity.engine.state.GameState;
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
        // 播放游戏开始音效
        com.battlecity.audio.AudioManager.getInstance().playSound("game_start");
    }

    public void startEndlessMode() {
        LevelDefinition levelDef = context.levelRepository().endlessPrototype();
        if (levelDef != null) {
            world = GameWorld.initialWorld(levelDef);
            stateManager.startEndless(world);
            controller.bindWorld(world);
            loop.start();
            // 播放游戏开始音效
            com.battlecity.audio.AudioManager.getInstance().playSound("game_start");
        }
    }

    public void startTimedMode() {
        LevelDefinition levelDef = context.levelRepository().timedPrototype();
        if (levelDef != null) {
            world = GameWorld.initialWorld(levelDef);
            stateManager.startTimed(world);
            controller.bindWorld(world);
            loop.start();
            // 播放游戏开始音效
            com.battlecity.audio.AudioManager.getInstance().playSound("game_start");
        }
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
        // 播放游戏开始音效
        com.battlecity.audio.AudioManager.getInstance().playSound("game_start");
    }
    
    /**
     * 根据关卡ID启动对应的游戏模式
     */
    public void startLevelByType(LevelDefinition levelDefinition) {
        if (levelDefinition == null) {
            throw new IllegalArgumentException("关卡定义为空");
        }
        world = GameWorld.initialWorld(levelDefinition);
        String levelId = levelDefinition.id();
        if (levelId != null) {
            if (levelId.startsWith("endless-")) {
                // 无尽模式
                stateManager.startEndless(world);
            } else if (levelId.startsWith("timed-") || levelId.startsWith("timed_challenge")) {
                // 限时模式
                stateManager.startTimed(world);
            } else {
                // 经典模式或自定义关卡
                stateManager.startClassic(world);
            }
        } else {
            // 没有ID的关卡，使用经典模式
            stateManager.startClassic(world);
        }
        controller.bindWorld(world);
        loop.start();
        // 播放游戏开始音效
        com.battlecity.audio.AudioManager.getInstance().playSound("game_start");
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
        // 更新游戏状态（时间）
        GameState currentState = stateManager.current();
        if (currentState != null) {
            currentState.update(deltaSeconds);
        }
        // GameController负责更新世界（包括碰撞检测）
        com.battlecity.engine.state.GameModeType gameMode = currentState != null ? currentState.mode() : com.battlecity.engine.state.GameModeType.CLASSIC;
        double elapsedSeconds = currentState != null ? currentState.elapsedSeconds() : 0.0;
        controller.update(deltaSeconds, gameMode, elapsedSeconds);
    }

    public GameWorld getWorld() {
        return world;
    }
    
    public GameStateManager getStateManager() {
        return stateManager;
    }
}

