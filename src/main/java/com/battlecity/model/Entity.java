package com.battlecity.model;

/**
 * 世界中的实体基类。
 */
public abstract class Entity {
    protected Vector2D position;
    protected final Size size;

    protected Entity(Vector2D position, Size size) {
        this.position = position;
        this.size = size;
    }

    public Vector2D position() {
        return position;
    }

    public Size size() {
        return size;
    }

    public double left() {
        return position.x();
    }

    public double right() {
        return position.x() + size.width();
    }

    public double top() {
        return position.y();
    }

    public double bottom() {
        return position.y() + size.height();
    }
}

