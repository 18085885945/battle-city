package com.battlecity.controller;

import com.battlecity.engine.GameEngine;
import javafx.scene.Scene;

/**
 * 通过接口避免控制层直接依赖具体 UI 实现。
 */
public interface SceneRouterFacade {
    Scene buildMainMenuScene(GameEngine engine);
}

