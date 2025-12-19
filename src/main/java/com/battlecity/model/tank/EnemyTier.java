package com.battlecity.model.tank;

public enum EnemyTier {
    NORMAL(TankType.ENEMY_NORMAL),
    ELITE(TankType.ENEMY_ELITE),
    ELITE_SPEED(TankType.ENEMY_ELITE_SPEED),      // 速度精英怪
    ELITE_FIRERATE(TankType.ENEMY_ELITE_FIRERATE), // 射速精英怪
    ELITE_AI(TankType.ENEMY_ELITE_AI),            // 高级AI精英怪
    ELITE_HEALTH(TankType.ENEMY_ELITE_HEALTH);     // 生命精英怪

    private final TankType mappedType;

    EnemyTier(TankType mappedType) {
        this.mappedType = mappedType;
    }

    public TankType asTankType() {
        return mappedType;
    }
    
    /**
     * 判断是否为精英怪类型
     */
    public boolean isElite() {
        return this != NORMAL;
    }
}

