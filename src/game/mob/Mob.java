package game.mob;

import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Hashtable;

import static engine.Time.getDelta;
import static game.mob.Human.registerHumanMob;

public class Mob {

    private static final MobDefinition[] mobDefinitions = new MobDefinition[16];

    private static final MobObject[] mobs = new MobObject[128]; //limited to 128 mobs

    private static int currentID = 0;
    private static int currentMobDefinitionKey = 0;

    public static MobDefinition getMobDefinition(int key){
        return mobDefinitions[key];
    }

    public static void registerMob(MobDefinition newMobDefinition){
        mobDefinitions[currentMobDefinitionKey] = newMobDefinition;
        currentMobDefinitionKey++;
    }

    //entry point
    public static void initializeMobRegister(){
        registerHumanMob();
    }

    public static void spawnMob(int ID, Vector3d pos, Vector3f inertia){
        System.out.println("spawning mob! ID: " + currentID);
        System.out.println("pos y:" + pos.y);
        mobs[currentID] = new MobObject(new Vector3d(pos),new Vector3f(inertia),ID,currentID);
        currentID++;
    }

    public static MobObject[] getAllMobs(){
        return mobs;
    }

    public static void mobsOnTick(){
        int count = 0;
        for (MobObject thisMob : mobs){
            if (thisMob == null){
                continue;
            }

            mobDefinitions[thisMob.ID].mobInterface.onTick(thisMob);


            if (thisMob.pos.y < 0){
                //deletionQueue.add(thisMob.ID);
                mobs[count] = null;
                System.out.println("mob " + count + " was deleted!");
            }

            count++;
        }
    }

    public static void punchMob(MobObject thisMob){
        if (thisMob.hurtTimer <= 0) {
            System.out.println("punched!");
            if (thisMob.onGround) {
                thisMob.inertia.y += 10;
            }
            thisMob.hurtTimer = 0.5f;
        }
    }

    //todo: shortest distance
    public static void mobSmoothRotation(MobObject thisObject){
        float delta = getDelta();
        if (thisObject.rotation - thisObject.smoothRotation < 0) {
            thisObject.smoothRotation -= delta * 500f;
            if (thisObject.smoothRotation < thisObject.rotation) {
                thisObject.smoothRotation = thisObject.rotation;
            }
        } else if (thisObject.rotation - thisObject.smoothRotation > 0) {
            thisObject.smoothRotation += delta * 500f;
            if (thisObject.smoothRotation > thisObject.rotation) {
                thisObject.smoothRotation = thisObject.rotation;
            }
        }
    }
}
