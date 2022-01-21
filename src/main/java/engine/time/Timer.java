package engine.time;

import engine.Window.updateWindowTitle;
import engine.gui.GUI.buildFPSMesh;
import game.Crafter.getVersionName;

public class Timer {

    public double timerGetTime(){
        return System.nanoTime();
    }


    private double lastFPSTime = System.nanoTime();
    private double elapsedTime = 0;
    private int framesPerSecond = 0;

    private int currentFpsCount = 0;

    public int getFpsCounted(){
        return (currentFpsCount);
    }

    public void countFPS() {
        double time = timerGetTime();
        double currentElapsedTime = time - lastFPSTime;
        lastFPSTime = time;
        elapsedTime += currentElapsedTime;
        framesPerSecond++;
        if (elapsedTime >= 1_000_000_000) {
            updateWindowTitle(getVersionName() + " | FPS: " + framesPerSecond);
            currentFpsCount = framesPerSecond;
            //dumpHeapSize();
            buildFPSMesh();
            framesPerSecond = 0;
            elapsedTime = 0;
        }
    }
}
