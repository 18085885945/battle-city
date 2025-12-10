package com.battlecity.ui;

import com.battlecity.controller.GameController;
import com.battlecity.controller.InputController;
import com.battlecity.controller.SceneRouterFacade;
import com.battlecity.engine.GameContext;
import com.battlecity.engine.GameEngine;
import com.battlecity.model.GameWorld;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;

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

        VBox menu = new VBox(12, title, classicBtn, endlessBtn, timedBtn);
        menu.setAlignment(Pos.CENTER);
        root.setCenter(menu);

        Scene scene = new Scene(root, context.config().virtualWidth(), context.config().virtualHeight());
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        return scene;
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

        hud.getChildren().addAll(healthLabel, enemiesLabel);
        root.setTop(hud);

        // 实时更新HUD并检查游戏失败
        final AnimationTimer[] hudTimerRef = new AnimationTimer[1];
        hudTimerRef[0] = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (world != null && world.playerTank() != null) {
                    healthLabel.setText("生命值: " + world.playerTank().health());
                    enemiesLabel.setText("敌人: " + world.enemyTanks().size());
                    
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
        returnBtn.setStyle("-fx-font-size: 18px; -fx-padding: 10px 20px;");
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
        
        // 触发场景切换回调
        if (sceneChangeCallback != null) {
            sceneChangeCallback.accept(gameOverScene);
        }
    }
}

