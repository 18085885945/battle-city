package com.battlecity.model.powerup;

/**
 * 道具类型枚举
 */
public enum PowerUpType {
    /** 增加子弹射击速度 */
    BULLET_SPEED,
    /** 增加子弹伤害，可破坏铁块 */
    BULLET_PENETRATION,
    /** 秒杀效果，一击摧毁所有坦克 */
    ONE_SHOT,
    /** 增加坦克移动速度 */
    TANK_SPEED,
    /** 恢复坦克生命值 */
    HEALTH_RESTORE,
    /** 冻结所有敌方坦克 */
    FREEZE
}