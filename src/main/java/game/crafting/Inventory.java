package game.crafting;

import engine.time.Delta;
import game.player.Player;
import org.joml.Vector2d;
import org.joml.Vector2i;

public class Inventory {
    private final Delta delta;
    private final Player player;

    private boolean inventoryOpen = false;
    private boolean atCraftingBench = false;

    //inventory when you're moving items around
    private String mouseInventory;
    private int mouseInventoryCount;

    //FIXME: special pseudo inventory for wielding item
    //FIXME: this is mainly used for the hud and is a massive oversight
    //FIXME: DEPRECATED!
    private String wieldInventory;

    //FIXME: WHY IS THE HUD GETTING INFORMATION FROM THE INVENTORY ON LIGHT DATA???
    private int oldSelectionPos = 0;
    private String oldItemName = "";
    private float updateTimer = 0f;
    private byte oldLight = 15;

    private final InventoryObject armor      ;
    private final InventoryObject output     ;
    private final InventoryObject smallCraft ;
    private final InventoryObject bigCraft   ;
    private final InventoryObject main       ;



    public Inventory(Delta delta, Player player){

        this.delta = delta;
        this.player = player;

        armor      = new InventoryObject("armor", new Vector2i(1,4), new Vector2d( -3.9875,2.15), false);
        output     = new InventoryObject("output", new Vector2i(1,1), new Vector2d(3.25,2.23), false);
        smallCraft = new InventoryObject("smallCraft", new Vector2i(2,2), new Vector2d(0.25,2.23), false);
        bigCraft   = new InventoryObject("bigCraft", new Vector2i(3,3), new Vector2d(0.1,2.23), false);
        main       = new InventoryObject("main", new Vector2i(9,4), new Vector2d(0,-2.15), true);
    }

    public boolean isAtCraftingBench(){
        return atCraftingBench;
    }

    public void setAtCraftingBench(boolean atCraftingBench){
        this.atCraftingBench = atCraftingBench;
    }

    public String getWieldInventory() {
        return wieldInventory;
    }

    //FIXME: THIS SHOULD NOT BE IN HERE OH MY GOD
    /*
    public void updateWieldInventory(byte light){

        int newSelectionPos = getCurrentInventorySelection();

        String newItem = getItemInInventory("main", newSelectionPos, 0);

        //don't update if wield hand
        if (newItem == null){
            //System.out.println("RESET TO 1");
            updatePlayerMiningLevelCache(0.3f,1,1,1);
            return;
        }

        updateTimer += delta.getDelta();

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
     */

    //could be used for when a player dies
    public void resetInventory(){
        for (int x = 0; x < 9; x++){
            for (int y = 0; y < 4; y++){
                main.deleteItem(x,y);
            }
        }
    }

    public void throwItem(){
        String thisItem = main.getItem(player.getPlayerInventorySelection(), 0);

        int count = main.getCount(player.getPlayerInventorySelection(), 0);

        if (thisItem != null) {
            /*if (getIfMultiplayer()){
                System.out.println("this gotta be fixed boi");
                //sendOutThrowItemUpdate();
            } else {
            createItem(thisItem,
                    getPlayerPosWithEyeHeightX(),getPlayerPosWithEyeHeightY(),getPlayerPosWithEyeHeightZ(),
                    (getCameraRotationVectorX()*10f) + getPlayerInertiaX(),(getCameraRotationVectorY()*10f) + getPlayerInertiaY(), (getCameraRotationVectorZ()*10f) + getPlayerInertiaZ()
                    , count, 0);
            //}
             */
            main.removeItem(player.getPlayerInventorySelection(), 0);
        }
    }


    public void clearOutCraft(){
        for (int x = 0; x < bigCraft.getSize().x; x++) {
            for (int y = 0; y < bigCraft.getSize().y; y++) {
                String thisItem = bigCraft.getItem(x,y);
                int count = bigCraft.getCount(x,y);

                if (thisItem != null) {
                    for (int i = 0; i < count; i++) {
                        /*
                        createItem(thisItem,
                                getPlayerPosWithEyeHeightX(),getPlayerPosWithEyeHeightY(),getPlayerPosWithEyeHeightZ(),
                                (getCameraRotationVectorX()*10f) + getPlayerInertiaX(),(getCameraRotationVectorY()*10f) + getPlayerInertiaY(), (getCameraRotationVectorZ()*10f) + getPlayerInertiaZ()
                                , 1, 0);
                         */
                    }
                }
            }
        }

        bigCraft.clear();

        for (int x = 0; x < smallCraft.getSize().x; x++) {
            for (int y = 0; y < smallCraft.getSize().y; y++) {

                String thisItem = smallCraft.getItem(x,y);
                int count = smallCraft.getCount(x,y);
                if (thisItem != null) {
                    for (int i = 0; i < count; i++) {
                        /*
                        createItem(thisItem,
                                getPlayerPosWithEyeHeightX(),getPlayerPosWithEyeHeightY(),getPlayerPosWithEyeHeightZ(),
                                (getCameraRotationVectorX()*10f) + getPlayerInertiaX(),(getCameraRotationVectorY()*10f) + getPlayerInertiaY(), (getCameraRotationVectorZ()*10f) + getPlayerInertiaZ()
                                , 1, 0);
                         */
                    }
                }
            }
        }
        smallCraft.clear();
    }

    public String getMouseInventory(){
        return mouseInventory;
    }
    public int getMouseCount(){
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
                /*
                createItem(mouseInventory,
                        getPlayerPosWithEyeHeightX(),getPlayerPosWithEyeHeightY(),getPlayerPosWithEyeHeightZ(),
                        (getCameraRotationVectorX()*10f) + getPlayerInertiaX(),(getCameraRotationVectorY()*10f) + getPlayerInertiaY(), (getCameraRotationVectorZ()*10f) + getPlayerInertiaZ()
                        , 1, 0);
                 */
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

    public void clearMemory(){
        armor.clear();
        output.clear();
        smallCraft.clear();
        bigCraft.clear();
        main.clear();
    }
}
