package game.entity.mob;

import engine.time.Delta;
import game.chunk.Chunk;
import game.entity.Entity;
import game.entity.EntityContainer;
import game.player.Player;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class Mob extends Entity {
    private final String name;
    private float hurtTimer = 0f;
    private float deathRotation = 0f;
    private float deathTimer = 0f;

    private int health;
    private int hurtAdder = 0;

    private final MobInterface mobInterface;

    public Mob(EntityContainer entityContainer, String name, MobInterface mobInterface, Vector3d pos, Vector3f inertia, float width, float height, int health) {
        super(entityContainer, pos, inertia, width, height, false, true, false);
        this.mobInterface = mobInterface;
        this.name = name;
        this.health = health;
    }

    public MobInterface getMobInterface(){
        return mobInterface;
    }

    public String getName(){
        return name;
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

    //mob utility code
    private final Vector3d headPos = new Vector3d();
    private final Vector3d headTurn = new Vector3d();
    private final Vector3d adjustedHeadPos = new Vector3d();

    public void doHeadCode(Mob mob){

        //this is a pointer object
        Vector3d thisMobPos = mob.getPos();

        //yet another pointer object
        Vector3f[] thisMobBodyOffsets = mob.getBodyOffsets();

        //look another pointer object
        Vector3f[] thisMobBodyRotations = MobObject.getMobBodyRotations(thisMob);

        float thisMobSmoothRotation = MobObject.getMobSmoothRotation(thisMob);

        float smoothToRad = Math.toRadians(thisMobSmoothRotation + 90f);

        //silly head turning
        headPos.set(thisMobPos.x, thisMobPos.y, thisMobPos.z);
        adjustedHeadPos.set(Math.cos(-smoothToRad), 0,Math.sin(smoothToRad));
        adjustedHeadPos.mul(thisMobBodyOffsets[0].z).add(0,thisMobBodyOffsets[0].y,0);
        headPos.add(adjustedHeadPos);

        //check if the mob can actual "see" the player
        if (!getLineOfSight(headPos, getPlayerPosWithEyeHeight())){
            return;
        }

        //this is debug code for creating a new mob
        //createParticle(headPos.x, headPos.y, headPos.z, 0.f,0.f,0.f, (byte) 7); //debug

        headTurn.set(getPlayerPosWithEyeHeight()).sub(headPos);
        //headTurn.normalize();

        float headYaw = (float) Math.toDegrees(Math.atan2(headTurn.z, headTurn.x)) + 90 - thisMobSmoothRotation;
        float pitch = (float)Math.toDegrees(Math.atan2(Math.sqrt(headTurn.z * headTurn.z + headTurn.x * headTurn.x), headTurn.y) + (Math.PI * 1.5));

        //correction of degrees overflow (-piToDegrees to piToDegrees) so it is workable
        if (headYaw < -180) {
            headYaw += 360;
        } else if (headYaw > 180){
            headYaw -= 360;
        }

        //a temporary reset, looks creepy
        if (headYaw > 90 || headYaw < -90){
            headYaw = 0;
            pitch = 0;
        }

        //weird OOP application
        thisMobBodyRotations[0].set(pitch,headYaw,0);
    }


    //todo: shortest distance
    public void mobSmoothRotation(Mob mob, Delta delta){
        double dtime = delta.getDelta();

        float thisMobRotation = mob.getRotation(mob);
        float thisMobSmoothRotation = MobObject.getMobSmoothRotation(mob);

        float diff = thisMobRotation - thisMobSmoothRotation;

        //correction of degrees overflow (-piToDegrees to piToDegrees) so it is workable
        if (diff < -180) {
            diff += 360;
        } else if (diff > 180){
            diff -= 360;
        }

        /*
        this is basically brute force inversion to correct the yaw
        addition and make the mob move to the shortest rotation
        vector possible
         */

        if (Math.abs(diff) < dtime * 500f){
            thisMobSmoothRotation = thisMobRotation;
        } else {
            if (Math.abs(diff) > 180) {
                if (diff < 0) {
                    thisMobSmoothRotation += dtime * 500f;
                } else if (diff > 0) {
                    thisMobSmoothRotation -= dtime * 500f;
                }

                //correction of degrees overflow (-piToDegrees to piToDegrees) so it is workable
                if (thisMobSmoothRotation < -180) {
                    thisMobSmoothRotation += 360;
                } else if (thisMobSmoothRotation > 180) {
                    thisMobSmoothRotation -= 360;
                }

            } else {
                if (diff < 0) {
                    thisMobSmoothRotation -= dtime * 500f;
                } else if (diff > 0) {
                    thisMobSmoothRotation += dtime * 500f;
                }
            }
        }

        MobObject.setMobSmoothRotation(mob, thisMobSmoothRotation);
    }
}
