package engine.time;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Delta {

    private double lastLoopTime = getTime();
    private double delta;

    private double getTime() {
        return glfwGetTime();
    }

    public void calculateDelta() {
        double time = getTime();
        delta =  (time - lastLoopTime);
        lastLoopTime = time;
    }

    public double getDelta() {
        return(delta);
    }
}
