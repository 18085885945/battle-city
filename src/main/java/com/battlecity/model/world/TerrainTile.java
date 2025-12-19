package com.battlecity.model.world;

import com.battlecity.map.TileType;
import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;

public class TerrainTile {
    private final TileType type;
    private final Vector2D position;
    private final Size size;

    public TerrainTile(TileType type, Vector2D position, Size size) {
        this.type = type;
        this.position = position;
        this.size = size;
    }

    public TileType type() {
        return type;
    }

    public Vector2D position() {
        return position;
    }

    public Size size() {
        return size;
    }
}

