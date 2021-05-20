package game.mainMenu;

import engine.gui.GUIObject;
import engine.sound.SoundSource;
import org.joml.Vector2d;

import java.util.Date;
import java.util.Random;

import static engine.MouseInput.*;
import static engine.Time.getDelta;
import static engine.Window.updateWindowTitle;
import static engine.gui.GUILogic.doGUIMouseCollisionDetection;
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

    private static final float blockOffsetInitial = 15f;
    private static float titleBounce = 10f;
    private static float bounceAnimation = 0.75f;

    private static float backGroundScroll = 0f;

    private static SoundSource titleMusic;

    private static boolean lockOutReset = false;

    private static final Random random = new Random();

    private static GUIObject[] mainMenuGUI;

    public static GUIObject[] getMainMenuGUI(){
        return mainMenuGUI;
    }

    public static void initMainMenu() throws Exception {

        mainMenuGUI = new GUIObject[]{
                new GUIObject("SINGLEPLAYER" , new Vector2d(0, 10), 10, 1),
                new GUIObject("MULTIPLAYER" , new Vector2d(0, -5), 10,1),
                new GUIObject("SETTINGS" , new Vector2d(0, -20), 10,1),
                new GUIObject("QUIT" , new Vector2d(0, -35), 10,1),
        };

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

        titleMusic = playSound("main_menu");
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

        if (isLeftButtonPressed()){
            titleMusic.stop();
            setScene((byte)1);
            toggleMouseLock();
            return;
        }

        if (isRightButtonPressed()){
            if (!lockOutReset){
                resetMainMenu();
            }
            lockOutReset = true;
        } else {
            lockOutReset = false;
        }
        makeBlocksFlyIn();
        makeTitleBounce();
        makeBackGroundScroll();

        if (!titleMusic.isPlaying()){
            titleMusic.play();
        }

        doGUIMouseCollisionDetection(mainMenuGUI);
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


    private static void selectTitleScreenGag(){
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
