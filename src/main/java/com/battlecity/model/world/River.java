package com.battlecity.model.world;

import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;

/**
 * 水路障碍物，无法通过（除非有特殊道具）
 */
public class River extends Obstacle {
    public River(Vector2D position, Size size) {
        super(position, size, false); // 水路不可破坏
    }
}

