package com.battlecity.ai;

import com.battlecity.ai.behavior.BehaviorState;
import com.battlecity.model.GameWorld;
import com.battlecity.model.Vector2D;
import com.battlecity.model.projectile.Bullet;
import com.battlecity.model.tank.EnemyTank;
import com.battlecity.model.tank.PlayerTank;

import java.util.*;

/**
 * 敌方坦克AI控制器
 */
public class EnemyAIController {
    
    private static final double BRICK_SIZE = 15.0;
    private static final double CRAZY_MODE_TIME = 600.0; // 10分钟 = 600秒
    private static final double SEARCH_DISAPPEAR_TIME = 3.0; // 玩家消失3秒后进入寻找模式
    private static final double SEARCH_DURATION = 5.0; // 寻找模式持续5秒
    private static final double CALL_RANGE = 40 * BRICK_SIZE; // 呼叫范围：40个砖块
    private static final double SEARCH_RANGE = 30 * BRICK_SIZE; // 寻找范围：30个砖块
    
    // 每个坦克的AI状态
    private final Map<EnemyTank, EnemyAIState> tankStates = new HashMap<>();
    private double gameTime = 0.0; // 游戏时间
    
    /**
     * 更新所有敌方坦克的AI
     */
    public void update(GameWorld world, List<EnemyTank> enemies, double deltaSeconds) {
        gameTime += deltaSeconds;
        
        // 清理已死亡坦克的状态
        tankStates.keySet().removeIf(tank -> !tank.alive() || !enemies.contains(tank));
        
        // 为每个坦克初始化状态
        for (EnemyTank enemy : enemies) {
            if (!tankStates.containsKey(enemy)) {
                tankStates.put(enemy, new EnemyAIState());
            }
        }
        
        // 检查是否进入疯狂模式
        boolean crazyMode = gameTime >= CRAZY_MODE_TIME;
        
        // 更新每个坦克的AI
        for (EnemyTank enemy : enemies) {
            if (!enemy.alive()) {
                continue;
            }
            
            EnemyAIState state = tankStates.get(enemy);
            
            // 疯狂模式：所有坦克攻击基地
            if (crazyMode) {
                state.currentState = BehaviorState.CRAZY;
            }
            
            // 根据状态执行相应行为
            switch (state.currentState) {
                case PATROL -> updatePatrol(world, enemy, state, deltaSeconds);
                case CHASE -> updateChase(world, enemy, state, deltaSeconds);
                case SEARCH -> updateSearch(world, enemy, state, deltaSeconds);
                case CRAZY -> updateCrazy(world, enemy, state, deltaSeconds);
            }
        }
    }
    
    /**
     * 巡逻模式：随机移动
     */
    private void updatePatrol(GameWorld world, EnemyTank enemy, EnemyAIState state, double deltaSeconds) {
        state.patrolChangeTimer += deltaSeconds;
        
        // 每2秒改变一次巡逻方向
        if (state.patrolTarget == null || state.patrolChangeTimer >= 2.0) {
            state.patrolChangeTimer = 0.0;
            // 随机选择一个方向（上下左右）
            Random random = new Random();
            int direction = random.nextInt(4); // 0:上, 1:下, 2:左, 3:右
            double distance = 50 + random.nextDouble() * 100; // 50-150像素
            Vector2D enemyCenter = enemy.center();
            
            switch (direction) {
                case 0 -> state.patrolTarget = new Vector2D(enemyCenter.x(), enemyCenter.y() - distance); // 上
                case 1 -> state.patrolTarget = new Vector2D(enemyCenter.x(), enemyCenter.y() + distance); // 下
                case 2 -> state.patrolTarget = new Vector2D(enemyCenter.x() - distance, enemyCenter.y()); // 左
                case 3 -> state.patrolTarget = new Vector2D(enemyCenter.x() + distance, enemyCenter.y()); // 右
            }
        }
        
        // 移动到巡逻目标
        moveTowards(world, enemy, state.patrolTarget, deltaSeconds);
        
        // 检查是否看到玩家
        if (canSeePlayer(world, enemy)) {
            state.currentState = BehaviorState.CHASE;
            state.lastKnownPlayerPosition = world.playerTank().center();
            state.lastSeenTimer = 0.0;
            // 呼叫附近坦克
            callNearbyTanks(world, enemy, world.enemyTanks());
        }
    }
    
