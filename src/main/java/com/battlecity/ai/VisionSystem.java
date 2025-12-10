package com.battlecity.ai;

import com.battlecity.model.Vector2D;
import com.battlecity.model.tank.Tank;

/**
 * 视野系统：检测敌方坦克是否能看到目标
 */
public class VisionSystem {
    
    private static final double BRICK_SIZE = 15.0; // 砖块大小
    private static final double VISION_RADIUS = 10 * BRICK_SIZE; // 视野半径：10个砖块
    private static final double VISION_ANGLE = Math.toRadians(35); // 视野角度：35度
    
    /**
     * 检查敌方坦克是否能看到目标
     * 视野为以敌方正面为圆心，10个砖块为半径，角度为35°的扇形
     * 
     * @param enemy 敌方坦克
     * @param target 目标位置
     * @return 是否在视野内
     */
    public static boolean canSee(Tank enemy, Vector2D target) {
        Vector2D enemyCenter = enemy.center();
        Vector2D toTarget = target.subtract(enemyCenter);
        double distance = toTarget.length();
        
        // 检查距离
        if (distance > VISION_RADIUS) {
            return false;
        }
        
        // 检查角度：目标是否在35度扇形内
        Vector2D facingDir = enemy.facingDirection();
        if (facingDir.length() == 0) {
            return false;
        }
        
        // 计算目标方向与面向方向的夹角
        double dotProduct = facingDir.x() * toTarget.x() + facingDir.y() * toTarget.y();
        double cosAngle = dotProduct / (facingDir.length() * distance);
        
        // 检查是否在视野角度内（35度 = cos(17.5度) ≈ 0.9537）
        double cosVisionAngle = Math.cos(VISION_ANGLE / 2.0);
        return cosAngle >= cosVisionAngle;
    }
    
    /**
     * 检查两点之间是否有障碍物阻挡视线（简单的直线检测）
     * 这里简化处理，实际可以更复杂
     */
    public static boolean hasLineOfSight(Vector2D from, Vector2D to, 
                                         java.util.List<com.battlecity.model.world.Obstacle> obstacles) {
        // 简化实现：检查路径上是否有障碍物
        // 可以采样路径上的点，检查是否与障碍物碰撞
        int samples = 10;
        for (int i = 1; i < samples; i++) {
            double t = (double) i / samples;
            Vector2D samplePoint = new Vector2D(
                from.x() + (to.x() - from.x()) * t,
                from.y() + (to.y() - from.y()) * t
            );
            
            // 检查采样点是否在障碍物内（简化：检查是否与障碍物边界相交）
            for (com.battlecity.model.world.Obstacle obstacle : obstacles) {
                if (isPointInObstacle(samplePoint, obstacle)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static boolean isPointInObstacle(Vector2D point, com.battlecity.model.world.Obstacle obstacle) {
        return point.x() >= obstacle.position().x() 
            && point.x() <= obstacle.position().x() + obstacle.size().width()
            && point.y() >= obstacle.position().y()
            && point.y() <= obstacle.position().y() + obstacle.size().height();
    }
}

