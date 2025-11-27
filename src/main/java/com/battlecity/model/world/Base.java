package com.battlecity.model.world;

import com.battlecity.model.Entity;
import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;

public class Base extends Entity {
    private int health = 3; // 基地初始血量为3

    public Base(Vector2D pos, Size size) {
        super(pos, size);
    }

    public boolean alive() {
        return health > 0;
    }

    public int health() {
        return health;
    }

    public void damage() {
        health = Math.max(0, health - 1);
    }
}