    /**
     * 追击模式：发现玩家后追击并攻击（使用路径查找走直线）
     */
    private void updateChase(GameWorld world, EnemyTank enemy, EnemyAIState state, double deltaSeconds) {
        PlayerTank player = world.playerTank();
        if (player == null || !player.alive()) {
            state.currentState = BehaviorState.PATROL;
            return;
        }
        
        Vector2D playerCenter = player.center();
        Vector2D enemyCenter = enemy.center();
        
        // 检查是否还能看到玩家
        if (canSeePlayer(world, enemy)) {
            state.lastKnownPlayerPosition = playerCenter;
            state.lastSeenTimer = 0.0;
            
            // 使用BFS寻路找到可以攻击玩家的位置（走直线，只能上下左右）
            Vector2D attackPosition = findAttackPosition(world, enemy, playerCenter);
            if (attackPosition != null) {
                // 使用路径查找走直线到攻击位置（只能上下左右）
                // 如果路径为空或已走完，或当前位置与路径起点距离太远，重新计算路径
                if (state.currentPath == null || state.pathIndex >= state.currentPath.size()) {
                    // 重新计算路径
                    state.currentPath = PathFinder.findPath(world, enemyCenter, attackPosition, enemy);
                    state.pathIndex = 0;
                } else if (!state.currentPath.isEmpty()) {
                    // 检查路径起点是否与当前位置匹配（允许一定误差）
                    Vector2D pathStart = state.currentPath.get(0);
                    double distToStart = enemyCenter.subtract(pathStart).length();
                    if (distToStart > 30) {
                        // 位置偏差太大，重新计算路径
                        state.currentPath = PathFinder.findPath(world, enemyCenter, attackPosition, enemy);
                        state.pathIndex = 0;
                    }
                }
                
                // 沿着路径移动（只能上下左右）
                if (state.currentPath != null && !state.currentPath.isEmpty() && state.pathIndex < state.currentPath.size()) {
                    Vector2D nextWaypoint = state.currentPath.get(state.pathIndex);
                    double distance = enemyCenter.subtract(nextWaypoint).length();
                    
                    if (distance < 20) {
                        // 到达当前路径点，移动到下一个
                        state.pathIndex++;
                    } else {
                        // 移动到当前路径点（只能上下左右）
                        moveTowards(world, enemy, nextWaypoint, deltaSeconds);
                    }
                } else {
                    // 没有路径，直接移动（只能上下左右）
                    moveTowards(world, enemy, attackPosition, deltaSeconds);
                }
                
                // 如果已经在攻击位置，尝试攻击
                double distanceToTarget = enemyCenter.subtract(attackPosition).length();
                if (distanceToTarget < 50) { // 距离足够近
                    tryAttack(world, enemy, playerCenter);
                }
            }
        } else {
            // 看不到玩家，计时
            state.lastSeenTimer += deltaSeconds;
            if (state.lastSeenTimer >= SEARCH_DISAPPEAR_TIME) {
                // 进入寻找模式
                state.currentState = BehaviorState.SEARCH;
                state.searchTimer = 0.0;
                state.currentPath = null; // 清除路径
                state.pathIndex = 0;
            } else {
                // 继续向最后已知位置移动（使用路径查找走直线，只能上下左右）
                if (state.lastKnownPlayerPosition != null) {
                    // 如果路径为空或已走完，或当前位置与路径起点距离太远，重新计算路径
                    if (state.currentPath == null || state.pathIndex >= state.currentPath.size()) {
                        // 重新计算路径
                        state.currentPath = PathFinder.findPath(world, enemyCenter, state.lastKnownPlayerPosition, enemy);
                        state.pathIndex = 0;
                    } else if (!state.currentPath.isEmpty()) {
                        // 检查路径起点是否与当前位置匹配（允许一定误差）
                        Vector2D pathStart = state.currentPath.get(0);
                        double distToStart = enemyCenter.subtract(pathStart).length();
                        if (distToStart > 30) {
                            // 位置偏差太大，重新计算路径
                            state.currentPath = PathFinder.findPath(world, enemyCenter, state.lastKnownPlayerPosition, enemy);
                            state.pathIndex = 0;
                        }
                    }
                    
                    // 沿着路径移动
                    if (state.currentPath != null && !state.currentPath.isEmpty() && state.pathIndex < state.currentPath.size()) {
                        Vector2D nextWaypoint = state.currentPath.get(state.pathIndex);
                        double distance = enemyCenter.subtract(nextWaypoint).length();
                        
                        if (distance < 20) {
                            state.pathIndex++;
                        } else {
                            moveTowards(world, enemy, nextWaypoint, deltaSeconds);
                        }
                    } else {
                        moveTowards(world, enemy, state.lastKnownPlayerPosition, deltaSeconds);
                    }
                }
            }
        }
    }
    
