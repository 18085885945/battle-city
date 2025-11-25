//主界面UI
package com.tankbattle.ui;

import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {
    private TankBattleGame parent;

    public MenuPanel(TankBattleGame parent) {
        this.parent = parent;
        setPreferredSize(new Dimension(800, 600));
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 标题
        JLabel titleLabel = new JLabel("坦克大战", JLabel.CENTER);
        titleLabel.setFont(new Font("宋体", Font.BOLD, 48));
        titleLabel.setForeground(Color.RED);
        add(titleLabel, BorderLayout.NORTH);

        // 按钮面板 - 改为3个按钮：开始游戏、操作指南、退出游戏
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 10, 10));
        buttonPanel.setOpaque(false);

        JButton startButton = new JButton("开始游戏");
        JButton helpButton = new JButton("操作指南"); // 新增操作指南按钮
        JButton exitButton = new JButton("退出游戏");

        // 设置按钮样式
        Font buttonFont = new Font("宋体", Font.BOLD, 24);
        startButton.setFont(buttonFont);
        helpButton.setFont(buttonFont);
        exitButton.setFont(buttonFont);

        // 设置按钮颜色
        startButton.setBackground(new Color(70, 130, 180)); // 钢蓝色
        startButton.setForeground(Color.WHITE);
        helpButton.setBackground(new Color(46, 139, 87)); // 海洋绿色 - 原关卡选择的颜色
        helpButton.setForeground(Color.WHITE);
        exitButton.setBackground(new Color(178, 34, 34)); // 火砖色
        exitButton.setForeground(Color.WHITE);

        startButton.setPreferredSize(new Dimension(200, 60));
        helpButton.setPreferredSize(new Dimension(200, 60));
        exitButton.setPreferredSize(new Dimension(200, 60));

        // 设置按钮监听器
        startButton.addActionListener(e -> parent.showLevelSelect());
        helpButton.addActionListener(e -> parent.showHelp()); // 新增操作指南按钮监听
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(startButton);
        buttonPanel.add(helpButton); // 添加操作指南按钮
        buttonPanel.add(exitButton);

        // 将按钮面板居中
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(buttonPanel);

        add(centerPanel, BorderLayout.CENTER);

        // 移除底部的操作提示文字
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 背景已经是白色，不需要额外绘制黑色背景

        // 在白色背景上绘制深色坦克图标
        drawTank(g, 100, 100, new Color(0, 100, 0), true); // 深绿色玩家坦克
        drawTank(g, 650, 100, new Color(139, 0, 0), false);  // 深红色敌方坦克

        // 添加一些装饰元素
        g.setColor(new Color(240, 240, 240)); // 浅灰色装饰线
        for (int i = 0; i < getWidth(); i += 30) {
            g.drawLine(i, 0, i, getHeight());
        }
        for (int i = 0; i < getHeight(); i += 30) {
            g.drawLine(0, i, getWidth(), i);
        }
    }

    private void drawTank(Graphics g, int x, int y, Color color, boolean isPlayer) {
        g.setColor(color);

        // 坦克主体
        g.fillRect(x, y, 40, 40);

        // 坦克炮管
        if (isPlayer) {
            g.fillRect(x + 18, y - 20, 4, 20); // 向上
        } else {
            g.fillRect(x + 18, y + 40, 4, 20); // 向下
        }

        // 坦克履带
        g.setColor(Color.GRAY);
        g.fillRect(x - 5, y, 5, 40);
        g.fillRect(x + 40, y, 5, 40);
    }
}