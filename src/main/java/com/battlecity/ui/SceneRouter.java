package com.battlecity.ui;

import com.battlecity.controller.GameController;
import com.battlecity.controller.SceneRouterFacade;
import com.battlecity.engine.GameContext;
import com.battlecity.engine.GameEngine;
import com.battlecity.map.LevelDefinition;
import com.battlecity.model.GameWorld;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.util.function.Consumer;
import java.util.List;
import java.util.ArrayList;
import com.battlecity.map.LevelRepositoryFactory;
import com.battlecity.map.LevelLoader;
import com.battlecity.util.ResourceLocator;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 简易场景路由，后续可扩展不同 Scene。
 */
public class SceneRouter implements SceneRouterFacade {

    private final GameContext context;
    private GameEngine currentEngine;
    private Scene currentGameScene;
    private Consumer<Scene> sceneChangeCallback;
    private Stage primaryStage;

    public SceneRouter(GameContext context) {
        this.context = context;
    }
    
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @Override
    public Scene buildMainMenuScene(GameEngine engine) {
        this.currentEngine = engine;
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(24));

        Label title = new Label("Battle City");
        title.getStyleClass().add("title");
        
        // 创建标题区域，包含两侧坦克和中间标题
        HBox titleContainer = new HBox(20);
        titleContainer.setAlignment(Pos.CENTER);
        
        // 创建左侧蓝色坦克Canvas
        Canvas leftTankCanvas = new Canvas(80, 100); // 宽度比标题略宽
        GraphicsContext leftGc = leftTankCanvas.getGraphicsContext2D();
        drawMenuTank(leftGc, 40, 50, Color.BLUE, 0, 1); // 炮口朝下（方向向量(0,1)）
        
        // 创建右侧红色坦克Canvas
        Canvas rightTankCanvas = new Canvas(80, 100); // 宽度比标题略宽
        GraphicsContext rightGc = rightTankCanvas.getGraphicsContext2D();
        drawMenuTank(rightGc, 40, 50, Color.RED, 0, 1); // 炮口朝下（方向向量(0,1)）
        
        // 将坦克和标题添加到标题容器
        titleContainer.getChildren().addAll(leftTankCanvas, title, rightTankCanvas);

