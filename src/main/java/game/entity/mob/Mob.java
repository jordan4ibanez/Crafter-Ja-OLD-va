package game.entity.mob;


import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.ArrayDeque;
import java.util.Deque;

import engine.sound.SoundAPI.playSound;
import engine.time.Delta.getDelta;
import game.chunk.Chunk.getLight;
import game.entity.collision.MobCollision.mobSoftCollisionDetect;
import game.entity.collision.MobCollision.mobSoftPlayerCollisionDetect;
import game.entity.mob.MobDefinition.*;
import game.entity.mob.MobObject.*;

//runs on main thread
final public class Mob {


    private final Deque<Integer> deletionQueue = new ArrayDeque<>();

    private final Vector3i currentFlooredPos = new Vector3i();

    public void mobsOnTick(){

        double delta = getDelta();

        //mob table key is int
        for (int thisMob : getMobKeys()){

            //this is the mob definition ID
            int thisMobDefinitionID = getMobID(thisMob);

            //these are object pointers
            Vector3d thisMobPos = getMobPos(thisMob);
            Vector3i thisMobOldFlooredPos = getMobOldFlooredPos(thisMob);

            //these are immutable primitives - they need to be set after they are modified
            float thisMobHurtTimer = getMobHurtTimer(thisMob);
            float thisMobLightTimer = getMobLightTimer(thisMob);
            float thisMobDeathRotation = getMobDeathRotation(thisMob);
            float thisMobWidth = getMobDefinitionWidth(thisMobDefinitionID);
            float thisMobHeight = getMobDefinitionHeight(thisMobDefinitionID);
            byte thisMobHealth = getMobHealth(thisMob);
            float thisMobDeathTimer = 0;

            thisMobLightTimer += delta;

            //only collision detect if alive
            if (getMobHealth(thisMob) > 0) {
                mobSoftPlayerCollisionDetect(thisMob, thisMobPos, thisMobHeight, thisMobWidth);
                mobSoftCollisionDetect(thisMob, thisMobPos, thisMobHeight, thisMobWidth);
            }

            //interface consumes object - no need for re-assignment to vars
            //only do it during alive state
            if (thisMobHealth > 0) {
                getMobDefinitionInterface(thisMobDefinitionID).onTick(thisMob);
            }

            if (thisMobPos.y < 0){
                deletionQueue.add(thisMob);
                continue;
            }

            //mob is now dead
            if (thisMobHealth <= 0){
                //mob dying animation
                if (thisMobDeathRotation < 90) {
                    //System.out.println(thisMobDeathRotation);
                    thisMobDeathRotation += delta * 300f;
                    if (thisMobDeathRotation >= 90) {
                        thisMobDeathRotation = 90;
                        setMobDeathTimer(thisMob, 0.0001f);
                    }
                    setMobDeathRotation(thisMob, thisMobDeathRotation);
                //mob will now sit there for a second
                } else {
                    thisMobDeathTimer = getMobDeathTimer(thisMob);
                    thisMobDeathTimer += delta;
                    setMobDeathTimer(thisMob, thisMobDeathTimer);
                }
            }

            currentFlooredPos.set((int)Math.floor(thisMobPos.x), (int)Math.floor(thisMobPos.y), (int)Math.floor(thisMobPos.z));

            //poll local light every quarter second
            if (thisMobLightTimer >= 0.25f || !currentFlooredPos.equals(thisMobOldFlooredPos)){
                setMobLight(thisMob, getLight(currentFlooredPos.x, currentFlooredPos.y, currentFlooredPos.z));
                thisMobLightTimer = 0f;
            }

            if (thisMobHealth <= 0 && thisMobDeathTimer >= 0.5f && thisMobDeathRotation == 90){
                getMobDefinitionInterface(thisMobDefinitionID).onDeath(thisMob);
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

            setMobLightTimer(thisMob, thisMobLightTimer);
            setMobOldFlooredPos(thisMob, currentFlooredPos.x, currentFlooredPos.y, currentFlooredPos.z);
            setMobOldPos(thisMob, thisMobPos.x, thisMobPos.y, thisMobPos.z);
        }

        while (!deletionQueue.isEmpty()){
            int thisMobGlobalID = deletionQueue.pop();
            //System.out.println("mob " + thisMobGlobalID + " was deleted!");
            deleteMob(thisMobGlobalID);
        }
    }


    public void punchMob(int thisMob){

        float thisMobHurtTimer = getMobHurtTimer(thisMob);
        byte thisMobHealth = getMobHealth(thisMob);

        if (thisMobHurtTimer <= 0 && thisMobHealth > 0) {
            thisMobHealth -= 1;

            //System.out.println("the mobs health is: " + thisMob.health);

            Vector3d thisMobPos = getMobPos(thisMob);

            int thisMobDefinitionID = getMobID(thisMob);

            playSound(getMobDefinitionHurtSound(thisMobDefinitionID),(float)thisMobPos.x, (float)thisMobPos.y, (float)thisMobPos.z, true);
            //play this after in case ID changes
            MobInterface thisInterface = getMobDefinitionInterface(thisMobDefinitionID);
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
