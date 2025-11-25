//子弹（测试）
package com.tankbattle.ui;

import java.awt.*;

public class Bullet {
    private int x, y;
    private int direction;
    private boolean fromPlayer;
    private int speed = 5;

    public Bullet(int x, int y, int direction, boolean fromPlayer) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.fromPlayer = fromPlayer;
    }

    public void draw(Graphics g) {
        g.setColor(fromPlayer ? Color.YELLOW : Color.ORANGE);
        g.fillOval(x - 3, y - 3, 6, 6);
    }

    public void move() {
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

    public Rectangle getBounds() {
        return new Rectangle(x - 3, y - 3, 6, 6);
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isFromPlayer() { return fromPlayer; }
}