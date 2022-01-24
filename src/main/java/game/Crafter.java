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

    public Crafter(){
        //engine initializers
        this.compression       = new Compression();
        this.runtimeInfo       = new RuntimeInfo();
        this.disk              = new Disk();
        this.sqLiteDiskHandler = new SQLiteDiskHandler();
        this.camera            = new Camera();
        this.guiLogic          = new GUILogic();



        //engine linkages

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
