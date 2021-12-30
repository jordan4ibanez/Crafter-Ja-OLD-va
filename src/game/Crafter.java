package game;

import engine.sound.SoundListener;
import game.chunk.BiomeGenerator;
import game.chunk.Chunk;
import game.chunk.ChunkMeshGenerator;
import org.joml.Vector3d;
import org.lwjgl.openal.AL11;

import java.awt.*;

import static engine.MouseInput.initMouseInput;
import static engine.Window.initWindow;
import static engine.disk.Disk.createWorldsDir;
import static engine.disk.Disk.savePlayerPos;
import static engine.disk.SQLiteDiskAccessThread.closeWorldDataBase;
import static engine.disk.SQLiteDiskAccessThread.connectWorldDataBase;
import static engine.gui.GUI.createGUI;
import static engine.gui.GUI.initializeHudAtlas;
import static engine.render.GameRenderer.cleanupRenderer;
import static engine.render.GameRenderer.initRenderer;
import static engine.scene.SceneHandler.handleSceneLogic;
import static engine.settings.Settings.getSettingsVsync;
import static engine.settings.Settings.loadSettings;
import static engine.sound.SoundManager.*;
import static game.blocks.BlockDefinition.initializeBlocks;
import static game.chunk.Chunk.globalFinalChunkSaveToDisk;
import static game.crafting.CraftRecipes.registerCraftRecipes;
import static game.item.ItemRegistration.registerItems;
import static game.mainMenu.MainMenu.initMainMenu;
import static game.mob.Mob.registerMobs;
import static game.player.Player.getPlayerPos;
import static game.tnt.TNTEntity.createTNTEntityMesh;

public class Crafter {

    //fields
    private static final String versionName = "Crafter 0.06d Massive Engine Rework - World Save Backend Rewrite - VERY Broken";

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

            //TODO: THIS IS DEBUG! THIS IS DEBUG! THIS IS DEBUG! THIS IS DEBUG! THIS IS DEBUG! THIS IS DEBUG! THIS IS DEBUG! THIS IS DEBUG! THIS IS DEBUG! THIS IS DEBUG!

            connectWorldDataBase("world1");

            //TODO: THIS IS DEBUG! THIS IS DEBUG! THIS IS DEBUG! THIS IS DEBUG! THIS IS DEBUG! THIS IS DEBUG! THIS IS DEBUG! THIS IS DEBUG! THIS IS DEBUG! THIS IS DEBUG!

            initMainMenu();

            //this is the chunk mesh generator thread
            ChunkMeshGenerator chunkMeshGenerator = new ChunkMeshGenerator();
            Thread chunkThread = new Thread(chunkMeshGenerator);

            chunkThread.start();

            //this is the biome generator thread
            BiomeGenerator biomeGenerator = new BiomeGenerator();
            Thread biomeThread = new Thread(biomeGenerator);

            biomeThread.start();


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

        registerMobs();

        setAttenuationModel(AL11.AL_LINEAR_DISTANCE);
        setListener(new SoundListener(new Vector3d()));
        createGUI();
        registerItems();
        registerCraftRecipes();
    }


    private static void cleanup(){
        Chunk.cleanChunkDataMemory();
        cleanupSoundManager();
        cleanupRenderer();
        System.out.println("REMEMBER TO REMOVE THE WORLD DATABASE CLOSURE FROM CLEANUP!");
        closeWorldDataBase();
    }
}
