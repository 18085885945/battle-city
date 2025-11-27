package com.battlecity.model.tank;

public enum EnemyTier {
    NORMAL(TankType.ENEMY_NORMAL),
    ELITE(TankType.ENEMY_ELITE);

    private final TankType mappedType;

    EnemyTier(TankType mappedType) {
        this.mappedType = mappedType;
    }

    public TankType asTankType() {
        return mappedType;
    }
}

