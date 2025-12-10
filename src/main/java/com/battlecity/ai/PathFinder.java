package com.battlecity.ai;

import com.battlecity.model.GameWorld;
import com.battlecity.model.Vector2D;
import com.battlecity.model.tank.Tank;
import com.battlecity.model.world.Obstacle;

import java.util.*;

/**
 * 路径查找系统：使用BFS算法寻找最短路径
 */
public class PathFinder {
    
    private static final double BRICK_SIZE = 15.0; // 砖块大小
    private static final double GRID_SIZE = BRICK_SIZE; // 网格大小
    
    /**
     * 使用BFS算法寻找从起点到终点的最短路径
     * 
     * @param world 游戏世界
     * @param start 起点
     * @param target 目标点
     * @param tank 坦克（用于碰撞检测）
     * @return 路径点列表，如果找不到路径返回空列表
     */
    public static List<Vector2D> findPath(GameWorld world, Vector2D start, Vector2D target, Tank tank) {
        // 将坐标转换为网格坐标
        int startGridX = (int) (start.x() / GRID_SIZE);
        int startGridY = (int) (start.y() / GRID_SIZE);
        int targetGridX = (int) (target.x() / GRID_SIZE);
        int targetGridY = (int) (target.y() / GRID_SIZE);
        
        // BFS搜索
        Queue<Node> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Map<String, Node> nodeMap = new HashMap<>();
        
        Node startNode = new Node(startGridX, startGridY, null);
        queue.offer(startNode);
        visited.add(startGridX + "," + startGridY);
        nodeMap.put(startGridX + "," + startGridY, startNode);
        
        int[] dx = {0, 1, 0, -1}; // 上下左右
        int[] dy = {-1, 0, 1, 0};
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            
            // 到达目标
            if (current.x == targetGridX && current.y == targetGridY) {
                // 重建路径
                List<Vector2D> path = new ArrayList<>();
                Node node = current;
                while (node != null) {
                    path.add(0, new Vector2D(node.x * GRID_SIZE + GRID_SIZE / 2, 
                                             node.y * GRID_SIZE + GRID_SIZE / 2));
                    node = node.parent;
                }
                return path;
            }
            
            // 探索四个方向
            for (int i = 0; i < 4; i++) {
                int newX = current.x + dx[i];
                int newY = current.y + dy[i];
                String key = newX + "," + newY;
                
                if (!visited.contains(key)) {
                    // 检查是否在地图范围内
                    if (isValidPosition(world, newX, newY, tank)) {
                        Node newNode = new Node(newX, newY, current);
                        queue.offer(newNode);
                        visited.add(key);
                        nodeMap.put(key, newNode);
                    }
                }
            }
        }
        
        // 找不到路径，返回空列表
        return new ArrayList<>();
    }
    
    /**
     * 检查位置是否有效（不碰撞障碍物）
     */
    private static boolean isValidPosition(GameWorld world, int gridX, int gridY, Tank tank) {
        double x = gridX * GRID_SIZE;
        double y = gridY * GRID_SIZE;
        
        // 检查是否在地图范围内
        if (x < 0 || y < 0 || 
            x + tank.size().width() > world.levelDefinition().width() ||
            y + tank.size().height() > world.levelDefinition().height()) {
            return false;
        }
        
        // 创建临时坦克位置进行碰撞检测
        Vector2D testPos = new Vector2D(x, y);
        Tank testTank = new com.battlecity.model.tank.EnemyTank(
            testPos, 
            new com.battlecity.model.tank.TankAttributes(100, 1.0, 250), 
            com.battlecity.model.tank.EnemyTier.NORMAL
        );
        
        // 检查与障碍物碰撞
        for (Obstacle obstacle : world.obstacles()) {
            if (world.collisionDetector().collide(testTank, obstacle)) {
                return false;
            }
        }
        
        // 检查与基地碰撞
        if (world.collisionDetector().collide(testTank, world.base())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * BFS节点
     */
    private static class Node {
        int x, y;
        Node parent;
        
        Node(int x, int y, Node parent) {
            this.x = x;
            this.y = y;
            this.parent = parent;
        }
    }
}