    /**
     * 寻找模式：在玩家最后消失位置附近寻找
     */
    private void updateSearch(GameWorld world, EnemyTank enemy, EnemyAIState state, double deltaSeconds) {
        state.searchTimer += deltaSeconds;
        
        // 寻找模式持续5秒后回到巡逻
        if (state.searchTimer >= SEARCH_DURATION) {
            state.currentState = BehaviorState.PATROL;
            state.lastKnownPlayerPosition = null;
            return;
        }
        
        // 在最后已知位置附近30个砖块范围内寻找
        if (state.lastKnownPlayerPosition != null) {
            Vector2D searchCenter = state.lastKnownPlayerPosition;
            Vector2D enemyCenter = enemy.center();
            
            // 如果距离搜索中心太远，移动到搜索中心
            double distance = enemyCenter.subtract(searchCenter).length();
            if (distance > SEARCH_RANGE) {
                moveTowards(world, enemy, searchCenter, deltaSeconds);
            } else {
                // 在搜索范围内随机移动（只能上下左右）
                Random random = new Random();
                int direction = random.nextInt(4); // 0:上, 1:下, 2:左, 3:右
                double searchDistance = random.nextDouble() * SEARCH_RANGE;
                Vector2D searchTarget;
                
                switch (direction) {
                    case 0 -> searchTarget = new Vector2D(searchCenter.x(), searchCenter.y() - searchDistance); // 上
                    case 1 -> searchTarget = new Vector2D(searchCenter.x(), searchCenter.y() + searchDistance); // 下
                    case 2 -> searchTarget = new Vector2D(searchCenter.x() - searchDistance, searchCenter.y()); // 左
                    case 3 -> searchTarget = new Vector2D(searchCenter.x() + searchDistance, searchCenter.y()); // 右
                    default -> searchTarget = searchCenter;
                }
                moveTowards(world, enemy, searchTarget, deltaSeconds);
            }
        }
        
        // 检查是否重新看到玩家
        if (canSeePlayer(world, enemy)) {
            state.currentState = BehaviorState.CHASE;
            state.lastKnownPlayerPosition = world.playerTank().center();
            state.lastSeenTimer = 0.0;
        }
    }
    
    /**
     * 疯狂模式：攻击基地
     */
    private void updateCrazy(GameWorld world, EnemyTank enemy, EnemyAIState state, double deltaSeconds) {
        Vector2D basePos = world.base().position();
        Vector2D baseSize = new Vector2D(world.base().size().width(), world.base().size().height());
        Vector2D baseCenter = basePos.add(baseSize.scale(0.5));
        Vector2D enemyCenter = enemy.center();
        
        // 使用BFS寻路到基地
        if (state.currentPath == null || state.pathIndex >= state.currentPath.size()) {
            // 重新计算路径
            state.currentPath = PathFinder.findPath(world, enemyCenter, baseCenter, enemy);
            state.pathIndex = 0;
        }
        
        // 沿着路径移动
        if (state.currentPath != null && !state.currentPath.isEmpty() && state.pathIndex < state.currentPath.size()) {
            Vector2D nextWaypoint = state.currentPath.get(state.pathIndex);
            double distance = enemyCenter.subtract(nextWaypoint).length();
            
            if (distance < 20) {
                // 到达当前路径点，移动到下一个
                state.pathIndex++;
            } else {
                // 移动到当前路径点
                moveTowards(world, enemy, nextWaypoint, deltaSeconds);
            }
        }
        
        // 如果接近基地，尝试攻击
        double distanceToBase = enemyCenter.subtract(baseCenter).length();
        if (distanceToBase < 200) { // 距离基地200像素内
            // 检查是否有砖块阻挡
            if (hasClearShotToBase(world, enemy)) {
                tryAttack(world, enemy, baseCenter);
            } else {
                // 尝试攻击阻挡的砖块
                tryAttackObstacles(world, enemy);
            }
        }
    }
    
    /**
     * 检查是否能看到玩家
     */
    private boolean canSeePlayer(GameWorld world, EnemyTank enemy) {
        PlayerTank player = world.playerTank();
        if (player == null || !player.alive()) {
            return false;
        }
        
        Vector2D playerCenter = player.center();
        
        // 检查视野
        if (!VisionSystem.canSee(enemy, playerCenter)) {
            return false;
        }
        
        // 检查是否有障碍物阻挡
        return VisionSystem.hasLineOfSight(enemy.center(), playerCenter, world.obstacles());
    }
    
