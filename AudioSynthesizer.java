package com.train.game;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class AudioSynthesizer implements Runnable {
    private static final int SAMPLE_RATE = 22050;
    private SourceDataLine line;
    private Thread audioThread;
    private volatile boolean running = false;
    private volatile boolean muted = false;

    // Controls
    public volatile boolean engineOn = false;
    public volatile double engineRpm = 0.0;
    public volatile boolean hornOn = false;
    public volatile double trainSpeed = 0.0;
    public volatile int rainMode = 0; // 0=Off, 1=Light, 2=Heavy, 3=Thunderstorm

    // One-shots
    private volatile int playAirPuffs = 0;
    private volatile int playNotches = 0;
    private volatile int playBeeps = 0;
    private volatile int playChimes = 0;
    private volatile int playCranks = 0;
    private volatile int playThunder = 0;

    // Phase counters
    private double enginePhase = 0;
    private double hornPhase1 = 0;
    private double hornPhase2 = 0;
    private double railPhase = 0;
    private double chimeTimer = 0;

    public AudioSynthesizer() {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, false, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (AudioSystem.isLineSupported(info)) {
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format, 2048);
                line.start();
                running = true;
                audioThread = new Thread(this, "AudioSynthesizer");
                audioThread.setDaemon(true);
                audioThread.start();
            }
        } catch (Exception e) {
            System.err.println("Audio setup failed: " + e.getMessage());
            running = false;
        }
    }

    public void setMuted(boolean muted) { this.muted = muted; }
    public boolean isMuted() { return muted; }

    public void triggerAir() { playAirPuffs++; }
    public void triggerNotch() { playNotches++; }
    public void triggerBeep() { playBeeps++; }
    public void triggerChime() { playChimes = 15; }
    public void triggerCrank() { playCranks = 40; }
    public void triggerThunder() { playThunder = 30; }

    @Override
    public void run() {
        byte[] buffer = new byte[512];
        while (running) {
            if (muted || line == null || !line.isOpen()) {
                try { Thread.sleep(20); } catch (InterruptedException e) {}
                continue;
            }

            for (int i = 0; i < buffer.length; i++) {
                double sample = 0.0;

                // 1. Diesel Engine (Sawtooth + Sub-harmonic pulse)
                if (engineOn) {
                    double freq = 22.0 + engineRpm * 60.0;
                    enginePhase += freq / SAMPLE_RATE;
                    if (enginePhase >= 1.0) enginePhase -= 1.0;

                    double saw = (enginePhase * 2.0 - 1.0);
                    double sub = (enginePhase < 0.5) ? 0.6 : -0.6;
                    sample += (saw * 0.6 + sub * 0.4) * (0.12 + 0.18 * engineRpm);
                }

                // 2. Starter Motor Crank
                if (playCranks > 0) {
                    double crankFreq = 40.0 + (40 - playCranks) * 3.0;
                    enginePhase += crankFreq / SAMPLE_RATE;
                    if (enginePhase >= 1.0) enginePhase -= 1.0;
                    sample += ((enginePhase < 0.5) ? 0.3 : -0.3) * 0.3;
                }

                // 3. Train Horn (Dual chord E-flat 311Hz + F-sharp 370Hz + Vibrato)
                if (hornOn) {
                    double vib = Math.sin(hornPhase1 * 12.0) * 4.0;
                    hornPhase1 += (311.12 + vib) / SAMPLE_RATE;
                    hornPhase2 += (370.00 + vib) / SAMPLE_RATE;
                    if (hornPhase1 >= 1.0) hornPhase1 -= 1.0;
                    if (hornPhase2 >= 1.0) hornPhase2 -= 1.0;

                    double h1 = Math.sin(hornPhase1 * 2.0 * Math.PI);
                    double h2 = Math.sin(hornPhase2 * 2.0 * Math.PI);
                    sample += (h1 + h2) * 0.25;
                }

                // 4. Rail Joint Clickety-clack (4-axle rhythmic wheel clicks)
                if (trainSpeed > 2.0) {
                    railPhase += (trainSpeed * 0.15) / SAMPLE_RATE;
                    if (railPhase >= 1.0) railPhase -= 1.0;
                    if (railPhase < 0.04 || (railPhase > 0.12 && railPhase < 0.16) ||
                        (railPhase > 0.40 && railPhase < 0.44) || (railPhase > 0.52 && railPhase < 0.56)) {
                        sample += (Math.random() - 0.5) * 0.28;
                    }
                }

                // 5. Thunder Boom & Rumble
                if (playThunder > 0) {
                    sample += (Math.random() - 0.5) * (playThunder / 30.0) * 0.6;
                }

                // 6. Rain sound (Hiss / Patter)
                if (rainMode > 0) {
                    double rainVol = (rainMode == 1) ? 0.05 : (rainMode == 2) ? 0.12 : 0.20;
                    sample += (Math.random() - 0.5) * rainVol;
                }

                // Air hiss & notches
                if (playAirPuffs > 0) sample += (Math.random() - 0.5) * 0.35;
                if (playNotches > 0) sample += (Math.random() > 0.5 ? 0.4 : -0.4);
                if (playBeeps > 0) sample += (Math.sin(i * 0.8) > 0 ? 0.3 : -0.3);
                if (playChimes > 0) {
                    chimeTimer += 1.0 / SAMPLE_RATE;
                    sample += Math.sin(chimeTimer * 2.0 * Math.PI * 880.0) * 0.3;
                }

                sample = Math.max(-1.0, Math.min(1.0, sample));
                buffer[i] = (byte) (sample * 127);
            }

            if (playAirPuffs > 0) playAirPuffs--;
            if (playNotches > 0) playNotches--;
            if (playBeeps > 0) playBeeps--;
            if (playCranks > 0) playCranks--;
            if (playThunder > 0) playThunder--;
            if (playChimes > 0) {
                playChimes--;
                if (playChimes == 0) chimeTimer = 0;
            }

            line.write(buffer, 0, buffer.length);
        }
    }

    public void stop() {
        running = false;
        if (line != null) line.close();
    }
}
