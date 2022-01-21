package game.crafting;

import org.joml.Math;

import engine.graphics.Camera.*;
import engine.network.Networking.*;
import engine.time.Delta.getDelta;
import game.crafting.InventoryObject.*;
import game.entity.item.ItemEntity.createItem;
import game.player.Player.*;

final public class Inventory {

    public void createInitialInventory(){
        createInventory("armor", 1,4, -3.9875,2.15, false);
        createInventory("output", 1,1, 3.25,2.23, false);
        createInventory("smallCraft", 2,2, 0.25,2.23, false);
        createInventory("bigCraft", 3,3, 0.1,2.23, false);
        createInventory("main", 9,4, 0,-2.15, true);
    }

    private boolean inventoryOpen = false;

    private boolean atCraftingBench = false;

    //inventory when you're moving items around
    private String mouseInventory;
    private int mouseInventoryCount;
    //special pseudo inventory for wielding item
    private String wieldInventory;

    private int oldSelectionPos = 0;
    private String oldItemName = "";
    private float updateTimer = 0f;
    private byte oldLight = 15;

    public boolean isAtCraftingBench(){
        return atCraftingBench;
    }

    public void setIsAtCraftingBench(boolean isCurrentlyAtCraftingBench){
        atCraftingBench = isCurrentlyAtCraftingBench;
    }

    public String getWieldInventory() {
        return wieldInventory;
    }

    public void updateWieldInventory(byte light){

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

    public void generateRandomInventory(){
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
    public void resetInventory(){
        for (int x = 0; x < 9; x++){
            for (int y = 0; y < 4; y++){
                deleteInventoryItem("main", x,y);
            }
        }
    }

    public void throwItem(){
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

    public void clearOutCraftInventories(){
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



    public String getMouseInventory(){
        return mouseInventory;
    }
    public int getMouseInventoryCount(){
        return mouseInventoryCount;
    }

    public void setMouseInventory(String newItem, int count){
        mouseInventory = newItem;
        mouseInventoryCount = count;
    }

    public void setMouseCount(int count){
        mouseInventoryCount = count;
    }

    public void emptyMouseInventory(){
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

    public void setPlayerInventoryIsOpen(boolean truth){
        inventoryOpen = truth;
    }

    public boolean isPlayerInventoryOpen(){
        return inventoryOpen;
    }

    public void cleanInventoryMemory(){
        clearOutInventory("armor");
        clearOutInventory("output");
        clearOutInventory("smallCraft");
        clearOutInventory("bigCraft");
        clearOutInventory("main");
    }
}
