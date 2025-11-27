package com.battlecity.model;

import com.battlecity.map.LevelDefinition;
import com.battlecity.map.ObstacleDefinition;
import com.battlecity.map.TileType;
import com.battlecity.model.projectile.Bullet;
import com.battlecity.model.tank.EnemyTank;
import com.battlecity.model.tank.EnemyTier;
import com.battlecity.model.tank.PlayerTank;
import com.battlecity.model.tank.Tank;
import com.battlecity.model.tank.TankAttributes;
import com.battlecity.model.world.Base;
import com.battlecity.model.world.BrickWall;
import com.battlecity.model.world.Obstacle;
import com.battlecity.model.world.SteelWall;
import com.battlecity.model.world.TerrainTile;
import com.battlecity.physics.CollisionDetector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 世界状态，后续可拆分为 Model 层。
 */
public class GameWorld {

    private final LevelDefinition levelDefinition;
    private final PlayerTank playerTank;
    private final Base base;
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final List<TerrainTile> terrains = new ArrayList<>();
    private final List<Bullet> playerBullets = new ArrayList<>();
    private final List<Bullet> enemyBullets = new ArrayList<>();
    private final List<EnemyTank> enemyTanks = new ArrayList<>();
    private final CollisionDetector collisionDetector = new CollisionDetector();

    private GameWorld(LevelDefinition levelDefinition, PlayerTank playerTank, Base base) {
        this.levelDefinition = levelDefinition;
        this.playerTank = playerTank;
        this.base = base;
    }

    public static GameWorld initialWorld(LevelDefinition definition) {
        Base base = new Base(new Vector2D(definition.base().x(), definition.base().y()), new Size(32, 32));
        
        // 玩家坦克生成在基地上方两个砖块长度的位置（砖块32x32，所以是64像素）
        double playerTankY = definition.base().y() - 64; // 基地上方64像素（两个砖块长度）
        double playerTankX = definition.base().x(); // 与基地x坐标对齐
        
        PlayerTank playerTank = new PlayerTank(
                new Vector2D(playerTankX, playerTankY),
                new TankAttributes(140, 1.0, 300)
        );
        
        GameWorld world = new GameWorld(definition, playerTank, base);
        world.buildObstacles(definition.obstacles());
        // 初始化玩家坦克位置记录
        world.lastPlayerPosition = playerTank.position();
        return world;
    }

    private void buildObstacles(List<ObstacleDefinition> definitions) {
        for (ObstacleDefinition definition : definitions) {
            Vector2D pos = new Vector2D(definition.x(), definition.y());
            Size tileSize = new Size(32, 32);
            if (definition.type() == TileType.BRICK) {
                obstacles.add(new BrickWall(pos, tileSize));
            } else if (definition.type() == TileType.STEEL) {
                obstacles.add(new SteelWall(pos, tileSize));
            } else if (definition.type() == TileType.RIVER || definition.type() == TileType.GRASS) {
                terrains.add(new TerrainTile(definition.type(), pos, tileSize));
            }
        }
    }

