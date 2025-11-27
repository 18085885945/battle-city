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

        Label healthLabel = new Label();
        healthLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        Label enemiesLabel = new Label();
        enemiesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        hud.getChildren().addAll(healthLabel, enemiesLabel);
        root.setTop(hud);

        // 实时更新HUD
        AnimationTimer hudTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (world != null && world.playerTank() != null) {
                    healthLabel.setText("生命值: " + world.playerTank().health());
                    enemiesLabel.setText("敌人: " + world.enemyTanks().size());
                }
            }
        };
        hudTimer.start();

        // 创建场景并绑定输入
        Scene gameScene = new Scene(root, context.config().virtualWidth(), context.config().virtualHeight());
        
        gameScene.setOnKeyPressed(e -> {
            // 暂停功能（P键或ESC）
            if (e.getCode() == KeyCode.P || e.getCode() == KeyCode.ESCAPE) {
                if (engine != null) {
                    engine.pause();
                }
            } else {
                GameController controller = getController(engine);
                if (controller != null) {
                    controller.onKeyPressed(e.getCode());
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
}

