package com.battlecity.model.powerup;

import com.battlecity.model.Vector2D;
import com.battlecity.model.tank.PlayerTank;

/**
 * 增加子弹射击速度的道具
 */
public class BulletSpeedPowerUp extends PowerUp {
    
    private static final double DURATION = 10.0; // 效果持续时间（秒）
    
    public BulletSpeedPowerUp(Vector2D position) {
        super(position, PowerUpType.BULLET_SPEED);
    }
    
    @Override
    public void applyEffect(PlayerTank playerTank) {
        // 提升子弹发射频率，将冷却时间减少50%
        playerTank.addPowerUpEffect(new PowerUpEffect(PowerUpType.BULLET_SPEED, DURATION) {
            @Override
            public void apply() {
                playerTank.setBulletSpeedBoost(0.5); // 50%冷却时间减少
            }
            
            @Override
            public void remove() {
                playerTank.setBulletSpeedBoost(0); // 恢复正常
            }
        });
    }
}