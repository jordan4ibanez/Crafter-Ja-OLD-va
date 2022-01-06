package game.crafting;

import org.joml.Math;

import static engine.graphics.Camera.*;
import static engine.network.Networking.*;
import static engine.time.Time.getDelta;
import static game.item.ItemDefinition.*;
import static game.item.ItemEntity.createItem;
import static game.player.Player.*;

public class Inventory {
    private static final InventoryObject armorInventory = new InventoryObject("armor", 1,4, -3.9875,2.15, false);
    private static final InventoryObject outputInventory = new InventoryObject("output", 1,1, 3.25,2.23, false);
    private static final InventoryObject smallCraftInventory = new InventoryObject("smallCraft", 2,2, 0.25,2.23, false);
    private static final InventoryObject bigCraftInventory = new InventoryObject("bigCraft", 3,3, 0.1,2.23, false);
    private static final InventoryObject mainInventory = new InventoryObject("main", 9,4, 0,-2.15, true);

    private static boolean inventoryOpen = false;

    private static boolean atCraftingBench = false;

    //inventory when you're moving items around
    private static String mouseInventory;
    private static int mouseInventoryCount;
    //special pseudo inventory for wielding item
    private static String wieldInventory;
    private static float wieldLight = 15f;


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

    public static String getWieldInventory() {
        return wieldInventory;
    }

    public static void updateWieldInventory(byte light){

        double delta = getDelta();

        int newSelectionPos = getCurrentInventorySelection();

        String newItem = getItemInInventorySlot(newSelectionPos, 0);

        //don't update if wield hand
        if (newItem == null){
            //System.out.println("RESET TO 1");
            updatePlayerMiningLevelCache(0.3f,1,1,1);
            return;
        }

        updateTimer += delta;

        if (oldLight != light || newSelectionPos != oldSelectionPos || !newItem.equals(oldItemName) || updateTimer > 0.5f){
            //update item
            if (!newItem.equals(oldItemName)){
                wieldInventory = newItem;

                float stoneMiningLevel = getStoneMiningLevel(newItem);
                float dirtMiningLevel = getDirtMiningLevel(newItem);
                float woodMiningLevel = getWoodMiningLevel(newItem);
                float leafMiningLevel = getLeafMiningLevel(newItem);

                //LEVEL OR 1
                updatePlayerMiningLevelCache(
                        stoneMiningLevel != 0 ? stoneMiningLevel : 0.3f,
                        dirtMiningLevel  != 0 ? dirtMiningLevel  : 1,
                        woodMiningLevel  != 0 ? woodMiningLevel  : 1,
                        leafMiningLevel  != 0 ? leafMiningLevel  : 1);
            }
            //update light level
            wieldLight = light;
            updateTimer = 0f;
        }

        oldLight = light;
        oldSelectionPos = newSelectionPos;
        oldItemName = newItem;
    }

    public static void generateRandomInventory(){
        for (int x = 0; x < 9; x++){
            for (int y = 0; y < 4; y++){
                String thisItem = getRandomItemDefinition();
                if (thisItem.equals("air")){
                    mainInventory.set(x,y,null, 0);
                } else {
                    int thisAmount = (int)Math.floor(Math.random() * 65);
                    if (thisAmount == 0){
                        thisAmount = 1;
                    }
                    mainInventory.set(x,y,thisItem, thisAmount);
                }
            }
        }
    }


    //could be used for when a player dies
    public static void resetInventory(){
        for (int x = 0; x < 9; x++){
            for (int y = 0; y < 4; y++){
                mainInventory.delete(x,y);
            }
        }
    }

    public static boolean addItemToInventory(String name){
        return mainInventory.addToInventory(name);
    }

    public static void throwItem(){
        String thisItem = mainInventory.getItem(getPlayerInventorySelection(), 0);
        int count = mainInventory.getCount(getPlayerInventorySelection(), 0);
        if (thisItem != null) {
            if (getIfMultiplayer()){
                System.out.println("this gotta be fixed boi");
                //sendOutThrowItemUpdate();
            } else {
                createItem(thisItem,
                        getPlayerPosWithEyeHeightX(),getPlayerPosWithEyeHeightY(),getPlayerPosWithEyeHeightZ(),
                        (getCameraRotationVectorX()*10f) + getPlayerInertiaX(),(getCameraRotationVectorY()*10f) + getPlayerInertiaY(), (getCameraRotationVectorZ()*10f) + getPlayerInertiaZ()
                        , count, 0);
            }
            removeItemFromInventory(getPlayerInventorySelection(), 0);
        }
    }

