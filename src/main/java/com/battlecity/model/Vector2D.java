package com.battlecity.model;

/**
 * 简单二维向量，避免频繁创建 javafx.geometry.Point2D。
 */
public record Vector2D(double x, double y) {

    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }

    public Vector2D scale(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }
    
    public double length() {
        return Math.sqrt(x * x + y * y);
    }
    
    public Vector2D normalize() {
        double len = length();
        if (len == 0) {
            return new Vector2D(0, -1); // 默认向上
        }
        return new Vector2D(x / len, y / len);
    }
    
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }
}

