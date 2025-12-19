package com.battlecity.model.world;

import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;

public class SteelWall extends Obstacle {
    public SteelWall(Vector2D position, Size size) {
        super(position, size, false);
    }
}

