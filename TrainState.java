package com.train.game;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class TrainState {
    public enum EngineState { OFF, CRANK, ON, SHUTDOWN }
    public enum SimState { STOPPED, DEPARTING, TRAVELING, ARRIVING, DRIVING }
    public enum StopPhase { SETTLE, READY, DOORS_OPEN, BOARDING, BOARDED, DOORS_CLOSE, WAIT, FLAG, CLEAR, HORN, PARKED, TERMINUS }

    public boolean driveMode = false;
    public boolean paused = false;
    public double timeScale = 1.0;
    public boolean titleScreen = true;
    public int titleSelection = 1;

    // Physics & Engine
    public EngineState engine = EngineState.OFF;
    public double engineT = 0.0;
    public double rpm = 0.0;
    public int lever = 0; // -5 (EB), -4..-1 (Brake), 0 (Coast), 1..8 (Power)
    public static final int EB = -5;
    public double speed = 0.0; // km/h
    public double accel = 0.0;
    public double dist = 0.0; // px
    public boolean headlight = true;
    public boolean horn = false;

    // Suspension Bounce & Camera Shake
    public double bouncePhase = 0.0;
    public double cameraShake = 0.0;

    // Simulation State Machine
    public SimState state = SimState.STOPPED;
    public StopPhase stopPhase = StopPhase.SETTLE;
    public double phaseT = 0.0;
    public int currentStationIdx = 0;
    public double stopOffset = 0.0;
    public double doorOpen = 0.0;
    public int passengersOnBoard = 0;

    // Score & Timetable
    public double tripTime = 0.0;
    public double scoreStops = 0;
    public double scoreTime = 0;
    public double scorePax = 0;
    public double scorePenalties = 0;
    public double comfort = 100.0;
    public boolean overspeed = false;
    public double overspeedBeepT = 0.0;
    public double currentSpeedLimit = 100.0;
    public boolean journeyComplete = false;

    public double finalTotal = 0;
    public int finalStars = 0;

    // Weather Modes
    public int monsoonMode = 0;
    public double rainLevel = 0.0;
    public boolean thunderstorm = false;
    public double thunderTimer = 0.0;
    public boolean lightningFlash = false;
    public double flashTimer = 0.0;

    // Time of Day
    public double hour = 5.6; // 5:36 AM start
    public int timeMode = 0;

    public final List<Toast> toasts = new ArrayList<>();
    public final List<Particle> particles = new ArrayList<>();
    public AudioSynthesizer audio;

    public static class Toast {
        public String text;
        public Color color;
        public boolean big;
        public double timer;

        public Toast(String text, Color color, boolean big) {
            this.text = text;
            this.color = color;
            this.big = big;
            this.timer = 0.0;
        }
    }

    public TrainState() {
        for (int i = 0; i < 60; i++) {
            particles.add(new Particle());
        }
    }

    public void addToast(String text, Color color, boolean big) {
        if (!driveMode) return;
        if (!toasts.isEmpty() && toasts.get(toasts.size() - 1).text.equals(text)) {
            toasts.get(toasts.size() - 1).timer = 0.0;
            return;
        }
        toasts.add(new Toast(text, color, big));
        if (toasts.size() > 3) toasts.remove(0);
    }

    public void setLever(int n) {
        if (journeyComplete) return;
        int old = lever;
        lever = Math.max(EB, Math.min(8, n));
        if (lever != old) {
            if (audio != null) audio.triggerNotch();
            if (lever == EB) {
                addToast("EMERGENCY BRAKE!", Color.RED, true);
                if (audio != null) audio.triggerAir();
            } else if (old < 0 && lever >= 0 && speed < 1.0) {
                if (audio != null) audio.triggerAir();
            }
        }
    }

    public void toggleEngine() {
        if (journeyComplete) return;
        if (engine == EngineState.OFF) {
            if (lever > 0) {
                addToast("LEVER TO N FIRST", Color.ORANGE, false);
                return;
            }
            engine = EngineState.CRANK;
            engineT = 0.0;
            if (audio != null) audio.triggerCrank();
        } else if (engine == EngineState.ON) {
            if (speed > 0.5) addToast("ENGINE OFF - COASTING", Color.ORANGE, false);
            engine = EngineState.SHUTDOWN;
            engineT = 0.0;
        }
    }

    public void toggleDoors() {
        if (journeyComplete) return;
        if (state == SimState.STOPPED) {
            switch (stopPhase) {
                case SETTLE:
                case READY:
                    setStopPhase(StopPhase.DOORS_OPEN);
                    if (audio != null) audio.triggerAir();
                    break;
                case BOARDING:
                    addToast("PASSENGERS STILL BOARDING", Color.ORANGE, false);
                    break;
                case BOARDED:
                    setStopPhase(StopPhase.DOORS_CLOSE);
                    if (audio != null) audio.triggerChime();
                    break;
                default:
                    addToast("DOORS LOCKED", Color.LIGHT_GRAY, false);
                    break;
            }
            return;
        }
        if (speed > 0.3) {
            addToast("STOP THE TRAIN FIRST", Color.ORANGE, false);
            return;
        }
        int targetIdx = getUpcomingStationIndex();
        if (targetIdx < RouteData.STATIONS.size()) {
            Station nextSt = RouteData.STATIONS.get(targetIdx);
            double offset = dist - nextSt.distPx;
            if (offset >= -60 && offset <= 60) {
                arriveAtStation(nextSt.index, offset);
                scoreArrival(nextSt.index, offset);
                setStopPhase(StopPhase.DOORS_OPEN);
                if (audio != null) audio.triggerAir();
            } else if (offset < -60) {
                addToast("PLATFORM AHEAD - " + (int)Math.ceil(-60 - offset) + " M", Color.ORANGE, false);
            } else {
                addToast("OVERSHOT THE PLATFORM", Color.RED, false);
            }
        }
    }

    public int getUpcomingStationIndex() {
        if (state == SimState.STOPPED) {
            return currentStationIdx;
        }
        return Math.min(RouteData.STATIONS.size() - 1, currentStationIdx + 1);
    }

    public void arriveAtStation(int stationIdx, double offset) {
        currentStationIdx = stationIdx;
        dist = RouteData.STATIONS.get(stationIdx).distPx + offset;
        stopOffset = offset;
        state = SimState.STOPPED;
    }

    public void setStopPhase(StopPhase ph) {
        stopPhase = ph;
        phaseT = 0.0;
    }

    public void scoreArrival(int stationIdx, double offset) {
        double absOff = Math.abs(offset);
        double dueTime = RouteData.SCHEDULE.get(stationIdx).arr;
        double lateMinutes = (tripTime - dueTime) * RouteData.HOURS_PER_S * 60.0;

        int timePts = (lateMinutes <= 8) ? 200 : Math.max(-200, (int)(200 - 6 * (lateMinutes - 8)));
        scoreTime += timePts;

        String msgTime = (lateMinutes < -8) ? "EARLY " + (int)-lateMinutes + " MIN"
                : (lateMinutes <= 8) ? "ON TIME" : "LATE " + (int)lateMinutes + " MIN";
        addToast(msgTime + (timePts >= 0 ? " +" : " ") + timePts, timePts >= 150 ? Color.GREEN : timePts > 0 ? Color.YELLOW : Color.RED, false);

        int stopPts = (absOff <= 3) ? 500 : (absOff <= 10) ? 300 : (absOff <= 25) ? 150 : 50;
        String stopLabel = (absOff <= 3) ? "PERFECT STOP" : (absOff <= 10) ? "GREAT STOP" : (absOff <= 25) ? "GOOD STOP" : "STOPPED";
        scoreStops += stopPts;
        addToast(stopLabel + " +" + stopPts, absOff <= 10 ? Color.GREEN : Color.CYAN, true);
        if (audio != null && absOff <= 10) audio.triggerChime();
    }

    public void restartJourney() {
        journeyComplete = false;
        state = SimState.STOPPED;
        stopPhase = StopPhase.SETTLE;
        currentStationIdx = 0;
        dist = 0;
        speed = 0;
        accel = 0;
        lever = 0;
        tripTime = 0;
        scoreStops = 0;
        scoreTime = 0;
        scorePax = 0;
        scorePenalties = 0;
        comfort = 100.0;
        engine = EngineState.OFF;
        rpm = 0.0;
        toasts.clear();
    }

    public void update(double dt) {
        if (paused || titleScreen) return;
        if (journeyComplete) {
            speed = 0;
            state = SimState.STOPPED;
            return;
        }

        double effDt = dt * timeScale;
        tripTime += effDt;

        bouncePhase += speed * effDt * 0.15;
        if (speed > 2.0) {
            cameraShake = Math.sin(bouncePhase) * (speed / 90.0);
        } else {
            cameraShake = 0;
        }

        if (timeMode == 0) {
            double maxDist = RouteData.STATIONS.get(RouteData.STATIONS.size() - 1).distPx;
            hour = 5.6 + (dist / maxDist) * (13.5 - 5.6);
        } else if (timeMode == 1) hour = 6.25;
        else if (timeMode == 2) hour = 12.0;
        else if (timeMode == 3) hour = 18.5;
        else if (timeMode == 4) hour = 22.0;

        updateWeather(effDt);
        updateEngine(effDt);

        if (driveMode) {
            updateDrivePhysics(effDt);
        } else {
            updateAutoPhysics(effDt);
        }

        for (int i = toasts.size() - 1; i >= 0; i--) {
            toasts.get(i).timer += dt;
            if (toasts.get(i).timer > 2.6) {
                toasts.remove(i);
            }
        }

        for (Particle p : particles) {
            p.update(dt, speed);
        }
    }

    private void updateWeather(double dt) {
        if (monsoonMode == 0) {
            rainLevel = (currentStationIdx >= 1 && currentStationIdx <= 3) ? 0.6 : 0.0;
            thunderstorm = (currentStationIdx == 2);
        } else if (monsoonMode == 1) { rainLevel = 0.0; thunderstorm = false; }
        else if (monsoonMode == 2) { rainLevel = 0.35; thunderstorm = false; }
        else if (monsoonMode == 3) { rainLevel = 0.85; thunderstorm = false; }
        else if (monsoonMode == 4) { rainLevel = 1.0; thunderstorm = true; }

        if (thunderstorm) {
            thunderTimer -= dt;
            if (thunderTimer <= 0) {
                thunderTimer = 4.0 + Math.random() * 6.0;
                lightningFlash = true;
                flashTimer = 0.15;
                if (audio != null) audio.triggerThunder();

                for (Particle p : particles) {
                    if (!p.active) {
                        p.spawnLightning(320);
                        break;
                    }
                }
            }
        }

        if (lightningFlash) {
            flashTimer -= dt;
            if (flashTimer <= 0) lightningFlash = false;
        }

        if (audio != null) {
            audio.rainMode = thunderstorm ? 3 : (rainLevel > 0.6) ? 2 : (rainLevel > 0.1) ? 1 : 0;
        }
    }

    private void updateEngine(double dt) {
        engineT += dt;
        double targetRpm = 0.0;
        switch (engine) {
            case CRANK:
                targetRpm = 0.12;
                if (engineT >= 2.2) {
                    engine = EngineState.ON;
                    rpm = 0.5;
                    addToast("ENGINE RUNNING", Color.GREEN, false);
                }
                break;
            case SHUTDOWN:
                if (engineT >= 2.5) {
                    engine = EngineState.OFF;
                    rpm = 0.0;
                }
                break;
            case ON:
                targetRpm = driveMode ? 0.3 + 0.7 * (doorOpen > 0 ? 0 : Math.max(0, lever)) / 8.0
                        : (state == SimState.DEPARTING ? 0.95 : state == SimState.TRAVELING ? 0.65 : 0.3);
                break;
            case OFF:
                targetRpm = 0.0;
                break;
        }
        double rate = (targetRpm > rpm) ? 0.6 : 0.4;
        rpm += Math.max(-rate * dt, Math.min(rate * dt, targetRpm - rpm));
        if (audio != null) {
            audio.engineOn = (engine == EngineState.ON);
            audio.engineRpm = rpm;
            audio.trainSpeed = speed;
        }
    }

    private void updateDrivePhysics(double dt) {
        if (state == SimState.STOPPED) {
            updateStopPhase(dt);
            accel = 0;
            if (lever > 0 && engine == EngineState.ON && doorOpen == 0) {
                departStation();
            }
            return;
        }

        double power = (lever > 0 && engine == EngineState.ON && doorOpen == 0) ? lever / 8.0 : 0;
        double trac = power * 15.0 * Math.min(1.0, 45.0 / Math.max(speed, 1.0));
        double brake = (lever == EB) ? 28.0 : (lever < 0) ? -lever * 4.5 : 0;
        double resist = (speed > 0) ? (0.5 + 0.00035 * speed * speed + brake) : 0;

        double prevSpeed = speed;
        speed = Math.max(0, speed + (trac - resist) * dt);
        dist += (prevSpeed + speed) / 2.0 * dt;
        accel = (speed - prevSpeed) / dt;

        double harsh = Math.abs(accel) - 16.0;
        if (harsh > 0) {
            comfort = Math.max(0, comfort - harsh * dt * 1.5);
        }

        currentSpeedLimit = RouteData.getSpeedLimitAt(dist);
        if (speed > currentSpeedLimit + 3.0) {
            if (!overspeed) addToast("OVERSPEED! LIMIT " + (int)currentSpeedLimit, Color.RED, true);
            overspeed = true;
            scorePenalties -= 20.0 * dt;
            overspeedBeepT -= dt;
            if (overspeedBeepT <= 0) {
                overspeedBeepT = 0.45;
                if (audio != null) audio.triggerBeep();
            }
        } else {
            overspeed = false;
        }

        int targetIdx = currentStationIdx + 1;
        if (targetIdx < RouteData.STATIONS.size()) {
            Station nextSt = RouteData.STATIONS.get(targetIdx);
            double offset = dist - nextSt.distPx;
            if (nextSt.index == RouteData.STATIONS.size() - 1 && offset >= 60.0) {
                dist = nextSt.distPx + 60.0;
                speed = 0;
                state = SimState.STOPPED;
                finishJourney();
            }
        }
    }

    private void updateAutoPhysics(double dt) {
        engine = EngineState.ON;
        rpm = 0.6;
        currentSpeedLimit = RouteData.getSpeedLimitAt(dist);

        if (state == SimState.STOPPED) {
            updateStopPhase(dt);
        } else if (state == SimState.DEPARTING) {
            speed = Math.min(60.0, speed + 10.0 * dt);
            dist += speed * dt;
            if (speed >= 60.0) state = SimState.TRAVELING;
        } else if (state == SimState.TRAVELING) {
            double targetSpeed = Math.min(84.0, currentSpeedLimit);
            speed += Math.max(-12.0 * dt, Math.min(12.0 * dt, targetSpeed - speed));
            dist += speed * dt;
            int targetIdx = currentStationIdx + 1;
            if (targetIdx < RouteData.STATIONS.size()) {
                double nextDist = RouteData.STATIONS.get(targetIdx).distPx;
                if (nextDist - dist <= 400.0) state = SimState.ARRIVING;
            }
        } else if (state == SimState.ARRIVING) {
            int targetIdx = currentStationIdx + 1;
            if (targetIdx < RouteData.STATIONS.size()) {
                double nextDist = RouteData.STATIONS.get(targetIdx).distPx;
                double remaining = nextDist - dist;
                speed = Math.max(0.5, remaining / 5.0);
                dist += speed * dt;
                if (remaining <= 2.0) {
                    arriveAtStation(targetIdx, 0.0);
                    if (currentStationIdx == RouteData.STATIONS.size() - 1) {
                        finishJourney();
                    }
                }
            }
        }
    }

    private void updateStopPhase(double dt) {
        phaseT += dt;
        boolean auto = !driveMode;
        boolean terminus = (currentStationIdx == RouteData.STATIONS.size() - 1);

        if (terminus) {
            finishJourney();
            return;
        }

        switch (stopPhase) {
            case SETTLE:
                if (phaseT >= 1.5) setStopPhase(auto ? StopPhase.DOORS_OPEN : StopPhase.READY);
                break;
            case READY:
                if (auto) setStopPhase(StopPhase.DOORS_OPEN);
                break;
            case DOORS_OPEN:
                doorOpen = Math.min(1.0, phaseT / 1.0);
                if (doorOpen >= 1.0) setStopPhase(StopPhase.BOARDING);
                break;
            case BOARDING:
                if (phaseT >= 4.0) setStopPhase(auto ? StopPhase.DOORS_CLOSE : StopPhase.BOARDED);
                break;
            case BOARDED:
                if (auto) setStopPhase(StopPhase.DOORS_CLOSE);
                break;
            case DOORS_CLOSE:
                doorOpen = Math.max(0.0, 1.0 - phaseT / 1.0);
                if (doorOpen <= 0.0) setStopPhase(StopPhase.FLAG);
                break;
            case FLAG:
                if (phaseT >= 1.6) {
                    setStopPhase(StopPhase.HORN);
                    if (audio != null) { audio.hornOn = true; }
                }
                break;
            case HORN:
                if (phaseT >= 1.2) {
                    if (audio != null) audio.hornOn = false;
                    if (auto) {
                        state = SimState.DEPARTING;
                    } else {
                        setStopPhase(StopPhase.CLEAR);
                        addToast("ALL CLEAR - DEPART", Color.GREEN, true);
                    }
                }
                break;
            case CLEAR:
                break;
            case PARKED:
            case TERMINUS:
                finishJourney();
                break;
        }
    }

    public void departStation() {
        if (currentStationIdx == RouteData.STATIONS.size() - 1) return;
        state = SimState.DRIVING;
        stopPhase = null;
    }

    public void finishJourney() {
        speed = 0;
        state = SimState.STOPPED;
        stopPhase = StopPhase.TERMINUS;
        journeyComplete = true;
        finalTotal = Math.round(scoreStops + scoreTime + scorePax + comfort * 4.0 + scorePenalties);
        finalStars = (finalTotal >= 3000) ? 3 : (finalTotal >= 2200) ? 2 : (finalTotal >= 1200) ? 1 : 0;
    }
}
