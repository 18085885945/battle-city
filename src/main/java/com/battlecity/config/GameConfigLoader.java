package com.battlecity.config;

import com.battlecity.engine.state.GameModeType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 通过 Jackson 解析 JSON 配置文件。
 */
public class GameConfigLoader {

    private final ObjectMapper mapper = new ObjectMapper();

    public GameConfig load(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
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
            throw new IllegalStateException("加载配置失败: " + path, e);
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

