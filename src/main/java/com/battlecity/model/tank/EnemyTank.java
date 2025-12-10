package com.battlecity.model.tank;

import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;
import com.battlecity.model.projectile.Bullet;

public class EnemyTank extends Tank {

    private final EnemyTier tier;

    public EnemyTank(Vector2D position, TankAttributes attributes, EnemyTier tier) {
        // 调整坦克大小为26x26，与玩家坦克保持一致
        super(position, new Size(26, 26), tier.asTankType(), attributes);
        this.tier = tier;
        // 设置敌方坦克初始生命值为1
        setHealth(1);
    }

    @Override
    protected Bullet createBullet() {
        // 从坦克中心发射，方向为当前面向方向（默认向下）
        Vector2D center = center();
        // 调整炮管偏移，让子弹从更靠下的位置发射
        double cannonOffset = size().width() * 0.5; // 增加偏移量
        Vector2D bulletStart = center.add(facingDirection().scale(cannonOffset));
        // Bullet的position是左上角，所以需要减去子弹大小的一半
        Vector2D bulletTopLeft = new Vector2D(
            bulletStart.x() - 2, // 子弹大小是4x4，所以减去2
            bulletStart.y() - 2
        );
        return new Bullet(bulletTopLeft, facingDirection(), 200);
    }

    public EnemyTier tier() {
        return tier;
    }
    
    /**
     * 向上移动（供AI使用）
     */
    public void moveUp(double deltaSeconds) {
        move(new Vector2D(0, -1), deltaSeconds);
    }
    
    /**
     * 向下移动（供AI使用）
     */
    public void moveDown(double deltaSeconds) {
        move(new Vector2D(0, 1), deltaSeconds);
    }
    
    /**
     * 向左移动（供AI使用）
     */
    public void moveLeft(double deltaSeconds) {
        move(new Vector2D(-1, 0), deltaSeconds);
    }
    
    /**
     * 向右移动（供AI使用）
     */
    public void moveRight(double deltaSeconds) {
        move(new Vector2D(1, 0), deltaSeconds);
    }
}

