package engine.scene;

import game.item.ItemEntity;
import game.tnt.TNTEntity;

import static engine.Controls.gameInput;
import static engine.Controls.mainMenuInput;
import static engine.MouseInput.*;
import static engine.Window.*;
import static engine.debug.CheckRuntimeInfo.doRuntimeInfoUpdate;
import static engine.debug.DebugTerrainDrawTypes.generateDebugChunkMesh;
import static engine.debug.RenderDebug.initializeDebugRenderShader;
import static engine.debug.RenderDebug.renderDebug;
import static engine.debug.debug.debugInput;
import static engine.graphics.Camera.*;
import static engine.gui.GUILogic.calculateHealthBarElements;
import static engine.gui.GUILogic.pauseMenuOnTick;
import static engine.render.MainMenuRenderer.renderMainMenu;
import static engine.render.GameRenderer.renderGame;
import static engine.Time.calculateDelta;
import static engine.Timer.countFPS;
import static engine.settings.Settings.getDebugInfo;
import static engine.sound.SoundManager.updateListenerPosition;
import static game.chunk.Chunk.*;
import static game.chunk.ChunkMesh.popChunkMeshQueue;
import static game.chunk.ChunkUpdateHandler.chunkUpdater;
import static game.crafting.Inventory.generateRandomInventory;
import static game.crafting.InventoryLogic.inventoryMenuOnTick;
import static game.falling.FallingEntity.fallingEntityOnStep;
import static game.item.ItemRegistration.registerItems;
import static game.mainMenu.MainMenu.doMainMenuLogic;
import static game.mainMenu.MainMenu.selectTitleScreenGag;
import static game.mob.Mob.initializeMobRegister;
import static game.mob.Mob.mobsOnTick;
import static game.particle.Particle.particlesOnStep;
import static game.player.Player.*;

public class SceneHandler {
    //0 main menu
    //1 gameplay
    //2 debug
    private static byte currentScene = 0;

    public static void setScene(byte newScene){

        //move the camera into position for the main menu
        if (newScene == 0){
            setWindowClearColor(0,0,0,1);
            selectTitleScreenGag();
            if (isMouseLocked()){
                toggleMouseLock();
            }
            globalFinalChunkSaveToDisk();
        }

        if (newScene == 1){
            setWindowClearColor(0.53f,0.81f,0.92f,0.f);
            calculateHealthBarElements();
            registerItems();
            initializeMobRegister();
            initialChunkPayload();
            generateRandomInventory();
        }

        currentScene = newScene;

    }

    public static void handleSceneLogic() throws Exception {

        //move the camera into position for the main menu
        if (currentScene == 0){
            setCameraPosition(0,-8,0);
        }

        //for debugging
        if (currentScene == 2){
            initializeDebugRenderShader();
            setWindowClearColor(0f,0f,0f,0f);
            generateDebugChunkMesh();
        }

        while (!windowShouldClose()){
            if (getDebugInfo()) {
                doRuntimeInfoUpdate();
            }

            switch (currentScene) {
                case 0 -> mainMenuLoop();
                case 1 -> gameLoop();
                case 2 -> debugLoop();
            }
        }

    }


    private static void debugLoop(){
        windowUpdate();
        calculateDelta();
        mouseInput();
        debugInput();
        renderDebug();
    }


    private static void mainMenuLoop(){
        setCameraPosition(0,-8,0);
        setCameraRotation(0,0,0);
        windowUpdate();
        calculateDelta();
        mainMenuInput();
        doMainMenuLogic();
        renderMainMenu();
    }

    //main game loop
    private static void gameLoop() throws Exception {
        calculateDelta();

        //indexLight();
        mouseInput();

        countFPS();
        updateWorldChunkLoader();
        popChunkMeshQueue(); //this actually transmits the data from the other threads into main thread

        updateListenerPosition();
        chunkUpdater();
        globalChunkSaveToDisk();
        gameInput();
        gameUpdate();
        updateCamera();
        renderGame();
        windowUpdate();

        processOldChunks();
    }

    private static void gameUpdate() throws Exception {
        testPlayerDiggingAnimation();
        playerOnTick();
        ItemEntity.onStep();
        TNTEntity.onTNTStep();
        pauseMenuOnTick();
        inventoryMenuOnTick();
        particlesOnStep();
        fallingEntityOnStep();
        //rainDropsOnTick();
        mobsOnTick();
    }
}
