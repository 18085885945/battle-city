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

    public void update(double deltaSeconds) {
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
        }
        
        // 更新世界（包括碰撞检测和回退）
        world.update(deltaSeconds);
        
        // 检查游戏失败（基地血量<=0）
        if (world.isGameOver()) {
            // 暂停游戏循环
            // 实际的处理会在UI层显示失败界面
        }
    }
    
    public boolean isGameOver() {
        return world != null && world.isGameOver();
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

