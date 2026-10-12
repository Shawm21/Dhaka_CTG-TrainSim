package com.train.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;

public class PixelRenderer {
    public static final int W = 320;
    public static final int H = 180;

    // Palette
    public static final Color C_SKY_DAY = new Color(125, 185, 235);
    public static final Color C_SKY_DAWN = new Color(230, 150, 130);
    public static final Color C_SKY_DUSK = new Color(210, 100, 75);
    public static final Color C_SKY_NIGHT = new Color(15, 20, 45);
    public static final Color C_SKY_STORM = new Color(30, 35, 50);

    public static final Color C_GREEN_DARK = new Color(30, 95, 45);
    public static final Color C_GREEN_PADDY = new Color(85, 175, 75);
    public static final Color C_GREEN_FIELD_ALT = new Color(75, 160, 65);

    public static final Color C_TRACK_BALLAST = new Color(85, 85, 90);
    public static final Color C_TRACK_SLEEPER = new Color(65, 48, 32);
    public static final Color C_RAIL = new Color(185, 190, 195);

    // Blue Locomotive
    public static final Color C_LOCO_BLUE = new Color(27, 74, 136);
    public static final Color C_LOCO_YELLOW = new Color(230, 184, 40);
    public static final Color C_LOCO_CAB = new Color(50, 70, 100);

    // Green Coaches
    public static final Color C_COACH_GREEN = new Color(28, 116, 66);
    public static final Color C_COACH_RED = new Color(214, 40, 40);
    public static final Color C_COACH_YELLOW = new Color(230, 184, 40);
    public static final Color C_COACH_ROOF = new Color(192, 197, 204);

    public static final Color C_HUD_BG = new Color(18, 22, 32, 230);
    public static final Color C_HUD_BORDER = new Color(55, 70, 95);
    public static final Color C_GLOW_YELLOW = new Color(255, 230, 100);
    public static final Color C_GLOW_GREEN = new Color(100, 240, 120);

    private final BufferedImage pixelBuffer;
    private final Graphics2D g2;

    public PixelRenderer() {
        pixelBuffer = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        g2 = pixelBuffer.createGraphics();
    }

    public BufferedImage render(TrainState st) {
        drawSky(st);
        drawBackgroundScenery(st);
        drawTelegraphPolesAndWires(st);
        drawTrackAndBridges(st);
        drawTrainWithWheels(st);
        drawFrontsidedStations(st);
        drawParticles(st);
        drawLighting(st);
        drawHUD(st);

        if (!st.journeyComplete) {
            drawStationBanner(st);
        }

        if (st.titleScreen) {
            drawTitleCard(st);
        } else if (st.journeyComplete) {
            drawScorePanel(st);
        }

        return pixelBuffer;
    }

    private void drawSky(TrainState st) {
        Color skyColor = C_SKY_DAY;
        if (st.thunderstorm) {
            skyColor = st.lightningFlash ? new Color(220, 230, 255) : C_SKY_STORM;
        } else if (st.hour >= 5.0 && st.hour < 7.0) {
            skyColor = C_SKY_DAWN;
        } else if (st.hour >= 7.0 && st.hour < 18.0) {
            skyColor = C_SKY_DAY;
        } else if (st.hour >= 18.0 && st.hour < 19.5) {
            skyColor = C_SKY_DUSK;
        } else {
            skyColor = C_SKY_NIGHT;
        }

        g2.setColor(skyColor);
        g2.fillRect(0, 0, W, H);

        if (!st.thunderstorm) {
            if (skyColor == C_SKY_NIGHT) {
                g2.setColor(new Color(240, 240, 210));
                g2.fillOval(250, 22, 14, 14);
                g2.setColor(C_SKY_NIGHT);
                g2.fillOval(247, 20, 12, 12);
            } else if (skyColor == C_SKY_DAWN) {
                g2.setColor(new Color(255, 215, 120));
                g2.fillOval(55, 45, 16, 16);
            } else if (skyColor == C_SKY_DUSK) {
                g2.setColor(new Color(255, 110, 60));
                g2.fillOval(240, 48, 18, 18);
            } else {
                g2.setColor(new Color(255, 235, 140));
                g2.fillOval(55, 28, 16, 16);
            }
        }

        g2.setColor(st.thunderstorm ? new Color(40, 45, 60, 200) : new Color(255, 255, 255, 200));
        double cloudOffset = (st.tripTime * 3.5) % (W * 2);
        for (int i = 0; i < 5; i++) {
            int cx = (int) ((i * 110 + cloudOffset) % (W + 80)) - 40;
            int cy = 15 + (i % 3) * 14;
            g2.fillRoundRect(cx, cy, 40, 12, 8, 8);
            g2.fillRoundRect(cx + 8, cy - 5, 24, 10, 6, 6);
        }
    }

