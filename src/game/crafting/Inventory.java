package game.crafting;

import game.item.Item;
import game.item.ItemDefinition;
import org.joml.Vector2d;

import static engine.Time.getDelta;
import static game.blocks.BlockDefinition.getBlockDefinition;
import static game.item.ItemDefinition.getItemDefinition;
import static game.item.ItemDefinition.getRandomItemDefinition;
import static game.item.ItemEntity.createItem;
import static engine.graphics.Camera.getCameraRotationVector;
import static game.player.Player.*;

public class Inventory {
    private static final InventoryObject armorInventory = new InventoryObject("armor", 1,4, new Vector2d(-3.9875,2.15), false);
    private static final InventoryObject outputInventory = new InventoryObject("output", 1,1, new Vector2d(3.25,2.23), false);
    private static final InventoryObject smallCraftInventory = new InventoryObject("smallCraft", 2,2, new Vector2d(0.25,2.23), false);
    private static final InventoryObject bigCraftInventory = new InventoryObject("bigCraft", 3,3, new Vector2d(0.1,2.23), false);
    private static final InventoryObject mainInventory = new InventoryObject("main", 9,4, new Vector2d(0,-2.15), true);

    private static boolean inventoryOpen = false;

    private static boolean atCraftingBench = false;

    //inventory when you're moving items around
    private static Item mouseInventory;
    //special pseudo inventory for wielding item
    private static Item wieldInventory;

    private static int oldSelectionPos = 0;
    private static String oldItemName = "";
    private static float updateTimer = 0f;
    private static byte oldLight = 15;

    public static InventoryObject getMainInventory(){
        return mainInventory;
    }

    public static InventoryObject getSmallCraftInventory(){
        return smallCraftInventory;
    }
    public static InventoryObject getBigCraftInventory(){
        return bigCraftInventory;
    }

    public static InventoryObject getOutputInventory(){
        return outputInventory;
    }

    public static InventoryObject getArmorInventory(){
        return armorInventory;
    }

    public static boolean isAtCraftingBench(){
        return atCraftingBench;
    }

    public static void setIsAtCraftingBench(boolean isCurrentlyAtCraftingBench){
        atCraftingBench = isCurrentlyAtCraftingBench;
    }

    public static Item getWieldInventory() {
        return wieldInventory;
    }

    public static void updateWieldInventory(byte light){

        float delta = getDelta();

        int newSelectionPos = getCurrentInventorySelection();
        Item newItem = getItemInInventorySlot(newSelectionPos, 0);

        //don't update if wield hand
        if (newItem == null){
            //System.out.println("RESET TO 1");
            updatePlayerMiningLevelCache(0.3f,1,1,1);
            return;
        }

        String newItemName = newItem.name;

        updateTimer += delta;

        if (oldLight != light || newSelectionPos != oldSelectionPos || !newItemName.equals(oldItemName) || updateTimer > 0.5f){
            //update item
            if (!newItemName.equals(oldItemName)){
                wieldInventory = new Item(newItemName, 1);
                ItemDefinition newDef = getItemDefinition(newItemName);

                //LEVEL OR 1
                updatePlayerMiningLevelCache(
                        newDef.stoneMiningLevel != 0 ? newDef.stoneMiningLevel : 0.3f,
                        newDef.dirtMiningLevel  != 0 ? newDef.dirtMiningLevel  : 1,
                        newDef.woodMiningLevel  != 0 ? newDef.woodMiningLevel  : 1,
                        newDef.leafMiningLevel  != 0 ? newDef.leafMiningLevel  : 1);
            }
            //update light level
            wieldInventory.light = light;
            wieldInventory.rebuildLightMesh(wieldInventory);

            updateTimer = 0f;
        }

        oldLight = light;
        oldSelectionPos = newSelectionPos;
        oldItemName = newItemName;
    }

    public static void generateRandomInventory(){
        for (int x = 0; x < 9; x++){
            for (int y = 0; y < 4; y++){
                String thisItem = getRandomItemDefinition().name;
                if (thisItem.equals("air")){
                    mainInventory.set(x,y,null);
                } else {
                    int thisAmount = (int) Math.floor(Math.random() * 65);
                    if (thisAmount == 0){
                        thisAmount = 1;
                    }
                    mainInventory.set(x,y,new Item(thisItem, thisAmount));
                }
            }
        }
    }


