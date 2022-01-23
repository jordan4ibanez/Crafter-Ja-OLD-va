package engine;

import engine.graphics.Camera;
import engine.gui.GUILogic;
import engine.settings.Settings;
import game.crafting.InventoryLogic;
import game.player.Player;

import static org.lwjgl.glfw.GLFW.*;

public class Controls {

    private final Player player;
    private final Camera camera;
    private final GUILogic guiLogic;
    private final Settings settings;
    private final Window window;
    private final InventoryLogic inventoryLogic;
    private final Mouse mouse;

    private boolean drop        = false;
    private boolean inventory   = false;
    private boolean fullScreen  = false;
    private boolean escape      = false;
    private boolean debug       = false;
    private boolean perspective = false;
    private boolean chatb       = false;
    private boolean enter       = false;
    private boolean forward     = false;
    private boolean backward    = false;
    private boolean left        = false;
    private boolean right       = false;
    private boolean sneak       = false;
    private boolean jump        = false;

    public Controls(Player player, Camera camera, GUILogic guiLogic, Settings settings, Window window, InventoryLogic inventoryLogic, Mouse mouse){
        this.player = player;
        this.camera = camera;
        this.guiLogic = guiLogic;
        this.settings = settings;
        this.window = window;
        this.inventoryLogic = inventoryLogic;
        this.mouse = mouse;
    }

    public void gameInput() {
        if (!player.isInventoryOpen() && !guiLogic.isPaused() && !guiLogic.isChatOpen()) {
            //normal inputs
            if (camera.getCameraPerspective() < 2) {
                forward = window.isKeyPressed(settings.getKeyForward());
                backward = window.isKeyPressed(settings.getKeyBack());
                left = window.isKeyPressed(settings.getKeyLeft());
                right = window.isKeyPressed(settings.getKeyRight());
            }
            //reversed inputs
            else {
                forward = window.isKeyPressed(settings.getKeyBack());
                backward = window.isKeyPressed(settings.getKeyForward());
                left = window.isKeyPressed(settings.getKeyRight());
                right = window.isKeyPressed(settings.getKeyLeft());
            }

            //sneaking
            sneak = window.isKeyPressed(settings.getKeySneak());
            jump = window.isKeyPressed(settings.getKeyJump());

            //drop
            if (window.isKeyPressed(settings.getKeyDrop())) {
                if (!drop) {
                    drop = true;
                    //throwItem();
                }
            } else if (!window.isKeyPressed(settings.getKeyDrop())){
                drop = false;
            }
        }


        //send chat message
        if (window.isKeyPressed(GLFW_KEY_ENTER)){
            if (!enter){
                if (guiLogic.isChatOpen()){
                    guiLogic.sendAndFlushChatMessage();
                    enter = true;
                }
            }
        } else if (!window.isKeyPressed(GLFW_KEY_ENTER)){
            enter = false;
        }

        //debug info
        if (window.isKeyPressed(GLFW_KEY_F3)) {
            if (!debug) {
                debug = true;
                //settings.invertDebugInfoBoolean();
            }
        } else if (!window.isKeyPressed(GLFW_KEY_F3)){
            debug = false;
        }


        //fullscreen
        if (window.isKeyPressed(GLFW_KEY_F11)) {
            if (!fullScreen) {
                fullScreen = true;
                window.toggleFullScreen();
            }
        } else if (!window.isKeyPressed(GLFW_KEY_F11)){
            fullScreen = false;
        }

        //chat
        if (window.isKeyPressed(GLFW_KEY_T)){
            if (!chatb){
                chatb = true;
                /*
                if (getIfMultiplayer()) { //only allow in multiplayer
                    if (!guiLogic.isChatOpen() && !isPlayerInventoryOpen() && !guiLogic.isPaused()) {
                        setChatOpen(true);
                    }
                }
                 */
            }
        } else if (!window.isKeyPressed(GLFW_KEY_T)){
            chatb = false;
        }


        //escape
        if (window.isKeyPressed(GLFW_KEY_ESCAPE)) {
            if (!escape) {
                escape = true;
                //close inventory
                if(player.isInventoryOpen()) {
                    inventoryLogic.closeCraftingInventory();
                //close chat box
                }else if (guiLogic.isChatOpen()){
                    guiLogic.setChatOpen(false);
                //pause game
                } else {
                    mouse.toggleMouseLock();
                    guiLogic.togglePauseMenu();
                    inventoryLogic.getInventory().emptyMouseInventory();
                }
                resetInputs();
            }
        } else if (!window.isKeyPressed(GLFW_KEY_ESCAPE)){
            escape = false;
        }


        //inventory
        if (window.isKeyPressed(settings.getKeyInventory()) && !guiLogic.isPaused() && !guiLogic.isChatOpen()) {
            if (!inventory) {
                inventory = true;
                if (player.isInventoryOpen()){
                    inventoryLogic.closeCraftingInventory();
                } else {
                    inventoryLogic.openCraftingInventory(false);
                }
            }
        } else if (!window.isKeyPressed(settings.getKeyInventory())){
            inventory = false;
        }


        if (!player.isInventoryOpen() && !guiLogic.isPaused()) {
            //mouse left button input
            if (mouse.isLeftButtonPressed()) {
                player.setPlayerMining(true);
                player.startDiggingAnimation();
            } else {
                player.setPlayerMining(false);
            }

            //mouse right button input
            if (mouse.isRightButtonPressed()) {
                player.setPlayerPlacing(true);
                player.startDiggingAnimation();
            } else {
                player.setPlayerPlacing(false);
            }

            float scroll = mouse.getMouseScroll();
            if (scroll < 0) {
                player.changeScrollSelection(1);
            } else if (scroll > 0) {
                player.changeScrollSelection(-1);
            }
        }

        //toggle camera
        if (window.isKeyPressed(GLFW_KEY_F5)) {
            if (!perspective) {
                perspective = true;
                camera.toggleCameraPerspective();
            }
        } else if (!window.isKeyPressed(GLFW_KEY_F5)){
            perspective = false;
        }
    }


    public void mainMenuInput(){
        if (window.isKeyPressed(GLFW_KEY_F11)) {
            if (!fullScreen) {
                fullScreen = true;
                window.toggleFullScreen();
            }
        } else if (!window.isKeyPressed(GLFW_KEY_F11)){
            fullScreen = false;
        }
    }

    public boolean getForward(){
        return forward;
    }
    public boolean getBackward(){
        return backward;
    }
    public boolean getLeft(){
        return left;
    }
    public boolean getRight(){
        return right;
    }
    public boolean getSneak(){
        return sneak;
    }
    public boolean getJump(){
        return jump;
    }

    public void resetInputs(){
        drop        = false;
        inventory   = false;
        fullScreen  = false;
        escape      = false;
        debug       = false;
        perspective = false;
        chatb        = false;
        enter       = false;
        forward     = false;
        backward    = false;
        left        = false;
        right       = false;
        sneak       = false;
        jump        = false;
    }
}
