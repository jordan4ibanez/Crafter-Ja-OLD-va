package game.item;

import engine.network.ItemSendingObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;

import static engine.time.Time.getDelta;
import static engine.sound.SoundAPI.playSound;
import static game.chunk.Chunk.getLight;
import static game.collision.Collision.applyInertia;
import static game.crafting.Inventory.addItemToInventory;
import static game.item.Item.getCurrentID;
import static game.player.Player.getPlayerPos;
import static game.player.Player.getPlayerPosWithCollectionHeight;

public class ItemEntity {
    private final static Int2ObjectArrayMap<Item> items = new Int2ObjectArrayMap<>();

    private final static float itemCollisionWidth = 0.2f;

    public static void cleanItemMemory(){
        items.clear();
    }


    public static void createItem(String name, double posX, double posY, double posZ, float inertiaX, float inertiaY, float inertiaZ, int stack, float timer) {
        items.put(getCurrentID(), new Item(name, posX, posY, posZ, inertiaX, inertiaY, inertiaZ, stack, timer));
    }


    public static Object[] getAllItems() {
        return items.values().toArray();
    }


    public static boolean itemKeyExists(int ID) {
        return items.containsKey(ID);
    }

    private static final Deque<ItemSendingObject> addingUpdatingList = new ArrayDeque<>();

    public static void processQueuedItemsToBeAddedInMultiplayer() {
        while (!addingUpdatingList.isEmpty()) {
            ItemSendingObject itemSendingObject = addingUpdatingList.pop();

            if (itemKeyExists(itemSendingObject.ID)) {
                Item thisEntity = items.get(itemSendingObject.ID);
                thisEntity.goalPos.set(itemSendingObject.pos);
            } else {
                //initial internal pos
                items.put(itemSendingObject.ID, new Item(itemSendingObject.name, itemSendingObject.pos.x, itemSendingObject.pos.y, itemSendingObject.pos.z, 0,0,0, 1, 0));
            }
        }
    }

    public static void addItemToQueueToBeUpdated(ItemSendingObject itemSendingObject){
        addingUpdatingList.add(itemSendingObject);
    }

    private static final Deque<Integer> deletionQueue = new ArrayDeque<>();

    public static void deleteItem(int ID){
        deletionQueue.add(ID);
    }

    final private static Vector3i currentFlooredPos = new Vector3i();
    final private static Vector3d normalizedPos = new Vector3d();

