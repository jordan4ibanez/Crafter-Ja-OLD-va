package engine.scene;

import game.item.ItemEntity;
import game.tnt.TNTEntity;

import static engine.Controls.gameInput;
import static engine.Controls.mainMenuInput;
import static engine.MouseInput.*;
import static engine.Window.*;
import static engine.debug.DebugTerrainDrawTypes.generateDebugChunkMesh;
import static engine.debug.RenderDebug.initializeDebugRenderShader;
import static engine.debug.RenderDebug.renderDebug;
import static engine.debug.debug.debugInput;
import static engine.graph.Camera.*;
import static engine.gui.GUILogic.hudOnStepTest;
import static engine.render.MainMenuRenderer.renderMainMenu;
import static engine.render.GameRenderer.renderGame;
import static engine.Time.calculateDelta;
import static engine.Timer.countFPS;
import static engine.sound.SoundManager.updateListenerPosition;
import static game.chunk.Chunk.globalChunkSaveToDisk;
import static game.chunk.Chunk.processOldChunks;
import static game.chunk.ChunkMesh.popChunkMeshQueue;
import static game.chunk.ChunkUpdateHandler.chunkUpdater;
import static game.falling.FallingEntity.fallingEntityOnStep;
import static game.mainMenu.MainMenu.doMainMenuLogic;
import static game.mainMenu.MainMenu.selectTitleScreenGag;
import static game.mob.Mob.mobsOnTick;
import static game.particle.Particle.particlesOnStep;
import static game.player.Player.*;

public class SceneHandler {
    //0 main menu
    //1 gameplay
    //2 debug
    private static byte currentScene = 0;

    public static void setScene(byte newScene){
        currentScene = newScene;

        //move the camera into position for the main menu
        if (currentScene == 0){
            setCameraPosition(0,-8,0);
            setCameraRotation(0,0,0);
            setWindowClearColor(0,0,0,1);
            selectTitleScreenGag();
            if (isMouseLocked()){
                toggleMouseLock();
            }
        }

        if (currentScene == 1){
            setWindowClearColor(0.53f,0.81f,0.92f,0.f);
        }

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
            switch (currentScene){
                case 0:
                    mainMenuLoop();
                    break;
                case 1:
                    gameLoop();
                    break;
                case 2:
                    debugLoop();
                    break;
            }
        }

    }


    private static void debugLoop(){
        calculateDelta();

        mouseInput();
        debugInput();

        renderDebug();
        windowUpdate();
    }


    private static void mainMenuLoop(){
        calculateDelta();

        mainMenuInput();
        doMainMenuLogic();
        renderMainMenu();
        windowUpdate();
    }

    //main game loop
    private static void gameLoop() throws Exception {
        calculateDelta();

        //indexLight();
        mouseInput();
        updateCamera();
        countFPS();
        updateWorldChunkLoader();
        popChunkMeshQueue(); //this actually transmits the data from the other threads into main thread

        updateListenerPosition();
        chunkUpdater();
        globalChunkSaveToDisk();
        gameInput();
        //testLightLevel();
        gameUpdate();

        renderGame();
        windowUpdate();

        processOldChunks();



        /*
        long count = 0;

        boolean debugLowFPS = true; //this sets my machine (jordan4ibanez) to 14FPS


        if (debugLowFPS) {
            while (count < 75_000L) {
                System.out.println(count);
                count++;
            }
        }

         */
    }

    private static void gameUpdate() throws Exception {
        testPlayerDiggingAnimation();
        playerOnTick();
        ItemEntity.onStep();
        TNTEntity.onTNTStep();
        hudOnStepTest();
        particlesOnStep();
        fallingEntityOnStep();
        //rainDropsOnTick();
        mobsOnTick();
    }
}
