package game.crafting;

import game.blocks.BlockDefinition;
import game.item.Item;
import game.item.ItemDefinition;
import org.joml.Vector2d;

import static engine.Time.getDelta;
import static game.blocks.BlockDefinition.getBlockDefinition;
import static game.item.ItemDefinition.getRandomItemDefinition;
import static game.item.ItemEntity.createItem;
import static engine.graph.Camera.getCameraRotationVector;
import static game.player.Player.*;

public class Inventory {

    private static final InventoryObject debugInventory = new InventoryObject(4,1, new Vector2d(0,0), false);
    private static final InventoryObject armorInventory = new InventoryObject(1,4, new Vector2d(-3.9875,2.15), false);
    private static final InventoryObject outputInventory = new InventoryObject(1,1, new Vector2d(3.25,2.23), false);
    private static final InventoryObject smallCraftInventory = new InventoryObject(2,2, new Vector2d(0.25,2.23), false);

    //0,-2.15
    private static final InventoryObject mainInventory = new InventoryObject(9,4, new Vector2d(0,-2.15), true);

    private static boolean inventoryOpen = false;

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

    public static InventoryObject getOutputInventory(){
        return outputInventory;
    }

    public static InventoryObject getArmorInventory(){
        return armorInventory;
    }

    public static InventoryObject getDebugInventory(){
        return debugInventory;
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
            return;
        }

        String newItemName = newItem.name;

        updateTimer += delta;

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
        if (mouseInventory != null && mouseInventory.mesh != null){
            mouseInventory.mesh.cleanUp(false);
            mouseInventory = null; //shove it into a null pointer
        }
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

    public static void togglePlayerInventory(){
        inventoryOpen = !inventoryOpen;
    }

    public static boolean isPlayerInventoryOpen(){
        return inventoryOpen;
    }
}
