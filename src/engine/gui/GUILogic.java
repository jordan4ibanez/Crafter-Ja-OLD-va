package engine.gui;

import game.item.Item;
import org.joml.Vector2d;
import org.joml.Vector3f;

import static engine.MouseInput.*;
import static engine.Time.getDelta;
import static engine.Window.*;
import static engine.render.GameRenderer.getWindowScale;
import static engine.render.GameRenderer.getWindowSize;
import static engine.scene.SceneHandler.setScene;
import static engine.settings.Settings.*;
import static engine.sound.SoundAPI.playSound;
import static game.player.Inventory.*;
import static game.player.Player.*;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class GUILogic {

    private static String oldSelection;
    private static final Vector3f playerRot = new Vector3f(0,0,0);
    private static boolean paused = false;
    private static int[] invSelection;
    private static boolean mouseButtonPushed = false;
    private static boolean mouseButtonWasPushed = false;
    private static boolean pollingButtonInputs = false;
    private static byte lockedOnButtonInput = -1;

    //0 main
    //1 settings base
    //2 buttons settings
    private static byte menuPage = 0;

    private static final GUIObject[] gamePauseMenuGUI = new GUIObject[]{
            new GUIObject("CONTINUE" , new Vector2d(0, 30), 10, 1),
            new GUIObject("SETTINGS" , new Vector2d(0, 10), 10,1),
            new GUIObject("QUIT TO MAIN MENU" , new Vector2d(0, -10), 10,1),
            new GUIObject("QUIT GAME" , new Vector2d(0, -30), 10,1),
    };

    private static final GUIObject[] gameSettingsMenuGUI = new GUIObject[]{
            new GUIObject("CONTROLS" ,             new Vector2d(0, 35), 12, 1),
            new GUIObject("VSYNC: ON" ,            new Vector2d(0, 21), 12, 1),
            new GUIObject("GRAPHICS MODE: FANCY" , new Vector2d(0, 7), 12,1),
            new GUIObject("RENDER DISTANCE: 5" ,   new Vector2d(0, -7), 12,1),
            new GUIObject("LAZY CHUNK LOADING: FALSE" , new Vector2d(0, -21), 12,1),
            new GUIObject("BACK" ,                  new Vector2d(0, -35), 12,1),
    };

    private static final GUIObject[] controlsMenuGUI = new GUIObject[]{
            new GUIObject("FORWARD: " + quickConvertKeyCode(getKeyForward()) , new Vector2d(-35, 30), 6, 1),
            new GUIObject("BACK: " + quickConvertKeyCode(getKeyBack()), new Vector2d(35, 30), 6, 1),
            new GUIObject("LEFT: " + quickConvertKeyCode(getKeyLeft()), new Vector2d(-35, 15), 6, 1),
            new GUIObject("RIGHT: " + quickConvertKeyCode(getKeyRight()), new Vector2d(35, 15), 6, 1),

            new GUIObject("SNEAK: " + quickConvertKeyCode(getKeySneak()), new Vector2d(-35, 0), 6, 1),
            new GUIObject("DROP: " + quickConvertKeyCode(getKeyDrop()), new Vector2d(35, 0), 6, 1),
            new GUIObject("JUMP: " + quickConvertKeyCode(getKeyJump()), new Vector2d(-35, -15), 6, 1),
            new GUIObject("INVENTORY: " + quickConvertKeyCode(getKeyInventory()) , new Vector2d(35, -15), 6, 1),

            new GUIObject("BACK" , new Vector2d(0, -30), 5, 1),
    };


    private static String quickConvertKeyCode(int keyCode){
        char code = (char)keyCode;

        if(code == 'Ŕ'){
            return "SHIFT";
        } else if (code == ' '){
            return "SPACE";
        } else if (code == 'Ř'){
            return "SHIFT";
        }

        return code + "";
    }


    public static GUIObject[] getGamePauseMenuGUI(){
        if (menuPage == 0) {
            return gamePauseMenuGUI;
        } else if (menuPage == 1){
            return gameSettingsMenuGUI;
        } else if (menuPage == 2){
            return controlsMenuGUI;
        }

        //have to return something
        return gameSettingsMenuGUI;
    }



    public static int[] getInvSelection(){
        return invSelection;
    }

    public static void togglePauseMenu(){
        setPaused(!isPaused());
        if (!isPaused()){
            menuPage = 0;
            pollingButtonInputs = false;
            lockedOnButtonInput = -1;
            flushControlsMenu();
        }
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


    private static void flushControlsMenu(){
        controlsMenuGUI[0].updateTextCenteredFixed("FORWARD: " + quickConvertKeyCode(getKeyForward()));
        controlsMenuGUI[1].updateTextCenteredFixed("BACK: " + quickConvertKeyCode(getKeyBack()));
        controlsMenuGUI[2].updateTextCenteredFixed("LEFT: " + quickConvertKeyCode(getKeyLeft()));
        controlsMenuGUI[3].updateTextCenteredFixed("RIGHT: " + quickConvertKeyCode(getKeyRight()));
        controlsMenuGUI[4].updateTextCenteredFixed("SNEAK: " + quickConvertKeyCode(getKeySneak()));
        controlsMenuGUI[5].updateTextCenteredFixed("DROP: " + quickConvertKeyCode(getKeyDrop()));
        controlsMenuGUI[6].updateTextCenteredFixed("JUMP: " + quickConvertKeyCode(getKeyJump()));
        controlsMenuGUI[7].updateTextCenteredFixed("INVENTORY: " + quickConvertKeyCode(getKeyInventory()));
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
                } else {
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
            //root pause menu
            if (menuPage == 0) {
                byte selection = doGUIMouseCollisionDetection(gamePauseMenuGUI);
                //0 continue
                //1 settings
                //2 quit

                if (selection >= 0 && isLeftButtonPressed() && !mouseButtonPushed && !mouseButtonWasPushed) {
                    playSound("button");
                    mouseButtonPushed = true;

                    if (selection == 0) {
                        toggleMouseLock();
                        setPaused(false);
                    } else if (selection == 1) {
                        menuPage = 1;
                    } else if (selection == 2) {
                        setScene((byte) 0);
                        setPaused(false);
                    } else if (selection == 3) {
                        glfwSetWindowShouldClose(getWindowHandle(), true);
                    }
                } else if (!isLeftButtonPressed()) {
                    mouseButtonPushed = false;
                }
            //settings menu
            } else if (menuPage == 1) {
                byte selection = doGUIMouseCollisionDetection(gameSettingsMenuGUI);

                //0 - controls
                //1 - vsync
                //2 - graphics render mode
                //3 - render distance
                //4 - lazy chunk loading
                //5 - back

                if (selection >= 0 && isLeftButtonPressed() && !mouseButtonPushed && !mouseButtonWasPushed) {
                    playSound("button");

                    mouseButtonPushed = true;

                    switch (selection){
                        case 0:
                            menuPage = 2;
                            break;
                        case 1:
                            System.out.println("vsync");
                            break;
                        case 2:
                            System.out.println("graphics render mode");
                            break;
                        case 3:
                            System.out.println("render distance");
                            break;
                        case 4:
                            System.out.println("lazy chunk loading");
                            break;
                        case 5:
                            menuPage = 0;
                            break;
                    }
                } else if (!isLeftButtonPressed()) {
                    mouseButtonPushed = false;
                }
            //control reassignment menu
            } else if (menuPage == 2){
                byte selection = doGUIMouseCollisionDetection(controlsMenuGUI);
                //0 - forward
                //1 - back
                //2 - left
                //3 - right
                //4 - sneak
                //5 - drop
                //6 - jump
                //7 - inventory
                //8 - back

                //todo: fix duplicating keys
                if (lockedOnButtonInput >= 0 && pollingButtonInputs) {
                    int dumpedKey = getDumpedKey();
                    //poll data stream of key inputs
                    if (dumpedKey >= 0){

                        //forward
                        if (lockedOnButtonInput == 0) {
                            setKeyForward(dumpedKey);
                            lockedOnButtonInput = -1;
                            pollingButtonInputs = false;
                            controlsMenuGUI[0].updateTextCenteredFixed("FORWARD: " + quickConvertKeyCode(dumpedKey));
                            saveSettings();
                        //back
                        } else if (lockedOnButtonInput == 1){
                            setKeyBack(dumpedKey);
                            lockedOnButtonInput = -1;
                            pollingButtonInputs = false;
                            controlsMenuGUI[1].updateTextCenteredFixed("BACK: " + quickConvertKeyCode(dumpedKey));
                            saveSettings();
                        //left
                        } else if (lockedOnButtonInput == 2){
                            setKeyLeft(dumpedKey);
                            lockedOnButtonInput = -1;
                            pollingButtonInputs = false;
                            controlsMenuGUI[2].updateTextCenteredFixed("LEFT: " + quickConvertKeyCode(dumpedKey));
                            saveSettings();
                        //right
                        } else if (lockedOnButtonInput == 3) {
                            setKeyRight(dumpedKey);
                            lockedOnButtonInput = -1;
                            pollingButtonInputs = false;
                            controlsMenuGUI[3].updateTextCenteredFixed("RIGHT: " + quickConvertKeyCode(dumpedKey));
                            saveSettings();
                        //sneak
                        } else if (lockedOnButtonInput == 4) {
                            setKeySneak(dumpedKey);
                            lockedOnButtonInput = -1;
                            pollingButtonInputs = false;
                            controlsMenuGUI[4].updateTextCenteredFixed("SNEAK: " + quickConvertKeyCode(dumpedKey));
                            saveSettings();
                        //drop
                        } else if (lockedOnButtonInput == 5) {
                            setKeyDrop(dumpedKey);
                            lockedOnButtonInput = -1;
                            pollingButtonInputs = false;
                            controlsMenuGUI[5].updateTextCenteredFixed("DROP: " + quickConvertKeyCode(dumpedKey));
                            saveSettings();
                        //jump
                        } else if (lockedOnButtonInput == 6) {
                            setKeyJump(dumpedKey);
                            lockedOnButtonInput = -1;
                            pollingButtonInputs = false;
                            controlsMenuGUI[6].updateTextCenteredFixed("JUMP: " + quickConvertKeyCode(dumpedKey));
                            saveSettings();
                        //jump
                        } else if (lockedOnButtonInput == 7) {
                            setKeyInventory(dumpedKey);
                            lockedOnButtonInput = -1;
                            pollingButtonInputs = false;
                            controlsMenuGUI[7].updateTextCenteredFixed("INVENTORY: " + quickConvertKeyCode(dumpedKey));
                            saveSettings();
                        }
                    }
                }

                if (lockedOnButtonInput < 0 && !pollingButtonInputs && selection >= 0 && isLeftButtonPressed() && !mouseButtonPushed && !mouseButtonWasPushed) {

                    playSound("button");

                    switch (selection){
                        case 0:
                            lockedOnButtonInput = 0;
                            pollingButtonInputs = true;
                            controlsMenuGUI[0].updateTextCenteredFixed("FORWARD:>" + quickConvertKeyCode(getKeyForward()) + "<");
                            break;
                        case 1:
                            lockedOnButtonInput = 1;
                            pollingButtonInputs = true;
                            controlsMenuGUI[1].updateTextCenteredFixed("BACK:>" + quickConvertKeyCode(getKeyBack()) + "<");
                            break;
                        case 2:
                            lockedOnButtonInput = 2;
                            pollingButtonInputs = true;
                            controlsMenuGUI[2].updateTextCenteredFixed("LEFT:>" + quickConvertKeyCode(getKeyLeft()) + "<");
                            break;
                        case 3:
                            lockedOnButtonInput = 3;
                            pollingButtonInputs = true;
                            controlsMenuGUI[3].updateTextCenteredFixed("RIGHT:>" + quickConvertKeyCode(getKeyRight()) + "<");
                            break;
                        case 4:
                            lockedOnButtonInput = 4;
                            pollingButtonInputs = true;
                            controlsMenuGUI[4].updateTextCenteredFixed("SNEAK:>" + quickConvertKeyCode(getKeySneak()) + "<");
                            break;
                        case 5:
                            lockedOnButtonInput = 5;
                            pollingButtonInputs = true;
                            controlsMenuGUI[5].updateTextCenteredFixed("DROP:>" + quickConvertKeyCode(getKeyDrop()) + "<");
                            break;
                        case 6:
                            lockedOnButtonInput = 6;
                            pollingButtonInputs = true;
                            controlsMenuGUI[6].updateTextCenteredFixed("JUMP:>" + quickConvertKeyCode(getKeyJump()) + "<");
                            break;
                        case 7:
                            lockedOnButtonInput = 7;
                            pollingButtonInputs = true;
                            controlsMenuGUI[7].updateTextCenteredFixed("INVENTORY:>" + quickConvertKeyCode(getKeyInventory()) + "<");
                            break;
                        case 8:
                            menuPage = 1;
                            mouseButtonPushed = true;
                            break;
                    }
                } else if (!isLeftButtonPressed()) {
                    mouseButtonPushed = false;
                }
            }
            mouseButtonWasPushed = isLeftButtonPressed();
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
