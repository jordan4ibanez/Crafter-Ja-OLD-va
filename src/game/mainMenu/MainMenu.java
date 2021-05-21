package game.mainMenu;

import engine.gui.GUIObject;
import engine.sound.SoundSource;
import org.joml.Vector2d;

import java.util.Date;
import java.util.Random;

import static engine.MouseInput.*;
import static engine.Time.getDelta;
import static engine.Window.*;
import static engine.gui.GUILogic.doGUIMouseCollisionDetection;
import static engine.scene.SceneHandler.setScene;
import static engine.settings.Settings.*;
import static engine.settings.Settings.getKeyInventory;
import static engine.sound.SoundAPI.playMusic;
import static engine.sound.SoundAPI.playSound;
import static game.Crafter.getVersionName;
import static game.mainMenu.MainMenuAssets.createMainMenuBackGroundTile;
import static game.mainMenu.MainMenuAssets.createMenuMenuTitleBlock;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class MainMenu {

    private static byte[][] titleBlocks;
    private static double[][] titleOffsets;
    private static boolean haltBlockFlyIn = false;
    private static String titleScreenGag = "";
    private static byte titleScreenGagLength = 0;
    private static final float blockOffsetInitial = 15f;
    private static float titleBounce = 10f;
    private static float bounceAnimation = 0.75f;
    private static float backGroundScroll = 0f;
    private static SoundSource titleMusic;
    private static boolean lockOutReset = false;
    private static final Random random = new Random();
    private static boolean mouseButtonPushed = false;
    private static boolean mouseButtonWasPushed = false;
    private static boolean pollingButtonInputs = false;
    private static byte lockedOnButtonInput = -1;

    //0 main
    //1 settings base
    //2 buttons settings
    private static byte menuPage = 0;

    private static final GUIObject[] mainMenuGUI = new GUIObject[]{
            new GUIObject("SINGLEPLAYER" , new Vector2d(0, 10), 10, 1),
            new GUIObject("MULTIPLAYER" , new Vector2d(0, -5), 10,1),
            new GUIObject("SETTINGS" , new Vector2d(0, -20), 10,1),
            new GUIObject("QUIT" , new Vector2d(0, -35), 10,1),
    };

    private static final GUIObject[] mainMenuSettingsMenuGUI = new GUIObject[]{
            new GUIObject("CONTROLS" ,             new Vector2d(0, 35), 12, 1),
            new GUIObject("VSYNC: " + boolToString(getSettingsVsync()),            new Vector2d(0, 21), 12, 1),
            new GUIObject("GRAPHICS MODE: " + graphicsThing(getGraphicsMode()) , new Vector2d(0, 7), 12,1),
            new GUIObject("RENDER DISTANCE: " + getRenderDistance(),   new Vector2d(0, -7), 12,1),
            new GUIObject("CHUNK LOADING: " + convertChunkLoadText(getSettingsChunkLoad()), new Vector2d(0, -21), 12,1),
            new GUIObject("BACK" ,                  new Vector2d(0, -35), 12,1),
    };

    private static final GUIObject[] controlsMenuGUI = new GUIObject[]{
            new GUIObject("FORWARD: " + quickConvertKeyCode(getKeyForward()) ,     new Vector2d(-35, 30), 6, 1),
            new GUIObject("BACK: " + quickConvertKeyCode(getKeyBack()),            new Vector2d(35, 30), 6, 1),
            new GUIObject("LEFT: " + quickConvertKeyCode(getKeyLeft()),            new Vector2d(-35, 15), 6, 1),
            new GUIObject("RIGHT: " + quickConvertKeyCode(getKeyRight()),          new Vector2d(35, 15), 6, 1),
            new GUIObject("SNEAK: " + quickConvertKeyCode(getKeySneak()),          new Vector2d(-35, 0), 6, 1),
            new GUIObject("DROP: " + quickConvertKeyCode(getKeyDrop()),            new Vector2d(35, 0), 6, 1),
            new GUIObject("JUMP: " + quickConvertKeyCode(getKeyJump()),            new Vector2d(-35, -15), 6, 1),
            new GUIObject("INVENTORY: " + quickConvertKeyCode(getKeyInventory()) , new Vector2d(35, -15), 6, 1),
            new GUIObject("BACK" , new Vector2d(0, -30), 5, 1),
    };

    public static byte getMainMenuPage(){
        return menuPage;
    }

    public static GUIObject[] getMainMenuGUI(){
        if (menuPage == 0) {
            return mainMenuGUI;
        } else if (menuPage == 1){
            return mainMenuSettingsMenuGUI;
        } else if (menuPage == 2){
            return controlsMenuGUI;
        }

        //have to return something
        return mainMenuGUI;
    }

    public static void initMainMenu() throws Exception {

        //seed the random generator
        random.setSeed(new Date().getTime());

        //in intellij, search for 1 and you'll be able to read it
        titleBlocks = new byte[][]{
                {1,1,1,0,1,1,0,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,0},
                {1,0,0,0,1,0,1,0,1,0,1,0,1,0,0,0,0,1,0,0,1,0,0,0,1,0,1},
                {1,0,0,0,1,1,0,0,1,1,1,0,1,1,1,0,0,1,0,0,1,1,1,0,1,1,0},
                {1,0,0,0,1,0,1,0,1,0,1,0,1,0,0,0,0,1,0,0,1,0,0,0,1,0,1},
                {1,1,1,0,1,0,1,0,1,0,1,0,1,0,0,0,0,1,0,0,1,1,1,0,1,0,1},
        };

       titleOffsets = new double[5][27];

        //set initial random float variables
        for (int x = 0; x < 5; x++){
            //assume equal lengths
            for (int y = 0; y < 27; y++){
                if (titleBlocks[x][y] == 1){
                    titleOffsets[x][y] = blockOffsetInitial + Math.random()*15d;
                }
            }
        }

        createMenuMenuTitleBlock();
        createMainMenuBackGroundTile();
        selectTitleScreenGag();

        titleMusic = playMusic("main_menu");
    }

    //debug
    private static void resetMainMenu(){
        //set initial random float variables
        for (int x = 0; x < 5; x++){
            //assume equal lengths
            for (int y = 0; y < 27; y++){
                if (titleBlocks[x][y] == 1){
                    titleOffsets[x][y] = blockOffsetInitial + Math.random()*15d;
                }
            }
        }

        haltBlockFlyIn = false;
        //bounceAnimation = 0f;

        selectTitleScreenGag();
    }

    public static byte[][] getTitleBlocks(){
        return titleBlocks;
    }

    public static double[][] getTitleBlockOffsets(){
        return titleOffsets;
    }


    public static void doMainMenuLogic(){
        if (isMouseLocked()){
            //System.out.println("unlocking");
            toggleMouseLock();
        }


        makeBackGroundScroll();

        //title screen
        if (menuPage == 0) {
            makeBlocksFlyIn();
            makeTitleBounce();

            if (!titleMusic.isPlaying()) {
                titleMusic.play();
            }

            byte selection = doGUIMouseCollisionDetection(mainMenuGUI);

            //0 singleplayer
            //1 multiplayer
            //2 settings
            //3 quit

            if (isLeftButtonPressed() && selection >= 0 && !mouseButtonPushed && !mouseButtonWasPushed) {
                mouseButtonPushed = true;
                playSound("button");

                if (selection == 0) {
                    titleMusic.stop();
                    setScene((byte) 1);
                    toggleMouseLock();
                }

                if (selection == 1) {
                    System.out.println("THERE'S NO MULTIPLAYER OH NO");
                }

                if (selection == 2) {
                    resetMainMenu();
                    menuPage = 1;
                }

                if (selection == 3) {
                    glfwSetWindowShouldClose(getWindowHandle(), true);
                }

            } else if (!isLeftButtonPressed()) {
                mouseButtonPushed = false;
            }
        //settings menu
        } else if (menuPage == 1){
            byte selection = doGUIMouseCollisionDetection(mainMenuSettingsMenuGUI);

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
                    case 0:
                        //goto controls menu
                        menuPage = 2;
                        break;
                    case 1:
                        boolean vSync = !getSettingsVsync();
                        setSettingsVsync(vSync);
                        mainMenuSettingsMenuGUI[1].updateTextCenteredFixed("VSYNC: " + boolToString(vSync));
                        saveSettings();
                        break;
                    case 2:
                        boolean graphicsMode = !getGraphicsMode();
                        setGraphicsMode(graphicsMode);
                        mainMenuSettingsMenuGUI[2].updateTextCenteredFixed("GRAPHICS: " + graphicsThing(graphicsMode));
                        saveSettings();
                        break;
                    case 3:
                        int renderDistance = getRenderDistance();
                        switch (renderDistance) {
                            case 3:
                                renderDistance = 5;
                                break;
                            case 5:
                                renderDistance = 7;
                                break;
                            case 7:
                                renderDistance = 9;
                                break;
                            case 9:
                                renderDistance = 3;
                        }
                        setRenderDistance(renderDistance);
                        mainMenuSettingsMenuGUI[3].updateTextCenteredFixed("RENDER DISTANCE: " + renderDistance);
                        saveSettings();
                        break;
                    case 4:
                        byte chunkLoad = getSettingsChunkLoad();
                        chunkLoad++;
                        if (chunkLoad > 5) {
                            chunkLoad = 0;
                        }
                        mainMenuSettingsMenuGUI[4].updateTextCenteredFixed("CHUNK LOADING: " + convertChunkLoadText(chunkLoad));
                        setSettingsChunkLoad(chunkLoad);
                        saveSettings();
                        break;
                    case 5:
                        menuPage = 0;
                        break;
                }
            } else if (!isLeftButtonPressed()) {
                mouseButtonPushed = false;
            }
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

    public static float getTitleBounce(){
        return titleBounce;
    }

    public static float getBackGroundScroll(){
        return backGroundScroll;
    }

    //infinitely scrolling background
    private static void makeBackGroundScroll(){

        float delta = getDelta();

        backGroundScroll += delta / 2f;

        if (backGroundScroll > 1f){
            backGroundScroll -= 1f;
        }
    }

    private static void makeTitleBounce(){
        float delta = getDelta();

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

        bounceAnimation += delta / 2f;

        if (bounceAnimation > 1f){
            bounceAnimation -= 1f;
        }

        //smooth bouncing
        titleBounce = (float)((Math.sin(bounceAnimation * Math.PI * 2f) * 1.25f) + 1.25f);
        //}
    }

    private static void makeBlocksFlyIn(){
        if (haltBlockFlyIn){
            return;
        }

        boolean found = false;

        float delta = getDelta();
        //set initial random float variables
        for (int x = 0; x < 5; x++){
            //assume equal lengths
            for (int y = 0; y < 27; y++){
                if (titleBlocks[x][y] == 1 && titleOffsets[x][y] > 0){
                    found = true;
                    titleOffsets[x][y] -= delta * 30f;
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

    private static String convertChunkLoadText(byte input){
        if (input == 0){
            return "SNAIL";
        } else if (input == 1){
            return "LAZY";
        } else if (input == 2){
            return "NORMAL";
        } else if (input == 3){
            return "SPEEDY";
        } else if (input == 4){
            return "INSANE";
        } else if (input == 5){
            return "FUTURE PC";
        }

        return "NULL";
    }

    public static void selectTitleScreenGag(){
        titleScreenGag = titleScreenGags[random.nextInt(titleScreenGags.length)];

        if (titleScreenGag.equals("Look at the window title!")){
            updateWindowTitle("Got em!");
        } else {
            updateWindowTitle(getVersionName());
        }
        titleScreenGagLength = (byte)titleScreenGag.length();
    }

    public static String getTitleScreenGag(){
        if (titleScreenGag.equals("R_A_N_D_O_M")){
            int leftLimit = 97; // letter 'a'
            int rightLimit = 122; // letter 'z'
            int targetStringLength = 10;
            Random random = new Random();

            return random.ints(leftLimit, rightLimit + 1)
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString() + "!";
        }
        return titleScreenGag;
    }

    public static byte getTitleScreenGagLength(){
        return titleScreenGagLength;
    }

    //please keep this on the bottom
    private static final String[] titleScreenGags = new String[]{
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
            "Doesn't ｆｏｒｍａｔ text right!",
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
    };
}
