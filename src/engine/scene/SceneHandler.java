package engine.scene;

import game.item.ItemEntity;
import game.tnt.TNTEntity;

import static engine.Controls.input;
import static engine.Hud.hudOnStepTest;
import static engine.MainMenuRenderer.renderMainMenu;
import static engine.MouseInput.mouseInput;
import static engine.GameRenderer.renderGame;
import static engine.Time.calculateDelta;
import static engine.Timer.countFPS;
import static engine.Window.windowShouldClose;
import static engine.Window.windowUpdate;
import static engine.graph.Camera.updateCamera;
import static engine.sound.SoundManager.updateListenerPosition;
import static game.chunk.Chunk.globalChunkSaveToDisk;
import static game.chunk.ChunkMesh.popChunkMeshQueue;
import static game.chunk.ChunkUpdateHandler.chunkUpdater;
import static game.falling.FallingEntity.fallingEntityOnStep;
import static game.mainMenu.MainMenu.doMainMenuLogic;
import static game.mob.Mob.mobsOnTick;
import static game.particle.Particle.particlesOnStep;
import static game.player.Player.*;

public class SceneHandler {
    private static byte currentScene = 0;

    public static void setScene(byte newScene){
        currentScene = newScene;
    }

    public static void handleSceneLogic() throws Exception {

        while (!windowShouldClose()){
            switch (currentScene){
                case 0:
                    mainMenuLoop();
                    break;
                case 1:
                    gameLoop();
                    break;
            }
        }

    }


    private static void mainMenuLoop(){
        calculateDelta();

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
        globalChunkSaveToDisk(); //add in a getDelta argument into this!
        input();
        //testLightLevel();
        gameUpdate();

        renderGame();
        windowUpdate();


        /*
        long count = 0;

        boolean debugLowFPS = false; //this sets my machine (jordan4ibanez) to 7-9FPS

        if (debugLowFPS) {
            while (count < 500_000_000L) {
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
