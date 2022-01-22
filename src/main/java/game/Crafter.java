package game;

import engine.Window;
import engine.debug.RuntimeInfo;
import engine.disk.Disk;
import engine.disk.SQLiteDiskHandler;
import engine.graphics.Transformation;
import engine.gui.GUI;
import engine.settings.Settings;
import engine.time.Delta;
import engine.time.TimeOfDay;
import engine.time.Timer;
import game.chunk.BiomeGenerator;
import game.chunk.Chunk;
import game.chunk.ChunkUpdateHandler;
import game.chunk.ChunkMeshGenerator;
import game.light.Light;
import game.mainMenu.MainMenu;

public class Crafter {

    public void main(String[] args) {
        Crafter crafter = new Crafter();
        crafter.runGame();
    }

    private final String versionName = "Crafter 0.08a";
    private final Delta delta;
    private final Window window;
    private final Timer timer;
    private final Light light;
    private final RuntimeInfo runtimeInfo;
    private final Disk disk;
    private final MainMenu mainMenu;
    private final Transformation transformation;
    private final GUI gui;
    private final Settings settings;
    private final Chunk chunk;
    private final BiomeGenerator biomeGenerator;
    private final SQLiteDiskHandler sqLiteDiskHandler;
    private final ChunkUpdateHandler chunkUpdateHandler;
    private final ChunkMeshGenerator chunkMeshGenerator;
    private final TimeOfDay timeOfDay;


    public Crafter(){

        this.delta = new Delta();
        this.window = new Window(this.versionName, true, delta); //vsync is on by default - save cpu resources I guess
        this.timer = new Timer(versionName, window);
        this.runtimeInfo = new RuntimeInfo();
        this.disk = new Disk();
        this.mainMenu = new MainMenu(this);
        this.transformation = new Transformation();
        this.gui = new GUI();
        this.settings = new Settings(disk, window);

        this.chunk = new Chunk(settings, delta); //chunk now needs 2 more objects to function, called later

        settings.setChunk(this.chunk);

        this.biomeGenerator = new BiomeGenerator(window, chunk);
        new Thread(this.biomeGenerator).start();
        this.sqLiteDiskHandler = new SQLiteDiskHandler(chunk, biomeGenerator);
        this.chunkUpdateHandler = new ChunkUpdateHandler(chunk, delta);

        this.chunk.setSqLiteDiskHandler(sqLiteDiskHandler);

        this.chunkMeshGenerator = new ChunkMeshGenerator(window,chunkUpdateHandler, chunk);
        new Thread(this.chunkMeshGenerator).start();

        this.chunk.setChunkUpdateHandler(this.chunkUpdateHandler);
        this.timeOfDay = new TimeOfDay(window, light);
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
            //loadSettings();

            //initWindow(versionName, getSettingsVsync());

            //initRenderer();
            //initMouseInput();
            //initSoundManager();
            //initGame();
            //createWorldsDir();

            //this is the biome generator thread singleton
            //this is the light handling thread singleton
            //new Thread(new Light()).start();


            //easterEgg();

            //this is the scene controller
            //handleSceneLogic();

        } catch ( Exception e ){
            System.out.println(e.getMessage());
            System.exit(-1);
        } finally {
            cleanup();
        }
    }

    //the game engine elements
    private void initGame() {
        //initializeHudAtlas();
        //this initializes the block definitions
        //initializeBlocks();
        //this creates a TNT mesh (here for now)
        //createTNTEntityMesh();

        //createInitialInventory();

        //registerMobs();

        //createGUI();
        //registerItems();
        //registerCraftRecipes();
    }

    private void cleanup(){
        chunk.cleanChunkDataMemory();
        //cleanupSoundManager();
        //cleanupRenderer();
    }
}
