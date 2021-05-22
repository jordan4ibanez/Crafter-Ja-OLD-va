package game.crafting;

import game.item.Item;
import org.joml.Vector2d;
import org.joml.Vector3d;
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

            collideMouseWithInventory(getMainInventory());
            collideMouseWithInventory(getSmallCraftInventory());
            collideMouseWithInventory(getOutputInventory());
            collideMouseWithInventory(getArmorInventory());

        }
    }

    public static Vector3f getPlayerHudRotation(){
        return playerRot;
    }



    public static void collideMouseWithInventory(InventoryObject inventory){
        Vector2d startingPoint = inventory.getPosition();

        float windowScale = getWindowScale();

        //need to create new object or the mouse position gets messed up
        Vector2d mousePos = new Vector2d(getMousePos());

        //work from the center
        mousePos.x -= (getWindowSize().x/2f);
        mousePos.y -= (getWindowSize().y/2f);

        //invert mouse
        mousePos.y *= -1;

        //this is the size of the actual slots
        //it also makes the default spacing of (0)
        //they bunch up right next to each other with 0
        double scale = windowScale/10.5d;

        //this is the spacing between the slots
        double spacing = windowScale / 75d;

        Vector2d offset = new Vector2d((double)inventory.getSize().x/2d,(double)inventory.getSize().y/2d);


        double yProgram;

        if (inventory.isMainInventory()) {

            for (int x = 0; x < inventory.getSize().x; x++) {
                for (int y = 0; y < inventory.getSize().y; y++) {

                    //this is a quick and dirty hack to implement
                    //the space between the hotbar and rest of inventory
                    //on the main inventory
                    if (y == 0){
                        yProgram = 0.2d;
                    } else {
                        yProgram = 0;
                    }
                    
                    Vector2d slotPosition = new Vector2d(((double) x + 0.5d - offset.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.5d + startingPoint.y + offset.y + yProgram) * (scale + spacing));
                    //scale is the size

                    //found selection break out of loop
                    if (mousePos.x >= slotPosition.x - (scale / 2) && mousePos.x <= slotPosition.x + (scale / 2) && mousePos.y >= slotPosition.y - (scale / 2) && mousePos.y <= slotPosition.y + (scale / 2)) {
                        inventory.setSelection(x, y);
                        return;
                    }
                }
            }
        } else {
            for (int x = 0; x < inventory.getSize().x; x++) {
                for (int y = 0; y < inventory.getSize().y; y++) {

                    Vector2d slotPosition = new Vector2d(((double) x + 0.5d - offset.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.5d + startingPoint.y + offset.y) * (scale + spacing));

                    //scale is the size

                    //found selection break out of loop
                    if (mousePos.x >= slotPosition.x - (scale / 2) && mousePos.x <= slotPosition.x + (scale / 2) && mousePos.y >= slotPosition.y - (scale / 2) && mousePos.y <= slotPosition.y + (scale / 2)) {
                        inventory.setSelection(x, y);
                        return;
                    }
                }
            }
        }

        //fail state
        inventory.setSelection(-1,-1);
    }

}
