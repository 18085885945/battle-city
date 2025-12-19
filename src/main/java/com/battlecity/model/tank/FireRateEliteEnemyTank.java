package com.battlecity.model.tank;

import com.battlecity.model.Vector2D;
import com.battlecity.model.projectile.Bullet;

/**
 * 射速精英怪：射速为普通敌方坦克的1.5倍
 */
public class FireRateEliteEnemyTank extends EnemyTank {

    private static final double DEFAULT_BULLET_SPEED = 200;

    public FireRateEliteEnemyTank(Vector2D position, TankAttributes attributes) {
        super(position, attributes, EnemyTier.ELITE_FIRERATE);
        // 射速精英怪生命值为1（普通）
        setHealth(1);
    }

    @Override
    protected Bullet createBullet() {
        return createBulletWithSpeed(DEFAULT_BULLET_SPEED);
    }
}

