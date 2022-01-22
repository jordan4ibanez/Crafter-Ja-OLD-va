package engine;

import static org.lwjgl.glfw.GLFW.*;

final public class Mouse {

    private final Window window;

    public Mouse(Window window){
        this.window = window;
    }

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
        glfwSetCursorPos(window.getWindowHandle(),window.getWidth() / 2f,window.getHeight() / 2f );

        double[] testx = new double[1];
        double[] testy = new double[1];
        glfwGetCursorPos(window.getWindowHandle(), testx, testy);
        currentPosX = (float)testx[0];
        currentPosY = (float)testy[0];

        currentPosX = window.getWidth() / 2f;
        currentPosY = window.getHeight() / 2f;

        previousPosX = currentPosX;
        previousPosY = currentPosY;
    }

    public void initMouseInput(){

        glfwSetCursorPosCallback(window.getWindowHandle(), (windowHandle, xPos, yPos) -> {
            currentPosX = xPos;
            currentPosY = yPos;
        });

        glfwSetCursorEnterCallback(window.getWindowHandle(), (windowHandle, entered) -> inWindow = entered);

        glfwSetMouseButtonCallback(window.getWindowHandle(), (windowHandle, button, action, mode) -> {
            leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
        });

        glfwSetScrollCallback(window.getWindowHandle(), (windowHandle, xOffset, yOffset) -> scroll = (float)yOffset);
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

        glfwGetCursorPos(window.getWindowHandle(), testX, testY);
        currentPosX = (float)testX[0];
        currentPosY = (float)testY[0];

        if (mouseLocked) {
            displVecX = 0;
            displVecY = 0;
            glfwSetCursorPos(window.getWindowHandle(), window.getWidth() / 2d, window.getHeight() / 2d);
            if (previousPosX > 0 && previousPosY > 0 && inWindow) {
                double deltaX = currentPosX - window.getWidth() / 2f;
                double deltaY = currentPosY - window.getHeight() / 2f;

                boolean rotateX = deltaX != 0;
                boolean rotateY = deltaY != 0;

                if (rotateX) {
                    displVecY = (float) deltaX;
                }

                if (rotateY) {
                    displVecX = (float) deltaY;
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
            glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        } else{
            glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
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
        return currentPosX - (window.getWidth() / 2f);
    }
    public double getMousePosCenteredY(){
        return (currentPosY - (window.getHeight() / 2f)) * -1;
    }

    public void toggleMouseLock(){
        mouseLocked = !mouseLocked;
        if(!isMouseLocked()) {
            glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        } else{
            glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }
        resetMousePosVector();
    }
}
