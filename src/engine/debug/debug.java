package engine.debug;

import org.joml.Vector2f;

import static engine.MouseInput.getMouseDisplVec;
import static engine.MouseInput.toggleMouseLock;
import static engine.Time.getDelta;
import static engine.Window.*;
import static engine.graphics.Camera.*;
import static org.lwjgl.glfw.GLFW.*;

public class debug {

    private static boolean F11Pushed           = false;
    private static boolean EPushed = false;

    public static void debugInput(){

        float delta = getDelta() * 2f;



        if (isKeyPressed(GLFW_KEY_E)) {
            if (!EPushed) {
                EPushed = true;
                toggleMouseLock();
            }
        } else if (!isKeyPressed(GLFW_KEY_E)){
            EPushed = false;
        }


        if (isKeyPressed(GLFW_KEY_W)){
            float yaw = (float)Math.toRadians(getCameraRotation().y) + (float)Math.PI;
            moveCameraPosition((float)(Math.sin(-yaw)) * delta,0,0);
            moveCameraPosition(0,0, (float)(Math.cos(yaw)) * delta);
        }
        if (isKeyPressed(GLFW_KEY_S)){
            //no mod needed
            float yaw = (float)Math.toRadians(getCameraRotation().y);
            moveCameraPosition((float)(Math.sin(-yaw)) * delta,0,0);
            moveCameraPosition(0,0, (float)(Math.cos(yaw) ) * delta);
        }

        if (isKeyPressed(GLFW_KEY_D)){
            float yaw = (float)Math.toRadians(getCameraRotation().y) - (float)(Math.PI /2);
            moveCameraPosition( (float)(Math.sin(-yaw)) * delta,0,0);
            moveCameraPosition(0,0, (float)(Math.cos(yaw) ) * delta);
        }

        if (isKeyPressed(GLFW_KEY_A)){
            float yaw = (float)Math.toRadians(getCameraRotation().y) + (float)(Math.PI /2);
            moveCameraPosition( (float)(Math.sin(-yaw)) * delta,0,0);
            moveCameraPosition(0,0,(float)(Math.cos(yaw) ) * delta);
        }

        if (isKeyPressed(GLFW_KEY_F11)) {
            if (!F11Pushed) {
                F11Pushed = true;
                toggleFullScreen();
            }
        } else if (!isKeyPressed(GLFW_KEY_F11)){
            F11Pushed = false;
        }

        if (isKeyPressed(GLFW_KEY_ESCAPE)){
            glfwSetWindowShouldClose(getWindowHandle(), true);
        }

        debugCameraUpdate();
    }

    private final static float MOUSE_SENSITIVITY = 0.009f;

    public static void debugCameraUpdate(){

//        update camera based on mouse
        Vector2f rotVec = getMouseDisplVec();
        moveCameraRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);

//        limit camera pitch
        if (getCameraRotation().x < -90f) {
            moveCameraRotation((90f + getCameraRotation().x) * -1f, 0, 0);
        }
        if (getCameraRotation().x > 90f){
            moveCameraRotation((getCameraRotation().x - 90f) * -1f , 0, 0);
        }
//        loop camera yaw
        if (getCameraRotation().y < -180f){
            moveCameraRotation(0,360f, 0);
        }
        if (getCameraRotation().y > 180f){
            moveCameraRotation(0,-360f, 0);
        }
    }
}
