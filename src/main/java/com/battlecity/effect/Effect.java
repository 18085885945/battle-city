package com.battlecity.effect;

import com.battlecity.model.Vector2D;

/**
 * 特效基类
 */
public abstract class Effect {
    protected Vector2D position;
    protected double duration; // 持续时间（秒）
    protected double elapsed;  // 已经过的时间（秒）
    protected boolean finished;

    public Effect(Vector2D position, double duration) {
        this.position = position;
        this.duration = duration;
        this.elapsed = 0;
        this.finished = false;
    }

    /**
     * 更新特效状态
     * @param deltaSeconds 时间增量（秒）
     */
    public void update(double deltaSeconds) {
        if (finished) {
            return;
        }

        elapsed += deltaSeconds;
        if (elapsed >= duration) {
            finished = true;
        } else {
            onUpdate(deltaSeconds);
        }
    }

    /**
     * 子类实现具体的更新逻辑
     * @param deltaSeconds 时间增量（秒）
     */
    protected abstract void onUpdate(double deltaSeconds);

    /**
     * 检查特效是否已完成
     * @return 如果特效已完成返回true
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * 获取特效位置
     * @return 特效位置
     */
    public Vector2D getPosition() {
        return position;
    }
}