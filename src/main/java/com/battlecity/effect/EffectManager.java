package com.battlecity.effect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 特效管理器，负责管理和更新所有特效
 */
public class EffectManager {
    private final List<Effect> effects;

    public EffectManager() {
        this.effects = new ArrayList<>();
    }

    /**
     * 添加特效
     * @param effect 要添加的特效
     */
    public void addEffect(Effect effect) {
        effects.add(effect);
    }

    /**
     * 更新所有特效
     * @param deltaSeconds 时间增量（秒）
     */
    public void update(double deltaSeconds) {
        Iterator<Effect> iterator = effects.iterator();
        while (iterator.hasNext()) {
            Effect effect = iterator.next();
            effect.update(deltaSeconds);
            if (effect.isFinished()) {
                iterator.remove();
            }
        }
    }

    /**
     * 获取所有特效
     * @return 特效列表
     */
    public List<Effect> getEffects() {
        return new ArrayList<>(effects);
    }

    /**
     * 清空所有特效
     */
    public void clear() {
        effects.clear();
    }
}