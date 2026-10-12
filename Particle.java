package com.train.game;

import java.awt.Color;

public class Particle {
    public enum Type { SMOKE, SPARK, RAIN, FIREFLY, TOAST, POPUP, LIGHTNING }

    public Type type;
    public double x, y;
    public double vx, vy;
    public double age;
    public double life;
    public double size;
    public boolean revving;
    public String text;
    public Color color;
    public boolean active;

    public Particle() {
        this.active = false;
    }

    public void spawnSmoke(double x, double y, boolean revving, double windX) {
        this.type = Type.SMOKE;
        this.x = x;
        this.y = y;
        this.vx = -1.5 + windX + (Math.random() - 0.5) * 0.8;
        this.vy = -1.2 - Math.random() * 0.8;
        this.age = 0;
        this.life = 1.4 + Math.random() * 0.8;
        this.size = revving ? 4.5 : 2.5;
        this.revving = revving;
        this.active = true;
    }

    public void spawnSpark(double x, double y) {
        this.type = Type.SPARK;
        this.x = x;
        this.y = y;
        this.vx = -4.0 - Math.random() * 6.0;
        this.vy = (Math.random() - 0.7) * 4.0;
        this.age = 0;
        this.life = 0.2 + Math.random() * 0.3;
        this.active = true;
    }

    public void spawnRain(double width, double height, double wind) {
        this.type = Type.RAIN;
        this.x = Math.random() * (width + 150) - 50;
        this.y = -10;
        this.vx = -3.0 + wind;
        this.vy = 10.0 + Math.random() * 6.0;
        this.age = 0;
        this.life = 1.2;
        this.active = true;
    }

    public void spawnLightning(double width) {
        this.type = Type.LIGHTNING;
        this.x = 40 + Math.random() * (width - 80);
        this.y = 0;
        this.age = 0;
        this.life = 0.15;
        this.active = true;
    }

    public void spawnToast(String text, Color color, double duration) {
        this.type = Type.TOAST;
        this.text = text;
        this.color = color;
        this.age = 0;
        this.life = duration;
        this.active = true;
    }

    public void spawnPopup(double x, double y, String text, Color color) {
        this.type = Type.POPUP;
        this.x = x;
        this.y = y;
        this.vy = -12.0;
        this.text = text;
        this.color = color;
        this.age = 0;
        this.life = 1.2;
        this.active = true;
    }

    public void update(double dt, double trainSpeed) {
        if (!active) return;
        age += dt;
        if (age >= life) {
            active = false;
            return;
        }

        switch (type) {
            case SMOKE:
                x += (vx - trainSpeed * 0.8) * dt * 30;
                y += vy * dt * 30;
                size += dt * 3.0;
                break;
            case SPARK:
                x += vx * dt * 30;
                y += vy * dt * 30;
                vy += 8.0 * dt;
                break;
            case RAIN:
                x += vx * dt * 30;
                y += vy * dt * 30;
                break;
            case POPUP:
                y += vy * dt;
                break;
            default:
                break;
        }
    }
}
