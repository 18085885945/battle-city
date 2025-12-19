package com.battlecity.model.world;

import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;

public class BrickWall extends Obstacle {
    public BrickWall(Vector2D position, Size size) {
        super(position, size, true);
    }
}

