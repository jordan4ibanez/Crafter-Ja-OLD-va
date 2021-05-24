package game.crafting;

import game.item.Item;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector3f;

import static engine.MouseInput.*;
import static engine.Window.getWindowHeight;
import static engine.Window.getWindowWidth;
import static engine.render.GameRenderer.getWindowScale;
import static engine.render.GameRenderer.getWindowSize;
import static game.crafting.CraftRecipes.recipeScan;
import static game.crafting.Inventory.*;
import static game.player.Player.*;

public class InventoryLogic {

    private static String oldSelection;

    private static final Vector3f playerRot = new Vector3f(0,0,0);

    private static boolean leftMouseButtonPushed = false;
    private static boolean leftMouseButtonWasPushed = false;
    private static boolean rightMouseButtonPushed = false;
    private static boolean rightMouseButtonWasPushed = false;


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
            if (isAtCraftingBench()){
                collideMouseWithInventory(getBigCraftInventory());
            } else {
                collideMouseWithInventory(getSmallCraftInventory());
            }
            collideMouseWithInventory(getOutputInventory());
            collideMouseWithInventory(getArmorInventory());


            String foundInventory = null;

            //normal left click moving items
            if (isLeftButtonPressed() && !leftMouseButtonPushed && !leftMouseButtonWasPushed) {
                leftMouseButtonPushed = true;
                //this might cause a memory leak todo: test if leaking
                InventoryObject[] tempInventoryObjectArray = new InventoryObject[]{
                        getMainInventory(), getSmallCraftInventory(), getBigCraftInventory(), getOutputInventory(), getArmorInventory()
                };

                for (InventoryObject inventory : tempInventoryObjectArray) {

                    //handle output inventory separately
                    if (!inventory.name.equals("output")) {
                        Vector2i selection = inventory.getSelection();
                        if (selection.x >= 0 && selection.y >= 0) {
                            Item thisItem = inventory.get(selection.x, selection.y);
                            Item mouseItem = getMouseInventory();

                            //pick up item
                            if (mouseItem == null && thisItem != null) {
                                setMouseInventory(thisItem);
                                inventory.set(selection.x, selection.y, null);
                                foundInventory = inventory.name;
                                //add to blank space
                            } else if (mouseItem != null && thisItem == null) {
                                inventory.set(selection.x, selection.y, mouseItem);
                                setMouseInventory(null);
                                foundInventory = inventory.name;
                                //two stacks to compare
                            } else if (mouseItem != null && thisItem != null) {
                                //try to add into the inventory stack or swap
                                if (thisItem.name.equals(mouseItem.name)) {

                                    //swap item

                                    //accommodate if player's find a way to spoof
                                    //higher than 64 item stacks
                                    if (thisItem.stack >= 64) {
                                        setMouseInventory(thisItem);
                                        inventory.set(selection.x, selection.y, mouseItem);
                                        foundInventory = inventory.name;
                                        //add into stack
                                    } else {
                                        int mouseStack = mouseItem.stack;
                                        int thisItemStack = thisItem.stack;

                                        int adder = mouseStack + thisItemStack;

                                        //compare, leave remainder
                                        if (adder > 64) {
                                            mouseItem.stack = adder - 64;
                                            thisItem.stack = 64;
                                            foundInventory = inventory.name;
                                            //dump full stack in
                                        } else {
                                            thisItem.stack = adder;
                                            setMouseInventory(null);
                                            foundInventory = inventory.name;
                                        }
                                    }
                                    //swap mouse item with inventory item
                                } else {
                                    setMouseInventory(thisItem);
                                    inventory.set(selection.x, selection.y, mouseItem);
                                    foundInventory = inventory.name;
                                }
                            }
                        }
                    //handle craft output
                    } else {
                        Vector2i selection = inventory.getSelection();
                        if (selection.x >= 0 && selection.y >= 0) {
                            Item thisItem = inventory.get(selection.x, selection.y);
                            Item mouseItem = getMouseInventory();

                            //new item in mouse inventory
                            if (mouseItem == null && thisItem != null){
                                setMouseInventory(thisItem);
                                setOutputInventory(null);
                                updateCraftingGrid();
                            //add to existing stack
                            } else if (mouseItem != null && thisItem != null && mouseItem.name.equals(thisItem.name)){
                                int first = mouseItem.stack;
                                int second = thisItem.stack;
                                int adder = first + second;

                                if (adder <= 64){
                                    mouseItem.stack = adder;
                                    setOutputInventory(null);
                                    updateCraftingGrid();
                                }
                            }
                        }
                    }
                }
            } else if (!isLeftButtonPressed()) {
                leftMouseButtonPushed = false;
            }

