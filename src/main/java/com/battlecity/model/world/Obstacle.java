package com.battlecity.model.world;

import com.battlecity.model.Entity;
import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;

/**
 * 世界中的阻挡物。
 */
public class Obstacle extends Entity {

    private final boolean destructible;

    protected Obstacle(Vector2D position, Size size, boolean destructible) {
        super(position, size);
        this.destructible = destructible;
    }

    public boolean destructible() {
        return destructible;
    }
}

