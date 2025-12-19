package com.battlecity.effect;

import com.battlecity.model.Vector2D;
import com.battlecity.model.Size;

/**
 * 爆炸特效
 */
public class ExplosionEffect extends Effect {
    private Size size;
    private double maxRadius;
    private double currentRadius;
    private double intensity;
    private double expansionRate; // 扩张速率

    public ExplosionEffect(Vector2D position, double duration, double maxRadius) {
        super(position, duration);
        this.size = new Size(maxRadius * 2, maxRadius * 2);
        this.maxRadius = maxRadius;
        this.currentRadius = 0;
        this.intensity = 1.0;
        this.expansionRate = maxRadius / duration; // 每秒扩张的最大半径
    }

    @Override
    protected void onUpdate(double deltaSeconds) {
        // 爆炸半径随时间增长（非线性增长，开始快，后来慢）
        double progress = elapsed / duration;
        currentRadius = maxRadius * (1 - Math.exp(-5 * progress)); // 指数衰减函数
        
        // 强度随时间衰减（非线性衰减）
        intensity = 1.0 - progress * progress; // 平方衰减，开始衰减慢，后来快
    }

    public double getCurrentRadius() {
        return currentRadius;
    }

    public double getIntensity() {
        return intensity;
    }

    public Size getSize() {
        return size;
    }
}