    //could be used for when a player dies
    public static void resetInventory(){
        for (int x = 0; x < 9; x++){
            for (int y = 0; y < 4; y++){
                mainInventory.set(x,y, null);
            }
        }
    }

    public static boolean addItemToInventory(String name){
        //check whole inventory
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 9; x++) {
                if (mainInventory.get(x,y) != null && mainInventory.get(x,y).name.equals(name)){
                    mainInventory.get(x,y).stack++;
                    return true;
                }
            }
        }
        //failed to find one, create new stack
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 9; x++) {
                if (mainInventory.get(x,y) == null){
                    mainInventory.set(x,y,new Item(name, 1));
                    return true;
                }
            }
        }
        return false;
    }

    public static void throwItem(){
        Item test = getItemInInventorySlot(getPlayerInventorySelection(), 0);
        if (test != null) {
            ItemDefinition layer2 = test.definition;
            if (layer2 != null) {
                String name = layer2.name;
                if (name != null) {
                    createItem(name, getPlayerPosWithEyeHeight(), getCameraRotationVector().mul(10f).add(getPlayerInertia()), test.stack);
                    removeItemFromInventory(getPlayerInventorySelection(), 0);
                }
            }
        }
    }

    public static void clearOutCraftInventories(){
        InventoryObject inventory = getBigCraftInventory();

        for (int x = 0; x < inventory.getSize().x; x++) {
            for (int y = 0; y < inventory.getSize().y; y++) {
                Item thisItem = inventory.get(x, y);
                if (thisItem != null) {
                    for (int i = 0; i < thisItem.stack; i++) {
                        createItem(thisItem.name, getPlayerPosWithEyeHeight(), getCameraRotationVector().mul(10f).add(getPlayerInertia()), 1);
                    }
                    inventory.set(x, y, null);
                }
            }
        }


        InventoryObject inventory2 = getSmallCraftInventory();
        for (int x = 0; x < inventory2.getSize().x; x++) {
            for (int y = 0; y < inventory2.getSize().y; y++) {
                Item thisItem = inventory2.get(x, y);
                if (thisItem != null) {
                    for (int i = 0; i < thisItem.stack; i++) {
                        createItem(thisItem.name, getPlayerPosWithEyeHeight(), getCameraRotationVector().mul(10f).add(getPlayerInertia()), 1);
                    }
                    inventory2.set(x, y, null);
                }
            }
        }
    }

    public static void setItemInInventory(int x, int y, String name, int stack){
        mainInventory.set(x,y,new Item(name, stack));
    }

    public static void removeItemFromInventory(int x, int y){
        mainInventory.get(x,y).stack--;
        if (mainInventory.get(x,y).stack <= 0){
            mainInventory.set(x,y,null);
        }
    }

    public static void removeStackFromInventory(int x, int y){
        mainInventory.set(x,y,null);
    }

    public static Item getItemInInventorySlot(int x, int y){
        return mainInventory.get(x,y);
    }

    public static String getItemInInventorySlotName(int x, int y){
        if (mainInventory.get(x,y) == null){
            return "null";
        } else {
            return mainInventory.get(x,y).name;
        }
    }


    public static Item getMouseInventory(){
        return mouseInventory;
    }

    public static void setMouseInventory(Item newItem){
        /* this crashes the game
        if (mouseInventory != null && mouseInventory.mesh != null){
            mouseInventory.mesh.cleanUp(false);
            mouseInventory = null; //shove it into a null pointer
        }
         */
        mouseInventory = newItem;
    }

    public static void emptyMouseInventory(){
        Item thisItem = getMouseInventory();
        if (thisItem != null) {
            for (int i = 0; i < thisItem.stack; i++) {
                createItem(thisItem.name, getPlayerPosWithEyeHeight(), getCameraRotationVector().mul(10f).add(getPlayerInertia()), 1);
            }
            setMouseInventory(null);
        }
    }

    public static void setOutputInventory(Item newItem) {
        outputInventory.set(0,0,newItem);
    }

    public static void setPlayerInventoryIsOpen(boolean truth){
        inventoryOpen = truth;
    }

    public static boolean isPlayerInventoryOpen(){
        return inventoryOpen;
    }
}
