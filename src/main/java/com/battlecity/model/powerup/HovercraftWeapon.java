package com.battlecity.model.powerup;

import com.battlecity.model.Vector2D;
import com.battlecity.model.tank.PlayerTank;

/**
 * 气垫装备道具
 */
public class HovercraftWeapon extends PowerUp {
    
    private static final double DURATION = 10.0; // 持续10秒
    
    public HovercraftWeapon(Vector2D position) {
        super(position, PowerUpType.HOVERCRAFT);
    }
    
    @Override
    public void applyEffect(PlayerTank playerTank) {
        // 给玩家坦克添加气垫效果
        playerTank.addPowerUpEffect(new PowerUpEffect(PowerUpType.HOVERCRAFT, DURATION) {
            @Override
            public void apply() {
                playerTank.setHovercraftActive(true);
            }
            
            @Override
            public void remove() {
                playerTank.setHovercraftActive(false);
            }
        });
    }
}

