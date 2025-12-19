package com.battlecity.audio;

import javafx.scene.media.AudioClip;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 音频管理器，负责加载和播放游戏音效
 */
public class AudioManager {
    // 单例实例
    private static AudioManager instance;
    
    // 音效缓存
    private final Map<String, AudioClip> soundCache = new HashMap<>();
    
    // 私有构造函数
    private AudioManager() {}
    
    /**
     * 获取音频管理器单例实例
     * @return AudioManager 实例
     */
    public static AudioManager getInstance() {
        if (instance == null) {
            synchronized (AudioManager.class) {
                if (instance == null) {
                    instance = new AudioManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 加载音效文件
     * @param soundName 音效名称
     * @param resourcePath 资源路径
     */
    public void loadSound(String soundName, String resourcePath) {
        try {
            URL resource = getClass().getResource(resourcePath);
            if (resource != null) {
                AudioClip clip = new AudioClip(resource.toString());
                soundCache.put(soundName, clip);
            } else {
                System.err.println("音效文件未找到: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("无法加载音效: " + soundName + ", 路径: " + resourcePath);
            e.printStackTrace();
        }
    }
    
    /**
     * 播放音效
     * @param soundName 音效名称
     */
    public void playSound(String soundName) {
        AudioClip clip = soundCache.get(soundName);
        if (clip != null) {
            clip.play();
        }
    }
    
    /**
     * 播放音效（带音量控制）
     * @param soundName 音效名称
     * @param volume 音量 (0.0-1.0)
     */
    public void playSound(String soundName, double volume) {
        AudioClip clip = soundCache.get(soundName);
        if (clip != null) {
            clip.setVolume(volume);
            clip.play();
        }
    }
    
    /**
     * 预加载所有游戏音效
     */
    public void preloadSounds() {
        loadSound("fire", "/sounds/fire.wav");
        loadSound("explosion", "/sounds/explosion.wav");
        loadSound("powerup", "/sounds/powerup.wav");
        loadSound("menu_select", "/sounds/menu_select.wav");
        loadSound("menu_confirm", "/sounds/menu_confirm.wav");
        loadSound("game_start", "/sounds/game_start.wav");
        loadSound("game_over", "/sounds/game_over.wav");
        loadSound("victory", "/sounds/victory.wav");
        loadSound("hit_brick", "/sounds/hit_brick.wav");
        loadSound("hit_steel", "/sounds/hit_steel.wav");
        loadSound("base_destroyed", "/sounds/base_destroyed.wav");
    }
}