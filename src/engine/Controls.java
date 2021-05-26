package engine;

import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.MouseInput.*;
import static engine.MouseInput.getMouseScroll;
import static engine.Window.isKeyPressed;
import static engine.Window.toggleFullScreen;
import static engine.graphics.Camera.getCameraPerspective;
import static engine.graphics.Camera.toggleCameraPerspective;
import static engine.gui.GUILogic.isPaused;
import static engine.gui.GUILogic.togglePauseMenu;
import static engine.settings.Settings.*;
import static game.crafting.Inventory.*;
import static game.crafting.InventoryLogic.closeCraftingInventory;
import static game.crafting.InventoryLogic.openCraftingInventory;
import static game.mob.Mob.spawnMob;
import static game.player.Player.*;
import static game.player.Player.changeScrollSelection;
import static org.lwjgl.glfw.GLFW.*;

public class Controls {

    private static boolean throwButtonPushed = false;
    private static boolean mobSpawningButtonPushed = false;
    private static boolean inventoryButtonPushed = false;
    private static boolean F11Pushed           = false;
    private static boolean escapePushed        = false;
    private static boolean F3Pushed            = false;
    private static boolean F5Pushed = false;

    public static void gameInput() {

        if (!isPlayerInventoryOpen() && !isPaused()) {
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
        }


        /*
        if (isKeyPressed(GLFW_KEY_G)) {
            clearItems();
        }

        if (isKeyPressed(GLFW_KEY_R)) {
            setPlayerRunning(true);
        } else {
            setPlayerRunning(false);
        }
         */


        //debug info
        if (isKeyPressed(GLFW_KEY_F3)) {
            if (!F3Pushed) {
                F3Pushed = true;
                invertDebugInfoBoolean();
            }
        } else if (!isKeyPressed(GLFW_KEY_F3)){
            F3Pushed = false;
        }


        //drop
        if (isKeyPressed(getKeyDrop())) {
            if (!throwButtonPushed) {
                throwButtonPushed = true;
                throwItem();
            }
        } else if (!isKeyPressed(getKeyDrop())){
            throwButtonPushed = false;
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

        //esape
        if (isKeyPressed(GLFW_KEY_ESCAPE)) {
            if (!escapePushed) {
                escapePushed = true;
                //close inventory
                if(isPlayerInventoryOpen()){
                    closeCraftingInventory();
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
        if (isKeyPressed(getKeyInventory()) && !isPaused()) {
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


        //spawn human mob
        if (isKeyPressed(GLFW_KEY_T)) {
            if (!mobSpawningButtonPushed) {
                mobSpawningButtonPushed = true;
                spawnMob((int)Math.floor(Math.random() + 0.5f),new Vector3d( getPlayerPos()), new Vector3f(0,0,0));
            }
        } else if (!isKeyPressed(GLFW_KEY_T)){
            mobSpawningButtonPushed = false;
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
                //System.out.println("CAMERA PERSPECTIVE IS NOW: " + getCameraPerspective());
            }
        } else if (!isKeyPressed(GLFW_KEY_F5)){
            F5Pushed = false;
        }
    }


    public static void mainMenuInput(){
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
