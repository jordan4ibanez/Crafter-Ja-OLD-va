package game;

import engine.Window;
import engine.debug.RuntimeInfo;
import engine.disk.Disk;
import engine.graphics.Transformation;
import engine.gui.GUI;
import game.chunk.BiomeGenerator;
import game.chunk.Chunk;
import game.chunk.ChunkMeshGenerator;
import game.light.Light;
import game.mainMenu.MainMenu;

import static engine.render.GameRenderer.cleanupRenderer;
import static engine.render.GameRenderer.initRenderer;
public class Crafter {

    public void main(String[] args) {
        //the whole game is an object :D
        Crafter crafter = new Crafter();
        crafter.runGame();
    }

    private final String versionName = "Crafter 0.08a";
    private final Window window;
    private final RuntimeInfo runtimeInfo;
    private final Disk disk;
    private final MainMenu mainMenu;
    private final Transformation transformation;
    private final GUI gui;

    public Crafter(){
        this.window = new Window(this.versionName, getSettingsVsync());
        this.runtimeInfo = new RuntimeInfo();
        this.disk = new Disk();
        this.mainMenu = new MainMenu(this);
        this.transformation = new Transformation();
        this.gui = new GUI();
    }

    public String getVersionName(){
        return this.versionName;
    }

    public Window getWindow(){
        return this.window;
    }

    public MainMenu getMainMenu(){
        return this.mainMenu;
    }


    //core game engine elements
    //load everything
    public void runGame(){
        try{


            loadSettings();

            initWindow(versionName, getSettingsVsync());

            initRenderer();
            initMouseInput();
            initSoundManager();
            initGame();
            createWorldsDir();

            //this is the chunk mesh generator thread singleton
            new Thread(new ChunkMeshGenerator()).start();
            //this is the biome generator thread singleton
            new Thread(new BiomeGenerator()).start();
            //this is the light handling thread singleton
            new Thread(new Light()).start();


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
    private void initGame() {
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


    private void cleanup(){
        Chunk.cleanChunkDataMemory();
        cleanupSoundManager();
        cleanupRenderer();
    }
}