    public static void clearOutCraftInventories(){
        for (int x = 0; x < bigCraftInventory.getSizeX(); x++) {
            for (int y = 0; y < bigCraftInventory.getSizeY(); y++) {
                String thisItem = bigCraftInventory.getItem(x, y);
                int count = bigCraftInventory.getCount(x, y);
                if (thisItem != null) {
                    for (int i = 0; i < count; i++) {
                        createItem(thisItem,
                                getPlayerPosWithEyeHeightX(),getPlayerPosWithEyeHeightY(),getPlayerPosWithEyeHeightZ(),
                                (getCameraRotationVectorX()*10f) + getPlayerInertiaX(),(getCameraRotationVectorY()*10f) + getPlayerInertiaY(), (getCameraRotationVectorZ()*10f) + getPlayerInertiaZ()
                                , 1, 0);
                    }
                    bigCraftInventory.delete(x, y);
                }
            }
        }

        for (int x = 0; x < smallCraftInventory.getSizeX(); x++) {
            for (int y = 0; y < smallCraftInventory.getSizeY(); y++) {
                String thisItem = smallCraftInventory.getItem(x, y);
                int count = smallCraftInventory.getCount(x, y);
                if (thisItem != null) {
                    for (int i = 0; i < count; i++) {
                        createItem(thisItem,
                                getPlayerPosWithEyeHeightX(),getPlayerPosWithEyeHeightY(),getPlayerPosWithEyeHeightZ(),
                                (getCameraRotationVectorX()*10f) + getPlayerInertiaX(),(getCameraRotationVectorY()*10f) + getPlayerInertiaY(), (getCameraRotationVectorZ()*10f) + getPlayerInertiaZ()
                                , 1, 0);
                    }
                    smallCraftInventory.delete(x, y);
                }
            }
        }
    }

    public static void setItemInInventory(int x, int y, String name, int count){
        mainInventory.set(x,y, name, count);
    }

    public static void removeItemFromInventory(int x, int y){
        mainInventory.removeItem(x,y);
    }

    public static void removeStackFromInventory(int x, int y){
        mainInventory.set(x,y,null,0);
    }

    public static String getItemInInventorySlot(int x, int y){
        return mainInventory.getItem(x,y);
    }

    public static int getCountInInventorySlot(int x, int y){
        return mainInventory.getCount(x,y);
    }

    public static String getItemInInventorySlotName(int x, int y){
        return mainInventory.getItem(x,y);
    }


    public static String getMouseInventory(){
        return mouseInventory;
    }
    public static int getMouseInventoryCount(){
        return mouseInventoryCount;
    }

    public static void setMouseInventory(String newItem, int count){
        /* this crashes the game
        if (mouseInventory != null && mouseInventory.mesh != null){
            mouseInventory.mesh.cleanUp(false);
            mouseInventory = null; //shove it into a null pointer
        }
         */
        mouseInventory = newItem;
        mouseInventoryCount = count;
    }

    public static void setMouseCount(int count){
        mouseInventoryCount = count;
    }

    public static void emptyMouseInventory(){
        if (mouseInventory != null) {
            for (int i = 0; i < mouseInventoryCount; i++) {
                createItem(mouseInventory,
                        getPlayerPosWithEyeHeightX(),getPlayerPosWithEyeHeightY(),getPlayerPosWithEyeHeightZ(),
                        (getCameraRotationVectorX()*10f) + getPlayerInertiaX(),(getCameraRotationVectorY()*10f) + getPlayerInertiaY(), (getCameraRotationVectorZ()*10f) + getPlayerInertiaZ()
                        , 1, 0);
            }
            setMouseInventory(null, 0);
        }
    }

    public static void setOutputInventory(String newItem, int count) {
        outputInventory.set(0,0,newItem, count);
    }

    public static void setPlayerInventoryIsOpen(boolean truth){
        inventoryOpen = truth;
    }

    public static boolean isPlayerInventoryOpen(){
        return inventoryOpen;
    }

    public static void cleanInventoryMemory(){
        armorInventory.clear();
        outputInventory.clear();
        smallCraftInventory.clear();
        bigCraftInventory.clear();
        mainInventory.clear();
    }
}
