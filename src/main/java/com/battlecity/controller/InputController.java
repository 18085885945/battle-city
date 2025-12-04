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
    
    // 记录最后按下的方向键，确保同时按下多个方向键时只朝一个方向移动
    private KeyCode lastDirectionKey = null;

    public void bindWorld(GameWorld world) {
        this.playerTank = world.playerTank();
    }

    public void onKeyPressed(KeyCode code) {
        if (!pressedKeys.contains(code)) {
            // 这是新按下的键
            pressedKeys.add(code);
            // 如果是方向键，更新最后按下的方向键
            if (isDirectionKey(code)) {
                lastDirectionKey = code;
            }
            // 标记刚按下的键（用于攻击等单次触发）
            if (code == KeyCode.SPACE || code == KeyCode.ENTER) {
                justPressedKeys.add(code);
                fireProcessed = false; // 重置攻击处理标志
            }
        }
    }
    
    /**
     * 判断是否是方向键
     */
    private boolean isDirectionKey(KeyCode code) {
        return code == KeyCode.W || code == KeyCode.UP ||
               code == KeyCode.S || code == KeyCode.DOWN ||
               code == KeyCode.A || code == KeyCode.LEFT ||
               code == KeyCode.D || code == KeyCode.RIGHT;
    }

    public void onKeyReleased(KeyCode code) {
        pressedKeys.remove(code);
        justPressedKeys.remove(code);
        
        // 如果释放的是最后按下的方向键，需要重新选择最后按下的方向键
        if (code == lastDirectionKey) {
            lastDirectionKey = null;
            // 从当前按下的方向键中选择最后一个
            for (KeyCode key : pressedKeys) {
                if (isDirectionKey(key)) {
                    lastDirectionKey = key;
                }
            }
        }
    }

    public com.battlecity.model.projectile.Bullet processInputs(double deltaSeconds) {
        if (playerTank == null) {
            return null;
        }
        
        // 移动控制：只响应最后按下的方向键，避免斜向移动
        // 如果 lastDirectionKey 为 null，检查当前按下的方向键
        KeyCode directionToMove = lastDirectionKey;
        if (directionToMove == null) {
            // 如果没有记录最后按下的方向键，从当前按下的方向键中选择一个
            // 优先级：上 > 下 > 左 > 右（或者可以改为最后按下的）
            if (pressedKeys.contains(KeyCode.W) || pressedKeys.contains(KeyCode.UP)) {
                directionToMove = pressedKeys.contains(KeyCode.W) ? KeyCode.W : KeyCode.UP;
            } else if (pressedKeys.contains(KeyCode.S) || pressedKeys.contains(KeyCode.DOWN)) {
                directionToMove = pressedKeys.contains(KeyCode.S) ? KeyCode.S : KeyCode.DOWN;
            } else if (pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.LEFT)) {
                directionToMove = pressedKeys.contains(KeyCode.A) ? KeyCode.A : KeyCode.LEFT;
            } else if (pressedKeys.contains(KeyCode.D) || pressedKeys.contains(KeyCode.RIGHT)) {
                directionToMove = pressedKeys.contains(KeyCode.D) ? KeyCode.D : KeyCode.RIGHT;
            }
        }
        
        // 只朝一个方向移动
        if (directionToMove != null) {
            if (directionToMove == KeyCode.W || directionToMove == KeyCode.UP) {
                playerTank.moveUp(deltaSeconds);
            } else if (directionToMove == KeyCode.S || directionToMove == KeyCode.DOWN) {
                playerTank.moveDown(deltaSeconds);
            } else if (directionToMove == KeyCode.A || directionToMove == KeyCode.LEFT) {
                playerTank.moveLeft(deltaSeconds);
            } else if (directionToMove == KeyCode.D || directionToMove == KeyCode.RIGHT) {
                playerTank.moveRight(deltaSeconds);
            }
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

