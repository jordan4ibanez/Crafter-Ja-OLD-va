package engine.scene;

import java.io.IOException;

public class SceneHandler {
    //0 main menu
    //1 gameplay
    //2 was debug - now reserved for something else in the future if needed
    //3 multiplayer

    private byte currentScene = 0;

    public void setScene(byte newScene){

        //move the camera into position for the main menu
        if (newScene == 0){

            setWindowClearColor(0,0,0,1);
            selectTitleScreenText();
            if (isMouseLocked()){
                toggleMouseLock();
            }

            //don't save the server chunks
            if(currentScene == 1) {
                globalFinalChunkSaveToDisk();
            }
        }


        //generic handler - this goes after scene 0 check for global final save chunks to disk

        //this cleans the memory out
        cleanMemory();

        //gameplay
        if (newScene == 1){
            setWindowClearColor(0.53f,0.81f,0.92f,0.f);
            calculateHealthBarElements(); //todo move this into a loader for player file things
            initialChunkPayload();
            //generateRandomInventory();
            System.out.println("link the cloud position setter into the world initialization protocol");
            setCloudPos(getPlayerCurrentChunkX(),getPlayerCurrentChunkZ());
            generateCloudData();
        }

        //multiplayer
        if (newScene == 3){
            setWindowClearColor(0.53f,0.81f,0.92f,0.f);
            calculateHealthBarElements(); //todo move this into a network thing or something
            if (!isMouseLocked()){
                toggleMouseLock();
            }
            initialChunkPayloadMultiplayer();
            generateCloudData();
        }

        currentScene = newScene;

    }

    public void handleSceneLogic() throws Exception {

        //move the camera into position for the main menu
        if (currentScene == 0){
            setCameraPosition(0,-8,0);
        }


        while (!windowShouldClose()){
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

    private void multiPlayerLoop() throws Exception {
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
        chunkUpdater();
        gameInput();
        multiPlayerUpdate();
        updateMultiplayerWorldChunkLoader();
        updateCamera();
        renderGame();

    }


    private void multiPlayerUpdate() {
        testPlayerDiggingAnimation();
        playerOnTick();
        pauseMenuOnTick();
        inventoryMenuOnTick();
        particlesOnStep();
        //rainDropsOnTick();
        sendPositionData();
    }


    private void mainMenuLoop() throws IOException {
        setCameraPosition(0,-8,0);
        setCameraRotation(0,0,0);
        windowUpdate();
        calculateDelta();
        mainMenuInput();
        doMainMenuLogic();
        renderMainMenu();
    }

    //main game loop
    private void gameLoop() throws Exception {
        pollReceivingPlayerDataFromSQLiteThread();
        calculateDelta();
        //indexLight();
        mouseInput();
        tickUpTimeOfDay();
        //pollTimeOfDay(); //this needs to be in the main thread
        makeCloudsMove();
        countFPS();

        runSpawningAlgorithm();

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

    private void gameUpdate(){
        doChunksHoveringUpThing();
        testPlayerDiggingAnimation();
        playerOnTick();
        itemsOnTick();
        //onTNTStep();
        pauseMenuOnTick();
        inventoryMenuOnTick();
        particlesOnStep();
        fallingEntityOnStep();
        //rainDropsOnTick();
        mobsOnTick();
    }
}
