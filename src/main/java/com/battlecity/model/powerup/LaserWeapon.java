package com.battlecity.model.powerup;

import com.battlecity.model.Vector2D;
import com.battlecity.model.tank.PlayerTank;

/**
 * 激光武器道具
 */
public class LaserWeapon extends PowerUp {
    
    public LaserWeapon(Vector2D position) {
        super(position, PowerUpType.LASER);
    }
    
    @Override
    public void applyEffect(PlayerTank playerTank) {
        // 如果已有激光武器，触发大激光效果；否则装备普通激光武器
        if (playerTank.getLaserAmmo() > 0) {
            // 已有激光武器，触发大激光效果
            playerTank.enableMegaLaser();
        } else {
            // 给玩家坦克添加激光武器
            playerTank.equipLaserWeapon();
        }
    }
}

