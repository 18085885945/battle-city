package com.battlecity.model.powerup;

import com.battlecity.model.Vector2D;
import com.battlecity.model.tank.PlayerTank;

/**
 * 散射子弹武器道具
 */
public class ScatterShotWeapon extends PowerUp {
    
    private static final double DURATION = 15.0; // 持续15秒
    
    public ScatterShotWeapon(Vector2D position) {
        super(position, PowerUpType.SCATTER_SHOT);
    }
    
    @Override
    public void applyEffect(PlayerTank playerTank) {
        // 给玩家坦克添加散射子弹效果
        playerTank.addPowerUpEffect(new PowerUpEffect(PowerUpType.SCATTER_SHOT, DURATION) {
            @Override
            public void apply() {
                playerTank.setScatterShotActive(true);
            }
            
            @Override
            public void remove() {
                playerTank.setScatterShotActive(false);
            }
        });
    }
}

