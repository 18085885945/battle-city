package com.battlecity.model.tank;

import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;
import com.battlecity.model.projectile.Bullet;

/**
 * 玩家坦克
 */
public class PlayerTank extends Tank {

    public PlayerTank(Vector2D position, TankAttributes attributes) {
        super(position, new Size(32, 32), TankType.PLAYER, attributes);
    }

    public void moveUp(double deltaSeconds) {
        move(new Vector2D(0, -1), deltaSeconds);
    }

    public void moveDown(double deltaSeconds) {
        move(new Vector2D(0, 1), deltaSeconds);
    }

    public void moveLeft(double deltaSeconds) {
        move(new Vector2D(-1, 0), deltaSeconds);
    }

    public void moveRight(double deltaSeconds) {
        move(new Vector2D(1, 0), deltaSeconds);
    }

    public Bullet tryFire() {
        return tryFireInternal().orElse(null);
    }

    @Override
    protected Bullet createBullet() {
        // 从坦克中心发射，方向为当前面向方向
        Vector2D center = center();
        double cannonOffset = size().width() * 0.4;
        Vector2D bulletStart = center.add(facingDirection().scale(cannonOffset));
        Vector2D bulletTopLeft = new Vector2D(
            bulletStart.x() - 2,
            bulletStart.y() - 2
        );
        return new Bullet(bulletTopLeft, facingDirection(), 240);
    }
}

