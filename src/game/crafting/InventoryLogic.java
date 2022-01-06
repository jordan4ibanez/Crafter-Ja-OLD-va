package game.crafting;

import org.joml.Vector2d;
import org.joml.Vector3f;

import static engine.MouseInput.*;
import static engine.Window.getWindowHeight;
import static engine.Window.getWindowWidth;
import static engine.network.Networking.getIfMultiplayer;
import static engine.render.GameRenderer.getWindowScale;
import static engine.render.GameRenderer.getWindowSize;
import static game.crafting.CraftRecipes.recipeScan;
import static game.crafting.Inventory.*;
import static game.player.Player.getCurrentInventorySelection;
import static game.player.Player.resetPlayerInputs;
import static game.player.WieldHand.resetWieldHandSetupTrigger;

public class InventoryLogic {

    private static String oldSelection;

    private static final Vector3f playerRot = new Vector3f(0,0,0);

    private static boolean leftMouseButtonPushed = false;
    private static boolean leftMouseButtonWasPushed = false;
    private static boolean rightMouseButtonPushed = false;
    private static boolean rightMouseButtonWasPushed = false;


    public static void inventoryMenuOnTick(){

        String itemWielding = getItemInInventorySlotName(getCurrentInventorySelection(), 0);

        if ((itemWielding != null && oldSelection == null) || (itemWielding == null && oldSelection != null) || (itemWielding != null && oldSelection != null && !itemWielding.equals(oldSelection))) {
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

            boolean inventoryUpdate = false;

            //normal left click moving items
            if (isLeftButtonPressed() && !leftMouseButtonPushed && !leftMouseButtonWasPushed) {
                leftMouseButtonPushed = true;
                //this might cause a memory leak todo: test if leaking
                InventoryObject[] tempInventoryObjectArray = new InventoryObject[]{
                        getMainInventory(), getSmallCraftInventory(), getBigCraftInventory(), getOutputInventory(), getArmorInventory()
                };

                for (InventoryObject inventory : tempInventoryObjectArray) {

                    //handle output inventory separately
                    //Vector2i selection = inventory.getSelection();
                    int selectionX = inventory.getSelectionX();
                    int selectionY = inventory.getSelectionY();

                    if (!inventory.getName().equals("output")) {
                        if (selectionX >= 0 && selectionY >= 0) {

                            String thisItem = inventory.getItem(selectionX, selectionY);
                            int thisCount = inventory.getCount(selectionX, selectionY);
                            String mouseItem = getMouseInventory();
                            int mouseCount = getMouseInventoryCount();

                            //pick up item
                            if (mouseItem == null && thisItem != null) {
                                setMouseInventory(thisItem, thisCount);
                                inventory.set(selectionX, selectionY, null, 0);
                                foundInventory = inventory.getName();
                                inventoryUpdate = true;
                                //add to blank space
                            } else if (mouseItem != null && thisItem == null) {
                                inventory.set(selectionX, selectionY, mouseItem, mouseCount);
                                setMouseInventory(null, 0);
                                foundInventory = inventory.getName();
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
                                        inventory.set(selectionX, selectionY, mouseItem, mouseCount);
                                        foundInventory = inventory.getName();
                                        inventoryUpdate = true;
                                        //add into stack
                                    } else {

                                        int adder = mouseCount + thisCount;

                                        //compare, leave remainder
                                        if (adder > 64) {
                                            mouseCount = adder - 64;
                                            thisCount = 64;
                                            foundInventory = inventory.getName();
                                            inventoryUpdate = true;
                                            //dump full stack in

                                            setMouseCount(mouseCount);
                                            inventory.setCount(selectionX, selectionY, thisCount);
                                        } else {

                                            thisCount = adder;
                                            setMouseInventory(null, 0);
                                            foundInventory = inventory.getName();
                                            inventoryUpdate = true;
                                            inventory.setCount(selectionX, selectionY, thisCount);
                                        }
                                    }
                                    //swap mouse item with inventory item
                                } else {
                                    setMouseInventory(thisItem, thisCount);
                                    inventory.set(selectionX, selectionY, mouseItem, mouseCount);
                                    foundInventory = inventory.getName();
                                    inventoryUpdate = true;
                                }
                            }
                        }
                    //handle craft output
                    } else {
                        if (selectionX >= 0 && selectionY >= 0) {
                            String thisItem = inventory.getItem(selectionX, selectionY);
                            int thisCount = inventory.getCount(selectionX, selectionY);
                            String mouseItem = getMouseInventory();
                            int mouseCount = getMouseInventoryCount();

                            //new item in mouse inventory
                            if (mouseItem == null && thisItem != null){
                                setMouseInventory(thisItem, thisCount);
                                setOutputInventory(null, 0);
                                updateCraftingGrid();
                                inventoryUpdate = true;
                            //add to existing stack
                            } else if (mouseItem != null && thisItem != null && mouseItem.equals(thisItem)){
                                int adder = mouseCount + thisCount;

                                if (adder <= 64){
                                    mouseCount = adder;
                                    setOutputInventory(null, 0);
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
                //this might cause a memory leak todo: test if leaking
                InventoryObject[] tempInventoryObjectArray = new InventoryObject[]{
                        getMainInventory(), getSmallCraftInventory(), getBigCraftInventory(), getOutputInventory(), getArmorInventory()
                };

                for (InventoryObject inventory : tempInventoryObjectArray) {

                    int selectionX = inventory.getSelectionX();
                    int selectionY = inventory.getSelectionY();

                    if (selectionX >= 0 && selectionY >= 0) {

                        //don't allow anything to happen to the output
                        if (!inventory.getName().equals("output")) {
                            String mouseItem = getMouseInventory();
                            int mouseCount = getMouseInventoryCount();
                            String thisItem = inventory.getItem(selectionX, selectionY);
                            int thisCount = inventory.getCount(selectionX,selectionY);

                            //mouse splits stack
                            if (mouseItem == null && thisItem != null) {
                                int subtraction = thisCount / 2;
                                int remainder = thisCount / 2;

                                //solve for odd numbered stacks
                                if (subtraction + remainder < thisCount) {
                                    subtraction += 1;
                                }

                                if (remainder == 0) {
                                    inventory.set(selectionX, selectionY, null, 0);
                                } else {
                                    thisCount = remainder;
                                    inventory.set(selectionX, selectionY, thisItem, thisCount);
                                }

                                foundInventory = inventory.getName();
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
                                    inventory.setCount(selectionX,selectionY, thisCount);
                                    if (mouseItemStack <= 0) {
                                        setMouseInventory(null, 0);
                                    } else {
                                        mouseCount = mouseItemStack;
                                        setMouseCount(mouseCount);
                                    }
                                    inventoryUpdate = true;
                                    foundInventory = inventory.getName();
                                }
                                //mouse creating initial stack
                            } else if (mouseItem != null){
                                //single add to non-existing

                                int mouseItemStack = mouseCount;

                                mouseItemStack -= 1;

                                inventory.set(selectionX, selectionY, mouseItem, 1);

                                if (mouseItemStack <= 0) {
                                    setMouseInventory(null, 0);
                                } else {
                                    mouseCount = mouseItemStack;
                                    setMouseInventory(mouseItem, mouseCount);
                                }
                                inventoryUpdate = true;
                                foundInventory = inventory.getName();
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
                        setOutputInventory(newItems.output, newItems.amountOutput);
                    //clear output inventory
                    } else {
                        setOutputInventory(null, 0);
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

        CraftRecipeObject newItems;

        if (isAtCraftingBench()){
            InventoryObject inventory = getBigCraftInventory();

            for (int x = 0; x < inventory.getSizeX(); x++){
                for (int y = 0; y < inventory.getSizeY(); y++){
                    if (inventory.getItem(x,y) != null){
                        int count = inventory.getCount(x,y);
                        count--;
                        if (count <= 0){
                            inventory.set(x,y,null, 0);
                        } else {
                            inventory.setCount(x,y,count);
                        }
                    }
                }
            }
            newItems = recipeScan(getBigCraftInventory());
        } else {
            InventoryObject inventory = getSmallCraftInventory();
            for (int x = 0; x < inventory.getSizeX(); x++){
                for (int y = 0; y < inventory.getSizeY(); y++){
                    if (inventory.getItem(x,y) != null){
                        int count = inventory.getCount(x,y);
                        count--;
                        if (count <= 0){
                            inventory.set(x,y,null, 0);
                        } else {
                            inventory.setCount(x,y,count);
                        }
                    }
                }
            }
            newItems = recipeScan(getSmallCraftInventory());
        }

        if (newItems != null){
            //addItemToInventory(newItems.output);
            setOutputInventory(newItems.output, newItems.amountOutput);
            //clear output inventory
        } else {
            setOutputInventory(null, 0);
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



    public static void collideMouseWithInventory(InventoryObject inventory){
        double startingPointX = inventory.getPosX();
        double startingPointY = inventory.getPosY();

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

        int inventorySizeX = inventory.getSizeX();
        int inventorySizeY = inventory.getSizeY();


        Vector2d offset = new Vector2d((double)inventorySizeX/2d,(double)inventorySizeY/2d);


        double yProgram;

        if (inventory.isMainInventory()) {

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
                    if (mousePos.x >= slotPosition.x - (scale / 2) && mousePos.x <= slotPosition.x + (scale / 2) && mousePos.y >= slotPosition.y - (scale / 2) && mousePos.y <= slotPosition.y + (scale / 2)) {
                        inventory.setSelection(x, y);
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
                    if (mousePos.x >= slotPosX - (scale / 2) && mousePos.x <= slotPosX + (scale / 2) && mousePos.y >= slotPosY - (scale / 2) && mousePos.y <= slotPosY + (scale / 2)) {
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
