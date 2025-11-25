//游戏内（测试）
package com.tankbattle.ui;

import com.tankbattle.TankBattleGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private TankBattleGame parent;
    private Timer timer;
    private Tank playerTank;
    private List<Tank> enemyTanks;
    private List<Bullet> bullets;
    private List<Wall> walls;
    private int currentLevel;
    private boolean gameRunning;

    public GamePanel(TankBattleGame parent) {
        this.parent = parent;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        timer = new Timer(16, this); // 约60FPS
        bullets = new ArrayList<>();
        walls = new ArrayList<>();
        enemyTanks = new ArrayList<>();
    }

    public void startLevel(int level) {
        currentLevel = level;
        gameRunning = true;

        // 初始化玩家坦克
        playerTank = new Tank(400, 500, 0, Color.GREEN, true);

        // 清空之前的对象
        bullets.clear();
        walls.clear();
        enemyTanks.clear();

        // 根据关卡设置敌人和障碍物
        setupLevel(level);

        // 启动游戏循环
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    private void setupLevel(int level) {
        switch(level) {
            case 1:
                // 关卡1 - 简单
                for (int i = 0; i < 3; i++) {
                    enemyTanks.add(new Tank(150 + i * 200, 100, 180, Color.RED, false));
                }

                // 添加一些墙壁
                walls.add(new Wall(200, 300, 400, 20));
                break;

            case 2:
                // 关卡2 - 中等
                for (int i = 0; i < 5; i++) {
                    enemyTanks.add(new Tank(100 + i * 150, 100, 180, Color.RED, false));
                }

                // 添加更多墙壁
                walls.add(new Wall(100, 200, 200, 20));
                walls.add(new Wall(500, 200, 200, 20));
                walls.add(new Wall(300, 400, 200, 20));
                break;

            case 3:
                // 关卡3 - 困难
                for (int i = 0; i < 7; i++) {
                    enemyTanks.add(new Tank(80 + i * 100, 100, 180, Color.RED, false));
                }

                // 添加复杂墙壁布局
                walls.add(new Wall(100, 150, 600, 20));
                walls.add(new Wall(100, 450, 600, 20));
                walls.add(new Wall(200, 250, 20, 200));
                walls.add(new Wall(400, 250, 20, 200));
                walls.add(new Wall(600, 250, 20, 200));
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!gameRunning) return;

        // 绘制游戏元素
        playerTank.draw(g);

        for (Tank tank : enemyTanks) {
            tank.draw(g);
        }

        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }

        for (Wall wall : walls) {
            wall.draw(g);
        }

        // 绘制关卡信息和分数
        g.setColor(Color.WHITE);
        g.setFont(new Font("宋体", Font.BOLD, 20));
        g.drawString("关卡: " + currentLevel, 20, 30);
        g.drawString("敌人数量: " + enemyTanks.size(), 20, 60);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameRunning) return;

        // 更新游戏状态
        updateGame();

        // 重绘画面
        repaint();
    }

    private void updateGame() {
        // 更新子弹位置
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.move();

            // 检查子弹是否超出边界
            if (bullet.getX() < 0 || bullet.getX() > getWidth() ||
                    bullet.getY() < 0 || bullet.getY() > getHeight()) {
                bullets.remove(i);
                continue;
            }

            // 检查子弹与坦克的碰撞
            if (bullet.isFromPlayer()) {
                // 玩家子弹击中敌人
                for (int j = enemyTanks.size() - 1; j >= 0; j--) {
                    Tank enemy = enemyTanks.get(j);
                    if (bullet.getBounds().intersects(enemy.getBounds())) {
                        enemyTanks.remove(j);
                        bullets.remove(i);
                        break;
                    }
                }
            } else {
                // 敌人子弹击中玩家
                if (bullet.getBounds().intersects(playerTank.getBounds())) {
                    gameOver(false);
                    return;
                }
            }

            // 检查子弹与墙壁的碰撞
            for (int j = walls.size() - 1; j >= 0; j--) {
                Wall wall = walls.get(j);
                if (bullet.getBounds().intersects(wall.getBounds())) {
                    bullets.remove(i);
                    break;
                }
            }
        }

        // 检查是否通关
        if (enemyTanks.isEmpty()) {
            if (currentLevel < 3) {
                JOptionPane.showMessageDialog(this, "恭喜通过关卡 " + currentLevel + "!");
                startLevel(currentLevel + 1);
            } else {
                gameOver(true);
            }
        }

        // 敌人AI - 随机移动和射击
        for (Tank enemy : enemyTanks) {
            if (Math.random() < 0.02) { // 2%的几率改变方向
                enemy.setDirection((int)(Math.random() * 4) * 90);
            }

            enemy.move();

            // 确保敌人不超出边界
            if (enemy.getX() < 0) enemy.setX(0);
            if (enemy.getX() > getWidth() - 30) enemy.setX(getWidth() - 30);
            if (enemy.getY() < 0) enemy.setY(0);
            if (enemy.getY() > getHeight() - 30) enemy.setY(getHeight() - 30);

            // 随机射击
            if (Math.random() < 0.01) { // 1%的几率射击
                bullets.add(enemy.fire());
            }
        }
    }

    private void gameOver(boolean win) {
        gameRunning = false;
        timer.stop();

        String message = win ? "恭喜你通关了所有关卡!" : "游戏结束! 你被击中了!";
        int option = JOptionPane.showConfirmDialog(this, message + "\n是否返回主菜单?",
                "游戏结束", JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            parent.showMenu();
        } else {
            System.exit(0);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameRunning) return;

        switch(e.getKeyCode()) {
            case KeyEvent.VK_UP:
                playerTank.setDirection(0);
                playerTank.setMoving(true);
                break;
            case KeyEvent.VK_RIGHT:
                playerTank.setDirection(90);
                playerTank.setMoving(true);
                break;
            case KeyEvent.VK_DOWN:
                playerTank.setDirection(180);
                playerTank.setMoving(true);
                break;
            case KeyEvent.VK_LEFT:
                playerTank.setDirection(270);
                playerTank.setMoving(true);
                break;
            case KeyEvent.VK_SPACE:
                bullets.add(playerTank.fire());
                break;
            case KeyEvent.VK_ESCAPE:
                parent.showMenu();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!gameRunning) return;

        switch(e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_LEFT:
                playerTank.setMoving(false);
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
