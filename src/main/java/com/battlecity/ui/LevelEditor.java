package com.battlecity.ui;

import com.battlecity.map.BaseDefinition;
import com.battlecity.map.LevelDefinition;
import com.battlecity.map.ObstacleDefinition;
import com.battlecity.map.TileType;
import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;
import com.battlecity.model.world.BrickWall;
import com.battlecity.model.world.Obstacle;
import com.battlecity.model.world.River;
import com.battlecity.model.world.SteelWall;
import com.battlecity.physics.AABB;
import com.battlecity.physics.CollisionDetector;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;

import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.battlecity.util.ResourceLocator;
import com.battlecity.map.LevelLoader;

/**
 * 地图编辑器
 */
public class LevelEditor {
    
    public enum MapSize {
        SMALL(640, 480, "小地图"),   // 40x30格子
        MEDIUM(832, 640, "中地图"),  // 52x40格子
        LARGE(1024, 768, "大地图");   // 64x48格子
        
        public final int width;
        public final int height;
        public final String name;
        
        MapSize(int width, int height, String name) {
            this.width = width;
            this.height = height;
            this.name = name;
        }
    }
    
    public enum GameMode {
        CLASSIC("经典"),
        TIMED("限时"),
        ENDLESS("无尽"),
        CUSTOM("自定义");
        
        public final String displayName;
        
        GameMode(String displayName) {
            this.displayName = displayName;
        }
    }
    
    private MapSize currentMapSize = MapSize.MEDIUM;
    private GameMode currentMode = GameMode.CUSTOM; // 默认模式为自定义
    private TileType selectedObstacleType = null;
    private List<ObstacleDefinition> obstacles = new ArrayList<>();
    private String currentLevelId = null; // 当前编辑的地图ID（用于修改时覆盖原文件）
    private String currentLevelName = null; // 当前编辑的地图名称（用于修改时保留原名称）
    
    // 敌人刷新速率和精英怪出现频率
    private Double enemySpawnInterval = 5.0; // 默认5秒（中等）
    private Double eliteSpawnRate = 0.2; // 默认20%（中等）
    private Canvas mapCanvas;
    private GraphicsContext gc;
    private CollisionDetector collisionDetector = new CollisionDetector();
    private ScrollPane mapScrollPane; // 地图滚动面板
    private Pane mapContainer; // 地图容器
    private StackPane centeredContainer; // 居中容器，用于在ScrollPane中居中显示地图
    private static final double GRID_SIZE = 16; // 格子大小（一个小砖块大小16x16）
    private static final double TERRAIN_SIZE = 32; // 地形大小（草丛和水路占2x2格子：2x16=32）

    // 拖动绘制/擦除相关状态
    private boolean isPainting = false;
    private PaintMode currentPaintMode = PaintMode.ADD;
    private double lastPaintX = Double.NaN;
    private double lastPaintY = Double.NaN;

    private enum PaintMode {
        ADD,
        REMOVE,
        ERASER  // 橡皮擦模式：删除任何类型的障碍物
    }
    private Runnable onBackCallback;
    private java.util.function.Consumer<Scene> sceneUpdateCallback;
    private java.util.function.Consumer<Boolean> maximizeCallback; // 窗口最大化回调
    private Runnable onSaveCallback; // 保存成功后的回调（用于刷新选关界面）
    private Scene currentScene;
    private BorderPane root;
    private HBox toolbar;
    
    public Scene buildEditorScene(Runnable onBack, java.util.function.Consumer<Scene> sceneUpdateCallback) {
        return buildEditorScene(onBack, sceneUpdateCallback, GameMode.CUSTOM);
    }
    
    public Scene buildEditorScene(Runnable onBack, java.util.function.Consumer<Scene> sceneUpdateCallback, GameMode mode) {
        return buildEditorScene(onBack, sceneUpdateCallback, mode, null);
    }
    
    public Scene buildEditorScene(Runnable onBack, java.util.function.Consumer<Scene> sceneUpdateCallback, GameMode mode, LevelDefinition existingLevel) {
        this.onBackCallback = onBack;
        this.sceneUpdateCallback = sceneUpdateCallback;
        this.currentMode = mode != null ? mode : GameMode.CUSTOM;
        
        // 如果提供了现有地图，加载它
        if (existingLevel != null) {
            loadLevel(existingLevel);
        } else {
            // 重置状态
            currentLevelId = null;
            currentLevelName = null;
            obstacles.clear();
            enemySpawnInterval = 5.0; // 重置为默认值
            eliteSpawnRate = 0.2; // 重置为默认值
        }
        
        return createEditorScene();
    }
    
    public void setOnSaveCallback(Runnable onSaveCallback) {
        this.onSaveCallback = onSaveCallback;
    }
    
