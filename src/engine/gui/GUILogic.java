package engine.gui;

import game.item.Item;
import org.joml.Vector2d;
import org.joml.Vector3f;

import static engine.MouseInput.*;
import static engine.Time.getDelta;
import static engine.Window.*;
import static engine.gui.GUI.toggleVsyncMesh;
import static engine.render.GameRenderer.getWindowScale;
import static engine.render.GameRenderer.getWindowSize;
import static engine.scene.SceneHandler.setScene;
import static engine.sound.SoundAPI.playSound;
import static game.player.Inventory.*;
import static game.player.Player.*;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class GUILogic {

    private static String oldSelection;
    private static final Vector3f playerRot = new Vector3f(0,0,0);
    private static boolean paused = false;
    private static int[] invSelection;
    private static boolean clicking = false;
    private static boolean mouseButtonPushed = false;
    private static int pauseButtonSelection;

    private static final GUIObject[] gamePauseMenuGUI = new GUIObject[]{
            new GUIObject("CONTINUE" , new Vector2d(0, 25), 10, 1),
            new GUIObject("SETTINGS" , new Vector2d(0, 0), 10,1),
            new GUIObject("QUIT TO MAIN MENU" , new Vector2d(0, -25), 10,1),
    };

    public static GUIObject[] getGamePauseMenuGUI(){
        return gamePauseMenuGUI;
    }



    public static int[] getInvSelection(){
        return invSelection;
    }

    public static int getPauseButtonSelection(){
        return pauseButtonSelection;
    }

    public static boolean getIfClicking(){
        return clicking;
    }


    public static Vector3f getPlayerHudRot(){
        return playerRot;
    }

    public static void togglePauseMenu(){
        setPaused(!isPaused());
    }

    public static Vector3f getPlayerHudRotation(){
        return playerRot;
    }

    public static boolean isPaused(){
        return paused;
    }

    public static void setPaused(boolean truth){
        paused = truth;
    }



    //todo: redo this mess
    public static void hudOnStepTest(){

        float delta = getDelta();

        if (!getItemInInventorySlotName(getCurrentInventorySelection(), 0).equals(oldSelection)) {
            resetWieldHandSetupTrigger();
            oldSelection = getItemInInventorySlotName(getCurrentInventorySelection(), 0);
        }

        if (isPlayerInventoryOpen()) {

            //begin player in inventory thing
            //new scope because lazy
            {

                float windowScale = getWindowScale();
                Vector2d basePlayerPos = new Vector2d(-(windowScale / 3.75d), (windowScale / 2.8d));
                Vector2d mousePos = getMousePos();

                float rotationY = (float)((mousePos.x - (getWindowWidth()/2f)) - basePlayerPos.x) / (windowScale * 1.2f);
                rotationY *= 40f;
                playerRot.y = rotationY;


                float rotationX = (float)((mousePos.y - (getWindowHeight()/2f)) + (basePlayerPos.y /2f)) / (windowScale * 1.2f);
                rotationX *= 40f;
                playerRot.x = rotationX;
            }


            if (invSelection == null){
                invSelection = new int[2];
            } else {
                if (isLeftButtonPressed()) {
                    if (!mouseButtonPushed) {
                        mouseButtonPushed = true;

                        if (getMouseInventory() == null) {
                            setMouseInventory(getItemInInventorySlot(invSelection[0], invSelection[1]));

                            removeStackFromInventory(invSelection[0], invSelection[1]);
                        } else {
                            Item bufferItemMouse = getMouseInventory();
                            Item bufferItemInv  = getItemInInventorySlot(invSelection[0], invSelection[1]);
                            setItemInInventory(invSelection[0], invSelection[1], bufferItemMouse.name, bufferItemMouse.stack);
                            setMouseInventory(bufferItemInv);
                        }

                    }
                } else if (!isLeftButtonPressed()){
                    mouseButtonPushed = false;
                }
            }

            //need to create new object or the mouse position gets messed up
            Vector2d mousePos = new Vector2d(getMousePos());

            //work from the center
            mousePos.x -= (getWindowSize().x/2f);
            mousePos.y -= (getWindowSize().y/2f);
            //invert the Y position to follow rendering coordinate system
            mousePos.y *= -1f;

            //collision detect the lower inventory
            for (int x = 1; x <= 9; x++) {
                for (int y = -2; y > -5; y--) {
                    if (
                            mousePos.x > ((x - 5) * (getWindowScale() / 9.5f)) - ((getWindowScale()/10.5f) / 2f) && mousePos.x < ((x - 5) * (getWindowScale() / 9.5f)) + ((getWindowScale()/10.5f) / 2f) && //x axis
                                    mousePos.y > ((y+0.3f) * (getWindowScale() / 9.5f)) - ((getWindowScale()/10.5f) / 2f) && mousePos.y < ((y+0.3f) * (getWindowScale() / 9.5f)) + ((getWindowScale()/10.5f) / 2f) //y axis
                    ){
                        invSelection[0] = (x-1);
                        invSelection[1] = ((y*-1) - 1);
                        return;
                    }
                }
            }

            //collision detect the inventory hotbar (upper part)
            for (int x = 1; x <= 9; x++) {
                if (
                        mousePos.x > ((x - 5) * (getWindowScale() / 9.5f)) - ((getWindowScale()/10.5f) / 2f) && mousePos.x < ((x - 5) * (getWindowScale() / 9.5f)) + ((getWindowScale()/10.5f) / 2f) && //x axis
                                mousePos.y > (-0.5f * (getWindowScale() / 9.5f)) - ((getWindowScale()/10.5f) / 2f) && mousePos.y < (-0.5f * (getWindowScale() / 9.5f)) + ((getWindowScale()/10.5f) / 2f) //y axis
                ){
                    invSelection[0] = (x-1);
                    invSelection[1] = 0;
                    return;
                }
            }
            invSelection = null;
        } else if (isPaused()){

            byte selection = doGUIMouseCollisionDetection(gamePauseMenuGUI);

            //0 continue
            //1 settings
            //2 quit

            if (selection >= 0 && isLeftButtonPressed() && !mouseButtonPushed){

                playSound("button");
                mouseButtonPushed = true;

                if (selection == 0){
                    toggleMouseLock();
                    setPaused(false);
                } else if (selection == 1){
                    System.out.println("YOU FORGOT TO ADD THE SETTINGS MENU >:(");
                } else if (selection == 2){
                    //glfwSetWindowShouldClose(getWindowHandle(), true);
                    setScene((byte) 0);
                    setPaused(false);
                }
            } else if (!isLeftButtonPressed()) {
                mouseButtonPushed = false;
            }
        }
    }

    public static byte doGUIMouseCollisionDetection(GUIObject[] guiElements){
        byte selected = -1;
        float windowScale = getWindowScale();

        //need to create new object or the mouse position gets messed up
        Vector2d mousePos = new Vector2d(getMousePos());

        //work from the center
        mousePos.x -= (getWindowSize().x/2f);
        mousePos.y -= (getWindowSize().y/2f);
        byte count = 0;
        for (GUIObject thisButton : guiElements){
            double xPos = thisButton.pos.x * (windowScale / 100d);
            double yPos = thisButton.pos.y * (windowScale / 100d);

            //y is inverted because GPU math
            yPos *= -1;

            float xAdder = (float)Math.ceil(windowScale / ( 20 / thisButton.buttonScale.x)) / 2f;
            float yAdder = (float)Math.ceil(windowScale / (20 / thisButton.buttonScale.y)) / 2f;

            if (mousePos.y <= yPos + yAdder && mousePos.y >= yPos - yAdder && mousePos.x <= xPos + xAdder && mousePos.x >= xPos - xAdder){
                thisButton.selected = true;
                selected = count;
            } else {
                thisButton.selected = false;
            }

            count++;
        }


        return selected;
    }
}
