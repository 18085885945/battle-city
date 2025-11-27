package com.battlecity.ai;

import com.battlecity.ai.behavior.BehaviorState;
import com.battlecity.model.GameWorld;
import com.battlecity.model.tank.EnemyTank;

import java.util.List;

/**
 * 简化版 AI 控制器，后续接入行为树与寻路。
 */
public class EnemyAIController {

    private BehaviorState currentState = BehaviorState.PATROL;

    public void update(GameWorld world, List<EnemyTank> enemies, double deltaSeconds) {
        switch (currentState) {
            case PATROL -> patrol(enemies, deltaSeconds);
            case CHASE -> chase(world, enemies, deltaSeconds);
            case ATTACK -> attack(world, enemies);
            case EVADE -> evade(enemies, deltaSeconds);
        }
    }

    private void patrol(List<EnemyTank> enemies, double deltaSeconds) {
        // TODO: 随机巡逻逻辑
    }

    private void chase(GameWorld world, List<EnemyTank> enemies, double deltaSeconds) {
        // TODO: BFS / A* 寻路追击玩家
    }

    private void attack(GameWorld world, List<EnemyTank> enemies) {
        // TODO: 射击判断
    }

    private void evade(List<EnemyTank> enemies, double deltaSeconds) {
        // TODO: 躲避逻辑
    }
}

