package game.crafting;

import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import org.joml.Vector2d;
import org.joml.Vector3f;

import static engine.MouseInput.*;
import static engine.Window.getWindowHeight;
import static engine.Window.getWindowWidth;
import static engine.network.Networking.getIfMultiplayer;
import static engine.render.GameRenderer.*;
import static game.crafting.CraftRecipes.recipeScan;
import static game.crafting.Inventory.*;
import static game.crafting.InventoryObject.*;
import static game.player.Player.getCurrentInventorySelection;
import static game.player.Player.resetPlayerInputs;
import static game.player.WieldHand.resetWieldHandSetupTrigger;

final public class InventoryLogic {

    private static String oldSelection;

    private static final Vector3f playerRot = new Vector3f(0,0,0);

    private static boolean leftMouseButtonPushed = false;
    private static boolean leftMouseButtonWasPushed = false;
    private static boolean rightMouseButtonPushed = false;
    private static boolean rightMouseButtonWasPushed = false;

    private static final String[] inventoryArray = {
            "main", "smallCraft", "bigCraft", "output", "armor"
    };


    public static void inventoryMenuOnTick(){

        String itemWielding = getItemInInventory("main",getCurrentInventorySelection(), 0);

        if ((itemWielding != null && oldSelection == null) || (itemWielding == null && oldSelection != null) || (itemWielding != null && oldSelection != null && !itemWielding.equals(oldSelection))) {
            resetWieldHandSetupTrigger();
            oldSelection = itemWielding;
        }

        if (isPlayerInventoryOpen()) {

            //begin player in inventory thing
            //new scope because lazy
            {
                float windowScale = getWindowScale();

                double basePlayerPosX = -(windowScale / 3.75d);
                double basePlayerPosY = (windowScale / 2.8d);

                double mousePosX = getMousePosX();
                double mousePosY = getMousePosY();

                double windowSizeX = getWindowSizeX();
                double windowSizeY = getWindowSizeY();

                //limiters
                if (mousePosX > windowSizeX){
                    mousePosX = windowSizeX;
                } else if (mousePosX < 0){
                    mousePosX = 0;
                }

                if (mousePosY > windowSizeY){
                    mousePosY = windowSizeY;
                } else if (mousePosY < 0){
                    mousePosY = 0;
                }

                float rotationY = (float)((mousePosX - (getWindowWidth()/2f)) - basePlayerPosX) / (windowScale * 1.2f);
                rotationY *= 40f;
                playerRot.y = rotationY;


                float rotationX = (float)((mousePosY - (getWindowHeight()/2f)) + (basePlayerPosY /2f)) / (windowScale * 1.2f);
                rotationX *= 40f;
                playerRot.x = rotationX;
            }

            collideMouseWithInventory("main");

            if (isAtCraftingBench()){
                collideMouseWithInventory("bigCraft");
            } else {
                collideMouseWithInventory("smallCraft");
            }

            collideMouseWithInventory("output");
            collideMouseWithInventory("armor");

            String foundInventory = null;

            boolean inventoryUpdate = false;

            //normal left click moving items
            if (isLeftButtonPressed() && !leftMouseButtonPushed && !leftMouseButtonWasPushed) {

                leftMouseButtonPushed = true;

                for (String inventory : inventoryArray) {

                    int selectionX = getInventorySelectionX(inventory);
                    int selectionY = getInventorySelectionY(inventory);

                    if (!inventory.equals("output")) {
                        if (selectionX >= 0 && selectionY >= 0) {
                            String thisItem = getItemInInventory(inventory, selectionX, selectionY);
                            int thisCount = getInventoryCount(inventory, selectionX, selectionY);
                            String mouseItem = getMouseInventory();
                            int mouseCount = getMouseInventoryCount();

                            //pick up item
                            if (mouseItem == null && thisItem != null) {
                                setMouseInventory(thisItem, thisCount);
                                setInventoryItem(inventory, selectionX, selectionY, null, 0);
                                foundInventory = inventory;
                                inventoryUpdate = true;
                                //add to blank space
                            } else if (mouseItem != null && thisItem == null) {
                                setInventoryItem(inventory, selectionX, selectionY, mouseItem, mouseCount);
                                setMouseInventory(null, 0);
                                foundInventory = inventory;
                                inventoryUpdate = true;
                                //two stacks to compare
                            } else if (mouseItem != null) {
                                //try to add into the inventory stack or swap
                                if (thisItem.equals(mouseItem)) {

                                    //swap item

                                    //accommodate if player's find a way to spoof
                                    //higher than 64 item stacks
                                    if (thisCount >= 64) {
                                        setMouseInventory(thisItem, thisCount);
                                        setInventoryItem(inventory, selectionX, selectionY, mouseItem, mouseCount);
                                        foundInventory = inventory;
                                        inventoryUpdate = true;
                                        //add into stack
                                    } else {

                                        int adder = mouseCount + thisCount;

                                        //compare, leave remainder
                                        if (adder > 64) {
                                            mouseCount = adder - 64;
                                            thisCount = 64;
                                            foundInventory = inventory;
                                            inventoryUpdate = true;
                                            //dump full stack in

                                            setMouseCount(mouseCount);
                                            setInventoryCount(inventory, selectionX, selectionY, thisCount);
                                        } else {

                                            thisCount = adder;
                                            setMouseInventory(null, 0);
                                            foundInventory = inventory;
                                            inventoryUpdate = true;
                                            setInventoryCount(inventory, selectionX, selectionY, thisCount);
                                        }
                                    }
                                    //swap mouse item with inventory item
                                } else {
                                    setMouseInventory(thisItem, thisCount);
                                    setInventoryItem(inventory, selectionX, selectionY, mouseItem, mouseCount);
                                    foundInventory = inventory;
                                    inventoryUpdate = true;
                                }
                            }
                        }
                        //handle craft output
                    } else {
                        if (selectionX >= 0 && selectionY >= 0) {
                            String thisItem = getItemInInventory(inventory, selectionX, selectionY);
                            int thisCount = getInventoryCount(inventory, selectionX, selectionY);
                            String mouseItem = getMouseInventory();
                            int mouseCount = getMouseInventoryCount();

                            //new item in mouse inventory
                            if (mouseItem == null && thisItem != null) {
                                setMouseInventory(thisItem, thisCount);
                                setInventoryItem("output", 0, 0, null, 0);
                                updateCraftingGrid();
                                inventoryUpdate = true;
                                //add to existing stack
                            } else if (mouseItem != null && thisItem != null && mouseItem.equals(thisItem)) {
                                int adder = mouseCount + thisCount;

                                if (adder <= 64) {
                                    mouseCount = adder;
                                    setInventoryItem("output", 0, 0, null, 0);
                                    updateCraftingGrid();
                                    inventoryUpdate = true;

                                    setMouseCount(mouseCount);
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

                for (String inventory : inventoryArray) {

                    int selectionX = getInventorySelectionX(inventory);
                    int selectionY = getInventorySelectionY(inventory);

                    if (selectionX >= 0 && selectionY >= 0) {

                        //don't allow anything to happen to the output
                        if (!inventory.equals("output")) {
                            String mouseItem = getMouseInventory();
                            int mouseCount = getMouseInventoryCount();
                            String thisItem = getItemInInventory(inventory, selectionX, selectionY);
                            int thisCount = getInventoryCount(inventory, selectionX,selectionY);

                            //mouse splits stack
                            if (mouseItem == null && thisItem != null) {
                                int subtraction = thisCount / 2;
                                int remainder = thisCount / 2;

                                //solve for odd numbered stacks
                                if (subtraction + remainder < thisCount) {
                                    subtraction += 1;
                                }

                                if (remainder == 0) {
                                    setInventoryItem(inventory,selectionX, selectionY, null, 0);
                                } else {
                                    thisCount = remainder;
                                    setInventoryItem(inventory, selectionX, selectionY, thisItem, thisCount);
                                }

                                foundInventory = inventory;
                                setMouseInventory(thisItem, subtraction);
                                inventoryUpdate = true;
                            }
                            //mouse single place into existing stack
                            else if (thisItem != null) {
                                //single add to existing stack
                                if (mouseItem.equals(thisItem) && thisCount < 64) {

                                    int mouseItemStack = mouseCount;
                                    mouseItemStack -= 1;
                                    thisCount++;
                                    setInventoryCount(inventory, selectionX, selectionY, thisCount);
                                    if (mouseItemStack <= 0) {
                                        setMouseInventory(null, 0);
                                    } else {
                                        mouseCount = mouseItemStack;
                                        setMouseCount(mouseCount);
                                    }
                                    inventoryUpdate = true;
                                    foundInventory = inventory;
                                }
                                //mouse creating initial stack
                            } else if (mouseItem != null){
                                //single add to non-existing

                                int mouseItemStack = mouseCount;

                                mouseItemStack -= 1;

                                setInventoryItem(inventory, selectionX, selectionY, mouseItem, 1);

                                if (mouseItemStack <= 0) {
                                    setMouseInventory(null, 0);
                                } else {
                                    mouseCount = mouseItemStack;
                                    setMouseInventory(mouseItem, mouseCount);
                                }
                                inventoryUpdate = true;
                                foundInventory = inventory;
                            }
                        }
                    }
                }
            } else if (!isRightButtonPressed()) {
                rightMouseButtonPushed = false;
            }

            if (foundInventory != null){
                if (foundInventory.equals("smallCraft") || foundInventory.equals("bigCraft")){

                    ObjectIntImmutablePair<String> newItems;
                    //small craft recipe scan
                    if (foundInventory.equals("smallCraft")){
                        newItems = recipeScan("smallCraft");
                        //large craft recipe scan
                    } else {
                        newItems = recipeScan("bigCraft");
                    }

                    if (newItems != null){
                        //addItemToInventory(newItems.output);
                        setInventoryItem("output", 0,0, newItems.left(), newItems.rightInt());
                    //clear output inventory
                    } else {
                        setInventoryItem("output", 0, 0, null, 0);
                    }
                }
            }

            //update the server inventory
            if (inventoryUpdate && getIfMultiplayer()){
                System.out.println("WHOOPS GOTTA FIX THIS TOO");
                //sendServerUpdatedInventory();
            }

            leftMouseButtonWasPushed = leftMouseButtonPushed;
            rightMouseButtonWasPushed = rightMouseButtonPushed;
        }
    }

    public static void updateCraftingGrid(){

        ObjectIntImmutablePair<String> newItems;

        if (isAtCraftingBench()){
            String[][] thisInventory = getInventoryAsArray("bigCraft");
            int[][] thisCount = getInventoryCountAsArray("bigCraft");

            for (int x = 0; x < thisInventory.length; x++){
                for (int y = 0; y < thisInventory[0].length; y++){
                    if (thisInventory[y][x] != null){
                        int count = thisCount[y][x];
                        count--;
                        if (count <= 0){
                            setInventoryItem("bigCraft", x, y,null, 0);
                        } else {
                            setInventoryCount("bigCraft", x, y, count);
                        }
                    }
                }
            }

            newItems = recipeScan("bigCraft");
        } else {

            String[][] thisInventory = getInventoryAsArray("smallCraft");
            int[][] thisCount = getInventoryCountAsArray("smallCraft");


            for (int x = 0; x < thisInventory.length; x++){
                for (int y = 0; y < thisInventory[0].length; y++){
                    if (thisInventory[y][x] != null){
                        int count = thisCount[y][x];
                        count--;
                        if (count <= 0){
                            setInventoryItem("smallCraft", x, y,null, 0);
                        } else {
                            setInventoryCount("smallCraft", x, y, count);
                        }
                    }
                }
            }
            newItems = recipeScan("smallCraft");
        }

        if (newItems != null){
            //addItemToInventory(newItems.output);
            setInventoryItem("output", 0,0, newItems.left(), newItems.rightInt());
            //clear output inventory
        } else {
            setInventoryItem("output", 0, 0, null, 0);
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

    //mutable - be careful with this
    public static Vector3f getPlayerHudRotation(){
        return playerRot;
    }
    //immutable
    public static float getPlayerHudRotationX(){
        return playerRot.x;
    }
    //immutable
    public static float getPlayerHudRotationY(){
        return playerRot.y;
    }
    //immutable
    public static float getPlayerHudRotationZ(){
        return playerRot.z;
    }



    public static void collideMouseWithInventory(String inventoryName){
        double startingPointX = getInventoryPosX(inventoryName);
        double startingPointY = getInventoryPosY(inventoryName);

        float windowScale = getWindowScale();

        double mousePosX = getMousePosX();
        double mousePosY = getMousePosY();

        //work from the center
        mousePosX -= (getWindowSizeX()/2f);
        mousePosY -= (getWindowSizeY()/2f);

        //invert mouse
        mousePosY *= -1;

        //this is the size of the actual slots
        //it also makes the default spacing of (0)
        //they bunch up right next to each other with 0
        double scale = windowScale/10.5d;

        //this is the spacing between the slots
        double spacing = windowScale / 75d;

        int inventorySizeX = getInventorySizeX(inventoryName);
        int inventorySizeY = getInventorySizeY(inventoryName);


        Vector2d offset = new Vector2d((double)inventorySizeX/2d,(double)inventorySizeY/2d);


        double yProgram;

        if (inventoryName.equals("main")) {

            for (int x = 0; x < inventorySizeX; x++) {
                for (int y = 0; y < inventorySizeY; y++) {

                    //this is a quick and dirty hack to implement
                    //the space between the hotbar and rest of inventory
                    //on the main inventory
                    if (y == 0){
                        yProgram = 0.2d;
                    } else {
                        yProgram = 0;
                    }

                    Vector2d slotPosition = new Vector2d(((double) x + 0.5d - offset.x + startingPointX) * (scale + spacing), ((y * -1d) - 0.5d + startingPointY + offset.y + yProgram) * (scale + spacing));
                    //scale is the size

                    //found selection break out of loop
                    if (mousePosX >= slotPosition.x - (scale / 2) && mousePosX <= slotPosition.x + (scale / 2) && mousePosY >= slotPosition.y - (scale / 2) && mousePosY <= slotPosition.y + (scale / 2)) {
                        setInventorySelection(inventoryName, x, y);
                        return;
                    }
                }
            }
        } else {
            for (int x = 0; x < inventorySizeX; x++) {
                for (int y = 0; y < inventorySizeY; y++) {

                    //scale is the size
                    double slotPosX = ((double) x + 0.5d - offset.x + startingPointX) * (scale + spacing);
                    double slotPosY = ((y * -1d) - 0.5d + startingPointY + offset.y) * (scale + spacing);

                    //found selection break out of loop
                    if (mousePosX >= slotPosX - (scale / 2) && mousePosX <= slotPosX + (scale / 2) && mousePosY >= slotPosY - (scale / 2) && mousePosY <= slotPosY + (scale / 2)) {
                        setInventorySelection(inventoryName, x, y);
                        return;
                    }
                }
            }
        }

        //fail state
        setInventorySelection(inventoryName, -1, -1);
    }

}
