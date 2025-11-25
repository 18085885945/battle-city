//操作指南UI
package com.tankbattle.ui;

import com.tankbattle.TankBattleGame;

import javax.swing.*;
import java.awt.*;

public class HelpPanel extends JPanel {
    private TankBattleGame parent;

    public HelpPanel(TankBattleGame parent) {
        this.parent = parent;
        setPreferredSize(new Dimension(800, 600));
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 标题
        JLabel titleLabel = new JLabel("操作指南", JLabel.CENTER);
        titleLabel.setFont(new Font("宋体", Font.BOLD, 48));
        titleLabel.setForeground(Color.BLUE);
        add(titleLabel, BorderLayout.NORTH);

        // 创建主内容面板，使用BorderLayout分区
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setOpaque(false);

        // 上部：操作说明面板
        JPanel instructionPanel = createInstructionPanel();
        contentPanel.add(instructionPanel, BorderLayout.NORTH);

        // 中部：图形演示面板
        JPanel graphicPanel = createGraphicPanel();
        contentPanel.add(graphicPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // 返回按钮
        JButton backButton = new JButton("返回主菜单");
        backButton.setFont(new Font("宋体", Font.BOLD, 20));
        backButton.setBackground(new Color(169, 169, 169));
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> parent.showMenu());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createInstructionPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

        // 创建操作说明标签
        JLabel moveLabel = createInstructionLabel("方向键 ↑↓←→", "控制坦克移动");
        JLabel shootLabel = createInstructionLabel("空格键", "发射子弹");
        JLabel menuLabel = createInstructionLabel("ESC键", "返回主菜单");
        JLabel goalLabel = createInstructionLabel("游戏目标", "消灭所有敌方坦克通关");

        panel.add(moveLabel);
        panel.add(shootLabel);
        panel.add(menuLabel);
        panel.add(goalLabel);

        return panel;
    }

    private JPanel createGraphicPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 50, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // 左侧：坦克示意图
        JPanel tankPanel = createTankDemoPanel();
        // 右侧：键盘示意图
        JPanel keyboardPanel = createKeyboardDemoPanel();

        panel.add(tankPanel);
        panel.add(keyboardPanel);

        return panel;
    }

    private JPanel createTankDemoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("坦克类型", JLabel.CENTER);
        titleLabel.setFont(new Font("宋体", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));

        JPanel tankDemoPanel = new JPanel(new GridLayout(2, 1, 0, 30));
        tankDemoPanel.setOpaque(false);
        tankDemoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // 玩家坦克演示
        JPanel playerTankPanel = new JPanel(new BorderLayout());
        playerTankPanel.setOpaque(false);
        JLabel playerLabel = new JLabel("玩家坦克", JLabel.CENTER);
        playerLabel.setFont(new Font("宋体", Font.PLAIN, 18));
        playerLabel.setForeground(Color.BLACK);

        JPanel playerTankGraphic = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawTankExample(g, getWidth()/2 - 20, 20, Color.GREEN);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(100, 80);
            }
        };
        playerTankGraphic.setOpaque(false);

        playerTankPanel.add(playerLabel, BorderLayout.NORTH);
        playerTankPanel.add(playerTankGraphic, BorderLayout.CENTER);

        // 敌方坦克演示
        JPanel enemyTankPanel = new JPanel(new BorderLayout());
        enemyTankPanel.setOpaque(false);
        JLabel enemyLabel = new JLabel("敌方坦克", JLabel.CENTER);
        enemyLabel.setFont(new Font("宋体", Font.PLAIN, 18));
        enemyLabel.setForeground(Color.BLACK);

        JPanel enemyTankGraphic = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawTankExample(g, getWidth()/2 - 20, 20, Color.RED);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(100, 80);
            }
        };
        enemyTankGraphic.setOpaque(false);

        enemyTankPanel.add(enemyLabel, BorderLayout.NORTH);
        enemyTankPanel.add(enemyTankGraphic, BorderLayout.CENTER);

        tankDemoPanel.add(playerTankPanel);
        tankDemoPanel.add(enemyTankPanel);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(tankDemoPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createKeyboardDemoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("键盘控制", JLabel.CENTER);
        titleLabel.setFont(new Font("宋体", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));

        JPanel keyboardDemoPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        keyboardDemoPanel.setOpaque(false);
        keyboardDemoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // 添加键盘按键演示
        keyboardDemoPanel.add(createKeyDemo("↑", "上移", 60, 40));
        keyboardDemoPanel.add(createKeyDemo("↓", "下移", 60, 40));
        keyboardDemoPanel.add(createKeyDemo("←", "左移", 60, 40));
        keyboardDemoPanel.add(createKeyDemo("→", "右移", 60, 40));
        keyboardDemoPanel.add(createKeyDemo("空格", "射击", 100, 40));
        keyboardDemoPanel.add(createKeyDemo("ESC", "返回", 80, 40));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(keyboardDemoPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createKeyDemo(String key, String function, int width, int height) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // 按键图形
        JPanel keyPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawKeyExample(g, getWidth()/2 - width/2, 5, key, width, height);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(120, 60);
            }
        };
        keyPanel.setOpaque(false);

        // 功能说明
        JLabel functionLabel = new JLabel(function, JLabel.CENTER);
        functionLabel.setFont(new Font("宋体", Font.PLAIN, 16));
        functionLabel.setForeground(Color.BLACK);

        panel.add(keyPanel, BorderLayout.CENTER);
        panel.add(functionLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createInstructionLabel(String title, String description) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("宋体", Font.BOLD, 20));
        titleLabel.setForeground(new Color(70, 130, 180));

        JLabel descLabel = new JLabel(description, JLabel.CENTER);
        descLabel.setFont(new Font("宋体", Font.PLAIN, 16));
        descLabel.setForeground(Color.BLACK);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(descLabel, BorderLayout.CENTER);

        // 将面板包装在JLabel中返回
        JLabel container = new JLabel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 80);
            }
        };
        container.setLayout(new BorderLayout());
        container.add(panel, BorderLayout.CENTER);
        container.setOpaque(true);
        container.setBackground(new Color(240, 248, 255)); // 浅蓝色背景
        container.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));

        return container;
    }

    private void drawTankExample(Graphics g, int x, int y, Color color) {
        g.setColor(color);
        g.fillRect(x, y, 40, 40); // 坦克主体
        g.fillRect(x + 18, y - 20, 4, 20); // 炮管

        // 坦克履带
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x - 5, y, 5, 40);
        g.fillRect(x + 40, y, 5, 40);
    }

    private void drawKeyExample(Graphics g, int x, int y, String key, int width, int height) {
        // 绘制按键
        g.setColor(new Color(220, 220, 220));
        g.fillRoundRect(x, y, width, height, 10, 10);
        g.setColor(Color.BLACK);
        g.drawRoundRect(x, y, width, height, 10, 10);

        g.setFont(new Font("宋体", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(key);
        g.drawString(key, x + width/2 - textWidth/2, y + height/2 + 5);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 只绘制背景，所有图形都在子面板中绘制
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
