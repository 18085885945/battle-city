package com.battlecity.physics;

public record AABB(double left, double top, double right, double bottom) {

    public boolean intersects(AABB other) {
        return right > other.left
                && left < other.right
                && bottom > other.top
                && top < other.bottom;
    }
}

