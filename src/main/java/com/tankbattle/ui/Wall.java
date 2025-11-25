//地形（测试）
package com.tankbattle.ui;

import java.awt.*;

public class Wall {
    private int x, y, width, height;

    public Wall(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(x, y, width, height);

        // 墙壁纹理
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i < width; i += 10) {
            g.drawLine(x + i, y, x + i, y + height);
        }
        for (int i = 0; i < height; i += 10) {
            g.drawLine(x, y + i, x + width, y + i);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
