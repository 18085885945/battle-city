package com.battlecity.bootstrap;

import com.battlecity.config.GameConfig;
import com.battlecity.config.GameConfigLoader;
import com.battlecity.engine.GameContext;
import com.battlecity.map.LevelRepository;
import com.battlecity.map.LevelRepositoryFactory;
import com.battlecity.util.ResourceLocator;

/**
 * 负责完成配置加载、关卡仓库初始化等启动流程。
 */
public class GameBootstrapper {

    public GameContext bootstrap() {
        GameConfig config = new GameConfigLoader().load(ResourceLocator.defaultConfigPath());
        LevelRepository levelRepository = LevelRepositoryFactory.fromClasspath(ResourceLocator.levelsDirectory());
        return new GameContext(config, levelRepository);
    }
}

