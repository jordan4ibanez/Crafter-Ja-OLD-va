package game.entity.item;

import engine.sound.SoundAPI;
import engine.time.Delta;
import game.chunk.Chunk;
import game.crafting.InventoryLogic;
import game.entity.Entity;
import game.entity.EntityContainer;
import game.entity.collision.Collision;
import game.player.Player;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class ItemEntity extends Entity {

    final private Vector3f normalizedPos = new Vector3f();

    private final String itemName;
    private int stack;

    private float hover = 0f;
    private boolean floatUp = true;
    private boolean collecting = false;
    private float collectionTimer = 0f;
    private float rotation = 0f;


    public ItemEntity(Chunk chunk, EntityContainer entityContainer, Vector3d pos, Vector3f inertia, String itemName, int stack) {
        super(chunk, entityContainer, pos, inertia, true, false);
        this.itemName = itemName;
        this.stack = stack;
    }

    public void setStack(int stack){
        this.stack = stack;
    }

    public int getStack(){
        return stack;
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
    public void onTick(Entity entity, Player player, Delta delta) {

    }

    @Override
    public void onTick(Entity entity, InventoryLogic inventoryLogic, Player player, Delta delta) {

    }

    @Override
    public void onTick(Entity entity, SoundAPI soundAPI, InventoryLogic inventoryLogic, Player player, Delta delta) {
        
    }

    @Override
    public void onTick(Collision collision, Entity entity, SoundAPI soundAPI, InventoryLogic inventoryLogic, Player player, Delta delta) {

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
                    if (inventoryLogic.getInventory().getMain().addItem(this.itemName)) {
                        soundAPI.playSound("pickup");
                        thisCollecting = true;
                        collectionTimer = 0.1f;
                    }
                    //an extreme edge case so a completely full inventory does not
                    //hammer the player's RAM
                    else {
                        collectionTimer = 2;
                    }
                }
                //do not do else-if here, can go straight to this logic
                if (thisCollecting) {
                    this.setInertia(normalizedPos.set(player.getPlayerPosWithCollectionHeight().sub(this.getPos()).normalize().mul(15f)));
                }
            }

            if (this.getPos().distance(player.getPlayerPosWithCollectionHeight()) < 0.2f){
                this.delete();
                return;
            }
        }

        if (thisCollecting != oldCollecting){
            collecting = true;
        }

        float itemCollisionWidth = 0.2f;
        if (thisCollecting) {
            collision.applyInertia(this.getPos(), this.getInertia(), false, itemCollisionWidth, itemCollisionWidth, false, false, false, false, false);
        } else {
            collision.applyInertia(this.getPos(), this.getInertia(), false, itemCollisionWidth, itemCollisionWidth, true, false, true, false, false);
        }


        rotation += dtime * 50;

        if (rotation > 360f) {
            rotation -= 360f;
        }



        if (floatUp){
            hover += dtime / 10;
            if (hover >= 0.5f){
                floatUp = false;
            }
        } else {
            hover -= dtime / 10;
            if (hover <= 0.0f){
                floatUp = true;
            }
        }

        //fallen out of the world
        if (this.getPos().y < 0){
            this.delete();
        }
    }
}