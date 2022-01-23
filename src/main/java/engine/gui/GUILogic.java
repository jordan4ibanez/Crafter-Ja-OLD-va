package engine.gui;

import engine.Mouse;
import engine.Window;
import engine.settings.Settings;
import engine.time.Delta;
import game.chat.Chat;
import game.mainMenu.MainMenu;
import game.player.Player;
import org.joml.Vector2d;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class GUILogic {

    private final Settings settings;
    private final Mouse mouse;
    private final Window window;
    private final Chat chat;
    private final Player player;
    private final Delta delta;
    private final MainMenu mainMenu;


    private boolean paused = false;
    private boolean chatOpen = false;
    private int chatBoxEntryKey = 84;

    private boolean mouseButtonPushed = false;
    private boolean mouseButtonWasPushed = false;
    private boolean pollingButtonInputs = false;

    private byte lockedOnButtonInput = -1;

    //health bar elements
    //calculated per half heart
    private final byte[] healthHudArray = new byte[10];
    private boolean heartUp = true;
    private final float[] healthHudFloatArray = new float[10];

    //0 main
    //1 settings base
    //2 buttons settings
    private byte menuPage = 0;

    private final GUIObject[] gamePauseMenuGUI;

    private final GUIObject[] gameSettingsMenuGUI;

    private final GUIObject[] controlsMenuGUI;

    public GUILogic(Settings settings, Mouse mouse, Window window, Chat chat, Player player, Delta delta, MainMenu mainMenu){
        this.settings = settings;
        this.mouse = mouse;
        this.window = window;
        this.chat = chat;
        this.player = player;
        this.delta = delta;
        this.mainMenu = mainMenu;

        gamePauseMenuGUI = new GUIObject[]{
                new GUIObject("CONTINUE" , new Vector2d(0, 30), 10, 1),
                new GUIObject("SETTINGS" , new Vector2d(0, 10), 10,1),
                new GUIObject("QUIT TO MAIN MENU" , new Vector2d(0, -10), 10,1),
                new GUIObject("QUIT GAME" , new Vector2d(0, -30), 10,1),
        };

        gameSettingsMenuGUI = new GUIObject[]{
                new GUIObject("CONTROLS" ,             new Vector2d(0, 35), 12, 1),
                new GUIObject("VSYNC: " + boolToString(settings.getSettingsVsync()),            new Vector2d(0, 21), 12, 1),
                new GUIObject("GRAPHICS MODE: " + graphicsThing(settings.getGraphicsMode()) , new Vector2d(0, 7), 12,1),
                new GUIObject("RENDER DISTANCE: " + settings.getRenderDistance(),   new Vector2d(0, -7), 12,1),
                //new GUIObject("CHUNK LOADING: " + convertChunkLoadText(settings.getSettingsChunkLoad()), new Vector2d(0, -21), 12,1),
                new GUIObject("BACK" ,                  new Vector2d(0, -35), 12,1),
        };

        controlsMenuGUI = new GUIObject[]{
                new GUIObject("FORWARD: " + quickConvertKeyCode(settings.getKeyForward()) , new Vector2d(-35, 30), 6, 1),
                new GUIObject("BACK: " + quickConvertKeyCode(settings.getKeyBack()), new Vector2d(35, 30), 6, 1),
                new GUIObject("LEFT: " + quickConvertKeyCode(settings.getKeyLeft()), new Vector2d(-35, 15), 6, 1),
                new GUIObject("RIGHT: " + quickConvertKeyCode(settings.getKeyRight()), new Vector2d(35, 15), 6, 1),

                new GUIObject("SNEAK: " + quickConvertKeyCode(settings.getKeySneak()), new Vector2d(-35, 0), 6, 1),
                new GUIObject("DROP: " + quickConvertKeyCode(settings.getKeyDrop()), new Vector2d(35, 0), 6, 1),
                new GUIObject("JUMP: " + quickConvertKeyCode(settings.getKeyJump()), new Vector2d(-35, -15), 6, 1),
                new GUIObject("INVENTORY: " + quickConvertKeyCode(settings.getKeyInventory()) , new Vector2d(35, -15), 6, 1),

                new GUIObject("BACK" , new Vector2d(0, -30), 5, 1),
        };
    }

    public void sendAndFlushChatMessage(){
        //sendChatMessage(getCurrentChatMessage());
        //clearCurrentChatMessage();
        setChatOpen(false);
    }


    private String quickConvertKeyCode(int keyCode){
        
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

    private String boolToString(boolean bool){
        if (bool){
            return "ON";
        }
        return "OFF";
    }

    private String graphicsThing(boolean bool){
        if (bool){
            return "FANCY";
        }
        return "FAST";
    }


    public GUIObject[] getGamePauseMenuGUI(){
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


    private String convertChunkLoadText(byte input){
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


    public void togglePauseMenu(){
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

    public boolean isPaused(){
        return paused;
    }

    public boolean isChatOpen(){
        return chatOpen;
    }

    public void setChatOpen(boolean truth){
        chatOpen = truth;
        mouse.toggleMouseLock();
        chatBoxEntryKey = 84;
        //setCurrentChatMessage("");
    }

    public void setPaused(boolean truth){
        paused = truth;
    }


    private void flushControlsMenu(){
        controlsMenuGUI[0].updateTextCenteredFixed("FORWARD: " + quickConvertKeyCode(settings.getKeyForward()));
        controlsMenuGUI[1].updateTextCenteredFixed("BACK: " + quickConvertKeyCode(settings.getKeyBack()));
        controlsMenuGUI[2].updateTextCenteredFixed("LEFT: " + quickConvertKeyCode(settings.getKeyLeft()));
        controlsMenuGUI[3].updateTextCenteredFixed("RIGHT: " + quickConvertKeyCode(settings.getKeyRight()));
        controlsMenuGUI[4].updateTextCenteredFixed("SNEAK: " + quickConvertKeyCode(settings.getKeySneak()));
        controlsMenuGUI[5].updateTextCenteredFixed("DROP: " + quickConvertKeyCode(settings.getKeyDrop()));
        controlsMenuGUI[6].updateTextCenteredFixed("JUMP: " + quickConvertKeyCode(settings.getKeyJump()));
        controlsMenuGUI[7].updateTextCenteredFixed("INVENTORY: " + quickConvertKeyCode(settings.getKeyInventory()));
    }


    public void pauseMenuOnTick(){

        if (isPaused()){
            //root pause menu
            if (menuPage == 0) {
                byte selection = doGUIMouseCollisionDetection(gamePauseMenuGUI);
                //0 continue
                //1 settings
                //2 quit

                if (selection >= 0 && mouse.isLeftButtonPressed() && !mouseButtonPushed && !mouseButtonWasPushed) {
                    //playSound("button");
                    mouseButtonPushed = true;

                    if (selection == 0) {
                        mouse.toggleMouseLock();
                        setPaused(false);
                    } else if (selection == 1) {
                        menuPage = 1;
                    } else if (selection == 2) {
                        closeWorldDataBase();
                        mainMenu.resetMainMenuPage();
                        mainMenu.resetMainMenu();
                        setScene((byte) 0);
                        //disconnectClient();
                        setPaused(false);
                    } else if (selection == 3) {
                        closeWorldDataBase();
                        disconnectClient();
                        glfwSetWindowShouldClose(window.getWindowHandle(), true);
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

                if (selection >= 0 && mouse.isLeftButtonPressed() && !mouseButtonPushed && !mouseButtonWasPushed) {
                    //playSound("button");

                    mouseButtonPushed = true;

                    switch (selection) {
                        case 0 ->
                                //goto controls menu
                                menuPage = 2;
                        case 1 -> {
                            boolean vSync = !settings.getSettingsVsync();
                            settings.setSettingsVsync(vSync);
                            gameSettingsMenuGUI[1].updateTextCenteredFixed("VSYNC: " + boolToString(vSync));
                            settings.saveSettings();
                        }
                        case 2 -> {
                            boolean graphicsMode = !settings.getGraphicsMode();
                            settings.setGraphicsMode(graphicsMode);
                            gameSettingsMenuGUI[2].updateTextCenteredFixed("GRAPHICS: " + graphicsThing(graphicsMode));
                            settings.saveSettings();
                        }
                        case 3 -> {
                            int renderDistance = settings.getRenderDistance();
                            renderDistance = switch (renderDistance) {
                                case 5 -> 7;
                                case 7 -> 9;
                                case 9 -> 3;
                                default -> 5;
                            };
                            settings.setRenderDistance(renderDistance, true);
                            gameSettingsMenuGUI[3].updateTextCenteredFixed("RENDER DISTANCE: " + renderDistance);
                            settings.saveSettings();
                        }
                        /*case 4 -> {
                            //byte chunkLoad = settings.getSettingsChunkLoad();
                            chunkLoad++;
                            if (chunkLoad > 5) {
                                chunkLoad = 0;
                            }
                            gameSettingsMenuGUI[4].updateTextCenteredFixed("CHUNK LOADING: " + convertChunkLoadText(chunkLoad));
                            setSettingsChunkLoad(chunkLoad);
                            saveSettings();
                        }

                         */
                        case 4 -> menuPage = 0;
                    }
                } else if (!mouse.isLeftButtonPressed()) {
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
                    int dumpedKey = window.getDumpedKey();
                    //poll data stream of key inputs
                    if (dumpedKey >= 0){

                        //forward
                        if (lockedOnButtonInput == 0) {
                            settings.setKeyForward(dumpedKey);
                            lockedOnButtonInput = -1;
                            pollingButtonInputs = false;
                            controlsMenuGUI[0].updateTextCenteredFixed("FORWARD: " + quickConvertKeyCode(dumpedKey));
                            settings.saveSettings();
                        //back
                        } else if (lockedOnButtonInput == 1){
                            settings.setKeyBack(dumpedKey);
                            lockedOnButtonInput = -1;
                            pollingButtonInputs = false;
                            controlsMenuGUI[1].updateTextCenteredFixed("BACK: " + quickConvertKeyCode(dumpedKey));
                            settings.saveSettings();
                        //left
                        } else if (lockedOnButtonInput == 2){
                            settings.setKeyLeft(dumpedKey);
                            lockedOnButtonInput = -1;
                            pollingButtonInputs = false;
                            controlsMenuGUI[2].updateTextCenteredFixed("LEFT: " + quickConvertKeyCode(dumpedKey));
                            settings.saveSettings();
                        //right
                        } else if (lockedOnButtonInput == 3) {
                            settings.setKeyRight(dumpedKey);
                            lockedOnButtonInput = -1;
                            pollingButtonInputs = false;
                            controlsMenuGUI[3].updateTextCenteredFixed("RIGHT: " + quickConvertKeyCode(dumpedKey));
                            settings.saveSettings();
                        //sneak
                        } else if (lockedOnButtonInput == 4) {
                            settings.setKeySneak(dumpedKey);
                            lockedOnButtonInput = -1;
                            pollingButtonInputs = false;
                            controlsMenuGUI[4].updateTextCenteredFixed("SNEAK: " + quickConvertKeyCode(dumpedKey));
                            settings.saveSettings();
                        //drop
                        } else if (lockedOnButtonInput == 5) {
                            settings.setKeyDrop(dumpedKey);
                            lockedOnButtonInput = -1;
                            pollingButtonInputs = false;
                            controlsMenuGUI[5].updateTextCenteredFixed("DROP: " + quickConvertKeyCode(dumpedKey));
                            settings.saveSettings();
                        //jump
                        } else if (lockedOnButtonInput == 6) {
                            settings.setKeyJump(dumpedKey);
                            lockedOnButtonInput = -1;
                            pollingButtonInputs = false;
                            controlsMenuGUI[6].updateTextCenteredFixed("JUMP: " + quickConvertKeyCode(dumpedKey));
                            settings.saveSettings();
                        //jump
                        } else if (lockedOnButtonInput == 7) {
                            settings.setKeyInventory(dumpedKey);
                            lockedOnButtonInput = -1;
                            pollingButtonInputs = false;
                            controlsMenuGUI[7].updateTextCenteredFixed("INVENTORY: " + quickConvertKeyCode(dumpedKey));
                            settings.saveSettings();
                        }
                    }
                }

                if (lockedOnButtonInput < 0 && !pollingButtonInputs && selection >= 0 && mouse.isLeftButtonPressed() && !mouseButtonPushed && !mouseButtonWasPushed) {

                    //playSound("button");

                    switch (selection) {
                        case 0 -> {
                            lockedOnButtonInput = 0;
                            pollingButtonInputs = true;
                            controlsMenuGUI[0].updateTextCenteredFixed("FORWARD:>" + quickConvertKeyCode(settings.getKeyForward()) + "<");
                        }
                        case 1 -> {
                            lockedOnButtonInput = 1;
                            pollingButtonInputs = true;
                            controlsMenuGUI[1].updateTextCenteredFixed("BACK:>" + quickConvertKeyCode(settings.getKeyBack()) + "<");
                        }
                        case 2 -> {
                            lockedOnButtonInput = 2;
                            pollingButtonInputs = true;
                            controlsMenuGUI[2].updateTextCenteredFixed("LEFT:>" + quickConvertKeyCode(settings.getKeyLeft()) + "<");
                        }
                        case 3 -> {
                            lockedOnButtonInput = 3;
                            pollingButtonInputs = true;
                            controlsMenuGUI[3].updateTextCenteredFixed("RIGHT:>" + quickConvertKeyCode(settings.getKeyRight()) + "<");
                        }
                        case 4 -> {
                            lockedOnButtonInput = 4;
                            pollingButtonInputs = true;
                            controlsMenuGUI[4].updateTextCenteredFixed("SNEAK:>" + quickConvertKeyCode(settings.getKeySneak()) + "<");
                        }
                        case 5 -> {
                            lockedOnButtonInput = 5;
                            pollingButtonInputs = true;
                            controlsMenuGUI[5].updateTextCenteredFixed("DROP:>" + quickConvertKeyCode(settings.getKeyDrop()) + "<");
                        }
                        case 6 -> {
                            lockedOnButtonInput = 6;
                            pollingButtonInputs = true;
                            controlsMenuGUI[6].updateTextCenteredFixed("JUMP:>" + quickConvertKeyCode(settings.getKeyJump()) + "<");
                        }
                        case 7 -> {
                            lockedOnButtonInput = 7;
                            pollingButtonInputs = true;
                            controlsMenuGUI[7].updateTextCenteredFixed("INVENTORY:>" + quickConvertKeyCode(settings.getKeyInventory()) + "<");
                        }
                        case 8 -> {
                            menuPage = 1;
                            mouseButtonPushed = true;
                        }
                    }
                } else if (!mouse.isLeftButtonPressed()) {
                    mouseButtonPushed = false;
                }
            }
            mouseButtonWasPushed = mouse.isLeftButtonPressed();
        } else if (chatOpen){

            int dumpedKey = window.getDumpedKey();

            if (dumpedKey != -1 && dumpedKey != chatBoxEntryKey){
                chatBoxEntryKey = dumpedKey;

                String textInput = chat.getCurrentChatMessage();

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
                        chat.setCurrentChatMessage(textInput + newChar);
                    }

                } else if (dumpedKey == 259){
                    textInput  = textInput.replaceAll(".$", "");
                    chat.setCurrentChatMessage(textInput);
                }
            } else if (dumpedKey == -1) {
                chatBoxEntryKey = -1;
            }
        }
    }


    public byte doGUIMouseCollisionDetection(GUIObject[] guiElements){
        byte selected = -1;
        float windowScale = window.getScale();

        double mousePosX = mouse.getPos().x;
        double mousePosY = mouse.getPos().y;

        //work from the center
        mousePosX -= (window.getWidth()/2f);
        mousePosY -= (window.getHeight()/2f);
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

    private boolean baseOdd = true;

    public void makeHeartsJiggle(){

        double delta = this.delta.getDelta();

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

    public void calculateHealthBarElements(){
        int health = player.getPlayerHealth();

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

    public byte[] getHealthHudArray(){
        return healthHudArray;
    }

    public float[] getHealthHudFloatArray(){
        return healthHudFloatArray;
    }
}
