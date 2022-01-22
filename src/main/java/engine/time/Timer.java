package engine.time;

import engine.Window;

public class Timer {

    private final Window window;

    private final String versionName;
    private double lastFPSTime = System.nanoTime();
    private double elapsedTime = 0;
    private int framesPerSecond = 0;

    public Timer(String versionName, Window window){
        this.versionName = versionName;
        this.window = window;
    }


    private double getTime(){
        return System.nanoTime();
    }

    public void countFPS() {
        double time = getTime();
        double currentElapsedTime = time - lastFPSTime;
        lastFPSTime = time;
        elapsedTime += currentElapsedTime;
        framesPerSecond++;
        if (elapsedTime >= 1_000_000_000) {
            window.updateTitle(versionName + " | FPS: " + framesPerSecond);
            //dumpHeapSize();
            buildFPSMesh();
            framesPerSecond = 0;
            elapsedTime = 0;
        }
    }
}
