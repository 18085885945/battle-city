package com.battlecity.model.powerup;

import com.battlecity.model.Vector2D;
import com.battlecity.model.tank.PlayerTank;

/**
 * 增加坦克移动速度的道具
 */
public class TankSpeedPowerUp extends PowerUp {
    
    private static final double DURATION = 10.0; // 效果持续时间（秒）
    
    public TankSpeedPowerUp(Vector2D position) {
        super(position, PowerUpType.TANK_SPEED);
    }
    
    @Override
    public void applyEffect(PlayerTank playerTank) {
        // 提升坦克移动速度50%
        playerTank.addPowerUpEffect(new PowerUpEffect(PowerUpType.TANK_SPEED, DURATION) {
            @Override
            public void apply() {
                playerTank.setSpeedBoost(0.5); // 50%速度提升
            }
            
            @Override
            public void remove() {
                playerTank.setSpeedBoost(0); // 恢复正常
            }
        });
    }
}