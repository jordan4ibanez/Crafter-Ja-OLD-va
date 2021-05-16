package game.mainMenu;

import engine.sound.SoundBuffer;
import engine.sound.SoundSource;

import static engine.MouseInput.*;
import static engine.Time.getDelta;
import static engine.Window.updateWindowTitle;
import static engine.graph.Camera.setCameraPosition;
import static engine.scene.SceneHandler.setScene;
import static engine.sound.SoundAPI.playSound;
import static game.Crafter.getVersionName;
import static game.mainMenu.MainMenuAssets.createMainMenuBackGroundTile;
import static game.mainMenu.MainMenuAssets.createMenuMenuTitleBlock;

public class MainMenu {

    private static byte[][] titleBlocks;
    private static double[][] titleOffsets;
    private static boolean haltBlockFlyIn = false;
    private static String titleScreenGag = "";
    private static byte titleScreenGagLength = 0;

    private static boolean initialBounce = true;
    private static float titleBounce = 25f;
    private static float bounceAnimation = 0.75f;

    private static float backGroundScroll = 0f;

    private static SoundSource titleMusic;

    private static boolean lockOutReset = false;

    public static void initMainMenu() throws Exception {

        setCameraPosition(0,-8,0);

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
                    titleOffsets[x][y] = 25d + Math.random()*15d;
                }
            }
        }

        createMenuMenuTitleBlock();
        createMainMenuBackGroundTile();
        selectTitleScreenGag();

        titleMusic = playSound("main_menu");
    }

    //debug
    private static void resetMainMenu(){
        //set initial random float variables
        for (int x = 0; x < 5; x++){
            //assume equal lengths
            for (int y = 0; y < 27; y++){
                if (titleBlocks[x][y] == 1){
                    titleOffsets[x][y] = 25d + Math.random()*15d;
                }
            }
        }

        haltBlockFlyIn = false;
        initialBounce = true;
        titleBounce = 25f;
        bounceAnimation = 0.75f;

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

        if (isLeftButtonPressed()){
            titleMusic.stop();
            setScene((byte)1);
            toggleMouseLock();
        }

        if (isRightButtonPressed() && !lockOutReset){
            lockOutReset = true;
            resetMainMenu();
        } else {
            lockOutReset = false;
        }
        makeBlocksFlyIn();
        makeTitleBounce();
        makeBackGroundScroll();
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

        if (initialBounce){
            float adder = delta * 30f;

            titleBounce -= adder;

            if (titleBounce <= 0){
                initialBounce = false;
                titleBounce = 0;
            }
        } else {

            bounceAnimation += delta / 2f;

            if (bounceAnimation > 1f){
                bounceAnimation -= 1f;
            }

            //smooth bouncing
            titleBounce = (float)((Math.sin(bounceAnimation * Math.PI * 2f) * 1.25f) + 1.25f);
        }
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


    private static void selectTitleScreenGag(){
        titleScreenGag = titleScreenGags[(int)Math.floor(Math.random() * titleScreenGags.length)];
        if (titleScreenGag.equals("Look at the window title!")){
            updateWindowTitle("Got em!");
        } else {
            updateWindowTitle(getVersionName());
        }
        titleScreenGagLength = (byte)titleScreenGag.length();
    }

    public static String getTitleScreenGag(){
        return titleScreenGag;
    }

    public static byte getTitleScreenGagLength(){
        return titleScreenGagLength;
    }

    //please keep this on the bottom
    private static final String[] titleScreenGags = new String[]{
            "Made in America!",
            "Made in Canada!",
            "Clam approved!",
            "Open source!",
            "Runs great on Linux!",
            "Doesnt doubt!",
            "Probably original!",
            "The pigs are dangerous!",
            "2D!",
            "Also available online!",
            "Created with love!",
            "Now with more math!",
            "Crashes are free!",
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
            "Its chaos!",
            "Dont shuck me, bro!",
            "Translated to 1 language!",
            "Its this!",
            "Probably cures eyestrain!",
            "Multithreaded!",
            "You see it! Believe it!",

    };
}
