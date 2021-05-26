package game.item;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;

import static engine.FancyMath.getDistance;
import static engine.Time.getDelta;
import static engine.sound.SoundAPI.playSound;
import static game.chunk.Chunk.getLight;
import static game.collision.Collision.applyInertia;
import static game.crafting.Inventory.addItemToInventory;
import static game.item.Item.getCurrentID;
import static game.player.Player.getPlayerPosWithCollectionHeight;

public class ItemEntity {
    private final static Map<Integer, Item> items = new HashMap<>();

    private final static float itemCollisionWidth = 0.2f;

    public static void createItem(String name, Vector3d pos, int stack){
        items.put(getCurrentID(), new Item(name, pos, stack));
    }

    public static void createItem(String name, Vector3d pos, int stack, float life){
        items.put(getCurrentID(), new Item(name, pos, stack, life));
    }

    public static void createItem(String name, Vector3d pos, Vector3f inertia, int stack){
        items.put(getCurrentID(), new Item(name, pos, inertia, stack));
    }

    public static void createItem(String name, Vector3d pos, Vector3f inertia, int stack, float life){
        items.put(getCurrentID(), new Item(name, pos, inertia, stack, life));
    }

    public static Collection<Item> getAllItems(){
        return items.values();
    }

    private static final Deque<Integer> deletionQueue = new ArrayDeque<>();

    public static void onStep(){
        float delta = getDelta();
        for (Item thisItem : items.values()){

            if (thisItem.collectionTimer > 0f){

                thisItem.collectionTimer -= delta;
                if (thisItem.collectionTimer <= 0){
                    thisItem.deletionOkay = true;
                }
            }

            thisItem.timer += delta;
            thisItem.lightUpdateTimer += delta;

            Vector3i currentFlooredPos = new Vector3i((int)Math.floor(thisItem.pos.x), (int)Math.floor(thisItem.pos.y), (int)Math.floor(thisItem.pos.z));

            //poll local light every half second
            if (thisItem.lightUpdateTimer >= 0.25f || !currentFlooredPos.equals(thisItem.oldFlooredPos)){

                byte newLight = getLight(currentFlooredPos.x, currentFlooredPos.y, currentFlooredPos.z);

                //don't do extra work if nothing changed
                if (newLight != thisItem.light){
                    thisItem.light = newLight;
                    //System.out.println("rebuild light mesh");
                    thisItem.rebuildLightMesh(thisItem);
                }

                thisItem.lightUpdateTimer = 0f;
            }

            thisItem.oldFlooredPos = currentFlooredPos;

            //delete items that are too old
            if (thisItem.timer > 50f){
                deletionQueue.add(thisItem.ID);
            }

            //collect items after 3 seconds
            if (thisItem.timer > 3f){
                if (getDistance(thisItem.pos, getPlayerPosWithCollectionHeight()) < 3f){
                    if (!thisItem.collecting){
                        if (addItemToInventory(thisItem.name)) {
                            playSound("pickup");
                            thisItem.collecting = true;
                            thisItem.collectionTimer = 0.1f;
                        }
                    }
                    //do not do else-if here, can go straight to this logic
                    if (thisItem.collecting) {
                        Vector3d normalizedPos = new Vector3d(getPlayerPosWithCollectionHeight());
                        normalizedPos.sub(thisItem.pos).normalize().mul(15f);

                        Vector3f normalizedDirection = new Vector3f();
                        normalizedDirection.x = (float)normalizedPos.x;
                        normalizedDirection.y = (float)normalizedPos.y;
                        normalizedDirection.z = (float)normalizedPos.z;

                        thisItem.inertia = normalizedDirection;
                    }
                }

                if (getDistance(thisItem.pos, getPlayerPosWithCollectionHeight()) < 0.2f || thisItem.deletionOkay){
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
            Item thisItem = items.get(thisItemKey);
            if (thisItem != null && thisItem.mesh != null){
                thisItem.mesh.cleanUp(false);
            }
            items.remove(thisItemKey);
        }
    }

    public static void clearItems(){
        items.clear();
    }
}