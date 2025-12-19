package com.battlecity.model.tank;

import com.battlecity.model.Vector2D;
import com.battlecity.model.projectile.Bullet;

/**
 * 速度精英怪：速度为玩家的1.5倍
 */
public class SpeedEliteEnemyTank extends EnemyTank {

    private static final double DEFAULT_BULLET_SPEED = 200;

    public SpeedEliteEnemyTank(Vector2D position, TankAttributes attributes) {
        super(position, attributes, EnemyTier.ELITE_SPEED);
        // 速度精英怪生命值为1（普通）
        setHealth(1);
    }

    @Override
    protected Bullet createBullet() {
        return createBulletWithSpeed(DEFAULT_BULLET_SPEED);
    }
}

