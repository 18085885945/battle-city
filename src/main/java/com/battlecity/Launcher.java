package com.battlecity;

/**
 * JavaFX 启动器，负责解耦可执行入口与 JavaFX Application。
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        BattleCityApplication.launchApp(args);
    }
}

