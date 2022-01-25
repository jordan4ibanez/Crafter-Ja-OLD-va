package game.entity.mob;

import engine.time.Delta;
import game.chunk.Chunk;
import game.entity.Entity;
import game.entity.EntityContainer;
import game.player.Player;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class Mob extends Entity {

    private float hurtTimer = 0f;
    private float deathRotation = 0f;
    private float deathTimer = 0f;

    private int health;
    private int hurtAdder = 0;

    public Mob(EntityContainer entityContainer, Vector3d pos, Vector3f inertia, float width, float height, int health) {
        super(entityContainer, pos, inertia, width, height, false, true, false);
        this.health = health;
    }

    public int getHurtAdder(){
        return hurtAdder;
    }

    public int getHealth(){
        return health;
    }


    public void onTick(Chunk chunk, Delta delta, Player player){
        super.onTick(chunk, delta);
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

        mobSmoothRotation(thisMob);
        doHeadCode(thisMob);
    }


    public void hurt(int damage){
        this.health -= damage;
        this.hurtAdder = 15;
        this.hurtTimer = 0.5f;
    }
}
