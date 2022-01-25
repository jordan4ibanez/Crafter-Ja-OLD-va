package engine.scene;

import engine.Controls;
import engine.MemorySweeper;
import engine.Mouse;
import engine.Window;
import engine.disk.SQLiteDiskHandler;
import engine.graphics.Camera;
import engine.gui.GUILogic;
import engine.render.GameRenderer;
import engine.render.MainMenuRenderer;
import engine.sound.SoundAPI;
import engine.time.Delta;
import engine.time.TimeOfDay;
import engine.time.Timer;
import game.chunk.Chunk;
import game.chunk.ChunkUpdateHandler;
import game.clouds.Cloud;
import game.crafting.InventoryLogic;
import game.entity.EntityContainer;
import game.entity.mob.MobMeshBuilder;
import game.entity.mob.MobSpawning;
import game.mainMenu.MainMenu;
import game.player.Player;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.io.IOException;

public class SceneHandler {
    //0 main menu
    //1 gameplay
    //2 was debug - now reserved for something else in the future if needed
    //3 multiplayer

    private Window window;
    private MainMenu mainMenu;
    private Mouse mouse;
    private Chunk chunk;
    private MemorySweeper memorySweeper;
    private InventoryLogic inventoryLogic;
    private GUILogic guiLogic;
    private Cloud cloud;
    private Player player;
    private Camera camera;
    private Delta delta;
    private Controls controls;
    private SQLiteDiskHandler sqLiteDiskHandler;
    private TimeOfDay timeOfDay;
    private Timer timer;
    private MobSpawning mobSpawning;
    private MobMeshBuilder mobMeshBuilder;
    private EntityContainer entityContainer;
    private ChunkUpdateHandler chunkUpdateHandler;
    private SoundAPI soundAPI;
    private GameRenderer gameRenderer;
    private MainMenuRenderer mainMenuRenderer;

    public void passObjects(Window window, MainMenu mainMenu, Mouse mouse, Chunk chunk, MemorySweeper memorySweeper, InventoryLogic inventoryLogic, GUILogic guiLogic,
                            Cloud cloud, Player player, Camera camera, Delta delta, Controls controls,SQLiteDiskHandler sqLiteDiskHandler, TimeOfDay timeOfDay,
                            Timer timer, MobSpawning mobSpawning, MobMeshBuilder mobMeshBuilder, EntityContainer entityContainer, ChunkUpdateHandler chunkUpdateHandler,
                            SoundAPI soundAPI, GameRenderer gameRenderer, MainMenuRenderer mainMenuRenderer){
        this.window = window;
        this.mainMenu = mainMenu;
        this.mouse = mouse;
        this.chunk = chunk;
        this.memorySweeper = memorySweeper;
        this.inventoryLogic = inventoryLogic;
        this.guiLogic = guiLogic;
        this.cloud = cloud;
        this.player = player;
        this.camera = camera;
        this.delta = delta;
        this.controls = controls;
        this.sqLiteDiskHandler = sqLiteDiskHandler;
        this.timeOfDay = timeOfDay;
        this.timer = timer;
        this.mobSpawning = mobSpawning;
        this.mobMeshBuilder = mobMeshBuilder;
        this.entityContainer = entityContainer;
        this.chunkUpdateHandler = chunkUpdateHandler;
        this.soundAPI = soundAPI;
        this.gameRenderer = gameRenderer;
        this.mainMenuRenderer = mainMenuRenderer;
    }

    private byte currentScene = 0;

    public void setScene(byte newScene){

        //move the camera into position for the main menu
        if (newScene == 0){

            window.setWindowClearColor(0,0,0,1);
            mainMenu.selectTitleScreenText();
            if (mouse.isLocked()){
                mouse.toggleMouseLock();
            }

            //don't save the server chunks
            if(currentScene == 1) {
                chunk.globalFinalChunkSaveToDisk();
            }
        }


        //generic handler - this goes after scene 0 check for global final save chunks to disk

        //this cleans the memory out
        memorySweeper.cleanMemory();

        //gameplay
        if (newScene == 1){
            window.setWindowClearColor(0.53f,0.81f,0.92f,0.f);
            guiLogic.calculateHealthBarElements(); //todo move this into a loader for player file things
            chunk.initialChunkPayload();
            //generateRandomInventory();
            System.out.println("link the cloud position setter into the world initialization protocol");
            cloud.setCloudPos(player.getPlayerCurrentChunk().x,player.getPlayerCurrentChunk().y);
            cloud.generateCloudData();
        }

        //multiplayer
        if (newScene == 3){
            window.setWindowClearColor(0.53f,0.81f,0.92f,0.f);
            guiLogic.calculateHealthBarElements(); //todo move this into a network thing or something
            if (!mouse.isLocked()){
                mouse.toggleMouseLock();
            }
            //chunk.initialChunkPayloadMultiplayer();
            cloud.generateCloudData();
        }

        currentScene = newScene;

    }

