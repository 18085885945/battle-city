package com.battlecity.physics;

import com.battlecity.model.Entity;

/**
 * 基于 AABB 的检测器，后续可扩展空间分割。
 */
public class CollisionDetector {

    public boolean collide(Entity a, Entity b) {
        return toAabb(a).intersects(toAabb(b));
    }

    private AABB toAabb(Entity entity) {
        return new AABB(entity.left(), entity.top(), entity.right(), entity.bottom());
    }
}

