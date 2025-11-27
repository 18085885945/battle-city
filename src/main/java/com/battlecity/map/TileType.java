package com.battlecity.map;

/**
 * 地图瓦片类型
 */
public enum TileType {
    /**
     * 砖墙：可破坏
     */
    BRICK,
    
    /**
     * 钢墙：不可破坏（需要特殊道具）
     */
    STEEL,
    
    /**
     * 河流：需要特殊道具才能通过
     */
    RIVER,
    
    /**
     * 草丛：隐藏坦克
     */
    GRASS
}

