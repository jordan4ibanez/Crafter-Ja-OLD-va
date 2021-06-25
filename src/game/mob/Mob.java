package game.mob;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.joml.Vector3d;
import org.joml.Vector3f;

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

    public static void spawnMob(int ID, Vector3d pos, Vector3f inertia){
        System.out.println("spawning mob! ID: " + currentID);
        System.out.println("pos y:" + pos.y);

        mobs.put(currentID,new MobObject(new Vector3d(pos),new Vector3f(inertia),ID,currentID));
        currentID++;
    }

    public static MobObject[] getAllMobs(){
        return mobs.values().toArray(new MobObject[0]);
    }

    public static void mobsOnTick(){

        int count = 0;

        double delta = getDelta();

        for (MobObject thisMob : mobs.values()){
            if (thisMob == null){
                count++;
                continue;
            }

            mobDefinitions[thisMob.ID].mobInterface.onTick(thisMob);

            if (thisMob.pos.y < 0){
                mobs.remove(count);
                System.out.println("mob " + count + " was deleted!");
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
                mobs.remove(count);
                System.out.println("mob " + count + " was deleted!");
            }

            //count down hurt timer
            if(thisMob.hurtTimer > 0f){
                thisMob.hurtTimer -= delta;
                if (thisMob.hurtTimer <= 0){
                    thisMob.hurtTimer = 0;
                }
            }

            count++;
        }
    }

    public static void punchMob(MobObject thisMob){
        if (thisMob.hurtTimer <= 0 && thisMob.health > 0) {
            thisMob.health -= 1;
            System.out.println("the mobs health is: " + thisMob.health);
            playSound(thisMob.hurtSound, new Vector3f((float)thisMob.pos.x, (float)thisMob.pos.y, (float)thisMob.pos.z), true);
            if (thisMob.onGround) {
                thisMob.inertia.y = 7;
            }
            thisMob.hurtTimer = 0.5f;
        }
    }
}
