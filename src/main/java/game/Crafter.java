package game;

import engine.*;
import engine.compression.Compression;
import engine.debug.RuntimeInfo;
import engine.disk.Disk;
import engine.disk.SQLiteDiskHandler;
import engine.graphics.Camera;
import engine.gui.GUI;
import engine.gui.GUILogic;
import engine.network.Networking;
import engine.render.GameRenderer;
import engine.render.MainMenuRenderer;
import engine.scene.SceneHandler;
import engine.settings.Settings;
import engine.sound.SoundAPI;
import engine.time.Delta;
import engine.time.TimeOfDay;
import engine.time.Timer;
import game.chat.Chat;
import game.chunk.BiomeGenerator;
import game.chunk.Chunk;
import game.chunk.ChunkMeshGenerator;
import game.chunk.ChunkUpdateHandler;
import game.clouds.Cloud;
import game.crafting.InventoryLogic;
import game.entity.EntityContainer;
import game.entity.collision.Collision;
import game.entity.collision.MobCollision;
import game.entity.collision.ParticleCollision;
import game.entity.item.ItemDefinitionContainer;
import game.entity.mob.MobMeshBuilder;
import game.entity.mob.MobSpawning;
import game.light.Light;
import game.mainMenu.MainMenu;
import game.player.Player;
import game.ray.Ray;

public class Crafter {

    public static void main(String[] args) {
        Crafter crafter = new Crafter();
    }

    private final String versionName = "Crafter 0.08a";

    //engine components
    private final Window window;
    private final Compression compression;
    private final RuntimeInfo runtimeInfo;
    private final Disk disk;
    private final SQLiteDiskHandler sqLiteDiskHandler;
    private final Camera camera;
    private final GUI gui;
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

    //game components
    private final Chat chat;
    private final BiomeGenerator biomeGenerator;
    private final Chunk chunk;
    private final ChunkMeshGenerator chunkMeshGenerator;
    private final ChunkUpdateHandler chunkUpdateHandler;
    private final Cloud cloud;
    private final InventoryLogic inventoryLogic;
    private final EntityContainer entityContainer;
    private final Collision collision;
    private final MobCollision mobCollision;
    private final ParticleCollision particleCollision;
    private final MobMeshBuilder mobMeshBuilder;
    private final MobSpawning mobSpawning;
    private final ItemDefinitionContainer itemDefinitionContainer;
    private final MainMenu mainMenu;
    private final Light light;
    private final Player player;
    private final Ray ray;


