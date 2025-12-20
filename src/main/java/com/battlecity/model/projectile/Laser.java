package com.battlecity.model.projectile;

import com.battlecity.model.Entity;
import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;

/**
 * 激光实体，用于激光武器攻击
 */
public class Laser extends Entity {
    
    private final Vector2D startPoint;
    private final Vector2D direction;
    private final double mapWidth;
    private final double mapHeight;
    private boolean alive = true;
    private double duration = 0.1; // 激光持续时间（秒）
    private double elapsedTime = 0.0;
    private final boolean isMegaLaser; // 是否为大激光
    private final double laserWidth; // 激光宽度
    
    /**
     * 创建激光
     * @param startPoint 起始点（坦克中心）
     * @param direction 方向（归一化）
     * @param mapWidth 地图宽度
     * @param mapHeight 地图高度
     */
    public Laser(Vector2D startPoint, Vector2D direction, double mapWidth, double mapHeight) {
        this(startPoint, direction, mapWidth, mapHeight, false);
    }
    
    /**
     * 创建激光（支持大激光）
     * @param startPoint 起始点（坦克中心）
     * @param direction 方向（归一化）
     * @param mapWidth 地图宽度
     * @param mapHeight 地图高度
     * @param isMegaLaser 是否为大激光
     */
    public Laser(Vector2D startPoint, Vector2D direction, double mapWidth, double mapHeight, boolean isMegaLaser) {
        // 激光的position和size用于碰撞检测，实际渲染时使用startPoint和endPoint
        // 先计算激光宽度，然后调用super（super必须是第一行）
        super(startPoint, new Size(isMegaLaser ? 32.0 : 16.0, isMegaLaser ? 32.0 : 16.0));
        this.isMegaLaser = isMegaLaser;
        this.laserWidth = isMegaLaser ? 32.0 : 16.0; // 大激光两个砖块宽，普通激光一个砖块宽
        this.startPoint = startPoint;
        this.direction = direction.normalize();
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }
    
    /**
     * 是否为大激光
     */
    public boolean isMegaLaser() {
        return isMegaLaser;
    }
    
    /**
     * 获取激光宽度
     */
    public double getLaserWidth() {
        return laserWidth;
    }
    
    /**
     * 获取激光起始点
     */
    public Vector2D startPoint() {
        return startPoint;
    }
    
    /**
     * 获取激光结束点（贯穿整个地图）
     */
    public Vector2D endPoint() {
        // 计算激光延伸到地图边界的点
        double maxDistance = Math.max(mapWidth, mapHeight) * 2; // 确保贯穿整个地图
        return startPoint.add(direction.scale(maxDistance));
    }
    
    /**
     * 获取激光方向
     */
    public Vector2D direction() {
        return direction;
    }
    
    /**
     * 更新激光
     */
    public void update(double deltaSeconds) {
        elapsedTime += deltaSeconds;
        if (elapsedTime >= duration) {
            alive = false;
        }
    }
    
    /**
     * 检查激光是否存活
     */
    public boolean alive() {
        return alive;
    }
    
    /**
     * 销毁激光
     */
    public void destroy() {
        alive = false;
    }
    
    /**
     * 检查点是否在激光路径上
     */
    public boolean pointOnLaser(Vector2D point, double tolerance) {
        Vector2D endPoint = endPoint();
        // 计算点到直线的距离
        Vector2D lineVec = endPoint.subtract(startPoint);
        Vector2D pointVec = point.subtract(startPoint);
        
        double lineLength = lineVec.length();
        if (lineLength < 0.001) {
            return false;
        }
        
        // 计算投影
        double projection = pointVec.dot(lineVec) / (lineLength * lineLength);
        
        // 检查投影是否在线段范围内
        if (projection < 0 || projection > 1) {
            return false;
        }
        
        // 计算点到直线的距离
        Vector2D closestPoint = startPoint.add(lineVec.scale(projection));
        double distance = point.subtract(closestPoint).length();
        
        return distance <= tolerance;
    }
}