    public void setMaximizeCallback(java.util.function.Consumer<Boolean> maximizeCallback) {
        this.maximizeCallback = maximizeCallback;
    }
    
    public MapSize getCurrentMapSize() {
        return currentMapSize;
    }
    
    private Scene createEditorScene() {
        root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // 顶部工具栏
        toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        // 地图大小选择
        Label sizeLabel = new Label("地图大小：");
        ToggleGroup sizeGroup = new ToggleGroup();
        RadioButton smallBtn = new RadioButton("小");
        smallBtn.setToggleGroup(sizeGroup);
        RadioButton mediumBtn = new RadioButton("中");
        mediumBtn.setToggleGroup(sizeGroup);
        RadioButton largeBtn = new RadioButton("大");
        largeBtn.setToggleGroup(sizeGroup);
        
        // 根据当前地图大小设置选中状态
        switch (currentMapSize) {
            case SMALL:
                smallBtn.setSelected(true);
                break;
            case MEDIUM:
                mediumBtn.setSelected(true);
                break;
            case LARGE:
                largeBtn.setSelected(true);
                break;
        }
        
        smallBtn.setOnAction(e -> {
            currentMapSize = MapSize.SMALL;
            updateCanvasSize();
            updateWindowSize();
            // 非大地图时取消最大化
            if (maximizeCallback != null) {
                maximizeCallback.accept(false);
            }
        });
        mediumBtn.setOnAction(e -> {
            currentMapSize = MapSize.MEDIUM;
            updateCanvasSize();
            updateWindowSize();
            // 非大地图时取消最大化
            if (maximizeCallback != null) {
                maximizeCallback.accept(false);
            }
        });
        largeBtn.setOnAction(e -> {
            currentMapSize = MapSize.LARGE;
            updateCanvasSize();
            updateWindowSize();
            // 大地图时最大化窗口
            if (maximizeCallback != null) {
                maximizeCallback.accept(true);
            }
        });
        
        HBox sizeBox = new HBox(5, sizeLabel, smallBtn, mediumBtn, largeBtn);
        sizeBox.setAlignment(Pos.CENTER_LEFT);
        
        // 障碍物选择按钮
        Label obstacleLabel = new Label("障碍物：");
        ToggleGroup obstacleGroup = new ToggleGroup();
        RadioButton brickBtn = new RadioButton("砖块");
        brickBtn.setToggleGroup(obstacleGroup);
        RadioButton steelBtn = new RadioButton("钢墙");
        steelBtn.setToggleGroup(obstacleGroup);
        RadioButton riverBtn = new RadioButton("河流");
        riverBtn.setToggleGroup(obstacleGroup);
        RadioButton grassBtn = new RadioButton("草丛");
        grassBtn.setToggleGroup(obstacleGroup);
        
        brickBtn.setOnAction(e -> {
            selectedObstacleType = TileType.BRICK;
            currentPaintMode = PaintMode.ADD;
        });
        steelBtn.setOnAction(e -> {
            selectedObstacleType = TileType.STEEL;
            currentPaintMode = PaintMode.ADD;
        });
        riverBtn.setOnAction(e -> {
            selectedObstacleType = TileType.RIVER;
            currentPaintMode = PaintMode.ADD;
        });
        grassBtn.setOnAction(e -> {
            selectedObstacleType = TileType.GRASS;
            currentPaintMode = PaintMode.ADD;
        });
        
        // 橡皮擦按钮
        RadioButton eraserBtn = new RadioButton("橡皮擦");
        eraserBtn.setToggleGroup(obstacleGroup);
        eraserBtn.setOnAction(e -> {
            selectedObstacleType = null; // 橡皮擦不需要选择障碍物类型
            currentPaintMode = PaintMode.ERASER;
        });
        
        HBox obstacleBox = new HBox(5, obstacleLabel, brickBtn, steelBtn, riverBtn, grassBtn, eraserBtn);
        obstacleBox.setAlignment(Pos.CENTER_LEFT);
        
        // 敌人刷新速率选择
        Label spawnRateLabel = new Label("刷新速率：");
        ToggleGroup spawnRateGroup = new ToggleGroup();
        RadioButton fastSpawnBtn = new RadioButton("快(3s)");
        fastSpawnBtn.setToggleGroup(spawnRateGroup);
        RadioButton mediumSpawnBtn = new RadioButton("中(5s)");
        mediumSpawnBtn.setToggleGroup(spawnRateGroup);
        RadioButton slowSpawnBtn = new RadioButton("慢(7s)");
        slowSpawnBtn.setToggleGroup(spawnRateGroup);
        
        // 根据当前值设置选中状态
        if (enemySpawnInterval != null) {
            if (Math.abs(enemySpawnInterval - 3.0) < 0.1) {
                fastSpawnBtn.setSelected(true);
            } else if (Math.abs(enemySpawnInterval - 5.0) < 0.1) {
                mediumSpawnBtn.setSelected(true);
            } else if (Math.abs(enemySpawnInterval - 7.0) < 0.1) {
                slowSpawnBtn.setSelected(true);
            } else {
                mediumSpawnBtn.setSelected(true);
            }
        } else {
            mediumSpawnBtn.setSelected(true);
        }
        
        fastSpawnBtn.setOnAction(e -> enemySpawnInterval = 3.0);
        mediumSpawnBtn.setOnAction(e -> enemySpawnInterval = 5.0);
        slowSpawnBtn.setOnAction(e -> enemySpawnInterval = 7.0);
        
        HBox spawnRateBox = new HBox(5, spawnRateLabel, fastSpawnBtn, mediumSpawnBtn, slowSpawnBtn);
        spawnRateBox.setAlignment(Pos.CENTER_LEFT);
        
        // 精英怪出现频率选择
        Label eliteRateLabel = new Label("精英频率：");
        ToggleGroup eliteRateGroup = new ToggleGroup();
        RadioButton lowEliteBtn = new RadioButton("低(15%)");
        lowEliteBtn.setToggleGroup(eliteRateGroup);
        RadioButton mediumEliteBtn = new RadioButton("中(20%)");
        mediumEliteBtn.setToggleGroup(eliteRateGroup);
        RadioButton highEliteBtn = new RadioButton("高(25%)");
        highEliteBtn.setToggleGroup(eliteRateGroup);
        
        // 根据当前值设置选中状态
        if (eliteSpawnRate != null) {
            if (Math.abs(eliteSpawnRate - 0.15) < 0.01) {
                lowEliteBtn.setSelected(true);
            } else if (Math.abs(eliteSpawnRate - 0.2) < 0.01) {
                mediumEliteBtn.setSelected(true);
            } else if (Math.abs(eliteSpawnRate - 0.25) < 0.01) {
                highEliteBtn.setSelected(true);
            } else {
                mediumEliteBtn.setSelected(true);
            }
        } else {
            mediumEliteBtn.setSelected(true);
        }
        
        lowEliteBtn.setOnAction(e -> eliteSpawnRate = 0.15);
        mediumEliteBtn.setOnAction(e -> eliteSpawnRate = 0.2);
        highEliteBtn.setOnAction(e -> eliteSpawnRate = 0.25);
        
        HBox eliteRateBox = new HBox(5, eliteRateLabel, lowEliteBtn, mediumEliteBtn, highEliteBtn);
        eliteRateBox.setAlignment(Pos.CENTER_LEFT);
        
        // 清除按钮
        Button clearBtn = new Button("清除所有");
        clearBtn.setOnAction(e -> {
            obstacles.clear();
            drawMap();
        });
        
        // 保存按钮
        Button saveBtn = new Button("保存地图");
        saveBtn.setOnAction(e -> saveMap());
        
        // 返回按钮
        Button backBtn = new Button("返回");
        backBtn.setOnAction(e -> {
            if (onBackCallback != null) {
                onBackCallback.run();
            }
        });
        
        toolbar.getChildren().addAll(sizeBox, new Separator(), obstacleBox, new Separator(), 
                                    spawnRateBox, new Separator(), eliteRateBox, new Separator(), 
                                    clearBtn, saveBtn, backBtn);
        
        // 创建地图画布
        mapCanvas = new Canvas(currentMapSize.width, currentMapSize.height);
        gc = mapCanvas.getGraphicsContext2D();
        
        // 鼠标事件处理：支持悬停预览 + 按住拖动连续绘制/擦除
        mapCanvas.setOnMouseMoved(this::handleMouseMove);
        mapCanvas.setOnMousePressed(this::handleMousePressed);
        mapCanvas.setOnMouseDragged(this::handleMouseDragged);
        mapCanvas.setOnMouseReleased(this::handleMouseReleased);
        
        // 绘制初始地图
        drawMap();
        
        // 将地图画布包裹在一个Pane中
        mapContainer = new Pane(mapCanvas);
        mapCanvas.setLayoutX(0);
        mapCanvas.setLayoutY(0);
        // 设置容器大小与画布相同
        mapContainer.setPrefSize(currentMapSize.width, currentMapSize.height);
        mapContainer.setMinSize(currentMapSize.width, currentMapSize.height);
        
        centeredContainer = new StackPane();
        // 防止内容被拉伸，保持画布固定尺寸
        mapContainer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        centeredContainer.getChildren().add(mapContainer);
        StackPane.setAlignment(mapContainer, Pos.TOP_CENTER);
        
        // 将居中容器包裹在ScrollPane中，支持滚动查看大地图
        mapScrollPane = new ScrollPane(centeredContainer);
        mapScrollPane.setFitToWidth(true);
        mapScrollPane.setFitToHeight(true);
        mapScrollPane.setPannable(true); // 允许拖动滚动
        mapScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mapScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mapScrollPane.setStyle("-fx-background-color: #e0e0e0;");
        mapScrollPane.setVvalue(0);
        
        // 布局
        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER); // 内容居中对齐
        centerBox.getChildren().addAll(toolbar, mapScrollPane);
        VBox.setVgrow(mapScrollPane, Priority.ALWAYS); // 让滚动区域占据剩余空间
        
