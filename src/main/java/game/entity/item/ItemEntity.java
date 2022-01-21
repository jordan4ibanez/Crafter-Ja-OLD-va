package game.entity.item;

import it.unimi.dsi.fastutil.ints.*;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayDeque;
import java.util.Deque;

import engine.FancyMath.randomForceValue;
import engine.sound.SoundAPI.playSound;
import engine.time.Delta.getDelta;
import game.chunk.Chunk.getLight;
import game.entity.collision.Collision.applyInertia;
import game.crafting.InventoryObject.addToInventory;
import game.player.Player.getPlayerPosWithCollectionHeight;

final public class ItemEntity {

    private final float itemCollisionWidth = 0.2f;

    private int currentID = 0;

    private final Int2ObjectOpenHashMap<String> name = new Int2ObjectOpenHashMap<>();
    private final Int2IntOpenHashMap stack = new Int2IntOpenHashMap();
    private final Int2ObjectOpenHashMap<Vector3d> pos = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<Vector3d> goalPos = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<Vector3f> inertia = new Int2ObjectOpenHashMap<>();
    private final Int2FloatOpenHashMap timer = new Int2FloatOpenHashMap();
    private final Int2FloatOpenHashMap hover = new Int2FloatOpenHashMap();
    private final Int2BooleanOpenHashMap floatUp = new Int2BooleanOpenHashMap();
    private final Int2BooleanOpenHashMap collecting = new Int2BooleanOpenHashMap();
    private final Int2FloatOpenHashMap collectionTimer = new Int2FloatOpenHashMap();
    private final Int2BooleanOpenHashMap deletionOkay = new Int2BooleanOpenHashMap();
    private final Int2FloatOpenHashMap rotation = new Int2FloatOpenHashMap();

    private final Int2ByteOpenHashMap light = new Int2ByteOpenHashMap();
    private final Int2FloatOpenHashMap lightUpdateTimer = new Int2FloatOpenHashMap();
    private final Int2ObjectOpenHashMap<Vector3i> oldFlooredPos = new Int2ObjectOpenHashMap<>();


    public void createItem(String newName, double posX, double posY, double posZ, float inertiaX, float inertiaY, float inertiaZ, int newStack, float newTimer) {
        name.put(currentID, newName);
        stack.put(currentID, newStack);
        pos.put(currentID, new Vector3d(posX, posY, posZ));
        goalPos.put(currentID, new Vector3d(posX, posY, posZ));
        inertia.put(currentID, new Vector3f(inertiaX,inertiaY,inertiaZ));
        timer.put(currentID,newTimer);
        hover.put(currentID,0);
        floatUp.put(currentID, true);
        collecting.put(currentID,false);
        collectionTimer.put(currentID,0);
        deletionOkay.put(currentID, false);
        rotation.put(currentID, 0);
        light.put(currentID, (byte) 15);
        lightUpdateTimer.put(currentID,1f);
        oldFlooredPos.put(currentID, new Vector3i(0,0,0));

        tickUpCurrentID();
    }

    public void throwItem(String newName, double posX, double posY, double posZ, int newStack, float newTimer){
        name.put(currentID, newName);
        stack.put(currentID, newStack);
        pos.put(currentID, new Vector3d(posX, posY, posZ));
        goalPos.put(currentID, new Vector3d(posX, posY, posZ));
        inertia.put(currentID, new Vector3f(randomForceValue(2f), (float) java.lang.Math.random() * 4f, randomForceValue(2f)));
        timer.put(currentID,newTimer);
        hover.put(currentID,0);
        floatUp.put(currentID, true);
        collecting.put(currentID,false);
        collectionTimer.put(currentID,0);
        deletionOkay.put(currentID, false);
        rotation.put(currentID, 0);
        light.put(currentID, (byte) 15);
        lightUpdateTimer.put(currentID,1f);
        oldFlooredPos.put(currentID, new Vector3i(0,0,0));

        tickUpCurrentID();
    }

    //internal automatic integer overflow to 0
    private void tickUpCurrentID(){
        currentID++;
        if (currentID == 2147483647){
            currentID = 0;
        }
    }

    public IntSet getAllItems() {
        return name.keySet();
    }


    //public boolean itemKeyExists(int ID) {
        //return name.containsKey(ID);
    //}

    //private final Deque<ItemSendingObject> addingUpdatingList = new ArrayDeque<>();

    /*
    //probably receives items from the multiplayer server then puts them in memory
    public void processQueuedItemsToBeAddedInMultiplayer() {
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


    //a true mystery
    public void addItemToQueueToBeUpdated(ItemSendingObject itemSendingObject){
        addingUpdatingList.add(itemSendingObject);
    }
     */

    private final Deque<Integer> deletionQueue = new ArrayDeque<>();

    public void deleteItem(int ID){
        deletionQueue.add(ID);
    }

    private void removeItem(int ID){
        name.remove(ID);
        stack.remove(ID);
        pos.remove(ID);
        goalPos.remove(ID);
        inertia.remove(ID);
        timer.remove(ID);
        hover.remove(ID);
        floatUp.remove(ID);
        collecting.remove(ID);
        collectionTimer.remove(ID);
        deletionOkay.remove(ID);
        rotation.remove(ID);
        light.remove(ID);
        lightUpdateTimer.remove(ID);
        oldFlooredPos.remove(ID);
    }

    //immutable
    public double getItemPosX(int ID){
        return pos.get(ID).x;
    }
    //immutable
    public double getItemPosY(int ID){
        return pos.get(ID).y;
    }
    //immutable
    public double getItemPosZ(int ID){
        return pos.get(ID).z;
    }

