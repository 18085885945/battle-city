package com.battlecity.model.powerup;

import com.battlecity.model.Vector2D;

import java.util.Random;

/**
 * 道具工厂，用于生成随机道具
 */
public class PowerUpFactory {
    
    private static final Random random = new Random();
    private static final PowerUpType[] ALL_TYPES = PowerUpType.values();
    
    /**
     * 随机生成一个道具
     * @param position 道具生成位置
     * @return 生成的道具，如果50%概率不生成则返回null
     */
    public static PowerUp createRandomPowerUp(Vector2D position) {
        // 50%概率不生成道具
        if (random.nextDouble() < 0.5) {
            return null;
        }
        
        PowerUpType type = ALL_TYPES[random.nextInt(ALL_TYPES.length)];
        return createPowerUp(position, type);
    }
    
    /**
     * 根据类型生成道具
     * @param position 道具生成位置
     * @param type 道具类型
     * @return 生成的道具
     */
    public static PowerUp createPowerUp(Vector2D position, PowerUpType type) {
        switch (type) {
            case BULLET_SPEED:
                return new BulletSpeedPowerUp(position);
            case BULLET_PENETRATION:
                return new BulletPenetrationPowerUp(position);
            case ONE_SHOT:
                return new OneShotPowerUp(position);
            case TANK_SPEED:
                return new TankSpeedPowerUp(position);
            case HEALTH_RESTORE:
                return new HealthRestorePowerUp(position);
            case FREEZE:
                return new FreezePowerUp(position);
            case LASER:
                return new LaserWeapon(position);
            case SCATTER_SHOT:
                return new ScatterShotWeapon(position);
            case HOVERCRAFT:
                return new HovercraftWeapon(position);
            case AIRSTRIKE:
                return new AirstrikePowerUp(position);
            default:
                throw new IllegalArgumentException("未知的道具类型: " + type);
        }
    }
    
    /**
     * 创建武器（10%概率）
     * @param position 生成位置
     * @return 武器道具，如果未生成则返回null
     */
    public static PowerUp createWeapon(Vector2D position) {
        // 10%概率生成武器
        if (random.nextDouble() < 0.1) {
            // 50%概率激光，50%概率散射子弹
            if (random.nextDouble() < 0.5) {
                return new LaserWeapon(position);
            } else {
                return new ScatterShotWeapon(position);
            }
        }
        return null;
    }
}