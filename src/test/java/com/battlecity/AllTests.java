package com.battlecity;

import com.battlecity.map.LevelLoader;
import com.battlecity.map.LevelDefinition;
import com.battlecity.model.GameWorld;
import com.battlecity.model.tank.PlayerTank;
import com.battlecity.model.world.Base;
import com.battlecity.physics.AABB;
import com.battlecity.physics.CollisionDetector;
import com.battlecity.model.Entity;
import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;
import com.battlecity.model.powerup.PowerUpFactory;
import com.battlecity.model.powerup.PowerUpType;
import com.battlecity.model.powerup.PowerUp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AllTests {

    @Test
    void testLevelLoaderCanLoadLevel() {
        LevelLoader levelLoader = new LevelLoader();
        LevelDefinition levelDefinition = levelLoader.loadFromClasspath("/levels/classic-level-1.json");
        
        assertNotNull(levelDefinition);
        assertEquals("classic-1", levelDefinition.id());
        assertEquals("经典第1关", levelDefinition.name());
        assertEquals(832, levelDefinition.width());
        assertEquals(640, levelDefinition.height());
        assertNotNull(levelDefinition.base());
        assertNotNull(levelDefinition.obstacles());
    }

    @Test
    void testGameWorldInitialization() {
        LevelLoader levelLoader = new LevelLoader();
        LevelDefinition levelDefinition = levelLoader.loadFromClasspath("/levels/classic-level-1.json");
        
        GameWorld world = GameWorld.initialWorld(levelDefinition);
        
        assertNotNull(world);
        assertNotNull(world.playerTank());
        assertNotNull(world.base());
        assertNotNull(world.obstacles());
        assertFalse(world.obstacles().isEmpty());
    }

    @Test
    void testPlayerTankInitialization() {
        LevelLoader levelLoader = new LevelLoader();
        LevelDefinition levelDefinition = levelLoader.loadFromClasspath("/levels/classic-level-1.json");
        
        GameWorld world = GameWorld.initialWorld(levelDefinition);
        PlayerTank playerTank = world.playerTank();
        
        assertNotNull(playerTank);
        assertTrue(playerTank.alive());
        assertEquals(5, playerTank.health()); // 默认生命值为5
    }

    @Test
    void testBaseInitialization() {
        LevelLoader levelLoader = new LevelLoader();
        LevelDefinition levelDefinition = levelLoader.loadFromClasspath("/levels/classic-level-1.json");
        
        GameWorld world = GameWorld.initialWorld(levelDefinition);
        Base base = world.base();
        
        assertNotNull(base);
        assertTrue(base.alive());
        assertEquals(3, base.health()); // 基地默认生命值为3
    }

    @Test
    void testAABBIntersection() {
        AABB aabb1 = new AABB(0, 0, 10, 10);
        AABB aabb2 = new AABB(5, 5, 15, 15);
        AABB aabb3 = new AABB(15, 15, 25, 25);
        
        // 重叠的AABB
        assertTrue(aabb1.intersects(aabb2));
        
        // 不重叠的AABB
        assertFalse(aabb1.intersects(aabb3));
        
        // 边缘接触的AABB（应该返回false，因为边缘接触不算重叠）
        AABB aabb4 = new AABB(10, 0, 20, 10);
        assertFalse(aabb1.intersects(aabb4));
    }

    @Test
    void testCollisionDetector() {
        CollisionDetector detector = new CollisionDetector();
        
        // 创建两个重叠的测试实体
        TestEntity entity1 = new TestEntity(new Vector2D(0, 0), new Size(10, 10));
        TestEntity entity2 = new TestEntity(new Vector2D(5, 5), new Size(10, 10));
        
        // 这两个实体应该碰撞
        assertTrue(detector.collide(entity1, entity2));
        
        // 创建两个不重叠的测试实体
        TestEntity entity3 = new TestEntity(new Vector2D(20, 20), new Size(10, 10));
        
        // 这两个实体不应该碰撞
        assertFalse(detector.collide(entity1, entity3));
    }

    @Test
    void testPowerUpFactoryCreation() {
        // 测试创建随机道具
        PowerUp powerUp = PowerUpFactory.createRandomPowerUp(new Vector2D(100, 100));
        // 可能返回null（50%概率），也可能返回道具
        assertTrue(powerUp == null || powerUp.getClass() != null);
        
        // 测试创建特定类型的道具
        for (PowerUpType type : PowerUpType.values()) {
            PowerUp specificPowerUp = PowerUpFactory.createPowerUp(new Vector2D(100, 100), type);
            assertNotNull(specificPowerUp);
            assertEquals(type, specificPowerUp.getType());
        }
    }

    @Test
    void testPowerUpFactoryWeaponCreation() {
        // 测试创建武器
        PowerUp weapon = PowerUpFactory.createWeapon(new Vector2D(100, 100));
        // 可能返回null（10%概率），也可能返回武器
        assertTrue(weapon == null || 
                  weapon.getType() == PowerUpType.LASER || 
                  weapon.getType() == PowerUpType.SCATTER_SHOT);
    }

    // 简单的测试实体类
    private static class TestEntity extends Entity {
        public TestEntity(Vector2D position, Size size) {
            super(position, size);
        }
    }
}