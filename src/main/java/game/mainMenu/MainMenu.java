package game.mainMenu;

import engine.Mouse;
import engine.Window;
import engine.disk.Disk;
import engine.graphics.Mesh;
import engine.gui.GUILogic;
import engine.gui.GUIObject;
import engine.gui.TextHandling;
import engine.scene.SceneHandler;
import engine.settings.Settings;
import engine.time.Delta;
import game.player.Player;
import org.joml.Vector2d;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class MainMenu {

    //assets and credits are objects
    private String versionName;
    private Window window;
    private final MainMenuAssets mainMenuAssets = new MainMenuAssets();
    private final Credits credits = new Credits();
    private Settings settings;
    private Disk disk;
    private Mouse mouse;
    private GUILogic guiLogic;
    private SceneHandler sceneHandler;
    private Player player;
    private final TextHandling textHandling = new TextHandling();

    private byte[][] titleBlocks;
    private double[][] titleOffsets;
    private boolean haltBlockFlyIn = false;

    private byte[][] worldTitleBlocks;
    private double[][] worldTitleOffsets;
    private boolean haltWorldBlockFlyIn = false;


    private String titleScreenText = "";
    private byte titleScreenTextLength = 0;
    private final float blockOffsetInitial = 15f;
    private float titleBounce = 10f;
    private float bounceAnimation = 0.75f;
    private float backGroundScroll = 0f;

    private float creditsScroll = -6f;

    //private final SoundSource titleMusic;
    //private final SoundSource creditsMusic;

    private final Random random = new Random();
    private boolean mouseButtonPushed = false;
    private boolean mouseButtonWasPushed = false;
    private boolean pollingButtonInputs = false;
    private byte lockedOnButtonInput = -1;

    private int multiplayerScreenTextInput = -1;

    private boolean serverConnected = false;

    public void setMenuPage(byte page) {
        menuPage = page;
        System.out.println("menu page is now " + menuPage);
    }

    //0 main
    //1 settings base
    //2 buttons settings
    //3 singleplayer worlds menu
    //4 credits
    //5 multiplayer
    //6 connecting to server
    //7 could not connect to server
    private byte menuPage = 0;

    public Credits getCredits(){
        return this.credits;
    }

    private GUIObject[] mainMenuGUI;

    private GUIObject[] mainMenuSettingsMenuGUI;

    private GUIObject[] controlsMenuGUI;

    private GUIObject[] worldSelectionGUI;

    private final GUIObject[] multiPlayerGUI = new GUIObject[]{
            new GUIObject("Name:", true, new Vector2d(0, 40)),
            new GUIObject(new Vector2d(0, 30), 10, 1),

            new GUIObject("IP Address:", true, new Vector2d(0, 10)),
            new GUIObject(new Vector2d(0, 0), 10, 1),

            new GUIObject("CONNECT", new Vector2d(0, -20), 10, 1),

            new GUIObject("BACK", new Vector2d(0, -40), 10, 1),
    };

    private final GUIObject[] multiPlayerLoadingGUI = new GUIObject[]{
            new GUIObject("CONNECTING TO SERVER...", true, new Vector2d(0, 5)),
            new GUIObject("BACK", new Vector2d(0, -5), 10, 1),
    };

    private final GUIObject[] multiplayerFailureGUI = new GUIObject[]{
            new GUIObject("COULD NOT CONNECT TO SERVER", true, new Vector2d(0, 5)),
            new GUIObject("BACK", new Vector2d(0, -5), 10, 1),
    };

    public void setSettings(Settings settings) {
        if (this.settings == null) {
            this.settings = settings;
        }
    }
    public void setDisk(Disk disk) {
        if (this.disk == null) {
            this.disk = disk;
        }
    }
    public void setMouse(Mouse mouse) {
        if (this.mouse == null) {
            this.mouse = mouse;
        }
    }
    public void setGuiLogic(GUILogic guiLogic) {
        if (this.guiLogic == null) {
            this.guiLogic = guiLogic;
        }
    }
    public void setWindow(Window window) {
        if (this.window == null) {
            this.window = window;
        }
    }
    public void setSceneHandler(SceneHandler sceneHandler) {
        if (this.sceneHandler == null) {
            this.sceneHandler = sceneHandler;
        }
    }
    public void setPlayer(Player player) {
        if (this.player == null) {
            this.player = player;
        }
    }

    public void setVersionName(String versionName){
        if (this.versionName == null){
            this.versionName = versionName;
        }
    }

    public MainMenu() {

    }
    public void initializeGUI(){

        //titleMusic = playMusic("main_menu");
        //creditsMusic = playMusic("credits");
        //creditsMusic.stop();

        mainMenuGUI = new GUIObject[]{
                new GUIObject("SINGLEPLAYER" , new Vector2d(0, 10), 10, 1),
                new GUIObject("MULTIPLAYER" , new Vector2d(0, -3), 10,1),
                new GUIObject("SETTINGS" , new Vector2d(0, -16), 10,1),
                new GUIObject("CREDITS" , new Vector2d(0, -29), 10,1),
                new GUIObject("QUIT" , new Vector2d(0, -42), 10,1),
        };

        mainMenuSettingsMenuGUI = new GUIObject[]{
                new GUIObject("CONTROLS" ,             new Vector2d(0, 35), 12, 1),
                new GUIObject("VSYNC: " + boolToString(settings.getSettingsVsync()),            new Vector2d(0, 21), 12, 1),
                new GUIObject("GRAPHICS MODE: " + graphicsThing(settings.getGraphicsMode()) , new Vector2d(0, 7), 12,1),
                new GUIObject("RENDER DISTANCE: " + settings.getRenderDistance(),   new Vector2d(0, -7), 12,1),
                new GUIObject("BACK" ,                  new Vector2d(0, -35), 12,1),
        };

        controlsMenuGUI = new GUIObject[]{
                new GUIObject("FORWARD: " + quickConvertKeyCode(settings.getKeyForward()) ,     new Vector2d(-35, 30), 6, 1),
                new GUIObject("BACK: " + quickConvertKeyCode(settings.getKeyBack()),            new Vector2d(35, 30), 6, 1),
                new GUIObject("LEFT: " + quickConvertKeyCode(settings.getKeyLeft()),            new Vector2d(-35, 15), 6, 1),
                new GUIObject("RIGHT: " + quickConvertKeyCode(settings.getKeyRight()),          new Vector2d(35, 15), 6, 1),
                new GUIObject("SNEAK: " + quickConvertKeyCode(settings.getKeySneak()),          new Vector2d(-35, 0), 6, 1),
                new GUIObject("DROP: " + quickConvertKeyCode(settings.getKeyDrop()),            new Vector2d(35, 0), 6, 1),
                new GUIObject("JUMP: " + quickConvertKeyCode(settings.getKeyJump()),            new Vector2d(-35, -15), 6, 1),
                new GUIObject("INVENTORY: " + quickConvertKeyCode(settings.getKeyInventory()) , new Vector2d(35, -15), 6, 1),
                new GUIObject("BACK" , new Vector2d(0, -30), 5, 1),
        };

        worldSelectionGUI = new GUIObject[]{
                new GUIObject("WORLD 1" + disk.worldSize((byte)1) , new Vector2d(0, 10), 10, 1),
                new GUIObject("WORLD 2" + disk.worldSize((byte)2) , new Vector2d(0, -3), 10,1),
                new GUIObject("WORLD 3" + disk.worldSize((byte)3) , new Vector2d(0, -16), 10,1),
                new GUIObject("WORLD 4" + disk.worldSize((byte)4) , new Vector2d(0, -29), 10,1),
                new GUIObject("BACK" , new Vector2d(0, -42), 10,1),
        };

        //seed the random generator
        random.setSeed(new Date().getTime());

        //in intellij, search for 1 and you'll be able to read it
        if (Math.random() > 0.025) {
            titleBlocks = new byte[][]{
                    {1, 1, 1, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0},
                    {1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 1},
                    {1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0, 1, 1, 0},
                    {1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 1},
                    {1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 0, 1, 0, 1},
            };

            worldTitleBlocks = new byte[][]{
                    {1, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1},
                    {1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0},
                    {1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0},
                    {1, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1, 1, 0, 0, 1, 1, 1, 0},
            };

        } else {
            titleBlocks = new byte[][]{
                    {1, 1, 1, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0},
                    {1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1},
                    {1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0},
                    {1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1},
                    {1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 1, 1, 0, 1, 0, 1},
            };

            worldTitleBlocks = new byte[][]{
                    {1, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1},
                    {1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0},
                    {1, 0, 1, 0, 1, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0},
                    {1, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1},
                    {1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 0, 0, 1, 1, 1, 0},
            };
        }

        titleOffsets = new double[5][27];

        //set initial random float variables
        for (int x = 0; x < 5; x++) {
            //assume equal lengths
            for (int y = 0; y < 27; y++) {
                if (titleBlocks[x][y] == 1) {
                    titleOffsets[x][y] = blockOffsetInitial + Math.random() * 15d;
                }
            }
        }

        worldTitleOffsets = new double[5][27];

        for (int x = 0; x < 5; x++) {
            //assume equal lengths
            for (int y = 0; y < 27; y++) {
                if (worldTitleBlocks[x][y] == 1) {
                    worldTitleOffsets[x][y] = blockOffsetInitial + Math.random() * 15d;
                }
            }
        }

        selectTitleScreenText();
    }

    public byte getMainMenuPage(){
        return menuPage;
    }

    public GUIObject[] getMainMenuGUI(){
        if (menuPage == 0) {
            return mainMenuGUI;
        } else if (menuPage == 1){
            return mainMenuSettingsMenuGUI;
        } else if (menuPage == 2){
            return controlsMenuGUI;
        } else if (menuPage == 3){
            return worldSelectionGUI;
        } else if (menuPage == 5){
            return multiPlayerGUI;
        } else if (menuPage == 6){
            return multiPlayerLoadingGUI;
        } else if (menuPage == 7){
            return multiplayerFailureGUI;
        }

        //have to return something
        return mainMenuGUI;
    }


    public void resetMainMenuPage(){
        menuPage = 0;
    }

    public void resetMainMenu(){
        //set initial random float variables
        for (int x = 0; x < 5; x++){
            //assume equal lengths
            for (int y = 0; y < 27; y++){
                if (titleBlocks[x][y] == 1){
                    titleOffsets[x][y] = blockOffsetInitial + Math.random()*15d;
                }
            }
        }
        for (int x = 0; x < 5; x++){
            //assume equal lengths
            for (int y = 0; y < 27; y++){
                if (worldTitleBlocks[x][y] == 1){
                    worldTitleOffsets[x][y] = blockOffsetInitial + Math.random()*15d;
                }
            }
        }

        haltBlockFlyIn = false;
        haltWorldBlockFlyIn = false;

        selectTitleScreenText();
    }

    public byte[][] getTitleBlocks(){
        return titleBlocks;
    }

    public double[][] getTitleBlockOffsets(){
        return titleOffsets;
    }

    public byte[][] getWorldTitleBlocks(){
        return worldTitleBlocks;
    }

    public double[][] getWorldTitleOffsets(){
        return worldTitleOffsets;
    }

    private boolean connectionFailure = false;

    public void setConnectionFailure(){
        connectionFailure = true;
    }

    public void doMainMenuLogic(Delta delta) {

        if(connectionFailure){
            connectionFailure = false;
            menuPage = 7;
        }


        if (mouse.isLocked()){
            //System.out.println("unlocking");
            mouse.toggleMouseLock();
        }


        makeBackGroundScroll(delta);

        //title screen
        if (menuPage == 0) {
            makeBlocksFlyIn(delta);
            makeTitleBounce(delta);

            /*
            if (!titleMusic.isPlaying()) {
                titleMusic.play();
            }
             */

            byte selection = guiLogic.doGUIMouseCollisionDetection(mainMenuGUI);

            //0 singleplayer
            //1 multiplayer
            //2 settings
            //3 quit

            if (mouse.isLeftButtonPressed() && selection >= 0 && !mouseButtonPushed && !mouseButtonWasPushed) {
                mouseButtonPushed = true;
                //playSound("button");

                if (selection == 0) {
                    resetMainMenu();
                    menuPage = 3;
                }

                if (selection == 1) {
                    resetMainMenu();
                    menuPage = 5;
                }

                if (selection == 2) {
                    resetMainMenu();
                    menuPage = 1;
                }

                if (selection == 3){
                    //titleMusic.stop();
                    menuPage = 4;
                    //creditsMusic.play();
                    resetMainMenu();
                }

                if (selection == 4) {
                    glfwSetWindowShouldClose(window.getWindowHandle(), true);
                }

            } else if (!mouse.isLeftButtonPressed()) {
                mouseButtonPushed = false;
            }
        //settings menu
        } else if (menuPage == 1){
            byte selection = guiLogic.doGUIMouseCollisionDetection(mainMenuSettingsMenuGUI);

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
                        mainMenuSettingsMenuGUI[1].updateTextCenteredFixed("VSYNC: " + boolToString(vSync));
                        settings.saveSettings();
                    }
                    case 2 -> {
                        boolean graphicsMode = !settings.getGraphicsMode();
                        settings.setGraphicsMode(graphicsMode);
                        mainMenuSettingsMenuGUI[2].updateTextCenteredFixed("GRAPHICS: " + graphicsThing(graphicsMode));
                        settings.saveSettings();
                    }
                    case 3 -> {
                        int renderDistance = settings.getRenderDistance();
                        renderDistance = switch (renderDistance) {
                            case 5 -> 7;
                            case 7 -> 9;
                            case 9 -> 3;
                            default -> 5; //reset to 5
                        };
                        settings.setRenderDistance(renderDistance, false);
                        mainMenuSettingsMenuGUI[3].updateTextCenteredFixed("RENDER DISTANCE: " + renderDistance);
                        settings.saveSettings();
                    }
                    case 4 -> menuPage = 0;
                }
            } else if (!mouse.isLeftButtonPressed()) {
                mouseButtonPushed = false;
            }
        } else if (menuPage == 2){
            byte selection = guiLogic.doGUIMouseCollisionDetection(controlsMenuGUI);
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
        } else if (menuPage == 3){
            makeWorldBlocksFlyIn(delta);

            byte selection = guiLogic.doGUIMouseCollisionDetection(worldSelectionGUI);


            if (mouse.isLeftButtonPressed() && selection >= 0 && !mouseButtonPushed && !mouseButtonWasPushed) {
                //playSound("button");
                mouseButtonPushed = true;

                if (selection < 4) {
                    byte selectedWorld = (byte) (selection + 1); //base 1 counting
                    disk.setCurrentActiveWorld(selectedWorld);
                    //titleMusic.stop();
                    mouse.toggleMouseLock();
                    //generateRandomInventory();
                    sceneHandler.setScene((byte) 1);
                } else {
                    resetMainMenu();
                    menuPage = 0;
                }

            } else if (!mouse.isLeftButtonPressed()) {
                mouseButtonPushed = false;
            }
        } else if (menuPage == 4){
            makeCreditsScroll(delta);
            if (window.getDumpedKey() > -1){
                //creditsMusic.stop();
                //titleMusic.play();
                menuPage = 0;
                creditsScroll = -6f;
            }
        } else if (menuPage == 5){
            byte selection = guiLogic.doGUIMouseCollisionDetection(multiPlayerGUI);

            /*
                0 - name text
                1 - name input box
                2 - IP address text
                3 - ip adddress input box
                4 - connect
                5 - back
             */

            int dumpedKey = window.getDumpedKey();

            if (dumpedKey != -1 && dumpedKey != multiplayerScreenTextInput){
                multiplayerScreenTextInput = dumpedKey;

                //this is a HORRIBLE way to filter text input
                if ((dumpedKey >= 65 && dumpedKey <= 90) || (dumpedKey >= 48 && dumpedKey <= 57) || dumpedKey == 45 || dumpedKey == 47 || dumpedKey == 59 || dumpedKey == 46){

                    char newChar;

                    if (dumpedKey == 47){
                        newChar = '/';
                    } else if (dumpedKey == 46){
                        newChar = '.';
                    } else if (dumpedKey == 59) {
                        newChar = ':';
                    } else if (dumpedKey == 45){
                        newChar = '-';
                    } else {
                        newChar = (char)dumpedKey;
                    }

                    if (selection == 1) {
                        multiPlayerGUI[1].inputText = multiPlayerGUI[1].inputText + newChar;
                        multiPlayerGUI[1].updateInputBoxText(multiPlayerGUI[1].inputText);
                    } else if (selection == 3){
                        multiPlayerGUI[3].inputText = multiPlayerGUI[3].inputText + newChar;
                        multiPlayerGUI[3].updateInputBoxText(multiPlayerGUI[3].inputText);
                    }

                } else if (dumpedKey == 259){
                    if (selection == 1) {
                        multiPlayerGUI[1].inputText = multiPlayerGUI[1].inputText.replaceAll(".$", "");
                        multiPlayerGUI[1].updateInputBoxText(multiPlayerGUI[1].inputText);
                    } else if (selection == 3){
                        multiPlayerGUI[3].inputText = multiPlayerGUI[3].inputText.replaceAll(".$", "");
                        multiPlayerGUI[3].updateInputBoxText(multiPlayerGUI[3].inputText);
                    }
                }

                //literally split the text into 2
                String[] texted = multiPlayerGUI[3].inputText.split(":");

                if (texted.length == 2){
                    int newPort = Integer.parseInt(texted[1]);
                    //setPort(newPort);
                } else {
                    //reset to default port
                    //setPort(30_150);
                }

                //literally dumping the object data into the name
                player.setPlayerName(multiPlayerGUI[1].inputText);

            } else if (dumpedKey == -1) {
                multiplayerScreenTextInput = -1;
            }


            if (mouse.isLeftButtonPressed() && selection >= 0 && !mouseButtonPushed && !mouseButtonWasPushed) {
                //playSound("button");
                mouseButtonPushed = true;

                //literally split the text into 2
                String[] texted = multiPlayerGUI[3].inputText.split(":");

                if (selection == 4){
                    System.out.println(Arrays.toString(texted));
                    //System.out.println("port is: " + getPort());
                    if (texted[0] != null && texted[0].equals("")){
                        System.out.println("NO ADDRESS OR NAME INPUTTED!");
                    } else if (texted[0] != null) {
                        //System.out.println("Address is: " + multiPlayerGUI[0].inputText);
                        //System.out.println("BEGIN CONNECTION");

                        //this freezes the main menu
                        //sendOutHandshake(texted[0]);
                        menuPage = 6;
                    }
                } else if (selection == 5){
                    menuPage = 0;
                }


            } else if (!mouse.isLeftButtonPressed()) {
                mouseButtonPushed = false;
            }
        } else if (menuPage == 6){

            if (serverConnected){
                //serverConnected = false;
                System.out.println("CONNECT TO SERVER");
                //titleMusic.stop();
                sceneHandler.setScene((byte)3);
                return;
            }

            byte selection = guiLogic.doGUIMouseCollisionDetection(multiPlayerLoadingGUI);

            if (mouse.isLeftButtonPressed() && selection >= 0 && !mouseButtonPushed && !mouseButtonWasPushed) {
                //playSound("button");
                mouseButtonPushed = true;

                if (selection == 1){
                    System.out.println("CANCELING CONNECTION");
                    menuPage = 5;
                }

            } else if (!mouse.isLeftButtonPressed()) {
                mouseButtonPushed = false;
            }
        } else if (menuPage == 7){

            byte selection = guiLogic.doGUIMouseCollisionDetection(multiplayerFailureGUI);

            if (mouse.isLeftButtonPressed() && selection >= 0 && !mouseButtonPushed && !mouseButtonWasPushed) {
                //playSound("button");
                mouseButtonPushed = true;

                if (selection == 1){
                    menuPage = 5;
                }

            } else if (!mouse.isLeftButtonPressed()) {
                mouseButtonPushed = false;
            }
        }

        mouseButtonWasPushed = mouse.isLeftButtonPressed();
    }


    public void setServerConnected(boolean truth){
        serverConnected = truth;
    }

    public float getTitleBounce(){
        return titleBounce;
    }

    public float getBackGroundScroll(){
        return backGroundScroll;
    }

    //infinitely scrolling background
    private void makeBackGroundScroll(Delta delta){

        backGroundScroll += delta.getDelta() / 2f;

        if (backGroundScroll > 1f){
            backGroundScroll -= 1f;
        }
    }

    //scrolling credits
    private void makeCreditsScroll(Delta delta){

        float lockScroll = 62.5f;

        if (creditsScroll < lockScroll) {
            creditsScroll += delta.getDelta() / 1.3501f;

            if (creditsScroll >= lockScroll){
                creditsScroll = lockScroll;
            }
        }

    }

    public float getCreditsScroll(){
        return creditsScroll;
    }

    private void makeTitleBounce(Delta delta){
        /*
        if (initialBounce){
            float adder = delta * 30f;

            titleBounce -= adder;

            if (titleBounce <= 0){
                initialBounce = false;
                titleBounce = 0;
            }
        } else {

         */

        bounceAnimation += delta.getDelta() / 2f;

        if (bounceAnimation > 1f){
            bounceAnimation -= 1f;
        }

        //smooth bouncing
        titleBounce = (float)((Math.sin(bounceAnimation * Math.PI * 2f) * 1.25f) + 1.25f);
        //}
    }

    private void makeBlocksFlyIn(Delta delta){
        if (haltBlockFlyIn){
            return;
        }

        boolean found = false;

        //set initial random float variables
        for (int x = 0; x < 5; x++){
            //assume equal lengths
            for (int y = 0; y < 27; y++){
                if (titleBlocks[x][y] == 1 && titleOffsets[x][y] > 0){
                    found = true;
                    titleOffsets[x][y] -= delta.getDelta() * 30f;
                    if (titleOffsets[x][y] <= 0){
                        titleOffsets[x][y] = 0;
                    }
                }
            }
        }

        if (!found){
            haltBlockFlyIn = true;
        }
    }

    private void makeWorldBlocksFlyIn(Delta delta){
        if (haltWorldBlockFlyIn){
            return;
        }

        boolean found = false;

        //set initial random float variables
        for (int x = 0; x < 5; x++){
            //assume equal lengths
            for (int y = 0; y < 27; y++){
                if (worldTitleBlocks[x][y] == 1 && worldTitleOffsets[x][y] > 0){
                    found = true;
                    worldTitleOffsets[x][y] -= delta.getDelta() * 30f;
                    if (worldTitleOffsets[x][y] <= 0){
                        worldTitleOffsets[x][y] = 0;
                    }
                }
            }
        }

        if (!found){
            haltWorldBlockFlyIn = true;
        }
    }

    private String quickConvertKeyCode(int keyCode){
        char code = (char)keyCode;

        System.out.println(keyCode);
        /*
        if(code == 'Ŕ'){
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

    private String convertChunkLoadText(byte input){
        switch (input) {
            case 0 -> {
                return "SNAIL";
            }
            case 1 -> {
                return "LAZY";
            }
            case 2 -> {
                return "NORMAL";
            }
            case 3 -> {
                return "SPEEDY";
            }
            case 4 -> {
                return "INSANE";
            }
            case 5 -> {
                return "FUTURE PC";
            }
            default -> {
            }
        }

        return "NULL";
    }

    private Mesh titleScreenTextMeshBackGround = textHandling.createTextCentered("", 0.2f, 0.2f, 0f);
    private Mesh titleScreenTextMeshForeGround = textHandling.createTextCentered("", 1f, 1f, 0f);

    private final DateTimeFormatter dtf =  DateTimeFormatter.ofPattern("MM/dd");
    private final LocalDateTime now = LocalDateTime.now();
    private final String date = dtf.format(now);
    private final String[][] specialDates = {
            {"01/01", "Happy new year!"},
            {"01/08", "Happy Birthday Stephen Hawking!"},
            {"03/17", "Luck o' the Irish!"},
            {"04/17", "Happy Easter!"},
            {"08/18", "Happy Birthday to me!"},
            {"09/17", "Happy birthday Linux!"},
            {"10/10", "Happy birthday Minetest!"},
            {"10/14", "Happy birthday KDE!"},
            {"10/31", "Happy Halloween!"},
            {"11/24", "Happy Thanksgiving!"},
            {"12/24", "Merry Christmas!"},
            {"12/25", "Merry Christmas!"},
            {"12/31", "Happy new year's eve!"}
    };

    public void easterEgg(){
        if (date.equals("01/08")) {
            //playSound("easter_egg_1");
        }
    }


    public void selectTitleScreenText(){
        boolean dateFound = false;

        for (String[] dateArray : specialDates){
            if (date.equals(dateArray[0])){
                dateFound = true;
                titleScreenText = dateArray[1];
                break;
            }
        }

        if (!dateFound) {
            titleScreenText = titleScreenTextList[random.nextInt(titleScreenTextList.length)];
        }

        switch (titleScreenText) {
            case "Look at the window title!" -> window.updateTitle("Got you!");
            case "Jump scare free!" -> window.updateTitle("BOO!");
            default -> window.updateTitle(this.versionName);
        }

        titleScreenTextLength = (byte) titleScreenText.length();

        //create a new mesh for title screen text
        if (!titleScreenText.equals("R_A_N_D_O_M")){
            titleScreenTextMeshBackGround.cleanUp(false);
            titleScreenTextMeshForeGround.cleanUp(false);

            titleScreenTextMeshBackGround = textHandling.createTextCentered(getTitleScreenText(), 0.2f, 0.2f, 0f);
            titleScreenTextMeshForeGround = textHandling.createTextCentered(getTitleScreenText(), 1f, 1f, 0f);
        }
    }

    public Mesh getTitleScreenTextMeshBackGround(){
        return titleScreenTextMeshBackGround;
    }

    public Mesh getTitleScreenTextMeshForeGround(){
        return titleScreenTextMeshForeGround;
    }

    public String getTitleScreenText(){
        if (titleScreenText.equals("R_A_N_D_O_M")){
            int leftLimit = 97; // letter 'a'
            int rightLimit = 122; // letter 'z'
            int targetStringLength = 10;
            
            return random.ints(leftLimit, rightLimit + 1)
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString() + "!";
        }
        return titleScreenText;
    }
    public boolean titleScreenIsRandom(){
        return titleScreenText.equals("R_A_N_D_O_M");
    }

    public byte getTitleScreenTextLength(){
        return titleScreenTextLength;
    }

    //please keep this on the bottom
    private final String[] titleScreenTextList = new String[]{
            "R_A_N_D_O_M",
            "aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ!",
            "Made in America!",
            "Made in Canada!",
            "Clam approved!",
            "Open source!",
            "Runs great on Linux!",
            "Doesn't doubt!",
            "Probably original!",
            "The pigs are dangerous!",
            "2D!",
            "Also available online!",
            "Created with love!",
            "Now with more math!",
            "Crashes for free!",
            "Made by one guy!",
            "Tutorial friendly!",
            "Minetest compatible!",
            "Now with less bugs!",
            "LWJGL is cool!",
            "Feels like a Friday!",
            "Infinite blocks!",
            "Now on Github!",
            "Created for free!",
            "Now with more textures!",
            "Now with more Java!",
            "Don't look at the bugs!",
            "Taco Tuesday!",
            "Runs on DOS!",
            "Doesn't ｆ�?ｒ�?�?ｔ text right!",
            "Look at the window title!",
            "My spoon is too big!",
            "It's chaos!",
            "Don't shuck me, bro!",
            "Translated to 1 language!",
            "It's this!",
            "Probably cures eyestrain!",
            "Multithreaded!",
            "You see it! Believe it!",
            "Awesome!",
            "Fancy!",
            "Null pointers!",
            "Renders to your screen!",
            "Context is current!",
            "Words words words!",
            "Flipped arrays!",
            "Works until broken!",
            "Tomorrows today!",
            "Creeper free!",
            "Automatic!",
            "Chunks!",
            "Yo, banana boy!",
            "Was it a car or a cat I saw?",
            "Now with more bugs!",
            "No punctuation included!",
            "Wow this text is really really really really small!",
            "Credit is in the credits!",
            "Made for people by robots!",
            "Undefined behavior!",
            "128 bit math!",
            "Almost a game!",
            "Tell your friends!",
            "Please distribute!",
            "Gas gas gas!",
            "Initial B!",
            "Liquid cooled!",
            "Not sus!",
            "#%^&*()_=+[]{}\\|;:'\",<.>/?`~!",
            "Created the hard way!",
            "Coming to a town near you!",
            "Celeron55 is cool!",
            "Found in the grocery isle!",
            "Also try Mineclone 2!",
            "Now with bigger buttons!",
            "Object oriented!",
            "The boats never sink!",
            "404 - Sheep not found!",
            "Almost alpha!",
            "Optional options!",
            "Don't fall off the edge!",
            "Collides!",
            "Works underwater!",
            "Doesn't include crafting!",
            "Too much math!",
            "Multiple worlds!",
            "Polished, but not enough!",
            "Inconvenient!",
            "Powered by coal!",
            "Now with crafting!",
            "Craft the universe!",
            "Jump for joy!",
            "Now with more binary!",
            "Algorithmic!",
            "Now multiplayer!",
            "Duplicate pointers!",
            "Crashes on command!",
            "Doesn't burn your toast!",
            "Widescreen compatible!",
            "Jeb is cool!",
            "Now with more code!",
            "Binary until compiled!",
            "Go west to head east!",
            "Has been to space!",
            "Climbs mountains!",
            "Polls your inputs!",
            "Written in Rust...Go...Java!",
            "Appends data to objects!",
            "Found on the internet!",
            "Reverts changes!",
            "Now recyclable!",
            "Notch is cool!",
            "No smokin', and no flash photography!",
            "Walks like it talks!",
            "Dries off in seconds!",
            "Machine washable!",
            "Does what others do and less!",
            "Goes 0-60 in -0 seconds!",
            "Folds into a neat swan!",
            "Reads bytes!",
            "Web capable!",
            "No nodes here!",
            "They're minerals!",
            "Green flag! Go go go!",
            "java.lang.nullpointerexception!",
            "Supported by an open web!",
            "Buffalo buffalo Buffalo buffalo buffalo buffalo Buffalo buffalo!",
            "Tom Scott's bound to find it!",
            "It's pretty alright!",
            "Holy macaroni!",
            "Do a barrel roll!",
            "Fish on!",
            "Puts pixels on your screen!",
            "Final public!",
            "Public final!",
            "Made with no artificial ingredients!",
            "MIT Licensed!",
            "Can be used as a floatation device!",
            "TYPES LIKE THIS!",
            "YELLS QUITE A LOT!",
            "Has been under the ocean!",
            "Made with electrons!",
            "Knows where to go!",
            "True + true = 2!",
            "Now for PC!",
            "Faster than ever!",
            "Cats love it!",
            "Pie flavored!",
            "The pointers are always NULL!",
            "Uses Arch, BTW!",
            "Also try Minetest!",
            "Too many threads!",
            "Luultavasti kaannetty oikein!",
            "Found in the Arctic!",
            "New build now!",
            "Now with water!",
            "Wooden tools!",
            "Massages your CPU!",
            "Rated H for human!",
            "Filled with Strings!",
            "Jump scare free!",
            "Ghost free!",
            "Moving clouds!",
            "3 2 1 Let's sausage!",
            "It's an anomaly!",
            "Bleep bloop!",
            "Robotic!",
            "Verbose!",
            "Verbose!",
            "Verbose!",
            "Also try Open Miner!",
            "Rubenwardy is cool!",
            "Dynamic allocations!",
            "Flushes buffers!",
            "Steams hams!",
            "Static!",
            "Speeeeeeen!",
            "It's magic, Joel!",
            "Error!",
            "Hello there!",
            "A strange game!",
            "It's always a tie!",
            "The only winning move is not to play!",
            "How about a nice game of chess?",
            "Open gate, walk through gate, close gate!",
            "Everything's an object!"
    };
}
