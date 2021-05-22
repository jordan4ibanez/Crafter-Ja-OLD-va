package game.crafting;

import game.item.Item;
import org.joml.Vector2d;
import org.joml.Vector3f;

import static engine.MouseInput.getMousePos;
import static engine.MouseInput.isLeftButtonPressed;
import static engine.Window.getWindowHeight;
import static engine.Window.getWindowWidth;
import static engine.render.GameRenderer.getWindowScale;
import static engine.render.GameRenderer.getWindowSize;
import static game.crafting.Inventory.*;
import static game.player.Player.getCurrentInventorySelection;
import static game.player.Player.resetWieldHandSetupTrigger;

public class InventoryLogic {

    private static boolean mouseButtonPushed = false;
    private static boolean mouseButtonWasPushed = false;

    private static String oldSelection;

    private static final Vector3f playerRot = new Vector3f(0,0,0);



    public static void inventoryMenuOnTick(){

        if (!getItemInInventorySlotName(getCurrentInventorySelection(), 0).equals(oldSelection)) {
            resetWieldHandSetupTrigger();
            oldSelection = getItemInInventorySlotName(getCurrentInventorySelection(), 0);
        }

        if (isPlayerInventoryOpen()) {

            //begin player in inventory thing
            //new scope because lazy
            {
                float windowScale = getWindowScale();
                Vector2d basePlayerPos = new Vector2d(-(windowScale / 3.75d), (windowScale / 2.8d));
                Vector2d mousePos = getMousePos();

                //limiters
                if (mousePos.x > getWindowSize().x){
                    mousePos.x = getWindowSize().x;
                } else if (mousePos.x < 0){
                    mousePos.x = 0;
                }

                if (mousePos.y > getWindowSize().y){
                    mousePos.y = getWindowSize().y;
                } else if (mousePos.y < 0){
                    mousePos.y = 0;
                }

                float rotationY = (float)((mousePos.x - (getWindowWidth()/2f)) - basePlayerPos.x) / (windowScale * 1.2f);
                rotationY *= 40f;
                playerRot.y = rotationY;


                float rotationX = (float)((mousePos.y - (getWindowHeight()/2f)) + (basePlayerPos.y /2f)) / (windowScale * 1.2f);
                rotationX *= 40f;
                playerRot.x = rotationX;
            }


            /*
            int[] invSelection = new int[0];
            {
                if (isLeftButtonPressed()) {
                    if (!mouseButtonPushed) {
                        mouseButtonPushed = true;

                        if (getMouseInventory() == null) {
                            setMouseInventory(getItemInInventorySlot(invSelection[0], invSelection[1]));

                            removeStackFromInventory(invSelection[0], invSelection[1]);
                        } else {
                            Item bufferItemMouse = getMouseInventory();
                            Item bufferItemInv  = getItemInInventorySlot(invSelection[0], invSelection[1]);
                            setItemInInventory(invSelection[0], invSelection[1], bufferItemMouse.name, bufferItemMouse.stack);
                            setMouseInventory(bufferItemInv);
                        }

                    }
                } else {
                    mouseButtonPushed = false;
                }
            }

            //need to create new object or the mouse position gets messed up
            Vector2d mousePos = new Vector2d(getMousePos());

            //work from the center
            mousePos.x -= (getWindowSize().x/2f);
            mousePos.y -= (getWindowSize().y/2f);
            //invert the Y position to follow rendering coordinate system
            mousePos.y *= -1f;

            //collision detect the lower inventory
            for (int x = 1; x <= 9; x++) {
                for (int y = -2; y > -5; y--) {
                    if (
                            mousePos.x > ((x - 5) * (getWindowScale() / 9.5f)) - ((getWindowScale()/10.5f) / 2f) && mousePos.x < ((x - 5) * (getWindowScale() / 9.5f)) + ((getWindowScale()/10.5f) / 2f) && //x axis
                                    mousePos.y > ((y+0.3f) * (getWindowScale() / 9.5f)) - ((getWindowScale()/10.5f) / 2f) && mousePos.y < ((y+0.3f) * (getWindowScale() / 9.5f)) + ((getWindowScale()/10.5f) / 2f) //y axis
                    ){
                        invSelection[0] = (x-1);
                        invSelection[1] = ((y*-1) - 1);
                        return;
                    }
                }
            }

            //collision detect the inventory hotbar (upper part)
            for (int x = 1; x <= 9; x++) {
                if (
                        mousePos.x > ((x - 5) * (getWindowScale() / 9.5f)) - ((getWindowScale()/10.5f) / 2f) && mousePos.x < ((x - 5) * (getWindowScale() / 9.5f)) + ((getWindowScale()/10.5f) / 2f) && //x axis
                                mousePos.y > (-0.5f * (getWindowScale() / 9.5f)) - ((getWindowScale()/10.5f) / 2f) && mousePos.y < (-0.5f * (getWindowScale() / 9.5f)) + ((getWindowScale()/10.5f) / 2f) //y axis
                ){
                    invSelection[0] = (x-1);
                    invSelection[1] = 0;
                    return;
                }
            }

             */

        }
    }

    public static Vector3f getPlayerHudRotation(){
        return playerRot;
    }
}
