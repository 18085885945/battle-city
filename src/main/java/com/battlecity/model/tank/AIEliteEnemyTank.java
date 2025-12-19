package com.battlecity.model.tank;

import com.battlecity.model.Vector2D;
import com.battlecity.model.projectile.Bullet;

/**
 * 高级AI精英怪：AI为疯狂模式（专门攻击基地）
 */
public class AIEliteEnemyTank extends EnemyTank {

    private static final double DEFAULT_BULLET_SPEED = 200;

    public AIEliteEnemyTank(Vector2D position, TankAttributes attributes) {
        super(position, attributes, EnemyTier.ELITE_AI);
        // AI精英怪生命值为1（普通）
        setHealth(1);
    }

    @Override
    protected Bullet createBullet() {
        return createBulletWithSpeed(DEFAULT_BULLET_SPEED);
    }
}