    private void drawBackgroundScenery(TrainState st) {
        double camX = st.dist;

        g2.setColor(C_GREEN_DARK);
        for (int x = 0; x < W; x += 4) {
            int hillH = (int) (14 + Math.sin((x + camX * 0.1) * 0.02) * 8 + Math.cos((x + camX * 0.04) * 0.03) * 6);
            g2.fillRect(x, H - 92 - hillH, 4, hillH + 40);
        }

        g2.setColor(C_GREEN_PADDY);
        g2.fillRect(0, H - 75, W, 48);

        g2.setColor(C_GREEN_FIELD_ALT);
        for (int x = -20; x < W + 20; x += 32) {
            int gx = (int) (x - (camX * 0.4) % 32);
            g2.fillRect(gx, H - 65, 16, 20);
        }

        for (int i = 0; i < 4; i++) {
            double px = (i * 450 - camX * 0.5) % (W * 2.5);
            if (px < -60) px += W * 2.5;
            if (px < W + 40) {
                int pondX = (int) px;
                g2.setColor(new Color(56, 120, 180));
                g2.fillOval(pondX, H - 58, 42, 14);
                g2.setColor(new Color(110, 180, 240, 180));
                g2.fillOval(pondX + 8, H - 55, 26, 6);
            }
        }

        for (int i = 0; i < 10; i++) {
            double ox = (i * 320 - camX * 0.5) % (W * 3);
            if (ox < -40) ox += W * 3;
            if (ox < W + 40) {
                int objX = (int) ox;
                if (i % 2 == 0) {
                    g2.setColor(new Color(165, 170, 175));
                    g2.fillRect(objX, H - 82, 16, 5);
                    g2.setColor(new Color(135, 95, 65));
                    g2.fillRect(objX + 2, H - 77, 12, 8);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRect(objX, H - 52, 6, 4);
                    g2.setColor(new Color(90, 60, 30));
                    g2.fillRect(objX + 2, H - 51, 2, 2);
                }
            }
        }
    }

    private void drawTelegraphPolesAndWires(TrainState st) {
        double camX = st.dist;
        int poleSpacing = 90;

        g2.setColor(new Color(25, 25, 30));

        for (int x = -poleSpacing; x < W + poleSpacing; x += poleSpacing) {
            double pWorldX = Math.floor((camX + x) / poleSpacing) * poleSpacing;
            int screenX1 = (int) (pWorldX - camX);
            int screenX2 = screenX1 + poleSpacing;

            int y1 = H - 85;
            int y2 = H - 85;
            int ctrlY = H - 77;

            QuadCurve2D wire1 = new QuadCurve2D.Float(screenX1, y1, (screenX1 + screenX2) / 2.0f, ctrlY, screenX2, y2);
            QuadCurve2D wire2 = new QuadCurve2D.Float(screenX1, y1 - 4, (screenX1 + screenX2) / 2.0f, ctrlY - 4, screenX2, y2 - 4);
            g2.draw(wire1);
            g2.draw(wire2);
        }

        g2.setColor(new Color(110, 70, 40));
        for (int x = -poleSpacing; x < W + poleSpacing; x += poleSpacing) {
            double pWorldX = Math.floor((camX + x) / poleSpacing) * poleSpacing;
            int px = (int) (pWorldX - camX);
            g2.fillRect(px, H - 88, 3, 30);
            g2.fillRect(px - 4, H - 86, 11, 2);
            g2.fillRect(px - 4, H - 82, 11, 2);
        }
    }

    private void drawTrackAndBridges(TrainState st) {
        double camX = st.dist;

        g2.setColor(C_TRACK_BALLAST);
        g2.fillRect(0, H - 35, W, 15);

        g2.setColor(C_TRACK_SLEEPER);
        for (int x = -10; x < W + 10; x += 8) {
            double worldX = camX + x;
            if (((int) worldX / 8) % 2 == 0) {
                g2.fillRect(x, H - 32, 4, 10);
            }
        }

        g2.setColor(C_RAIL);
        g2.fillRect(0, H - 33, W, 2);
        g2.fillRect(0, H - 25, W, 2);

        drawBridgeSection(camX, RouteData.TURAG_BRIDGE_START, RouteData.TURAG_BRIDGE_END);
        drawBridgeSection(camX, RouteData.MEGHNA_BRIDGE_START, RouteData.MEGHNA_BRIDGE_END);
    }

