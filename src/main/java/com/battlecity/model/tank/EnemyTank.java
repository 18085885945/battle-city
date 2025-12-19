package com.battlecity.model.tank;

import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;
import com.battlecity.model.projectile.Bullet;

public class EnemyTank extends Tank {

    private final EnemyTier tier;
    private static final double DEFAULT_BULLET_SPEED = 200;
    private boolean frozen = false; // 是否被冻结
    private double freezeTimeRemaining = 0.0; // 冻结剩余时间

    public EnemyTank(Vector2D position, TankAttributes attributes, EnemyTier tier) {
        // 调整坦克大小为26x26，与玩家坦克保持一致
        super(position, new Size(26, 26), tier.asTankType(), attributes);
        this.tier = tier;
        // 设置敌方坦克初始生命值为1
        setHealth(1);
    }

    @Override
    protected Bullet createBullet() {
        // 如果被冻结，不能开火
        if (frozen) {
            return null;
        }
        return createBulletWithSpeed(DEFAULT_BULLET_SPEED);
    }

    /**
     * 子类可复用的子弹创建逻辑（例如精英怪更快的子弹速度等）。
     */
    protected Bullet createBulletWithSpeed(double bulletSpeed) {
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
        return new Bullet(bulletTopLeft, facingDirection(), bulletSpeed);
    }

    @Override
    public void tick(double deltaSeconds) {
        super.tick(deltaSeconds);
        // 更新冻结时间
        if (frozen) {
            freezeTimeRemaining -= deltaSeconds;
            if (freezeTimeRemaining <= 0) {
                unfreeze();
            }
        }
    }
    
    /**
     * 冻结坦克
     * @param duration 冻结持续时间
     */
    public void freeze(double duration) {
        this.frozen = true;
        this.freezeTimeRemaining = duration;
    }
    
    /**
     * 解冻坦克
     */
    private void unfreeze() {
        this.frozen = false;
        this.freezeTimeRemaining = 0.0;
    }
    
    /**
     * 检查坦克是否被冻结
     */
    public boolean isFrozen() {
        return frozen;
    }

    public EnemyTier tier() {
        return tier;
    }
    
    /**
     * 向上移动（供AI使用）
     */
    public void moveUp(double deltaSeconds) {
        if (!frozen) {
            move(new Vector2D(0, -1), deltaSeconds);
        }
    }
    
    /**
     * 向下移动（供AI使用）
     */
    public void moveDown(double deltaSeconds) {
        if (!frozen) {
            move(new Vector2D(0, 1), deltaSeconds);
        }
    }
    
    /**
     * 向左移动（供AI使用）
     */
    public void moveLeft(double deltaSeconds) {
        if (!frozen) {
            move(new Vector2D(-1, 0), deltaSeconds);
        }
    }
    
    /**
     * 向右移动（供AI使用）
     */
    public void moveRight(double deltaSeconds) {
        if (!frozen) {
            move(new Vector2D(1, 0), deltaSeconds);
        }
    }
}

