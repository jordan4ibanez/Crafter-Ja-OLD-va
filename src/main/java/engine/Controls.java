package engine;

import engine.MouseInput.*;
import engine.Window.isKeyPressed;
import engine.Window.toggleFullScreen;
import engine.graphics.Camera.getCameraPerspective;
import engine.graphics.Camera.toggleCameraPerspective;
import engine.gui.GUILogic.*;
import engine.network.Networking.getIfMultiplayer;
import engine.settings.Settings.*;
import game.crafting.Inventory.*;
import game.crafting.InventoryLogic.closeCraftingInventory;
import game.crafting.InventoryLogic.openCraftingInventory;
import game.player.Player.*;
import game.player.WieldHand.startDiggingAnimation;
import org.lwjgl.glfw.GLFW.*;

public class Controls {

    private boolean throwButtonPushed = false;
    private boolean inventoryButtonPushed = false;
    private boolean F11Pushed           = false;
    private boolean escapePushed        = false;
    private boolean F3Pushed            = false;
    private boolean F5Pushed = false;
    private boolean chatButtonPushed = false;
    private boolean enterPushed = false;

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
                if (!throwButtonPushed) {
                    throwButtonPushed = true;
                    throwItem();
                }
            } else if (!isKeyPressed(getKeyDrop())){
                throwButtonPushed = false;
            }
        }


        //send chat message
        if (isKeyPressed(GLFW_KEY_ENTER)){
            if (!enterPushed){
                if (isChatOpen()){
                    sendAndFlushChatMessage();
                    enterPushed = true;
                }
            }
        } else if (!isKeyPressed(GLFW_KEY_ENTER)){
            enterPushed = false;
        }

        //debug info
        if (isKeyPressed(GLFW_KEY_F3)) {
            if (!F3Pushed) {
                F3Pushed = true;
                invertDebugInfoBoolean();
            }
        } else if (!isKeyPressed(GLFW_KEY_F3)){
            F3Pushed = false;
        }


        //fullscreen
        if (isKeyPressed(GLFW_KEY_F11)) {
            if (!F11Pushed) {
                F11Pushed = true;
                toggleFullScreen();
            }
        } else if (!isKeyPressed(GLFW_KEY_F11)){
            F11Pushed = false;
        }

        //chat
        if (isKeyPressed(GLFW_KEY_T)){
            if (!chatButtonPushed){
                chatButtonPushed = true;
                if (getIfMultiplayer()) { //only allow in multiplayer
                    if (!isChatOpen() && !isPlayerInventoryOpen() && !isPaused()) {
                        setChatOpen(true);
                    }
                }
            }
        } else if (!isKeyPressed(GLFW_KEY_T)){
            chatButtonPushed = false;
        }


        //escape
        if (isKeyPressed(GLFW_KEY_ESCAPE)) {
            if (!escapePushed) {
                escapePushed = true;
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
            escapePushed = false;
        }


        //inventory
        if (isKeyPressed(getKeyInventory()) && !isPaused() && !isChatOpen()) {
            if (!inventoryButtonPushed) {
                inventoryButtonPushed = true;
                if (isPlayerInventoryOpen()){
                    closeCraftingInventory();
                } else {
                    openCraftingInventory(false);
                }
            }
        } else if (!isKeyPressed(getKeyInventory())){
            inventoryButtonPushed = false;
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
            if (!F5Pushed) {
                F5Pushed = true;
                toggleCameraPerspective();
            }
        } else if (!isKeyPressed(GLFW_KEY_F5)){
            F5Pushed = false;
        }
    }


    public void mainMenuInput(){
        if (isKeyPressed(GLFW_KEY_F11)) {
            if (!F11Pushed) {
                F11Pushed = true;
                toggleFullScreen();
            }
        } else if (!isKeyPressed(GLFW_KEY_F11)){
            F11Pushed = false;
        }
    }
}