    private void drawBridgeSection(double camX, double bStartPx, double bEndPx) {
        double bStart = bStartPx - camX;
        double bEnd = bEndPx - camX;

        if (bEnd > 0 && bStart < W) {
            g2.setColor(new Color(35, 85, 145));
            int rLeft = (int) Math.max(0, bStart);
            int rRight = (int) Math.min(W, bEnd);
            g2.fillRect(rLeft, H - 24, rRight - rLeft, 24);

            g2.setColor(new Color(110, 115, 125));
            for (double px = bStart; px <= bEnd; px += 100) {
                if (px >= -20 && px <= W + 20) {
                    g2.fillRect((int) px - 4, H - 24, 8, 24);
                    g2.drawLine((int) px, H - 33, (int) px + 100, H - 33);
                    g2.drawLine((int) px, H - 33, (int) px + 50, H - 55);
                    g2.drawLine((int) px + 50, H - 55, (int) px + 100, H - 33);
                }
            }
        }
    }

    private void drawTrainWithWheels(TrainState st) {
        int trainX = 230;
        int bounceY = (int) st.cameraShake;

        double wheelAngle = (st.dist * 0.25) % (Math.PI * 2);

        // 1. Blue Locomotive
        g2.setColor(C_LOCO_BLUE);
        g2.fillRect(trainX, H - 54 + bounceY, 48, 22);

        g2.setColor(C_LOCO_YELLOW);
        g2.fillRect(trainX, H - 44 + bounceY, 48, 4);
        g2.fillRect(trainX + 42, H - 54 + bounceY, 6, 22);

        g2.setColor(C_LOCO_CAB);
        g2.fillRect(trainX + 26, H - 52 + bounceY, 12, 7);
        g2.setColor(Color.BLACK);
        g2.fillRect(trainX + 38, H - 58 + bounceY, 4, 5);

        drawBogieWheelSet(trainX + 4, H - 32 + bounceY, wheelAngle, 3);
        drawBogieWheelSet(trainX + 28, H - 32 + bounceY, wheelAngle, 3);

        g2.setColor(st.headlight && st.engine != TrainState.EngineState.OFF ? C_GLOW_YELLOW : Color.GRAY);
        g2.fillRect(trainX + 46, H - 48 + bounceY, 3, 5);

        // 2. 5 Green Passenger Coaches
        for (int c = 1; c <= 5; c++) {
            int cx = trainX - c * 52;
            if (cx + 48 < -60) continue;

            g2.setColor(Color.BLACK);
            g2.fillRect(cx + 48, H - 42 + bounceY, 4, 2);

            g2.setColor(C_COACH_GREEN);
            g2.fillRect(cx, H - 52 + bounceY, 48, 20);

            g2.setColor(C_COACH_RED);
            g2.fillRect(cx, H - 45 + bounceY, 48, 3);
            g2.setColor(C_COACH_YELLOW);
            g2.fillRect(cx, H - 42 + bounceY, 48, 1);

            g2.setColor(C_COACH_ROOF);
            g2.fillRect(cx - 1, H - 54 + bounceY, 50, 3);

            for (int wx = cx + 4; wx < cx + 42; wx += 9) {
                boolean isDoor = (wx == cx + 22);
                if (isDoor && st.doorOpen > 0.1) {
                    g2.setColor(new Color(20, 20, 25));
                    g2.fillRect(wx, H - 50 + bounceY, 6, 16);
                } else {
                    g2.setColor(new Color(255, 235, 120));
                    g2.fillRect(wx, H - 48 + bounceY, 5, 6);
                    g2.setColor(new Color(40, 30, 20));
                    g2.fillRect(wx + 1, H - 46 + bounceY, 3, 3);
                }
            }

            drawBogieWheelSet(cx + 4, H - 32 + bounceY, wheelAngle, 2);
            drawBogieWheelSet(cx + 30, H - 32 + bounceY, wheelAngle, 2);
        }
    }

    private void drawBogieWheelSet(int x, int y, double angle, int axles) {
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x - 2, y + 2, axles * 7 + 4, 3);

