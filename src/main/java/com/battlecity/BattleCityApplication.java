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
        sceneRouter.setPrimaryStage(primaryStage); // 传递Stage引用以便控制窗口最大化
        // 初始化音频管理器并预加载音效
        com.battlecity.audio.AudioManager audioManager = com.battlecity.audio.AudioManager.getInstance();
        audioManager.preloadSounds();
        
        GameController controller = new GameController(context, sceneRouter);
        sceneRouter.setController(controller);

        gameEngine = new GameEngine(context, controller);
        Scene initialScene = sceneRouter.buildMainMenuScene(gameEngine);

        primaryStage.setTitle("Battle City - JavaFX Edition");
        primaryStage.setScene(initialScene);
        primaryStage.setResizable(true); // 允许调整大小，以便地图编辑器可以调整窗口
        primaryStage.show();

        // 监听场景切换
        sceneRouter.setOnSceneChange(scene -> {
            if (scene != null) {
                // 检查是否是地图编辑器场景
                Boolean isLevelEditor = (Boolean) scene.getProperties().get("isLevelEditor");
                
                // 使用数组包装场景引用，以便在lambda中修改
                final Scene[] sceneRef = new Scene[1];
                sceneRef[0] = scene;
                
                if (Boolean.TRUE.equals(isLevelEditor)) {
                    javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
                    javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
                    primaryStage.setMaximized(false);
                    primaryStage.setX(bounds.getMinX());
                    primaryStage.setY(bounds.getMinY());
                    primaryStage.setWidth(bounds.getWidth());
                    primaryStage.setHeight(bounds.getHeight());
                    primaryStage.setScene(scene);
                } else {
                    primaryStage.setScene(scene);
                    
                    // 优先从场景属性中获取大小信息（游戏场景会设置这些属性）
                    Double storedWidth = (Double) scene.getProperties().get("sceneWidth");
                    Double storedHeight = (Double) scene.getProperties().get("sceneHeight");
                    
                    double sceneWidth;
                    double sceneHeight;
                    
                    if (storedWidth != null && storedHeight != null && storedWidth > 0 && storedHeight > 0) {
                        // 使用存储的场景大小
                        sceneWidth = storedWidth;
                        sceneHeight = storedHeight;
                    } else {
                        // 从场景获取大小
                        sceneWidth = scene.getWidth();
                        sceneHeight = scene.getHeight();
                        
                        // 如果场景大小无效（0或NaN），使用场景的prefSize或默认值
                        if (sceneWidth <= 0 || Double.isNaN(sceneWidth) || sceneHeight <= 0 || Double.isNaN(sceneHeight)) {
                            // 尝试从场景的根节点获取大小
                            javafx.scene.Node root = scene.getRoot();
                            if (root instanceof javafx.scene.layout.Region) {
                                javafx.scene.layout.Region region = (javafx.scene.layout.Region) root;
                                sceneWidth = region.getPrefWidth() > 0 ? region.getPrefWidth() : 800;
                                sceneHeight = region.getPrefHeight() > 0 ? region.getPrefHeight() : 640;
                            } else {
                                sceneWidth = 800;
                                sceneHeight = 640;
                            }
                        }
                    }
                    
                    // 获取屏幕大小，确保窗口不会超出屏幕
                    javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
                    javafx.geometry.Rectangle2D screenBounds = screen.getVisualBounds();
                    double maxWidth = screenBounds.getWidth() * 0.95; // 留5%边距
                    double maxHeight = screenBounds.getHeight() * 0.95;
                    
                    // 如果场景很大，使用最大化或限制在屏幕范围内
                    boolean shouldMaximize = sceneWidth >= maxWidth * 0.9 || sceneHeight >= maxHeight * 0.9;
                    
                    if (shouldMaximize) {
                        // 大地图场景，最大化窗口
                        primaryStage.setMaximized(true);
                    } else {
                        // 小地图场景，取消最大化并调整到场景大小
                        if (primaryStage.isMaximized()) {
                            primaryStage.setMaximized(false);
                        }
                        
                        // 窗口装饰尺寸（标题栏、边框等）
                        // 标题栏高度约30-40像素，左右边框各约8-10像素
                        double windowDecorationHeight = 40; // 窗口标题栏和边框的高度
                        double windowDecorationWidth = 16; // 左右边框的总宽度（约8像素每边）
                        
                        // 计算需要的窗口大小（场景大小 + 窗口装饰）
                        double requiredWidth = sceneWidth + windowDecorationWidth;
                        double requiredHeight = sceneHeight + windowDecorationHeight;
                        
                        // 确保窗口大小不超过屏幕，但至少能完整显示场景
                        double finalWidth = Math.min(requiredWidth, maxWidth);
                        double finalHeight = Math.min(requiredHeight, maxHeight);
                        
                        // 如果计算出的窗口大小小于场景大小，说明屏幕太小，使用最大化
                        if (finalWidth < sceneWidth + windowDecorationWidth || finalHeight < sceneHeight + windowDecorationHeight) {
                            primaryStage.setMaximized(true);
                        } else {
                            // 设置窗口大小
                            primaryStage.setWidth(finalWidth);
                            primaryStage.setHeight(finalHeight);
                            
                            // 居中显示
                            primaryStage.centerOnScreen();
                        }
                    }
                }
                
                // 场景切换后请求焦点，确保能接收键盘输入
                // 使用Platform.runLater确保在场景完全显示后请求焦点
                javafx.application.Platform.runLater(() -> {
                    sceneRef[0].getRoot().requestFocus();
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

