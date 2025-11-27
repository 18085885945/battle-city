package com.battlecity.map;

import java.nio.file.Path;

public final class LevelRepositoryFactory {

    private LevelRepositoryFactory() {
    }

    public static LevelRepository fromClasspath(Path levelsDir) {
        LevelLoader loader = new LevelLoader();
        return new SimpleLevelRepository(
                loader.load(levelsDir.resolve("classic-level-1.json")),
                loader.load(levelsDir.resolve("endless-prototype.json")),
                loader.load(levelsDir.resolve("timed-challenge.json"))
        );
    }

    private record SimpleLevelRepository(
            LevelDefinition classic,
            LevelDefinition endless,
            LevelDefinition timed
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
    }
}

