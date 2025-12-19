package com.battlecity.model.tank;

import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;
import com.battlecity.model.projectile.Bullet;
import com.battlecity.model.powerup.PowerUpEffect;

import java.util.ArrayList;
import java.util.List;

public class PlayerTank extends Tank {
    
    private double bulletSpeedBoost = 0.0; // 子弹速度提升百分比（0.0-1.0）
    private boolean bulletCanPenetrate = false; // 子弹是否可以穿透铁块
    private boolean oneShotMode = false; // 秒杀模式
    private double speedBoost = 0.0; // 移动速度提升百分比（0.0-1.0）
    private final List<PowerUpEffect> activeEffects = new ArrayList<>();

    public PlayerTank(Vector2D position, TankAttributes attributes) {
        // 调整坦克大小为26x26，使得子弹能够击中底部砖块
        super(position, new Size(26, 26), TankType.PLAYER, attributes);
        // 设置玩家坦克初始生命值为5
        setHealth(5);
    }

    public void moveUp(double deltaSeconds) {
        move(new Vector2D(0, -1), deltaSeconds * (1 + speedBoost));
    }

    public void moveDown(double deltaSeconds) {
        move(new Vector2D(0, 1), deltaSeconds * (1 + speedBoost));
    }

    public void moveLeft(double deltaSeconds) {
        move(new Vector2D(-1, 0), deltaSeconds * (1 + speedBoost));
    }

    public void moveRight(double deltaSeconds) {
        move(new Vector2D(1, 0), deltaSeconds * (1 + speedBoost));
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
        
        // 创建子弹时设置其属性
        Bullet bullet = new Bullet(bulletTopLeft, facingDirection(), 240);
        bullet.setCanPenetrate(bulletCanPenetrate);
        bullet.setOneShotMode(oneShotMode);
        return bullet;
    }
    
    @Override
    public void tick(double deltaSeconds) {
        super.tick(deltaSeconds);
        // 更新所有活跃的道具效果
        updatePowerUpEffects(deltaSeconds);
    }
    
    /**
     * 更新所有活跃的道具效果
     */
    private void updatePowerUpEffects(double deltaSeconds) {
        activeEffects.removeIf(effect -> !effect.isActive());
        for (PowerUpEffect effect : activeEffects) {
            effect.update(deltaSeconds);
        }
    }
    
    /**
     * 添加道具效果
     */
    public void addPowerUpEffect(PowerUpEffect effect) {
        // 如果已有同类型的效果，移除旧效果
        activeEffects.removeIf(e -> e.getType() == effect.getType());
        activeEffects.add(effect);
        effect.activate();
    }
    
    /**
     * 设置子弹速度提升
     */
    public void setBulletSpeedBoost(double boost) {
        this.bulletSpeedBoost = boost;
    }
    
    /**
     * 设置子弹穿透能力
     */
    public void setBulletCanPenetrate(boolean canPenetrate) {
        this.bulletCanPenetrate = canPenetrate;
    }
    
    /**
     * 设置秒杀模式
     */
    public void setOneShotMode(boolean oneShotMode) {
        this.oneShotMode = oneShotMode;
    }
    
    /**
     * 设置移动速度提升
     */
    public void setSpeedBoost(double boost) {
        this.speedBoost = boost;
    }
    
    /**
     * 获取当前活跃的道具效果
     */
    public List<PowerUpEffect> getActiveEffects() {
        return new ArrayList<>(activeEffects);
    }
}