    public void handleSceneLogic() throws Exception {

        //move the camera into position for the main menu
        if (currentScene == 0){
            camera.setCameraPosition(new Vector3d(0,-8,0));
        }


        while (!window.shouldClose()){
            //if (getDebugInfo()) {
                //turn this back on
                //doRuntimeInfoUpdate();
            //}

            switch (currentScene) {
                case 0 -> mainMenuLoop();
                case 1 -> gameLoop();
                case 3 -> multiPlayerLoop();
            }
        }

    }


    private void multiPlayerLoop() {
        System.out.println("Multiplayer is disabled until Netty is implemented");
        /*
        if (!getIfConnected()){
            setScene((byte)0);
            setMenuPage((byte)7);
            return;
        }
        windowUpdate();
        calculateDelta();
        mouseInput();
        countFPS();
        makeCloudsMove();
        popChatMessageBuffer();
        pollTimeOfDay(); //this needs to be in the main thread
        tickUpChatTimers();
        deleteOldChatMeshes();
        //processQueuedItemsToBeAddedInMultiplayer();
        //itemsOnTickMultiplayer();
        popItemsAddingQueue();
        popChunkMeshQueue(); //this actually transmits the data from the other threads into main thread
        updateListenerPosition();
        gameInput();
        multiPlayerUpdate();
        updateMultiplayerWorldChunkLoader();
        updateCamera();
        renderGame();
         */
    }


    private void multiPlayerUpdate() {
        /*
        testPlayerDiggingAnimation();
        playerOnTick();
        pauseMenuOnTick();
        inventoryMenuOnTick();
        particlesOnStep();
        //rainDropsOnTick();
        sendPositionData();
         */
    }


    private void mainMenuLoop() throws IOException {
        camera.setCameraPosition(new Vector3d(0,-8,0));
        camera.setCameraRotation(new Vector3f(0,0,0));
        window.windowUpdate();
        delta.calculateDelta();
        controls.mainMenuInput();
        mainMenu.doMainMenuLogic(this.delta);
        mainMenuRenderer.renderMainMenu();
    }

    //main game loop
    private void gameLoop() throws Exception {
        sqLiteDiskHandler.poll();
        delta.calculateDelta();
        //indexLight();
        mouse.mouseInput();
        timeOfDay.tickUpTimeOfDay(this.delta);
        //pollTimeOfDay(); //this needs to be in the main thread
        cloud.makeCloudsMove();
        timer.countFPS();

        mobSpawning.runSpawningAlgorithm(this.player,this.chunk,this.delta,this.mobMeshBuilder,this.entityContainer);

        player.updateWorldChunkLoader();

        chunkUpdateHandler.popChunkMeshQueue(); //this actually transmits the data from the other threads into main thread

        soundAPI.updateListenerPos();

        //chunkUpdater();

        chunk.globalChunkSaveToDisk();

        controls.gameInput();

        gameUpdate();

        camera.updateCamera();

        gameRenderer.renderGame();

        window.windowUpdate();

        chunk.processOldChunks();
    }

    private void gameUpdate(){
        chunk.doChunksHoveringUpThing(this.delta);
        //player.testPlayerDiggingAnimation();
        player.playerOnTick();
        //itemsOnTick();
        //onTNTStep();
        guiLogic.pauseMenuOnTick();
        //inventoryMenuOnTick();
        //particlesOnStep();
        //fallingEntityOnStep();
        //rainDropsOnTick();
        //mobsOnTick();
    }
}
