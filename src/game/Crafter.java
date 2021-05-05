package game;

import engine.sound.SoundListener;
import game.chunk.Chunk;
import game.item.ItemDefinition;
import game.item.ItemEntity;
import game.tnt.TNTEntity;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.openal.AL11;

import java.awt.*;

import static engine.Time.calculateDelta;
import static engine.disk.Disk.*;
import static engine.disk.SaveQueue.startSaveThread;
import static game.chunk.Chunk.*;
import static game.chunk.ChunkMesh.generateChunkMesh;
import static game.chunk.ChunkMesh.popChunkMeshQueue;
import static game.chunk.ChunkUpdateHandler.chunkUpdater;
import static engine.Hud.*;
import static engine.MouseInput.*;
import static game.falling.FallingEntity.fallingEntityOnStep;
import static game.item.ItemEntity.clearItems;
import static game.light.Light.indexLight;
import static game.mob.Mob.*;
import static game.particle.Particle.particlesOnStep;
import static game.tnt.TNTEntity.createTNTEntityMesh;
import static engine.Timer.*;
import static engine.Window.*;
import static engine.graph.Camera.*;
import static engine.sound.SoundManager.*;
import static engine.Renderer.*;
import static game.blocks.BlockDefinition.initializeBlocks;
import static game.item.ItemRegistration.registerTools;
import static game.player.Inventory.*;
import static game.player.Player.*;
import static game.weather.Weather.rainDropsOnTick;
import static org.lwjgl.glfw.GLFW.*;

public class Crafter {

    //variables
    private static int     chunkRenderDistance = 5;
    private static boolean qButtonPushed       = false;
    private static boolean rButtonPushed       = false;
    private static boolean tButtonPushed       = false;
    private static boolean cButtonPushed       = false;
    private static boolean eButtonPushed       = false;
    private static boolean F11Pushed           = false;
    private static boolean escapePushed        = false;
    private static final String versionName = "Crafter Pre-Alpha 0.02b";

    public static String getVersionName(){
        return versionName;
    }

    //core game engine elements

    //load everything
    public static void main(String[] args){
        try{
            boolean vSync = true;
            Toolkit tk = Toolkit.getDefaultToolkit();
            Dimension d = tk.getScreenSize();

            initWindow(versionName, d.width/2,d.height/2,vSync);
            initRenderer();
            initMouseInput();
            initSoundManager();
            initGame();
            startSaveThread();
            gameLoop();

        } catch ( Exception excp ){
            excp.printStackTrace();
            System.exit(-1);
        } finally {
            globalFinalChunkSaveToDisk();
            savePlayerPos(getPlayerPos());
            cleanup();
        }
    }

    //the game engine elements

    //create game loop
    private static void gameLoop() throws Exception {
        double elapsedTime;
        double accumulator = 0d;
        boolean running = true;
        while(running && !windowShouldClose()){

            calculateDelta();

            elapsedTime = timerGetElapsedTime();
            accumulator += elapsedTime;

            globalChunkSaveToDisk();
            input();
            mouseInput();
            updateCamera();
            countFPS();
            updateWorldChunkLoader();
            indexLight();
            chunkUpdater();
            renderGame();
            windowUpdate();
            popChunkMeshQueue();
            updateListenerPosition();

            while (accumulator >= 1_000_000){
                gameUpdate();
                accumulator -= 1_000_000;
            }
        }
    }



    public static int getChunkRenderDistance(){
        return chunkRenderDistance;
    }

    public static void initGame() throws Exception{
        initializeHudAtlas();
        //this initializes the block definitions
        initializeBlocks();
        //this creates a TNT mesh (here for now)
        createTNTEntityMesh();

        setAttenuationModel(AL11.AL_LINEAR_DISTANCE);
        setListener(new SoundListener(new Vector3d()));
        createHud();

        registerTools();

        initializeMobRegister();

        initializeWorldHandling();

        //create the initial map in memory
        int x;
        int z;
        int[] currentChunk = getPlayerCurrentChunk();
        for (x = -chunkRenderDistance + currentChunk[0]; x < chunkRenderDistance + currentChunk[0]; x++){
            for (z = -chunkRenderDistance + currentChunk[1]; z< chunkRenderDistance + currentChunk[1]; z++){
                genBiome(x,z);
            }
        }

        /*
        for (x = -chunkRenderDistance + currentChunk[0]; x < chunkRenderDistance + currentChunk[0]; x++){
            for (z = -chunkRenderDistance + currentChunk[1]; z< chunkRenderDistance + currentChunk[1]; z++){
                for (int y = 0; y < 8; y++){
                    generateChunkMesh(x,z,y);
                }
            }
        }

         */

//        createToolDebugInventory();
        generateRandomInventory();
//        tntFillErUp();
    }

