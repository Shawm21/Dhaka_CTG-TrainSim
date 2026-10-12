# Intercity Pixel Train: Dhaka to Chittagong Line (বাংলাদেশ রেলওয়ে)

A retro pixel-art train simulator inspired by Bangladesh Railway, featuring the **Dhaka to Chittagong** intercity route with 5 stations, dynamic monsoon thunderstorms, frontsided platform rendering, spoked rotating train wheels, and dual Autopilot & Player Driver modes!

![Route Preview](https://img.shields.io/badge/Route-Dhaka_to_Chittagong-blue)
![Stations](https://img.shields.io/badge/Stations-5_Stops-green)
![Tech](https://img.shields.io/badge/Languages-Java_|_HTML5_Canvas-orange)

---

##  The Route (5 Stations)

| # | Station Name (Bangla) | Station Name (English) | Code | Distance |
|---|-----------------------|------------------------|------|----------|
| 1 | **কমলাপুর** | Kamalapur (Dhaka) | `DHK` | 0 km |
| 2 | **ঢাকা বিমানবন্দর** | Dhaka Airport | `DAA` | 21 km |
| 3 | **গাজীপুর** | Gazipur | `GZP` | 36 km |
| 4 | **কুমিল্লা** | Comilla | `CO` | 173 km |
| 5 | **চট্টগ্রাম** | Chittagong (Terminus) | `CTG` | 321 km |

---

##  Game Controls

| Control | Key | Action |
|---------|-----|--------|
| **Driver Mode** | `C` | Toggle between **AUTO** (Autopilot) and **DRIVE** |
| **Engine** | `E` | Engine Start / Stop (with starter crank delay) |
| **Power** | `Up Arrow` | Notch Up (`P1` .. `P8`) / Release Brake |
| **Brake** | `Down Arrow` / `B` | Notch Down / Brake (`B1` .. `B4`, `B` = Emergency Brake `EB`) |
| **Doors** | `O` | Open / Close passenger doors at platform |
| **Horn** | `H` | Sound dual-tone air horn |
| **Headlight** | `L` | Toggle locomotive headlight beam |
| **Monsoon** | `W` | Cycle Weather (Clear, Drizzle, Torrential Downpour, Thunderstorm ⚡) |
| **Time of Day** | `T` | Cycle Time (Dawn 🌅, Day ☀️, Dusk 🌇, Night 🌙, Auto 🔄) |
| **Pause** | `P` / `Space` | Pause / Resume |
| **Speed** | `1` / `2` / `4` | 1x, 2x, 4x speed multiplier |
| **Next Station** | `N` | Skip to next station |

---

##  Key Features

- **Bangladesh Railway Class 2900 Blue Locomotive & 5 Green Coaches**: Detailed spoked wheels rotating in sync with train velocity.
- **Frontsided Platform**: Platform slab, safety lines, station pillars, tea stalls (`চা দোকান`), and passengers are rendered in the foreground in front of the train.
- **Monsoon Thunderstorm System**: Lightning flashes across the sky with synthesized rolling thunder rumbles and torrential rain streaks.
- **Rich Bangladeshi Landscape**: Rural ponds (পুকুর), tin-roof huts (টিনের ঘর), paddy fields, grazing cows, and Turag & Meghna railway bridges.
- **Audio Synthesizer**: Procedural audio engine generating diesel engine revs, air releases, dual-tone horn chords, and rail joint clicks.
- **Terminus Concludes**: Upon reaching Chittagong, the train comes to a full stop and presents the complete journey summary and score rating.

---

##  How to Play

### 1. Web Browser (Instant Play, No Setup Required)
Simply open `index.html` in any modern web browser (Chrome, Edge, Firefox, Safari).

### 2. Java Desktop Version
Compile and run with Java 8 or higher:
```cmd
run.bat
```
Or manually:
```cmd
mkdir bin
javac -d bin -sourcepath src src/com/train/game/Main.java
java -cp bin com.train.game.Main
```
Or with Maven:
```cmd
mvn compile exec:java
```
