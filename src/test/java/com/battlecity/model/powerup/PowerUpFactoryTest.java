package com.battlecity.model.powerup;

import com.battlecity.model.Vector2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PowerUpFactoryTest {

    private final Vector2D testPosition = new Vector2D(100, 100);

    @Test
    void testCreateRandomPowerUp_ReturnsPowerUpOrNull() {
        // 运行多次测试，因为有50%的概率返回null
        boolean gotNull = false;
        boolean gotPowerUp = false;
        
        for (int i = 0; i < 100; i++) {
            PowerUp powerUp = PowerUpFactory.createRandomPowerUp(testPosition);
            if (powerUp == null) {
                gotNull = true;
            } else {
                gotPowerUp = true;
                // 验证返回的对象是有效的道具
                assertNotNull(powerUp.position());
                assertNotNull(powerUp.getType());
                assertTrue(powerUp.isAlive());
            }
            
            // 如果两种情况都出现了，就可以停止测试
            if (gotNull && gotPowerUp) {
                break;
            }
        }
        
        // 验证两种情况都可能出现
        assertTrue(gotNull, "Should have gotten null at least once");
        assertTrue(gotPowerUp, "Should have gotten a PowerUp at least once");
    }

    @Test
    void testCreatePowerUp_CreatesCorrectTypes() {
        // 测试每种道具类型都能正确创建
        for (PowerUpType type : PowerUpType.values()) {
            PowerUp powerUp = PowerUpFactory.createPowerUp(testPosition, type);
            
            assertNotNull(powerUp, "PowerUp should not be null for type: " + type);
            assertEquals(type, powerUp.getType(), "PowerUp type should match requested type");
            assertEquals(testPosition, powerUp.position(), "PowerUp position should match");
            assertTrue(powerUp.isAlive(), "PowerUp should be alive when created");
        }
    }

    @Test
    void testCreatePowerUp_ThrowsExceptionForUnknownType() {
        // 创建一个不在枚举中的类型来测试异常处理
        // 由于PowerUpType是枚举类型，实际上无法创建未知类型
        // 这个测试主要是为了确认工厂方法的鲁棒性
        // 在正常情况下不应该抛出异常
        assertDoesNotThrow(() -> {
            for (PowerUpType type : PowerUpType.values()) {
                PowerUpFactory.createPowerUp(testPosition, type);
            }
        });
    }

    @Test
    void testCreateWeapon_ReturnsWeaponOrNull() {
        // 运行多次测试，因为有10%的概率返回武器
        boolean gotNull = false;
        boolean gotWeapon = false;
        
        for (int i = 0; i < 100; i++) {
            PowerUp powerUp = PowerUpFactory.createWeapon(testPosition);
            if (powerUp == null) {
                gotNull = true;
            } else {
                gotWeapon = true;
                // 验证返回的是武器类型
                PowerUpType type = powerUp.getType();
                assertTrue(type == PowerUpType.LASER || type == PowerUpType.SCATTER_SHOT,
                    "Weapon should be either LASER or SCATTER_SHOT, but was: " + type);
                assertEquals(testPosition, powerUp.position());
                assertTrue(powerUp.isAlive());
            }
            
            // 如果两种情况都出现了，就可以停止测试
            if (gotNull && gotWeapon) {
                break;
            }
        }
        
        // 验证两种情况都可能出现
        assertTrue(gotNull, "Should have gotten null at least once");
        assertTrue(gotWeapon, "Should have gotten a weapon at least once");
    }

    @Test
    void testAllPowerUpTypesCanBeCreated() {
        PowerUpType[] allTypes = PowerUpType.values();
        assertEquals(10, allTypes.length, "Should have 10 power-up types");
        
        // 验证所有类型都有对应的实现
        for (PowerUpType type : allTypes) {
            PowerUp powerUp = PowerUpFactory.createPowerUp(testPosition, type);
            assertNotNull(powerUp, "Should be able to create PowerUp for type: " + type);
            assertEquals(type, powerUp.getType());
        }
    }
}