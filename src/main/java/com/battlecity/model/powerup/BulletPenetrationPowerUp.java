package com.battlecity.model.powerup;

import com.battlecity.model.Vector2D;
import com.battlecity.model.tank.PlayerTank;

/**
 * 增加子弹伤害，可破坏铁块的道具
 */
public class BulletPenetrationPowerUp extends PowerUp {
    
    private static final double DURATION = 10.0; // 效果持续时间（秒）
    
    public BulletPenetrationPowerUp(Vector2D position) {
        super(position, PowerUpType.BULLET_PENETRATION);
    }
    
    @Override
    public void applyEffect(PlayerTank playerTank) {
        // 使子弹可以破坏铁块
        playerTank.addPowerUpEffect(new PowerUpEffect(PowerUpType.BULLET_PENETRATION, DURATION) {
            @Override
            public void apply() {
                playerTank.setBulletCanPenetrate(true);
            }
            
            @Override
            public void remove() {
                playerTank.setBulletCanPenetrate(false);
            }
        });
    }
}