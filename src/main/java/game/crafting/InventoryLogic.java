package game.crafting;

import engine.Controls;
import engine.Mouse;
import engine.Window;
import game.player.Player;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class InventoryLogic {

    private Player player;
    private Mouse mouse;
    private Window window;
    private Controls controls;
    private CraftRecipes craftRecipes;

    public void setPlayer(Player player){
        if (this.player == null){
            this.player = player;
            this.craftRecipes = new CraftRecipes(player);
        }
    }

    public void setMouse(Mouse mouse){
        if (this.mouse == null){
            this.mouse = mouse;
        }
    }
    public void setWindow(Window window){
        if (this.window == null){
            this.window = window;
        }
    }

    public void setControls(Controls controls){
        if (this.controls == null){
            this.controls = controls;
        }
    }

    private final Inventory inventory = new Inventory();

    private String oldSelection;

    private final Vector3f playerRot = new Vector3f(0,0,0);

    //FIXME this shouldn't be handled in this object
    private boolean leftMouseButtonPushed = false;
    private boolean leftMouseButtonWasPushed = false;
    private boolean rightMouseButtonPushed = false;
    private boolean rightMouseButtonWasPushed = false;



    public InventoryLogic(){
    }

    private final InventoryObject[] inventoryArray = {
            inventory.getMain(), inventory.getSmallCraft(), inventory.getBigCraft(), inventory.getOutput(), inventory.getArmor()
    };

    public Inventory getInventory() {
        return inventory;
    }

    public void inventoryOnTick(){

        String itemWielding = inventory.getMain().getItem(player.getCurrentInventorySelection(), 0);

        if ((itemWielding != null && oldSelection == null) || (itemWielding == null && oldSelection != null) || (itemWielding != null && oldSelection != null && !itemWielding.equals(oldSelection))) {
            //resetWieldHandSetupTrigger();
            oldSelection = itemWielding;
        }

        if (player.isInventoryOpen()) {

            //begin player in inventory thing
            //new scope because lazy
            {
                float windowScale = window.getScale();

                double basePlayerPosX = -(windowScale / 3.75d);
                double basePlayerPosY = (windowScale / 2.8d);

                double mousePosX = mouse.getPos().x;
                double mousePosY = mouse.getPos().y;

                double windowSizeX = window.getWidth();
                double windowSizeY = window.getHeight();

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

                float rotationY = (float)((mousePosX - (windowSizeX/2f)) - basePlayerPosX) / (windowScale * 1.2f);
                rotationY *= 40f;
                playerRot.y = rotationY;


                float rotationX = (float)((mousePosY - (windowSizeY/2f)) + (basePlayerPosY /2f)) / (windowScale * 1.2f);
                rotationX *= 40f;
                playerRot.x = rotationX;
            }

            collideMouseWithInventory(this.inventory.getMain());

            if (player.isAtCraftingBench()){
                collideMouseWithInventory(this.inventory.getBigCraft());
            } else {
                collideMouseWithInventory(this.inventory.getSmallCraft());
            }

            collideMouseWithInventory(this.inventory.getOutput());
            collideMouseWithInventory(this.inventory.getOutput());

            String foundInventory = null;

            boolean inventoryUpdate = false;

            //normal left click moving items
            if (mouse.isLeftButtonPressed() && !leftMouseButtonPushed && !leftMouseButtonWasPushed) {

                leftMouseButtonPushed = true;

                for (InventoryObject inventory : inventoryArray) {

                    Vector2i selection = inventory.getSelection();

                    if (!inventory.getName().equals("output")) {
                        if (selection.x >= 0 && selection.y >= 0) {
                            String thisItem = inventory.getItem(selection.x, selection.y);
                            int thisCount = inventory.getCount(selection.x, selection.y);
                            String mouseItem = this.inventory.getMouseInventory();
                            int mouseCount = this.inventory.getMouseCount();

                            //pick up item
                            if (mouseItem == null && thisItem != null) {
                                this.inventory.setMouseInventory(thisItem, thisCount);
                                inventory.setItem(selection.x, selection.y, null, 0);
                                foundInventory = inventory.getName();
                                inventoryUpdate = true;
                                //add to blank space
                            } else if (mouseItem != null && thisItem == null) {
                                inventory.setItem(selection.x, selection.y, mouseItem, mouseCount);
                                this.inventory.setMouseInventory(null, 0);
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
                                        this.inventory.setMouseInventory(thisItem, thisCount);
                                        inventory.setItem(selection.x, selection.y, mouseItem, mouseCount);
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

                                            this.inventory.setMouseCount(mouseCount);
                                            inventory.setCount(selection.x, selection.y, thisCount);
                                        } else {

                                            thisCount = adder;
                                            this.inventory.setMouseInventory(null, 0);
                                            foundInventory = inventory.getName();
                                            inventoryUpdate = true;
                                            inventory.setCount(selection.x, selection.y, thisCount);
                                        }
                                    }
                                    //swap mouse item with inventory item
                                } else {
                                    this.inventory.setMouseInventory(thisItem, thisCount);
                                    inventory.setItem(selection.x, selection.y, mouseItem, mouseCount);
                                    foundInventory = inventory.getName();
                                    inventoryUpdate = true;
                                }
                            }
                        }
                        //handle craft output
                    } else {
                        if (selection.x >= 0 && selection.y >= 0) {
                            String thisItem = inventory.getItem(selection.x, selection.y);
                            int thisCount = inventory.getCount(selection.x, selection.y);
                            String mouseItem = this.inventory.getMouseInventory();
                            int mouseCount = this.inventory.getMouseCount();

                            //new item in mouse inventory
                            if (mouseItem == null && thisItem != null) {
                                this.inventory.setMouseInventory(thisItem, thisCount);
                                this.inventory.getOutput().setItem(0, 0, null, 0);
                                updateCraftingGrid();
                                inventoryUpdate = true;
                                //add to existing stack
                            } else if (mouseItem != null && thisItem != null && mouseItem.equals(thisItem)) {
                                int adder = mouseCount + thisCount;

                                if (adder <= 64) {
                                    mouseCount = adder;
                                    this.inventory.getOutput().setItem(0, 0, null, 0);
                                    updateCraftingGrid();
                                    inventoryUpdate = true;

                                    this.inventory.setMouseCount(mouseCount);
                                }
                            }
                        }
                    }
                }
            } else if (!mouse.isLeftButtonPressed()) {
                leftMouseButtonPushed = false;
            }

            //right click moving items/splitting stack/dropping single items
            if (mouse.isRightButtonPressed() && !rightMouseButtonPushed && !rightMouseButtonWasPushed) {
                rightMouseButtonPushed = true;

                for (InventoryObject inventory : inventoryArray) {


                    Vector2i selection = inventory.getSelection();

                    if (selection.x >= 0 && selection.y >= 0) {

                        //don't allow anything to happen to the output
                        if (!inventory.equals("output")) {
                            String mouseItem = this.inventory.getMouseInventory();
                            int mouseCount = this.inventory.getMouseCount();
                            String thisItem = inventory.getItem(selection.x, selection.y);
                            int thisCount = inventory.getCount(selection.x,selection.y);

                            //mouse splits stack
                            if (mouseItem == null && thisItem != null) {
                                int subtraction = thisCount / 2;
                                int remainder = thisCount / 2;

                                //solve for odd numbered stacks
                                if (subtraction + remainder < thisCount) {
                                    subtraction += 1;
                                }

                                if (remainder == 0) {
                                    inventory.setItem(selection.x, selection.y, null, 0);
                                } else {
                                    thisCount = remainder;
                                    inventory.setItem(selection.x, selection.y, thisItem, thisCount);
                                }

                                foundInventory = inventory.getName();
                                this.inventory.setMouseInventory(thisItem, subtraction);
                                inventoryUpdate = true;
                            }
                            //mouse single place into existing stack
                            else if (thisItem != null) {
                                //single add to existing stack
                                if (mouseItem.equals(thisItem) && thisCount < 64) {

                                    int mouseItemStack = mouseCount;
                                    mouseItemStack -= 1;
                                    thisCount++;
                                    inventory.setCount(selection.x, selection.y, thisCount);
                                    if (mouseItemStack <= 0) {
                                        this.inventory.setMouseInventory(null, 0);
                                    } else {
                                        mouseCount = mouseItemStack;
                                        this.inventory.setMouseCount(mouseCount);
                                    }
                                    inventoryUpdate = true;
                                    foundInventory = inventory.getName();
                                }
                                //mouse creating initial stack
                            } else if (mouseItem != null){
                                //single add to non-existing

                                int mouseItemStack = mouseCount;

                                mouseItemStack -= 1;

                                inventory.setItem(selection.x, selection.y, mouseItem, 1);

                                if (mouseItemStack <= 0) {
                                    this.inventory.setMouseInventory(null, 0);
                                } else {
                                    mouseCount = mouseItemStack;
                                    this.inventory.setMouseInventory(mouseItem, mouseCount);
                                }
                                inventoryUpdate = true;
                                foundInventory = inventory.getName();
                            }
                        }
                    }
                }
            } else if (!mouse.isRightButtonPressed()) {
                rightMouseButtonPushed = false;
            }

            if (foundInventory != null){
                if (foundInventory.equals("smallCraft") || foundInventory.equals("bigCraft")){

                    CraftRecipeObject newItems;
                    //small craft recipe scan
                    if (foundInventory.equals("smallCraft")){
                        newItems = this.craftRecipes.recipeScan(this.inventory.getSmallCraft());
                        //large craft recipe scan
                    } else {
                        newItems = this.craftRecipes.recipeScan(this.inventory.getBigCraft());
                    }

                    if (newItems != null){
                        //addItemToInventory(newItems.output);
                        this.inventory.getOutput().setItem(0,0, newItems.getOutput(), newItems.getAmount());
                    //clear output inventory
                    } else {
                        this.inventory.getOutput().setItem(0, 0, null, 0);
                    }
                }
            }

            //update the server inventory
            /*
            if (inventoryUpdate && getIfMultiplayer()){
                System.out.println("WHOOPS GOTTA FIX THIS TOO");
                //sendServerUpdatedInventory();
            }
             */

            //overkill but it gets the job done
            //save the player's main inventory
            /*
            if (inventoryUpdate){
                savePlayerData("singleplayer");
            }
             */

            leftMouseButtonWasPushed = leftMouseButtonPushed;
            rightMouseButtonWasPushed = rightMouseButtonPushed;
        }
    }

    public void updateCraftingGrid(){

        CraftRecipeObject newItems;

        if (player.isAtCraftingBench()){
            for (int x = 0; x < this.inventory.getBigCraft().getSize().x; x++){
                for (int y = 0; y < this.inventory.getBigCraft().getSize().y; y++){
                    if (this.inventory.getBigCraft().getItem(x,y) != null){
                        int count = this.inventory.getBigCraft().getCount(x,y);
                        count--;
                        this.inventory.getBigCraft().setCount(x,y, count);
                        if (count <= 0){
                            this.inventory.getBigCraft().setItem(x, y,null, 0);
                        } else {
                            this.inventory.getBigCraft().setCount(x, y, count);
                        }
                    }
                }
            }

            newItems = this.craftRecipes.recipeScan(this.inventory.getBigCraft());
        } else {
            for (int x = 0; x < this.inventory.getSmallCraft().getSize().x; x++){
                for (int y = 0; y < this.inventory.getSmallCraft().getSize().y; y++){
                    if (this.inventory.getSmallCraft().getItem(x,y) != null){
                        int count = this.inventory.getSmallCraft().getCount(x,y);
                        count--;
                        this.inventory.getSmallCraft().setCount(x,y, count);
                        if (count <= 0){
                            this.inventory.getSmallCraft().setItem(x, y,null, 0);
                        } else {
                            this.inventory.getSmallCraft().setCount(x, y, count);
                        }
                    }
                }
            }
            newItems = this.craftRecipes.recipeScan(this.inventory.getSmallCraft());
        }

        if (newItems != null){
            //addItemToInventory(newItems.output);
            this.inventory.getOutput().setItem(0,0, newItems.getOutput(), newItems.getAmount());
            //clear output inventory
        } else {
            this.inventory.getOutput().setItem(0, 0, null, 0);
        }

    }

    public void openCraftingInventory(boolean isCraftingTable) {
        //inventory closed, open it
        if (!player.isInventoryOpen()){
            mouse.setLocked(false);
            controls.resetInputs();
            player.setAtCraftingBench(isCraftingTable);
            player.setInventoryOpen(true);
        }

    }

    public void closeCraftingInventory(){
        //inventory open, close it
        if (player.isInventoryOpen()){
            mouse.setLocked(true);
            controls.resetInputs();
            player.setInventoryOpen(false);
            this.inventory.clearOutCraft();
            player.setAtCraftingBench(false);
            this.inventory.emptyMouseInventory();
            updateCraftingGrid();
        }
    }

    public Vector3f getPlayerHudRotation(){
        return playerRot;
    }



    public void collideMouseWithInventory(InventoryObject inventory){
        Vector2d startingPoint = inventory.getPos();

        float windowScale = window.getScale();

        Vector2d mousePos = mouse.getPos();

        //work from the center
        mousePos.x -= (window.getWidth() / 2f);
        mousePos.y -= (window.getHeight() / 2f);

        //invert mouse
        mousePos.y *= -1;

        //this is the size of the actual slots
        //it also makes the default spacing of (0)
        //they bunch up right next to each other with 0
        double scale = windowScale/10.5d;

        //this is the spacing between the slots
        double spacing = windowScale / 75d;

        Vector2d offset = new Vector2d((double) inventory.getSize().x / 2d,(double) inventory.getSize().y / 2d);


        double yProgram;

        if (inventory.getName().equals("main")) {

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
                        inventory.setSelection(x,y);
                        return;
                    }
                }
            }
        } else {
            for (int x = 0; x < inventory.getSize().x; x++) {
                for (int y = 0; y < inventory.getSize().y; y++) {

                    //scale is the size
                    double slotPosX = ((double) x + 0.5d - offset.x + startingPoint.x) * (scale + spacing);
                    double slotPosY = ((y * -1d) - 0.5d + startingPoint.y + offset.y) * (scale + spacing);

                    //found selection break out of loop
                    if (mousePos.x >= slotPosX - (scale / 2) && mousePos.x <= slotPosX + (scale / 2) && mousePos.y >= slotPosY - (scale / 2) && mousePos.y <= slotPosY + (scale / 2)) {
                        inventory.setSelection(x, y);
                        return;
                    }
                }
            }
        }

        //fail state
        inventory.setSelection(-1, -1);
    }

}
