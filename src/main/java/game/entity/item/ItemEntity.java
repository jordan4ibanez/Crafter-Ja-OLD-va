package game.entity.item;

import engine.time.Delta;
import game.chunk.Chunk;
import game.entity.Entity;
import game.entity.EntityContainer;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class ItemEntity extends Entity {

    final private Vector3d normalizedPos = new Vector3d();

    private final String itemName;
    private int stack;

    private float hover = 0f;
    private boolean floatUp = true;
    private boolean collecting = false;
    private float collectionTimer = 0f;
    private boolean deletionOkay = false;
    private float rotation = 0f;


    public ItemEntity(Chunk chunk, EntityContainer entityContainer, Vector3d pos, Vector3f inertia, String itemName, int stack) {
        super(chunk, entityContainer, pos, inertia, true, false);
        this.itemName = itemName;
        this.stack = stack;
    }

    public float getHover(){
        return hover;
    }

    public float getRotation(){
        return rotation;
    }

    public String getItem(){
        return itemName;
    }



    @Override
    public void onTick(Entity entity, Delta delta) {

        super.onTick(entity);

        double dtime = delta.getDelta();


        if (collectionTimer > 0f){

            collectionTimer -= dtime;

            if (collectionTimer <= 0){
                this.delete();
                return;
            }
        }

        float timer = this.getTimer();
        timer += dtime;
        this.setTimer(timer);


        //delete items that are too old
        if (timer > 50f){
            this.delete();
            return;
        }

        boolean thisCollecting = this.collecting;
        boolean oldCollecting = thisCollecting;

        //collect items after 3 seconds
        if (timer > 3f){
            if (this.getPos().distance(player.getPlayerPosWithCollectionHeight()) < 3f){
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

        float itemCollisionWidth = 0.2f;
        if (thisCollecting) {
            applyInertia(pos.get(thisKey), inertia.get(thisKey), false, itemCollisionWidth, itemCollisionWidth, false, false, false, false, false);
        } else {
            applyInertia(pos.get(thisKey), inertia.get(thisKey), false, itemCollisionWidth, itemCollisionWidth, true, false, true, false, false);
        }

        float thisRotation = rotation.get(thisKey);

        thisRotation += dtime * 50;

        if (thisRotation > 360f) {
            thisRotation -= 360f;
        }

        boolean thisFloatUp = floatUp.get(thisKey);
        boolean oldFloatUp = thisFloatUp;

        float thisHover = hover.get(thisKey);

        if (thisFloatUp){
            thisHover += dtime / 10;
            if (thisHover >= 0.5f){
                thisFloatUp = false;
            }
        } else {
            thisHover -= dtime / 10;
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





    public void popItemsAddingQueue(){
        if (addToInventory("main", newItem)) {
            playSound("pickup");
        }
    }

}