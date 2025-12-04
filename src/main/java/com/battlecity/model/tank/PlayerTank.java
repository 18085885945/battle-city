package com.battlecity.model.tank;

import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;
import com.battlecity.model.projectile.Bullet;

public class PlayerTank extends Tank {

    public PlayerTank(Vector2D position, TankAttributes attributes) {
        // 调整坦克大小为26x26，使得子弹能够击中底部砖块
        super(position, new Size(26, 26), TankType.PLAYER, attributes);
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
        // 子弹从炮管位置发射（中心 + 方向 * 炮管长度的一半）
        // 调整炮管偏移，让子弹从更靠下的位置发射，确保能击中底部砖块
        double cannonOffset = size().width() * 0.5; // 增加偏移量
        Vector2D bulletStart = center.add(facingDirection().scale(cannonOffset));
        // Bullet的position是左上角，所以需要减去子弹大小的一半
        Vector2D bulletTopLeft = new Vector2D(
            bulletStart.x() - 2, // 子弹大小是4x4，所以减去2
            bulletStart.y() - 2
        );
        return new Bullet(bulletTopLeft, facingDirection(), 240);
    }
}

