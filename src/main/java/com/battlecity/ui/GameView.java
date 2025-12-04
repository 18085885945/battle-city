package com.battlecity.ui;

import com.battlecity.model.*;
import com.battlecity.model.projectile.Bullet;
import com.battlecity.model.tank.EnemyTank;
import com.battlecity.model.tank.PlayerTank;
import com.battlecity.map.TileType;
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

        // 绘制地形（草丛）
        for (TerrainTile terrain : world.terrains()) {
            drawTerrain(terrain);
        }

        // 绘制障碍物（砖块、铁块、水路）
        for (Obstacle obstacle : world.obstacles()) {
            drawObstacle(obstacle);
        }

        // 绘制基地
        drawBase(world.base());

        // 绘制玩家坦克
        if (world.playerTank().alive()) {
            drawTank(world.playerTank(), Color.GREEN);
            // 如果玩家坦克在草丛中，在坦克上方绘制草丛遮住效果
            drawGrassOverTank(world.playerTank());
        }

        // 绘制敌方坦克
        for (EnemyTank enemy : world.enemyTanks()) {
            if (enemy.alive()) {
                drawTank(enemy, Color.RED);
                // 如果敌方坦克在草丛中，在坦克上方绘制草丛遮住效果
                drawGrassOverTank(enemy);
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
        if (terrain.type() == TileType.GRASS) {
            // 草丛：绿色
            gc.setFill(Color.GREEN);
            gc.fillRect(terrain.position().x(), terrain.position().y(), terrain.size().width(), terrain.size().height());
            // 添加一些纹理效果
            gc.setStroke(Color.DARKGREEN);
            gc.setLineWidth(1);
            for (int i = 0; i < 3; i++) {
                double x = terrain.position().x() + (i + 1) * terrain.size().width() / 4;
                gc.strokeLine(x, terrain.position().y(), x, terrain.position().y() + terrain.size().height());
            }
        }
    }

    private void drawObstacle(Obstacle obstacle) {
        if (obstacle instanceof com.battlecity.model.world.BrickWall) {
            // 砖块：橙色/棕色
            gc.setFill(Color.ORANGE);
            gc.fillRect(obstacle.left(), obstacle.top(), obstacle.size().width(), obstacle.size().height());
            // 添加砖块纹理
            gc.setStroke(Color.DARKORANGE);
            gc.setLineWidth(1);
            gc.strokeRect(obstacle.left(), obstacle.top(), obstacle.size().width(), obstacle.size().height());
            // 中间横线
            gc.strokeLine(obstacle.left(), obstacle.top() + obstacle.size().height() / 2, 
                         obstacle.right(), obstacle.top() + obstacle.size().height() / 2);
        } else if (obstacle instanceof com.battlecity.model.world.SteelWall) {
            // 铁块：深灰色
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(obstacle.left(), obstacle.top(), obstacle.size().width(), obstacle.size().height());
            // 添加金属光泽效果
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(1);
            gc.strokeRect(obstacle.left(), obstacle.top(), obstacle.size().width(), obstacle.size().height());
        } else if (obstacle instanceof com.battlecity.model.world.River) {
            // 水路：蓝色
            gc.setFill(Color.BLUE);
            gc.fillRect(obstacle.left(), obstacle.top(), obstacle.size().width(), obstacle.size().height());
            // 添加水波效果
            gc.setStroke(Color.DARKBLUE);
            gc.setLineWidth(1);
            gc.strokeRect(obstacle.left(), obstacle.top(), obstacle.size().width(), obstacle.size().height());
            // 波浪线
            for (int i = 0; i < 2; i++) {
                double y = obstacle.top() + (i + 1) * obstacle.size().height() / 3;
                gc.strokeLine(obstacle.left(), y, obstacle.right(), y);
            }
        } else {
            gc.setFill(Color.BROWN);
            gc.fillRect(obstacle.left(), obstacle.top(), obstacle.size().width(), obstacle.size().height());
        }
    }
    
    /**
     * 如果坦克在草丛中，在坦克上方绘制草丛遮住效果
     */
    private void drawGrassOverTank(com.battlecity.model.tank.Tank tank) {
        for (TerrainTile terrain : world.terrains()) {
            if (terrain.type() == TileType.GRASS && isTankInTerrain(tank, terrain)) {
                // 在坦克上方绘制草丛，遮住坦克
                gc.setFill(Color.GREEN);
                gc.fillRect(terrain.position().x(), terrain.position().y(), terrain.size().width(), terrain.size().height());
                // 添加纹理
                gc.setStroke(Color.DARKGREEN);
                gc.setLineWidth(1);
                for (int i = 0; i < 3; i++) {
                    double x = terrain.position().x() + (i + 1) * terrain.size().width() / 4;
                    gc.strokeLine(x, terrain.position().y(), x, terrain.position().y() + terrain.size().height());
                }
            }
        }
    }
    
    /**
     * 检查坦克是否在地形中（用于草丛遮住效果）
     */
    private boolean isTankInTerrain(com.battlecity.model.tank.Tank tank, TerrainTile terrain) {
        // 检查坦克中心点是否在地形范围内
        Vector2D tankCenter = tank.center();
        return tankCenter.x() >= terrain.position().x() 
            && tankCenter.x() <= terrain.position().x() + terrain.size().width()
            && tankCenter.y() >= terrain.position().y()
            && tankCenter.y() <= terrain.position().y() + terrain.size().height();
    }

    private void drawBase(Base base) {
        double x = base.left();
        double y = base.top();
        double width = base.size().width();
        double height = base.size().height();
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        
        // 绘制基地主体（绿色，带纹理）
        gc.setFill(Color.GREEN);
        gc.fillRect(x, y, width, height);
        
        // 绘制边框
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);
        
        // 绘制内部结构（类似星形标志）
        gc.setFill(Color.YELLOW);
        double starSize = Math.min(width, height) * 0.6;
        // 绘制五角星（简化版：绘制一个圆形和十字）
        gc.fillOval(centerX - starSize / 2, centerY - starSize / 2, starSize, starSize);
        
        // 绘制十字标志
        gc.setFill(Color.RED);
        double crossWidth = starSize * 0.3;
        gc.fillRect(centerX - crossWidth / 2, centerY - starSize / 2, crossWidth, starSize);
        gc.fillRect(centerX - starSize / 2, centerY - crossWidth / 2, starSize, crossWidth);
        
        // 绘制基地边框装饰
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(x + 2, y + 2, width - 4, height - 4);
    }

    private void drawTank(com.battlecity.model.tank.Tank tank, Color color) {
        double x = tank.left();
        double y = tank.top();
        double width = tank.size().width();
        double height = tank.size().height();
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        
        Vector2D direction = tank.facingDirection();
        boolean isVertical = Math.abs(direction.y()) > Math.abs(direction.x());
        
        // 确定坦克方向（上、下、左、右）
        boolean facingUp = direction.y() < -0.5;
        boolean facingDown = direction.y() > 0.5;
        boolean facingLeft = direction.x() < -0.5;
        boolean facingRight = direction.x() > 0.5;
        
        // 绘制坦克主体（带圆角效果）
        gc.setFill(color);
        gc.fillRoundRect(x + 2, y + 2, width - 4, height - 4, 4, 4);
        
        // 绘制坦克边框
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x + 2, y + 2, width - 4, height - 4, 4, 4);
        
        // 绘制履带（上下两条）
        gc.setFill(Color.DARKGRAY);
        if (isVertical) {
            // 垂直方向：左右履带
            gc.fillRect(x, y + 2, 3, height - 4);
            gc.fillRect(x + width - 3, y + 2, 3, height - 4);
            // 履带纹理
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(0.5);
            for (int i = 0; i < 3; i++) {
                double ty = y + 4 + i * (height - 8) / 2;
                gc.strokeLine(x, ty, x + 3, ty);
                gc.strokeLine(x + width - 3, ty, x + width, ty);
            }
        } else {
            // 水平方向：上下履带
            gc.fillRect(x + 2, y, width - 4, 3);
            gc.fillRect(x + 2, y + height - 3, width - 4, 3);
            // 履带纹理
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(0.5);
            for (int i = 0; i < 3; i++) {
                double tx = x + 4 + i * (width - 8) / 2;
                gc.strokeLine(tx, y, tx, y + 3);
                gc.strokeLine(tx, y + height - 3, tx, y + height);
            }
        }
        
        // 绘制炮塔（中心圆形）
        gc.setFill(Color.rgb(
            (int)(color.getRed() * 255 * 0.8),
            (int)(color.getGreen() * 255 * 0.8),
            (int)(color.getBlue() * 255 * 0.8)
        ));
        double turretSize = Math.min(width, height) * 0.5;
        gc.fillOval(centerX - turretSize / 2, centerY - turretSize / 2, turretSize, turretSize);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeOval(centerX - turretSize / 2, centerY - turretSize / 2, turretSize, turretSize);
        
        // 绘制炮管（根据方向）
        double cannonLength = Math.max(width, height) * 0.7;
        double cannonWidth = 4;
        double cannonEndX = centerX + direction.x() * cannonLength;
        double cannonEndY = centerY + direction.y() * cannonLength;
        
        // 计算炮管的角度
        double angle = Math.atan2(direction.y(), direction.x());
        
        // 绘制炮管（矩形，带旋转效果）
        gc.save();
        gc.translate(centerX, centerY);
        gc.rotate(Math.toDegrees(angle));
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(-cannonWidth / 2, -cannonWidth / 2, cannonLength, cannonWidth);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(-cannonWidth / 2, -cannonWidth / 2, cannonLength, cannonWidth);
        gc.restore();
        
        // 绘制炮管口（圆形）
        gc.setFill(Color.BLACK);
        gc.fillOval(cannonEndX - 3, cannonEndY - 3, 6, 6);
    }

    private void drawBullet(Bullet bullet, Color color) {
        double x = bullet.left();
        double y = bullet.top();
        double width = bullet.size().width();
        double height = bullet.size().height();
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        
        Vector2D direction = bullet.direction();
        boolean isVertical = Math.abs(direction.y()) > Math.abs(direction.x());
        
        // 绘制子弹主体（根据方向绘制不同形状）
        if (isVertical) {
            // 垂直方向：绘制椭圆形子弹（上下方向）
            // 外层（阴影/光晕）
            gc.setFill(Color.rgb(
                Math.max(0, (int)(color.getRed() * 255) - 40),
                Math.max(0, (int)(color.getGreen() * 255) - 40),
                Math.max(0, (int)(color.getBlue() * 255) - 40),
                0.5
            ));
            gc.fillOval(centerX - width / 2 - 1, centerY - height / 2 - 2, width + 2, height + 4);
            
            // 主体（椭圆形，垂直方向更长）
            gc.setFill(color);
            gc.fillOval(centerX - width / 2, centerY - height / 2, width, height * 1.5);
            
            // 高光效果
            gc.setFill(Color.WHITE);
            gc.fillOval(centerX - 1, centerY - height / 2 - 1, 2, 3);
        } else {
            // 水平方向：绘制椭圆形子弹（左右方向）
            // 外层（阴影/光晕）
            gc.setFill(Color.rgb(
                Math.max(0, (int)(color.getRed() * 255) - 40),
                Math.max(0, (int)(color.getGreen() * 255) - 40),
                Math.max(0, (int)(color.getBlue() * 255) - 40),
                0.5
            ));
            gc.fillOval(centerX - width / 2 - 2, centerY - height / 2 - 1, width + 4, height + 2);
            
            // 主体（椭圆形，水平方向更长）
            gc.setFill(color);
            gc.fillOval(centerX - width / 2, centerY - height / 2, width * 1.5, height);
            
            // 高光效果
            gc.setFill(Color.WHITE);
            gc.fillOval(centerX - width / 2 - 1, centerY - 1, 3, 2);
        }
        
        // 边框
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(0.5);
        if (isVertical) {
            gc.strokeOval(centerX - width / 2, centerY - height / 2, width, height * 1.5);
        } else {
            gc.strokeOval(centerX - width / 2, centerY - height / 2, width * 1.5, height);
        }
        
        // 绘制方向箭头（可选，增强方向感）
        double arrowSize = 2;
        if (direction.y() < -0.5) { // 向上
            gc.setFill(Color.WHITE);
            double[] xPoints = {centerX, centerX - arrowSize, centerX + arrowSize};
            double[] yPoints = {centerY - height / 2, centerY - height / 2 + arrowSize, centerY - height / 2 + arrowSize};
            gc.fillPolygon(xPoints, yPoints, 3);
        } else if (direction.y() > 0.5) { // 向下
            gc.setFill(Color.WHITE);
            double[] xPoints = {centerX, centerX - arrowSize, centerX + arrowSize};
            double[] yPoints = {centerY + height / 2, centerY + height / 2 - arrowSize, centerY + height / 2 - arrowSize};
            gc.fillPolygon(xPoints, yPoints, 3);
        } else if (direction.x() < -0.5) { // 向左
            gc.setFill(Color.WHITE);
            double[] xPoints = {centerX - width / 2, centerX - width / 2 + arrowSize, centerX - width / 2 + arrowSize};
            double[] yPoints = {centerY, centerY - arrowSize, centerY + arrowSize};
            gc.fillPolygon(xPoints, yPoints, 3);
        } else if (direction.x() > 0.5) { // 向右
            gc.setFill(Color.WHITE);
            double[] xPoints = {centerX + width / 2, centerX + width / 2 - arrowSize, centerX + width / 2 - arrowSize};
            double[] yPoints = {centerY, centerY - arrowSize, centerY + arrowSize};
            gc.fillPolygon(xPoints, yPoints, 3);
        }
    }
}

