package game;

import game.chunk.BiomeGenerator;
import game.chunk.Chunk;
import game.chunk.ChunkMeshGenerator;

import java.awt.*;

import static engine.MouseInput.initMouseInput;
import static engine.Window.initWindow;
import static engine.disk.Disk.createWorldsDir;
import static engine.gui.GUI.createGUI;
import static engine.gui.GUI.initializeHudAtlas;
import static engine.render.GameRenderer.cleanupRenderer;
import static engine.render.GameRenderer.initRenderer;
import static engine.scene.SceneHandler.handleSceneLogic;
import static engine.settings.Settings.getSettingsVsync;
import static engine.settings.Settings.loadSettings;
import static engine.sound.SoundManager.*;
import static game.blocks.BlockDefinition.initializeBlocks;
import static game.crafting.CraftRecipes.registerCraftRecipes;
import static game.crafting.Inventory.createInitialInventory;
import static game.item.ItemRegistration.registerItems;
import static game.mainMenu.MainMenu.easterEgg;
import static game.mainMenu.MainMenu.initMainMenu;
import static game.mob.MobDefinition.registerMobs;
import static game.tnt.TNTEntity.createTNTEntityMesh;

public class Crafter {

    //fields
    private static final String versionName = "Crafter 0.07c";

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

            initMainMenu();

            //this is the chunk mesh generator thread
            ChunkMeshGenerator chunkMeshGenerator = new ChunkMeshGenerator();
            Thread chunkThread = new Thread(chunkMeshGenerator);
            chunkThread.start();

            //this is the biome generator thread
            //TODO: MOVE THIS SOMEWHERE SANE!
            BiomeGenerator biomeGenerator = new BiomeGenerator();
            Thread biomeThread = new Thread(biomeGenerator);
            biomeThread.start();

            easterEgg();

            //this is the scene controller
            handleSceneLogic();

        } catch ( Exception e ){
            System.out.println(e.getMessage());
            System.exit(-1);
        } finally {
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

        createInitialInventory();

        registerMobs();

        createGUI();
        registerItems();
        registerCraftRecipes();
    }


    private static void cleanup(){
        Chunk.cleanChunkDataMemory();
        cleanupSoundManager();
        cleanupRenderer();
    }
}
