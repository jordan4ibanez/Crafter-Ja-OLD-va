package engine.gui;

import org.joml.Vector2d;

import static engine.MouseInput.*;
import static engine.Window.getDumpedKey;
import static engine.Window.getWindowHandle;
import static engine.disk.SQLiteDiskHandler.closeWorldDataBase;
import static engine.network.Networking.disconnectClient;
import static engine.network.Networking.sendChatMessage;
import static engine.render.GameRenderer.*;
import static engine.scene.SceneHandler.setScene;
import static engine.settings.Settings.*;
import static engine.sound.SoundAPI.playSound;
import static engine.time.Time.getDelta;
import static game.chat.Chat.*;
import static game.mainMenu.MainMenu.resetMainMenu;
import static game.mainMenu.MainMenu.resetMainMenuPage;
import static game.player.Player.getPlayerHealth;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class GUILogic {

    private static boolean paused = false;
    private static boolean chatOpen = false;
    private static int chatBoxEntryKey = 84;

    private static boolean mouseButtonPushed = false;
    private static boolean mouseButtonWasPushed = false;
    private static boolean pollingButtonInputs = false;

    private static byte lockedOnButtonInput = -1;

    //health bar elements
    //calculated per half heart
    private static final byte[] healthHudArray = new byte[10];
    private static boolean heartUp = true;
    private static final float[] healthHudFloatArray = new float[10];

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
            new GUIObject("VSYNC: " + boolToString(getSettingsVsync()),            new Vector2d(0, 21), 12, 1),
            new GUIObject("GRAPHICS MODE: " + graphicsThing(getGraphicsMode()) , new Vector2d(0, 7), 12,1),
            new GUIObject("RENDER DISTANCE: " + getRenderDistance(),   new Vector2d(0, -7), 12,1),
            new GUIObject("CHUNK LOADING: " + convertChunkLoadText(getSettingsChunkLoad()), new Vector2d(0, -21), 12,1),
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

    public static void sendAndFlushChatMessage(){
        sendChatMessage(getCurrentChatMessage());
        clearCurrentChatMessage();
        setChatOpen(false);
    }


    private static String quickConvertKeyCode(int keyCode){
        
        System.out.println("keycode");
        
        char code = (char)keyCode;

        /*
        if (code ==  'Ŕ'){
            return "SHIFT";
        } else if (code == ' '){
            return "SPACE";
        } else if (code == 'Ř'){
            return "SHIFT";
        }
        */

        return code + "";
    }

    private static String boolToString(boolean bool){
        if (bool){
            return "ON";
        }
        return "OFF";
    }

    private static String graphicsThing(boolean bool){
        if (bool){
            return "FANCY";
        }
        return "FAST";
    }


    public static GUIObject[] getGamePauseMenuGUI(){
        switch (menuPage) {
            case 0:
                return gamePauseMenuGUI;
            case 1:
                return gameSettingsMenuGUI;
            case 2:
                return controlsMenuGUI;
            default:
                break;
        }

        //have to return something
        return gameSettingsMenuGUI;
    }


    private static String convertChunkLoadText(byte input){
        switch (input) {
            case 0:
                return "SNAIL";
            case 1:
                return "LAZY";
            case 2:
                return "NORMAL";
            case 3:
                return "SPEEDY";
            case 4:
                return "INSANE";
            case 5:
                return "FUTURE PC";
            default:
                break;
        }

        return "NULL";
    }


    public static void togglePauseMenu(){
        setPaused(!isPaused());
        if (!isPaused()){
            menuPage = 0;
            pollingButtonInputs = false;
            lockedOnButtonInput = -1;
            flushControlsMenu();

            if (chatOpen){
                setChatOpen(false);
            }
        }
    }

    public static boolean isPaused(){
        return paused;
    }

    public static boolean isChatOpen(){
        return chatOpen;
    }

    public static void setChatOpen(boolean truth){
        chatOpen = truth;
        toggleMouseLock();
        chatBoxEntryKey = 84;
        setCurrentChatMessage("");
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


    public static void pauseMenuOnTick(){

        if (isPaused()){
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
                        closeWorldDataBase();
                        resetMainMenuPage();
                        resetMainMenu();
                        setScene((byte) 0);
                        disconnectClient();
                        setPaused(false);
                    } else if (selection == 3) {
                        closeWorldDataBase();
                        disconnectClient();
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

                    switch (selection) {
                        case 0 ->
                                //goto controls menu
                                menuPage = 2;
                        case 1 -> {
                            boolean vSync = !getSettingsVsync();
                            setSettingsVsync(vSync);
                            gameSettingsMenuGUI[1].updateTextCenteredFixed("VSYNC: " + boolToString(vSync));
                            saveSettings();
                        }
                        case 2 -> {
                            boolean graphicsMode = !getGraphicsMode();
                            setGraphicsMode(graphicsMode);
                            gameSettingsMenuGUI[2].updateTextCenteredFixed("GRAPHICS: " + graphicsThing(graphicsMode));
                            saveSettings();
                        }
                        case 3 -> {
                            int renderDistance = getRenderDistance();
                            renderDistance = switch (renderDistance) {
                                case 5 -> 7;
                                case 7 -> 9;
                                case 9 -> 3;
                                default -> 5;
                            };
                            setRenderDistance(renderDistance, true);
                            gameSettingsMenuGUI[3].updateTextCenteredFixed("RENDER DISTANCE: " + renderDistance);
                            saveSettings();
                        }
                        case 4 -> {
                            byte chunkLoad = getSettingsChunkLoad();
                            chunkLoad++;
                            if (chunkLoad > 5) {
                                chunkLoad = 0;
                            }
                            gameSettingsMenuGUI[4].updateTextCenteredFixed("CHUNK LOADING: " + convertChunkLoadText(chunkLoad));
                            setSettingsChunkLoad(chunkLoad);
                            saveSettings();
                        }
                        case 5 -> menuPage = 0;
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

                    switch (selection) {
                        case 0 -> {
                            lockedOnButtonInput = 0;
                            pollingButtonInputs = true;
                            controlsMenuGUI[0].updateTextCenteredFixed("FORWARD:>" + quickConvertKeyCode(getKeyForward()) + "<");
                        }
                        case 1 -> {
                            lockedOnButtonInput = 1;
                            pollingButtonInputs = true;
                            controlsMenuGUI[1].updateTextCenteredFixed("BACK:>" + quickConvertKeyCode(getKeyBack()) + "<");
                        }
                        case 2 -> {
                            lockedOnButtonInput = 2;
                            pollingButtonInputs = true;
                            controlsMenuGUI[2].updateTextCenteredFixed("LEFT:>" + quickConvertKeyCode(getKeyLeft()) + "<");
                        }
                        case 3 -> {
                            lockedOnButtonInput = 3;
                            pollingButtonInputs = true;
                            controlsMenuGUI[3].updateTextCenteredFixed("RIGHT:>" + quickConvertKeyCode(getKeyRight()) + "<");
                        }
                        case 4 -> {
                            lockedOnButtonInput = 4;
                            pollingButtonInputs = true;
                            controlsMenuGUI[4].updateTextCenteredFixed("SNEAK:>" + quickConvertKeyCode(getKeySneak()) + "<");
                        }
                        case 5 -> {
                            lockedOnButtonInput = 5;
                            pollingButtonInputs = true;
                            controlsMenuGUI[5].updateTextCenteredFixed("DROP:>" + quickConvertKeyCode(getKeyDrop()) + "<");
                        }
                        case 6 -> {
                            lockedOnButtonInput = 6;
                            pollingButtonInputs = true;
                            controlsMenuGUI[6].updateTextCenteredFixed("JUMP:>" + quickConvertKeyCode(getKeyJump()) + "<");
                        }
                        case 7 -> {
                            lockedOnButtonInput = 7;
                            pollingButtonInputs = true;
                            controlsMenuGUI[7].updateTextCenteredFixed("INVENTORY:>" + quickConvertKeyCode(getKeyInventory()) + "<");
                        }
                        case 8 -> {
                            menuPage = 1;
                            mouseButtonPushed = true;
                        }
                    }
                } else if (!isLeftButtonPressed()) {
                    mouseButtonPushed = false;
                }
            }
            mouseButtonWasPushed = isLeftButtonPressed();
        } else if (chatOpen){

            int dumpedKey = getDumpedKey();

            if (dumpedKey != -1 && dumpedKey != chatBoxEntryKey){
                chatBoxEntryKey = dumpedKey;

                String textInput = getCurrentChatMessage();

                if (textInput == null){
                    textInput = "";
                }

                //this is a HORRIBLE way to filter text input
                if ((dumpedKey >= 65 && dumpedKey <= 90) || (dumpedKey >= 48 && dumpedKey <= 57) || dumpedKey == 45 || dumpedKey == 47 || dumpedKey == 59 || dumpedKey == 46 || dumpedKey == 32){

                    char newChar;

                    if (dumpedKey == 47){
                        newChar = '/';
                    } else if (dumpedKey == 46){
                        newChar = '.';
                    } else if (dumpedKey == 59) {
                        newChar = ':';
                    } else if (dumpedKey == 45) {
                        newChar = '-';
                    }else if (dumpedKey == 32){
                        newChar = ' ';
                    } else {
                        newChar = (char)dumpedKey;
                    }



                    //add it to the messages
                    if (textInput.length() < 256){
                        setCurrentChatMessage(textInput + newChar);
                    }

                } else if (dumpedKey == 259){
                    textInput  = textInput.replaceAll(".$", "");
                    setCurrentChatMessage(textInput);
                }
            } else if (dumpedKey == -1) {
                chatBoxEntryKey = -1;
            }
        }
    }


    public static byte doGUIMouseCollisionDetection(GUIObject[] guiElements){
        byte selected = -1;
        float windowScale = getWindowScale();

        double mousePosX = getMousePosX();
        double mousePosY = getMousePosY();

        //work from the center
        mousePosX -= (getWindowSizeX()/2f);
        mousePosY -= (getWindowSizeY()/2f);
        byte count = 0;
        for (GUIObject thisButton : guiElements){
            double xPos = thisButton.pos.x * (windowScale / 100d);
            double yPos = thisButton.pos.y * (windowScale / 100d);

            //y is inverted because GPU math
            yPos *= -1;

            float xAdder = (float)Math.ceil(windowScale / ( 20 / thisButton.buttonScale.x)) / 2f;
            float yAdder = (float)Math.ceil(windowScale / (20 / thisButton.buttonScale.y)) / 2f;

            if (mousePosY <= yPos + yAdder && mousePosY >= yPos - yAdder && mousePosX <= xPos + xAdder && mousePosX >= xPos - xAdder){
                thisButton.selected = true;
                selected = count;
            } else {
                thisButton.selected = false;
            }

            count++;
        }

        return selected;
    }

    private static boolean baseOdd = true;

    public static void makeHeartsJiggle(){

        double delta = getDelta();

        boolean odd = baseOdd;

        byte baseHeart;
        if (baseOdd){
            baseHeart = 0;
        } else {
            baseHeart = 1;
        }

        if (heartUp) {
            float workerHealth = healthHudFloatArray[baseHeart] += delta * 200f;
            if (workerHealth >= 10f){
                workerHealth = 10f;
                heartUp = false;
            }
            for (int i = 0; i < 10; i++){
                if (odd){
                    healthHudFloatArray[i] = workerHealth;
                }
                odd = !odd;
            }
        } else {
            float workerHealth = healthHudFloatArray[baseHeart] -= delta * 200f;
            if (workerHealth <= 0f){
                workerHealth = 0f;
                heartUp = true;
                baseOdd = !baseOdd;
            }
            for (int i = 0; i < 10; i++){
                if (odd){
                    healthHudFloatArray[i] = workerHealth;
                }
                odd = !odd;
            }
        }
    }

    public static void calculateHealthBarElements(){
        int health = getPlayerHealth();

        byte z = 1; //this needs to start from 1 like lua

        //compare health elements (base 2), generate new health bar
        for (byte i = 0; i < 10; i++){
            int compare = health - (z * 2);

            if (compare >= 0){
                healthHudArray[i] = 2;
            } else if (compare == -1){
                healthHudArray[i] = 1;
            } else {
                healthHudArray[i] = 0;
            }
            z++;
        }
        //System.out.println(Arrays.toString(healthHudArray));
    }

    public static byte[] getHealthHudArray(){
        return healthHudArray;
    }

    public static float[] getHealthHudFloatArray(){
        return healthHudFloatArray;
    }
}
