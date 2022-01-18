package engine.time;

import static engine.Window.updateWindowTitle;
import static engine.gui.GUI.buildFPSMesh;
import static game.Crafter.getVersionName;

public class Timer {

    public static double timerGetTime(){
        return System.nanoTime();
    }


    private static double lastFPSTime = System.nanoTime();
    private static double elapsedTime = 0;
    private static int framesPerSecond = 0;

    private static int currentFpsCount = 0;

    public static int getFpsCounted(){
        return (currentFpsCount);
    }

    public static void countFPS() {
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