        // 顶部标题区域：显示模式名称和地图编辑器标题（减小字体和padding以节省空间）
        HBox topBox = new HBox(12);
        topBox.setPadding(new Insets(8, 10, 8, 10));
        topBox.setAlignment(Pos.CENTER);
        topBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");
        
        Label modeLabel = new Label("模式：");
        modeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
        
        Label modeNameLabel = new Label(currentMode.displayName);
        modeNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c5aa0;");
        
        Label titleLabel = new Label("地图编辑器");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        // 添加分隔符
        Separator separator = new Separator();
        separator.setOrientation(javafx.geometry.Orientation.VERTICAL);
        
        topBox.getChildren().addAll(modeLabel, modeNameLabel, separator, titleLabel);
        BorderPane.setAlignment(topBox, Pos.CENTER);
        
        root.setTop(topBox);
        root.setCenter(centerBox);
        BorderPane.setAlignment(centerBox, Pos.CENTER); // 中心区域内容居中对齐
        
        // 创建场景（窗口大小根据地图大小调整）
        currentScene = new Scene(root, calculateWindowWidth(), calculateWindowHeight());
        // 标记这是地图编辑器场景，用于在场景切换时保持最大化
        currentScene.getProperties().put("isLevelEditor", true);
        // 打开创建地图时，强制最大化窗口
        if (maximizeCallback != null) {
            maximizeCallback.accept(true);
        }
        
