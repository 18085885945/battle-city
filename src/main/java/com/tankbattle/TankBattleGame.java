//main
package com.tankbattle;

import com.tankbattle.ui.GamePanel;
import com.tankbattle.ui.HelpPanel;
import com.tankbattle.ui.LevelSelectPanel;
import com.tankbattle.ui.MenuPanel;

import javax.swing.*;
import java.awt.*;

public class TankBattleGame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private MenuPanel menuPanel;
    private LevelSelectPanel levelSelectPanel;
    private GamePanel gamePanel;
    private HelpPanel helpPanel; // 新增操作指南面板

    public TankBattleGame() {
        setTitle("坦克大战");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // 设置卡片布局
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 创建各个面板
        menuPanel = new MenuPanel(this);
        levelSelectPanel = new LevelSelectPanel(this);
        gamePanel = new GamePanel(this);
        helpPanel = new HelpPanel(this); // 新增操作指南面板

        // 添加面板到主面板
        mainPanel.add(menuPanel, "Menu");
        mainPanel.add(levelSelectPanel, "LevelSelect");
        mainPanel.add(gamePanel, "Game");
        mainPanel.add(helpPanel, "Help"); // 新增操作指南面板

        add(mainPanel);

        // 显示菜单
        showMenu();

        pack();
        setLocationRelativeTo(null);
    }

    public void showMenu() {
        cardLayout.show(mainPanel, "Menu");
    }

    public void showLevelSelect() {
        cardLayout.show(mainPanel, "LevelSelect");
    }

    // 新增显示操作指南方法
    public void showHelp() {
        cardLayout.show(mainPanel, "Help");
    }

    public void startGame(int level) {
        gamePanel.startLevel(level);
        cardLayout.show(mainPanel, "Game");
        gamePanel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TankBattleGame().setVisible(true);
        });
    }
}