        for (int a = 0; a < axles; a++) {
            int wx = x + a * 7;
            g2.setColor(new Color(30, 30, 35));
            g2.fillOval(wx, y, 6, 6);
            g2.setColor(new Color(180, 185, 190));
            g2.drawOval(wx, y, 6, 6);

            int cos = (int) (Math.cos(angle) * 2.5);
            int sin = (int) (Math.sin(angle) * 2.5);
            g2.drawLine(wx + 3 - cos, y + 3 - sin, wx + 3 + cos, y + 3 + sin);
        }
    }

    private void drawFrontsidedStations(TrainState st) {
        double camX = st.dist;

        for (Station station : RouteData.STATIONS) {
            double sX = station.distPx - camX;
            if (sX + station.platformRight < -50 || sX + station.platformLeft > W + 50) continue;

            int pLeft = (int) (sX + station.platformLeft);
            int pRight = (int) (sX + station.platformRight);
            int pWidth = pRight - pLeft;

            g2.setColor(new Color(190, 195, 200));
            g2.fillRect(pLeft, H - 30, pWidth, 12);
            g2.setColor(new Color(230, 200, 50));
            g2.fillRect(pLeft, H - 30, pWidth, 2);
            g2.setColor(new Color(100, 105, 110));
            g2.fillRect(pLeft, H - 18, pWidth, 18);

            for (int rx = pLeft + 20; rx < pRight - 20; rx += 45) {
                g2.setColor(new Color(80, 90, 100));
                g2.fillRect(rx, H - 65, 4, 36);
            }

            g2.setColor(station.roofColor.equalsIgnoreCase("red") ? new Color(180, 50, 40)
                    : station.roofColor.equalsIgnoreCase("blue") ? new Color(40, 90, 170) : new Color(40, 140, 70));
            g2.fillRect(pLeft, H - 72, pWidth, 8);

            int signW = 120, signH = 22;
            int signX = pLeft + pWidth / 2 - signW / 2;
            int signY = H - 62;

            g2.setColor(new Color(15, 25, 40));
            g2.fillRect(signX - 2, signY - 2, signW + 4, signH + 4);
            g2.setColor(new Color(250, 250, 240));
            g2.fillRect(signX, signY, signW, signH);

            g2.setColor(new Color(180, 20, 20));
            g2.setFont(new Font("Dialog", Font.BOLD, 10));
            g2.drawString(station.nameBn, signX + 8, signY + 11);

            g2.setColor(new Color(20, 30, 70));
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            g2.drawString(station.nameEn + " (" + station.code + ")", signX + 8, signY + 20);

            int stallX = pLeft + 40;
            g2.setColor(new Color(160, 100, 50));
            g2.fillRect(stallX, H - 44, 25, 14);
            g2.setColor(new Color(230, 40, 40));
            g2.fillRect(stallX - 2, H - 47, 29, 3);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Dialog", Font.PLAIN, 7));
            g2.drawString("চা", stallX + 10, H - 36);

            int markX = (int) (sX + 160);
            if (markX >= 0 && markX <= W) {
                g2.setColor(Color.DARK_GRAY);
                g2.fillRect(markX, H - 42, 3, 12);
                g2.setColor(Color.BLACK);
                g2.fillRect(markX - 4, H - 48, 11, 7);
                g2.setColor(C_GLOW_YELLOW);
                g2.fillRect(markX - 3, H - 47, 9, 5);
            }

            for (int i = 0; i < station.crowd; i++) {
                int px = pLeft + 25 + (i * 16) % Math.max(1, pWidth - 50);
                g2.setColor(new Color(210, 140, 100));
                g2.fillOval(px - 1, H - 44, 5, 5);
                g2.setColor(i % 2 == 0 ? new Color(200, 50, 50) : new Color(40, 120, 180));
                g2.fillRect(px, H - 39, 4, 10);
            }

            if (st.currentStationIdx == station.index && st.state == TrainState.SimState.STOPPED
                    && (st.stopPhase == TrainState.StopPhase.FLAG || st.stopPhase == TrainState.StopPhase.HORN)) {
                int gx = pLeft + 12;
                g2.setColor(new Color(30, 30, 80));
                g2.fillRect(gx, H - 43, 4, 13);
                g2.setColor(C_GLOW_GREEN);
                g2.fillRect(gx + 4, H - 48, 7, 6);
            }
        }
    }

    private void drawStationBanner(TrainState st) {
        int upcomingIdx = st.getUpcomingStationIndex();
        if (upcomingIdx < RouteData.STATIONS.size()) {
            Station upcoming = RouteData.STATIONS.get(upcomingIdx);
            int bw = 200, bh = 18, bx = (W - bw) / 2, by = 26;

            g2.setColor(new Color(15, 22, 35, 230));
            g2.fillRoundRect(bx, by, bw, bh, 6, 6);
            g2.setColor(new Color(60, 80, 110));
            g2.drawRoundRect(bx, by, bw, bh, 6, 6);

            g2.setColor(C_GLOW_YELLOW);
            g2.setFont(new Font("Dialog", Font.BOLD, 9));

            String bannerText = (st.state == TrainState.SimState.STOPPED)
                    ? "AT STATION: " + upcoming.nameBn + " / " + upcoming.nameEn
                    : "NEXT STATION: " + upcoming.nameBn + " / " + upcoming.nameEn;
            g2.drawString(bannerText, bx + 6, by + 13);
        }
    }

    private void drawParticles(TrainState st) {
        if (st.engine == TrainState.EngineState.CRANK || st.engine == TrainState.EngineState.ON) {
            double rate = (st.engine == TrainState.EngineState.CRANK) ? 0.3 : (st.rpm > 0.7 ? 0.9 : 0.4);
            if (Math.random() < rate) {
                for (Particle p : st.particles) {
                    if (!p.active) {
                        p.spawnSmoke(268, H - 58, st.rpm > 0.75, st.monsoonMode >= 3 ? -1.2 : 0);
                        break;
                    }
                }
            }
        }

        if (st.lever < -1 && st.speed > 15.0) {
            if (Math.random() < 0.6) {
                for (Particle p : st.particles) {
                    if (!p.active) {
                        p.spawnSpark(240 + Math.random() * 20, H - 30);
                        break;
                    }
                }
            }
        }

        if (st.rainLevel > 0.1 && Math.random() < st.rainLevel) {
            for (Particle p : st.particles) {
                if (!p.active) {
                    p.spawnRain(W, H, st.monsoonMode >= 3 ? -2.0 : 0);
                    break;
                }
            }
        }

        for (Particle p : st.particles) {
            if (!p.active) continue;

            switch (p.type) {
                case SMOKE:
                    int alpha = (int) (Math.max(0, 1.0 - p.age / p.life) * (p.revving ? 180 : 110));
                    g2.setColor(p.revving ? new Color(40, 40, 40, alpha) : new Color(220, 220, 220, alpha));
                    int s = (int) p.size;
                    g2.fillOval((int) p.x, (int) p.y, s, s);
                    break;

                case SPARK:
                    g2.setColor(new Color(255, 200, 50));
                    g2.fillRect((int) p.x, (int) p.y, 2, 2);
                    break;

                case RAIN:
                    g2.setColor(new Color(180, 200, 230, 160));
                    g2.drawLine((int) p.x, (int) p.y, (int) (p.x - 3), (int) (p.y + 7));
                    break;

                case LIGHTNING:
                    g2.setColor(new Color(240, 245, 255));
                    int lx = (int) p.x;
                    g2.drawLine(lx, 0, lx - 10, 30);
                    g2.drawLine(lx - 10, 30, lx + 5, 55);
                    g2.drawLine(lx + 5, 55, lx - 15, H - 70);
                    break;

                case POPUP:
                    g2.setColor(p.color);
                    g2.setFont(new Font("Monospaced", Font.BOLD, 9));
                    g2.drawString(p.text, (int) p.x, (int) p.y);
                    break;

                default:
                    break;
            }
        }
    }

    private void drawLighting(TrainState st) {
        if (st.headlight && st.engine != TrainState.EngineState.OFF) {
            int alpha = (st.hour > 19.0 || st.hour < 6.0 || st.thunderstorm) ? 100 : 35;
            g2.setColor(new Color(255, 240, 150, alpha));
            int[] xPoints = {273, W, W, 273};
            int[] yPoints = {H - 48, H - 75, H - 25, H - 45};
            g2.fillPolygon(xPoints, yPoints, 4);
        }
    }

    private void drawHUD(TrainState st) {
        int mapW = 180, mapH = 16, mapX = (W - mapW) / 2, mapY = 6;
        g2.setColor(new Color(14, 18, 28, 220));
        g2.fillRoundRect(mapX, mapY, mapW, mapH, 8, 8);
        g2.setColor(new Color(60, 75, 100));
        g2.drawRoundRect(mapX, mapY, mapW, mapH, 8, 8);

        g2.setColor(Color.WHITE);
        g2.drawLine(mapX + 10, mapY + 8, mapX + mapW - 10, mapY + 8);

        double totalDist = RouteData.STATIONS.get(RouteData.STATIONS.size() - 1).distPx;

        for (Station s : RouteData.STATIONS) {
            int sx = mapX + 10 + (int) ((s.distPx / totalDist) * (mapW - 20));
            g2.setColor(st.currentStationIdx == s.index ? C_GLOW_YELLOW : Color.WHITE);
            g2.fillRect(sx - 2, mapY + 6, 4, 4);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 7));
            g2.drawString(s.code, sx - 6, mapY + 5);
        }

        int tx = mapX + 10 + (int) Math.min(mapW - 20, Math.max(0, (st.dist / totalDist) * (mapW - 20)));
        g2.setColor(C_GLOW_GREEN);
        g2.fillRect(tx - 2, mapY + 4, 5, 8);

        if (st.driveMode && !st.titleScreen) {
            g2.setColor(C_HUD_BG);
            g2.fillRect(4, 48, 85, 42);
            g2.setColor(C_HUD_BORDER);
            g2.drawRect(4, 48, 85, 42);

            g2.setColor(st.overspeed ? Color.RED : Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            g2.drawString(String.format("%3d", (int) st.speed), 8, 62);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
            g2.drawString("KM/H", 38, 62);

            g2.setColor(Color.RED);
            g2.fillRect(62, 51, 22, 12);
            g2.setColor(Color.WHITE);
            g2.fillRect(63, 52, 20, 10);
            g2.setColor(Color.BLACK);
            g2.drawString(String.valueOf((int) st.currentSpeedLimit), 65, 60);

            g2.setFont(new Font("Monospaced", Font.BOLD, 9));
            String leverStr = st.lever > 0 ? "P" + st.lever : st.lever == TrainState.EB ? "EB" : st.lever < 0 ? "B" + (-st.lever) : "N";
            g2.setColor(st.lever == TrainState.EB ? Color.RED : st.lever > 0 ? C_GLOW_GREEN : Color.WHITE);
            g2.drawString("LEVER: " + leverStr, 8, 74);

            g2.setColor(Color.GRAY);
            g2.drawString("ENG:" + (st.engine == TrainState.EngineState.ON ? "ON" : st.engine == TrainState.EngineState.CRANK ? "START" : "OFF"), 8, 84);
            g2.setColor(new Color(40, 180, 80));
            g2.fillRect(52, 78, (int) (32 * st.rpm), 5);
            g2.setColor(Color.WHITE);
            g2.drawRect(52, 78, 32, 5);

            g2.setColor(C_HUD_BG);
            g2.fillRect(W - 96, 48, 92, 42);
            g2.setColor(C_HUD_BORDER);
            g2.drawRect(W - 96, 48, 92, 42);

            int hrs = (int) st.hour;
            int mins = (int) ((st.hour - hrs) * 60);
            String ampm = (hrs >= 12) ? "PM" : "AM";
            int hrs12 = (hrs % 12 == 0) ? 12 : hrs % 12;

            g2.setColor(C_GLOW_YELLOW);
            g2.setFont(new Font("Monospaced", Font.BOLD, 9));
            g2.drawString(String.format("%02d:%02d %s", hrs12, mins, ampm), W - 92, 60);

            String timePhase = (st.hour >= 5.0 && st.hour < 7.0) ? "DAWN" : (st.hour >= 7.0 && st.hour < 18.0) ? "DAY" : (st.hour >= 18.0 && st.hour < 19.5) ? "DUSK" : "NIGHT";
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
            g2.drawString("PHASE: " + timePhase, W - 92, 72);

            int upcomingIdx = st.getUpcomingStationIndex();
            double dueArr = RouteData.SCHEDULE.get(upcomingIdx).arr;
            double lateMin = (st.tripTime - dueArr) * RouteData.HOURS_PER_S * 60.0;
            String statusStr = (lateMin <= 8) ? "ON TIME" : "+" + (int) lateMin + "M LATE";
            g2.setColor((lateMin <= 8) ? C_GLOW_GREEN : Color.RED);
            g2.drawString(statusStr, W - 92, 84);
        }

        int toastY = 95;
        for (TrainState.Toast t : st.toasts) {
            g2.setColor(C_HUD_BG);
            int tw = t.text.length() * 7 + 10;
            int txLoc = (W - tw) / 2;
            g2.fillRect(txLoc, toastY, tw, 14);
            g2.setColor(C_HUD_BORDER);
            g2.drawRect(txLoc, toastY, tw, 14);

            g2.setColor(t.color);
            g2.setFont(new Font("Monospaced", Font.BOLD, 9));
            g2.drawString(t.text, txLoc + 5, toastY + 10);
            toastY += 16;
        }
    }

    private void drawTitleCard(TrainState st) {
        g2.setColor(new Color(15, 20, 30, 235));
        g2.fillRect(20, 15, W - 40, H - 30);
        g2.setColor(C_HUD_BORDER);
        g2.drawRect(20, 15, W - 40, H - 30);

        g2.setColor(C_GLOW_YELLOW);
        g2.setFont(new Font("Dialog", Font.BOLD, 13));
        g2.drawString("INTERCITY PIXEL TRAIN (বাংলাদেশ রেলওয়ে)", 35, 36);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Dialog", Font.BOLD, 10));
        g2.drawString("KAMALAPUR -> AIRPORT -> GAZIPUR -> COMILLA -> CHITTAGONG", 22, 54);

        g2.setColor(Color.LIGHT_GRAY);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
        g2.drawString("DHK > DAA > GZP > CO > CTG (321 KM)", 55, 68);

        int b1X = 55, b2X = 175, bY = 90;

        g2.setColor(st.titleSelection == 0 ? C_GLOW_YELLOW : Color.GRAY);
        g2.drawRect(b1X, bY, 90, 24);
        g2.setColor(st.titleSelection == 0 ? new Color(40, 80, 140) : new Color(30, 35, 45));
        g2.fillRect(b1X + 1, bY + 1, 88, 22);
        g2.setColor(Color.WHITE);
        g2.drawString("WATCH (AUTO)", b1X + 8, bY + 15);

        g2.setColor(st.titleSelection == 1 ? C_GLOW_YELLOW : Color.GRAY);
        g2.drawRect(b2X, bY, 90, 24);
        g2.setColor(st.titleSelection == 1 ? new Color(40, 140, 70) : new Color(30, 35, 45));
        g2.fillRect(b2X + 1, bY + 1, 88, 22);
        g2.setColor(Color.WHITE);
        g2.drawString("DRIVE TRAIN", b2X + 10, bY + 15);

        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
        g2.drawString("KEYS: E=Engine, Up/Down=Lever, O=Doors, H=Horn, W=Weather", 24, 138);
        g2.drawString("PRESS ENTER OR SPACE TO START", 75, 152);
    }

    private void drawScorePanel(TrainState st) {
        g2.setColor(new Color(15, 22, 38, 245));
        g2.fillRect(30, 15, W - 60, H - 30);
        g2.setColor(C_HUD_BORDER);
        g2.drawRect(30, 15, W - 60, H - 30);

        // Header Message: Journey Concludes Here
        g2.setColor(C_GLOW_YELLOW);
        g2.setFont(new Font("Dialog", Font.BOLD, 13));
        g2.drawString("JOURNEY CONCLUDES HERE! (যাত্রা সমাপ্ত)", 45, 34);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Dialog", Font.BOLD, 9));
        g2.drawString("CHITTAGONG TERMINUS REACHED | 321 KM", 60, 48);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
        g2.drawString("STOP ACCURACY: " + (int) st.scoreStops, 55, 66);
        g2.drawString("PUNCTUALITY:   " + (int) st.scoreTime, 55, 78);
        g2.drawString("COMFORT:       " + (int) st.comfort + "%", 55, 90);
        g2.drawString("PENALTIES:     " + (int) st.scorePenalties, 55, 102);

        g2.setColor(C_GLOW_GREEN);
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2.drawString("TOTAL SCORE: " + (int) st.finalTotal, 95, 118);

        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            stars.append(i < st.finalStars ? "* " : "- ");
        }
        g2.setColor(C_GLOW_YELLOW);
        g2.drawString("RATING: " + stars.toString(), 110, 132);

        // Restart Banner
        g2.setColor(new Color(40, 140, 70));
        g2.fillRect(70, 142, 180, 16);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 9));
        g2.drawString("PRESS ENTER TO RESTART TRIP", 78, 153);
    }
}
