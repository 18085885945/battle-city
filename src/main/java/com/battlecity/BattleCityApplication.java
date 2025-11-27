package com.battlecity;

import com.battlecity.bootstrap.GameBootstrapper;
import com.battlecity.controller.GameController;
import com.battlecity.engine.GameContext;
import com.battlecity.engine.GameEngine;
import com.battlecity.ui.SceneRouter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX Application 实现，负责初始化 UI 与游戏引擎。
 */
public class BattleCityApplication extends Application {

    private GameEngine gameEngine;

    public static void launchApp(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        GameBootstrapper bootstrapper = new GameBootstrapper();
        GameContext context = bootstrapper.bootstrap();

        SceneRouter sceneRouter = new SceneRouter(context);
        GameController controller = new GameController(context, sceneRouter);
        sceneRouter.setController(controller);

        gameEngine = new GameEngine(context, controller);
        Scene initialScene = sceneRouter.buildMainMenuScene(gameEngine);

        primaryStage.setTitle("Battle City - JavaFX Edition");
        primaryStage.setScene(initialScene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // 监听场景切换
        sceneRouter.setOnSceneChange(scene -> {
            if (scene != null) {
                primaryStage.setScene(scene);
                // 场景切换后请求焦点，确保能接收键盘输入
                // 使用Platform.runLater确保在场景完全显示后请求焦点
                javafx.application.Platform.runLater(() -> {
                    scene.getRoot().requestFocus();
                });
            }
        });
    }

    @Override
    public void stop() {
        if (gameEngine != null) {
            gameEngine.shutdown();
        }
    }
}

