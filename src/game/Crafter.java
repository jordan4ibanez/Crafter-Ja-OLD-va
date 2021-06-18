package game;

import com.esotericsoftware.kryo.Kryo;
import engine.network.ChunkRequest;
import engine.network.NetworkHandshake;
import engine.network.PlayerPosObject;
import engine.sound.SoundListener;
import game.chunk.Chunk;
import game.chunk.ChunkMeshGenerator;
import game.item.ItemDefinition;
import org.joml.Vector3d;
import org.lwjgl.openal.AL11;

import java.awt.*;

import static engine.disk.Disk.*;
import static engine.disk.SaveQueue.startSaveThread;
import static engine.scene.SceneHandler.handleSceneLogic;
import static engine.settings.Settings.getSettingsVsync;
import static engine.settings.Settings.loadSettings;
import static game.chunk.Chunk.*;
import static engine.gui.GUI.*;
import static engine.MouseInput.*;
import static game.crafting.CraftRecipes.registerCraftRecipes;
import static game.item.ItemRegistration.registerItems;
import static game.mainMenu.MainMenu.initMainMenu;
import static game.tnt.TNTEntity.createTNTEntityMesh;
import static engine.Window.*;
import static engine.sound.SoundManager.*;
import static engine.render.GameRenderer.*;
import static game.blocks.BlockDefinition.initializeBlocks;
import static game.player.Player.*;

public class Crafter {

    private static int test;

    //fields
    private static final String versionName = "Crafter 0.04b Survival Test";

    public static String getVersionName(){
        return versionName;
    }

    //core game engine elements
    //load everything
    public static void main(String[] args){
        try{
            Toolkit tk = Toolkit.getDefaultToolkit();
            Dimension d = tk.getScreenSize();

            loadSettings();

            initWindow(versionName, d.width/2,d.height/2,getSettingsVsync());

            initRenderer();
            initMouseInput();
            initSoundManager();
            initGame();
            createWorldsDir();
            startSaveThread();
            initMainMenu();

            //this is the chunk mesh generator thread
            ChunkMeshGenerator chunkMeshGenerator = new ChunkMeshGenerator();
            Thread chunkThread = new Thread(chunkMeshGenerator);
            chunkThread.start();

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

    //the game engine elements
    public static void initGame() throws Exception{
        initializeHudAtlas();
        //this initializes the block definitions
        initializeBlocks();
        //this creates a TNT mesh (here for now)
        createTNTEntityMesh();

        setAttenuationModel(AL11.AL_LINEAR_DISTANCE);
        setListener(new SoundListener(new Vector3d()));
        createGUI();
        registerItems();
        registerCraftRecipes();
    }


    private static void cleanup(){
        Chunk.cleanUp();
        cleanupSoundManager();
        ItemDefinition.cleanUp();
        cleanupRenderer();
    }
}
