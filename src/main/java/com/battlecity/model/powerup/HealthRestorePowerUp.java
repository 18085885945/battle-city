package com.battlecity.model.powerup;

import com.battlecity.model.Vector2D;
import com.battlecity.model.tank.PlayerTank;

/**
 * 恢复坦克生命值的道具
 */
public class HealthRestorePowerUp extends PowerUp {
    
    private static final int HEALTH_AMOUNT = 2; // 恢复的生命值数量
    
    public HealthRestorePowerUp(Vector2D position) {
        super(position, PowerUpType.HEALTH_RESTORE);
    }
    
    @Override
    public void applyEffect(PlayerTank playerTank) {
        // 直接恢复生命值，不需要持续时间
        playerTank.setHealth(playerTank.health() + HEALTH_AMOUNT);
    }
}