package com.battlecity.model;

import com.battlecity.audio.AudioManager;
import com.battlecity.map.LevelDefinition;
import com.battlecity.map.ObstacleDefinition;
import com.battlecity.map.TileType;
import com.battlecity.model.projectile.Bullet;
import com.battlecity.model.powerup.PowerUp;
import com.battlecity.model.powerup.PowerUpFactory;
import com.battlecity.model.powerup.PowerUpType;
import com.battlecity.model.tank.EnemyTank;
import com.battlecity.model.tank.EnemyTier;
import com.battlecity.model.tank.EnemyTankFactory;
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
import com.battlecity.effect.EffectManager;
import com.battlecity.effect.ExplosionEffect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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
    private final List<PowerUp> powerUps = new ArrayList<>(); // 道具列表
    private final CollisionDetector collisionDetector = new CollisionDetector();
    private final com.battlecity.ai.EnemyAIController aiController = new com.battlecity.ai.EnemyAIController();
    private final EffectManager effectManager = new EffectManager();
    
    // 敌对坦克生成相关
    private static final int MAX_ENEMY_ON_FIELD = 6; // 场上最大敌对坦克数量
    private static final double ENEMY_SPAWN_INTERVAL_NORMAL = 5.0; // 正常生成间隔（秒）
    private static final double ENEMY_SPAWN_INTERVAL_FORCE = 20.0; // 强制生成间隔（秒）
    private double enemySpawnTimer = 0.0; // 生成计时器
    
    // 胜利条件相关
    private static final int BASE_ENEMIES_PER_WAVE = 5; // 每波敌人数量
    private int enemiesKilled = 0; // 已击杀的敌人数量
    private int enemiesSpawned = 0; // 已生成的敌人数量（包括已死亡的）
    private int score = 0; // 玩家得分
    private int totalEnemiesToDefeat; // 当前关卡需要击杀的总敌人数
    
    // 精英坦克生成相关
    private boolean hasFirstEliteTank = false; // 是否已经生成了第一个精英坦克
    private boolean hasGuaranteedElite = false; // 是否已经确保了本关至少有一个精英坦克
    private EnemyTank firstEliteTank = null; // 第一个生成的精英坦克

    private GameWorld(LevelDefinition levelDefinition, PlayerTank playerTank, Base base) {
        this.levelDefinition = levelDefinition;
        this.playerTank = playerTank;
        this.base = base;
        // 计算当前关卡需要击杀的总敌人数
        this.totalEnemiesToDefeat = calculateTotalEnemies(levelDefinition);
    }
    
    /**
     * 随机选择敌方坦克类型
     * 80%概率普通，20%概率精英（精英类型随机分配）
     * @return 随机选择的敌方坦克类型
     */
    private static EnemyTier randomEnemyTier() {
        if (Math.random() < 0.8) {
            // 80%概率普通
            return EnemyTier.NORMAL;
        } else {
            // 20%概率精英，随机选择4种精英类型之一
            return getRandomEliteTier();
        }
    }
    
    /**
     * 随机选择精英坦克类型
     * @return 随机选择的精英坦克类型
     */
    private static EnemyTier getRandomEliteTier() {
        double rand = Math.random();
        if (rand < 0.25) {
            return EnemyTier.ELITE_SPEED;      // 速度精英怪
        } else if (rand < 0.5) {
            return EnemyTier.ELITE_FIRERATE;  // 射速精英怪
        } else if (rand < 0.75) {
            return EnemyTier.ELITE_AI;         // 高级AI精英怪
        } else {
            return EnemyTier.ELITE_HEALTH;     // 生命精英怪
        }
    }
    
    /**
     * 根据关卡ID计算需要击杀的总敌人数
     * 规则：第一关5个，每增加一关增加5个
     */
    private static int calculateTotalEnemies(LevelDefinition levelDefinition) {
        String levelId = levelDefinition.id();
        if (levelId != null) {
            // 经典模式关卡：classic-level-1, classic-level-2, ..., classic-level-5
            if (levelId.startsWith("classic-level-") || levelId.startsWith("classic-")) {
                int levelNum = 1;
                try {
                    // 提取关卡号
                    if (levelId.startsWith("classic-level-")) {
                        levelNum = Integer.parseInt(levelId.substring("classic-level-".length()));
                    } else if (levelId.startsWith("classic-")) {
                        levelNum = Integer.parseInt(levelId.substring("classic-".length()));
                    }
                    // 限制关卡号在1-5之间
                    levelNum = Math.max(1, Math.min(5, levelNum));
                } catch (NumberFormatException e) {
                    // 如果无法解析关卡号，默认为第一关
                    levelNum = 1;
                }
                // 计算总敌人数：5 * 关卡号
                return BASE_ENEMIES_PER_WAVE * levelNum;
            }
            // 限时模式关卡：根据关卡ID类似处理
            if (levelId.startsWith("timed-") || levelId.startsWith("timed_challenge")) {
                int levelNum = 1;
                try {
                    // 提取关卡号
                    if (levelId.startsWith("timed-") && !levelId.equals("timed-challenge")) {
                        levelNum = Integer.parseInt(levelId.substring("timed-".length()));
                    } else if (levelId.startsWith("timed_challenge")) {
                        levelNum = Integer.parseInt(levelId.substring("timed_challenge".length()));
                    }
                    // 限制关卡号在1-5之间
                    levelNum = Math.max(1, Math.min(5, levelNum));
                } catch (NumberFormatException e) {
                    // 如果无法解析关卡号，默认为第一关
                    levelNum = 1;
                }
                // 计算总敌人数：5 * 关卡号
                return BASE_ENEMIES_PER_WAVE * levelNum;
            }
        }
        // 默认返回5个敌人
        return BASE_ENEMIES_PER_WAVE;
    }

    public static GameWorld initialWorld(LevelDefinition definition) {
        // 基地位置：底部与地图下边界对齐
        // 调整基地核心大小为30x30，使得砖块可以正好与下边缘相接
        double baseSize = 30; // 基地核心大小
        double baseY = definition.height() - baseSize; // 基地底部与地图下边界对齐
        double baseX = definition.base().x();
        Base base = new Base(new Vector2D(baseX, baseY), new Size(baseSize, baseSize));
        
        // 检查是否为限时模式关卡，如果是则根据敌人数量计算时间限制
        LevelDefinition updatedDefinition = definition;
        String levelId = definition.id();
        if (levelId != null && levelId.startsWith("timed-")) {
            // 限时模式关卡，根据敌人数量计算时间限制
            // 先计算当前关卡需要击杀的总敌人数
            int totalEnemies = calculateTotalEnemies(definition);
            // 基础时间：5个敌人30秒，每增加1个敌人增加5秒
            int timeLimit = 30 + (totalEnemies - 5) * 5;
            // 创建新的LevelDefinition，添加时间限制
            updatedDefinition = new LevelDefinition(
                    definition.id(),
                    definition.name(),
                    definition.width(),
                    definition.height(),
                    definition.base(),
                    definition.obstacles(),
                    timeLimit
            );
        }
        
        GameWorld world = new GameWorld(updatedDefinition, null, base); // 先创建world，稍后添加坦克
        
        // 在基地周围生成一圈砖块，距离基地一个砖块，砖块与下边缘相接
        world.buildBaseProtectionWalls(baseX, baseY, baseSize);
        
        // 构建其他障碍物
        world.buildObstacles(updatedDefinition.obstacles());
        
        // 玩家坦克在砖块外随机生成，不与砖块重叠
        PlayerTank playerTank = world.generatePlayerTankPosition(updatedDefinition);
        world.playerTank = playerTank; // 设置玩家坦克
        
        // 初始化玩家坦克位置记录
        world.lastPlayerPosition = playerTank.position();
        
        // 游戏开始时生成5辆敌方坦克
        boolean hasElite = false;
        for (int i = 0; i < 5; i++) {
            EnemyTier tier;
            if (i == 0) {
                // 第一辆坦克总是精英坦克
                tier = getRandomEliteTier();
                hasElite = true;
            } else {
                // 后续坦克随机选择，但确保至少有一个精英坦克
                if (!hasElite && i == 4) {
                    // 最后一辆坦克，如果还没有精英坦克，生成精英坦克
                    tier = getRandomEliteTier();
                    hasElite = true;
                } else {
                    tier = randomEnemyTier();
                    if (tier.isElite()) {
                        hasElite = true;
                    }
                }
            }
            EnemyTank newEnemy = world.generateEnemyTankPosition(updatedDefinition, tier);
            if (newEnemy != null) {
                world.enemyTanks.add(newEnemy);
                world.lastEnemyPositions.put(newEnemy, newEnemy.position());
                world.enemiesSpawned++; // 增加已生成敌人计数
                
                // 记录第一个精英坦克
                if (tier.isElite() && !world.hasFirstEliteTank) {
                    world.hasFirstEliteTank = true;
                    world.firstEliteTank = newEnemy;
                }
            }
        }
        // 标记已经确保了本关至少有一个精英坦克
        world.hasGuaranteedElite = true;
        
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
            // 玩家坦克：速度140，攻速1秒3发（333ms冷却）
            PlayerTank testTank = new PlayerTank(tankPos, new TankAttributes(140, 1.0, 333));
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
        // 玩家坦克：速度140，攻速1秒3发（333ms冷却）
        return new PlayerTank(new Vector2D(defaultX, defaultY), new TankAttributes(140, 1.0, 333));
    }

    /**
     * 生成敌对坦克的生成位置
     * 敌对坦克只能从上边缘和离上边缘一个基地的高度的左右边缘出现
     * 
     * @param definition 关卡定义
     * @param tier 敌对坦克等级
     * @return 生成的敌对坦克，如果无法生成则返回null
     */
    public EnemyTank generateEnemyTankPosition(LevelDefinition definition, EnemyTier tier) {
        java.util.Random random = new java.util.Random();
        double tankSize = 26; // 坦克大小（26x26）
        double baseSize = 30; // 基地核心大小（用于确定左右边缘的y坐标）
        
        // 生成位置选项：
        // 1. 从上边缘：y = 0，x 随机（在边界内）
        // 2. 从左边缘：y = baseSize，x = 0
        // 3. 从右边缘：y = baseSize，x = definition.width() - tankSize
        
        // 尝试生成位置，最多尝试200次
        for (int attempt = 0; attempt < 200; attempt++) {
            double x, y;
            int spawnType = random.nextInt(3); // 0: 上边缘, 1: 左边缘, 2: 右边缘
            
            if (spawnType == 0) {
                // 从上边缘生成：y = 0，x 随机
                y = 0;
                x = random.nextDouble() * (definition.width() - tankSize);
            } else if (spawnType == 1) {
                // 从左边缘生成：y = baseSize，x = 0
                y = baseSize;
                x = 0;
            } else {
                // 从右边缘生成：y = baseSize，x = definition.width() - tankSize
                y = baseSize;
                x = definition.width() - tankSize;
            }
            
            // 确保在边界内
            if (x < 0 || x + tankSize > definition.width() || y < 0 || y + tankSize > definition.height()) {
                continue;
            }
            
            // 检查是否与障碍物重叠
            Vector2D tankPos = new Vector2D(x, y);
            EnemyTank testTank = EnemyTankFactory.create(tankPos, tier);
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
            
            // 检查是否与玩家坦克重叠
            if (!overlaps && playerTank != null && playerTank.alive() && 
                collisionDetector.collide(testTank, playerTank)) {
                overlaps = true;
            }
            
            // 检查是否与其他敌对坦克重叠
            if (!overlaps) {
                for (EnemyTank other : enemyTanks) {
                    if (other.alive() && collisionDetector.collide(testTank, other)) {
                        overlaps = true;
                        break;
                    }
                }
            }
            
            if (!overlaps) {
                return testTank;
            }
        }
        
        // 如果200次都失败，返回null（调用者可以稍后重试）
        return null;
    }

    private void buildObstacles(List<ObstacleDefinition> definitions) {
        for (ObstacleDefinition definition : definitions) {
            Vector2D pos = new Vector2D(definition.x(), definition.y());

            // 逻辑说明：
            // - 关卡 JSON 中的一块 BRICK，视为一个「大砖块」格子（约 32x32 的区域）
            // - 为了让砖块单位更精细，我们在物理世界中用 4 个小砖块（2x2）来拼成这个大格子
            //   小砖块大小为 16x16，摆放在 (x, y), (x+16, y), (x, y+16), (x+16, y+16)
            // - 这样：整体占用面积仍然是 32x32，地图布局尺寸不变，但视觉上砖块单位变小
            //
            // 钢块大小与单个砖块相同，为 16x16。
            Size smallBrickSize = new Size(16, 16);
            Size steelSize = new Size(16, 16);

            // 水路和草丛保持略大于坦克的尺寸，适当有一点重叠视觉效果即可
            Size terrainSize = new Size(34, 34);

            if (definition.type() == TileType.BRICK) {
                // 四个小砖块合成一个大砖块区域
                double x = pos.x();
                double y = pos.y();
                obstacles.add(new BrickWall(new Vector2D(x, y), smallBrickSize));
                obstacles.add(new BrickWall(new Vector2D(x + smallBrickSize.width(), y), smallBrickSize));
                obstacles.add(new BrickWall(new Vector2D(x, y + smallBrickSize.height()), smallBrickSize));
                obstacles.add(new BrickWall(new Vector2D(x + smallBrickSize.width(), y + smallBrickSize.height()), smallBrickSize));
            } else if (definition.type() == TileType.STEEL) {
                obstacles.add(new SteelWall(pos, steelSize));
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
                // 敌方坦克死亡，检查是否是精英坦克
                if (enemy.tier() != EnemyTier.NORMAL) {
                    PowerUp powerUp;
                    // 检查是否是第一个精英坦克，如果是，100%掉落道具
                    if (enemy == firstEliteTank) {
                        // 第一个精英坦克，100%掉落道具
                        // 确保生成道具，重试直到成功
                        do {
                            powerUp = PowerUpFactory.createPowerUp(
                                enemy.center(), 
                                PowerUpType.values()[new Random().nextInt(PowerUpType.values().length)]
                            );
                        } while (powerUp == null);
                        // 清除第一个精英坦克标记
                        firstEliteTank = null;
                    } else {
                        // 其他精英坦克，50%概率掉落道具
                        powerUp = PowerUpFactory.createRandomPowerUp(enemy.center());
                    }
                    if (powerUp != null) {
                        powerUps.add(powerUp);
                    }
                }
                enemyIterator.remove();
                lastEnemyPositions.remove(enemy);
                continue;
            }
            enemy.tick(deltaSeconds);
        }
        
        // 更新敌方坦克AI
        aiController.update(this, enemyTanks, deltaSeconds);
        
        // 更新道具
        updatePowerUps(deltaSeconds);
        
        // 生成新的敌对坦克
        enemySpawnTimer += deltaSeconds;
        
        boolean shouldSpawn = false;
        int currentEnemyCount = enemyTanks.size();
        int remainingEnemies = getRemainingEnemies();
        int needToSpawn = 0; // 需要生成的数量
        
        // 无尽模式特殊变量
        boolean isEndlessMode = false;
        
        // 更新无尽模式标志
        // 注意：这里我们无法直接获取GameState，因为GameWorld没有引用GameEngine或GameStateManager
        // 我们通过关卡ID来判断是否为无尽模式
        String levelId = levelDefinition.id();
        if (levelId != null && levelId.startsWith("endless-")) {
            isEndlessMode = true;
        }
        
        if (isEndlessMode) {
            // 无尽模式特殊规则：
            // 1. 当地图上没有敌人时，开始计时
            // 2. 半秒钟后生成新的一波敌人
            // 3. 每波生成5个敌人
            if (currentEnemyCount == 0) {
                if (enemySpawnTimer >= 0.5) {
                    shouldSpawn = true;
                    needToSpawn = 5; // 每波生成5个敌人
                }
            }
        } else {
            // 非无尽模式：使用原有规则
            // 生成条件：
            // 1. 如果场上敌方坦克数 < 2，每次刷新都尝试生成，直到达到2辆
            // 2. 否则：敌方坦克数 < 6 且 距离上次生成 >= 5秒
            // 3. 或者 距离上次生成 >= 20秒（强制生成）
            // 4. 如果关卡剩余敌人数量过小（小于需要补充的数量），则不再生成
            if (currentEnemyCount < 2) {
                // 场上坦克数小于2，尝试补充到2辆
                needToSpawn = 2 - currentEnemyCount;
                // 检查剩余敌人数量是否足够
                if (remainingEnemies >= needToSpawn) {
                    shouldSpawn = true;
                }
            } else if (currentEnemyCount < MAX_ENEMY_ON_FIELD && enemySpawnTimer >= ENEMY_SPAWN_INTERVAL_NORMAL) {
                // 场上坦克数在2-6之间，按正常间隔生成
                needToSpawn = 1;
                // 检查剩余敌人数量是否足够
                if (remainingEnemies >= needToSpawn) {
                    shouldSpawn = true;
                }
            } else if (enemySpawnTimer >= ENEMY_SPAWN_INTERVAL_FORCE) {
                // 强制生成（20秒间隔）
                needToSpawn = 1;
                // 检查剩余敌人数量是否足够
                if (remainingEnemies >= needToSpawn) {
                    shouldSpawn = true;
                }
            }
        }
        
        if (shouldSpawn) {
            if (isEndlessMode) {
                // 无尽模式：忽略剩余敌人数量限制，一直生成敌人
                for (int i = 0; i < needToSpawn; i++) {
                    // 随机选择普通或精英坦克（80%概率普通，20%概率精英，精英类型随机分配）
                    EnemyTier tier = randomEnemyTier();
                    EnemyTank newEnemy = generateEnemyTankPosition(levelDefinition, tier);
                    if (newEnemy != null) {
                        enemyTanks.add(newEnemy);
                        lastEnemyPositions.put(newEnemy, newEnemy.position());
                        enemiesSpawned++; // 增加已生成敌人计数
                    }
                }
                enemySpawnTimer = 0.0; // 重置计时器
            } else {
                // 非无尽模式：检查剩余敌人数量
                if (enemiesSpawned < totalEnemiesToDefeat && remainingEnemies >= needToSpawn) {
                    // 随机选择普通或精英坦克（80%概率普通，20%概率精英，精英类型随机分配）
                    EnemyTier tier = randomEnemyTier();
                    EnemyTank newEnemy = generateEnemyTankPosition(levelDefinition, tier);
                    if (newEnemy != null) {
                        enemyTanks.add(newEnemy);
                        lastEnemyPositions.put(newEnemy, newEnemy.position());
                        enemiesSpawned++; // 增加已生成敌人计数
                        enemySpawnTimer = 0.0; // 重置计时器
                    }
                    // 如果生成失败（位置被占用），不重置计时器，继续累积时间
                    // 但如果场上坦克数 < 2，下次刷新会继续尝试生成
                }
            }
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
            // 检测玩家子弹与敌方坦克的碰撞
            boolean hitEnemy = false;
            for (EnemyTank enemy : enemyTanks) {
                if (enemy.alive() && collisionDetector.collide(bullet, enemy)) {
                    // 检查是否是秒杀模式
                    if (bullet.isOneShotMode()) {
                        // 秒杀模式，直接摧毁坦克
                        enemy.setHealth(0);
                    } else {
                        enemy.takeDamage(1);
                    }
                    // 每次击中敌人加1分
                    increaseScore();
                    // 非穿透子弹击中敌人后销毁
                    if (!bullet.canPenetrate()) {
                        bullet.destroy();
                    }
                    // 如果敌人被击杀，增加击杀计数
                    if (!enemy.alive()) {
                        enemiesKilled++;
                        // 播放爆炸音效
                        com.battlecity.audio.AudioManager.getInstance().playSound("explosion");
                        // 创建爆炸特效
                        createExplosion(enemy.center(), 30, 0.5);
                    }
                    hitEnemy = true;
                    break;
                }
            }
            if (hitEnemy && !bullet.canPenetrate()) {
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
            // 检测敌方子弹与玩家坦克的碰撞
            if (playerTank != null && playerTank.alive() && collisionDetector.collide(bullet, playerTank)) {
                playerTank.takeDamage(1);
                // 播放爆炸音效
                com.battlecity.audio.AudioManager.getInstance().playSound("explosion");
                // 创建爆炸特效
                createExplosion(playerTank.center(), 30, 0.5);
                bullet.destroy();
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
        
        // 检测玩家与道具的碰撞
        handlePowerUpCollection();
        
        // 更新特效
        effectManager.update(deltaSeconds);
        
        // 检查游戏失败条件（基地血量<=0）
        checkGameOver();
        
        // 检查胜利条件（在失败检查之后，避免同时触发）
        checkVictory();
    }
    
    /**
     * 更新道具状态
     */
    private void updatePowerUps(double deltaSeconds) {
        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp powerUp = powerUpIterator.next();
            powerUp.update(deltaSeconds);
            if (!powerUp.isAlive()) {
                powerUpIterator.remove();
            }
        }
    }
    
    /**
     * 处理玩家与道具的碰撞
     */
    private void handlePowerUpCollection() {
        if (!playerTank.alive()) {
            return;
        }
        
        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp powerUp = powerUpIterator.next();
            if (collisionDetector.collide(playerTank, powerUp)) {
                // 播放道具拾取音效
                AudioManager.getInstance().playSound("powerup");
                // 应用道具效果
                powerUp.applyEffect(playerTank);
                powerUp.applyGlobalEffect(this);
                powerUpIterator.remove();
            }
        }
    }
    
    // 游戏失败原因
    private String gameOverReason = null;
    
    /**
     * 检查游戏是否失败（基地血量<=0 或 玩家坦克死亡 或 限时模式超时）
     * @return 如果游戏失败返回true
     */
    public boolean isGameOver() {
        // 如果已经设置了游戏失败原因，直接返回true
        if (gameOverReason != null) {
            return true;
        }
        
        // 检查基地和坦克状态
        boolean baseOrTankDead = !base.alive() || base.health() <= 0 || 
                                 (playerTank != null && !playerTank.alive());
        
        return baseOrTankDead;
    }
    
    /**
     * 设置游戏失败原因
     * @param reason 失败原因："BASE"表示基地被毁，"TANK"表示坦克被毁，"TIME"表示时间结束
     */
    public void setGameOverReason(String reason) {
        this.gameOverReason = reason;
    }
    
    /**
     * 获取游戏失败原因
     * @return 失败原因字符串："BASE" 表示基地被毁，"TANK" 表示坦克被毁，"TIME" 表示时间结束
     */
    public String getGameOverReason() {
        if (gameOverReason != null) {
            return gameOverReason;
        }
        if (!base.alive() || base.health() <= 0) {
            return "BASE";
        }
        if (playerTank != null && !playerTank.alive()) {
            return "TANK";
        }
        return "UNKNOWN";
    }
    
    /**
     * 检查游戏是否胜利
     * @param gameMode 游戏模式
     * @param elapsedSeconds 已用时间（秒）
     * @return 如果游戏胜利返回true
     */
    public boolean isVictory(com.battlecity.engine.state.GameModeType gameMode, double elapsedSeconds) {
        // 经典模式和限时模式：击杀全部敌人
            if (gameMode == com.battlecity.engine.state.GameModeType.CLASSIC || 
                gameMode == com.battlecity.engine.state.GameModeType.TIMED) {
                // 检查是否击杀了全部敌人
                if (enemiesKilled >= totalEnemiesToDefeat) {
                    // 限时模式还需要检查时间限制
                    if (gameMode == com.battlecity.engine.state.GameModeType.TIMED) {
                        Integer timeLimit = levelDefinition.timeLimitSeconds();
                        if (timeLimit != null && elapsedSeconds > timeLimit) {
                            return false; // 超时了，不算胜利
                        }
                    }
                    return true;
                }
        }
        return false;
    }
    
    /**
     * 获取剩余敌人数量（需要击杀的总数 - 已击杀数）
     */
    public int getRemainingEnemies() {
        return Math.max(0, totalEnemiesToDefeat - enemiesKilled);
    }
    
    /**
     * 获取已击杀敌人数量
     */
    public int getEnemiesKilled() {
        return enemiesKilled;
    }
    
    /**
     * 获取总敌人数（需要击杀的数量）
     */
    public int getTotalEnemies() {
        return totalEnemiesToDefeat;
    }
    
    /**
     * 获取玩家得分
     */
    public int getScore() {
        return score;
    }
    
    /**
     * 增加玩家得分
     */
    public void increaseScore() {
        score++;
    }
    
    private void checkGameOver() {
        if (isGameOver()) {
            // 游戏失败，可以在这里触发失败事件
            // 实际的处理会在GameController或GameEngine中
        }
    }
    
    private void checkVictory() {
        // 胜利检查在GameController中进行，这里不需要处理
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
        // 收集所有与子弹碰撞的砖块和钢墙
        List<BrickWall> collidingBricks = new ArrayList<>();
        List<SteelWall> collidingSteelWalls = new ArrayList<>();
        
        Iterator<Obstacle> obstacleIterator = obstacles.iterator();
        while (obstacleIterator.hasNext()) {
            Obstacle obstacle = obstacleIterator.next();
            
            // 河流（River）允许子弹穿过，跳过检测
            if (obstacle instanceof River) {
                continue;
            }
            
            if (collisionDetector.collide(bullet, obstacle)) {
                if (obstacle instanceof BrickWall) {
                    // 收集所有碰撞的砖块
                    collidingBricks.add((BrickWall) obstacle);
                } else if (obstacle instanceof SteelWall) {
                    // 收集所有碰撞的钢墙
                    collidingSteelWalls.add((SteelWall) obstacle);
                }
            }
        }
        
        // 如果击中了砖块，移除所有碰撞的砖块
        if (!collidingBricks.isEmpty()) {
            obstacles.removeAll(collidingBricks);
            // 播放击中砖墙音效
            com.battlecity.audio.AudioManager.getInstance().playSound("hit_brick");
        }
        
        // 如果击中了钢墙
        if (!collidingSteelWalls.isEmpty()) {
            // 检查子弹是否可以穿透钢墙
            if (bullet.canPenetrate()) {
                // 可以穿透，移除所有碰撞的钢墙
                obstacles.removeAll(collidingSteelWalls);
                // 播放击中钢墙音效
                com.battlecity.audio.AudioManager.getInstance().playSound("hit_steel");
            } else {
                // 不能穿透，销毁子弹
                bullet.destroy();
                // 播放击中钢墙音效
                com.battlecity.audio.AudioManager.getInstance().playSound("hit_steel");
                return true;
            }
        }
        
        // 检测子弹与基地的碰撞
        if (collisionDetector.collide(bullet, base)) {
            bullet.destroy();
            // 任何子弹击中基地，基地都受到伤害
            base.damage();
            // 播放基地被击中音效
            com.battlecity.audio.AudioManager.getInstance().playSound("base_destroyed");
            // 创建基地爆炸特效
            createExplosion(base.center(), 40, 0.8);
            return true;
        }
        
        // 如果击中了任何障碍物，且子弹不能穿透，销毁子弹
        if ((!collidingBricks.isEmpty() || !collidingSteelWalls.isEmpty()) && !bullet.canPenetrate()) {
            bullet.destroy();
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

    // 用于记录本帧已处理的碰撞，避免重复扣血
    private final java.util.Set<EnemyTank> processedCollisionsThisFrame = new java.util.HashSet<>();
    
    private void handleCollisions() {
        // 清空本帧碰撞记录
        processedCollisionsThisFrame.clear();
        
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

        // 玩家坦克与敌方坦克碰撞 - 双方都停下，不扣血
        for (EnemyTank enemy : enemyTanks) {
            if (enemy.alive() && collisionDetector.collide(playerTank, enemy)) {
                playerTank.setPosition(originalPos);
                lastPlayerPosition = originalPos;
                // 双方都停下，不扣血
                // 标记这个碰撞已处理，避免在敌方坦克碰撞检测中重复处理
                processedCollisionsThisFrame.add(enemy);
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

        // 敌方坦克与玩家坦克碰撞 - 双方都停下，不扣血
        if (playerTank.alive() && collisionDetector.collide(enemy, playerTank)) {
            enemy.setPosition(originalPos);
            lastEnemyPositions.put(enemy, originalPos);
            // 双方都停下，不扣血
            // 如果这个碰撞还没有处理过，标记已处理
            if (!processedCollisionsThisFrame.contains(enemy)) {
                processedCollisionsThisFrame.add(enemy);
            }
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
    
    public LevelDefinition levelDefinition() {
        return levelDefinition;
    }
    
    public CollisionDetector collisionDetector() {
        return collisionDetector;
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
    
    /**
     * 创建爆炸特效
     * @param position 爆炸位置
     * @param radius 爆炸半径
     * @param duration 持续时间（秒）
     */
    public void createExplosion(com.battlecity.model.Vector2D position, double radius, double duration) {
        ExplosionEffect explosion = new ExplosionEffect(position, duration, radius);
        effectManager.addEffect(explosion);
    }
    
    /**
     * 获取特效管理器
     * @return 特效管理器
     */
    public EffectManager getEffectManager() {
        return effectManager;
    }
}