    /**
     * 找到可以攻击玩家的位置
     */
    private Vector2D findAttackPosition(GameWorld world, EnemyTank enemy, Vector2D target) {
        // 简化实现：在目标附近寻找一个可以攻击的位置
        // 实际可以使用更复杂的算法
        Vector2D enemyCenter = enemy.center();
        double distance = enemyCenter.subtract(target).length();
        
        // 如果距离合适（100-200像素），当前位置就可以攻击
        if (distance >= 100 && distance <= 200) {
            return enemyCenter;
        }
        
        // 否则使用BFS寻路到目标附近
        List<Vector2D> path = PathFinder.findPath(world, enemyCenter, target, enemy);
        if (!path.isEmpty() && path.size() > 1) {
            // 返回路径中距离目标合适的位置
            for (int i = path.size() - 1; i >= 0; i--) {
                Vector2D pos = path.get(i);
                double dist = pos.subtract(target).length();
                if (dist >= 100 && dist <= 200) {
                    return pos;
                }
            }
            // 如果找不到合适位置，返回路径的最后一个点
            return path.get(path.size() - 1);
        }
        
        return target;
    }
    
    /**
     * 移动到目标位置（只能上下左右移动）
     */
    private void moveTowards(GameWorld world, EnemyTank enemy, Vector2D target, double deltaSeconds) {
        Vector2D current = enemy.center();
        Vector2D direction = target.subtract(current);
        double dx = direction.x();
        double dy = direction.y();
        
        // 如果距离很近，不移动
        if (Math.abs(dx) < 5 && Math.abs(dy) < 5) {
            return;
        }
        
        // 选择主要移动方向（上下左右）
        if (Math.abs(dx) > Math.abs(dy)) {
            // 水平移动
            if (dx > 0) {
                enemy.moveRight(deltaSeconds);
            } else {
                enemy.moveLeft(deltaSeconds);
            }
        } else {
            // 垂直移动
            if (dy > 0) {
                enemy.moveDown(deltaSeconds);
            } else {
                enemy.moveUp(deltaSeconds);
            }
        }
    }
    
    /**
     * 尝试攻击目标（只能向上下左右攻击）
     */
    private void tryAttack(GameWorld world, EnemyTank enemy, Vector2D target) {
        Vector2D current = enemy.center();
        Vector2D direction = target.subtract(current);
        double dx = Math.abs(direction.x());
        double dy = Math.abs(direction.y());
        
        // 先调整坦克面向目标方向（只能上下左右）
        if (dx > dy) {
            // 水平方向
            if (direction.x() > 0) {
                enemy.moveRight(0); // 只是调整方向，不移动
            } else {
                enemy.moveLeft(0);
            }
        } else {
            // 垂直方向
            if (direction.y() > 0) {
                enemy.moveDown(0);
            } else {
                enemy.moveUp(0);
            }
        }
        
        // 检查冷却时间并开火
        Optional<Bullet> bulletOpt = enemy.tryFireInternal();
        if (bulletOpt.isPresent()) {
            world.addEnemyBullet(bulletOpt.get());
        }
    }
    
    /**
     * 检查是否有到基地的清晰射击路径
     */
    private boolean hasClearShotToBase(GameWorld world, EnemyTank enemy) {
        Vector2D basePos = world.base().position();
        Vector2D baseSize = new Vector2D(world.base().size().width(), world.base().size().height());
        Vector2D baseCenter = basePos.add(baseSize.scale(0.5));
        return VisionSystem.hasLineOfSight(enemy.center(), baseCenter, world.obstacles());
    }
    
    /**
     * 尝试攻击障碍物（向当前面向方向射击，已经是上下左右之一）
     */
    private void tryAttackObstacles(GameWorld world, EnemyTank enemy) {
        // 向当前面向方向射击（已经是上下左右之一）
        Optional<Bullet> bulletOpt = enemy.tryFireInternal();
        if (bulletOpt.isPresent()) {
            world.addEnemyBullet(bulletOpt.get());
        }
    }
    
    /**
     * 呼叫附近坦克进入追击模式
     */
    private void callNearbyTanks(GameWorld world, EnemyTank caller, List<EnemyTank> allEnemies) {
        Vector2D callerCenter = caller.center();
        
        for (EnemyTank other : allEnemies) {
            if (other == caller || !other.alive()) {
                continue;
            }
            
            Vector2D otherCenter = other.center();
            double distance = callerCenter.subtract(otherCenter).length();
            
            if (distance <= CALL_RANGE) {
                EnemyAIState otherState = tankStates.get(other);
                if (otherState != null && otherState.currentState == BehaviorState.PATROL) {
                    otherState.currentState = BehaviorState.CHASE;
                    otherState.lastKnownPlayerPosition = world.playerTank().center();
                    otherState.lastSeenTimer = 0.0;
                }
            }
        }
    }
    
    /**
     * 获取所有敌方坦克
     */
    private List<EnemyTank> enemies(GameWorld world) {
        return world.enemyTanks();
    }
    
    /**
     * 重置游戏时间（新游戏开始时调用）
     */
    public void reset() {
        gameTime = 0.0;
        tankStates.clear();
    }
}
