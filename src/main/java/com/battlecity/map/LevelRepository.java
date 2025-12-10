package com.battlecity.map;

/**
 * 关卡仓库，后续可扩展到自定义编辑器。
 */
public interface LevelRepository {
    LevelDefinition defaultClassic();

    LevelDefinition endlessPrototype();

    LevelDefinition timedPrototype();

    /**
     * 返回所有可用关卡，便于自由选关。
     */
    java.util.List<LevelDefinition> allLevels();
}

