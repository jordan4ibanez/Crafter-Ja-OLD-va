package engine.time;

import static java.lang.System.nanoTime;

public class Delta {

    private double lastLoopTime = nanoTime();
    private double delta;

    private double getTime() {
        return nanoTime();
    }

    public void calculateDelta() {
        double time = getTime();
        delta =  (time - lastLoopTime) / 1_000_000;
        lastLoopTime = time;
    }

    public double getDelta() {
        return(delta);
    }
}