    public static void itemsOnTick(){
        double delta = getDelta();

        for (Item thisItem : items.values()){

            if (thisItem.collectionTimer > 0f){

                thisItem.collectionTimer -= delta;
                if (thisItem.collectionTimer <= 0){
                    thisItem.deletionOkay = true;
                }
            }

            thisItem.timer += delta;
            thisItem.lightUpdateTimer += delta;

            currentFlooredPos.set((int) Math.floor(thisItem.pos.x), (int)Math.floor(thisItem.pos.y), (int)Math.floor(thisItem.pos.z));

            //poll local light every quarter second
            if (thisItem.lightUpdateTimer >= 0.25f || !currentFlooredPos.equals(thisItem.oldFlooredPos)){

                thisItem.light = getLight(currentFlooredPos.x, currentFlooredPos.y, currentFlooredPos.z);

                thisItem.lightUpdateTimer = 0f;
            }

            thisItem.oldFlooredPos.set(currentFlooredPos);

            //delete items that are too old
            if (thisItem.timer > 50f){
                deletionQueue.add(thisItem.ID);
            }

            //collect items after 3 seconds
            if (thisItem.timer > 3f){
                if (thisItem.pos.distance(getPlayerPosWithCollectionHeight()) < 3f){
                    if (!thisItem.collecting){
                        if (addItemToInventory(thisItem.name)) {
                            playSound("pickup");
                            thisItem.collecting = true;
                            thisItem.collectionTimer = 0.1f;
                        }
                    }
                    //do not do else-if here, can go straight to this logic
                    if (thisItem.collecting) {
                        normalizedPos.set(getPlayerPosWithCollectionHeight().sub(thisItem.pos).normalize().mul(15f));
                        thisItem.inertia.set((float)normalizedPos.x,(float)normalizedPos.y,(float)normalizedPos.z);
                    }
                }

                if (thisItem.pos.distance(getPlayerPosWithCollectionHeight()) < 0.2f || thisItem.deletionOkay){
                    deletionQueue.add(thisItem.ID);
                }
            }

            if (thisItem.collecting) {
                applyInertia(thisItem.pos, thisItem.inertia, false, itemCollisionWidth, itemCollisionWidth, false, false, false, false, false);
            } else {
                applyInertia(thisItem.pos, thisItem.inertia, false, itemCollisionWidth, itemCollisionWidth, true, false, true, false, false);
            }

            thisItem.rotation.y += delta * 50;

            if (thisItem.rotation.y > 360f) {
                thisItem.rotation.y -= 360f;
            }

            if (thisItem.floatUp){
                thisItem.hover += delta / 10;
                if (thisItem.hover >= 0.5f){
                    thisItem.floatUp = false;
                }
            } else {
                thisItem.hover -= delta / 10;
                if (thisItem.hover <= 0.0f){
                    thisItem.floatUp = true;
                }
            }

            if (thisItem.pos.y < 0){
                deletionQueue.add(thisItem.ID);
            }
        }

        while (!deletionQueue.isEmpty()){
            int thisItemKey = deletionQueue.pop();
            items.remove(thisItemKey);
        }
    }

    public static void itemsOnTickMultiplayer(){
        double delta = getDelta();
        for (Item thisItem : items.values()){

            thisItem.timer += delta;

            //interpolate position to goal position
            //the second argument is the smoothing factor - higher is choppier but more accurate
            thisItem.pos.lerp(thisItem.goalPos, delta * 50d, thisItem.pos);

            //client side deletion
            if (thisItem.timer > 50){
                deletionQueue.add(thisItem.ID);
            }

            //client side deletion if too far
            if (thisItem.pos.distance(getPlayerPos()) > 15f){
                deletionQueue.add(thisItem.ID);
            }




            thisItem.lightUpdateTimer += delta;

            Vector3i currentFlooredPos = new Vector3i((int)Math.floor(thisItem.pos.x), (int)Math.floor(thisItem.pos.y), (int)Math.floor(thisItem.pos.z));

            //poll local light every quarter second
            if (thisItem.lightUpdateTimer >= 0.25f || !currentFlooredPos.equals(thisItem.oldFlooredPos)){
                thisItem.light = getLight(currentFlooredPos.x, currentFlooredPos.y, currentFlooredPos.z);
                thisItem.lightUpdateTimer = 0f;
            }

            thisItem.rotation.y += delta * 50;

            if (thisItem.rotation.y > 360f) {
                thisItem.rotation.y -= 360f;
            }

            if (thisItem.floatUp){
                thisItem.hover += delta / 10;
                if (thisItem.hover >= 0.5f){
                    thisItem.floatUp = false;
                }
            } else {
                thisItem.hover -= delta / 10;
                if (thisItem.hover <= 0.0f){
                    thisItem.floatUp = true;
                }
            }
        }

        while (!deletionQueue.isEmpty()){
            int thisItemKey = deletionQueue.pop();
            items.remove(thisItemKey);
        }
    }

    private static final Deque<String> itemsAddingQueue = new ArrayDeque<>();

    public static void addItemToCollectionQueue(String item){
        itemsAddingQueue.add(item);
    }

    public static void popItemsAddingQueue(){
        if (!itemsAddingQueue.isEmpty()) {
            String newItem = itemsAddingQueue.pop();
            if (addItemToInventory(newItem)) {
                playSound("pickup");
            }
        }
    }
}