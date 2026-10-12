package com.train.game;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel implements Runnable {
    public static final int WINDOW_W = 960;
    public static final int WINDOW_H = 540;

    private final TrainState trainState;
    private final PixelRenderer renderer;
    private final AudioSynthesizer audioSynth;
    private Thread gameThread;
    private volatile boolean running = false;

    private final JPanel toolbarPanel;
    private JButton bMode, bEngine, bPower, bBrake, bDoors, bHorn, bLight, bPause, bSpeed, bSkip, bTime, bRain, bMute;

    public GamePanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(WINDOW_W, WINDOW_H + 45));
        setFocusable(true);

        audioSynth = new AudioSynthesizer();
        trainState = new TrainState();
        trainState.audio = audioSynth;
        renderer = new PixelRenderer();

        toolbarPanel = createToolbar();
        add(toolbarPanel, BorderLayout.SOUTH);

        setupKeyListeners();
        setupMouseListeners();
    }

    private JPanel createToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        bar.setBackground(new Color(25, 30, 40));

        bMode = createButton("AUTO", "Driver mode (C)");
        bEngine = createButton("ENGINE", "Engine start/stop (E)");
        bPower = createButton("POWER ▲", "Power notch up (Up Arrow)");
        bBrake = createButton("BRAKE ▼", "Power notch down / brake (Down Arrow)");
        bDoors = createButton("DOORS", "Open/Close doors (O)");
        bHorn = createButton("HORN 🕪", "Sound Horn (Hold H)");
        bLight = createButton("LIGHT 💡", "Headlight toggle (L)");
        bPause = createButton("PAUSE ⏸", "Pause game (P)");
        bSpeed = createButton("1x", "Speed 1x/2x/4x");
        bSkip = createButton("NEXT ⏭", "Skip to next station (N)");
        bTime = createButton("TIME ☀️", "Time of day (T)");
        bRain = createButton("MONSOON ⛈", "Monsoon / Thunderstorm (W)");
        bMute = createButton("SOUND 🔊", "Mute sound (M)");

        bar.add(bMode);
        bar.add(bEngine);
        bar.add(bPower);
        bar.add(bBrake);
        bar.add(bDoors);
        bar.add(bHorn);
        bar.add(bLight);
        bar.add(bPause);
        bar.add(bSpeed);
        bar.add(bSkip);
        bar.add(bTime);
        bar.add(bRain);
        bar.add(bMute);

        // Action Handlers
        bMode.addActionListener(e -> toggleMode());
        bEngine.addActionListener(e -> trainState.toggleEngine());
        bPower.addActionListener(e -> trainState.setLever(trainState.lever + 1));
        bBrake.addActionListener(e -> trainState.setLever(trainState.lever - 1));
        bDoors.addActionListener(e -> trainState.toggleDoors());
        bHorn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { audioSynth.hornOn = true; }
            @Override
            public void mouseReleased(MouseEvent e) { audioSynth.hornOn = false; }
        });
        bLight.addActionListener(e -> trainState.headlight = !trainState.headlight);
        bPause.addActionListener(e -> trainState.paused = !trainState.paused);
        bSpeed.addActionListener(e -> cycleSpeed());
        bSkip.addActionListener(e -> skipStation());
        bTime.addActionListener(e -> cycleTime());
        bRain.addActionListener(e -> cycleWeather());
        bMute.addActionListener(e -> {
            audioSynth.setMuted(!audioSynth.isMuted());
            bMute.setText(audioSynth.isMuted() ? "MUTED" : "SOUND 🔊");
        });

        return bar;
    }

    private JButton createButton(String label, String tooltip) {
        JButton btn = new JButton(label);
        btn.setToolTipText(tooltip);
        btn.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 11));
        btn.setFocusable(false);
        btn.setBackground(new Color(45, 55, 70));
        btn.setForeground(Color.WHITE);
        return btn;
    }

    private void toggleMode() {
        if (trainState.journeyComplete) {
            trainState.restartJourney();
            return;
        }
        if (trainState.titleScreen) {
            trainState.titleScreen = false;
            trainState.driveMode = true;
        } else {
            trainState.driveMode = !trainState.driveMode;
        }
        bMode.setText(trainState.driveMode ? "DRIVE" : "AUTO");
    }

    private void cycleSpeed() {
        if (trainState.timeScale == 1.0) trainState.timeScale = 2.0;
        else if (trainState.timeScale == 2.0) trainState.timeScale = 4.0;
        else trainState.timeScale = 1.0;
        bSpeed.setText((int) trainState.timeScale + "x");
    }

    private void cycleTime() {
        trainState.timeMode = (trainState.timeMode + 1) % 5;
        String[] tLabels = {"TIME AUTO", "DAWN 🌅", "DAY ☀️", "DUSK 🌇", "NIGHT 🌙"};
        bTime.setText(tLabels[trainState.timeMode]);
    }

    private void cycleWeather() {
        trainState.monsoonMode = (trainState.monsoonMode + 1) % 5;
        String[] wLabels = {"AUTO", "CLEAR", "DRIZZLE", "HEAVY RAIN", "THUNDERSTORM ⚡"};
        bRain.setText(wLabels[trainState.monsoonMode]);
    }

    private void skipStation() {
        if (trainState.currentStationIdx + 1 < RouteData.STATIONS.size()) {
            trainState.currentStationIdx++;
            trainState.dist = RouteData.STATIONS.get(trainState.currentStationIdx).distPx;
            trainState.state = TrainState.SimState.STOPPED;
            trainState.setStopPhase(TrainState.StopPhase.SETTLE);
        }
    }

    private void setupKeyListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                if (trainState.journeyComplete) {
                    if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE || key == KeyEvent.VK_R) {
                        trainState.restartJourney();
                    }
                    return;
                }

                if (trainState.titleScreen) {
                    if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_TAB) {
                        trainState.titleSelection ^= 1;
                    } else if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
                        trainState.titleScreen = false;
                        trainState.driveMode = (trainState.titleSelection == 1);
                        bMode.setText(trainState.driveMode ? "DRIVE" : "AUTO");
                    } else if (key == KeyEvent.VK_C) {
                        trainState.titleScreen = false;
                        trainState.driveMode = true;
                        bMode.setText("DRIVE");
                    }
                    return;
                }

                switch (key) {
                    case KeyEvent.VK_C:
                        toggleMode();
                        break;
                    case KeyEvent.VK_E:
                        trainState.toggleEngine();
                        break;
                    case KeyEvent.VK_UP:
                        trainState.setLever(trainState.lever + 1);
                        break;
                    case KeyEvent.VK_DOWN:
                        trainState.setLever(trainState.lever - 1);
                        break;
                    case KeyEvent.VK_B:
                        trainState.setLever(TrainState.EB);
                        break;
                    case KeyEvent.VK_O:
                        trainState.toggleDoors();
                        break;
                    case KeyEvent.VK_H:
                        audioSynth.hornOn = true;
                        break;
                    case KeyEvent.VK_L:
                        trainState.headlight = !trainState.headlight;
                        break;
                    case KeyEvent.VK_P:
                    case KeyEvent.VK_SPACE:
                        trainState.paused = !trainState.paused;
                        break;
                    case KeyEvent.VK_N:
                        skipStation();
                        break;
                    case KeyEvent.VK_T:
                        cycleTime();
                        break;
                    case KeyEvent.VK_W:
                        cycleWeather();
                        break;
                    case KeyEvent.VK_M:
                        audioSynth.setMuted(!audioSynth.isMuted());
                        bMute.setText(audioSynth.isMuted() ? "MUTED" : "SOUND 🔊");
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_H) {
                    audioSynth.hornOn = false;
                }
            }
        });
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                if (trainState.journeyComplete) {
                    trainState.restartJourney();
                } else if (trainState.titleScreen) {
                    trainState.titleScreen = false;
                    trainState.driveMode = true;
                    bMode.setText("DRIVE");
                }
            }
        });
    }

    public void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this, "GameLoop");
        gameThread.start();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            double dt = (now - lastTime) / 1000000000.0;
            lastTime = now;

            trainState.update(dt);
            repaint();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {}
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage frame = renderer.render(trainState);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.drawImage(frame, 0, 0, getWidth(), getHeight() - toolbarPanel.getHeight(), null);
    }
}