        // 绘制地图（如果是加载的现有地图，会显示出来）
        drawMap();
        
        return currentScene;
    }
    
    private double calculateWindowWidth() {
        // 地图编辑器场景：使用足够大的默认大小，最大化时会自动填充屏幕
        // 使用一个很大的值，确保最大化时不会被场景大小限制
        return 1920; // 使用较大的宽度值
    }
    
    private double calculateWindowHeight() {
        // 地图编辑器场景：使用足够大的默认大小，最大化时会自动填充屏幕
        // 使用一个很大的值，确保最大化时不会被场景大小限制
        return 1080; // 使用较大的高度值，确保最大化时高度也能正确设置
    }
    
    private void updateWindowSize() {
        if (currentScene != null && sceneUpdateCallback != null) {
            // 重新创建场景以更新窗口大小
            currentScene = new Scene(root, calculateWindowWidth(), calculateWindowHeight());
            // 标记这是地图编辑器场景，用于在场景切换时保持最大化
            currentScene.getProperties().put("isLevelEditor", true);
            sceneUpdateCallback.accept(currentScene);
            // 地图编辑器始终最大化
            if (maximizeCallback != null) {
                maximizeCallback.accept(true);
            }
        }
    }
    
    private void updateCanvasSize() {
        mapCanvas.setWidth(currentMapSize.width);
        mapCanvas.setHeight(currentMapSize.height);
        // 更新容器大小
        if (mapContainer != null) {
            mapContainer.setPrefSize(currentMapSize.width, currentMapSize.height);
            mapContainer.setMinSize(currentMapSize.width, currentMapSize.height);
        }
        drawMap();
        // 请求重新布局以更新居中位置
        if (centeredContainer != null) {
            centeredContainer.requestLayout();
        }
    }
    
    private void handleMouseMove(MouseEvent e) {
        drawMap();
        double x = snapToGrid(e.getX());
        double y = snapToGrid(e.getY());
        
        if (currentPaintMode == PaintMode.ERASER) {
            // 橡皮擦模式：显示将要删除的障碍物预览
            // 先检查是否有大块障碍物（草丛或河流，都是2x2格子）
            ObstacleDefinition terrain = findTerrainAt(x, y);
            if (terrain != null) {
                // 显示将要删除的大块障碍物预览
                drawEraserPreview(terrain.x(), terrain.y(), terrain.type());
            } else {
                // 检查是否有小障碍物（砖块或钢墙，1个格子）
                ObstacleDefinition obstacle = findAnyObstacleAt(x, y);
                if (obstacle != null) {
                    // 显示将要删除的小障碍物预览
                    drawEraserPreview(obstacle.x(), obstacle.y(), obstacle.type());
                }
            }
        } else if (selectedObstacleType != null) {
            // 绘制虚幻的障碍物
            // 对于草丛和水路，需要对齐到2x2格子的边界（32像素的倍数）
            if (selectedObstacleType == TileType.RIVER || selectedObstacleType == TileType.GRASS) {
                x = Math.floor(x / TERRAIN_SIZE) * TERRAIN_SIZE;
                y = Math.floor(y / TERRAIN_SIZE) * TERRAIN_SIZE;
            }
            
            if (isValidPosition(x, y)) {
                drawGhostObstacle(x, y, selectedObstacleType);
            }
        }
    }

    private void handleMousePressed(MouseEvent e) {
        // 如果是橡皮擦模式，不需要selectedObstacleType
        if (currentPaintMode != PaintMode.ERASER && selectedObstacleType == null) {
            return;
        }
        
        double x = snapToGrid(e.getX());
        double y = snapToGrid(e.getY());

        // 如果是橡皮擦模式，需要检查点击位置是否有大块障碍物（草丛或河流）
        if (currentPaintMode == PaintMode.ERASER) {
            // 先检查是否有大块障碍物（草丛或河流，都是2x2格子）
            ObstacleDefinition terrain = findTerrainAt(x, y);
            if (terrain != null) {
                // 找到大块障碍物，对齐到网格
                x = terrain.x();
                y = terrain.y();
            }
        } else if (selectedObstacleType == TileType.RIVER || selectedObstacleType == TileType.GRASS) {
            // 对于草丛和水路，需要对齐到2x2格子的边界（32像素的倍数）
            x = Math.floor(x / TERRAIN_SIZE) * TERRAIN_SIZE;
            y = Math.floor(y / TERRAIN_SIZE) * TERRAIN_SIZE;
        }

        if (currentPaintMode != PaintMode.ERASER && !isValidPosition(x, y)) {
            return;
        }

        // 如果不是橡皮擦模式，根据起始格子是否已有同类型障碍物，决定当前是「绘制模式」还是「擦除模式」
        if (currentPaintMode != PaintMode.ERASER) {
            ObstacleDefinition existing = findObstacleAt(selectedObstacleType, x, y);
            currentPaintMode = (existing != null) ? PaintMode.REMOVE : PaintMode.ADD;
        }
        
        isPainting = true;
        lastPaintX = Double.NaN;
        lastPaintY = Double.NaN;

        applyPaintAt(x, y);
    }

    private void handleMouseDragged(MouseEvent e) {
        if (!isPainting) {
            return;
        }
        
        // 如果是橡皮擦模式，不需要selectedObstacleType
        if (currentPaintMode != PaintMode.ERASER && selectedObstacleType == null) {
            return;
        }
        
        double x = snapToGrid(e.getX());
        double y = snapToGrid(e.getY());

        // 如果是橡皮擦模式，需要检查拖动位置是否有大块障碍物（草丛或河流）
        if (currentPaintMode == PaintMode.ERASER) {
            ObstacleDefinition terrain = findTerrainAt(x, y);
            if (terrain != null) {
                x = terrain.x();
                y = terrain.y();
            }
        } else if (selectedObstacleType == TileType.RIVER || selectedObstacleType == TileType.GRASS) {
            // 对于草丛和水路，需要对齐到2x2格子的边界（32像素的倍数）
            x = Math.floor(x / TERRAIN_SIZE) * TERRAIN_SIZE;
            y = Math.floor(y / TERRAIN_SIZE) * TERRAIN_SIZE;
        }

        if (currentPaintMode != PaintMode.ERASER && !isValidPosition(x, y)) {
            return;
        }

        // 避免在同一个格子上重复处理
        if (!Double.isNaN(lastPaintX) && !Double.isNaN(lastPaintY)
                && lastPaintX == x && lastPaintY == y) {
            return;
        }

        applyPaintAt(x, y);
        lastPaintX = x;
        lastPaintY = y;
    }

    private void handleMouseReleased(MouseEvent e) {
        isPainting = false;
        lastPaintX = Double.NaN;
        lastPaintY = Double.NaN;
    }

    /**
     * 在指定格子应用当前绘制模式（添加或删除），用于单击和拖动画刷。
     */
    private void applyPaintAt(double x, double y) {
        if (currentPaintMode == PaintMode.ERASER) {
            // 橡皮擦模式：删除任何类型的障碍物
            // 先检查是否有大块障碍物（草丛或河流，都是2x2格子）
            ObstacleDefinition terrain = findTerrainAt(x, y);
            if (terrain != null) {
                // 删除整个大块障碍物
                obstacles.remove(terrain);
                drawMap();
                return;
            }
            
            // 检查是否有小障碍物（砖块或钢墙，1个格子）
            ObstacleDefinition obstacle = findAnyObstacleAt(x, y);
            if (obstacle != null) {
                obstacles.remove(obstacle);
                drawMap();
            }
            return;
        }
        
        if (currentPaintMode == PaintMode.REMOVE) {
            // 擦除：删除当前格子上的同类型障碍物（如果存在）
            ObstacleDefinition existing = findObstacleAt(selectedObstacleType, x, y);
            if (existing != null) {
                obstacles.remove(existing);
                drawMap();
            }
            return;
        }

        // 绘制：如果该格子已经有同类型障碍物，则跳过
        if (findObstacleAt(selectedObstacleType, x, y) != null) {
            return;
        }

        // 检查是否与现有障碍物重叠（避免和其他类型的格子冲突）
        Obstacle testObstacle = createObstacle(selectedObstacleType, x, y);
        boolean overlaps = false;

        for (ObstacleDefinition existingDef : obstacles) {
            Obstacle existingObstacle = createObstacle(existingDef.type(), existingDef.x(), existingDef.y());
            if (collisionDetector.collide(testObstacle, existingObstacle)) {
                overlaps = true;
                break;
            }
        }

        if (!overlaps) {
            obstacles.add(new ObstacleDefinition(selectedObstacleType, x, y));
            drawMap();
        }
    }

    /**
     * 查找指定类型在某个网格坐标上的障碍物（用于判断是否已存在/删除）。
     */
    private ObstacleDefinition findObstacleAt(TileType type, double x, double y) {
        for (ObstacleDefinition def : obstacles) {
            if (def.type() == type && def.x() == x && def.y() == y) {
                return def;
            }
        }
        return null;
    }
    
    /**
     * 查找在指定位置的大块障碍物（草丛或河流），通过碰撞检测
     */
    private ObstacleDefinition findTerrainAt(double x, double y) {
        // 创建测试点
        Vector2D testPoint = new Vector2D(x, y);
        
        for (ObstacleDefinition def : obstacles) {
            // 检查草丛和河流（都是2x2格子）
            if (def.type() == TileType.GRASS || def.type() == TileType.RIVER) {
                // 创建障碍物进行碰撞检测
                Obstacle obstacle = createObstacle(def.type(), def.x(), def.y());
                // 检查点是否在障碍物内
                if (testPoint.x() >= obstacle.position().x() && 
                    testPoint.x() < obstacle.position().x() + obstacle.size().width() &&
                    testPoint.y() >= obstacle.position().y() && 
                    testPoint.y() < obstacle.position().y() + obstacle.size().height()) {
                    return def;
                }
            }
        }
        return null;
    }
    
    /**
     * 查找在指定位置的小障碍物（砖块或钢墙），用于橡皮擦
     */
    private ObstacleDefinition findAnyObstacleAt(double x, double y) {
        // 创建测试点
        Vector2D testPoint = new Vector2D(x, y);
        
        for (ObstacleDefinition def : obstacles) {
            // 跳过已经处理的大块障碍物（草丛和河流是2x2格子）
            if (def.type() == TileType.GRASS || def.type() == TileType.RIVER) {
                continue;
            }
            
            // 检查点是否在障碍物内（精确匹配位置）
            if (Math.abs(def.x() - x) < 1 && Math.abs(def.y() - y) < 1) {
                return def;
            }
        }
        return null;
    }
    
    private double snapToGrid(double coord) {
        // 对齐到网格（16像素网格，一个小砖块大小）
        return Math.floor(coord / GRID_SIZE) * GRID_SIZE;
    }
    
    private boolean isValidPosition(double x, double y) {
        // 根据障碍物类型确定大小：草丛和水路占2x2格子（32x32），砖块和钢墙占1个格子（16x16）
        double size = (selectedObstacleType == TileType.RIVER || selectedObstacleType == TileType.GRASS) 
                ? TERRAIN_SIZE : GRID_SIZE;
        
        // 检查是否在边界内
        if (x < 0 || y < 0 || x + size > currentMapSize.width || y + size > currentMapSize.height) {
            return false;
        }
        
        // 检查是否与基地重叠（基地在下边界中间）
        double baseSize = 30;
        double baseX = (currentMapSize.width - baseSize) / 2.0;
        double baseY = currentMapSize.height - baseSize;
        
        AABB obstacleAABB = new AABB(x, y, x + size, y + size);
        AABB baseAABB = new AABB(baseX, baseY, baseX + baseSize, baseY + baseSize);
        
        if (obstacleAABB.intersects(baseAABB)) {
            return false;
        }
        
        return true;
    }
    
    private Obstacle createObstacle(TileType type, double x, double y) {
        // 草丛和水路占2x2格子（32x32），砖块和钢墙占1个格子（16x16）
        double sizeValue = (type == TileType.RIVER || type == TileType.GRASS) ? TERRAIN_SIZE : GRID_SIZE;
        Size size = new Size(sizeValue, sizeValue);
        Vector2D pos = new Vector2D(x, y);
        
        switch (type) {
            case BRICK:
                return new BrickWall(pos, size);
            case STEEL:
                return new SteelWall(pos, size);
            case RIVER:
                return new River(pos, size);
            case GRASS:
                return new BrickWall(pos, size); // GRASS不阻挡，但为了碰撞检测使用BrickWall
            default:
                return null;
        }
    }
    
    private void drawGhostObstacle(double x, double y, TileType type) {
        gc.setGlobalAlpha(0.5); // 半透明
        drawObstacle(gc, x, y, type);
        gc.setGlobalAlpha(1.0);
    }
    
    /**
     * 绘制橡皮擦预览效果（红色半透明覆盖层）
     */
    private void drawEraserPreview(double x, double y, TileType type) {
        // 根据障碍物类型确定大小：草丛和水路占2x2格子（32x32），砖块和钢墙占1个格子（16x16）
        double size = (type == TileType.RIVER || type == TileType.GRASS) 
                ? TERRAIN_SIZE : GRID_SIZE;
        
        // 绘制红色半透明覆盖层表示将要删除的区域
        gc.setGlobalAlpha(0.4);
        gc.setFill(Color.RED);
        gc.fillRect(x, y, size, size);
        gc.setGlobalAlpha(1.0);
        
        // 绘制红色边框
        gc.setStroke(Color.DARKRED);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, size, size);
    }
    
    private void drawMap() {
        // 清空画布
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, currentMapSize.width, currentMapSize.height);
        
        // 绘制网格（16像素网格，一个小砖块大小）
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(0.5);
        for (int x = 0; x <= currentMapSize.width; x += GRID_SIZE) {
            gc.strokeLine(x, 0, x, currentMapSize.height);
        }
        for (int y = 0; y <= currentMapSize.height; y += GRID_SIZE) {
            gc.strokeLine(0, y, currentMapSize.width, y);
        }
        
        // 绘制基地（下边界中间）
        double baseSize = 30;
        double baseX = (currentMapSize.width - baseSize) / 2.0;
        double baseY = currentMapSize.height - baseSize;
        gc.setFill(Color.GREEN);
        gc.fillRect(baseX, baseY, baseSize, baseSize);
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(2);
        gc.strokeRect(baseX, baseY, baseSize, baseSize);
        
        // 绘制所有障碍物
        for (ObstacleDefinition obstacle : obstacles) {
            drawObstacle(gc, obstacle.x(), obstacle.y(), obstacle.type());
        }
    }
    
    private void drawObstacle(GraphicsContext gc, double x, double y, TileType type) {
        // 草丛和水路占2x2格子（32x32），砖块和钢墙占1个格子（16x16）
        double size = (type == TileType.RIVER || type == TileType.GRASS) ? TERRAIN_SIZE : GRID_SIZE;
        
        switch (type) {
            case BRICK:
                gc.setFill(Color.rgb(139, 69, 19)); // 棕色
                gc.fillRect(x, y, size, size);
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1);
                gc.strokeRect(x, y, size, size);
                break;
            case STEEL:
                gc.setFill(Color.GRAY);
                gc.fillRect(x, y, size, size);
                gc.setStroke(Color.DARKGRAY);
                gc.setLineWidth(2);
                gc.strokeRect(x, y, size, size);
                break;
            case RIVER:
                gc.setFill(Color.BLUE);
                gc.fillRect(x, y, size, size);
                gc.setStroke(Color.DARKBLUE);
                gc.setLineWidth(1);
                gc.strokeRect(x, y, size, size);
                break;
            case GRASS:
                gc.setFill(Color.GREEN);
                gc.fillRect(x, y, size, size);
                gc.setStroke(Color.DARKGREEN);
                gc.setLineWidth(1);
                gc.strokeRect(x, y, size, size);
                break;
        }
    }
    
    public LevelDefinition createLevelDefinition(String id, String name) {
        double baseSize = 30;
        double baseX = (currentMapSize.width - baseSize) / 2.0;
        double baseY = currentMapSize.height - baseSize;
        BaseDefinition base = new BaseDefinition(baseX, baseY);
        
        // 根据游戏模式设置时间限制
        Integer timeLimit = null;
        if (currentMode == GameMode.TIMED) {
            // 限时模式，可以根据需要设置时间限制
            timeLimit = 300; // 默认5分钟
        }
        
        return new LevelDefinition(id, name, currentMapSize.width, currentMapSize.height, base, 
                                  new ArrayList<>(obstacles), timeLimit, enemySpawnInterval, eliteSpawnRate);
    }
    
    /**
     * 加载现有地图
     */
    private void loadLevel(LevelDefinition level) {
        currentLevelId = level.id();
        currentLevelName = level.name();
        
        // 根据地图大小设置MapSize
        if (level.width() == 600 && level.height() == 450) {
            currentMapSize = MapSize.SMALL;
        } else if (level.width() == 960 && level.height() == 690) {
            currentMapSize = MapSize.LARGE;
        } else {
            currentMapSize = MapSize.MEDIUM;
        }
        
        // 根据关卡ID判断游戏模式
        String id = level.id();
        if (id != null) {
            if (id.startsWith("classic-") || id.startsWith("classic_level-")) {
                currentMode = GameMode.CLASSIC;
            } else if (id.startsWith("endless-")) {
                currentMode = GameMode.ENDLESS;
            } else if (id.startsWith("timed-") || id.startsWith("timed_challenge")) {
                currentMode = GameMode.TIMED;
            } else {
                currentMode = GameMode.CUSTOM;
            }
        }
        
        // 加载障碍物
        obstacles.clear();
        obstacles.addAll(level.obstacles());
        
        // 加载敌人刷新速率和精英怪出现频率
        enemySpawnInterval = level.enemySpawnInterval();
        eliteSpawnRate = level.eliteSpawnRate();
        
        // 如果值为null，使用默认值
        if (enemySpawnInterval == null) {
            enemySpawnInterval = 5.0;
        }
        if (eliteSpawnRate == null) {
            eliteSpawnRate = 0.2;
        }
    }
    
    /**
     * 保存地图到文件
     */
    private void saveMap() {
        // 如果是修改现有地图，直接保存；否则弹出对话框让用户输入地图名称
        if (currentLevelId != null) {
            // 修改现有地图，直接保存（保留原ID和名称，覆盖原文件）
            try {
                LevelDefinition levelDef = createLevelDefinition(currentLevelId, currentLevelName);
                
                Path levelsDir = ResourceLocator.levelsFileSystemPath();
                
                // 确保目录存在
                if (!Files.exists(levelsDir)) {
                    Files.createDirectories(levelsDir);
                }
                
                // 查找原始文件名：遍历所有JSON文件，找到id匹配的文件
                String originalFileName = null;
                try (Stream<Path> paths = Files.list(levelsDir)) {
                    LevelLoader loader = new LevelLoader();
                    originalFileName = paths
                            .filter(Files::isRegularFile)
                            .filter(path -> path.getFileName().toString().endsWith(".json"))
                            .filter(path -> {
                                try {
                                    LevelDefinition def = loader.load(path);
                                    return currentLevelId.equals(def.id());
                                } catch (Exception e) {
                                    return false;
                                }
                            })
                            .map(path -> path.getFileName().toString())
                            .findFirst()
                            .orElse(null);
                }
                
                // 如果找到原始文件，使用原始文件名；否则使用id作为文件名
                String fileName = (originalFileName != null) ? originalFileName : (currentLevelId + ".json");
                Path filePath = levelsDir.resolve(fileName);
                
                // 保存到原文件（如果文件已存在，writeValue会自动覆盖）
                ObjectMapper mapper = new ObjectMapper();
                mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), levelDef);
                
                showAlert("成功", "地图已成功保存（覆盖原文件）！\n文件位置: " + filePath);
                
                // 触发保存回调（用于刷新选关界面）
                if (onSaveCallback != null) {
                    onSaveCallback.run();
                }
            } catch (Exception e) {
                showAlert("错误", "保存地图失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // 新建地图，弹出对话框让用户输入地图名称
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("保存地图");
            dialog.setHeaderText("请输入地图名称");
            dialog.setContentText("地图名称:");
            
            dialog.showAndWait().ifPresent(mapName -> {
                if (mapName == null || mapName.trim().isEmpty()) {
                    showAlert("错误", "地图名称不能为空！");
                    return;
                }
                
                // 生成唯一ID（基于名称和时间戳）
                String id = generateId(mapName.trim());
                
                // 创建LevelDefinition
                LevelDefinition levelDef = createLevelDefinition(id, mapName.trim());
                
                // 保存到文件
                try {
                    Path levelsDir = ResourceLocator.levelsFileSystemPath();
                    // 确保目录存在
                    if (!Files.exists(levelsDir)) {
                        Files.createDirectories(levelsDir);
                    }
                    
                    // 生成文件名（基于ID）
                    String fileName = id + ".json";
                    Path filePath = levelsDir.resolve(fileName);
                    
                    // 使用Jackson序列化为JSON
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), levelDef);
                    
                    showAlert("成功", "地图 \"" + mapName.trim() + "\" 已成功保存！\n文件位置: " + filePath);
                    
                    // 触发保存回调（用于刷新选关界面）
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                } catch (Exception e) {
                    showAlert("错误", "保存地图失败: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }
    
    /**
     * 生成唯一ID
     */
    private String generateId(String name) {
        // 将名称转换为小写，替换空格为连字符，移除特殊字符
        String baseId = name.toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "");
        
        // 添加时间戳确保唯一性
        long timestamp = System.currentTimeMillis();
        return baseId + "-" + timestamp;
    }
    
    /**
     * 显示提示对话框
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(
                title.equals("错误") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

