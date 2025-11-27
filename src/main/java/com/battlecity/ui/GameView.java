package com.battlecity.ui;

import com.battlecity.model.*;
import com.battlecity.model.projectile.Bullet;
import com.battlecity.model.tank.EnemyTank;
import com.battlecity.model.tank.PlayerTank;
import com.battlecity.model.world.Base;
import com.battlecity.model.world.Obstacle;
import com.battlecity.model.world.TerrainTile;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 游戏场景渲染视图
 */
public class GameView extends Canvas {

    private final GraphicsContext gc;
    private GameWorld world;
    private AnimationTimer renderTimer;

    public GameView(double width, double height) {
        super(width, height);
        this.gc = getGraphicsContext2D();
        // 设置Canvas不可聚焦，让父容器接收键盘事件
        setFocusTraversable(false);
    }

    public void bindWorld(GameWorld world) {
        this.world = world;
        startRendering();
    }

    private void startRendering() {
        if (renderTimer != null) {
            renderTimer.stop();
        }
        renderTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render();
            }
        };
        renderTimer.start();
    }

    public void stopRendering() {
        if (renderTimer != null) {
            renderTimer.stop();
        }
    }

    private void render() {
        if (world == null) {
            return;
        }

        // 清空画布
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, getWidth(), getHeight());

        // 绘制地形
        for (TerrainTile terrain : world.terrains()) {
            drawTerrain(terrain);
        }

        // 绘制障碍物
        for (Obstacle obstacle : world.obstacles()) {
            drawObstacle(obstacle);
        }

        // 绘制基地
        drawBase(world.base());

        // 绘制玩家坦克
        if (world.playerTank().alive()) {
            drawTank(world.playerTank(), Color.GREEN);
        }

        // 绘制敌方坦克
        for (EnemyTank enemy : world.enemyTanks()) {
            if (enemy.alive()) {
                drawTank(enemy, Color.RED);
            }
        }

        // 绘制玩家子弹
        for (Bullet bullet : world.playerBullets()) {
            if (bullet.alive()) {
                drawBullet(bullet, Color.YELLOW);
            }
        }

        // 绘制敌方子弹
        for (Bullet bullet : world.enemyBullets()) {
            if (bullet.alive()) {
                drawBullet(bullet, Color.ORANGE);
            }
        }
    }

    private void drawTerrain(TerrainTile terrain) {
        gc.setFill(Color.BLUE);
        gc.fillRect(terrain.position().x(), terrain.position().y(), terrain.size().width(), terrain.size().height());
    }

    private void drawObstacle(Obstacle obstacle) {
        if (obstacle instanceof com.battlecity.model.world.BrickWall) {
            gc.setFill(Color.ORANGE);
        } else if (obstacle instanceof com.battlecity.model.world.SteelWall) {
            gc.setFill(Color.GRAY);
        } else {
            gc.setFill(Color.BROWN);
        }
        gc.fillRect(obstacle.left(), obstacle.top(), obstacle.size().width(), obstacle.size().height());
    }

    private void drawBase(Base base) {
        gc.setFill(Color.GREEN);
        gc.fillRect(base.left(), base.top(), base.size().width(), base.size().height());
    }

    private void drawTank(com.battlecity.model.tank.Tank tank, Color color) {
        double x = tank.left();
        double y = tank.top();
        double width = tank.size().width();
        double height = tank.size().height();
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        
        // 绘制坦克主体
        gc.setFill(color);
        gc.fillRect(x, y, width, height);
        
        // 绘制坦克边框
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, width, height);
        
        // 绘制炮管（根据方向）
        Vector2D direction = tank.facingDirection();
        double cannonLength = width * 0.6;
        double cannonEndX = centerX + direction.x() * cannonLength;
        double cannonEndY = centerY + direction.y() * cannonLength;
        
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(3);
        gc.strokeLine(centerX, centerY, cannonEndX, cannonEndY);
        
        // 绘制中心点（方向指示）
        gc.setFill(Color.WHITE);
        gc.fillOval(centerX - 2, centerY - 2, 4, 4);
    }

    private void drawBullet(Bullet bullet, Color color) {
        gc.setFill(color);
        gc.fillOval(bullet.left(), bullet.top(), bullet.size().width(), bullet.size().height());
    }
}