    public void update(double deltaSeconds) {
        // 更新玩家坦克（tick用于更新冷却时间等）
        if (playerTank.alive()) {
            playerTank.tick(deltaSeconds);
        } else {
            // 玩家坦克死亡时重置位置记录
            lastPlayerPosition = null;
        }

        // 更新敌方坦克
        Iterator<EnemyTank> enemyIterator = enemyTanks.iterator();
        while (enemyIterator.hasNext()) {
            EnemyTank enemy = enemyIterator.next();
            if (!enemy.alive()) {
                enemyIterator.remove();
                lastEnemyPositions.remove(enemy);
                continue;
            }
            enemy.tick(deltaSeconds);
        }

        // 更新子弹并检测与障碍物的碰撞
        Iterator<Bullet> iterator = playerBullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.update(deltaSeconds);
            if (!bullet.alive() || isOutOfBounds(bullet)) {
                iterator.remove();
                continue;
            }
            // 检测玩家子弹与障碍物的碰撞
            if (handleBulletObstacleCollision(bullet, true)) {
                iterator.remove();
                continue;
            }
            // 检测玩家子弹与敌方子弹的碰撞
            if (handleBulletBulletCollision(bullet, enemyBullets)) {
                iterator.remove();
            }
        }
        iterator = enemyBullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.update(deltaSeconds);
            if (!bullet.alive() || isOutOfBounds(bullet)) {
                iterator.remove();
                continue;
            }
            // 检测敌方子弹与障碍物的碰撞
            if (handleBulletObstacleCollision(bullet, false)) {
                iterator.remove();
                continue;
            }
            // 检测敌方子弹与玩家子弹的碰撞（已在上面处理，这里不需要重复）
        }

        // 碰撞检测和响应
        handleCollisions();
        
        // 检查游戏失败条件（基地血量<=0）
        checkGameOver();
    }
    
    /**
     * 检查游戏是否失败（基地血量<=0）
     * @return 如果游戏失败返回true
     */
    public boolean isGameOver() {
        return !base.alive() || base.health() <= 0;
    }
    
    private void checkGameOver() {
        if (isGameOver()) {
            // 游戏失败，可以在这里触发失败事件
            // 实际的处理会在GameController或GameEngine中
        }
    }

    private boolean isOutOfBounds(Bullet bullet) {
        return bullet.left() < 0 || bullet.right() > levelDefinition.width()
                || bullet.top() < 0 || bullet.bottom() > levelDefinition.height();
    }

    /**
     * 处理子弹与障碍物的碰撞
     * @param bullet 子弹
     * @param isPlayerBullet 是否是玩家子弹
     * @return 如果子弹应该被销毁，返回true
     */
    private boolean handleBulletObstacleCollision(Bullet bullet, boolean isPlayerBullet) {
        Iterator<Obstacle> obstacleIterator = obstacles.iterator();
        while (obstacleIterator.hasNext()) {
            Obstacle obstacle = obstacleIterator.next();
            if (collisionDetector.collide(bullet, obstacle)) {
                // 子弹击中障碍物，销毁子弹
                bullet.destroy();
                
                // 如果是砖墙（可破坏），则移除障碍物
                if (obstacle.destructible() && obstacle instanceof BrickWall) {
                    obstacleIterator.remove();
                }
                // 钢墙不可破坏，只销毁子弹
                
                return true;
            }
        }
        
        // 检测子弹与基地的碰撞
        if (collisionDetector.collide(bullet, base)) {
            bullet.destroy();
            // 任何子弹击中基地，基地都受到伤害
            base.damage();
            return true;
        }
        
        return false;
    }

    /**
     * 处理子弹与子弹的碰撞（子弹相撞时都销毁）
     * @param bullet 当前子弹
     * @param otherBullets 其他子弹列表
     * @return 如果子弹应该被销毁，返回true
     */
    private boolean handleBulletBulletCollision(Bullet bullet, List<Bullet> otherBullets) {
        Iterator<Bullet> otherIterator = otherBullets.iterator();
        while (otherIterator.hasNext()) {
            Bullet otherBullet = otherIterator.next();
            if (otherBullet.alive() && collisionDetector.collide(bullet, otherBullet)) {
                // 两个子弹相撞，都销毁
                bullet.destroy();
                otherBullet.destroy();
                otherIterator.remove();
                return true;
            }
        }
        return false;
    }

    private void handleCollisions() {
        // 玩家坦克碰撞检测
        if (playerTank.alive()) {
            handlePlayerTankCollisions();
        }

        // 敌方坦克碰撞检测
        for (EnemyTank enemy : enemyTanks) {
            if (enemy.alive()) {
                handleEnemyTankCollisions(enemy);
            }
        }
    }

    private Vector2D lastPlayerPosition;

    public void savePlayerPositionBeforeMove() {
        if (playerTank != null && playerTank.alive()) {
            lastPlayerPosition = playerTank.position();
        }
    }

    private void handlePlayerTankCollisions() {
        // 使用保存的移动前位置
        Vector2D currentPos = playerTank.position();
        Vector2D originalPos = lastPlayerPosition != null ? lastPlayerPosition : currentPos;

        // 玩家坦克与障碍物碰撞
        for (Obstacle obstacle : obstacles) {
            if (collisionDetector.collide(playerTank, obstacle)) {
                playerTank.setPosition(originalPos);
                lastPlayerPosition = originalPos;
                return;
            }
        }

        // 玩家坦克与基地碰撞
        if (collisionDetector.collide(playerTank, base)) {
            playerTank.setPosition(originalPos);
            lastPlayerPosition = originalPos;
            return;
        }

        // 玩家坦克与敌方坦克碰撞
        for (EnemyTank enemy : enemyTanks) {
            if (enemy.alive() && collisionDetector.collide(playerTank, enemy)) {
                playerTank.setPosition(originalPos);
                lastPlayerPosition = originalPos;
                // 玩家坦克碰到敌方坦克时扣血
                playerTank.takeDamage(1);
                return;
            }
        }

        // 边界检测（确保不能穿过任何边界，包括下边缘）
        if (playerTank.left() < 0 || playerTank.right() > levelDefinition.width()
                || playerTank.top() < 0 || playerTank.bottom() > levelDefinition.height()) {
            playerTank.setPosition(originalPos);
            lastPlayerPosition = originalPos;
            return;
        }

        // 没有碰撞，更新位置记录为当前位置
        lastPlayerPosition = currentPos;
    }

    private final java.util.Map<EnemyTank, Vector2D> lastEnemyPositions = new java.util.HashMap<>();

    private void handleEnemyTankCollisions(EnemyTank enemy) {
        Vector2D currentPos = enemy.position();
        Vector2D originalPos = lastEnemyPositions.getOrDefault(enemy, currentPos);
        lastEnemyPositions.put(enemy, currentPos);

        // 敌方坦克与障碍物碰撞
        for (Obstacle obstacle : obstacles) {
            if (collisionDetector.collide(enemy, obstacle)) {
                enemy.setPosition(originalPos);
                lastEnemyPositions.put(enemy, originalPos);
                return;
            }
        }

        // 敌方坦克与基地碰撞
        if (collisionDetector.collide(enemy, base)) {
            enemy.setPosition(originalPos);
            lastEnemyPositions.put(enemy, originalPos);
            return;
        }

        // 敌方坦克与玩家坦克碰撞（不扣血，只阻挡）
        if (playerTank.alive() && collisionDetector.collide(enemy, playerTank)) {
            enemy.setPosition(originalPos);
            lastEnemyPositions.put(enemy, originalPos);
            return;
        }

        // 敌方坦克与其他敌方坦克碰撞
        for (EnemyTank other : enemyTanks) {
            if (other != enemy && other.alive() && collisionDetector.collide(enemy, other)) {
                enemy.setPosition(originalPos);
                lastEnemyPositions.put(enemy, originalPos);
                return;
            }
        }

        // 边界检测
        if (enemy.left() < 0 || enemy.right() > levelDefinition.width()
                || enemy.top() < 0 || enemy.bottom() > levelDefinition.height()) {
            enemy.setPosition(originalPos);
            lastEnemyPositions.put(enemy, originalPos);
        }
    }

    public PlayerTank playerTank() {
        return playerTank;
    }

    public Base base() {
        return base;
    }

    public LevelDefinition definition() {
        return levelDefinition;
    }

    public List<Obstacle> obstacles() {
        return obstacles;
    }

    public List<EnemyTank> enemyTanks() {
        return enemyTanks;
    }

    public List<Bullet> playerBullets() {
        return playerBullets;
    }

    public List<Bullet> enemyBullets() {
        return enemyBullets;
    }

    public List<TerrainTile> terrains() {
        return terrains;
    }

    /**
     * 添加玩家子弹
     */
    public void addPlayerBullet(Bullet bullet) {
        playerBullets.add(bullet);
    }

    /**
     * 添加敌方子弹
     */
    public void addEnemyBullet(Bullet bullet) {
        enemyBullets.add(bullet);
    }

    /**
     * 添加敌方坦克
     */
    public void addEnemyTank(EnemyTank enemy) {
        enemyTanks.add(enemy);
    }
}

