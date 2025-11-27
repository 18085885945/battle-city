package com.battlecity.model.tank;

import com.battlecity.model.Entity;
import com.battlecity.model.Size;
import com.battlecity.model.Vector2D;
import com.battlecity.model.projectile.Bullet;

import java.util.Optional;

public abstract class Tank extends Entity {

    private final TankType type;
    protected final TankAttributes attributes;
    private double fireCooldownTimer;
    private int health;
    private Vector2D facingDirection = new Vector2D(0, -1); // 默认向上

    protected Tank(Vector2D position, Size size, TankType type, TankAttributes attributes) {
        super(position, size);
        this.type = type;
        this.attributes = attributes;
        this.health = 1; // 默认生命值
    }

    protected void move(Vector2D direction, double deltaSeconds) {
        if (direction.length() > 0) {
            // 更新面向方向
            facingDirection = direction.normalize();
        }
        position = position.add(direction.scale(attributes.speed() * deltaSeconds));
    }
    
    public Vector2D facingDirection() {
        return facingDirection;
    }
    
    public Vector2D center() {
        return new Vector2D(
            position.x() + size.width() / 2.0,
            position.y() + size.height() / 2.0
        );
    }

    public Optional<Bullet> tryFireInternal() {
        if (fireCooldownTimer > 0) {
            return Optional.empty();
        }
        fireCooldownTimer = attributes.fireCooldownMillis();
        return Optional.of(createBullet());
    }

    public void tick(double deltaSeconds) {
        if (fireCooldownTimer > 0) {
            // fireCooldownTimer是毫秒，deltaSeconds是秒，需要转换为毫秒
            fireCooldownTimer = Math.max(0, fireCooldownTimer - deltaSeconds * 1000);
        }
    }

    protected abstract Bullet createBullet();

    public TankType type() {
        return type;
    }

    public int health() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, health);
    }

    public void takeDamage(int damage) {
        this.health = Math.max(0, this.health - damage);
    }

    public boolean alive() {
        return health > 0;
    }

    public void setPosition(Vector2D newPosition) {
        this.position = newPosition;
    }
}

