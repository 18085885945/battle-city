package com.battlecity.ai;

import com.battlecity.ai.behavior.BehaviorState;
import com.battlecity.model.GameWorld;
import com.battlecity.model.Vector2D;
import com.battlecity.model.projectile.Bullet;
import com.battlecity.model.tank.EnemyTank;
import com.battlecity.model.tank.EnemyTankFactory;
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
            
            // AI精英怪始终处于疯狂模式（专门攻击基地）
            if (enemy.tier() == com.battlecity.model.tank.EnemyTier.ELITE_AI) {
                state.currentState = BehaviorState.CRAZY;
            }
            // 疯狂模式：所有坦克攻击基地
            else if (crazyMode) {
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
     * 巡逻模式：随机移动，但慢慢靠近基地（如果还没过场地一半）
     */
    private void updatePatrol(GameWorld world, EnemyTank enemy, EnemyAIState state, double deltaSeconds) {
        state.patrolChangeTimer += deltaSeconds;
        Vector2D enemyCenter = enemy.center();
        Vector2D baseCenter = world.base().center();
        
        // 检查是否过了场地一半（以y坐标判断，因为基地在下边缘）
        double mapHeight = world.levelDefinition().height();
        double halfHeight = mapHeight / 2.0;
        boolean pastHalfway = enemyCenter.y() > halfHeight;
        
        // 每2秒改变一次巡逻方向
        if (state.patrolTarget == null || state.patrolChangeTimer >= 2.0) {
            state.patrolChangeTimer = 0.0;
            Random random = new Random();
            
            // 如果过了场地一半，只随机移动；否则70%概率向基地方向移动，30%概率随机移动
            if (pastHalfway || random.nextDouble() >= 0.7) {
                // 完全随机移动（过了场地一半，或者30%概率）
                int direction = random.nextInt(4);
                double distance = 50 + random.nextDouble() * 100;
                
                switch (direction) {
                    case 0 -> state.patrolTarget = new Vector2D(enemyCenter.x(), enemyCenter.y() - distance); // 上
                    case 1 -> state.patrolTarget = new Vector2D(enemyCenter.x(), enemyCenter.y() + distance); // 下
                    case 2 -> state.patrolTarget = new Vector2D(enemyCenter.x() - distance, enemyCenter.y()); // 左
                    case 3 -> state.patrolTarget = new Vector2D(enemyCenter.x() + distance, enemyCenter.y()); // 右
                }
            } else {
                // 向基地方向移动（但加入一些随机性，不完全直线）
                Vector2D directionToBase = baseCenter.subtract(enemyCenter);
                double baseDistance = directionToBase.length();
                
                // 如果距离基地较远，直接向基地移动
                if (baseDistance > 200) {
                    // 向基地方向移动，但加入一些随机偏移
                    double offsetX = (random.nextDouble() - 0.5) * 100; // -50到50的随机偏移
                    double offsetY = (random.nextDouble() - 0.5) * 100;
                    state.patrolTarget = new Vector2D(
                        enemyCenter.x() + directionToBase.x() * 0.3 + offsetX,
                        enemyCenter.y() + directionToBase.y() * 0.3 + offsetY
                    );
                } else {
                    // 距离基地较近，随机选择一个方向，但偏向基地方向
                    int direction = random.nextInt(4);
                    double distance = 50 + random.nextDouble() * 100;
                    double baseBias = 0.3; // 30%的偏向基地
                    
                    double targetX = enemyCenter.x();
                    double targetY = enemyCenter.y();
                    
                    switch (direction) {
                        case 0 -> targetY -= distance * (1 - baseBias) + (baseCenter.y() < enemyCenter.y() ? distance * baseBias : 0); // 上
                        case 1 -> targetY += distance * (1 - baseBias) + (baseCenter.y() > enemyCenter.y() ? distance * baseBias : 0); // 下
                        case 2 -> targetX -= distance * (1 - baseBias) + (baseCenter.x() < enemyCenter.x() ? distance * baseBias : 0); // 左
                        case 3 -> targetX += distance * (1 - baseBias) + (baseCenter.x() > enemyCenter.x() ? distance * baseBias : 0); // 右
                    }
                    state.patrolTarget = new Vector2D(targetX, targetY);
                }
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
            state.currentPath = null;
            state.pathIndex = 0;
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
                // 检查是否需要重新计算路径
                boolean needRecalculate = false;
                
                // 如果路径为空或已走完，需要重新计算
                if (state.currentPath == null || state.pathIndex >= state.currentPath.size()) {
                    needRecalculate = true;
                } else if (!state.currentPath.isEmpty()) {
                    // 检查路径起点是否与当前位置匹配（允许一定误差，增加到50像素）
                    Vector2D pathStart = state.currentPath.get(0);
                    double distToStart = enemyCenter.subtract(pathStart).length();
                    if (distToStart > 50) {
                        // 位置偏差太大，重新计算路径
                        needRecalculate = true;
                    } else {
                        // 检查目标位置是否变化太大（如果攻击位置变化超过50像素，重新计算）
                        Vector2D currentTarget = state.pathIndex < state.currentPath.size() ? 
                            state.currentPath.get(state.currentPath.size() - 1) : null;
                        if (currentTarget != null) {
                            double targetChange = currentTarget.subtract(attackPosition).length();
                            if (targetChange > 50) {
                                needRecalculate = true;
                            }
                        }
                    }
                }
                
                // 如果需要重新计算路径
                if (needRecalculate) {
                    state.currentPath = PathFinder.findPath(world, enemyCenter, attackPosition, enemy);
                    state.pathIndex = 0;
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
                    // 没有有效路径，尝试直接移动到攻击位置（但限制距离，避免旋转）
                    double distanceToTarget = enemyCenter.subtract(attackPosition).length();
                    if (distanceToTarget > 10) { // 只有当距离足够远时才移动
                        moveTowards(world, enemy, attackPosition, deltaSeconds);
                    }
                    // 如果距离很近但没有路径，可能是被障碍物阻挡，保持当前位置
                }
                
                // 如果已经在攻击位置，尝试攻击
                double distanceToTarget = enemyCenter.subtract(attackPosition).length();
                if (distanceToTarget < 50) { // 距离足够近
                    // 检查子弹是否能命中玩家，如果不能，调整位置
                    if (!canHitPlayer(world, enemy, playerCenter)) {
                        // 无法命中，寻找可以命中的位置
                        Vector2D adjustedPosition = findShootablePosition(world, enemy, playerCenter);
                        if (adjustedPosition != null) {
                            // 移动到可以命中的位置
                            state.currentPath = PathFinder.findPath(world, enemyCenter, adjustedPosition, enemy);
                            state.pathIndex = 0;
                            moveTowards(world, enemy, adjustedPosition, deltaSeconds);
                        } else {
                            // 找不到可以命中的位置，仍然尝试攻击（可能可以破坏障碍物）
                            tryAttack(world, enemy, playerCenter);
                        }
                    } else {
                        // 可以命中，直接攻击
                        tryAttack(world, enemy, playerCenter);
                    }
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
                    // 检查是否需要重新计算路径
                    boolean needRecalculate = false;
                    
                    if (state.currentPath == null || state.pathIndex >= state.currentPath.size()) {
                        needRecalculate = true;
                    } else if (!state.currentPath.isEmpty()) {
                        Vector2D pathStart = state.currentPath.get(0);
                        double distToStart = enemyCenter.subtract(pathStart).length();
                        if (distToStart > 50) {
                            needRecalculate = true;
                        }
                    }
                    
                    if (needRecalculate) {
                        state.currentPath = PathFinder.findPath(world, enemyCenter, state.lastKnownPlayerPosition, enemy);
                        state.pathIndex = 0;
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
                        // 没有有效路径，尝试直接移动（但限制距离）
                        double distanceToTarget = enemyCenter.subtract(state.lastKnownPlayerPosition).length();
                        if (distanceToTarget > 10) {
                            moveTowards(world, enemy, state.lastKnownPlayerPosition, deltaSeconds);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 寻找模式：先向最后已知位置移动较远距离，然后随机向某个方向移动较长距离
     */
    private void updateSearch(GameWorld world, EnemyTank enemy, EnemyAIState state, double deltaSeconds) {
        state.searchTimer += deltaSeconds;
        
        // 寻找模式持续5秒后回到巡逻
        if (state.searchTimer >= SEARCH_DURATION) {
            state.currentState = BehaviorState.PATROL;
            state.lastKnownPlayerPosition = null;
            state.searchTarget = null;
            state.searchPhase1Complete = false;
            return;
        }
        
        if (state.lastKnownPlayerPosition == null) {
            return;
        }
        
        Vector2D enemyCenter = enemy.center();
        Vector2D searchCenter = state.lastKnownPlayerPosition;
        
        // 第一阶段：向最后已知位置移动较远距离（使用路径查找）
        if (!state.searchPhase1Complete) {
            // 计算目标位置：向最后已知位置方向移动较远距离（200-300像素）
            Vector2D directionToLastKnown = searchCenter.subtract(enemyCenter);
            double distanceToLastKnown = directionToLastKnown.length();
            
            if (distanceToLastKnown > 10) {
                // 如果还没有设置目标，或者已经到达目标，设置新的目标
                if (state.searchTarget == null) {
                    // 向最后已知位置方向移动200-300像素
                    Random random = new Random();
                    double moveDistance = 200 + random.nextDouble() * 100; // 200-300像素
                    double directionLength = directionToLastKnown.length();
                    if (directionLength > 0) {
                        Vector2D normalized = new Vector2D(
                            directionToLastKnown.x() / directionLength * moveDistance,
                            directionToLastKnown.y() / directionLength * moveDistance
                        );
                        state.searchTarget = new Vector2D(
                            enemyCenter.x() + normalized.x(),
                            enemyCenter.y() + normalized.y()
                        );
                        
                        // 使用路径查找
                        state.currentPath = PathFinder.findPath(world, enemyCenter, state.searchTarget, enemy);
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
                    // 没有路径，直接移动
                    double distanceToTarget = enemyCenter.subtract(state.searchTarget).length();
                    if (distanceToTarget > 10) {
                        moveTowards(world, enemy, state.searchTarget, deltaSeconds);
                    } else {
                        // 到达目标，进入第二阶段
                        state.searchPhase1Complete = true;
                        state.searchTarget = null;
                        state.currentPath = null;
                        state.pathIndex = 0;
                    }
                }
            } else {
                // 已经到达最后已知位置，进入第二阶段
                state.searchPhase1Complete = true;
                state.searchTarget = null;
            }
        } else {
            // 第二阶段：随机向某个方向移动较长距离（150-250像素）
            if (state.searchTarget == null || enemyCenter.subtract(state.searchTarget).length() < 20) {
                Random random = new Random();
                int direction = random.nextInt(4); // 0:上, 1:下, 2:左, 3:右
                double moveDistance = 150 + random.nextDouble() * 100; // 150-250像素
                
                switch (direction) {
                    case 0 -> state.searchTarget = new Vector2D(enemyCenter.x(), enemyCenter.y() - moveDistance); // 上
                    case 1 -> state.searchTarget = new Vector2D(enemyCenter.x(), enemyCenter.y() + moveDistance); // 下
                    case 2 -> state.searchTarget = new Vector2D(enemyCenter.x() - moveDistance, enemyCenter.y()); // 左
                    case 3 -> state.searchTarget = new Vector2D(enemyCenter.x() + moveDistance, enemyCenter.y()); // 右
                }
                
                // 使用路径查找
                state.currentPath = PathFinder.findPath(world, enemyCenter, state.searchTarget, enemy);
                state.pathIndex = 0;
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
                // 没有路径，直接移动
                double distanceToTarget = enemyCenter.subtract(state.searchTarget).length();
                if (distanceToTarget > 10) {
                    moveTowards(world, enemy, state.searchTarget, deltaSeconds);
                }
            }
        }
        
        // 检查是否重新看到玩家
        if (canSeePlayer(world, enemy)) {
            state.currentState = BehaviorState.CHASE;
            state.lastKnownPlayerPosition = world.playerTank().center();
            state.lastSeenTimer = 0.0;
            state.searchTarget = null;
            state.searchPhase1Complete = false;
            // 呼叫附近坦克
            callNearbyTanks(world, enemy, world.enemyTanks());
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
        } else {
            // 如果路径查找失败（返回空列表），使用备用方案：直接向基地方向移动
            // 这样可以确保AI精英怪至少会尝试移动，即使路径查找失败
            double distanceToBase = enemyCenter.subtract(baseCenter).length();
            if (distanceToBase > 20) { // 只有当距离足够远时才移动
                moveTowards(world, enemy, baseCenter, deltaSeconds);
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
     * 检查从当前位置发射的子弹是否能命中玩家
     */
    private boolean canHitPlayer(GameWorld world, EnemyTank enemy, Vector2D playerCenter) {
        Vector2D enemyCenter = enemy.center();
        Vector2D direction = playerCenter.subtract(enemyCenter);
        double dx = Math.abs(direction.x());
        double dy = Math.abs(direction.y());
        
        // 确定攻击方向（只能上下左右）
        Vector2D attackDirection;
        if (dx > dy) {
            // 水平方向
            attackDirection = direction.x() > 0 ? new Vector2D(1, 0) : new Vector2D(-1, 0);
        } else {
            // 垂直方向
            attackDirection = direction.y() > 0 ? new Vector2D(0, 1) : new Vector2D(0, -1);
        }
        
        // 计算子弹发射位置
        double cannonOffset = enemy.size().width() * 0.5;
        Vector2D bulletStart = enemyCenter.add(attackDirection.scale(cannonOffset));
        
        // 预测子弹路径，检查是否能到达玩家位置
        return canBulletReachTarget(world, bulletStart, attackDirection, playerCenter);
    }
    
    /**
     * 检查子弹是否能到达目标（预测子弹路径）
     * 检查子弹是否与玩家坦克的边界框碰撞，而不仅仅是中心点
     */
    private boolean canBulletReachTarget(GameWorld world, Vector2D bulletStart, Vector2D direction, Vector2D target) {
        double bulletSpeed = 200; // 子弹速度
        double maxDistance = bulletStart.subtract(target).length() * 1.5; // 最大检查距离（稍微超过目标距离）
        
        // 玩家坦克大小是26x26，所以边界框是中心点±13
        double playerTankSize = 26;
        double playerHalfSize = playerTankSize / 2.0;
        double playerLeft = target.x() - playerHalfSize;
        double playerRight = target.x() + playerHalfSize;
        double playerTop = target.y() - playerHalfSize;
        double playerBottom = target.y() + playerHalfSize;
        
        // 子弹大小是4x4，所以边界框是中心点±2
        double bulletSize = 4;
        double bulletHalfSize = bulletSize / 2.0;
        
        // 沿着子弹路径采样，检查是否被障碍物阻挡或命中玩家
        int samples = (int)(maxDistance / 2); // 每2像素采样一次，提高精度
        for (int i = 0; i <= samples; i++) {
            double t = (double) i / samples;
            // 计算子弹当前位置（使用实际时间步长）
            double timeStep = 0.016; // 约60fps的时间步长
            Vector2D bulletCenter = bulletStart.add(direction.scale(bulletSpeed * t * timeStep));
            
            // 子弹边界框
            double bulletLeft = bulletCenter.x() - bulletHalfSize;
            double bulletRight = bulletCenter.x() + bulletHalfSize;
            double bulletTop = bulletCenter.y() - bulletHalfSize;
            double bulletBottom = bulletCenter.y() + bulletHalfSize;
            
            // 检查子弹是否与玩家坦克边界框碰撞（AABB碰撞检测）
            if (bulletLeft < playerRight && bulletRight > playerLeft &&
                bulletTop < playerBottom && bulletBottom > playerTop) {
                return true; // 命中玩家坦克
            }
            
            // 检查是否与障碍物碰撞
            for (com.battlecity.model.world.Obstacle obstacle : world.obstacles()) {
                // 检查子弹边界框是否与障碍物碰撞
                if (bulletLeft < obstacle.right() && bulletRight > obstacle.left() &&
                    bulletTop < obstacle.bottom() && bulletBottom > obstacle.top()) {
                    // 如果障碍物是可破坏的砖块，子弹可以穿过（但会减速），继续检查
                    if (obstacle instanceof com.battlecity.model.world.BrickWall) {
                        // 砖块会被破坏，但子弹可能也会消失，这里简化处理：认为无法命中
                        return false;
                    } else if (obstacle instanceof com.battlecity.model.world.SteelWall) {
                        // 铁块阻挡，无法命中
                        return false;
                    }
                    // 河流不阻挡子弹
                }
            }
            
            // 检查是否超出地图边界
            if (bulletLeft < 0 || bulletRight > world.levelDefinition().width() ||
                bulletTop < 0 || bulletBottom > world.levelDefinition().height()) {
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * 检查点是否在障碍物内
     */
    private boolean isPointInObstacle(Vector2D point, com.battlecity.model.world.Obstacle obstacle) {
        return point.x() >= obstacle.position().x() 
            && point.x() <= obstacle.position().x() + obstacle.size().width()
            && point.y() >= obstacle.position().y()
            && point.y() <= obstacle.position().y() + obstacle.size().height();
    }
    
    /**
     * 找到可以射击到玩家的位置
     */
    private Vector2D findShootablePosition(GameWorld world, EnemyTank enemy, Vector2D playerCenter) {
        Vector2D enemyCenter = enemy.center();
        
        // 尝试在玩家周围寻找可以射击的位置
        // 策略：尝试玩家上下左右四个方向的位置
        double searchRadius = 150; // 搜索半径
        Vector2D[] searchDirections = {
            new Vector2D(0, -searchRadius),  // 上
            new Vector2D(0, searchRadius),   // 下
            new Vector2D(-searchRadius, 0),  // 左
            new Vector2D(searchRadius, 0)    // 右
        };
        
        for (Vector2D offset : searchDirections) {
            Vector2D candidatePos = playerCenter.add(offset);
            
            // 确保位置在地图内
            if (candidatePos.x() < 0 || candidatePos.x() > world.levelDefinition().width() ||
                candidatePos.y() < 0 || candidatePos.y() > world.levelDefinition().height()) {
                continue;
            }
            
            // 检查从该位置是否能命中玩家
            // 创建临时坦克位置进行测试
            com.battlecity.model.tank.TankAttributes attrs = enemy.attributes();
            EnemyTank testTank = EnemyTankFactory.create(
                new Vector2D(candidatePos.x() - enemy.size().width() / 2, 
                           candidatePos.y() - enemy.size().height() / 2),
                enemy.tier(),
                attrs
            );
            
            // 检查是否有障碍物阻挡
            boolean blocked = false;
            for (com.battlecity.model.world.Obstacle obstacle : world.obstacles()) {
                if (world.collisionDetector().collide(testTank, obstacle)) {
                    blocked = true;
                    break;
                }
            }
            
            if (!blocked) {
                // 检查从该位置是否能命中玩家
                if (canHitPlayerFromPosition(world, candidatePos, playerCenter)) {
                    return candidatePos;
                }
            }
        }
        
        // 如果四个方向都不行，尝试更近的位置
        for (double radius = 100; radius <= 200; radius += 20) {
            for (int angle = 0; angle < 360; angle += 45) {
                double rad = Math.toRadians(angle);
                Vector2D offset = new Vector2D(
                    Math.cos(rad) * radius,
                    Math.sin(rad) * radius
                );
                Vector2D candidatePos = playerCenter.add(offset);
                
                // 确保位置在地图内
                if (candidatePos.x() < 0 || candidatePos.x() > world.levelDefinition().width() ||
                    candidatePos.y() < 0 || candidatePos.y() > world.levelDefinition().height()) {
                    continue;
                }
                
                // 检查从该位置是否能命中玩家
                if (canHitPlayerFromPosition(world, candidatePos, playerCenter)) {
                    return candidatePos;
                }
            }
        }
        
        return null; // 找不到可以射击的位置
    }
    
    /**
     * 检查从指定位置是否能命中玩家
     */
    private boolean canHitPlayerFromPosition(GameWorld world, Vector2D position, Vector2D playerCenter) {
        Vector2D direction = playerCenter.subtract(position);
        double dx = Math.abs(direction.x());
        double dy = Math.abs(direction.y());
        
        // 确定攻击方向（只能上下左右）
        Vector2D attackDirection;
        if (dx > dy) {
            attackDirection = direction.x() > 0 ? new Vector2D(1, 0) : new Vector2D(-1, 0);
        } else {
            attackDirection = direction.y() > 0 ? new Vector2D(0, 1) : new Vector2D(0, -1);
        }
        
        // 计算子弹发射位置
        double tankSize = 26;
        double cannonOffset = tankSize * 0.5;
        Vector2D bulletStart = position.add(attackDirection.scale(cannonOffset));
        
        // 预测子弹路径
        return canBulletReachTarget(world, bulletStart, attackDirection, playerCenter);
    }
    
    /**
     * 移动到目标位置（只能上下左右移动，将斜线拆成两段直线）
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
        
        // 获取AI状态，用于跟踪两段移动的中间点
        EnemyAIState state = tankStates.get(enemy);
        if (state == null) {
            state = new EnemyAIState();
            tankStates.put(enemy, state);
        }
        
        // 检查目标位置是否变化很大（超过30像素），如果是，清除中间点
        if (state.originalTarget != null) {
            double targetChange = state.originalTarget.subtract(target).length();
            if (targetChange > 30) {
                // 目标位置变化较大，清除中间点，重新计算
                state.intermediateTarget = null;
                state.originalTarget = target;
            }
        } else {
            state.originalTarget = target;
        }
        
        // 如果dx和dy都不为0且都大于阈值（15像素），需要拆成两段直线
        // 增加阈值，减少频繁切换
        if (Math.abs(dx) > 15 && Math.abs(dy) > 15) {
            // 如果还没有设置中间点，或者中间点与当前目标不匹配，设置一个新的
            if (state.intermediateTarget == null) {
                // 选择先水平还是先垂直（根据距离决定）
                if (Math.abs(dx) > Math.abs(dy)) {
                    // 先水平移动，中间点：目标x，当前y
                    state.intermediateTarget = new Vector2D(target.x(), current.y());
                } else {
                    // 先垂直移动，中间点：当前x，目标y
                    state.intermediateTarget = new Vector2D(current.x(), target.y());
                }
            }
            
            // 先移动到中间点
            Vector2D toIntermediate = state.intermediateTarget.subtract(current);
            double distToIntermediate = toIntermediate.length();
            
            if (distToIntermediate > 10) { // 增加阈值，避免频繁切换
                // 移动到中间点（只走一个方向）
                boolean moved = false;
                if (Math.abs(toIntermediate.x()) > Math.abs(toIntermediate.y())) {
                    if (toIntermediate.x() > 0) {
                        moved = checkAndMove(world, enemy, 1, 0, deltaSeconds);
                    } else {
                        moved = checkAndMove(world, enemy, -1, 0, deltaSeconds);
                    }
                } else {
                    if (toIntermediate.y() > 0) {
                        moved = checkAndMove(world, enemy, 0, 1, deltaSeconds);
                    } else {
                        moved = checkAndMove(world, enemy, 0, -1, deltaSeconds);
                    }
                }
                
                // 如果移动失败（遇到不可破坏的障碍物），清除路径和中间点，让上层逻辑重新计算
                if (!moved) {
                    state.currentPath = null;
                    state.pathIndex = 0;
                    state.intermediateTarget = null;
                    state.originalTarget = null;
                    return;
                }
                
                if (moved) {
                    return; // 成功移动或处理了障碍物
                }
            } else {
                // 到达中间点，清除中间点，继续向目标移动
                state.intermediateTarget = null;
            }
        } else {
            // 不需要拆成两段，清除中间点
            state.intermediateTarget = null;
        }
        
        // 直接向目标移动（此时dx或dy中有一个接近0，或已经到达中间点）
        boolean moved = false;
        if (Math.abs(dx) > Math.abs(dy)) {
            // 水平移动
            if (dx > 0) {
                moved = checkAndMove(world, enemy, 1, 0, deltaSeconds);
            } else {
                moved = checkAndMove(world, enemy, -1, 0, deltaSeconds);
            }
        } else {
            // 垂直移动
            if (dy > 0) {
                moved = checkAndMove(world, enemy, 0, 1, deltaSeconds);
            } else {
                moved = checkAndMove(world, enemy, 0, -1, deltaSeconds);
            }
        }
        
        // 如果移动失败（遇到不可破坏的障碍物），清除当前路径，让上层逻辑重新计算路径
        if (!moved) {
            EnemyAIState aiState = tankStates.get(enemy);
            if (aiState != null) {
                aiState.currentPath = null;
                aiState.pathIndex = 0;
                aiState.intermediateTarget = null; // 清除中间点
                aiState.originalTarget = null;
            }
        }
    }
    
    /**
     * 检查前方是否有障碍物，如果有可破坏的障碍物则开火，如果不可破坏则返回false（需要绕过）
     * @param world 游戏世界
     * @param enemy 敌方坦克
     * @param dirX 移动方向x（-1, 0, 1）
     * @param dirY 移动方向y（-1, 0, 1）
     * @param deltaSeconds 时间增量
     * @return 如果成功移动或处理了障碍物返回true，如果需要绕过返回false
     */
    private boolean checkAndMove(GameWorld world, EnemyTank enemy, int dirX, int dirY, double deltaSeconds) {
        Vector2D currentPos = enemy.position();
        Vector2D currentCenter = enemy.center();
        double tankSize = enemy.size().width();
        
        // 获取AI状态，检查避免无限递归
        EnemyAIState state = tankStates.get(enemy);
        if (state == null) {
            state = new EnemyAIState();
            tankStates.put(enemy, state);
        }
        
        // 防止无限递归：如果尝试次数过多，直接返回false
        if (state.avoidanceAttempts > 5) {
            state.avoidanceAttempts = 0;
            return false;
        }
        
        // 计算移动后的位置
        com.battlecity.model.tank.TankAttributes attrs = enemy.attributes();
        double moveDistance = attrs.speed() * deltaSeconds;
        Vector2D nextPos = new Vector2D(
            currentPos.x() + dirX * moveDistance,
            currentPos.y() + dirY * moveDistance
        );
        
        // 创建临时坦克位置进行碰撞检测
        EnemyTank testTank = EnemyTankFactory.create(nextPos, enemy.tier(), attrs);
        
        // 检查与障碍物的碰撞
        for (com.battlecity.model.world.Obstacle obstacle : world.obstacles()) {
            if (world.collisionDetector().collide(testTank, obstacle)) {
                // 检查障碍物类型
                if (obstacle instanceof com.battlecity.model.world.BrickWall) {
                    // 可破坏的障碍物
                    Vector2D obstacleCenter = new Vector2D(
                        obstacle.position().x() + obstacle.size().width() / 2.0,
                        obstacle.position().y() + obstacle.size().height() / 2.0
                    );
                    
                    // 检查是否可以直接命中（坦克与障碍物在同一水平或垂直线上）
                    Vector2D toObstacle = obstacleCenter.subtract(currentCenter);
                    double dx = Math.abs(toObstacle.x());
                    double dy = Math.abs(toObstacle.y());
                    
                    // 如果坦克与障碍物在同一水平或垂直线上（误差5像素内）
                    if (dx < 5 || dy < 5) {
                        // 检测前方是否有连续的砖块行
                        Vector2D attackTarget = detectConsecutiveBricks(world, enemy, 
                            (com.battlecity.model.world.BrickWall) obstacle, dirX, dirY);
                        if (attackTarget != null) {
                            // 攻击连续砖块的中间位置
                            tryAttack(world, enemy, attackTarget);
                        } else {
                            // 攻击单个砖块
                            tryAttack(world, enemy, obstacleCenter);
                        }
                        state.avoidanceAttempts = 0; // 重置尝试次数
                        return true; // 已处理（开火），不移动
                    } else {
                        // 无法直接命中，需要调整位置
                        // 直接计算调整方向并移动，避免递归调用moveTowards
                        int adjustDirX = 0;
                        int adjustDirY = 0;
                        if (dx < dy) {
                            // 水平方向更近，调整到同一水平线
                            adjustDirX = toObstacle.x() > 0 ? 1 : -1;
                        } else {
                            // 垂直方向更近，调整到同一垂直线
                            adjustDirY = toObstacle.y() > 0 ? 1 : -1;
                        }
                        
                        // 检查调整方向是否有障碍物，如果没有则移动
                        // 注意：这里直接移动而不调用moveTowards，避免递归
                        if (adjustDirX != 0 || adjustDirY != 0) {
                            Vector2D adjustNextPos = new Vector2D(
                                currentPos.x() + adjustDirX * attrs.speed() * deltaSeconds,
                                currentPos.y() + adjustDirY * attrs.speed() * deltaSeconds
                            );
                            EnemyTank adjustTestTank = EnemyTankFactory.create(adjustNextPos, enemy.tier(), attrs);
                            boolean canAdjust = true;
                            for (com.battlecity.model.world.Obstacle obs : world.obstacles()) {
                                if (world.collisionDetector().collide(adjustTestTank, obs)) {
                                    canAdjust = false;
                                    break;
                                }
                            }
                            if (canAdjust && !world.collisionDetector().collide(adjustTestTank, world.base())) {
                                // 可以调整，直接移动（避免递归调用moveTowards）
                                if (adjustDirX > 0) {
                                    enemy.moveRight(deltaSeconds);
                                } else if (adjustDirX < 0) {
                                    enemy.moveLeft(deltaSeconds);
                                } else if (adjustDirY > 0) {
                                    enemy.moveDown(deltaSeconds);
                                } else if (adjustDirY < 0) {
                                    enemy.moveUp(deltaSeconds);
                                }
                                state.avoidanceAttempts = 0; // 重置尝试次数
                                return true; // 已处理（调整位置），不继续原方向移动
                            }
                        }
                        // 如果无法调整位置，尝试开火破坏障碍物
                        tryAttack(world, enemy, obstacleCenter);
                        state.avoidanceAttempts = 0; // 重置尝试次数
                        return true; // 已处理（开火），不继续原方向移动
                    }
                } else if (obstacle instanceof com.battlecity.model.world.SteelWall) {
                    // 不可破坏的障碍物（钢墙），需要绕过
                    // 尝试向左右或后方移动，然后重新计算路径
                    boolean avoiding = tryAvoidObstacle(world, enemy, obstacle, dirX, dirY, deltaSeconds);
                    if (avoiding) {
                        // 正在绕过，返回true表示已处理
                        return true;
                    } else {
                        // 无法绕过或绕过完成，返回false让上层逻辑重新计算路径
                        return false;
                    }
                } else if (obstacle instanceof com.battlecity.model.world.River) {
                    // 河流，可以穿过，继续移动
                    continue;
                }
            }
        }
        
        // 检查与基地的碰撞
        if (world.collisionDetector().collide(testTank, world.base())) {
            return false; // 不能移动到基地位置
        }
        
        // 没有障碍物，可以移动
        state.avoidanceAttempts = 0; // 重置尝试次数
        if (dirX > 0) {
            enemy.moveRight(deltaSeconds);
        } else if (dirX < 0) {
            enemy.moveLeft(deltaSeconds);
        } else if (dirY > 0) {
            enemy.moveDown(deltaSeconds);
        } else if (dirY < 0) {
            enemy.moveUp(deltaSeconds);
        }
        return true;
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
     * 检测前方是否有连续的砖块行，如果有则返回中间位置用于同时破坏两个砖块
     * @param world 游戏世界
     * @param enemy 敌方坦克
     * @param firstBrick 第一个砖块
     * @param dirX 移动方向x
     * @param dirY 移动方向y
     * @return 如果有连续砖块，返回中间位置；否则返回null
     */
    private Vector2D detectConsecutiveBricks(GameWorld world, EnemyTank enemy, 
                                              com.battlecity.model.world.BrickWall firstBrick, 
                                              int dirX, int dirY) {
        Vector2D firstBrickPos = firstBrick.position();
        double brickSize = firstBrick.size().width(); // 砖块大小（16x16）
        Vector2D enemyCenter = enemy.center();
        
        // 确定检查方向（与移动方向垂直的方向）
        // 如果水平移动，检查垂直方向的连续砖块；如果垂直移动，检查水平方向的连续砖块
        boolean checkHorizontal = (dirY != 0); // 垂直移动时检查水平方向
        boolean checkVertical = (dirX != 0);   // 水平移动时检查垂直方向
        
        // 检查连续砖块（最多检查3个砖块，即2个连续砖块）
        int consecutiveCount = 1;
        Vector2D lastBrickPos = firstBrickPos;
        
        for (int i = 1; i <= 2; i++) {
            Vector2D checkPos;
            if (checkHorizontal) {
                // 检查左右方向
                // 先检查右侧
                checkPos = new Vector2D(firstBrickPos.x() + i * brickSize, firstBrickPos.y());
            } else {
                // 检查上下方向
                // 先检查下方
                checkPos = new Vector2D(firstBrickPos.x(), firstBrickPos.y() + i * brickSize);
            }
            
            // 检查该位置是否有砖块
            boolean foundBrick = false;
            for (com.battlecity.model.world.Obstacle obs : world.obstacles()) {
                if (obs instanceof com.battlecity.model.world.BrickWall) {
                    Vector2D obsPos = obs.position();
                    // 检查位置是否匹配（允许1像素误差）
                    if (Math.abs(obsPos.x() - checkPos.x()) < 1 && 
                        Math.abs(obsPos.y() - checkPos.y()) < 1) {
                        consecutiveCount++;
                        lastBrickPos = obsPos;
                        foundBrick = true;
                        break;
                    }
                }
            }
            
            if (!foundBrick) {
                break;
            }
        }
        
        // 如果有2个或更多连续砖块，返回中间位置
        if (consecutiveCount >= 2) {
            Vector2D middlePos = new Vector2D(
                (firstBrickPos.x() + lastBrickPos.x() + brickSize) / 2.0,
                (firstBrickPos.y() + lastBrickPos.y() + brickSize) / 2.0
            );
            return middlePos;
        }
        
        return null; // 没有连续砖块
    }
    
    /**
     * 尝试绕过不可破坏的障碍物（钢墙）
     * 策略：尝试向左右或后方移动，然后重新计算路径
     * @param world 游戏世界
     * @param enemy 敌方坦克
     * @param obstacle 障碍物
     * @param dirX 原移动方向x
     * @param dirY 原移动方向y
     * @param deltaSeconds 时间增量
     * @return 如果成功绕过返回true，否则返回false
     */
    private boolean tryAvoidObstacle(GameWorld world, EnemyTank enemy, 
                                    com.battlecity.model.world.Obstacle obstacle,
                                    int dirX, int dirY, double deltaSeconds) {
        EnemyAIState state = tankStates.get(enemy);
        if (state == null) {
            state = new EnemyAIState();
            tankStates.put(enemy, state);
        }
        
        // 防止无限递归
        state.avoidanceAttempts++;
        if (state.avoidanceAttempts > 5) {
            state.avoidanceAttempts = 0;
            return false;
        }
        
        // 更新绕过障碍物计时器
        state.obstacleAvoidanceTimer += deltaSeconds;
        if (state.obstacleAvoidanceTimer > 3.0) {
            // 3秒后重置，避免一直卡住
            state.obstacleAvoidanceTimer = 0.0;
            state.avoidanceAttempts = 0;
            state.obstacleAvoidanceTarget = null;
            return false;
        }
        
        Vector2D currentCenter = enemy.center();
        Vector2D obstacleCenter = new Vector2D(
            obstacle.position().x() + obstacle.size().width() / 2.0,
            obstacle.position().y() + obstacle.size().height() / 2.0
        );
        
        // 如果还没有设置绕过目标，计算绕过方向
        if (state.obstacleAvoidanceTarget == null) {
            // 计算绕过方向：优先左右，然后后方
            Vector2D toObstacle = obstacleCenter.subtract(currentCenter);
            double dx = toObstacle.x();
            double dy = toObstacle.y();
            
            // 确定原移动方向
            boolean movingHorizontally = (dirX != 0);
            boolean movingVertically = (dirY != 0);
            
            // 尝试绕过方向：左右优先，然后后方
            Vector2D[] avoidDirections = new Vector2D[3];
            if (movingHorizontally) {
                // 水平移动时，尝试上下（垂直方向）绕过
                avoidDirections[0] = new Vector2D(0, -1); // 上
                avoidDirections[1] = new Vector2D(0, 1);  // 下
                avoidDirections[2] = new Vector2D(-dirX, 0); // 后方（反向）
            } else if (movingVertically) {
                // 垂直移动时，尝试左右（水平方向）绕过
                avoidDirections[0] = new Vector2D(1, 0);   // 右
                avoidDirections[1] = new Vector2D(-1, 0);   // 左
                avoidDirections[2] = new Vector2D(0, -dirY); // 后方（反向）
            } else {
                // 默认情况
                avoidDirections[0] = new Vector2D(1, 0);   // 右
                avoidDirections[1] = new Vector2D(-1, 0);   // 左
                avoidDirections[2] = new Vector2D(0, 1);    // 下
            }
            
            // 尝试每个绕过方向
            com.battlecity.model.tank.TankAttributes attrs = enemy.attributes();
            double avoidDistance = 50.0; // 绕过距离：50像素
            
            for (Vector2D avoidDir : avoidDirections) {
                Vector2D avoidTarget = new Vector2D(
                    currentCenter.x() + avoidDir.x() * avoidDistance,
                    currentCenter.y() + avoidDir.y() * avoidDistance
                );
                
                // 检查绕过目标位置是否可行（没有障碍物）
                Vector2D avoidPos = new Vector2D(
                    avoidTarget.x() - enemy.size().width() / 2.0,
                    avoidTarget.y() - enemy.size().height() / 2.0
                );
                EnemyTank testTank = EnemyTankFactory.create(avoidPos, enemy.tier(), attrs);
                
                boolean canAvoid = true;
                for (com.battlecity.model.world.Obstacle obs : world.obstacles()) {
                    if (world.collisionDetector().collide(testTank, obs)) {
                        canAvoid = false;
                        break;
                    }
                }
                
                if (canAvoid && !world.collisionDetector().collide(testTank, world.base())) {
                    // 可以绕过，设置绕过目标
                    state.obstacleAvoidanceTarget = avoidTarget;
                    break;
                }
            }
            
            // 如果所有方向都不可行，返回false
            if (state.obstacleAvoidanceTarget == null) {
                state.avoidanceAttempts = 0;
                return false;
            }
        }
        
        // 向绕过目标移动
        Vector2D toAvoidTarget = state.obstacleAvoidanceTarget.subtract(currentCenter);
        double distanceToAvoid = toAvoidTarget.length();
        
        if (distanceToAvoid > 10) {
            // 还没有到达绕过目标，继续移动
            int avoidDirX = 0;
            int avoidDirY = 0;
            if (Math.abs(toAvoidTarget.x()) > Math.abs(toAvoidTarget.y())) {
                avoidDirX = toAvoidTarget.x() > 0 ? 1 : -1;
            } else {
                avoidDirY = toAvoidTarget.y() > 0 ? 1 : -1;
            }
            
            // 直接移动，避免递归调用checkAndMove
            boolean moved = false;
            Vector2D currentPos = enemy.position();
            com.battlecity.model.tank.TankAttributes attrs = enemy.attributes();
            Vector2D avoidNextPos = new Vector2D(
                currentPos.x() + avoidDirX * attrs.speed() * deltaSeconds,
                currentPos.y() + avoidDirY * attrs.speed() * deltaSeconds
            );
            EnemyTank avoidTestTank = EnemyTankFactory.create(avoidNextPos, enemy.tier(), attrs);
            
            boolean canMove = true;
            for (com.battlecity.model.world.Obstacle obs : world.obstacles()) {
                if (world.collisionDetector().collide(avoidTestTank, obs)) {
                    // 如果遇到的是同一个障碍物，继续尝试绕过
                    if (obs == obstacle) {
                        continue;
                    }
                    canMove = false;
                    break;
                }
            }
            
            if (canMove && !world.collisionDetector().collide(avoidTestTank, world.base())) {
                // 可以移动
                if (avoidDirX > 0) {
                    enemy.moveRight(deltaSeconds);
                } else if (avoidDirX < 0) {
                    enemy.moveLeft(deltaSeconds);
                } else if (avoidDirY > 0) {
                    enemy.moveDown(deltaSeconds);
                } else if (avoidDirY < 0) {
                    enemy.moveUp(deltaSeconds);
                }
                moved = true;
            }
            
            if (moved) {
                return true; // 正在绕过
            } else {
                // 无法移动，清除绕过目标，重新尝试
                state.obstacleAvoidanceTarget = null;
                state.avoidanceAttempts++;
                return false;
            }
        } else {
            // 已到达绕过目标，清除绕过状态，重新计算路径
            state.obstacleAvoidanceTarget = null;
            state.obstacleAvoidanceTimer = 0.0;
            state.avoidanceAttempts = 0;
            // 清除当前路径，让上层逻辑重新计算
            state.currentPath = null;
            state.pathIndex = 0;
            return false; // 返回false，让上层逻辑重新计算路径
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
