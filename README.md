# 碰撞箱
主要由陈怡酉与穆旭负责

碰撞检测模块，实现基于AABB（Axis-Aligned Bounding Box）的碰撞检测算法。

## 功能

- AABB碰撞检测算法
- 实体间碰撞检测
- 支持矩形边界框检测

## 核心类

- `AABB`: AABB边界框实现
- `CollisionDetector`: 碰撞检测器
- `Entity`: 实体基类（提供位置和大小信息）

## 使用示例

```java
CollisionDetector detector = new CollisionDetector();
boolean isColliding = detector.collide(entity1, entity2);
```
