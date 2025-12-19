package com.battlecity.map;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class LevelRepositoryFactory {

    private LevelRepositoryFactory() {
    }

    public static LevelRepository fromClasspath(Path levelsDir) {
        LevelLoader loader = new LevelLoader();
        try {
            List<LevelDefinition> levels = Files.list(levelsDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .map(loader::load)
                    .toList();
            return buildRepository(levels);
        } catch (IOException e) {
            throw new IllegalStateException("无法读取关卡目录: " + levelsDir, e);
        }
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