    private static void input() {
        if (!isPlayerInventoryOpen() && !isPaused()) {
            if (isKeyPressed(GLFW_KEY_W)) {
                setPlayerForward(true);
            } else {
                setPlayerForward(false);
            }

            if (isKeyPressed(GLFW_KEY_S)) {
                setPlayerBackward(true);
            } else {
                setPlayerBackward(false);
            }
            if (isKeyPressed(GLFW_KEY_A)) {
                setPlayerLeft(true);
            } else {
                setPlayerLeft(false);
            }
            if (isKeyPressed(GLFW_KEY_D)) {
                setPlayerRight(true);
            } else {
                setPlayerRight(false);
            }

            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT)) { //sneaking
                setPlayerSneaking(true);
            } else {
                setPlayerSneaking(false);
            }

            if (isKeyPressed(GLFW_KEY_SPACE)) {
                setPlayerJump(true);
            } else {
                setPlayerJump(false);
            }
        }


        if (isKeyPressed(GLFW_KEY_G)) {
            clearItems();
        }

        if (isKeyPressed(GLFW_KEY_R)) {
            setPlayerRunning(true);
        } else {
            setPlayerRunning(false);
        }


        if (isKeyPressed(GLFW_KEY_R)) {
            if (!rButtonPushed) {
                rButtonPushed = true;
//                resetInventory();
                generateRandomInventory();
            }
        } else if (!isKeyPressed(GLFW_KEY_R)){
            rButtonPushed = false;
        }


        if (isKeyPressed(GLFW_KEY_Q)) {
            if (!qButtonPushed) {
                qButtonPushed = true;
                throwItem();
            }
        } else if (!isKeyPressed(GLFW_KEY_Q)){
            qButtonPushed = false;
        }

        if (isKeyPressed(GLFW_KEY_F11)) {
            if (!F11Pushed) {
                F11Pushed = true;
                toggleFullScreen();
            }
        } else if (!isKeyPressed(GLFW_KEY_F11)){
            F11Pushed = false;
        }

        if (isKeyPressed(GLFW_KEY_ESCAPE)) {
            if (!escapePushed) {
                escapePushed = true;
                if(isPlayerInventoryOpen()){
                    togglePlayerInventory();
                    toggleMouseLock();
                    emptyMouseInventory();
                } else {
                    toggleMouseLock();
                    togglePauseMenu();
                    emptyMouseInventory();
                }
                resetPlayerInputs();
            }
        } else if (!isKeyPressed(GLFW_KEY_ESCAPE)){
            escapePushed = false;
        }


        //prototype clear objects - C KEY
        if (isKeyPressed(GLFW_KEY_E) && !isPaused()) {
            if (!eButtonPushed) {
                eButtonPushed = true;
                togglePlayerInventory();
                toggleMouseLock();
                resetPlayerInputs();
                emptyMouseInventory();
            }
        } else if (!isKeyPressed(GLFW_KEY_E)){
            eButtonPushed = false;
        }


        //spawn human mob
        if (isKeyPressed(GLFW_KEY_T)) {
            spawnMob("human", new Vector3d(getPlayerPos()), new Vector3f(0,0,0));
        }


        if (!isPlayerInventoryOpen() && !isPaused()) {
            //mouse left button input
            if (isLeftButtonPressed()) {
                setPlayerMining(true);
                startDiggingAnimation();
            } else {
                setPlayerMining(false);
            }

            //mouse right button input
            if (isRightButtonPressed()) {
                setPlayerPlacing(true);
                startDiggingAnimation();
            } else {
                setPlayerPlacing(false);
            }

            float scroll = getMouseScroll();
            if (scroll < 0) {
                changeScrollSelection(1);
            } else if (scroll > 0) {
                changeScrollSelection(-1);
            }
        }
    }

    private static void gameUpdate() throws Exception {
        testPlayerDiggingAnimation();
        playerOnTick();
        ItemEntity.onStep();
        TNTEntity.onTNTStep();
        hudOnStepTest();
        particlesOnStep();
        fallingEntityOnStep();
        //rainDropsOnTick();
        mobsOnTick();
    }

    private static void cleanup(){
        Chunk.cleanUp();
        cleanupSoundManager();
        ItemDefinition.cleanUp();
        cleanupRenderer();
    }
}