            //right click moving items/splitting stack/dropping single items
            if (isRightButtonPressed() && !rightMouseButtonPushed && !rightMouseButtonWasPushed) {
                rightMouseButtonPushed = true;
                //this might cause a memory leak todo: test if leaking
                InventoryObject[] tempInventoryObjectArray = new InventoryObject[]{
                        getMainInventory(), getSmallCraftInventory(), getBigCraftInventory(), getOutputInventory(), getArmorInventory()
                };

                for (InventoryObject inventory : tempInventoryObjectArray) {
                    Vector2i selection = inventory.getSelection();
                    if (selection.x >= 0 && selection.y >= 0) {

                        //don't allow anything to happen to the output
                        if (!inventory.name.equals("output")) {
                            Item mouseItem = getMouseInventory();
                            Item thisItem = inventory.get(selection.x, selection.y);

                            //mouse splits stack
                            if (mouseItem == null && thisItem != null) {
                                int first = thisItem.stack;
                                int subtraction = thisItem.stack / 2;
                                int remainder = thisItem.stack / 2;

                                //solve for odd numbered stacks
                                if (subtraction + remainder < first) {
                                    subtraction += 1;
                                }

                                if (remainder == 0) {
                                    inventory.set(selection.x, selection.y, null);
                                } else {
                                    thisItem.stack = remainder;
                                    inventory.set(selection.x, selection.y, thisItem);
                                }

                                foundInventory = inventory.name;
                                Item newMouseItem = new Item(thisItem);
                                newMouseItem.stack = subtraction;
                                setMouseInventory(newMouseItem);
                            }
                            //mouse single place into existing stack
                            else if (thisItem != null) {
                                //single add to existing stack
                                if (mouseItem.name.equals(thisItem.name) && thisItem.stack < 64) {
                                    int mouseItemStack = mouseItem.stack;
                                    mouseItemStack -= 1;
                                    thisItem.stack++;
                                    if (mouseItemStack <= 0) {
                                        setMouseInventory(null);
                                    } else {
                                        mouseItem.stack = mouseItemStack;
                                        setMouseInventory(mouseItem);
                                    }
                                    foundInventory = inventory.name;
                                }
                                //mouse creating initial stack
                            } else if (mouseItem != null){
                                //single add to non-existing

                                int mouseItemStack = mouseItem.stack;
                                mouseItemStack -= 1;


                                Item creationItem = new Item(mouseItem);
                                creationItem.stack = 1;

                                inventory.set(selection.x, selection.y, creationItem);

                                if (mouseItemStack <= 0) {
                                    setMouseInventory(null);
                                } else {
                                    mouseItem.stack = mouseItemStack;
                                    setMouseInventory(mouseItem);
                                }
                                foundInventory = inventory.name;
                            }
                        }
                    }
                }
            } else if (!isRightButtonPressed()) {
                rightMouseButtonPushed = false;
            }
            if (foundInventory != null){
                if (foundInventory.equals("smallCraft") || foundInventory.equals("bigCraft")){

                    CraftRecipeObject newItems;
                    //small craft recipe scan
                    if (foundInventory.equals("smallCraft")){
                        newItems = recipeScan(getSmallCraftInventory());
                        //large craft recipe scan
                    } else {
                        newItems = recipeScan(getBigCraftInventory());
                    }

                    if (newItems != null){
                        //addItemToInventory(newItems.output);
                        Item outputItem = new Item(newItems.output, newItems.amountOutput);
                        setOutputInventory(outputItem);
                    //clear output inventory
                    } else {
                        setOutputInventory(null);
                    }
                }
            }

            leftMouseButtonWasPushed = leftMouseButtonPushed;
            rightMouseButtonWasPushed = rightMouseButtonPushed;
        }
    }

    public static void updateCraftingGrid(){

        CraftRecipeObject newItems;

        if (isAtCraftingBench()){
            InventoryObject inventory = getBigCraftInventory();

            for (int x = 0; x < inventory.size.x; x++){
                for (int y = 0; y < inventory.size.y; y++){
                    if (inventory.get(x,y) != null){
                        inventory.get(x,y).stack--;
                        if (inventory.get(x,y).stack <= 0){
                            inventory.set(x,y,null);
                        }
                    }
                }
            }
            newItems = recipeScan(getBigCraftInventory());
        } else {
            InventoryObject inventory = getSmallCraftInventory();
            for (int x = 0; x < inventory.size.x; x++){
                for (int y = 0; y < inventory.size.y; y++){
                    if (inventory.get(x,y) != null){
                        inventory.get(x,y).stack--;
                        if (inventory.get(x,y).stack <= 0){
                            inventory.set(x,y,null);
                        }
                    }
                }
            }
            newItems = recipeScan(getSmallCraftInventory());
        }

        if (newItems != null){
            //addItemToInventory(newItems.output);
            Item outputItem = new Item(newItems.output, newItems.amountOutput);
            setOutputInventory(outputItem);
            //clear output inventory
        } else {
            setOutputInventory(null);
        }

    }

    public static void openCraftingInventory(boolean isCraftingTable) {
        //inventory closed, open it
        if (!isPlayerInventoryOpen()){
            setMouseLocked(false);
            resetPlayerInputs();
            setIsAtCraftingBench(isCraftingTable);
            setPlayerInventoryIsOpen(true);
            resetPlayerInputs();
        }

    }

    public static void closeCraftingInventory(){
        //inventory open, close it
        if (isPlayerInventoryOpen()){
            setMouseLocked(true);
            resetPlayerInputs();
            setPlayerInventoryIsOpen(false);
            clearOutCraftInventories();
            emptyMouseInventory();
            updateCraftingGrid();
            resetPlayerInputs();
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
