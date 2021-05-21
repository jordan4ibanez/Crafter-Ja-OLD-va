package game.crafting;

import game.blocks.BlockDefinition;
import game.item.Item;
import game.item.ItemDefinition;

import static game.blocks.BlockDefinition.getBlockDefinition;
import static game.item.ItemDefinition.getRandomItemDefinition;
import static game.item.ItemEntity.createItem;
import static engine.graph.Camera.getCameraRotationVector;
import static game.player.Player.*;

public class Inventory {

    private static final InventoryObject smallCraftInventory = new InventoryObject(2,2);
    private static final InventoryObject inventory = new InventoryObject(9,4);

    private static Item mouseInventory;

    //special pseudo inventory for wielding item
    private static Item wieldInventory;

    private static int oldSelectionPos = 0;
    private static String oldItemName = "";
    private static float updateTimer = 0f;
    private static byte oldLight = 15;

    public static Item getWieldInventory() {
        return wieldInventory;
    }

    public static void updateWieldInventory(byte light){

        int newSelectionPos = getCurrentInventorySelection();
        Item newItem = getItemInInventorySlot(newSelectionPos, 0);

        //don't update if wieldhand
        if (newItem == null){
            return;
        }

        String newItemName = newItem.name;

        updateTimer += 0.001f;

        if (oldLight != light || newSelectionPos != oldSelectionPos || !newItemName.equals(oldItemName) || updateTimer > 0.5f){
            //update item
            if (!newItemName.equals(oldItemName)){
                wieldInventory = new Item(newItemName, 1);
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
                    inventory.set(x,y,null);
                } else {
                    int thisAmount = (int) Math.floor(Math.random() * 65);
                    if (thisAmount == 0){
                        thisAmount = 1;
                    }
                    inventory.set(x,y,new Item(thisItem, thisAmount));
                }
            }
        }
    }


    //could be used for when a player dies
    public static void resetInventory(){
        for (int x = 0; x < 9; x++){
            for (int y = 0; y < 4; y++){
                inventory.set(x,y, null);
            }
        }
    }

    public static boolean addItemToInventory(String name){
        //check whole inventory
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 9; x++) {
                if (inventory.get(x,y) != null && inventory.get(x,y).name.equals(name)){
                    inventory.get(x,y).stack++;
                    return true;
                }
            }
        }
        //failed to find one, create new stack
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 9; x++) {
                if (inventory.get(x,y) == null){
                    inventory.set(x,y,new Item(name, 1));
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
    public static void setItemInInventory(int x, int y, String name, int stack){
        inventory.set(x,y,new Item(name, stack));
    }

    public static void removeItemFromInventory(int x, int y){
        inventory.get(x,y).stack--;
        if (inventory.get(x,y).stack <= 0){
            inventory.set(x,y,null);
        }
    }

    public static void removeStackFromInventory(int x, int y){
        inventory.set(x,y,null);
    }

    public static Item getItemInInventorySlot(int x, int y){
        return inventory.get(x,y);
    }

    public static String getItemInInventorySlotName(int x, int y){
        if (inventory.get(x,y) == null){
            return "null";
        } else {
            return inventory.get(x,y).name;
        }
    }


    public static Item getMouseInventory(){
        return mouseInventory;
    }

    public static void setMouseInventory(Item newItem){
        System.out.println("clean up old item mesh");
        mouseInventory = newItem;
    }

    public static void emptyMouseInventory(){
        Item test = getMouseInventory();
        if (test != null) {
            BlockDefinition layer2 = getBlockDefinition(test.name);
            if (layer2 != null) {
                String name = layer2.name;
                if (name != null) {
                    createItem(name, getPlayerPosWithEyeHeight(), getCameraRotationVector().mul(10f), test.stack);
                    setMouseInventory(null);
                }
            }
        }
    }
}
