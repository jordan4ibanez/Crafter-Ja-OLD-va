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

    private static final Vector3i currentFlooredPos = new Vector3i();

    public static void mobsOnTick(){

        double delta = getDelta();

        //mob table key is int
        for (int thisMob : getMobKeys()){

            //this is the mob definition ID
            int thisMobDefinitionID = getMobID(thisMob);

            //this is an object pointer
            Vector3d thisMobPos = getMobPos(thisMob);
            Vector3i thisMobOldFlooredPos = getMobOldFlooredPos(thisMob);

            //these are immutable primitives - they need to be set after they are modified
            float thisMobTimer = getMobTimer(thisMob);
            float thisMobHurtTimer = getMobHurtTimer(thisMob);
            float thisMobLightTimer = getMobLightTimer(thisMob);
            float thisMobDeathRotation = getMobDeathRotation(thisMob);
            float thisMobWidth = getMobWidth(thisMobDefinitionID);
            float thisMobHeight = getMobHeight(thisMobDefinitionID);

            byte thisMobHealth = getMobHealth(thisMob);


            //only collision detect if alive
            if (getMobHealth(thisMob) > 0) {
                mobSoftPlayerCollisionDetect(thisMob, thisMobPos, thisMobHeight, thisMobWidth);
                mobSoftCollisionDetect(thisMob, thisMobPos, thisMobHeight, thisMobWidth);
            }

            //interface consumes object - no need for re-assignment to vars
            //todo: this needs a complete reimplementation
            getMobInterface(thisMobDefinitionID).onTick(thisMob);

            if (thisMobPos.y < 0){
                deletionQueue.add(thisMob);
                continue;
            }

            //mob dying animation
            if (thisMobHealth <= 0 && thisMobDeathRotation < 90){
                thisMobDeathRotation += delta * 300f;
                if (thisMobDeathRotation >= 90){
                    thisMobDeathRotation = 90;
                    thisMobTimer = 0f;
                }

                setMobDeathRotation(thisMob, thisMobDeathRotation);
            }

            currentFlooredPos.set((int)Math.floor(thisMobPos.x), (int)Math.floor(thisMobPos.y), (int)Math.floor(thisMobPos.z));

            //poll local light every quarter second
            if (thisMobLightTimer >= 0.25f || !currentFlooredPos.equals(thisMobOldFlooredPos)){
                setMobLight(thisMob, getLight(currentFlooredPos.x, currentFlooredPos.y, currentFlooredPos.z));
                setMobLightTimer(thisMob, 0f);
            }

            if (thisMobHealth <= 0 && thisMobTimer >= 0.5f && thisMobDeathRotation == 90){
                deletionQueue.add(thisMob);
            }

            //count down hurt timer
            if(thisMobHurtTimer > 0f && thisMobHealth > 0){ //don't reset when dead
                thisMobHurtTimer -= delta;
                if (thisMobHurtTimer <= 0){
                    thisMobHurtTimer = 0;
                    setMobHurtAdder(thisMob, (byte) 0);
                }

                setMobHurtTimer(thisMob, thisMobHurtTimer);
            }

            getMobOldFlooredPos(thisMob).set(currentFlooredPos);
            getMobOldPos(thisMob).set(thisMob);
        }

        while (!deletionQueue.isEmpty()){
            int thisMobGlobalID = deletionQueue.pop();
            //System.out.println("mob " + thisMobGlobalID + " was deleted!");
            deleteMob(thisMobGlobalID);
        }
    }


    public static void punchMob(int thisMob){

        float thisMobHurtTimer = getMobHurtTimer(thisMob);
        byte thisMobHealth = getMobHealth(thisMob);

        if (thisMobHurtTimer <= 0 && thisMobHealth > 0) {
            thisMobHealth -= 1;

            //System.out.println("the mobs health is: " + thisMob.health);

            Vector3d thisMobPos = getMobPos(thisMob);

            playSound(getMobHurtSound(thisMob),(float)thisMobPos.x, (float)thisMobPos.y, (float)thisMobPos.z, true);
            //play this after in case ID changes
            MobInterface thisInterface = getMobInterface(thisMob);
            if (thisInterface != null){
                thisInterface.onPunch(thisMob);
            }

            if (getIfMobOnGround(thisMob)) {
                getMobInertia(thisMob).y = 7;
            }

            setMobHealth(thisMob, thisMobHealth);
            setMobHurtAdder(thisMob, (byte) 15);
            setMobHurtTimer(thisMob, 0.5f);
        }
    }
}