        Button classicBtn = new Button("经典模式");
        classicBtn.setOnAction(e -> {
            try {
                engine.startClassicMode();
                switchToGameScene(engine);
            } catch (Exception ex) {
                System.err.println("启动经典模式失败: " + ex.getMessage());
                ex.printStackTrace();
                // 显示错误提示
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText("无法启动经典模式");
                alert.setContentText("错误信息: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        Button endlessBtn = new Button("无尽试炼");
        endlessBtn.setOnAction(e -> {
            try {
                engine.startEndlessMode();
                switchToGameScene(engine);
            } catch (Exception ex) {
                System.err.println("启动无尽试炼失败: " + ex.getMessage());
                ex.printStackTrace();
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText("无法启动无尽试炼");
                alert.setContentText("错误信息: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        Button timedBtn = new Button("限时挑战");
        timedBtn.setOnAction(e -> {
            try {
                engine.startTimedMode();
                switchToGameScene(engine);
            } catch (Exception ex) {
                System.err.println("启动限时挑战失败: " + ex.getMessage());
                ex.printStackTrace();
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText("无法启动限时挑战");
                alert.setContentText("错误信息: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        Button freeSelectBtn = new Button("自由选关");
        freeSelectBtn.setOnAction(e -> {
            Scene selectScene = buildLevelSelectScene(engine);
            if (sceneChangeCallback != null) {
                sceneChangeCallback.accept(selectScene);
            }
        });
        
        Button exitBtn = new Button("退出游戏");
        exitBtn.setOnAction(e -> {
            // 退出游戏
            System.exit(0);
        });

        VBox menu = new VBox(20, titleContainer, classicBtn, endlessBtn, timedBtn, freeSelectBtn, exitBtn);
        menu.setAlignment(Pos.CENTER);
        root.setCenter(menu);

        Scene scene = new Scene(root, context.config().virtualWidth(), context.config().virtualHeight());
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        return scene;
    }
    
    /**
     * 在主菜单绘制坦克图案
     * @param gc 图形上下文
     * @param centerX 坦克中心点X坐标
     * @param centerY 坦克中心点Y坐标
     * @param color 坦克颜色
     * @param dirX 炮口方向X分量
     * @param dirY 炮口方向Y分量
     */
    private void drawMenuTank(GraphicsContext gc, double centerX, double centerY, Color color, double dirX, double dirY) {
        double tankSize = 50; // 坦克主体大小
        double x = centerX - tankSize / 2;
        double y = centerY - tankSize / 2;
        
        // 确定坦克方向（上、下、左、右）
        boolean isVertical = Math.abs(dirY) > Math.abs(dirX);
        
        // 绘制坦克主体（带圆角效果）
        gc.setFill(color);
        gc.fillRoundRect(x + 2, y + 2, tankSize - 4, tankSize - 4, 4, 4);
        
        // 绘制坦克边框
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x + 2, y + 2, tankSize - 4, tankSize - 4, 4, 4);
        
        // 绘制履带（上下两条）
        gc.setFill(Color.DARKGRAY);
        if (isVertical) {
            // 垂直方向：左右履带
            gc.fillRect(x, y + 2, 3, tankSize - 4);
            gc.fillRect(x + tankSize - 3, y + 2, 3, tankSize - 4);
            // 履带纹理
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(0.5);
            for (int i = 0; i < 3; i++) {
                double ty = y + 4 + i * (tankSize - 8) / 2;
                gc.strokeLine(x, ty, x + 3, ty);
                gc.strokeLine(x + tankSize - 3, ty, x + tankSize, ty);
            }
        } else {
            // 水平方向：上下履带
            gc.fillRect(x + 2, y, tankSize - 4, 3);
            gc.fillRect(x + 2, y + tankSize - 3, tankSize - 4, 3);
            // 履带纹理
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(0.5);
            for (int i = 0; i < 3; i++) {
                double tx = x + 4 + i * (tankSize - 8) / 2;
                gc.strokeLine(tx, y, tx, y + 3);
                gc.strokeLine(tx, y + tankSize - 3, tx, y + tankSize);
            }
        }
        
        // 绘制炮塔（中心圆形）
        gc.setFill(Color.rgb(
            (int)(color.getRed() * 255 * 0.8),
            (int)(color.getGreen() * 255 * 0.8),
            (int)(color.getBlue() * 255 * 0.8)
        ));
        double turretSize = Math.min(tankSize, tankSize) * 0.5;
        gc.fillOval(centerX - turretSize / 2, centerY - turretSize / 2, turretSize, turretSize);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeOval(centerX - turretSize / 2, centerY - turretSize / 2, turretSize, turretSize);
        
        // 绘制炮管（根据方向）
        double cannonLength = Math.max(tankSize, tankSize) * 0.7;
        double cannonWidth = 4;
        double cannonEndX = centerX + dirX * cannonLength;
        double cannonEndY = centerY + dirY * cannonLength;
        
        // 计算炮管的角度
        double angle = Math.atan2(dirY, dirX);
        
        // 绘制炮管（矩形，带旋转效果）
        gc.save();
        gc.translate(centerX, centerY);
        gc.rotate(Math.toDegrees(angle));
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(-cannonWidth / 2, -cannonWidth / 2, cannonLength, cannonWidth);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(-cannonWidth / 2, -cannonWidth / 2, cannonLength, cannonWidth);
        gc.restore();
        
        // 绘制炮管口（圆形）
        gc.setFill(Color.BLACK);
        gc.fillOval(cannonEndX - 3, cannonEndY - 3, 6, 6);
    }

    private void switchToGameScene(GameEngine engine) {
        GameWorld world = engine.getWorld();
        if (world == null) {
            System.err.println("错误: 游戏世界未初始化，无法切换到游戏场景");
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText("无法进入游戏");
            alert.setContentText("游戏世界未初始化，请检查关卡文件是否正确加载。");
            alert.showAndWait();
            return;
        }

        BorderPane root = new BorderPane();
        // 设置BorderPane可以接收键盘焦点
        root.setFocusTraversable(true);
        
        // 获取地图的实际大小
        double mapWidth = world.levelDefinition().width();
        double mapHeight = world.levelDefinition().height();
        
        // 创建游戏视图，使用地图的实际大小
        GameView gameView = new GameView(mapWidth, mapHeight);
        gameView.bindWorld(world);
        
        // Canvas的大小在构造函数中已设置，使用setWidth/setHeight确保大小正确
        gameView.setWidth(mapWidth);
        gameView.setHeight(mapHeight);
        
        root.setCenter(gameView);
        
        // 创建控制台UI（显示在底部，覆盖基地区域）
        VBox consoleContainer = new VBox();
        consoleContainer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9); -fx-border-color: #00ff00; -fx-border-width: 2px 0 0 0;");
        consoleContainer.setVisible(false);
        consoleContainer.setManaged(false);
        consoleContainer.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        consoleContainer.setPadding(new Insets(15));
        consoleContainer.setSpacing(10);
        
        VBox consoleBox = new VBox(10);
        consoleBox.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        consoleBox.setMaxWidth(Double.MAX_VALUE);
        
        Label consoleTitle = new Label("控制台");
        consoleTitle.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label promptLabel = new Label(">");
        promptLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 14px; -fx-font-family: 'Courier New', monospace;");
        
        TextField consoleInput = new TextField();
        consoleInput.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: #00ff00; -fx-font-size: 14px; -fx-font-family: 'Courier New', monospace; -fx-border-color: #00ff00; -fx-border-width: 1px;");
        consoleInput.setPromptText("输入命令... (kill, god)");
        HBox.setHgrow(consoleInput, Priority.ALWAYS);
        
        inputBox.getChildren().addAll(promptLabel, consoleInput);
        
        Label consoleOutput = new Label();
        consoleOutput.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 12px; -fx-font-family: 'Courier New', monospace;");
        consoleOutput.setWrapText(true);
        consoleOutput.setMaxWidth(Double.MAX_VALUE);
        consoleOutput.setPrefHeight(40);
        
        Label consoleHint = new Label("按 ESC 关闭控制台 | 按 Enter 执行命令");
        consoleHint.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
        
        consoleBox.getChildren().addAll(consoleTitle, inputBox, consoleOutput, consoleHint);
        consoleContainer.getChildren().add(consoleBox);
        
        // 将控制台添加到根节点的底部（覆盖基地区域）
        root.setBottom(consoleContainer);
        
        // 控制台状态
        final boolean[] consoleOpen = {false};

        // 创建HUD
        HBox hud = new HBox(20);
        hud.setPadding(new Insets(10));
        hud.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        // 固定HUD高度，确保场景高度计算正确
        hud.setMinHeight(40);
        hud.setPrefHeight(40);
        hud.setMaxHeight(40);

        Label healthLabel = new Label();
        healthLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        Label enemiesLabel = new Label();
        enemiesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        // 剩余敌人数量标签（限时模式显示）
        Label remainingEnemiesLabel = new Label();
        remainingEnemiesLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        // 限时模式的时间显示
        Label timeLabel = new Label();
        timeLabel.setStyle("-fx-text-fill: cyan; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label baseHealthLabel = new Label();
        baseHealthLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: center-right;");
        
        // 剩余敌人标签（所有模式都显示，放在最右边）
        Label remainingEnemiesRightLabel = new Label();
        remainingEnemiesRightLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 14px; -fx-font-weight: bold;");

        // 将baseHealthLabel放置在HBox的右侧，占据剩余空间
        HBox.setHgrow(baseHealthLabel, Priority.ALWAYS);
        
        // 布局顺序：左边是基础信息，中间是baseHealthLabel（占据剩余空间），右边是剩余敌人
        hud.getChildren().addAll(healthLabel, enemiesLabel, remainingEnemiesLabel, timeLabel, baseHealthLabel, remainingEnemiesRightLabel);
        root.setTop(hud);

        // 实时更新HUD并检查游戏失败/胜利
        final AnimationTimer[] hudTimerRef = new AnimationTimer[1];
        hudTimerRef[0] = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (world != null && world.playerTank() != null) {
                    healthLabel.setText("生命值: " + world.playerTank().health());
                    baseHealthLabel.setText("基地生命: " + world.base().health());
                    
                    // 获取游戏状态
                    com.battlecity.engine.state.GameState currentState = engine.getStateManager().current();
                    com.battlecity.engine.state.GameModeType gameMode = currentState != null ? currentState.mode() : com.battlecity.engine.state.GameModeType.CLASSIC;
                    double elapsedSeconds = currentState != null ? currentState.elapsedSeconds() : 0.0;
                    
                    // 计算剩余敌人数量
                    int remaining = world.getRemainingEnemies();
                    
                    // 根据游戏模式显示不同的UI
                    if (gameMode == com.battlecity.engine.state.GameModeType.ENDLESS) {
                        // 无尽模式：显示得分，隐藏敌人数量
                        enemiesLabel.setText("得分: " + world.getScore());
                        remainingEnemiesRightLabel.setVisible(false);
                    } else {
                        // 经典模式和限时模式：显示敌人数量
                        enemiesLabel.setText("敌人: " + world.enemyTanks().size());
                        // 显示剩余敌人数量（所有模式都显示，放在右边）
                        remainingEnemiesRightLabel.setText("剩余敌人: " + remaining);
                        remainingEnemiesRightLabel.setVisible(true);
                    }
                    
                    // 限时模式：显示时间，不重复显示剩余敌人数量
                    if (gameMode == com.battlecity.engine.state.GameModeType.TIMED) {
                        // 显示剩余时间
                        Integer timeLimit = world.levelDefinition().timeLimitSeconds();
                        if (timeLimit != null) {
                            double remainingTime = Math.max(0, timeLimit - elapsedSeconds);
                            int minutes = (int)(remainingTime / 60);
                            int seconds = (int)(remainingTime % 60);
                            timeLabel.setText(String.format("剩余时间: %02d:%02d", minutes, seconds));
                            timeLabel.setVisible(true);
                            
                            // 时间不足时变红
                            if (remainingTime < 60) {
                                timeLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px; -fx-font-weight: bold;");
                            } else {
                                timeLabel.setStyle("-fx-text-fill: cyan; -fx-font-size: 14px; -fx-font-weight: bold;");
                            }
                        } else {
                            timeLabel.setVisible(false);
                        }
                    } else {
                        // 非限时模式，隐藏时间显示
                        timeLabel.setVisible(false);
                    }
                    
                    // 隐藏左侧的剩余敌人标签，只保留右侧的一个
                    remainingEnemiesLabel.setVisible(false);
                    
                    // 检查游戏是否失败
                    if (world.isGameOver()) {
                        hudTimerRef[0].stop();
                        engine.pause();
                        showGameOverScene(engine);
                        return;
                    }
                    
                    // 检查游戏是否胜利
                    GameController controller = getController(engine);
                    if (controller != null && controller.isVictory(gameMode, elapsedSeconds)) {
                        hudTimerRef[0].stop();
                        engine.pause();
                        showVictoryScene(engine, gameMode, elapsedSeconds);
                        return;
                    }
                }
            }
        };
        hudTimerRef[0].start();

        // 创建场景并绑定输入
        // 场景高度 = 地图高度 + HUD高度，确保下边界可见
        // 使用地图的实际大小（已在上面获取）
        double hudHeight = 40; // HUD固定高度
        double sceneWidth = mapWidth;
        double sceneHeight = mapHeight + hudHeight;
        
        // 设置根节点的首选大小，确保场景大小正确
        root.setPrefSize(sceneWidth, sceneHeight);
        root.setMinSize(sceneWidth, sceneHeight);
        root.setMaxSize(sceneWidth, sceneHeight);
        
        // 确保BorderPane不会自动调整大小，保持固定大小
        // 移除重复设置，使用固定大小
        
        Scene gameScene = new Scene(root, sceneWidth, sceneHeight);
        
        // 在场景属性中存储地图大小信息，供窗口调整使用
        gameScene.getProperties().put("mapWidth", mapWidth);
        gameScene.getProperties().put("mapHeight", mapHeight);
        gameScene.getProperties().put("sceneWidth", sceneWidth);
        gameScene.getProperties().put("sceneHeight", sceneHeight);
        
        gameScene.setOnKeyPressed(e -> {
            // 控制台功能：波浪键（~）打开/关闭控制台
            if (e.getCode() == KeyCode.BACK_QUOTE || e.getCode() == KeyCode.QUOTE) {
                // 切换控制台显示状态
                consoleOpen[0] = !consoleOpen[0];
                consoleContainer.setVisible(consoleOpen[0]);
                consoleContainer.setManaged(consoleOpen[0]);
                
                if (consoleOpen[0]) {
                    // 打开控制台时，暂停游戏
                    if (engine != null && !engine.isPaused()) {
                        engine.pause();
                    }
                    // 请求焦点到输入框
                    consoleInput.requestFocus();
                } else {
                    // 关闭控制台时，清空输入和输出
                    consoleInput.clear();
                    consoleOutput.setText("");
                    // 恢复游戏
                    if (engine != null && engine.isPaused()) {
                        engine.resume();
                    }
                    // 恢复游戏输入焦点
                    root.requestFocus();
                }
                e.consume(); // 消耗事件，防止触发其他处理
                return;
            }
            
            // 如果控制台打开，处理控制台相关按键
            if (consoleOpen[0]) {
                if (e.getCode() == KeyCode.ESCAPE) {
                    // ESC关闭控制台
                    consoleOpen[0] = false;
                    consoleContainer.setVisible(false);
                    consoleContainer.setManaged(false);
                    consoleInput.clear();
                    consoleOutput.setText("");
                    // 恢复游戏
                    if (engine != null && engine.isPaused()) {
                        engine.resume();
                    }
                    root.requestFocus();
                    e.consume();
                    return;
                } else if (e.getCode() == KeyCode.ENTER) {
                    // Enter执行命令
                    String command = consoleInput.getText().trim();
                    if (!command.isEmpty()) {
                        String result = world.executeConsoleCommand(command);
                        consoleOutput.setText(result.isEmpty() ? "命令已执行" : result);
                        consoleInput.clear();
                    }
                    e.consume();
                    return;
                }
                // 控制台打开时，不处理其他按键（让TextField处理）
                return;
            }
            
            // 暂停功能（P键或ESC）：切换暂停/继续状态
            if (e.getCode() == KeyCode.P || e.getCode() == KeyCode.ESCAPE) {
                if (engine != null) {
                    if (engine.isPaused()) {
                        engine.resume();
                    } else {
                        engine.pause();
                        showPauseScene(engine);
                    }
                }
            } else {
                // 只有在未暂停时才处理其他按键输入
                if (engine == null || !engine.isPaused()) {
                    GameController controller = getController(engine);
                    if (controller != null) {
                        controller.onKeyPressed(e.getCode());
                    }
                }
            }
        });
        gameScene.setOnKeyReleased(e -> {
            // 如果控制台打开，不处理按键释放事件
            if (consoleOpen[0]) {
                return;
            }
            
            GameController controller = getController(engine);
            if (controller != null) {
                controller.onKeyReleased(e.getCode());
            }
        });

        // 鼠标点击时请求焦点，确保能接收键盘输入
        root.setOnMouseClicked(e -> root.requestFocus());

        this.currentGameScene = gameScene;
        
        // 触发场景切换回调（BattleCityApplication会处理窗口大小和最大化）
        if (sceneChangeCallback != null) {
            sceneChangeCallback.accept(gameScene);
        }
    }

    public void setOnSceneChange(Consumer<Scene> callback) {
        this.sceneChangeCallback = callback;
    }

    private GameController controller;

    public void setController(GameController controller) {
        this.controller = controller;
    }

    private GameController getController(GameEngine engine) {
        return controller;
    }

    public Scene getCurrentGameScene() {
        return currentGameScene;
    }
    
    /**
     * 显示游戏暂停界面
     */
    private void showPauseScene(GameEngine engine) {
        GameWorld world = engine.getWorld();
        if (world == null) {
            return;
        }
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        
        Label pauseLabel = new Label("游戏暂停");
        pauseLabel.setStyle("-fx-text-fill: white; -fx-font-size: 48px; -fx-font-weight: bold;");
        
        Button resumeBtn = new Button("继续游戏");
        resumeBtn.getStyleClass().add("button"); // 添加与主界面相同的按钮样式
        resumeBtn.setOnAction(e -> {
            if (engine != null) {
                engine.resume();
            }
            // 切换回原来的游戏场景
            if (currentGameScene != null && sceneChangeCallback != null) {
                sceneChangeCallback.accept(currentGameScene);
            }
        });
        
        Button returnBtn = new Button("返回主界面");
        returnBtn.getStyleClass().add("button"); // 添加与主界面相同的按钮样式
        returnBtn.setOnAction(e -> {
            if (engine != null) {
                engine.shutdown();
            }
            Scene mainMenuScene = buildMainMenuScene(engine);
            if (sceneChangeCallback != null) {
                sceneChangeCallback.accept(mainMenuScene);
            }
        });
        
        content.getChildren().addAll(pauseLabel, resumeBtn, returnBtn);
        root.setCenter(content);
        
        // 创建暂停场景，使用地图的实际大小
        double mapWidth = world.levelDefinition().width();
        double mapHeight = world.levelDefinition().height();
        double hudHeight = 40; // HUD固定高度
        Scene pauseScene = new Scene(root, mapWidth, mapHeight + hudHeight);
        
        // 添加CSS样式表，确保按钮应用相同的样式
        pauseScene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        
        // 暂停界面也支持按P键或ESC键继续游戏
        pauseScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.P || e.getCode() == KeyCode.ESCAPE) {
                if (engine != null) {
                    engine.resume();
                }
                // 切换回原来的游戏场景
                if (currentGameScene != null && sceneChangeCallback != null) {
                    sceneChangeCallback.accept(currentGameScene);
                }
            }
        });
        
        // 触发场景切换回调
        if (sceneChangeCallback != null) {
            sceneChangeCallback.accept(pauseScene);
        }
    }
    
    /**
     * 显示游戏胜利界面
     */
    private void showVictoryScene(GameEngine engine, com.battlecity.engine.state.GameModeType gameMode, double elapsedSeconds) {
        // 播放游戏胜利音效
        com.battlecity.audio.AudioManager.getInstance().playSound("victory");
        
        GameWorld world = engine.getWorld();
        if (world == null) {
            return;
        }
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");
        
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        
        Label victoryLabel = new Label("游戏胜利");
        victoryLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 48px; -fx-font-weight: bold;");
        
        // 根据模式显示不同的消息
        String messageText;
        if (gameMode == com.battlecity.engine.state.GameModeType.CLASSIC || gameMode == com.battlecity.engine.state.GameModeType.TIMED) {
            // 经典模式和限时模式：UI风格与失败画面相同
            messageText = "您已击败所有敌人";
        } else {
            // 其他模式：保持原有消息
            int minutes = (int)(elapsedSeconds / 60);
            int seconds = (int)(elapsedSeconds % 60);
            messageText = String.format("恭喜！你在 %02d:%02d 内完成了挑战！", minutes, seconds);
        }
        
        Label messageLabel = new Label(messageText);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");
        
        // 经典模式和限时模式只显示基本信息，与失败画面风格一致
        if (gameMode == com.battlecity.engine.state.GameModeType.CLASSIC || gameMode == com.battlecity.engine.state.GameModeType.TIMED) {
            // 检查是否为经典模式
            if (gameMode == com.battlecity.engine.state.GameModeType.CLASSIC) {
                // 经典模式：添加下一关按钮
                String currentLevelId = world.levelDefinition().id();
                // 获取下一关
                LevelDefinition nextLevel = context.levelRepository().getNextClassicLevel(currentLevelId);
                
                // 检查是否是最后一关
                List<LevelDefinition> classicLevels = context.levelRepository().classicLevels();
                boolean isLastLevel = false;
                if (currentLevelId != null && !classicLevels.isEmpty()) {
                    LevelDefinition lastLevel = classicLevels.get(classicLevels.size() - 1);
                    isLastLevel = currentLevelId.equals(lastLevel.id());
                }
                
                if (isLastLevel) {
                    // 最后一关：显示通关信息
                    Label clearLabel = new Label("恭喜！你已通关经典模式");
                    clearLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 48px; -fx-font-weight: bold;");
                    
                    Button returnBtn = new Button("返回主界面");
                    returnBtn.getStyleClass().add("button");
                    returnBtn.setOnAction(e -> {
                        if (engine != null) {
                            engine.shutdown();
                        }
                        Scene mainMenuScene = buildMainMenuScene(engine);
                        if (sceneChangeCallback != null) {
                            sceneChangeCallback.accept(mainMenuScene);
                        }
                    });
                    
                    content.getChildren().addAll(clearLabel, returnBtn);
                } else {
                    // 不是最后一关：显示下一关按钮
                    Button nextLevelBtn = new Button("进行下一关");
                    nextLevelBtn.getStyleClass().add("button");
                    nextLevelBtn.setOnAction(e -> {
                        if (engine != null) {
                            engine.shutdown();
                        }
                        // 启动下一关
                        if (nextLevel != null) {
                            engine.startCustomLevel(nextLevel);
                            switchToGameScene(engine);
                        }
                    });
                    
                    Button returnBtn = new Button("返回主菜单");
                    returnBtn.getStyleClass().add("button");
                    returnBtn.setOnAction(e -> {
                        if (engine != null) {
                            engine.shutdown();
                        }
                        Scene mainMenuScene = buildMainMenuScene(engine);
                        if (sceneChangeCallback != null) {
                            sceneChangeCallback.accept(mainMenuScene);
                        }
                    });
                    
                    HBox buttonsBox = new HBox(20, nextLevelBtn, returnBtn);
                    buttonsBox.setAlignment(Pos.CENTER);
                    
                    content.getChildren().addAll(victoryLabel, messageLabel, buttonsBox);
                }
            } else {
                // 限时模式：只显示胜利标题、消息和返回按钮
                Button returnBtn = new Button("返回主菜单");
                returnBtn.getStyleClass().add("button");
                returnBtn.setOnAction(e -> {
                    if (engine != null) {
                        engine.shutdown();
                    }
                    Scene mainMenuScene = buildMainMenuScene(engine);
                    if (sceneChangeCallback != null) {
                        sceneChangeCallback.accept(mainMenuScene);
                    }
                });
                
                content.getChildren().addAll(victoryLabel, messageLabel, returnBtn);
            }
        } else {
            // 其他模式：显示完整的统计信息
            // 显示统计信息
            Label statsLabel = new Label(String.format("击杀数: %d / %d", 
                world.getEnemiesKilled(), world.getTotalEnemies()));
            statsLabel.setStyle("-fx-text-fill: lightgreen; -fx-font-size: 18px;");
            
            Button returnBtn = new Button("返回主菜单");
            returnBtn.getStyleClass().add("button");
            returnBtn.setOnAction(e -> {
                if (engine != null) {
                    engine.shutdown();
                }
                Scene mainMenuScene = buildMainMenuScene(engine);
                if (sceneChangeCallback != null) {
                    sceneChangeCallback.accept(mainMenuScene);
                }
            });
            
            content.getChildren().addAll(victoryLabel, messageLabel, statsLabel, returnBtn);
        }
        
        root.setCenter(content);
        
        // 使用地图的实际大小
        double mapWidth = world.levelDefinition().width();
        double mapHeight = world.levelDefinition().height();
        double hudHeight = 40;
        Scene victoryScene = new Scene(root, mapWidth, mapHeight + hudHeight);
        
        // 添加CSS样式表
        victoryScene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        
        // 触发场景切换回调
        if (sceneChangeCallback != null) {
            sceneChangeCallback.accept(victoryScene);
        }
    }
    
    /**
     * 显示游戏失败界面
     */
    private void showGameOverScene(GameEngine engine) {
        GameWorld world = engine.getWorld();
        if (world == null) {
            return;
        }
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");
        
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        
        Label gameOverLabel = new Label("游戏失败");
        gameOverLabel.setStyle("-fx-text-fill: red; -fx-font-size: 48px; -fx-font-weight: bold;");
        
        // 根据失败原因显示不同的消息
        String reason = world.getGameOverReason();
        String messageText;
        if ("BASE".equals(reason)) {
            messageText = "你的基地被毁了";
        } else if ("TANK".equals(reason)) {
            messageText = "你的坦克被毁了";
        } else if ("TIME".equals(reason)) {
            messageText = "限时已结束";
        } else {
            messageText = "游戏失败";
        }
        
        Label messageLabel = new Label(messageText);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");
        
        // 获取游戏模式，用于无尽模式显示得分
        com.battlecity.engine.state.GameState currentState = engine.getStateManager().current();
        com.battlecity.engine.state.GameModeType gameMode = currentState != null ? currentState.mode() : com.battlecity.engine.state.GameModeType.CLASSIC;
        
        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getChildren().addAll(gameOverLabel, messageLabel);
        
        // 无尽模式：添加得分显示
        if (gameMode == com.battlecity.engine.state.GameModeType.ENDLESS) {
            Label scoreLabel = new Label("得分: " + world.getScore());
            scoreLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 24px; -fx-font-weight: bold;");
            contentBox.getChildren().add(scoreLabel);
        }
        
        Button returnBtn = new Button("返回主菜单");
        returnBtn.getStyleClass().add("button"); // 添加与主界面相同的按钮样式
        returnBtn.setOnAction(e -> {
            if (engine != null) {
                engine.shutdown();
            }
            Scene mainMenuScene = buildMainMenuScene(engine);
            if (sceneChangeCallback != null) {
                sceneChangeCallback.accept(mainMenuScene);
            }
        });
        
        contentBox.getChildren().add(returnBtn);
        content.getChildren().add(contentBox);
        root.setCenter(content);
        
        // 使用地图的实际大小
        double mapWidth = world.levelDefinition().width();
        double mapHeight = world.levelDefinition().height();
        double hudHeight = 40; // HUD固定高度
        Scene gameOverScene = new Scene(root, mapWidth, mapHeight + hudHeight);
        
        // 添加CSS样式表，确保按钮应用相同的样式
        gameOverScene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        
        // 触发场景切换回调
        if (sceneChangeCallback != null) {
            sceneChangeCallback.accept(gameOverScene);
        }
    }

    /**
     * 自由选关界面
     */
    private Scene buildLevelSelectScene(GameEngine engine) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));

        Label title = new Label("自由选关");
        title.getStyleClass().add("title");
        BorderPane.setAlignment(title, Pos.CENTER);
        root.setTop(title);

        VBox listBox = new VBox(8);
        listBox.setFillWidth(true);

        // 重新加载关卡列表，以便显示新保存的地图
        List<LevelDefinition> levels = reloadLevels();
        if (levels == null || levels.isEmpty()) {
            Label empty = new Label("未找到关卡文件，请检查 resources/levels 目录。");
            empty.setStyle("-fx-text-fill: red;");
            listBox.getChildren().add(empty);
        } else {
            // 按分类组织关卡
            List<LevelDefinition> classicLevels = new ArrayList<>();
            List<LevelDefinition> endlessLevels = new ArrayList<>();
            List<LevelDefinition> timedLevels = new ArrayList<>();
            List<LevelDefinition> customLevels = new ArrayList<>();
            
            for (LevelDefinition level : levels) {
                String id = level.id();
                if (id != null) {
                    if (id.startsWith("classic-") || id.startsWith("classic_level-")) {
                        classicLevels.add(level);
                    } else if (id.startsWith("endless-")) {
                        endlessLevels.add(level);
                    } else if (id.startsWith("timed-") || id.startsWith("timed_challenge")) {
                        timedLevels.add(level);
                    } else {
                        customLevels.add(level);
                    }
                } else {
                    customLevels.add(level);
                }
            }
            
            // 添加分类标题和关卡
            boolean hasPrevious = false;
            if (!classicLevels.isEmpty()) {
                addCategorySection(listBox, "经典模式", classicLevels, engine, hasPrevious);
                hasPrevious = true;
            }
            if (!endlessLevels.isEmpty()) {
                addCategorySection(listBox, "无尽试炼", endlessLevels, engine, hasPrevious);
                hasPrevious = true;
            }
            if (!timedLevels.isEmpty()) {
                addCategorySection(listBox, "限时挑战", timedLevels, engine, hasPrevious);
                hasPrevious = true;
            }
            if (!customLevels.isEmpty()) {
                addCategorySection(listBox, "自定义关卡", customLevels, engine, hasPrevious);
            }
        }

        ScrollPane scrollPane = new ScrollPane(listBox);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        // 创建地图按钮
        Button createMapBtn = new Button("创建地图");
        createMapBtn.setOnAction(e -> {
            LevelEditor editor = new LevelEditor();
            // 设置最大化回调
            editor.setMaximizeCallback(shouldMaximize -> {
                if (primaryStage != null) {
                    primaryStage.setMaximized(shouldMaximize);
                }
            });
            // 设置保存回调，保存后刷新选关界面
            editor.setOnSaveCallback(() -> {
                // 保存成功后，如果当前在选关界面，刷新它
                // 注意：这里只是触发刷新，实际刷新会在返回选关界面时进行
            });
            // 在自由选关界面创建地图时，模式为自定义
            Scene editorScene = editor.buildEditorScene(() -> {
                // 返回自由选关界面，取消最大化
                if (primaryStage != null) {
                    primaryStage.setMaximized(false);
                }
                Scene selectScene = buildLevelSelectScene(engine);
                if (sceneChangeCallback != null) {
                    sceneChangeCallback.accept(selectScene);
                }
            }, (updatedScene) -> {
                // 地图大小改变时更新窗口
                if (sceneChangeCallback != null) {
                    sceneChangeCallback.accept(updatedScene);
                }
            }, LevelEditor.GameMode.CUSTOM);
            
            // 触发场景切换，BattleCityApplication 会检测到 isLevelEditor 标记并最大化窗口
            if (sceneChangeCallback != null) {
                sceneChangeCallback.accept(editorScene);
            }
        });

        HBox bottomBox = new HBox(10, createMapBtn, new Separator(), new Button("返回主菜单"));
        bottomBox.setAlignment(Pos.CENTER);
        Button backBtn = new Button("返回主菜单");
        backBtn.setOnAction(e -> {
            Scene menuScene = buildMainMenuScene(engine);
            if (sceneChangeCallback != null) {
                sceneChangeCallback.accept(menuScene);
            }
        });
        BorderPane.setAlignment(backBtn, Pos.CENTER);
        BorderPane.setMargin(backBtn, new Insets(12, 0, 0, 0));
        
        HBox bottomButtons = new HBox(10, createMapBtn, backBtn);
        bottomButtons.setAlignment(Pos.CENTER);
        root.setBottom(bottomButtons);

        Scene scene = new Scene(root, context.config().virtualWidth(), context.config().virtualHeight());
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        return scene;
    }
    
    /**
     * 添加分类区域
     */
    private void addCategorySection(VBox listBox, String categoryName, List<LevelDefinition> levels, GameEngine engine, boolean hasPrevious) {
        if (levels.isEmpty()) {
            return; // 如果该分类没有关卡，不显示分类标题
        }
        
        // 如果前面有分类，添加分隔线
        if (hasPrevious) {
            Separator separator = new Separator();
            separator.setPadding(new Insets(10, 0, 10, 0));
            listBox.getChildren().add(separator);
        }
        
        // 分类标题
        Label categoryLabel = new Label(categoryName);
        categoryLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f4d35e; -fx-padding: 10 0 5 0;");
        listBox.getChildren().add(categoryLabel);
        
        // 该分类下的关卡
        for (LevelDefinition level : levels) {
            // 只显示地图名称（用户自定义的名称）
            Button levelBtn = new Button(level.name());
            levelBtn.setMaxWidth(Double.MAX_VALUE);
            levelBtn.setOnAction(e -> {
                // 根据关卡ID启动对应的游戏模式
                engine.startLevelByType(level);
                switchToGameScene(engine);
            });

            Label meta = new Label(level.width() + " x " + level.height());
            meta.setStyle("-fx-text-fill: #ccc;");
            
            // 添加修改按钮
            Button editBtn = new Button("修改");
            editBtn.setPrefWidth(40); // 设置宽度为原来的一半
            editBtn.setOnAction(e -> {
                // 打开地图编辑器并加载该关卡
                LevelEditor editor = new LevelEditor();
                // 设置最大化回调
                editor.setMaximizeCallback(shouldMaximize -> {
                    if (primaryStage != null) {
                        primaryStage.setMaximized(shouldMaximize);
                    }
                });
                // 设置保存回调，保存后刷新选关界面
                editor.setOnSaveCallback(() -> {
                    // 保存成功后，刷新选关界面
                });
                
                // 根据关卡ID判断游戏模式
                LevelEditor.GameMode editorMode = LevelEditor.GameMode.CUSTOM;
                String id = level.id();
                if (id != null) {
                    if (id.startsWith("classic-") || id.startsWith("classic_level-")) {
                        editorMode = LevelEditor.GameMode.CLASSIC;
                    } else if (id.startsWith("endless-")) {
                        editorMode = LevelEditor.GameMode.ENDLESS;
                    } else if (id.startsWith("timed-") || id.startsWith("timed_challenge")) {
                        editorMode = LevelEditor.GameMode.TIMED;
                    }
                }
                
                // 构建编辑器场景，加载现有地图
                Scene editorScene = editor.buildEditorScene(() -> {
                    // 返回自由选关界面，取消最大化
                    if (primaryStage != null) {
                        primaryStage.setMaximized(false);
                    }
                    Scene selectScene = buildLevelSelectScene(engine);
                    if (sceneChangeCallback != null) {
                        sceneChangeCallback.accept(selectScene);
                    }
                }, (updatedScene) -> {
                    // 地图大小改变时更新窗口
                    if (sceneChangeCallback != null) {
                        sceneChangeCallback.accept(updatedScene);
                    }
                }, editorMode, level);
                
                // 触发场景切换
                if (sceneChangeCallback != null) {
                    sceneChangeCallback.accept(editorScene);
                }
            });
            
            // 添加删除按钮
            Button deleteBtn = new Button("删除");
            deleteBtn.setPrefWidth(40); // 设置宽度为原来的一半
            deleteBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
            deleteBtn.setOnAction(e -> {
                // 弹出确认对话框
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("确认删除");
                confirmAlert.setHeaderText("删除地图");
                confirmAlert.setContentText("确定要删除地图 \"" + level.name() + "\" 吗？\n此操作无法撤销。");
                
                confirmAlert.showAndWait().ifPresent(buttonType -> {
                    if (buttonType == ButtonType.OK) {
                        // 确认删除
                        try {
                            // 获取关卡文件路径
                            Path levelsDir = ResourceLocator.levelsDirectory();
                            String fileName = level.id() + ".json";
                            Path filePath = levelsDir.resolve(fileName);
                            
                            // 删除文件
                            if (Files.exists(filePath)) {
                                Files.delete(filePath);
                                
                                // 显示成功提示
                                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                                successAlert.setTitle("删除成功");
                                successAlert.setHeaderText(null);
                                successAlert.setContentText("地图 \"" + level.name() + "\" 已成功删除。");
                                successAlert.showAndWait();
                                
                                // 刷新选关界面
                                Scene selectScene = buildLevelSelectScene(engine);
                                if (sceneChangeCallback != null) {
                                    sceneChangeCallback.accept(selectScene);
                                }
                            } else {
                                // 文件不存在
                                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                errorAlert.setTitle("删除失败");
                                errorAlert.setHeaderText(null);
                                errorAlert.setContentText("找不到地图文件: " + filePath);
                                errorAlert.showAndWait();
                            }
                        } catch (Exception ex) {
                            // 删除失败
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("删除失败");
                            errorAlert.setHeaderText(null);
                            errorAlert.setContentText("删除地图时发生错误: " + ex.getMessage());
                            errorAlert.showAndWait();
                            ex.printStackTrace();
                        }
                    }
                });
            });

            HBox row = new HBox(10, levelBtn, meta, editBtn, deleteBtn);
            row.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(levelBtn, Priority.ALWAYS);
            listBox.getChildren().add(row);
        }
    }
    
    /**
     * 重新加载关卡列表
     */
    private List<LevelDefinition> reloadLevels() {
        try {
            LevelLoader loader = new LevelLoader();
            Path levelsDir = ResourceLocator.levelsDirectory();
            return Files.list(levelsDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .map(loader::load)
                    .toList();
        } catch (Exception e) {
            // 如果重新加载失败，使用原来的关卡列表
            return context.levelRepository().allLevels();
        }
    }
}

