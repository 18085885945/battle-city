package com.battlecity.physics;

import com.battlecity.model.Entity;
import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollisionDetectorTest {

    private CollisionDetector collisionDetector;
    private TestEntity entityA;
    private TestEntity entityB;

    @BeforeEach
    void setUp() {
        collisionDetector = new CollisionDetector();
        // 初始化两个测试实体
        entityA = new TestEntity(new Vector2D(0, 0), new Size(10, 10));
        entityB = new TestEntity(new Vector2D(5, 5), new Size(10, 10));
    }

    @Test
    void testCollide_OverlappingEntities_ShouldReturnTrue() {
        // entityA: (0,0) to (10,10)
        // entityB: (5,5) to (15,15)
        // These entities overlap
        assertTrue(collisionDetector.collide(entityA, entityB));
    }

    @Test
    void testCollide_NonOverlappingEntities_ShouldReturnFalse() {
        // Move entityB far away from entityA
        entityB = new TestEntity(new Vector2D(20, 20), new Size(10, 10));
        // entityA: (0,0) to (10,10)
        // entityB: (20,20) to (30,30)
        // These entities do not overlap
        assertFalse(collisionDetector.collide(entityA, entityB));
    }

    @Test
    void testCollide_EntitiesTouchingEdges_ShouldReturnTrue() {
        // Make entityB touch entityA on the right edge
        entityB = new TestEntity(new Vector2D(10, 0), new Size(10, 10));
        // entityA: (0,0) to (10,10)
        // entityB: (10,0) to (20,10)
        // These entities touch at the edge, but should not collide
        assertFalse(collisionDetector.collide(entityA, entityB));
    }

    @Test
    void testCollide_EntitiesTouchingCorners_ShouldReturnTrue() {
        // Make entityB touch entityA at the corner
        entityB = new TestEntity(new Vector2D(10, 10), new Size(10, 10));
        // entityA: (0,0) to (10,10)
        // entityB: (10,10) to (20,20)
        // These entities touch at the corner, but should not collide
        assertFalse(collisionDetector.collide(entityA, entityB));
    }

    @Test
    void testCollide_OneEntityInsideAnother_ShouldReturnTrue() {
        // Make entityB completely inside entityA
        entityB = new TestEntity(new Vector2D(2, 2), new Size(5, 5));
        // entityA: (0,0) to (10,10)
        // entityB: (2,2) to (7,7)
        // entityB is completely inside entityA
        assertTrue(collisionDetector.collide(entityA, entityB));
    }

    // 测试实体类
    private static class TestEntity extends Entity {
        public TestEntity(Vector2D position, Size size) {
            super(position, size);
        }
    }
}