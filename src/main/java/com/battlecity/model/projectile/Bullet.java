package com.battlecity.model.projectile;

import com.battlecity.model.Entity;
import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;

public class Bullet extends Entity {

    private final Vector2D direction;
    private final double speed;
    private boolean alive = true;

    public Bullet(Vector2D position, Vector2D direction, double speed) {
        super(position, new Size(4, 4));
        this.direction = direction;
        this.speed = speed;
    }

    public void update(double deltaSeconds) {
        position = position.add(direction.scale(speed * deltaSeconds));
    }

    public boolean alive() {
        return alive;
    }

    public void destroy() {
        alive = false;
    }
    
    public Vector2D direction() {
        return direction;
    }
}

