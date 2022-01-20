package game.crafting;

import org.joml.Math;

import static engine.graphics.Camera.*;
import static engine.network.Networking.*;
import static engine.time.Time.getDelta;
import static game.crafting.InventoryObject.*;
import static game.entity.item.ItemEntity.createItem;
import static game.player.Player.*;

final public class Inventory {

    public static void createInitialInventory(){
        createInventory("armor", 1,4, -3.9875,2.15, false);
        createInventory("output", 1,1, 3.25,2.23, false);
        createInventory("smallCraft", 2,2, 0.25,2.23, false);
        createInventory("bigCraft", 3,3, 0.1,2.23, false);
        createInventory("main", 9,4, 0,-2.15, true);
    }

    private static boolean inventoryOpen = false;

    private static boolean atCraftingBench = false;

    //inventory when you're moving items around
    private static String mouseInventory;
    private static int mouseInventoryCount;
    //special pseudo inventory for wielding item
    private static String wieldInventory;

    private static int oldSelectionPos = 0;
    private static String oldItemName = "";
    private static float updateTimer = 0f;
    private static byte oldLight = 15;

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

        String newItem = getItemInInventory("main", newSelectionPos, 0);

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
                    setInventoryItem("main", x,y,null, 0);
                } else {
                    int thisAmount = (int)Math.floor(Math.random() * 65);
                    if (thisAmount == 0){
                        thisAmount = 1;
                    }
                    setInventoryItem("main", x,y,thisItem, thisAmount);
                }
            }
        }
    }


    //could be used for when a player dies
    public static void resetInventory(){
        for (int x = 0; x < 9; x++){
            for (int y = 0; y < 4; y++){
                deleteInventoryItem("main", x,y);
            }
        }
    }

    public static void throwItem(){
        String thisItem = getItemInInventory("main", getPlayerInventorySelection(), 0);

        int count = getInventoryCount("main", getPlayerInventorySelection(), 0);

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
            removeItemFromInventory("main", getPlayerInventorySelection(), 0);
        }
    }

    public static void clearOutCraftInventories(){
        String[][] bigCraftInventory = getInventoryAsArray("bigCraft");
        int[][] bigCraftCount = getInventoryCountAsArray("bigCraft");

        for (int x = 0; x < bigCraftInventory.length; x++) {
            for (int y = 0; y < bigCraftInventory[0].length; y++) {

                String thisItem = bigCraftInventory[y][x];
                int count = bigCraftCount[y][x];

                if (thisItem != null) {
                    for (int i = 0; i < count; i++) {
                        createItem(thisItem,
                                getPlayerPosWithEyeHeightX(),getPlayerPosWithEyeHeightY(),getPlayerPosWithEyeHeightZ(),
                                (getCameraRotationVectorX()*10f) + getPlayerInertiaX(),(getCameraRotationVectorY()*10f) + getPlayerInertiaY(), (getCameraRotationVectorZ()*10f) + getPlayerInertiaZ()
                                , 1, 0);
                    }
                }
            }
        }

        clearOutInventory("bigCraft");

        String[][] smallCraftInventory = getInventoryAsArray("smallCraft");
        int[][] smallCraftCount = getInventoryCountAsArray("smallCraft");

        for (int x = 0; x < smallCraftInventory.length; x++) {
            for (int y = 0; y < smallCraftInventory[0].length; y++) {

                String thisItem = smallCraftInventory[y][x];
                int count = smallCraftCount[y][x];
                if (thisItem != null) {
                    for (int i = 0; i < count; i++) {
                        createItem(thisItem,
                                getPlayerPosWithEyeHeightX(),getPlayerPosWithEyeHeightY(),getPlayerPosWithEyeHeightZ(),
                                (getCameraRotationVectorX()*10f) + getPlayerInertiaX(),(getCameraRotationVectorY()*10f) + getPlayerInertiaY(), (getCameraRotationVectorZ()*10f) + getPlayerInertiaZ()
                                , 1, 0);
                    }
                }
            }
        }

        clearOutInventory("smallCraft");
    }



    public static String getMouseInventory(){
        return mouseInventory;
    }
    public static int getMouseInventoryCount(){
        return mouseInventoryCount;
    }

    public static void setMouseInventory(String newItem, int count){
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

    public static void setPlayerInventoryIsOpen(boolean truth){
        inventoryOpen = truth;
    }

    public static boolean isPlayerInventoryOpen(){
        return inventoryOpen;
    }

    public static void cleanInventoryMemory(){
        clearOutInventory("armor");
        clearOutInventory("output");
        clearOutInventory("smallCraft");
        clearOutInventory("bigCraft");
        clearOutInventory("main");
    }
}
