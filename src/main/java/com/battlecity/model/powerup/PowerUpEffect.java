package com.battlecity.model.powerup;

/**
 * 道具效果，用于管理道具效果的持续时间和状态
 */
public abstract class PowerUpEffect {
    
    private final PowerUpType type;
    private final double duration;
    private double remainingTime;
    private boolean isActive = false;
    
    public PowerUpEffect(PowerUpType type, double duration) {
        this.type = type;
        this.duration = duration;
        this.remainingTime = duration;
    }
    
    public PowerUpType getType() {
        return type;
    }
    
    public double getDuration() {
        return duration;
    }
    
    public double getRemainingTime() {
        return remainingTime;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void activate() {
        if (!isActive) {
            isActive = true;
            apply();
        }
    }
    
    public void deactivate() {
        if (isActive) {
            isActive = false;
            remove();
        }
    }
    
    public void update(double deltaSeconds) {
        if (isActive) {
            remainingTime -= deltaSeconds;
            if (remainingTime <= 0) {
                deactivate();
            }
        }
    }
    
    /**
     * 应用效果
     */
    public abstract void apply();
    
    /**
     * 移除效果
     */
    public abstract void remove();
}