package com.battlecity.model.powerup;

import com.battlecity.model.Vector2D;
import com.battlecity.model.tank.PlayerTank;

/**
 * 空袭道具
 */
public class AirstrikePowerUp extends PowerUp {
    
    public AirstrikePowerUp(Vector2D position) {
        super(position, PowerUpType.AIRSTRIKE);
    }
    
    @Override
    public void applyEffect(PlayerTank playerTank) {
        // 空袭效果通过全局效果实现，这里不需要对玩家坦克做任何操作
    }
    
    @Override
    public void applyGlobalEffect(com.battlecity.model.GameWorld world) {
        // 对所有敌人发动空袭，所有敌人会被秒杀
        java.util.List<com.battlecity.model.tank.EnemyTank> enemyTanks = world.enemyTanks();
        for (com.battlecity.model.tank.EnemyTank enemy : enemyTanks) {
            if (enemy.alive()) {
                // 秒杀敌人
                enemy.takeDamage(999);
                // 创建爆炸特效
                com.battlecity.effect.ExplosionEffect explosion = new com.battlecity.effect.ExplosionEffect(
                    enemy.center(), 0.5, 20.0
                );
                world.getEffectManager().addEffect(explosion);
            }
        }
        // 播放爆炸音效
        com.battlecity.audio.AudioManager.getInstance().playSound("explosion");
    }
}

