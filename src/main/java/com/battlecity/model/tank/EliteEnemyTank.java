package com.battlecity.model.tank;

import com.battlecity.model.Vector2D;
import com.battlecity.model.projectile.Bullet;

/**
 * 精英敌方坦克（框架）。
 *
 * <p>当前实现的差异点：
 * <ul>
 *   <li>更高生命值</li>
 *   <li>更快子弹速度（便于后续继续扩展：更强火力、更高移速、更聪明AI等）</li>
 * </ul>
 */
public class EliteEnemyTank extends EnemyTank {

    private static final int ELITE_HEALTH = 2;
    private static final double ELITE_BULLET_SPEED = 230;

    public EliteEnemyTank(Vector2D position, TankAttributes attributes) {
        super(position, attributes, EnemyTier.ELITE);
        setHealth(ELITE_HEALTH);
    }

    @Override
    protected Bullet createBullet() {
        return createBulletWithSpeed(ELITE_BULLET_SPEED);
    }
}


