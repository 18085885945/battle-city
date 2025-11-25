//关卡选择UI
package com.tankbattle.ui;

import com.tankbattle.TankBattleGame;

import javax.swing.*;
import java.awt.*;

public class LevelSelectPanel extends JPanel {
    private TankBattleGame parent;

    // 在LevelSelectPanel的构造函数中，修改返回按钮的行为
    public LevelSelectPanel(TankBattleGame parent) {
        this.parent = parent;
        setPreferredSize(new Dimension(800, 600));
        setLayout(new BorderLayout());

        // 标题
        JLabel titleLabel = new JLabel("选择关卡", JLabel.CENTER);
        titleLabel.setFont(new Font("宋体", Font.BOLD, 48));
        titleLabel.setForeground(Color.BLUE); // 改为蓝色以适应白色背景
        add(titleLabel, BorderLayout.NORTH);

        // 关卡按钮面板
        JPanel levelPanel = new JPanel();
        levelPanel.setLayout(new GridLayout(1, 3, 20, 0));
        levelPanel.setOpaque(false);

        for (int i = 1; i <= 3; i++) {
            JButton levelButton = new JButton("关卡 " + i);
            levelButton.setFont(new Font("宋体", Font.BOLD, 24));
            levelButton.setPreferredSize(new Dimension(150, 80));

            // 设置不同关卡的按钮颜色
            switch(i) {
                case 1:
                    levelButton.setBackground(new Color(144, 238, 144)); // 浅绿色 - 简单
                    break;
                case 2:
                    levelButton.setBackground(new Color(255, 165, 0));   // 橙色 - 中等
                    break;
                case 3:
                    levelButton.setBackground(new Color(255, 99, 71));   // 番茄红 - 困难
                    break;
            }
            levelButton.setForeground(Color.BLACK);

            final int level = i;
            levelButton.addActionListener(e -> parent.startGame(level));
            levelPanel.add(levelButton);
        }

        // 返回按钮 - 修改为返回主菜单
        JButton backButton = new JButton("返回主菜单");
        backButton.setFont(new Font("宋体", Font.BOLD, 20));
        backButton.setBackground(new Color(169, 169, 169)); // 深灰色
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> parent.showMenu()); // 返回主菜单

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.add(backButton);

        // 将关卡面板居中
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(levelPanel);

        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 设置白色背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // 绘制关卡预览
        drawLevelPreview(g, 100, 200, 1);
        drawLevelPreview(g, 300, 200, 2);
        drawLevelPreview(g, 500, 200, 3);

        // 添加关卡难度说明
        g.setColor(Color.BLACK);
        g.setFont(new Font("宋体", Font.PLAIN, 16));
        g.drawString("简单", 150, 380);
        g.drawString("中等", 350, 380);
        g.drawString("困难", 550, 380);
    }

    private void drawLevelPreview(Graphics g, int x, int y, int level) {
        g.setColor(Color.WHITE);
        g.drawRect(x, y, 150, 150);

        g.setColor(Color.GRAY);
        // 绘制不同关卡的简单地图预览
        switch(level) {
            case 1:
                // 简单关卡 - 少量障碍物
                g.fillRect(x + 30, y + 30, 20, 90);
                g.fillRect(x + 100, y + 30, 20, 90);
                break;
            case 2:
                // 中等关卡 - 更多障碍物
                g.fillRect(x + 20, y + 20, 110, 20);
                g.fillRect(x + 20, y + 110, 110, 20);
                g.fillRect(x + 65, y + 50, 20, 50);
                break;
            case 3:
                // 困难关卡 - 复杂障碍物
                g.fillRect(x + 20, y + 20, 20, 110);
                g.fillRect(x + 110, y + 20, 20, 110);
                g.fillRect(x + 50, y + 50, 50, 20);
                g.fillRect(x + 50, y + 100, 50, 20);
                break;
        }

        // 绘制坦克
        drawTank(g, x + 75, y + 130, Color.GREEN, true); // 玩家坦克
        drawTank(g, x + 75, y + 30, Color.RED, false);   // 敌方坦克
    }

    private void drawTank(Graphics g, int x, int y, Color color, boolean isPlayer) {
        g.setColor(color);

        // 坦克主体
        g.fillRect(x - 15, y - 15, 30, 30);

        // 坦克炮管
        if (isPlayer) {
            g.fillRect(x - 2, y - 30, 4, 15); // 向上
        } else {
            g.fillRect(x - 2, y + 15, 4, 15); // 向下
        }
    }
}