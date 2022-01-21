package engine;

import engine.Window.*;
import engine.render.GameRenderer.getWindowSizeX;
import engine.render.GameRenderer.getWindowSizeY;
import org.lwjgl.glfw.GLFW.*;

final public class MouseInput {

    private double previousPosX = -1;
    private double previousPosY = -1;

    private double currentPosX = 0;
    private double currentPosY = 0;

    private float displVecX = 0;
    private float displVecY = 0;


    private boolean  inWindow    = false;
    private boolean  leftButtonPressed  = false;
    private boolean  rightButtonPressed = false;
    private boolean  mouseLocked = true;
    private float    scroll      = 0;

    public void resetMousePosVector(){
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

    public void initMouseInput(){

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


    public float getMouseDisplVecX(){
        return displVecX;
    }

    public float getMouseDisplVecY(){
        return displVecY;
    }

    private final double[] testX = new double[1];
    private final double[] testY = new double[1];

    public void mouseInput(){


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

    public boolean isLeftButtonPressed(){
        return leftButtonPressed;
    }

    public boolean isRightButtonPressed(){
        return rightButtonPressed;
    }

    public void setMouseLocked(boolean lock){
        if(!lock) {
            glfwSetInputMode(getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        } else{
            glfwSetInputMode(getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }
        mouseLocked = lock;
        resetMousePosVector();
    }

    public boolean isMouseLocked(){
        return mouseLocked;
    }


    public float getMouseScroll(){
        float thisScroll = scroll;
        scroll = 0.0f;
        return thisScroll;
    }

    //immutable
    public double getMousePosX(){
        return currentPosX;
    }
    //immutable
    public double getMousePosY(){
        return currentPosY;
    }

    //SPECIAL gui management tool for mouse position
    public double getMousePosCenteredX(){
        return currentPosX - (getWindowSizeX() / 2f);
    }
    public double getMousePosCenteredY(){
        return (currentPosY - (getWindowSizeY() / 2f)) * -1;
    }

    public void toggleMouseLock(){
        mouseLocked = !mouseLocked;
        if(!isMouseLocked()) {
            glfwSetInputMode(getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        } else{
            glfwSetInputMode(getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }
        resetMousePosVector();
    }
}
