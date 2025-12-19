package com.battlecity.ui;

import com.battlecity.model.*;
import com.battlecity.model.projectile.Bullet;
import com.battlecity.model.tank.EnemyTank;
import com.battlecity.model.tank.EnemyTier;
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
        if (world.playerTank() != null && world.playerTank().alive()) {
            drawTank(world.playerTank(), Color.GREEN);
            // 如果玩家坦克在草丛中，在坦克上方绘制草丛遮住效果
            drawGrassOverTank(world.playerTank());
        }

        // 绘制敌方坦克
        for (EnemyTank enemy : world.enemyTanks()) {
            if (enemy.alive()) {
                // 根据精英怪类型选择不同颜色，与玩家坦克（绿色）区分
                Color enemyColor;
                switch (enemy.tier()) {
                    case ELITE_SPEED:      // 速度精英怪
                        enemyColor = Color.BLUE;
                        break;
                    case ELITE_FIRERATE:  // 射速精英怪
                        enemyColor = Color.PURPLE;
                        break;
                    case ELITE_AI:         // 高级AI精英怪
                        enemyColor = Color.ORANGE;
                        break;
                    case ELITE_HEALTH:     // 生命精英怪
                        enemyColor = Color.RED;
                        break;
                    case ELITE:           // 普通精英怪（旧类型，保持兼容）
                        enemyColor = Color.MAGENTA;
                        break;
                    default:              // 普通坦克
                        enemyColor = Color.GRAY;
                }
                // 如果被冻结，使用半透明效果
                if (enemy.isFrozen()) {
                    gc.setGlobalAlpha(0.5);
                    drawTank(enemy, Color.LIGHTBLUE);
                    gc.setGlobalAlpha(1.0);
                } else {
                    drawTank(enemy, enemyColor);
                }
                // 如果敌方坦克在草丛中，在坦克上方绘制草丛遮住效果
                drawGrassOverTank(enemy);
            }
        }

        // 绘制玩家子弹
        for (Bullet bullet : world.playerBullets()) {
            if (bullet.alive()) {
                // 秒杀模式子弹特殊颜色
                Color bulletColor = bullet.isOneShotMode() ? Color.RED : Color.YELLOW;
                drawBullet(bullet, bulletColor);
            }
        }

        // 绘制敌方子弹
        for (Bullet bullet : world.enemyBullets()) {
            if (bullet.alive()) {
                drawBullet(bullet, Color.ORANGE);
            }
        }
        
        // 绘制道具
        drawPowerUps();
        
        // 绘制道具效果状态
        drawPowerUpEffects();
    }
    
    /**
     * 绘制所有活跃的道具
     */
    private void drawPowerUps() {
        // 由于我们无法直接访问world中的powerUps列表（它是private的），
        // 我们需要通过反射来获取它
        try {
            java.lang.reflect.Field field = GameWorld.class.getDeclaredField("powerUps");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.List<com.battlecity.model.powerup.PowerUp> powerUps = (java.util.List<com.battlecity.model.powerup.PowerUp>) field.get(world);
            
            for (com.battlecity.model.powerup.PowerUp powerUp : powerUps) {
                if (powerUp.isAlive()) {
                    drawPowerUp(powerUp);
                }
            }
        } catch (Exception e) {
            // 忽略反射异常，不绘制道具
        }
    }
    
    /**
     * 绘制单个道具
     */
    private void drawPowerUp(com.battlecity.model.powerup.PowerUp powerUp) {
        double x = powerUp.left();
        double y = powerUp.top();
        double width = powerUp.size().width();
        double height = powerUp.size().height();
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        
        // 根据道具类型选择颜色
        Color color;
        switch (powerUp.getType()) {
            case BULLET_SPEED:
                color = Color.CYAN;
                break;
            case BULLET_PENETRATION:
                color = Color.MAGENTA;
                break;
            case ONE_SHOT:
                color = Color.RED;
                break;
            case TANK_SPEED:
                color = Color.GREEN;
                break;
            case HEALTH_RESTORE:
                color = Color.YELLOW;
                break;
            case FREEZE:
                color = Color.LIGHTBLUE;
                break;
            default:
                color = Color.WHITE;
        }
        
        // 绘制道具主体
        gc.setFill(color);
        gc.fillOval(x, y, width, height);
        
        // 绘制道具边框
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeOval(x, y, width, height);
        
        // 绘制道具图标
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(12));
        String icon;
        switch (powerUp.getType()) {
            case BULLET_SPEED:
                icon = "B";
                break;
            case BULLET_PENETRATION:
                icon = "P";
                break;
            case ONE_SHOT:
                icon = "S";
                break;
            case TANK_SPEED:
                icon = "T";
                break;
            case HEALTH_RESTORE:
                icon = "H";
                break;
            case FREEZE:
                icon = "F";
                break;
            default:
                icon = "?";
        }
        gc.fillText(icon, centerX - 4, centerY + 4);
    }
    
    /**
     * 绘制当前激活的道具效果
     */
    private void drawPowerUpEffects() {
        if (world.playerTank() == null || !world.playerTank().alive()) {
            return;
        }
        
        // 获取当前激活的道具效果
        java.util.List<com.battlecity.model.powerup.PowerUpEffect> effects = world.playerTank().getActiveEffects();
        if (effects.isEmpty()) {
            return;
        }
        
        // 在屏幕右上角绘制效果
        double x = getWidth() - 150;
        double y = 10;
        double height = 30;
        
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(x - 5, y - 5, 140, effects.size() * height + 10);
        
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRect(x - 5, y - 5, 140, effects.size() * height + 10);
        
        gc.setFont(javafx.scene.text.Font.font(12));
        
        for (int i = 0; i < effects.size(); i++) {
            com.battlecity.model.powerup.PowerUpEffect effect = effects.get(i);
            double effectY = y + i * height;
            
            // 绘制效果类型
            String effectName;
            switch (effect.getType()) {
                case BULLET_SPEED:
                    effectName = "Bullet Speed";
                    break;
                case BULLET_PENETRATION:
                    effectName = "Penetration";
                    break;
                case ONE_SHOT:
                    effectName = "One Shot";
                    break;
                case TANK_SPEED:
                    effectName = "Tank Speed";
                    break;
                case HEALTH_RESTORE:
                    effectName = "Health";
                    break;
                case FREEZE:
                    effectName = "Freeze";
                    break;
                default:
                    effectName = "Unknown";
            }
            
            // 绘制效果名称和剩余时间
            gc.setFill(Color.WHITE);
            gc.fillText(effectName, x, effectY + 15);
            
            // 绘制剩余时间进度条
            double progress = effect.getRemainingTime() / effect.getDuration();
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(x, effectY + 20, 130, 5);
            gc.setFill(Color.GREEN);
            gc.fillRect(x, effectY + 20, 130 * progress, 5);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(0.5);
            gc.strokeRect(x, effectY + 20, 130, 5);
            
            // 绘制剩余时间文本
            int remainingSeconds = (int) Math.ceil(effect.getRemainingTime());
            gc.setFill(Color.WHITE);
            gc.fillText(remainingSeconds + "s", x + 110, effectY + 15);
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
     * 如果坦克与草丛有重叠，在重叠区域绘制草丛遮住效果
     * 无论进入的部分有多少，都会遮住进入的部分（包括炮管）
     */
    private void drawGrassOverTank(com.battlecity.model.tank.Tank tank) {
        for (TerrainTile terrain : world.terrains()) {
            if (terrain.type() == TileType.GRASS) {
                // 获取草丛边界
                double grassLeft = terrain.position().x();
                double grassRight = terrain.position().x() + terrain.size().width();
                double grassTop = terrain.position().y();
                double grassBottom = terrain.position().y() + terrain.size().height();
                
                // 1. 检查坦克主体与草丛的重叠
                double tankLeft = tank.left();
                double tankRight = tank.right();
                double tankTop = tank.top();
                double tankBottom = tank.bottom();
                
                double overlapLeft = Math.max(tankLeft, grassLeft);
                double overlapRight = Math.min(tankRight, grassRight);
                double overlapTop = Math.max(tankTop, grassTop);
                double overlapBottom = Math.min(tankBottom, grassBottom);
                
                if (overlapLeft < overlapRight && overlapTop < overlapBottom) {
                    drawGrassOverlap(overlapLeft, overlapTop, overlapRight - overlapLeft, overlapBottom - overlapTop,
                                   grassLeft, grassTop, terrain.size().width(), terrain.size().height());
                }
                
                // 2. 检查炮管与草丛的重叠
                double centerX = tank.left() + tank.size().width() / 2;
                double centerY = tank.top() + tank.size().height() / 2;
                Vector2D direction = tank.facingDirection();
                double cannonLength = Math.max(tank.size().width(), tank.size().height()) * 0.7;
                double cannonWidth = 4;
                double cannonEndX = centerX + direction.x() * cannonLength;
                double cannonEndY = centerY + direction.y() * cannonLength;
                
                // 计算炮管的边界框（考虑炮管宽度）
                // 炮管是旋转的，需要计算旋转后的边界框
                double angle = Math.atan2(direction.y(), direction.x());
                double cosAngle = Math.abs(Math.cos(angle));
                double sinAngle = Math.abs(Math.sin(angle));
                
                // 炮管旋转后的宽度和高度
                double rotatedWidth = cannonLength * cosAngle + cannonWidth * sinAngle;
                double rotatedHeight = cannonLength * sinAngle + cannonWidth * cosAngle;
                
                // 炮管边界框的中心点（炮管中点）
                double cannonCenterX = (centerX + cannonEndX) / 2;
                double cannonCenterY = (centerY + cannonEndY) / 2;
                
                // 炮管边界框
                double cannonLeft = cannonCenterX - rotatedWidth / 2;
                double cannonRight = cannonCenterX + rotatedWidth / 2;
                double cannonTop = cannonCenterY - rotatedHeight / 2;
                double cannonBottom = cannonCenterY + rotatedHeight / 2;
                
                // 计算炮管与草丛的重叠
                overlapLeft = Math.max(cannonLeft, grassLeft);
                overlapRight = Math.min(cannonRight, grassRight);
                overlapTop = Math.max(cannonTop, grassTop);
                overlapBottom = Math.min(cannonBottom, grassBottom);
                
                if (overlapLeft < overlapRight && overlapTop < overlapBottom) {
                    drawGrassOverlap(overlapLeft, overlapTop, overlapRight - overlapLeft, overlapBottom - overlapTop,
                                   grassLeft, grassTop, terrain.size().width(), terrain.size().height());
                }
            }
        }
    }
    
    /**
     * 绘制草丛重叠区域
     */
    private void drawGrassOverlap(double x, double y, double width, double height,
                                  double grassLeft, double grassTop, double grassWidth, double grassHeight) {
        // 绘制重叠区域的草丛
        gc.setFill(Color.GREEN);
        gc.fillRect(x, y, width, height);
        
        // 添加纹理效果
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(1);
        // 绘制纹理线，但只绘制在重叠区域内的部分
        for (int i = 0; i < 3; i++) {
            double textureX = grassLeft + (i + 1) * grassWidth / 4;
            // 如果纹理线在重叠区域内，绘制它
            if (textureX >= x && textureX <= x + width) {
                gc.strokeLine(textureX, y, textureX, y + height);
            }
        }
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
        
        // 炮管口不再绘制黑点（已移除）
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