    public Crafter(){
        //engine initializers
        this.window                  = new Window(this.versionName, true);

        this.compression             = new Compression();
        this.runtimeInfo             = new RuntimeInfo();
        this.disk                    = new Disk();
        this.sqLiteDiskHandler       = new SQLiteDiskHandler();
        this.camera                  = new Camera();
        this.gui                     = new GUI(this.versionName);
        this.guiLogic                = new GUILogic();
        this.networking              = new Networking();
        this.gameRenderer            = new GameRenderer();
        this.mainMenuRenderer        = new MainMenuRenderer();
        this.sceneHandler            = new SceneHandler();
        this.settings                = new Settings();
        this.soundAPI                = new SoundAPI();
        this.delta                   = new Delta();
        this.timeOfDay               = new TimeOfDay();
        this.timer                   = new Timer();
        this.controls                = new Controls();
        this.fancyMath               = new FancyMath();
        this.fastNoise               = new FastNoise();
        this.memorySweeper           = new MemorySweeper();
        this.mouse                   = new Mouse();
        this.utils                   = new Utils();

        //game initializers
        this.chat                    = new Chat();
        this.biomeGenerator          = new BiomeGenerator();
        this.chunk                   = new Chunk();
        this.chunkMeshGenerator      = new ChunkMeshGenerator();
        this.chunkUpdateHandler      = new ChunkUpdateHandler();
        this.cloud                   = new Cloud();
        this.inventoryLogic          = new InventoryLogic();
        this.entityContainer         = new EntityContainer();
        this.collision               = new Collision();
        this.mobCollision            = new MobCollision();
        this.particleCollision       = new ParticleCollision();
        this.mobMeshBuilder          = new MobMeshBuilder();
        this.mobSpawning             = new MobSpawning();
        this.itemDefinitionContainer = new ItemDefinitionContainer();
        this.mainMenu                = new MainMenu();
        this.light                   = new Light();
        this.player                  = new Player();
        this.ray                     = new Ray();



        //engine linkages
        {
            //window
            window.setDelta(this.delta);

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
            gameRenderer.setGui(this.gui);
            gameRenderer.setTimeOfDay(this.timeOfDay);
            gameRenderer.setCloud(this.cloud);
            gameRenderer.setLight(this.light);

            //main menu renderer
            mainMenuRenderer.setWindow(this.window);
            mainMenuRenderer.setCamera(this.camera);
            mainMenuRenderer.setMainMenu(this.mainMenu);
            mainMenuRenderer.setGui(this.gui);
            mainMenuRenderer.setDelta(this.delta);

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
            controls.setInventoryLogic(this.inventoryLogic);
            controls.setMouse(this.mouse);

            //mouse
            mouse.setWindow(this.window);
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
        chunk.setChunkMeshGenerator(this.chunkMeshGenerator);

        //chunk mesh generator
        chunkMeshGenerator.setWindow(this.window);
        chunkMeshGenerator.setChunkUpdateHandler(this.chunkUpdateHandler);
        chunkMeshGenerator.setChunk(this.chunk);
        new Thread(chunkMeshGenerator).start(); //send it on it's way

        //chunk update handler
        chunkUpdateHandler.setDelta(this.delta);
        chunkUpdateHandler.setChunk(this.chunk);
        chunkUpdateHandler.setChunkMeshGenerator(this.chunkMeshGenerator);

        //cloud
        cloud.setDelta(this.delta);

        //inventory logic
        inventoryLogic.setPlayer(this.player);
        inventoryLogic.setMouse(this.mouse);
        inventoryLogic.setWindow(this.window);
        inventoryLogic.setControls(this.controls);

        //collision
        collision.setDelta(this.delta);
        collision.setChunk(this.chunk);
        collision.setPlayer(this.player);

        //mob collision
        mobCollision.setEntityContainer(this.entityContainer);

        //particle collision
        particleCollision.setDelta(this.delta);
        particleCollision.setChunk(this.chunk);

        //item definition container
        itemDefinitionContainer.setChunk(this.chunk);
        itemDefinitionContainer.setPlayer(this.player);
        itemDefinitionContainer.setSoundAPI(this.soundAPI);
        itemDefinitionContainer.setInventoryLogic(this.inventoryLogic);

        //main menu
        mainMenu.setSettings(this.settings);
        mainMenu.setDisk(this.disk);
        mainMenu.setMouse(this.mouse);
        mainMenu.setGuiLogic(this.guiLogic);
        mainMenu.setWindow(this.window);
        mainMenu.setSceneHandler(this.sceneHandler);
        mainMenu.setPlayer(this.player);
        mainMenu.setVersionName(this.versionName);
        mainMenu.initializeGUI();

        //light
        light.setChunk(this.chunk);
        light.setChunkMeshGenerator(this.chunkMeshGenerator);
        light.setWindow(this.window);

        new Thread(this.light).start(); //send light off on it's own thread

        //player
        player.setDelta(this.delta);
        player.setChunk(this.chunk);
        player.setRay(this.ray);
        player.setCamera(this.camera);
        player.setControls(this.controls);
        player.setCollision(this.collision);
        player.setInventoryLogic(this.inventoryLogic);
        player.setCloud(this.cloud);
        player.setGuiLogic(this.guiLogic);
        player.initialize(this.inventoryLogic.getInventory(), this.camera);

        //ray
        ray.setChunk(this.chunk);


        //finally, pass literally everything to the scene handler

    }


    /*
    //core game engine elements
    //load everything
    //public void runGame(){
        //try{
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
     */
}
