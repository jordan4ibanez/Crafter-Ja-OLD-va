package engine;

import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.MouseInput.*;
import static engine.MouseInput.getMouseScroll;
import static engine.Window.isKeyPressed;
import static engine.Window.toggleFullScreen;
import static engine.gui.GUILogic.isPaused;
import static engine.gui.GUILogic.togglePauseMenu;
import static engine.settings.Settings.*;
import static game.crafting.Inventory.*;
import static game.mob.Mob.spawnMob;
import static game.player.Player.*;
import static game.player.Player.changeScrollSelection;
import static org.lwjgl.glfw.GLFW.*;

public class Controls {

    private static boolean qButtonPushed       = false;
    private static boolean tButtonPushed       = false;
    private static boolean eButtonPushed       = false;
    private static boolean F11Pushed           = false;
    private static boolean escapePushed        = false;
    private static boolean F3Pushed            = false;

    public static void gameInput() {

        if (!isPlayerInventoryOpen() && !isPaused()) {
            if (isKeyPressed(getKeyForward())) {
                setPlayerForward(true);
            } else {
                setPlayerForward(false);
            }

            if (isKeyPressed(getKeyBack())) {
                setPlayerBackward(true);
            } else {
                setPlayerBackward(false);
            }
            if (isKeyPressed(getKeyLeft())) {
                setPlayerLeft(true);
            } else {
                setPlayerLeft(false);
            }
            if (isKeyPressed(getKeyRight())) {
                setPlayerRight(true);
            } else {
                setPlayerRight(false);
            }

            if (isKeyPressed(getKeySneak())) { //sneaking
                setPlayerSneaking(true);
            } else {
                setPlayerSneaking(false);
            }

            if (isKeyPressed(getKeyJump())) {
                setPlayerJump(true);
            } else {
                setPlayerJump(false);
            }
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


        if (isKeyPressed(GLFW_KEY_F3)) {
            if (!F3Pushed) {
                F3Pushed = true;
                invertDebugInfoBoolean();
            }
        } else if (!isKeyPressed(GLFW_KEY_F3)){
            F3Pushed = false;
        }


        if (isKeyPressed(getKeyDrop())) {
            if (!qButtonPushed) {
                qButtonPushed = true;
                throwItem();
            }
        } else if (!isKeyPressed(getKeyDrop())){
            qButtonPushed = false;
        }

        if (isKeyPressed(GLFW_KEY_F11)) {
            if (!F11Pushed) {
                F11Pushed = true;
                toggleFullScreen();
            }
        } else if (!isKeyPressed(GLFW_KEY_F11)){
            F11Pushed = false;
        }

        if (isKeyPressed(GLFW_KEY_ESCAPE)) {
            if (!escapePushed) {
                escapePushed = true;
                if(isPlayerInventoryOpen()){
                    togglePlayerInventory();
                    toggleMouseLock();
                    emptyMouseInventory();
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


        //prototype clear objects - C KEY
        if (isKeyPressed(getKeyInventory()) && !isPaused()) {
            if (!eButtonPushed) {
                eButtonPushed = true;
                togglePlayerInventory();
                toggleMouseLock();
                resetPlayerInputs();
                emptyMouseInventory();
            }
        } else if (!isKeyPressed(getKeyInventory())){
            eButtonPushed = false;
        }


        //spawn human mob
        if (isKeyPressed(GLFW_KEY_T)) {
            if (!tButtonPushed) {
                tButtonPushed = true;
                spawnMob((int)Math.floor(Math.random() + 0.5f),new Vector3d( getPlayerPos()), new Vector3f(0,0,0));
            }
        } else if (!isKeyPressed(GLFW_KEY_T)){
            tButtonPushed = false;
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
