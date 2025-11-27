package com.battlecity.engine.state;

/**
 * 游戏模式类型枚举
 */
public enum GameModeType {
    /**
     * 经典模式：保护基地，消灭所有敌人
     */
    CLASSIC,
    
    /**
     * 无尽模式：无限波次敌人，生存时间越长得分越高
     */
    ENDLESS,
    
    /**
     * 限时模式：规定时间内完成目标
     */
    TIMED
}

