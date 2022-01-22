package engine;

public class Controls {

    private boolean drop        = false;
    private boolean inventory   = false;
    private boolean fullScreen  = false;
    private boolean escape      = false;
    private boolean debug       = false;
    private boolean perspective = false;
    private boolean chat        = false;
    private boolean enter       = false;
    private boolean forward     = false;
    private boolean backward    = false;
    private boolean left        = false;
    private boolean right       = false;
    private boolean sneak       = false;
    private boolean jump        = false;

    public void gameInput() {
        if (!isPlayerInventoryOpen() && !isPaused() && !isChatOpen()) {
            //normal inputs
            if (getCameraPerspective() < 2) {
                setPlayerForward(isKeyPressed(getKeyForward()));
                setPlayerBackward(isKeyPressed(getKeyBack()));
                setPlayerLeft(isKeyPressed(getKeyLeft()));
                setPlayerRight(isKeyPressed(getKeyRight()));
            }
            //reversed inputs
            else {
                setPlayerForward(isKeyPressed(getKeyBack()));
                setPlayerBackward(isKeyPressed(getKeyForward()));
                setPlayerLeft(isKeyPressed(getKeyRight()));
                setPlayerRight(isKeyPressed(getKeyLeft()));
            }

            //sneaking
            setPlayerSneaking(isKeyPressed(getKeySneak()));
            setPlayerJump(isKeyPressed(getKeyJump()));

            //drop
            if (isKeyPressed(getKeyDrop())) {
                if (!drop) {
                    drop = true;
                    throwItem();
                }
            } else if (!isKeyPressed(getKeyDrop())){
                drop = false;
            }
        }


        //send chat message
        if (isKeyPressed(GLFW_KEY_ENTER)){
            if (!enter){
                if (isChatOpen()){
                    sendAndFlushChatMessage();
                    enter = true;
                }
            }
        } else if (!isKeyPressed(GLFW_KEY_ENTER)){
            enter = false;
        }

        //debug info
        if (isKeyPressed(GLFW_KEY_F3)) {
            if (!debug) {
                debug = true;
                invertDebugInfoBoolean();
            }
        } else if (!isKeyPressed(GLFW_KEY_F3)){
            debug = false;
        }


        //fullscreen
        if (isKeyPressed(GLFW_KEY_F11)) {
            if (!fullScreen) {
                fullScreen = true;
                toggleFullScreen();
            }
        } else if (!isKeyPressed(GLFW_KEY_F11)){
            fullScreen = false;
        }

        //chat
        if (isKeyPressed(GLFW_KEY_T)){
            if (!chat){
                chat = true;
                if (getIfMultiplayer()) { //only allow in multiplayer
                    if (!isChatOpen() && !isPlayerInventoryOpen() && !isPaused()) {
                        setChatOpen(true);
                    }
                }
            }
        } else if (!isKeyPressed(GLFW_KEY_T)){
            chat = false;
        }


        //escape
        if (isKeyPressed(GLFW_KEY_ESCAPE)) {
            if (!escape) {
                escape = true;
                //close inventory
                if(isPlayerInventoryOpen()) {
                    closeCraftingInventory();
                //close chat box
                }else if (isChatOpen()){
                    setChatOpen(false);
                //pause game
                } else {
                    toggleMouseLock();
                    togglePauseMenu();
                    emptyMouseInventory();
                }
                resetPlayerInputs();
            }
        } else if (!isKeyPressed(GLFW_KEY_ESCAPE)){
            escape = false;
        }


        //inventory
        if (isKeyPressed(getKeyInventory()) && !isPaused() && !isChatOpen()) {
            if (!inventory) {
                inventory = true;
                if (isPlayerInventoryOpen()){
                    closeCraftingInventory();
                } else {
                    openCraftingInventory(false);
                }
            }
        } else if (!isKeyPressed(getKeyInventory())){
            inventory = false;
        }


        if (!isPlayerInventoryOpen() && !isPaused()) {
            //mouse left button input
            if (isLeftButtonPressed()) {
                setPlayerMining(true);
                startDiggingAnimation();
            } else {
                setPlayerMining(false);
            }

            //mouse right button input
            if (isRightButtonPressed()) {
                setPlayerPlacing(true);
                startDiggingAnimation();
            } else {
                setPlayerPlacing(false);
            }

            float scroll = getMouseScroll();
            if (scroll < 0) {
                changeScrollSelection(1);
            } else if (scroll > 0) {
                changeScrollSelection(-1);
            }
        }

        //toggle camera
        if (isKeyPressed(GLFW_KEY_F5)) {
            if (!perspective) {
                perspective = true;
                toggleCameraPerspective();
            }
        } else if (!isKeyPressed(GLFW_KEY_F5)){
            perspective = false;
        }
    }


    public void mainMenuInput(){
        if (isKeyPressed(GLFW_KEY_F11)) {
            if (!fullScreen) {
                fullScreen = true;
                toggleFullScreen();
            }
        } else if (!isKeyPressed(GLFW_KEY_F11)){
            fullScreen = false;
        }
    }
}
