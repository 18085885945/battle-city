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
    
    /**
     * 重置输入状态，清除所有按下的键
     * 用于关卡切换时清除上一关的输入状态
     */
    public void reset() {
        pressedKeys.clear();
        justPressedKeys.clear();
        lastDirectionKey = null;
        fireProcessed = false;
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
    
    /**
     * 获取当前按下的键集合
     */
    public Set<KeyCode> getPressedKeys() {
        return pressedKeys;
    }

    /**
     * 处理输入，返回子弹列表和激光
     * @param deltaSeconds 时间增量
     * @return 输入结果，包含子弹和激光
     */
    public InputResult processInputs(double deltaSeconds) {
        if (playerTank == null) {
            return new InputResult(null, null);
        }
        
        // 检查是否正在蓄力激光
        boolean isChargingLaser = playerTank.isChargingLaser();
        
        // 移动控制：只响应最后按下的方向键，避免斜向移动
        // 如果正在蓄力激光，不能移动
        if (!isChargingLaser) {
            KeyCode directionToMove = lastDirectionKey;
            if (directionToMove == null) {
                // 如果没有记录最后按下的方向键，从当前按下的方向键中选择一个
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
        } else {
            // 蓄力时，如果按下方向键，取消蓄力
            if (pressedKeys.contains(KeyCode.W) || pressedKeys.contains(KeyCode.UP) ||
                pressedKeys.contains(KeyCode.S) || pressedKeys.contains(KeyCode.DOWN) ||
                pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.LEFT) ||
                pressedKeys.contains(KeyCode.D) || pressedKeys.contains(KeyCode.RIGHT)) {
                playerTank.cancelLaserCharge();
                return new InputResult(null, null);
            }
        }
        
        // 攻击控制：空格或Enter
        boolean fireKeyPressed = pressedKeys.contains(KeyCode.SPACE) || pressedKeys.contains(KeyCode.ENTER);
        
        if (fireKeyPressed) {
            // 如果有激光武器，尝试使用激光
            if (playerTank.getLaserAmmo() > 0) {
                // 激光处理在GameController中完成，这里返回null
                return new InputResult(null, null);
            } else {
                // 普通射击或散射子弹
                java.util.List<com.battlecity.model.projectile.Bullet> bullets = playerTank.tryFire();
                if (bullets != null && !bullets.isEmpty()) {
                    return new InputResult(bullets, null);
                }
            }
        } else {
            // 如果释放了攻击键且正在蓄力，取消蓄力
            if (isChargingLaser) {
                playerTank.cancelLaserCharge();
            }
        }
        
        return new InputResult(null, null);
    }
    
    /**
     * 输入结果类
     */
    public static class InputResult {
        private final java.util.List<com.battlecity.model.projectile.Bullet> bullets;
        private final com.battlecity.model.projectile.Laser laser;
        
        public InputResult(java.util.List<com.battlecity.model.projectile.Bullet> bullets, 
                          com.battlecity.model.projectile.Laser laser) {
            this.bullets = bullets;
            this.laser = laser;
        }
        
        public java.util.List<com.battlecity.model.projectile.Bullet> bullets() {
            return bullets;
        }
        
        public com.battlecity.model.projectile.Laser laser() {
            return laser;
        }
    }
    
    // 保持向后兼容的旧方法
    @Deprecated
    public com.battlecity.model.projectile.Bullet processInputsOld(double deltaSeconds) {
        InputResult result = processInputs(deltaSeconds);
        if (result.bullets() != null && !result.bullets().isEmpty()) {
            return result.bullets().get(0);
        }
        return null;
    }
}

