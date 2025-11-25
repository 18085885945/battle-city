//坦克控制（测试）
package com.tankbattle.ui;

import java.awt.*;

public class Tank {
    private int x, y;
    private int direction; // 0=上, 90=右, 180=下, 270=左
    private Color color;
    private boolean isPlayer;
    private boolean moving;

    public Tank(int x, int y, int direction, Color color, boolean isPlayer) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.color = color;
        this.isPlayer = isPlayer;
        this.moving = false;
    }

    public void draw(Graphics g) {
        g.setColor(color);

        // 坦克主体
        g.fillRect(x - 15, y - 15, 30, 30);

        // 坦克炮管
        switch(direction) {
            case 0: // 上
                g.fillRect(x - 2, y - 30, 4, 15);
                break;
            case 90: // 右
                g.fillRect(x + 15, y - 2, 15, 4);
                break;
            case 180: // 下
                g.fillRect(x - 2, y + 15, 4, 15);
                break;
            case 270: // 左
                g.fillRect(x - 30, y - 2, 15, 4);
                break;
        }

        // 坦克履带标记
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x - 15, y - 15, 30, 5);
        g.fillRect(x - 15, y + 10, 30, 5);
    }

    public void move() {
        if (!moving) return;

        int speed = 2;
        switch(direction) {
            case 0: // 上
                y -= speed;
                break;
            case 90: // 右
                x += speed;
                break;
            case 180: // 下
                y += speed;
                break;
            case 270: // 左
                x -= speed;
                break;
        }
    }

    public Bullet fire() {
        int bulletX = x;
        int bulletY = y;

        switch(direction) {
            case 0: // 上
                bulletY -= 30;
                break;
            case 90: // 右
                bulletX += 30;
                break;
            case 180: // 下
                bulletY += 30;
                break;
            case 270: // 左
                bulletX -= 30;
                break;
        }

        return new Bullet(bulletX, bulletY, direction, isPlayer);
    }

    public Rectangle getBounds() {
        return new Rectangle(x - 15, y - 15, 30, 30);
    }

    // Getters and Setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getDirection() { return direction; }
    public void setDirection(int direction) { this.direction = direction; }
    public boolean isMoving() { return moving; }
    public void setMoving(boolean moving) { this.moving = moving; }
}