package engine;

import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.Hud.isPaused;
import static engine.Hud.togglePauseMenu;
import static engine.MouseInput.*;
import static engine.MouseInput.getMouseScroll;
import static engine.Window.isKeyPressed;
import static engine.Window.toggleFullScreen;
import static game.item.ItemEntity.clearItems;
import static game.mob.Mob.spawnMob;
import static game.player.Inventory.emptyMouseInventory;
import static game.player.Inventory.throwItem;
import static game.player.Player.*;
import static game.player.Player.changeScrollSelection;
import static org.lwjgl.glfw.GLFW.*;

public class Controls {

    private static boolean qButtonPushed       = false;
    private static boolean rButtonPushed       = false;
    private static boolean tButtonPushed       = false;
    private static boolean cButtonPushed       = false;
    private static boolean eButtonPushed       = false;
    private static boolean F11Pushed           = false;
    private static boolean escapePushed        = false;
    public static void input() {
        if (!isPlayerInventoryOpen() && !isPaused()) {
            if (isKeyPressed(GLFW_KEY_W)) {
                setPlayerForward(true);
            } else {
                setPlayerForward(false);
            }

            if (isKeyPressed(GLFW_KEY_S)) {
                setPlayerBackward(true);
            } else {
                setPlayerBackward(false);
            }
            if (isKeyPressed(GLFW_KEY_A)) {
                setPlayerLeft(true);
            } else {
                setPlayerLeft(false);
            }
            if (isKeyPressed(GLFW_KEY_D)) {
                setPlayerRight(true);
            } else {
                setPlayerRight(false);
            }

            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT)) { //sneaking
                setPlayerSneaking(true);
            } else {
                setPlayerSneaking(false);
            }

            if (isKeyPressed(GLFW_KEY_SPACE)) {
                setPlayerJump(true);
            } else {
                setPlayerJump(false);
            }
        }


        if (isKeyPressed(GLFW_KEY_G)) {
            clearItems();
        }

        if (isKeyPressed(GLFW_KEY_R)) {
            setPlayerRunning(true);
        } else {
            setPlayerRunning(false);
        }


        /*
        if (isKeyPressed(GLFW_KEY_R)) {
            if (!rButtonPushed) {
                rButtonPushed = true;
//                resetInventory();
                //generateRandomInventory();
            }
        } else if (!isKeyPressed(GLFW_KEY_R)){
            rButtonPushed = false;
        }
         */


        if (isKeyPressed(GLFW_KEY_Q)) {
            if (!qButtonPushed) {
                qButtonPushed = true;
                throwItem();
            }
        } else if (!isKeyPressed(GLFW_KEY_Q)){
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
        if (isKeyPressed(GLFW_KEY_E) && !isPaused()) {
            if (!eButtonPushed) {
                eButtonPushed = true;
                togglePlayerInventory();
                toggleMouseLock();
                resetPlayerInputs();
                emptyMouseInventory();
            }
        } else if (!isKeyPressed(GLFW_KEY_E)){
            eButtonPushed = false;
        }


        //spawn human mob
        if (isKeyPressed(GLFW_KEY_T)) {
            if (!tButtonPushed) {
                tButtonPushed = true;
                spawnMob("human", new Vector3d(getPlayerPos()), new Vector3f(0,0,0));
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
}