    //immutable - special case for renderer
    public double getItemPosYWithHover(int ID){
        return pos.get(ID).y + hover.get(ID);
    }

    //immutable
    public float getItemHover(int ID){
        return hover.get(ID);
    }

    //immutable
    public float getItemRotation(int ID){
        return rotation.get(ID);
    }

    //immutable
    public float getItemLight(int ID){
        return light.get(ID);
    }

    //mutable - be careful with this
    public String getItemName(int ID){
        return name.get(ID);
    }


    final private Vector3i currentFlooredPos = new Vector3i();
    final private Vector3d normalizedPos = new Vector3d();
    final private Vector3d currentPos = new Vector3d();

    public void itemsOnTick(){
        double delta = getDelta();

        for (int thisKey : name.keySet()){

            float thisCollectionTimer = collectionTimer.get(thisKey);

            if (thisCollectionTimer > 0f){

                thisCollectionTimer -= delta;

                collectionTimer.put(thisKey, thisCollectionTimer);

                if (thisCollectionTimer <= 0){
                    deletionOkay.put(thisKey,true);
                }
            }


            float thisTimer = timer.get(thisKey);
            float thisLightUpdateTimer = lightUpdateTimer.get(thisKey);

            thisTimer += delta;
            thisLightUpdateTimer += delta;

            timer.put(thisKey,thisTimer);

            currentPos.set(pos.get(thisKey));
            currentFlooredPos.set((int) Math.floor(currentPos.x), (int)Math.floor(currentPos.y), (int)Math.floor(currentPos.z));

            //poll local light every quarter second
            if (thisLightUpdateTimer >= 0.25f || !currentFlooredPos.equals(oldFlooredPos.get(thisKey))){

                light.put(thisKey, getLight(currentFlooredPos.x, currentFlooredPos.y, currentFlooredPos.z));

                thisLightUpdateTimer = 0f;
            }

            lightUpdateTimer.put(thisKey,thisLightUpdateTimer);

            oldFlooredPos.get(thisKey).set(currentFlooredPos);

            //delete items that are too old
            if (thisTimer > 50f){
                deletionQueue.add(thisKey);
            }

            boolean thisCollecting = collecting.get(thisKey);
            boolean oldCollecting = thisCollecting;

            //collect items after 3 seconds
            if (thisTimer > 3f){
                if (currentPos.distance(getPlayerPosWithCollectionHeight()) < 3f){
                    if (!thisCollecting){
                        if (addToInventory("main", name.get(thisKey))) {
                            playSound("pickup");
                            thisCollecting = true;
                            collectionTimer.put(thisKey,0.1f);
                        }
                        //an extreme edge case so a completely full inventory does not
                        //hammer the player's RAM
                        else {
                            timer.put(thisKey, 2);
                        }
                    }
                    //do not do else-if here, can go straight to this logic
                    if (thisCollecting) {
                        normalizedPos.set(getPlayerPosWithCollectionHeight().sub(currentPos).normalize().mul(15f));
                        inertia.get(thisKey).set((float)normalizedPos.x,(float)normalizedPos.y,(float)normalizedPos.z);
                    }
                }

                if (currentPos.distance(getPlayerPosWithCollectionHeight()) < 0.2f || deletionOkay.get(thisKey)){
                    deletionQueue.add(thisKey);
                }
            }

            if (thisCollecting != oldCollecting){
                collecting.put(thisKey,thisCollecting);
            }

            if (thisCollecting) {
                applyInertia(pos.get(thisKey), inertia.get(thisKey), false, itemCollisionWidth, itemCollisionWidth, false, false, false, false, false);
            } else {
                applyInertia(pos.get(thisKey), inertia.get(thisKey), false, itemCollisionWidth, itemCollisionWidth, true, false, true, false, false);
            }

            float thisRotation = rotation.get(thisKey);

            thisRotation += delta * 50;

            if (thisRotation > 360f) {
                thisRotation -= 360f;
            }

            boolean thisFloatUp = floatUp.get(thisKey);
            boolean oldFloatUp = thisFloatUp;

            float thisHover = hover.get(thisKey);

            if (thisFloatUp){
                thisHover += delta / 10;
                if (thisHover >= 0.5f){
                    thisFloatUp = false;
                }
            } else {
                thisHover -= delta / 10;
                if (thisHover <= 0.0f){
                    thisFloatUp = true;
                }
            }

            if (thisFloatUp != oldFloatUp){
                floatUp.put(thisKey, thisFloatUp);
            }

            hover.put(thisKey,thisHover);

            rotation.put(thisKey,thisRotation);

            if (currentPos.y < 0){
                deletionQueue.add(thisKey);
            }
        }

        while (!deletionQueue.isEmpty()){
            int thisItemKey = deletionQueue.pop();
            removeItem(thisItemKey);
        }
    }

    /*
    public void itemsOnTickMultiplayer(){
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
     */

    private final Deque<String> itemsAddingQueue = new ArrayDeque<>();

    public void addItemToCollectionQueue(String item){
        itemsAddingQueue.add(item);
    }

    public void popItemsAddingQueue(){
        if (!itemsAddingQueue.isEmpty()) {
            String newItem = itemsAddingQueue.pop();
            if (addToInventory("main", newItem)) {
                playSound("pickup");
            }
        }
    }

    public void cleanItemMemory(){
        name.clear();
        stack.clear();
        pos.clear();
        goalPos.clear();
        inertia.clear();
        timer.clear();
        hover.clear();
        floatUp.clear();
        collecting.clear();
        collectionTimer.clear();
        deletionOkay.clear();
        rotation.clear();
    }
}