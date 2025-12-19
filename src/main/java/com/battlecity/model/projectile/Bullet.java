package com.battlecity.model.projectile;

import com.battlecity.model.Entity;
import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;

public class Bullet extends Entity {

    private final Vector2D direction;
    private final double speed;
    private boolean alive = true;
    private boolean canPenetrate = false; // 子弹是否可以穿透铁块
    private boolean oneShotMode = false; // 秒杀模式，一击摧毁所有坦克

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
    
    /**
     * 设置子弹是否可以穿透铁块
     */
    public void setCanPenetrate(boolean canPenetrate) {
        this.canPenetrate = canPenetrate;
    }
    
    /**
     * 获取子弹是否可以穿透铁块
     */
    public boolean canPenetrate() {
        return canPenetrate;
    }
    
    /**
     * 设置秒杀模式
     */
    public void setOneShotMode(boolean oneShotMode) {
        this.oneShotMode = oneShotMode;
    }
    
    /**
     * 获取是否处于秒杀模式
     */
    public boolean isOneShotMode() {
        return oneShotMode;
    }
}

