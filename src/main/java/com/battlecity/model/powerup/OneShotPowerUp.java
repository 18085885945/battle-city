package com.battlecity.model.powerup;

import com.battlecity.model.Vector2D;
import com.battlecity.model.tank.PlayerTank;

/**
 * 秒杀效果的道具，一击摧毁所有坦克
 */
public class OneShotPowerUp extends PowerUp {
    
    private static final double DURATION = 8.0; // 效果持续时间（秒）
    
    public OneShotPowerUp(Vector2D position) {
        super(position, PowerUpType.ONE_SHOT);
    }
    
    @Override
    public void applyEffect(PlayerTank playerTank) {
        // 使子弹可以一击摧毁所有坦克
        playerTank.addPowerUpEffect(new PowerUpEffect(PowerUpType.ONE_SHOT, DURATION) {
            @Override
            public void apply() {
                playerTank.setOneShotMode(true);
            }
            
            @Override
            public void remove() {
                playerTank.setOneShotMode(false);
            }
        });
    }
}