package com.battlecity.engine.loop;

import javafx.animation.AnimationTimer;

import java.util.function.DoubleConsumer;

/**
 * 基于 {@link AnimationTimer} 的主循环，提供目标 FPS 控制。
 */
public class GameLoop {

    private final DoubleConsumer onTick;
    private final double targetFrameTimeNanos;
    private AnimationTimer timer;
    private boolean paused;

    public GameLoop(DoubleConsumer onTick, int targetFps) {
        this.onTick = onTick;
        this.targetFrameTimeNanos = 1_000_000_000.0 / Math.max(1, targetFps);
    }

    public void start() {
        if (timer != null) {
            return;
        }
        timer = new AnimationTimer() {
            private long lastUpdate = -1;

            @Override
            public void handle(long now) {
                if (paused) {
                    lastUpdate = now;
                    return;
                }
                if (lastUpdate < 0) {
                    lastUpdate = now;
                    return;
                }
                double delta = (now - lastUpdate) / 1_000_000_000.0;
                if ((now - lastUpdate) >= targetFrameTimeNanos) {
                    onTick.accept(delta);
                    lastUpdate = now;
                }
            }
        };
        timer.start();
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }
}

