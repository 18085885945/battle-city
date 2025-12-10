package com.battlecity.ai;

import com.battlecity.ai.behavior.BehaviorState;
import com.battlecity.model.Vector2D;

import java.util.List;

/**
 * 单个敌方坦克的AI状态
 */
public class EnemyAIState {
    public BehaviorState currentState = BehaviorState.PATROL;
    public Vector2D lastKnownPlayerPosition; // 最后已知的玩家位置
    public double searchTimer = 0.0; // 寻找模式计时器
    public double lastSeenTimer = 0.0; // 最后看到玩家的时间
    public List<Vector2D> currentPath; // 当前路径
    public int pathIndex = 0; // 当前路径索引
    public Vector2D patrolTarget; // 巡逻目标位置
    public double patrolChangeTimer = 0.0; // 巡逻方向改变计时器
    
    public void reset() {
        currentState = BehaviorState.PATROL;
        lastKnownPlayerPosition = null;
        searchTimer = 0.0;
        lastSeenTimer = 0.0;
        currentPath = null;
        pathIndex = 0;
        patrolTarget = null;
        patrolChangeTimer = 0.0;
    }
}

