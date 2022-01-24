package game.entity.mob;

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

public class Mob extends Entity {

    private float hurtTimer = 0f;
    private float deathRotation = 0f;
    private float deathTimer = 0f;
    private final float width;
    private final float height;
    private int health;
    private int hurtAdder = 0;

    public Mob(Chunk chunk, EntityContainer entityContainer, Vector3d pos, Vector3f inertia, float width, float height, int health) {
        super(chunk, entityContainer, pos, inertia, false, true, false);
        this.height = height;
        this.width = width;
        this.health = health;
    }

    public int getHurtAdder(){
        return hurtAdder;
    }

    public int getHealth(){
        return health;
    }

    public float getWidth(){
        return width;
    }

    public float getHeight(){
        return height;
    }

    public void onTick(Delta delta, Player player){

        super.onTick(this, delta, player);
        double dtime = delta.getDelta();

        /*
        if (getMobHealth(thisMob) > 0) {
            mobSoftPlayerCollisionDetect(thisMob, thisMobPos, thisMobHeight, thisMobWidth);
            mobSoftCollisionDetect(thisMob, thisMobPos, thisMobHeight, thisMobWidth);
        }
         */

        //fallen out of world
        if (this.getPos().y < 0){
            this.delete();
            return;
        }

        //mob is now dead
        if (health <= 0){
            //mob dying animation
            if (deathRotation < 90) {
                //System.out.println(thisMobDeathRotation);
                deathRotation += dtime * 300f;
                if (deathRotation >= 90) {
                    deathRotation = 90;
                }
            //mob will now sit there for a second
            } else {
                deathTimer += dtime;
            }
        }

        if (health <= 0 && deathTimer >= 0.5f){
            this.delete();
            return;
        }

        //count down hurt timer
        if(hurtTimer > 0f && health > 0){
            hurtTimer -= dtime;
            if (hurtTimer <= 0){
                hurtTimer = 0;

                hurtAdder = 0;
            }
        }
    }


    @Override
    public void hurt(int damage){
        this.health -= damage;
        this.hurtAdder = 15;
        this.hurtTimer = 0.5f;
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

    }
}
