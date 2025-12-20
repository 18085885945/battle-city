package com.battlecity.model.tank;

import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;
import com.battlecity.model.projectile.Bullet;
import com.battlecity.model.projectile.Laser;
import com.battlecity.model.powerup.PowerUpEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerTank extends Tank {
    
    private double bulletSpeedBoost = 0.0; // 子弹速度提升百分比（0.0-1.0）
    private boolean bulletCanPenetrate = false; // 子弹是否可以穿透铁块
    private boolean oneShotMode = false; // 秒杀模式
    private double speedBoost = 0.0; // 移动速度提升百分比（0.0-1.0）
    private final List<PowerUpEffect> activeEffects = new ArrayList<>();
    
    // 武器系统
    private int laserAmmo = 0; // 激光武器剩余次数
    private double laserChargeTime = 0.0; // 激光蓄力时间
    private static final double LASER_CHARGE_DURATION = 1.0; // 激光蓄力时间1秒
    private static final double MEGA_LASER_CHARGE_DURATION = 2.0; // 大激光蓄力时间2秒
    private boolean isChargingLaser = false; // 是否正在蓄力
    private boolean scatterShotActive = false; // 散射子弹是否激活
    private boolean megaLaserEnabled = false; // 大激光是否激活
    private boolean hovercraftActive = false; // 气垫是否激活
    private double hovercraftDeactivatedTime = -1.0; // 气垫效果消失的时间（-1表示未消失或已过期）
    private static final double HOVERCRAFT_GRACE_PERIOD = 1.0; // 气垫效果消失后的宽限期（1秒）
    private boolean godMode = false; // 无敌模式（控制台命令）

    public PlayerTank(Vector2D position, TankAttributes attributes) {
        // 调整坦克大小为26x26，使得子弹能够击中底部砖块
        super(position, new Size(26, 26), TankType.PLAYER, attributes);
        // 设置玩家坦克初始生命值为5
        setHealth(5);
    }

    public void moveUp(double deltaSeconds) {
        if (!isChargingLaser) { // 蓄力时无法移动
            move(new Vector2D(0, -1), deltaSeconds * (1 + speedBoost));
        }
    }

    public void moveDown(double deltaSeconds) {
        if (!isChargingLaser) { // 蓄力时无法移动
            move(new Vector2D(0, 1), deltaSeconds * (1 + speedBoost));
        }
    }

    public void moveLeft(double deltaSeconds) {
        if (!isChargingLaser) { // 蓄力时无法移动
            move(new Vector2D(-1, 0), deltaSeconds * (1 + speedBoost));
        }
    }

    public void moveRight(double deltaSeconds) {
        if (!isChargingLaser) { // 蓄力时无法移动
            move(new Vector2D(1, 0), deltaSeconds * (1 + speedBoost));
        }
    }

    /**
     * 尝试开火，返回子弹或激光
     * @return 子弹列表（散射子弹可能返回多个），如果使用激光则返回null（激光由tryFireLaser处理）
     */
    public List<Bullet> tryFire() {
        // 如果正在蓄力激光，不发射子弹
        if (isChargingLaser) {
            return new ArrayList<>();
        }
        
        // 如果有激光武器且按空格键，开始蓄力
        // 注意：这个逻辑需要在InputController中处理
        
        // 普通射击或散射子弹
        if (scatterShotActive) {
            // 散射子弹：先检查冷却时间
            Optional<Bullet> testBullet = tryFireInternal();
            if (!testBullet.isPresent()) {
                return new ArrayList<>(); // 冷却中
            }
            
            // 散射子弹：发射2个子弹，相隔一个砖块距离（16像素）
            List<Bullet> bullets = new ArrayList<>();
            Vector2D center = center();
            double cannonOffset = size().width() * 0.5;
            Vector2D direction = facingDirection();
            
            // 计算垂直于方向的向量（用于偏移）
            Vector2D perpendicular = new Vector2D(-direction.y(), direction.x());
            double offsetDistance = 8.0; // 半个砖块距离（16/2）
            
            // 第一个子弹（左侧）
            Vector2D bulletStart1 = center.add(direction.scale(cannonOffset))
                                          .add(perpendicular.scale(-offsetDistance));
            Vector2D bulletTopLeft1 = new Vector2D(bulletStart1.x() - 2, bulletStart1.y() - 2);
            Bullet bullet1 = new Bullet(bulletTopLeft1, direction, 240);
            bullet1.setCanPenetrate(bulletCanPenetrate);
            bullet1.setOneShotMode(oneShotMode);
            bullets.add(bullet1);
            
            // 第二个子弹（右侧）
            Vector2D bulletStart2 = center.add(direction.scale(cannonOffset))
                                          .add(perpendicular.scale(offsetDistance));
            Vector2D bulletTopLeft2 = new Vector2D(bulletStart2.x() - 2, bulletStart2.y() - 2);
            Bullet bullet2 = new Bullet(bulletTopLeft2, direction, 240);
            bullet2.setCanPenetrate(bulletCanPenetrate);
            bullet2.setOneShotMode(oneShotMode);
            bullets.add(bullet2);
            
            return bullets;
        } else {
            // 普通射击
            Bullet bullet = tryFireInternal().orElse(null);
            if (bullet != null) {
                List<Bullet> bullets = new ArrayList<>();
                bullets.add(bullet);
                return bullets;
            }
            return new ArrayList<>();
        }
    }
    
    /**
     * 尝试发射激光（开始蓄力或完成蓄力）
     * @param deltaSeconds 时间增量
     * @return 如果完成蓄力并发射激光，返回激光列表（组合效果时返回2条，否则返回1条）；否则返回空列表
     */
    public List<Laser> tryFireLaser(double deltaSeconds, double mapWidth, double mapHeight) {
        if (laserAmmo <= 0) {
            return new ArrayList<>(); // 没有激光弹药
        }
        
        // 确定蓄力时间（大激光2秒，普通激光1秒）
        double chargeDuration = megaLaserEnabled ? MEGA_LASER_CHARGE_DURATION : LASER_CHARGE_DURATION;
        
        if (!isChargingLaser) {
            // 开始蓄力
            isChargingLaser = true;
            laserChargeTime = 0.0;
            return new ArrayList<>();
        } else {
            // 继续蓄力
            laserChargeTime += deltaSeconds;
            if (laserChargeTime >= chargeDuration) {
                // 蓄力完成，发射激光
                isChargingLaser = false;
                laserChargeTime = 0.0;
                laserAmmo--; // 消耗一次使用次数
                
                List<Laser> lasers = new ArrayList<>();
                Vector2D center = center();
                Vector2D direction = facingDirection();
                
                // 检查是否有大激光效果
                if (megaLaserEnabled) {
                    // 大激光：发射一条宽度为两个砖块的激光，可以破坏障碍物
                    lasers.add(new Laser(center, direction, mapWidth, mapHeight, true));
                    megaLaserEnabled = false; // 大激光是一次性的，使用后清除
                } else {
                    // 检查是否有组合效果（同时拥有激光武器和散射子弹效果）
                    boolean hasComboEffect = scatterShotActive;
                    
                    if (hasComboEffect) {
                        // 组合效果：发射两条激光，类似散射子弹
                        // 计算垂直于方向的向量（用于偏移）
                        Vector2D perpendicular = new Vector2D(-direction.y(), direction.x());
                        double offsetDistance = 8.0; // 半个砖块距离（16/2）
                        
                        // 第一条激光（左侧）
                        Vector2D startPoint1 = center.add(perpendicular.scale(-offsetDistance));
                        lasers.add(new Laser(startPoint1, direction, mapWidth, mapHeight));
                        
                        // 第二条激光（右侧）
                        Vector2D startPoint2 = center.add(perpendicular.scale(offsetDistance));
                        lasers.add(new Laser(startPoint2, direction, mapWidth, mapHeight));
                    } else {
                        // 普通激光：发射一条
                        lasers.add(new Laser(center, direction, mapWidth, mapHeight));
                    }
                }
                
                return lasers;
            }
            return new ArrayList<>();
        }
    }
    
    /**
     * 取消激光蓄力（如果玩家移动或做其他操作）
     */
    public void cancelLaserCharge() {
        if (isChargingLaser) {
            isChargingLaser = false;
            laserChargeTime = 0.0;
        }
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
        // 更新激光蓄力（如果正在蓄力）
        if (isChargingLaser) {
            laserChargeTime += deltaSeconds;
            if (laserChargeTime >= LASER_CHARGE_DURATION) {
                // 蓄力完成，但激光发射由tryFireLaser处理
                // 这里只是更新状态
            }
        }
        // 更新气垫效果消失后的计时
        if (hovercraftDeactivatedTime >= 0) {
            hovercraftDeactivatedTime += deltaSeconds;
            // 如果超过宽限期，重置为-1（表示已过期）
            if (hovercraftDeactivatedTime > HOVERCRAFT_GRACE_PERIOD) {
                hovercraftDeactivatedTime = -1.0;
            }
        }
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
    
    /**
     * 装备激光武器
     */
    public void equipLaserWeapon() {
        laserAmmo = 3; // 激光武器可以使用3次
    }
    
    /**
     * 启用大激光（当已有激光武器时再次拾取激光武器触发）
     */
    public void enableMegaLaser() {
        megaLaserEnabled = true;
        // 如果激光弹药为0，给一次使用机会
        if (laserAmmo == 0) {
            laserAmmo = 1;
        }
    }
    
    /**
     * 设置散射子弹激活状态
     */
    public void setScatterShotActive(boolean active) {
        this.scatterShotActive = active;
    }
    
    /**
     * 设置气垫激活状态
     */
    public void setHovercraftActive(boolean active) {
        if (this.hovercraftActive && !active) {
            // 气垫效果从激活变为非激活，记录时间
            this.hovercraftDeactivatedTime = 0.0; // 将在tick中更新
        }
        this.hovercraftActive = active;
        if (active) {
            // 重新激活时重置时间
            this.hovercraftDeactivatedTime = -1.0;
        }
    }
    
    /**
     * 获取激光武器剩余次数
     */
    public int getLaserAmmo() {
        return laserAmmo;
    }
    
    /**
     * 是否正在蓄力激光
     */
    public boolean isChargingLaser() {
        return isChargingLaser;
    }
    
    /**
     * 获取激光蓄力进度（0.0-1.0）
     */
    public double getLaserChargeProgress() {
        double chargeDuration = megaLaserEnabled ? MEGA_LASER_CHARGE_DURATION : LASER_CHARGE_DURATION;
        return Math.min(1.0, laserChargeTime / chargeDuration);
    }
    
    /**
     * 是否激活散射子弹
     */
    public boolean isScatterShotActive() {
        return scatterShotActive;
    }
    
    /**
     * 是否激活气垫
     */
    public boolean isHovercraftActive() {
        return hovercraftActive;
    }
    
    /**
     * 是否启用大激光
     */
    public boolean isMegaLaserEnabled() {
        return megaLaserEnabled;
    }
    
    /**
     * 获取气垫效果消失后的经过时间（如果未消失或已过期则返回-1）
     */
    public double getHovercraftDeactivatedTime() {
        return hovercraftDeactivatedTime;
    }
    
    /**
     * 是否在气垫效果消失后的宽限期内
     */
    public boolean isInHovercraftGracePeriod() {
        return hovercraftDeactivatedTime >= 0 && hovercraftDeactivatedTime <= HOVERCRAFT_GRACE_PERIOD;
    }
    
    /**
     * 重写takeDamage方法，检查无敌模式
     */
    @Override
    public void takeDamage(int damage) {
        if (!godMode) {
            super.takeDamage(damage);
        }
        // 无敌模式下不受到伤害
    }
    
    /**
     * 设置无敌模式（控制台命令）
     */
    public void setGodMode(boolean enabled) {
        this.godMode = enabled;
    }
    
    /**
     * 获取无敌模式状态
     */
    public boolean isGodMode() {
        return godMode;
    }
}

