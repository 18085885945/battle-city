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

    /**
     * 从类路径加载关卡文件（用于JAR包中）
     * @param resourcePath 类路径资源路径，如 "/levels/classic-level-1.json"
     * @return LevelDefinition 实例
     */
    public LevelDefinition loadFromClasspath(String resourcePath) {
        try (InputStream in = LevelLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("找不到关卡文件: " + resourcePath);
            }
            return objectMapper.readValue(in, LevelDefinition.class);
        } catch (IOException e) {
            throw new IllegalStateException("无法加载关卡: " + resourcePath, e);
        }
    }

    /**
     * 从文件系统路径加载关卡文件（用于关卡编辑器）
     * @param path 文件系统路径
     * @return LevelDefinition 实例
     */
    public LevelDefinition load(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
            return objectMapper.readValue(in, LevelDefinition.class);
        } catch (IOException e) {
            throw new IllegalStateException("无法加载关卡: " + path, e);
        }
    }
}

