package game.mob;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.Deque;

import static engine.time.Time.getDelta;
import static engine.sound.SoundAPI.playSound;
import static game.mob.Human.registerHumanMob;
import static game.mob.Pig.registerPigMob;

public class Mob {

    //todo: ADD MOBS TO MEMORY SWEEPER
    private static final MobDefinition[] mobDefinitions = new MobDefinition[3];

    private static final Int2ObjectArrayMap<MobObject> mobs = new Int2ObjectArrayMap<>();

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
    public static void registerMobs(){
        registerHumanMob();
        registerPigMob();
    }

    public static void spawnMob(byte ID, Vector3d pos, Vector3f inertia){
        System.out.println("spawning mob! ID: " + currentID);
        System.out.println("pos y:" + pos.y);

        mobs.put(currentID,new MobObject(new Vector3d(pos),new Vector3f(inertia),ID,currentID));
        currentID++;
    }

    public static MobObject[] getAllMobs(){
        return mobs.values().toArray(new MobObject[0]);
    }

    private static String getHurtSound(byte ID){
        return mobDefinitions[ID].hurtSound;
    }

    private static final Deque<Integer> deletionQueue = new ArrayDeque<>();

    public static void mobsOnTick(){

        double delta = getDelta();

        for (MobObject thisMob : mobs.values()){
            if (thisMob == null){
                continue;
            }

            //interface consumes object - no need for re-assignment to vars
            mobDefinitions[thisMob.ID].mobInterface.onTick(thisMob);

            if (thisMob.pos.y < 0){
                deletionQueue.add(thisMob.globalID);
            }

            //mob dying animation
            if (thisMob.health <= 0 && thisMob.deathRotation < 90){
                thisMob.deathRotation += delta * 300f;
                if (thisMob.deathRotation >= 90){
                    thisMob.deathRotation = 90;
                    thisMob.timer = 0f;
                }
            }

            if (thisMob.health <= 0 && thisMob.timer >= 0.5f && thisMob.deathRotation == 90){
                deletionQueue.add(thisMob.globalID);
            }

            //count down hurt timer
            if(thisMob.hurtTimer > 0f){
                thisMob.hurtTimer -= delta;
                if (thisMob.hurtTimer <= 0){
                    thisMob.hurtTimer = 0;
                }
            }
        }

        while (!deletionQueue.isEmpty()){
            int thisMobGlobalID = deletionQueue.pop();
            System.out.println("mob " + thisMobGlobalID + " was deleted!");
            mobs.remove(thisMobGlobalID);
        }
    }

    public static void punchMob(MobObject thisMob){
        if (thisMob.hurtTimer <= 0 && thisMob.health > 0) {
            thisMob.health -= 1;
            System.out.println("the mobs health is: " + thisMob.health);
            playSound(getHurtSound(thisMob.ID), new Vector3f((float)thisMob.pos.x, (float)thisMob.pos.y, (float)thisMob.pos.z), true);
            if (thisMob.onGround) {
                thisMob.inertia.y = 7;
            }
            thisMob.hurtTimer = 0.5f;
        }
    }
}
