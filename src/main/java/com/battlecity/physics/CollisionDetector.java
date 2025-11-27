package com.battlecity.physics;

import com.battlecity.model.Entity;

/**
 * 基于 AABB 的碰撞检测器
 * 支持实体间的碰撞检测，后续可扩展空间分割优化
 */
public class CollisionDetector {

    /**
     * 检测两个实体是否碰撞
     * @param a 实体A
     * @param b 实体B
     * @return 如果碰撞返回true
     */
    public boolean collide(Entity a, Entity b) {
        return toAabb(a).intersects(toAabb(b));
    }

    /**
     * 将实体转换为AABB
     */
    private AABB toAabb(Entity entity) {
        return new AABB(entity.left(), entity.top(), entity.right(), entity.bottom());
    }
}

