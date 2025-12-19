package com.battlecity.model.tank;

import com.battlecity.model.Vector2D;

/**
 * 敌方坦克创建工厂（框架）。
 *
 * <p>目的：把“按tier决定的属性/构造方式”集中起来，避免散落在生成/AI/测试代码里。
 * 后续如果要从配置文件读取精英参数，也只需要改这里。
 */
public final class EnemyTankFactory {

    private EnemyTankFactory() {
    }

    public static EnemyTank create(Vector2D position, EnemyTier tier) {
        return create(position, tier, defaultAttributes(tier));
    }

    /**
     * 创建敌方坦克（允许调用者提供属性，用于AI临时碰撞测试等场景）。
     */
    public static EnemyTank create(Vector2D position, EnemyTier tier, TankAttributes attributes) {
        switch (tier) {
            case ELITE:
                return new EliteEnemyTank(position, attributes);
            case ELITE_SPEED:
                return new SpeedEliteEnemyTank(position, attributes);
            case ELITE_FIRERATE:
                return new FireRateEliteEnemyTank(position, attributes);
            case ELITE_AI:
                return new AIEliteEnemyTank(position, attributes);
            case ELITE_HEALTH:
                return new HealthEliteEnemyTank(position, attributes);
            default:
                return new EnemyTank(position, attributes, tier);
        }
    }

    /**
     * 默认敌方坦克属性（后续可迁移到配置里）。
     */
    public static TankAttributes defaultAttributes(EnemyTier tier) {
        // 玩家坦克速度：140
        // 普通敌方坦克：速度98（玩家速度140的70%），攻速1秒1发（1000ms冷却）
        double normalSpeed = 98;
        double normalFireCooldown = 1000;
        
        switch (tier) {
            case ELITE:
                // 原有精英怪：稍快移动 + 稍快射速
                return new TankAttributes(110, 1.0, 800);
            case ELITE_SPEED:
                // 速度精英怪：速度为玩家的1.5倍 = 140 * 1.5 = 210
                return new TankAttributes(210, 1.0, normalFireCooldown);
            case ELITE_FIRERATE:
                // 射速精英怪：射速为普通敌方坦克的1.5倍 = 1000 / 1.5 ≈ 667ms
                return new TankAttributes(normalSpeed, 1.0, 667);
            case ELITE_AI:
                // AI精英怪：普通属性，但AI为疯狂模式
                return new TankAttributes(normalSpeed, 1.0, normalFireCooldown);
            case ELITE_HEALTH:
                // 生命精英怪：普通属性，但生命值为2
                return new TankAttributes(normalSpeed, 1.0, normalFireCooldown);
            default:
                return new TankAttributes(normalSpeed, 1.0, normalFireCooldown);
        }
    }
}


