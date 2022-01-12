package game.mob;


import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.ArrayDeque;
import java.util.Deque;

import static engine.sound.SoundAPI.playSound;
import static engine.time.Time.getDelta;
import static game.chunk.Chunk.getLight;
import static game.collision.MobCollision.mobSoftCollisionDetect;
import static game.collision.MobCollision.mobSoftPlayerCollisionDetect;
import static game.mob.MobDefinition.*;
import static game.mob.MobObject.*;

//runs on main thread
final public class Mob {


    private static final Deque<Integer> deletionQueue = new ArrayDeque<>();

    public static void mobsOnTick(){

        double delta = getDelta();

        for (int thisMob : getMobKeys()){

            //this is the mob definition ID
            int thisMobID = getMobID(thisMob);

            float thisMobWidth = getMobWidth(thisMobID);
            float thisMobHeight = getMobHeight(thisMobID);

            //this is an object pointer
            Vector3d thisMobPos = getMobPos(thisMob);

            //only collision detect if alive
            if (getMobHealth(thisMob) > 0) {
                mobSoftPlayerCollisionDetect(thisMob, thisMobPos, thisMobHeight, thisMobWidth);
                mobSoftCollisionDetect(thisMob, thisMobPos, thisMobHeight, thisMobWidth);
            }

            //interface consumes object - no need for re-assignment to vars
            //todo: this needs a complete reimplementation 
            getMobInterface(thisMobID).onTick(thisMob);

            if (thisMob.pos.y < 0){
                deletionQueue.add(thisMob.globalID);
                continue;
            }

            //mob dying animation
            if (thisMob.health <= 0 && thisMob.deathRotation < 90){
                thisMob.deathRotation += delta * 300f;
                if (thisMob.deathRotation >= 90){
                    thisMob.deathRotation = 90;
                    thisMob.timer = 0f;
                }
            }

            Vector3i currentFlooredPos = new Vector3i((int)Math.floor(thisMob.pos.x), (int)Math.floor(thisMob.pos.y), (int)Math.floor(thisMob.pos.z));

            //poll local light every quarter second
            if (thisMob.lightTimer >= 0.25f || !currentFlooredPos.equals(thisMob.oldFlooredPos)){
                thisMob.light = getLight(currentFlooredPos.x, currentFlooredPos.y, currentFlooredPos.z);
                thisMob.lightTimer = 0f;
            }

            if (thisMob.health <= 0 && thisMob.timer >= 0.5f && thisMob.deathRotation == 90){
                deletionQueue.add(thisMob.globalID);
            }

            //count down hurt timer
            if(thisMob.hurtTimer > 0f && thisMob.health > 0){ //don't reset when dead
                thisMob.hurtTimer -= delta;
                if (thisMob.hurtTimer <= 0){
                    thisMob.hurtTimer = 0;
                    thisMob.hurtAdder = 0; //reset red coloring
                }
            }

            thisMob.oldFlooredPos.set(currentFlooredPos);
            thisMob.oldPos.set(thisMob.pos);
        }

        while (!deletionQueue.isEmpty()){
            int thisMobGlobalID = deletionQueue.pop();
            //System.out.println("mob " + thisMobGlobalID + " was deleted!");
            mobs.remove(thisMobGlobalID);
            globalMobs--;
        }
    }


    public static void punchMob(MobObject thisMob){
        if (thisMob.hurtTimer <= 0 && thisMob.health > 0) {
            thisMob.health -= 1;
            //System.out.println("the mobs health is: " + thisMob.health);
            playSound(getHurtSound(thisMob.ID),(float)thisMob.pos.x, (float)thisMob.pos.y, (float)thisMob.pos.z, true);
            //play this after in case ID changes
            MobInterface thisInterface = getMobInterface(thisMob.ID);
            if (thisInterface != null){
                thisInterface.onPunch(thisMob);
            }

            if (thisMob.onGround) {
                thisMob.inertia.y = 7;
            }
            thisMob.hurtAdder = 15;
            thisMob.hurtTimer = 0.5f;
        }
    }
}
