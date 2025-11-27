package com.battlecity.physics;

/**
 * AABB (Axis-Aligned Bounding Box) 碰撞检测算法
 * 用于快速检测两个矩形是否相交
 */
public record AABB(double left, double top, double right, double bottom) {

    /**
     * 检查两个AABB是否相交
     * @param other 另一个AABB
     * @return 如果相交返回true
     */
    public boolean intersects(AABB other) {
        return right > other.left
                && left < other.right
                && bottom > other.top
                && top < other.bottom;
    }
    
    /**
     * 获取AABB的宽度
     */
    public double width() {
        return right - left;
    }
    
    /**
     * 获取AABB的高度
     */
    public double height() {
        return bottom - top;
    }
}

