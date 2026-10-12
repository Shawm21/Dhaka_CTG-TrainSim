@echo off
echo Building Intercity Pixel Train: Dhaka to Chittagong...
mkdir bin 2>nul
javac -d bin -sourcepath src src/com/train/game/Main.java
if %ERRORLEVEL% EQU 0 (
    echo Launching Game...
    java -cp bin com.train.game.Main
) else (
    echo Compilation failed. Ensure JDK (javac and java) is installed and added to PATH.
    pause
)
