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

import static engine.Controls.input;
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
import static game.light.Light.*;
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
import static game.player.Player.*;

public class Crafter {

    //variables

    //DO NOT finalize this
    private static int     chunkRenderDistance = 5;

    private static final String versionName = "Crafter Pre-Alpha 0.03a";

    public static String getVersionName(){
        return versionName;
    }

    public static int getRenderDistance(){
        return chunkRenderDistance;
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
            //assistantThread();
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

    //assistant thread
    private static void assistantThread(){
        new Thread(() -> {
            while(!windowShouldClose()){
                //heavy

            }
        }).start();
    }

    //the game engine elements

    //main game loop
    private static void gameLoop() throws Exception {
        while(!windowShouldClose()){

            calculateDelta();

            indexLight();
            mouseInput();
            updateCamera();
            countFPS();
            updateWorldChunkLoader();
            popChunkMeshQueue(); //this actually transmits the data from the other threads into main thread
            renderGame();
            windowUpdate();
            updateListenerPosition();
            chunkUpdater();
            globalChunkSaveToDisk(); //add in a getDelta argument into this!

            input();

            //testLightLevel();
            gameUpdate();


            long count = 0;

            boolean debugLowFPS = false; //this sets my machine (jordan4ibanez) to 7-9FPS

            if (debugLowFPS) {
                while (count < 500_000_000L) {
                    count++;
                }
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

        for (x = -chunkRenderDistance + currentChunk[0]; x < chunkRenderDistance + currentChunk[0]; x++){
            for (z = -chunkRenderDistance + currentChunk[1]; z< chunkRenderDistance + currentChunk[1]; z++){
                for (int y = 0; y < 8; y++){
                    generateChunkMesh(x,z,y);
                }
            }
        }

        //createToolDebugInventory();
        //generateRandomInventory();
        //tntFillErUp();
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
