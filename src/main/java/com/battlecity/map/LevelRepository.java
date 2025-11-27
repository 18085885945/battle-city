package com.battlecity.map;

/**
 * 关卡仓库，后续可扩展到自定义编辑器。
 */
public interface LevelRepository {
    LevelDefinition defaultClassic();

    LevelDefinition endlessPrototype();

    LevelDefinition timedPrototype();
}

