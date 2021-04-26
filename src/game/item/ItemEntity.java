package game.item;

import org.joml.Vector3f;

import java.util.*;

import static engine.FancyMath.*;
import static engine.sound.SoundAPI.playSound;
import static game.collision.Collision.applyInertia;
import static game.item.Item.getCurrentID;
import static game.player.Inventory.addItemToInventory;
import static game.player.Player.getPlayerPosWithCollectionHeight;

public class ItemEntity {
    private final static Map<Integer, Item> items = new HashMap<>();
    private final static float itemSize = 0.4f;

    public static void createItem(String name, Vector3f pos, int stack){
        items.put(getCurrentID(), new Item(name, pos, stack));
    }


    public static void createItem(String name, Vector3f pos, Vector3f inertia, int stack){
        items.put(getCurrentID(), new Item(name, pos, inertia, stack));
    }

    public static Collection<Item> getAllItems(){
        return items.values();
    }

    private static final Deque<Integer> deletionQueue = new ArrayDeque<>();

    public static void onStep(){
        for (Item thisItem : items.values()){
            thisItem.timer += 0.001f;

            //delete items that are too old
            if (thisItem.timer > 10f){
                deletionQueue.add(thisItem.ID);
                continue;
            }

            if (thisItem.timer > 3){
                if (getDistance(thisItem.pos, getPlayerPosWithCollectionHeight()) < 3f){
                    if (!thisItem.collecting){
                        if (addItemToInventory(thisItem.name)) {
                            playSound("pickup");
                            thisItem.collecting = true;
                        }
                    }
                    if (thisItem.collecting) {
                        Vector3f normalizedPos = new Vector3f(getPlayerPosWithCollectionHeight());
                        normalizedPos.sub(thisItem.pos).normalize().mul(15f);
                        thisItem.inertia = normalizedPos;
                    }
                }

                if (getDistance(thisItem.pos, getPlayerPosWithCollectionHeight()) < 0.2f){
                    deletionQueue.add(thisItem.ID);
                    continue;
                }
            }

            if (thisItem.collecting) {
                applyInertia(thisItem.pos, thisItem.inertia, false, itemSize, itemSize * 2, false, false, false, false);
            } else {
                applyInertia(thisItem.pos, thisItem.inertia, false, itemSize, itemSize * 2, true, false, true, false);
            }

            thisItem.rotation.y += 0.1f;

            if (thisItem.floatUp){
                thisItem.hover += 0.00025f;
                if (thisItem.hover >= 0.5f){
                    thisItem.floatUp = false;
                }
            } else {
                thisItem.hover -= 0.00025f;
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

    public static void clearItems(){
        items.clear();
    }
}