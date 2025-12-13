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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.ScrollPane;
import java.util.function.Consumer;
import java.util.List;

/**
 * 简易场景路由，后续可扩展不同 Scene。
 */
public class SceneRouter implements SceneRouterFacade {

    private final GameContext context;
    private GameEngine currentEngine;
    private Scene currentGameScene;
    private Consumer<Scene> sceneChangeCallback;

    public SceneRouter(GameContext context) {
        this.context = context;
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
            engine.startClassicMode();
            switchToGameScene(engine);
        });

        Button endlessBtn = new Button("无尽模式");
        endlessBtn.setOnAction(e -> {
            engine.startEndlessMode();
            switchToGameScene(engine);
        });

        Button timedBtn = new Button("限时模式");
        timedBtn.setOnAction(e -> {
            engine.startTimedMode();
            switchToGameScene(engine);
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
            return;
        }

        BorderPane root = new BorderPane();
        // 设置BorderPane可以接收键盘焦点
        root.setFocusTraversable(true);
        
        // 创建游戏视图
        GameView gameView = new GameView(context.config().virtualWidth(), context.config().virtualHeight());
        gameView.bindWorld(world);
        root.setCenter(gameView);

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
        
        Label baseHealthLabel = new Label();
        baseHealthLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: center-right;");

        // 将baseHealthLabel放置在HBox的右侧
        HBox.setHgrow(baseHealthLabel, Priority.ALWAYS);
        
        hud.getChildren().addAll(healthLabel, enemiesLabel, baseHealthLabel);
        root.setTop(hud);

        // 实时更新HUD并检查游戏失败
        final AnimationTimer[] hudTimerRef = new AnimationTimer[1];
        hudTimerRef[0] = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (world != null && world.playerTank() != null) {
                    healthLabel.setText("生命值: " + world.playerTank().health());
                    enemiesLabel.setText("敌人: " + world.enemyTanks().size());
                    baseHealthLabel.setText("基地生命: " + world.base().health());
                    
                    // 检查游戏是否失败
                    if (world.isGameOver()) {
                        hudTimerRef[0].stop();
                        engine.pause();
                        showGameOverScene(engine);
                    }
                }
            }
        };
        hudTimerRef[0].start();

        // 创建场景并绑定输入
        // 场景高度 = 地图高度 + HUD高度，确保下边界可见
        double mapWidth = context.config().virtualWidth();
        double mapHeight = context.config().virtualHeight();
        double hudHeight = 40; // HUD固定高度
        Scene gameScene = new Scene(root, mapWidth, mapHeight + hudHeight);
        
        gameScene.setOnKeyPressed(e -> {
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
            GameController controller = getController(engine);
            if (controller != null) {
                controller.onKeyReleased(e.getCode());
            }
        });

        // 鼠标点击时请求焦点，确保能接收键盘输入
        root.setOnMouseClicked(e -> root.requestFocus());

        this.currentGameScene = gameScene;
        
        // 触发场景切换回调
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
        
        // 创建暂停场景
        double mapWidth = context.config().virtualWidth();
        double mapHeight = context.config().virtualHeight();
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
        } else {
            messageText = "游戏失败";
        }
        
        Label messageLabel = new Label(messageText);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");
        
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
        
        content.getChildren().addAll(gameOverLabel, messageLabel, returnBtn);
        root.setCenter(content);
        
        Scene gameOverScene = new Scene(root, context.config().virtualWidth(), context.config().virtualHeight());
        
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

        List<LevelDefinition> levels = context.levelRepository().allLevels();
        if (levels == null || levels.isEmpty()) {
            Label empty = new Label("未找到关卡文件，请检查 resources/levels 目录。");
            empty.setStyle("-fx-text-fill: red;");
            listBox.getChildren().add(empty);
        } else {
            for (LevelDefinition level : levels) {
                Button levelBtn = new Button(level.name() + " (" + level.id() + ")");
                levelBtn.setMaxWidth(Double.MAX_VALUE);
                levelBtn.setOnAction(e -> {
                    engine.startCustomLevel(level);
                    switchToGameScene(engine);
                });

                Label meta = new Label(level.width() + " x " + level.height());
                meta.setStyle("-fx-text-fill: #ccc;");

                HBox row = new HBox(10, levelBtn, meta);
                row.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(levelBtn, Priority.ALWAYS);
                listBox.getChildren().add(row);
            }
        }

        ScrollPane scrollPane = new ScrollPane(listBox);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        Button backBtn = new Button("返回主菜单");
        backBtn.setOnAction(e -> {
            Scene menuScene = buildMainMenuScene(engine);
            if (sceneChangeCallback != null) {
                sceneChangeCallback.accept(menuScene);
            }
        });
        BorderPane.setAlignment(backBtn, Pos.CENTER);
        BorderPane.setMargin(backBtn, new Insets(12, 0, 0, 0));
        root.setBottom(backBtn);

        Scene scene = new Scene(root, context.config().virtualWidth(), context.config().virtualHeight());
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        return scene;
    }
}

