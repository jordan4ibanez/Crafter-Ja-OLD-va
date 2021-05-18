package game;

import engine.sound.SoundListener;
import game.chunk.Chunk;
import game.item.ItemDefinition;
import org.joml.Vector3d;
import org.lwjgl.openal.AL11;

import java.awt.*;

import static engine.disk.Disk.*;
import static engine.disk.SaveQueue.startSaveThread;
import static engine.scene.SceneHandler.handleSceneLogic;
import static game.chunk.Chunk.*;
import static game.chunk.ChunkMesh.generateChunkMesh;
import static engine.hud.Hud.*;
import static engine.MouseInput.*;
import static game.mainMenu.MainMenu.initMainMenu;
import static game.mob.Mob.*;
import static game.tnt.TNTEntity.createTNTEntityMesh;
import static engine.Window.*;
import static engine.sound.SoundManager.*;
import static engine.render.GameRenderer.*;
import static game.blocks.BlockDefinition.initializeBlocks;
import static game.item.ItemRegistration.registerTools;
import static game.player.Player.*;

public class Crafter {

    //fields
    //DO NOT finalize this
    private static int     chunkRenderDistance = 5;
    private static boolean debugInfo = true;
    private static final String versionName = "Crafter 0.03b Survival Test";
    

    public static void setDebugInfo(boolean truth){
        debugInfo = truth;
    }

    public static boolean getDebugInfo(){
        return debugInfo;
    }

    public static void invertDebugInfoBoolean(){
        debugInfo = !debugInfo;
    }

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
            calculateHealthBarElements();

            initMainMenu();
            //this is the scene controller
            handleSceneLogic();

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


    private static void cleanup(){
        Chunk.cleanUp();
        cleanupSoundManager();
        ItemDefinition.cleanUp();
        cleanupRenderer();
    }
}
