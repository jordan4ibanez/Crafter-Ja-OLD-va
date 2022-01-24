package engine.time;

import engine.Window;
import engine.graphics.Mesh;
import engine.gui.TextHandling;

public class Timer {

    private final TextHandling textHandling = new TextHandling();

    private Window window;
    private String versionName;
    private Mesh fpsMesh;

    public void setWindow(Window window){
        if (this.window == null){
            this.window = window;
        }
    }
    public void setVersionName(String versionName){
        if (this.versionName == null){
            this.versionName = versionName;
        }
    }

    private double lastFPSTime = System.nanoTime();
    private double elapsedTime = 0;
    private int framesPerSecond = 0;

    public Timer(){
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
            buildFPSMesh(framesPerSecond);
            framesPerSecond = 0;
            elapsedTime = 0;
        }
    }

    public void buildFPSMesh(int framesPerSecond) {
        fpsMesh.cleanUp(false);
        fpsMesh = textHandling.createTextWithShadow("FPS: " + framesPerSecond, 1f, 1f, 1f);
    }

    public Mesh getFpsMesh(){
        return this.fpsMesh;
    }
}
