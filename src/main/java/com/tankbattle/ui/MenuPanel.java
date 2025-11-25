//主界面UI
package com.tankbattle.ui;

import com.tankbattle.TankBattleGame;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public class MenuPanel extends JPanel {
    private TankBattleGame parent;

    public MenuPanel(TankBattleGame parent) {
        this.parent = parent;
        setPreferredSize(new Dimension(800, 600));
        setLayout(new BorderLayout());

        // 标题
        JLabel titleLabel = new JLabel("坦克大战", JLabel.CENTER);
        titleLabel.setFont(new Font("华文行楷", Font.BOLD, 60));
        titleLabel.setForeground(new Color(178, 34, 34));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 100, 100, 100));

        JButton startButton = createStyledButton("开始游戏", new Color(70, 130, 180));
        JButton helpButton = createStyledButton("操作指南", new Color(46, 139, 87));
        JButton exitButton = createStyledButton("退出游戏", new Color(178, 34, 34));

        // 设置按钮监听器
        startButton.addActionListener(e -> parent.showLevelSelect());
        helpButton.addActionListener(e -> parent.showHelp());
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(startButton);
        buttonPanel.add(helpButton);
        buttonPanel.add(exitButton);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(buttonPanel);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(baseColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(baseColor.brighter());
                } else {
                    g2.setColor(baseColor);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("微软雅黑", Font.BOLD, 24));

                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getHeight();

                g2.drawString(getText(),
                        getWidth()/2 - textWidth/2,
                        getHeight()/2 + textHeight/4);

                g2.dispose();
            }
        };

        button.setPreferredSize(new Dimension(250, 70));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制渐变背景
        GradientPaint gradient = new GradientPaint(0, 0, new Color(240, 248, 255),
                getWidth(), getHeight(), new Color(230, 230, 250));
        g2.setPaint(gradient);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 绘制战场环境
        drawBattlefield(g2);

        // 绘制精美的坦克
        drawBeautifulTank(g2, 150, 150, new Color(34, 139, 34), true);
        drawBeautifulTank(g2, 600, 150, new Color(178, 34, 34), false);
        drawBeautifulTank(g2, 100, 400, new Color(70, 130, 180), true);
        drawBeautifulTank(g2, 650, 400, new Color(139, 69, 19), false);

        // 绘制装饰边框
        g2.setColor(new Color(70, 130, 180, 100));
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(20, 20, getWidth()-40, getHeight()-40, 20, 20);
    }

    private void drawBattlefield(Graphics2D g2) {
        // 绘制地面纹理
        g2.setColor(new Color(139, 69, 19, 30));
        for (int i = 0; i < getWidth(); i += 40) {
            for (int j = 100; j < getHeight(); j += 40) {
                g2.fillRect(i, j, 20, 20);
            }
        }

        // 绘制障碍物轮廓
        g2.setColor(new Color(128, 128, 128, 80));
        g2.fillRect(300, 200, 60, 60);
        g2.fillRect(450, 300, 80, 40);
        g2.fillRect(200, 350, 50, 70);

        // 绘制爆炸效果
        g2.setColor(new Color(255, 165, 0, 60));
        for (int i = 0; i < 5; i++) {
            int x = 350 + i * 80;
            int y = 250;
            drawExplosion(g2, x, y);
        }
    }

    private void drawExplosion(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(255, 165, 0, 80));
        g2.fillOval(x-10, y-10, 20, 20);
        g2.setColor(new Color(255, 69, 0, 60));
        g2.fillOval(x-15, y-15, 30, 30);
    }

    private void drawBeautifulTank(Graphics2D g2, int x, int y, Color color, boolean isPlayer) {
        // 保存原始变换
        java.awt.geom.AffineTransform originalTransform = g2.getTransform();

        // 坦克主体 - 圆角矩形
        RoundRectangle2D body = new RoundRectangle2D.Double(x - 20, y - 15, 40, 30, 10, 10);

        // 坦克炮塔 - 圆形
        Ellipse2D turret = new Ellipse2D.Double(x - 12, y - 12, 24, 24);

        // 设置坦克颜色渐变
        GradientPaint tankGradient = new GradientPaint(
                x - 20, y - 15, color.brighter(),
                x + 20, y + 15, color.darker()
        );

        // 绘制坦克主体
        g2.setPaint(tankGradient);
        g2.fill(body);

        // 绘制炮塔
        g2.setPaint(tankGradient);
        g2.fill(turret);

        // 绘制炮管
        g2.setColor(color.darker().darker());
        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (isPlayer) {
            g2.drawLine(x, y, x, y - 25); // 玩家坦克炮管向上
        } else {
            g2.drawLine(x, y, x, y + 25); // 敌方坦克炮管向下
        }

        // 绘制履带
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(8));
        g2.drawLine(x - 25, y - 15, x - 25, y + 15); // 左履带
        g2.drawLine(x + 25, y - 15, x + 25, y + 15); // 右履带

        // 绘制履带纹理
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        for (int i = -12; i <= 12; i += 6) {
            g2.drawLine(x - 25, y + i, x - 20, y + i);
            g2.drawLine(x + 20, y + i, x + 25, y + i);
        }

        // 绘制坦克细节
        g2.setColor(new Color(30, 30, 30, 150));
        g2.draw(body);
        g2.draw(turret);

        // 绘制观察窗
        g2.setColor(new Color(100, 149, 237, 180)); // 矢车菊蓝
        Ellipse2D window = new Ellipse2D.Double(x - 6, y - 6, 12, 12);
        g2.fill(window);

        // 恢复原始变换
        g2.setTransform(originalTransform);

        // 绘制坦克阴影
        g2.setColor(new Color(0, 0, 0, 40));
        Ellipse2D shadow = new Ellipse2D.Double(x - 18, y + 10, 36, 10);
        g2.fill(shadow);
    }
}