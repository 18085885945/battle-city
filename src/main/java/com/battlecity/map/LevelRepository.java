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
    
    /**
     * 返回经典模式的所有关卡，按顺序排列。
     */
    java.util.List<LevelDefinition> classicLevels();
    
    /**
     * 根据关卡ID获取下一关关卡定义。
     * @param currentLevelId 当前关卡ID
     * @return 下一关关卡定义，如果是最后一关则返回null
     */
    LevelDefinition getNextClassicLevel(String currentLevelId);
}

