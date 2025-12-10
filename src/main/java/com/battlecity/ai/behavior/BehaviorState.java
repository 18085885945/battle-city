package com.battlecity.ai.behavior;

/**
 * 敌人 AI 行为状态。
 */
public enum BehaviorState {
    PATROL,      // 巡逻模式：随机移动
    CHASE,       // 追击模式：发现玩家后追击
    SEARCH,      // 寻找模式：玩家消失后在最后位置附近寻找
    CRAZY        // 疯狂模式：10分钟后攻击基地
}

