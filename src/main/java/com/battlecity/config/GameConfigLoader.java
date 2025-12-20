package com.battlecity.config;

import com.battlecity.engine.state.GameModeType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 通过 Jackson 解析 JSON 配置文件。
 */
public class GameConfigLoader {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 从类路径加载配置文件
     * @param resourcePath 类路径资源路径，如 "/config/game-config.json"
     * @return GameConfig 实例
     */
    public GameConfig load(String resourcePath) {
        try (InputStream in = GameConfigLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("找不到配置文件: " + resourcePath);
            }
            RawGameConfig raw = mapper.readValue(in, RawGameConfig.class);
            Map<GameModeType, ModeConfig> mappedModes = new EnumMap<>(GameModeType.class);
            raw.modeConfigs().forEach((key, value) -> mappedModes.put(GameModeType.valueOf(key), value));
            return new GameConfig(
                    raw.targetFps(),
                    raw.virtualWidth(),
                    raw.virtualHeight(),
                    mappedModes,
                    raw.player(),
                    raw.enemyWaves()
            );
        } catch (IOException e) {
            throw new IllegalStateException("加载配置失败: " + resourcePath, e);
        }
    }

    private record RawGameConfig(
            int targetFps,
            int virtualWidth,
            int virtualHeight,
            Map<String, ModeConfig> modeConfigs,
            PlayerConfig player,
            List<EnemyWaveConfig> enemyWaves
    ) {
    }
}

