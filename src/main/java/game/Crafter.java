package game;

import engine.*;
import engine.compression.Compression;
import engine.debug.RuntimeInfo;
import engine.disk.Disk;
import engine.disk.SQLiteDiskAccessThread;
import engine.disk.SQLiteDiskHandler;
import engine.graphics.Camera;
import engine.graphics.Transformation;
import engine.gui.GUI;
import engine.gui.GUILogic;
import engine.network.Networking;
import engine.render.GameRenderer;
import engine.render.MainMenuRenderer;
import engine.scene.Scene;
import engine.scene.SceneHandler;
import engine.settings.Settings;
import engine.sound.SoundAPI;
import engine.time.Delta;
import engine.time.TimeOfDay;
import engine.time.Timer;
import game.chat.Chat;
import game.chunk.BiomeGenerator;
import game.chunk.Chunk;
import game.chunk.ChunkUpdateHandler;
import game.chunk.ChunkMeshGenerator;
import game.crafting.Inventory;
import game.crafting.InventoryLogic;
import game.light.Light;
import game.mainMenu.MainMenu;
import game.player.Player;
import game.ray.Ray;

import java.util.Set;

public class Crafter {

    public void main(String[] args) {
        Crafter crafter = new Crafter();
        crafter.runGame();
    }

    private final String versionName = "Crafter 0.08a";

    //engine components
    private final Compression compression;
    private final RuntimeInfo runtimeInfo;
    private final Disk disk;
    private final SQLiteDiskHandler sqLiteDiskHandler;
    private final Camera camera;
    private final GUILogic guiLogic;
    private final Networking networking;
    private final GameRenderer gameRenderer;
    private final MainMenuRenderer mainMenuRenderer;
    private final SceneHandler sceneHandler;
    private final Settings settings;
    private final SoundAPI soundAPI;
    private final Delta delta;
    private final TimeOfDay timeOfDay;
    private final Timer timer;
    private final Controls controls;
    private final FancyMath fancyMath;
    private final FastNoise fastNoise;
    private final MemorySweeper memorySweeper;
    private final Mouse mouse;
    private final Utils utils;
    private final Window window;

    //game components
    private final Chat chat;
    private final BiomeGenerator biomeGenerator;
    private final Chunk chunk;
    private final ChunkUpdateHandler chunkUpdateHandler;


    public Crafter(){
        //engine initializers
        this.compression         = new Compression();
        this.runtimeInfo         = new RuntimeInfo();
        this.disk                = new Disk();
        this.sqLiteDiskHandler   = new SQLiteDiskHandler();
        this.camera              = new Camera();
        this.guiLogic            = new GUILogic();
        this.networking          = new Networking();
        this.gameRenderer        = new GameRenderer();
        this.mainMenuRenderer    = new MainMenuRenderer();
        this.sceneHandler        = new SceneHandler();
        this.settings            = new Settings();
        this.soundAPI            = new SoundAPI();
        this.delta               = new Delta();
        this.timeOfDay           = new TimeOfDay();
        this.timer               = new Timer();
        this.controls            = new Controls();
        this.fancyMath           = new FancyMath();
        this.fastNoise           = new FastNoise();
        this.memorySweeper       = new MemorySweeper();
        this.mouse               = new Mouse();
        this.utils               = new Utils();
        this.window              = new Window(this.versionName, this.settings.getSettingsVsync());

        //game initializers
        this.chat               = new Chat();
        this.biomeGenerator     = new BiomeGenerator();
        this.chunk              = new Chunk();
        this.chunkUpdateHandler = new ChunkUpdateHandler();

        //engine linkages
        {
            //disk
            disk.setSqLiteDiskHandler(this.sqLiteDiskHandler);

            //SQLiteDiskHandler
            sqLiteDiskHandler.setBiomeGenerator(this.biomeGenerator);
            sqLiteDiskHandler.setChunk(this.chunk);
            sqLiteDiskHandler.setPlayer(this.player);
            sqLiteDiskHandler.setInventoryLogic(this.inventoryLogic);

            //camera
            camera.setMouse(this.mouse);
            camera.setPlayer(this.player);
            camera.setRay(this.ray);

            //gui logic
            guiLogic.setSettings(this.settings);
            guiLogic.setMouse(this.mouse);
            guiLogic.setWindow(this.window);
            guiLogic.setChat(this.chat);
            guiLogic.setPlayer(this.player);
            guiLogic.setDelta(this.delta);
            guiLogic.setMainMenu(this.mainMenu);
            guiLogic.setSqLiteDiskHandler(this.sqLiteDiskHandler);
            guiLogic.setSceneHandler(this.sceneHandler);

            //networking
            networking.setChunk(this.chunk);

            //game renderer
            gameRenderer.setCamera(this.camera);
            gameRenderer.setDelta(this.delta);
            gameRenderer.setSettings(this.settings);
            gameRenderer.setWindow(this.window);
            gameRenderer.setPlayer(this.player);

            //settings
            settings.setChunk(this.chunk);
            settings.setWindow(this.window);
            settings.setDisk(this.disk);

            //sound API
            soundAPI.setCamera(this.camera);
            soundAPI.setWindow(this.window);

            //time of day
            timeOfDay.setWindow(this.window);
            timeOfDay.setLight(this.light);

            //timer
            timer.setWindow(this.window);
            timer.setVersionName(this.versionName);

            //controls
            controls.setPlayer(this.player);
            controls.setCamera(this.camera);
            controls.setGuiLogic(this.guiLogic);
            controls.setSettings(this.settings);
            controls.setWindow(this.window);
            controls.setInventoryLogic(this.invetoryLogic);
            controls.setMouse(this.mouse);

            //mouse
            mouse.setWindow(this.window);

            //window
            window.setDelta(this.delta);
        }
        //game linkages

        //chat
        chat.setPlayer(this.player);
        chat.setDelta(this.delta);

        //biome generator
        biomeGenerator.setWindow(this.window);
        biomeGenerator.setChunk(this.chunk);

        //chunk
        chunk.setDelta(this.delta);
        chunk.setSettings(this.settings);
        chunk.setPlayer(this.player);
        chunk.setSqLiteDiskHandler(this.sqLiteDiskHandler);
        chunk.setChunkUpdateHandler(this.chunkUpdateHandler);

        //chunk update handler
        chunkUpdateHandler.setDelta(this.delta);
        chunkUpdateHandler.setChunk(this.chunk);



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
        //chunk.cleanChunkDataMemory();
        //cleanupSoundManager();
        //cleanupRenderer();
    }
}
