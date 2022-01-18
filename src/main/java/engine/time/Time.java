package engine.time;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Time {

    private static double lastLoopTime = getTime();
    private static double delta;

    private static double getTime() {
        return glfwGetTime();
    }

    public static void calculateDelta() {
        double time = getTime();
        delta =  (time - lastLoopTime);
        lastLoopTime = time;
    }

    public static double getDelta() {
        return(delta);
    }
}
