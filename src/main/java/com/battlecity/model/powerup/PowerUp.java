package com.battlecity.model.powerup;

import com.battlecity.model.Entity;
import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;

/**
 * 道具基类
 */
public abstract class PowerUp extends Entity {
    
    private final PowerUpType type;
    private boolean alive = true;
    private double spawnTime = 0.0;
    private static final double DURATION = 10.0; // 道具存在时间（秒）
    
    protected PowerUp(Vector2D position, PowerUpType type) {
        super(position, new Size(16, 16)); // 道具大小为16x16
        this.type = type;
    }
    
    public PowerUpType getType() {
        return type;
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    public void destroy() {
        this.alive = false;
    }
    
    public void update(double deltaSeconds) {
        spawnTime += deltaSeconds;
        if (spawnTime >= DURATION) {
            destroy(); // 超时自动消失
        }
    }
    
    /**
     * 应用道具效果
     * @param playerTank 玩家坦克
     */
    public abstract void applyEffect(com.battlecity.model.tank.PlayerTank playerTank);
    
    /**
     * 应用道具效果到整个游戏世界（用于冻结等全局效果）
     * @param world 游戏世界
     */
    public void applyGlobalEffect(com.battlecity.model.GameWorld world) {
        // 默认不实现，由需要全局效果的道具重写
    }
}