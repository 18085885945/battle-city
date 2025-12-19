package com.battlecity.controller;

import com.battlecity.engine.GameContext;
import com.battlecity.model.GameWorld;
import javafx.scene.input.KeyCode;

/**
 * 负责游戏流程控制，驱动世界更新。
 */
public class GameController {

    private final GameContext context;
    private final SceneRouterFacade sceneRouter;
    private final InputController inputController;
    private GameWorld world;

    public GameController(GameContext context, SceneRouterFacade sceneRouter) {
        this.context = context;
        this.sceneRouter = sceneRouter;
        this.inputController = new InputController();
    }

    public void bindWorld(GameWorld world) {
        this.world = world;
        inputController.bindWorld(world);
    }

    public void update(double deltaSeconds, com.battlecity.engine.state.GameModeType gameMode, double elapsedSeconds) {
        if (world == null) {
            return;
        }
        
        // 保存玩家坦克移动前的位置（用于碰撞检测）
        if (world.playerTank() != null && world.playerTank().alive()) {
            world.savePlayerPositionBeforeMove();
        }
        
        // 处理输入并获取可能产生的子弹
        // 注意：processInputs 会移动坦克，但移动后会在 handlePlayerTankCollisions 中检测并回退
        com.battlecity.model.projectile.Bullet newBullet = inputController.processInputs(deltaSeconds);
        if (newBullet != null) {
            world.addPlayerBullet(newBullet);
            // 播放开火音效
            com.battlecity.audio.AudioManager.getInstance().playSound("fire");
        }
        
        // 更新世界（包括碰撞检测和回退）
        world.update(deltaSeconds);
        
        // 检查限时模式是否超时
        if (gameMode == com.battlecity.engine.state.GameModeType.TIMED) {
            Integer timeLimit = world.levelDefinition().timeLimitSeconds();
            if (timeLimit != null && elapsedSeconds > timeLimit) {
                // 超时，设置游戏失败原因
                world.setGameOverReason("TIME");
            }
        }
        
        // 检查游戏失败（基地血量<=0 或 玩家坦克死亡 或 限时模式超时）
        if (world.isGameOver()) {
            // 播放游戏结束音效
            com.battlecity.audio.AudioManager.getInstance().playSound("game_over");
            // 暂停游戏循环
            // 实际的处理会在UI层显示失败界面
        }
        
        // 检查游戏胜利（在失败检查之后）
        if (!world.isGameOver() && world.isVictory(gameMode, elapsedSeconds)) {
            // 胜利，实际的处理会在UI层显示胜利界面
        }
    }
    
    public boolean isGameOver() {
        return world != null && world.isGameOver();
    }
    
    public boolean isVictory(com.battlecity.engine.state.GameModeType gameMode, double elapsedSeconds) {
        return world != null && world.isVictory(gameMode, elapsedSeconds);
    }

    public void onKeyPressed(KeyCode code) {
        inputController.onKeyPressed(code);
    }

    public void onKeyReleased(KeyCode code) {
        inputController.onKeyReleased(code);
    }

    public InputController getInputController() {
        return inputController;
    }
}

