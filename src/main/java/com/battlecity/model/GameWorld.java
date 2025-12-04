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
import com.battlecity.model.world.River;
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
    private PlayerTank playerTank; // 改为非final，因为需要在初始化时设置
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
        // 基地位置：底部与地图下边界对齐
        // 调整基地核心大小为30x30，使得砖块可以正好与下边缘相接
        double baseSize = 30; // 基地核心大小
        double baseY = definition.height() - baseSize; // 基地底部与地图下边界对齐
        double baseX = definition.base().x();
        Base base = new Base(new Vector2D(baseX, baseY), new Size(baseSize, baseSize));
        
        GameWorld world = new GameWorld(definition, null, base); // 先创建world，稍后添加坦克
        
        // 在基地周围生成一圈砖块，距离基地一个砖块，砖块与下边缘相接
        world.buildBaseProtectionWalls(baseX, baseY, baseSize);
        
        // 构建其他障碍物
        world.buildObstacles(definition.obstacles());
        
        // 玩家坦克在砖块外随机生成，不与砖块重叠
        PlayerTank playerTank = world.generatePlayerTankPosition(definition);
        world.playerTank = playerTank; // 设置玩家坦克
        
        // 初始化玩家坦克位置记录
        world.lastPlayerPosition = playerTank.position();
        return world;
    }
    
    /**
     * 在基地周围生成一圈砖块，距离基地一个砖块，砖块与下边缘相接且每个砖块相连
     * 布局：
     *   砖砖砖砖砖  (上方5个砖块)
     *   砖      砖  (左侧上方 + 右侧上方)
     *   砖  基  砖  (左侧中间 + 基地核心 + 右侧中间)
     *   砖      砖  (左侧下方 + 右侧下方，与下边缘相接)
     * 
     * 注意：每个砖块是独立的，与基地保持一个砖块的距离，砖块之间相连无间隙
     */
    private void buildBaseProtectionWalls(double baseX, double baseY, double baseSize) {
        double brickSize = 15; // 砖块大小（15x15）
        double gap = 15; // 基地与砖块的距离（一个砖块宽度）
        
        // 基地范围：左上角(baseX, baseY)，大小baseSize x baseSize
        // 基地右边界：baseX + baseSize
        // 基地下边界：baseY + baseSize（与地图下边界对齐）
        
        // 根据实际尺寸动态计算上方砖块数量
        // 覆盖范围：从 baseX - gap 到 baseX + baseSize + gap
        // 总宽度 = baseSize + 2 * gap
        double topStartX = baseX - gap;
        double topEndX = baseX + baseSize + gap;
        double topWidth = topEndX - topStartX;
        int topBrickCount = (int)Math.ceil(topWidth / brickSize);
        double topY = baseY - gap - brickSize; // 砖块顶部y坐标
        
        // 上方砖块：每个砖块15宽，相连无间隙
        for (int i = 0; i < topBrickCount; i++) {
            double x = topStartX + i * brickSize;
            obstacles.add(new BrickWall(new Vector2D(x, topY), new Size(brickSize, brickSize)));
        }
        
        // 根据实际尺寸动态计算左侧砖块数量
        // 覆盖范围：从上方砖块位置（topY）到地图下边缘（baseY + baseSize）
        // 总高度 = (baseY + baseSize) - topY = baseSize + gap + brickSize
        double leftX = baseX - gap - brickSize; // 砖块左边界x坐标
        double leftStartY = topY; // 从上方砖块位置开始
        double leftEndY = baseY + baseSize; // 到地图下边缘
        double leftHeight = leftEndY - leftStartY;
        int leftBrickCount = (int)Math.ceil(leftHeight / brickSize);
        
        // 左侧砖块：每个砖块15高，相连无间隙，延伸到下边缘
        for (int i = 0; i < leftBrickCount; i++) {
            double y = leftStartY + i * brickSize;
            obstacles.add(new BrickWall(new Vector2D(leftX, y), new Size(brickSize, brickSize)));
        }
        
        // 右侧砖块：与左侧对称，数量相同
        double rightX = baseX + baseSize + gap; // 砖块左边界x坐标
        for (int i = 0; i < leftBrickCount; i++) {
            double y = leftStartY + i * brickSize;
            obstacles.add(new BrickWall(new Vector2D(rightX, y), new Size(brickSize, brickSize)));
        }
    }
    
    /**
     * 生成玩家坦克位置，在砖块外随机生成，不与砖块重叠
     */
    private PlayerTank generatePlayerTankPosition(LevelDefinition definition) {
        java.util.Random random = new java.util.Random();
        double tankSize = 26; // 坦克大小（26x26）
        double baseX = definition.base().x();
        double baseSize = 30; // 基地核心大小
        double baseY = definition.height() - baseSize;
        
        // 砖块保护圈的范围（砖块现在是15x15）
        double brickSize = 15;
        double gap = 15;
        double wallLeft = baseX - gap - brickSize; // baseX - 30
        double wallRight = baseX + baseSize + gap; // baseX + 45
        
        // 坦克底部与地图下边缘对齐：y坐标固定为 definition.height() - tankSize
        double y = definition.height() - tankSize;
        
        // 尝试生成位置，最多尝试200次
        for (int attempt = 0; attempt < 200; attempt++) {
            double x;
            
            // 只在x轴上随机选择位置，避开基地区域
            // 策略：随机选择左侧或右侧
            if (random.nextBoolean()) {
                // 左侧区域：x < wallLeft
                if (wallLeft - tankSize > 0) {
                    x = random.nextDouble() * (wallLeft - tankSize);
                } else {
                    // 左侧空间不足，尝试右侧
                    double rightStartX = Math.max(wallRight + 5, 0);
                    if (rightStartX + tankSize > definition.width()) {
                        continue; // 右侧也没有足够空间
                    }
                    x = rightStartX + random.nextDouble() * (definition.width() - rightStartX - tankSize);
                }
            } else {
                // 右侧区域：x > wallRight
                double rightStartX = Math.max(wallRight + 5, 0);
                if (rightStartX + tankSize > definition.width()) {
                    // 右侧空间不足，尝试左侧
                    if (wallLeft - tankSize > 0) {
                        x = random.nextDouble() * (wallLeft - tankSize);
                    } else {
                        continue; // 左右都没有足够空间
                    }
                } else {
                    x = rightStartX + random.nextDouble() * (definition.width() - rightStartX - tankSize);
                }
            }
            
            // 确保x在边界内
            if (x < 0 || x + tankSize > definition.width()) {
                continue;
            }
            
            // 检查是否与基地区域重叠（包括基地核心和保护砖块）
            // 基地区域：x在wallLeft到wallRight之间
            if (x < wallRight && x + tankSize > wallLeft) {
                continue; // 与基地区域重叠，跳过
            }
            
            // 检查是否与障碍物重叠
            Vector2D tankPos = new Vector2D(x, y);
            PlayerTank testTank = new PlayerTank(tankPos, new TankAttributes(140, 1.0, 300));
            boolean overlaps = false;
            
            for (Obstacle obstacle : obstacles) {
                if (collisionDetector.collide(testTank, obstacle)) {
                    overlaps = true;
                    break;
                }
            }
            
            // 检查是否与基地核心重叠
            if (!overlaps && collisionDetector.collide(testTank, base)) {
                overlaps = true;
            }
            
            if (!overlaps) {
                return testTank;
            }
        }
        
        // 如果200次都失败，使用默认位置：地图左下角或右下角，避开基地区域
        double defaultX = 10; // 左侧留出10像素
        // 如果默认位置在基地区域内，调整到右侧
        if (defaultX < wallRight && defaultX + tankSize > wallLeft) {
            defaultX = Math.max(wallRight + 10, 0);
            // 如果右侧也没有空间，尝试左侧更靠左的位置
            if (defaultX + tankSize > definition.width()) {
                defaultX = Math.max(0, wallLeft - tankSize - 10);
            }
        }
        double defaultY = definition.height() - tankSize; // 与下边缘对齐
        return new PlayerTank(new Vector2D(defaultX, defaultY), new TankAttributes(140, 1.0, 300));
    }

    private void buildObstacles(List<ObstacleDefinition> definitions) {
        for (ObstacleDefinition definition : definitions) {
            Vector2D pos = new Vector2D(definition.x(), definition.y());
            // 砖块和铁块：每个砖块17x17，使得4个砖块（2x2）的总大小34x34，略大于坦克（32x32）
            Size brickSteelSize = new Size(17, 17);
            // 水路和草丛：34x34，比坦克（32x32）大一点
            Size terrainSize = new Size(34, 34);
            
            if (definition.type() == TileType.BRICK) {
                obstacles.add(new BrickWall(pos, brickSteelSize));
            } else if (definition.type() == TileType.STEEL) {
                obstacles.add(new SteelWall(pos, brickSteelSize));
            } else if (definition.type() == TileType.RIVER) {
                // 水路作为障碍物，阻挡坦克移动
                obstacles.add(new River(pos, terrainSize));
            } else if (definition.type() == TileType.GRASS) {
                // 草丛作为地形，不阻挡移动，但会遮住坦克
                terrains.add(new TerrainTile(definition.type(), pos, terrainSize));
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

        // 如果当前位置与移动前位置相同，说明没有移动，不需要检测
        if (currentPos.equals(originalPos)) {
            return;
        }

        // 玩家坦克与障碍物碰撞 - 直接阻止，不移动
        for (Obstacle obstacle : obstacles) {
            if (collisionDetector.collide(playerTank, obstacle)) {
                playerTank.setPosition(originalPos);
                lastPlayerPosition = originalPos;
                return;
            }
        }

        // 玩家坦克与基地碰撞 - 直接阻止，不移动
        if (collisionDetector.collide(playerTank, base)) {
            playerTank.setPosition(originalPos);
            lastPlayerPosition = originalPos;
            return;
        }

        // 玩家坦克与敌方坦克碰撞 - 直接阻止，不移动，但扣血
        for (EnemyTank enemy : enemyTanks) {
            if (enemy.alive() && collisionDetector.collide(playerTank, enemy)) {
                playerTank.setPosition(originalPos);
                lastPlayerPosition = originalPos;
                // 玩家坦克碰到敌方坦克时扣血
                playerTank.takeDamage(1);
                return;
            }
        }

        // 边界检测（确保不能穿过任何边界，包括下边缘）- 直接阻止，不移动
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

