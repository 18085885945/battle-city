package com.battlecity.map;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 负责解析 JSON 关卡文件。
 */
public class LevelLoader {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LevelDefinition load(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
            return objectMapper.readValue(in, LevelDefinition.class);
        } catch (IOException e) {
            throw new IllegalStateException("无法加载关卡: " + path, e);
        }
    }
}

