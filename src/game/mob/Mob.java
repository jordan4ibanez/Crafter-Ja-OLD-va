package game.mob;

import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Hashtable;

import static game.mob.Human.registerHumanMob;

public class Mob {

    private static final Hashtable<String, MobDefinition> mobDefinitions = new Hashtable<>();
    private static final Hashtable<Integer, MobObject> mobs = new Hashtable<>();

    private static int currentID = 0;


    public static MobDefinition getMobDefinition(String key){
        return mobDefinitions.get(key);
    }

    public static void registerMob(MobDefinition newMobDefinition){
        mobDefinitions.put(newMobDefinition.mobDefinitionKey, newMobDefinition);
    }

    //entry point
    public static void initializeMobRegister(){
        registerHumanMob();
    }

    public static void spawnMob(String name, Vector3f pos, Vector3f inertia){
        System.out.println("spawning mob! ID: " + currentID);
        System.out.println("pos y:" + pos.y);
        mobs.put(currentID, new MobObject(pos,inertia,name,currentID));
        currentID++;
    }

    public static Collection<MobObject> getAllMobs(){
        return mobs.values();
    }

    private static final Deque<Integer> deletionQueue = new ArrayDeque<>();

    public static void mobsOnTick(){
        for (MobObject thisMob : mobs.values()){
            mobDefinitions.get(thisMob.mobDefinitionKey).mobInterface.onTick(thisMob);

            if (thisMob.pos.y < 0){
                deletionQueue.add(thisMob.mobTableKey);
            }
        }

        while (!deletionQueue.isEmpty()){
            int deleter = deletionQueue.pop();
            mobs.remove(deleter);
            System.out.println("mob " + deleter + " was deleted!");
        }
    }
}
