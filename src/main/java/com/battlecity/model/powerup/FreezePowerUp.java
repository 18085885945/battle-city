package com.battlecity.model.powerup;

import com.battlecity.model.Vector2D;
import com.battlecity.model.tank.EnemyTank;
import com.battlecity.model.tank.PlayerTank;

import java.util.List;

/**
 * 冻结所有敌方坦克的道具
 */
public class FreezePowerUp extends PowerUp {
    
    private static final double DURATION = 5.0; // 冻结持续时间（秒）
    
    public FreezePowerUp(Vector2D position) {
        super(position, PowerUpType.FREEZE);
    }
    
    @Override
    public void applyEffect(PlayerTank playerTank) {
        // 冻结效果不直接影响玩家坦克，而是通过全局效果影响敌方坦克
    }
    
    @Override
    public void applyGlobalEffect(com.battlecity.model.GameWorld world) {
        // 冻结所有敌方坦克
        List<EnemyTank> enemyTanks = world.enemyTanks();
        for (EnemyTank enemyTank : enemyTanks) {
            enemyTank.freeze(DURATION);
        }
    }
}