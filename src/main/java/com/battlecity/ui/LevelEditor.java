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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.battlecity.util.ResourceLocator;

/**
 * 地图编辑器
 */
public class LevelEditor {
    
    public enum MapSize {
        SMALL(600, 450, "小地图"),   // 40x30格子
        MEDIUM(750, 600, "中地图"),  // 53x40格子
        LARGE(960, 690, "大地图");   // 64x48格子
        
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
    private Canvas mapCanvas;
    private GraphicsContext gc;
    private CollisionDetector collisionDetector = new CollisionDetector();
    private ScrollPane mapScrollPane; // 地图滚动面板
    private Pane mapContainer; // 地图容器
    private StackPane centeredContainer; // 居中容器，用于在ScrollPane中居中显示地图
    private static final double GRID_SIZE = 15; // 格子大小（砖块大小15x15）
    private static final double TERRAIN_SIZE = 60; // 地形大小（草丛和水路占4个格子：4x15=60）

    // 拖动绘制/擦除相关状态
    private boolean isPainting = false;
    private PaintMode currentPaintMode = PaintMode.ADD;
    private double lastPaintX = Double.NaN;
    private double lastPaintY = Double.NaN;

    private enum PaintMode {
        ADD,
        REMOVE
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
        this.onBackCallback = onBack;
        this.sceneUpdateCallback = sceneUpdateCallback;
        this.currentMode = mode != null ? mode : GameMode.CUSTOM;
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
        mediumBtn.setSelected(true);
        RadioButton largeBtn = new RadioButton("大");
        largeBtn.setToggleGroup(sizeGroup);
        
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
        
        brickBtn.setOnAction(e -> selectedObstacleType = TileType.BRICK);
        steelBtn.setOnAction(e -> selectedObstacleType = TileType.STEEL);
        riverBtn.setOnAction(e -> selectedObstacleType = TileType.RIVER);
        grassBtn.setOnAction(e -> selectedObstacleType = TileType.GRASS);
        
        HBox obstacleBox = new HBox(5, obstacleLabel, brickBtn, steelBtn, riverBtn, grassBtn);
        obstacleBox.setAlignment(Pos.CENTER_LEFT);
        
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
        
        toolbar.getChildren().addAll(sizeBox, new Separator(), obstacleBox, new Separator(), clearBtn, saveBtn, backBtn);
        
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
        if (selectedObstacleType != null) {
            // 绘制虚幻的障碍物
            double x = snapToGrid(e.getX());
            double y = snapToGrid(e.getY());
            
            // 对于草丛和水路，需要对齐到4个格子的边界（60像素的倍数）
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
        if (selectedObstacleType == null) {
            return;
        }
        double x = snapToGrid(e.getX());
        double y = snapToGrid(e.getY());

        // 对于草丛和水路，需要对齐到4个格子的边界（60像素的倍数）
        if (selectedObstacleType == TileType.RIVER || selectedObstacleType == TileType.GRASS) {
            x = Math.floor(x / TERRAIN_SIZE) * TERRAIN_SIZE;
            y = Math.floor(y / TERRAIN_SIZE) * TERRAIN_SIZE;
        }

        if (!isValidPosition(x, y)) {
            return;
        }

        // 根据起始格子是否已有同类型障碍物，决定当前是「绘制模式」还是「擦除模式」
        ObstacleDefinition existing = findObstacleAt(selectedObstacleType, x, y);
        currentPaintMode = (existing != null) ? PaintMode.REMOVE : PaintMode.ADD;
        isPainting = true;
        lastPaintX = Double.NaN;
        lastPaintY = Double.NaN;

        applyPaintAt(x, y);
    }

    private void handleMouseDragged(MouseEvent e) {
        if (!isPainting || selectedObstacleType == null) {
            return;
        }
        double x = snapToGrid(e.getX());
        double y = snapToGrid(e.getY());

        if (selectedObstacleType == TileType.RIVER || selectedObstacleType == TileType.GRASS) {
            x = Math.floor(x / TERRAIN_SIZE) * TERRAIN_SIZE;
            y = Math.floor(y / TERRAIN_SIZE) * TERRAIN_SIZE;
        }

        if (!isValidPosition(x, y)) {
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
    
    private double snapToGrid(double coord) {
        // 对齐到网格（15像素网格，砖块大小）
        return Math.floor(coord / GRID_SIZE) * GRID_SIZE;
    }
    
    private boolean isValidPosition(double x, double y) {
        // 根据障碍物类型确定大小
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
        // 草丛和水路占4个格子（60x60），其他占1个格子（15x15）
        double sizeValue = (type == TileType.RIVER || type == TileType.GRASS) ? TERRAIN_SIZE : GRID_SIZE;
        Size size = new Size(sizeValue, sizeValue);
        Vector2D pos = new Vector2D(x, y);
        
        return switch (type) {
            case BRICK -> new BrickWall(pos, size);
            case STEEL -> new SteelWall(pos, size);
            case RIVER -> new River(pos, size);
            case GRASS -> new BrickWall(pos, size); // GRASS不阻挡，但为了碰撞检测使用BrickWall
            default -> null;
        };
    }
    
    private void drawGhostObstacle(double x, double y, TileType type) {
        gc.setGlobalAlpha(0.5); // 半透明
        drawObstacle(gc, x, y, type);
        gc.setGlobalAlpha(1.0);
    }
    
    private void drawMap() {
        // 清空画布
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, currentMapSize.width, currentMapSize.height);
        
        // 绘制网格（15像素网格，砖块大小）
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
        // 草丛和水路占4个格子（60x60），其他占1个格子（15x15）
        double size = (type == TileType.RIVER || type == TileType.GRASS) ? TERRAIN_SIZE : GRID_SIZE;
        
        switch (type) {
            case BRICK -> {
                gc.setFill(Color.rgb(139, 69, 19)); // 棕色
                gc.fillRect(x, y, size, size);
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1);
                gc.strokeRect(x, y, size, size);
            }
            case STEEL -> {
                gc.setFill(Color.GRAY);
                gc.fillRect(x, y, size, size);
                gc.setStroke(Color.DARKGRAY);
                gc.setLineWidth(2);
                gc.strokeRect(x, y, size, size);
            }
            case RIVER -> {
                gc.setFill(Color.BLUE);
                gc.fillRect(x, y, size, size);
                gc.setStroke(Color.DARKBLUE);
                gc.setLineWidth(1);
                gc.strokeRect(x, y, size, size);
            }
            case GRASS -> {
                gc.setFill(Color.GREEN);
                gc.fillRect(x, y, size, size);
                gc.setStroke(Color.DARKGREEN);
                gc.setLineWidth(1);
                gc.strokeRect(x, y, size, size);
            }
        }
    }
    
    public LevelDefinition createLevelDefinition(String id, String name) {
        double baseSize = 30;
        double baseX = (currentMapSize.width - baseSize) / 2.0;
        double baseY = currentMapSize.height - baseSize;
        BaseDefinition base = new BaseDefinition(baseX, baseY);
        
        return new LevelDefinition(id, name, currentMapSize.width, currentMapSize.height, base, new ArrayList<>(obstacles));
    }
    
    /**
     * 保存地图到文件
     */
    private void saveMap() {
        // 弹出对话框让用户输入地图名称
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
                Path levelsDir = ResourceLocator.levelsDirectory();
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

