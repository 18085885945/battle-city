package com.battlecity.controller;

import com.battlecity.model.GameWorld;
import com.battlecity.model.tank.PlayerTank;
import javafx.scene.input.KeyCode;

import java.util.EnumSet;
import java.util.Set;

/**
 * 负责键盘输入处理。
 */
public class InputController {

    private final Set<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);
    private final Set<KeyCode> justPressedKeys = EnumSet.noneOf(KeyCode.class);
    private boolean fireProcessed = false; // 标记攻击是否已处理
    private PlayerTank playerTank;

    public void bindWorld(GameWorld world) {
        this.playerTank = world.playerTank();
    }

    public void onKeyPressed(KeyCode code) {
        if (!pressedKeys.contains(code)) {
            // 这是新按下的键
            pressedKeys.add(code);
            // 标记刚按下的键（用于攻击等单次触发）
            if (code == KeyCode.SPACE || code == KeyCode.ENTER) {
                justPressedKeys.add(code);
                fireProcessed = false; // 重置攻击处理标志
            }
        }
    }

    public void onKeyReleased(KeyCode code) {
        pressedKeys.remove(code);
        justPressedKeys.remove(code);
    }

    public com.battlecity.model.projectile.Bullet processInputs(double deltaSeconds) {
        if (playerTank == null) {
            return null;
        }
        
        // 移动控制：WASD 或方向键（持续按下时移动）
        boolean moveUp = pressedKeys.contains(KeyCode.W) || pressedKeys.contains(KeyCode.UP);
        boolean moveDown = pressedKeys.contains(KeyCode.S) || pressedKeys.contains(KeyCode.DOWN);
        boolean moveLeft = pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.LEFT);
        boolean moveRight = pressedKeys.contains(KeyCode.D) || pressedKeys.contains(KeyCode.RIGHT);
        
        if (moveUp) {
            playerTank.moveUp(deltaSeconds);
        }
        if (moveDown) {
            playerTank.moveDown(deltaSeconds);
        }
        if (moveLeft) {
            playerTank.moveLeft(deltaSeconds);
        }
        if (moveRight) {
            playerTank.moveRight(deltaSeconds);
        }
        
        // 攻击控制：空格或Enter（只在刚按下时触发一次）
        boolean firePressed = (justPressedKeys.contains(KeyCode.SPACE) || justPressedKeys.contains(KeyCode.ENTER))
                && !fireProcessed;
        
        if (firePressed) {
            fireProcessed = true; // 标记已处理
            return playerTank.tryFire();
        }
        
        // 清除刚按下的标记（如果按键已释放）
        if (!pressedKeys.contains(KeyCode.SPACE) && !pressedKeys.contains(KeyCode.ENTER)) {
            justPressedKeys.remove(KeyCode.SPACE);
            justPressedKeys.remove(KeyCode.ENTER);
            fireProcessed = false;
        }
        
        return null;
    }
}

