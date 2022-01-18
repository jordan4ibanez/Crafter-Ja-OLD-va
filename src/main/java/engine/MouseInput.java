package engine.base;

import org.joml.Vector2d;
import org.joml.Vector2f;

import static engine.base.Window.*;
import static engine.render.GameRenderer.getWindowSizeX;
import static engine.render.GameRenderer.getWindowSizeY;
import static org.lwjgl.glfw.GLFW.*;

final public class MouseInput {

    private static double previousPosX = -1;
    private static double previousPosY = -1;

    private static double currentPosX = 0;
    private static double currentPosY = 0;

    private static float displVecX = 0;
    private static float displVecY = 0;


    private static boolean  inWindow    = false;
    private static boolean  leftButtonPressed  = false;
    private static boolean  rightButtonPressed = false;
    private static boolean  mouseLocked = true;
    private static float    scroll      = 0;

    public static void resetMousePosVector(){
        glfwSetCursorPos(getWindowHandle(),getWindowWidth() / 2f,getWindowHeight() / 2f );

        double[] testx = new double[1];
        double[] testy = new double[1];
        glfwGetCursorPos(getWindowHandle(), testx, testy);
        currentPosX = (float)testx[0];
        currentPosY = (float)testy[0];

        currentPosX = getWindowWidth() / 2f;
        currentPosY = getWindowHeight() / 2f;

        previousPosX = currentPosX;
        previousPosY = currentPosY;
    }

    public static void initMouseInput(){

        glfwSetCursorPosCallback(getWindowHandle(), (windowHandle, xpos, ypos) -> {
            currentPosX = xpos;
            currentPosY = ypos;
        });

        glfwSetCursorEnterCallback(getWindowHandle(), (windowHandle, entered) -> inWindow = entered);

        glfwSetMouseButtonCallback(getWindowHandle(), (windowHandle, button, action, mode) -> {
            leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
        });

        glfwSetScrollCallback(getWindowHandle(), (windowHandle, xOffset, yOffset) -> scroll = (float)yOffset);
    }


    public static float getMouseDisplVecX(){
        return displVecX;
    }

    public static float getMouseDisplVecY(){
        return displVecY;
    }

    private static final double[] testX = new double[1];
    private static final double[] testY = new double[1];

    public static void mouseInput(){


        glfwGetCursorPos(getWindowHandle(), testX, testY);
        currentPosX = (float)testX[0];
        currentPosY = (float)testY[0];

        if (mouseLocked) {
            displVecX = 0;
            displVecY = 0;
            glfwSetCursorPos(getWindowHandle(), getWindowWidth() / 2d, getWindowHeight() / 2d);
            if (previousPosX > 0 && previousPosY > 0 && inWindow) {
                double deltax = currentPosX - getWindowWidth() / 2f;
                double deltay = currentPosY - getWindowHeight() / 2f;

                boolean rotateX = deltax != 0;
                boolean rotateY = deltay != 0;

                if (rotateX) {
                    displVecY = (float) deltax;
                }

                if (rotateY) {
                    displVecX = (float) deltay;
                }
            }
            previousPosX = currentPosX;
            previousPosY = currentPosY;
        } else {
            displVecX = 0;
            displVecY = 0;
        }
    }

    public static boolean isLeftButtonPressed(){
        return leftButtonPressed;
    }

    public static boolean isRightButtonPressed(){
        return rightButtonPressed;
    }

    public static void setMouseLocked(boolean lock){
        if(!lock) {
            glfwSetInputMode(getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        } else{
            glfwSetInputMode(getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }
        mouseLocked = lock;
        resetMousePosVector();
    }

    public static boolean isMouseLocked(){
        return mouseLocked;
    }


    public static float getMouseScroll(){
        float thisScroll = scroll;
        scroll = 0.0f;
        return thisScroll;
    }

    //immutable
    public static double getMousePosX(){
        return currentPosX;
    }
    //immutable
    public static double getMousePosY(){
        return currentPosY;
    }

    //SPECIAL gui management tool for mouse position
    public static double getMousePosCenteredX(){
        return currentPosX - (getWindowSizeX() / 2f);
    }
    public static double getMousePosCenteredY(){
        return (currentPosY - (getWindowSizeY() / 2f)) * -1;
    }

    public static void toggleMouseLock(){
        mouseLocked = !mouseLocked;
        if(!isMouseLocked()) {
            glfwSetInputMode(getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        } else{
            glfwSetInputMode(getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }
        resetMousePosVector();
    }
}
