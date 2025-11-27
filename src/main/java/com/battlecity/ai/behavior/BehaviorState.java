package com.battlecity.ai.behavior;

/**
 * 敌人AI行为状态枚举
 */
public enum BehaviorState {
    /**
     * 巡逻：随机移动
     */
    PATROL,
    
    /**
     * 追击：发现玩家后追踪
     */
    CHASE,
    
    /**
     * 攻击：智能射击判断
     */
    ATTACK,
    
    /**
     * 躲避：规避障碍物
     */
    EVADE
}

