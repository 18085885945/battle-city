package com.battlecity.ai;

import com.battlecity.model.tank.EnemyTank;
import com.battlecity.model.GameWorld;
import com.battlecity.model.Vector2D;
import com.battlecity.ai.behavior.BehaviorState;

/**
 * 敌人AI控制器
 * 负责控制敌方坦克的行为（巡逻、追击、攻击等）
 */
public class EnemyAIController {
    
    private final EnemyTank enemy;
    private BehaviorState currentState;
    private final GameWorld world;
    
    public EnemyAIController(EnemyTank enemy, GameWorld world) {
        this.enemy = enemy;
        this.world = world;
        this.currentState = BehaviorState.PATROL;
    }
    
    /**
     * 更新AI行为
     * @param deltaSeconds 时间增量
     */
    public void update(double deltaSeconds) {
        // 根据当前状态执行行为
        switch (currentState) {
            case PATROL:
                patrol(deltaSeconds);
                break;
            case CHASE:
                chase(deltaSeconds);
                break;
            case ATTACK:
                attack(deltaSeconds);
                break;
            case EVADE:
                evade(deltaSeconds);
                break;
        }
    }
    
    private void patrol(double deltaSeconds) {
        // 随机移动逻辑（待实现）
    }
    
    private void chase(double deltaSeconds) {
        // 追击玩家逻辑（待实现）
        if (world.playerTank() != null && world.playerTank().alive()) {
            Vector2D playerPos = world.playerTank().position();
            Vector2D enemyPos = enemy.position();
            // 计算方向并移动
        }
    }
    
    private void attack(double deltaSeconds) {
        // 攻击逻辑（待实现）
    }
    
    private void evade(double deltaSeconds) {
        // 躲避逻辑（待实现）
    }
    
    public BehaviorState getCurrentState() {
        return currentState;
    }
    
    public void setState(BehaviorState state) {
        this.currentState = state;
    }
}

