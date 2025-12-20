package com.battlecity.map;

import java.util.ArrayList;
import java.util.List;

public final class LevelRepositoryFactory {

    private LevelRepositoryFactory() {
    }

    /**
     * 从类路径加载所有关卡文件
     * @param levelsDir 类路径资源目录，如 "/levels"
     * @return LevelRepository 实例
     */
    public static LevelRepository fromClasspath(String levelsDir) {
        LevelLoader loader = new LevelLoader();
        List<LevelDefinition> levels = new ArrayList<>();
        
        // 列出所有关卡文件（由于JAR中无法直接列出目录，使用硬编码列表）
        String[] levelFiles = {
            "classic-level-1.json",
            "classic-level-2.json",
            "classic-level-3.json",
            "classic-level-4.json",
            "classic-level-5.json",
            "endless-1.json",
            "endless-prototype.json",
            "timed-1.json",
            "timed-2.json",
            "timed-3.json",
            "timed-4.json",
            "timed-5.json",
            "timed-challenge.json"
        };
        
        // 加载所有存在的关卡文件（某些文件可能不存在，忽略即可）
        for (String fileName : levelFiles) {
            String resourcePath = levelsDir + "/" + fileName;
            try {
                LevelDefinition level = loader.loadFromClasspath(resourcePath);
                levels.add(level);
            } catch (IllegalStateException e) {
                // 如果文件不存在，跳过
                System.err.println("警告: 跳过不存在的关卡文件: " + resourcePath);
            }
        }
        
        if (levels.isEmpty()) {
            throw new IllegalStateException("未找到任何关卡文件，请检查资源目录: " + levelsDir);
        }
        
        return buildRepository(levels);
    }

    private record SimpleLevelRepository(
            LevelDefinition classic,
            LevelDefinition endless,
            LevelDefinition timed,
            List<LevelDefinition> all,
            List<LevelDefinition> classicLevels
    ) implements LevelRepository {

        @Override
        public LevelDefinition defaultClassic() {
            return classic;
        }

        @Override
        public LevelDefinition endlessPrototype() {
            return endless;
        }

        @Override
        public LevelDefinition timedPrototype() {
            return timed;
        }

        @Override
        public List<LevelDefinition> allLevels() {
            return all;
        }
        
        @Override
        public List<LevelDefinition> classicLevels() {
            return classicLevels;
        }
        
        @Override
        public LevelDefinition getNextClassicLevel(String currentLevelId) {
            for (int i = 0; i < classicLevels.size(); i++) {
                LevelDefinition level = classicLevels.get(i);
                if (level.id().equals(currentLevelId) && i < classicLevels.size() - 1) {
                    return classicLevels.get(i + 1);
                }
            }
            return null; // 当前是最后一关或找不到当前关卡
        }
    }

    private static SimpleLevelRepository buildRepository(List<LevelDefinition> levels) {
        // 支持多种可能的ID格式
        LevelDefinition classic = pickLevel(levels, "classic-level-1");
        if (classic == null) {
            classic = pickLevel(levels, "classic-1");
        }
        LevelDefinition endless = pickLevel(levels, "endless-prototype");
        LevelDefinition timed = pickLevel(levels, "timed-challenge");
        
        // 收集所有经典模式关卡并按顺序排列
        List<LevelDefinition> classicLevels = new java.util.ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            LevelDefinition level = pickLevel(levels, "classic-level-" + i);
            if (level == null) {
                level = pickLevel(levels, "classic-" + i);
            }
            if (level != null) {
                classicLevels.add(level);
            }
        }
        
        return new SimpleLevelRepository(
                classic != null ? classic : fallback(levels),
                endless != null ? endless : fallback(levels),
                timed != null ? timed : fallback(levels),
                List.copyOf(levels),
                List.copyOf(classicLevels)
        );
    }

    private static LevelDefinition pickLevel(List<LevelDefinition> levels, String id) {
        if (levels == null || levels.isEmpty()) {
            return null;
        }
        return levels.stream()
                .filter(l -> l != null && id.equalsIgnoreCase(l.id()))
                .findFirst()
                .orElse(null);
    }

    private static LevelDefinition fallback(List<LevelDefinition> levels) {
        if (levels == null || levels.isEmpty()) {
            throw new IllegalStateException("未找到任何关卡文件");
        }
        return levels.getFirst();
    }
}

