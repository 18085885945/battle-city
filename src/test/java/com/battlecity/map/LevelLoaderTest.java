package com.battlecity.map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class LevelLoaderTest {

    private LevelLoader levelLoader;
    private String validJsonContent;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        levelLoader = new LevelLoader();
        // 创建一个有效的JSON内容用于测试
        validJsonContent = "{\n" +
                "  \"id\": \"test-level\",\n" +
                "  \"name\": \"Test Level\",\n" +
                "  \"width\": 416,\n" +
                "  \"height\": 416,\n" +
                "  \"base\": {\n" +
                "    \"x\": 193,\n" +
                "    \"y\": 386\n" +
                "  },\n" +
                "  \"obstacles\": []\n" +
                "}";
    }

    @Test
    void testLoad_ValidFileSystemPath_ShouldReturnLevelDefinition() throws IOException {
        // 创建临时文件
        Path tempFile = tempDir.resolve("test-level.json");
        Files.writeString(tempFile, validJsonContent);

        // 加载关卡
        LevelDefinition level = levelLoader.load(tempFile);

        // 验证结果
        assertNotNull(level);
        assertEquals("test-level", level.id());
        assertEquals("Test Level", level.name());
        assertEquals(416, level.width());
        assertEquals(416, level.height());
        assertNotNull(level.base());
        assertEquals(193, level.base().x());
        assertEquals(386, level.base().y());
        assertNotNull(level.obstacles());
    }

    @Test
    void testLoadFromClasspath_ValidResourcePath_ShouldReturnLevelDefinition() {
        // 使用项目中已存在的关卡文件进行测试
        LevelDefinition level = levelLoader.loadFromClasspath("/levels/classic-level-1.json");

        // 验证结果
        assertNotNull(level);
        assertEquals("classic-1", level.id());
        assertEquals("经典第1关", level.name());
        assertEquals(832, level.width());
        assertEquals(640, level.height());
        assertNotNull(level.base());
        assertNotNull(level.obstacles());
    }

    @Test
    void testLoad_InvalidFilePath_ShouldThrowException() {
        Path invalidPath = tempDir.resolve("non-existent.json");

        // 验证抛出异常
        assertThrows(IllegalStateException.class, () -> {
            levelLoader.load(invalidPath);
        });
    }

    @Test
    void testLoadFromClasspath_InvalidResourcePath_ShouldThrowException() {
        // 验证抛出异常
        assertThrows(IllegalStateException.class, () -> {
            levelLoader.loadFromClasspath("/levels/non-existent.json");
        });
    }

    @Test
    void testLoad_InvalidJsonContent_ShouldThrowException() throws IOException {
        // 创建包含无效JSON的临时文件
        Path tempFile = tempDir.resolve("invalid-level.json");
        String invalidJson = "{ invalid json content }";
        Files.writeString(tempFile, invalidJson);

        // 验证抛出异常
        assertThrows(IllegalStateException.class, () -> {
            levelLoader.load(tempFile);
        });
    }

    @Test
    void testLoad_LevelWithTimeLimit_ShouldParseCorrectly() throws IOException {
        // 创建包含时间限制的JSON内容
        String jsonWithTimeLimit = "{\n" +
                "  \"id\": \"timed-level\",\n" +
                "  \"name\": \"Timed Level\",\n" +
                "  \"width\": 416,\n" +
                "  \"height\": 416,\n" +
                "  \"base\": {\n" +
                "    \"x\": 193,\n" +
                "    \"y\": 386\n" +
                "  },\n" +
                "  \"obstacles\": [],\n" +
                "  \"timeLimitSeconds\": 120\n" +
                "}";

        // 创建临时文件
        Path tempFile = tempDir.resolve("timed-level.json");
        Files.writeString(tempFile, jsonWithTimeLimit);

        // 加载关卡
        LevelDefinition level = levelLoader.load(tempFile);

        // 验证结果
        assertNotNull(level);
        assertEquals("timed-level", level.id());
        assertEquals(Integer.valueOf(120), level.timeLimitSeconds());
    }

    @Test
    void testLoad_LevelWithEnemySettings_ShouldParseCorrectly() throws IOException {
        // 创建包含敌人设置的JSON内容
        String jsonWithEnemySettings = "{\n" +
                "  \"id\": \"custom-enemy-level\",\n" +
                "  \"name\": \"Custom Enemy Level\",\n" +
                "  \"width\": 416,\n" +
                "  \"height\": 416,\n" +
                "  \"base\": {\n" +
                "    \"x\": 193,\n" +
                "    \"y\": 386\n" +
                "  },\n" +
                "  \"obstacles\": [],\n" +
                "  \"enemySpawnInterval\": 3.5,\n" +
                "  \"eliteSpawnRate\": 0.3\n" +
                "}";

        // 创建临时文件
        Path tempFile = tempDir.resolve("custom-enemy-level.json");
        Files.writeString(tempFile, jsonWithEnemySettings);

        // 加载关卡
        LevelDefinition level = levelLoader.load(tempFile);

        // 验证结果
        assertNotNull(level);
        assertEquals("custom-enemy-level", level.id());
        assertEquals(3.5, level.enemySpawnInterval());
        assertEquals(0.3, level.eliteSpawnRate());
    }
}