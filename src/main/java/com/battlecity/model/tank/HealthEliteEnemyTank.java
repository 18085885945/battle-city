package com.battlecity.model.tank;

import com.battlecity.model.Vector2D;
import com.battlecity.model.projectile.Bullet;

/**
 * 生命精英怪：生命为2点
 */
public class HealthEliteEnemyTank extends EnemyTank {

    private static final int ELITE_HEALTH = 2;
    private static final double DEFAULT_BULLET_SPEED = 200;

    public HealthEliteEnemyTank(Vector2D position, TankAttributes attributes) {
        super(position, attributes, EnemyTier.ELITE_HEALTH);
        // 生命精英怪生命值为2
        setHealth(ELITE_HEALTH);
    }

    @Override
    protected Bullet createBullet() {
        return createBulletWithSpeed(DEFAULT_BULLET_SPEED);
    }
}

