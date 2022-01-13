package game.mob;

import it.unimi.dsi.fastutil.ints.*;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static engine.FancyMath.randomDirFloat;
import static game.mob.MobDefinition.getMobDefinitionBaseHealth;
import static game.mob.MobDefinition.getMobDefinitionBodyRotations;

final public class MobObject {
    //todo: ADD MOBS TO MEMORY SWEEPER

    //this is the counter to count how many mobs are spawned into the world
    private static int globalMobs = 0;

    //the mobDefinition ID
    private static final Int2IntOpenHashMap mobID = new Int2IntOpenHashMap();

    //the global reference to the object in the list
    //public final int globalID;

    private static final Int2ObjectOpenHashMap<Vector3d> pos = new Int2ObjectOpenHashMap<>();
    //was new Vector3i(0,-1,0)
    private static final Int2ObjectOpenHashMap<Vector3i> oldFlooredPos = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectOpenHashMap<Vector3d> oldPos = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectOpenHashMap<Vector3f> inertia = new Int2ObjectOpenHashMap<>();
    private static final Int2FloatOpenHashMap rotation = new Int2FloatOpenHashMap();
    private static final Int2FloatOpenHashMap smoothRotation = new Int2FloatOpenHashMap();

    private static final Int2ObjectOpenHashMap<Vector3f[]> bodyRotations = new Int2ObjectOpenHashMap<>();

    //was 0
    private static final Int2ByteOpenHashMap light = new Int2ByteOpenHashMap();

    //was 1 //causes an instant update
    private static final Int2FloatOpenHashMap lightTimer = new Int2FloatOpenHashMap();

    private static final Int2FloatOpenHashMap animationTimer = new Int2FloatOpenHashMap();

    private static final Int2FloatOpenHashMap timer = new Int2FloatOpenHashMap();
    private static final Int2BooleanOpenHashMap onGround = new Int2BooleanOpenHashMap();
    //what is this even for?
    private static final Int2BooleanOpenHashMap stand = new Int2BooleanOpenHashMap();

    //was 0f
    private static final Int2FloatOpenHashMap hurtTimer = new Int2FloatOpenHashMap();

    private static final Int2ByteOpenHashMap health = new Int2ByteOpenHashMap();
    //was 0
    private static final Int2FloatOpenHashMap deathRotation = new Int2FloatOpenHashMap();
    //was 0
    private static final Int2ByteOpenHashMap hurtAdder = new Int2ByteOpenHashMap();

    private static int currentMobPublicID = 0;

    public static void spawnMob(int newMobID, double posX, double posY, double posZ, float inertiaX, float inertiaY, float inertiaZ){

        //todo, tick up until null mob slot is found boi
        //do this to avoid overwriting a mob because that's silly

        mobID.put(currentMobPublicID,newMobID);
        pos.put(currentMobPublicID, new Vector3d(posX, posY, posZ));
        oldFlooredPos.put(currentMobPublicID, new Vector3i(0,-1,0));
        oldPos.put(currentMobPublicID, new Vector3d(posX, posY, posZ));
        inertia.put(currentMobPublicID, new Vector3f(inertiaX, inertiaY, inertiaZ));
        rotation.put(currentMobPublicID, (float)(Math.toDegrees(Math.PI * Math.random() * randomDirFloat())));
        smoothRotation.put(currentMobPublicID, 0f);
        //stop memory pointer leak with thorough clone
        bodyRotations.put(currentMobPublicID, getMobDefinitionBodyRotations(newMobID).clone());
        light.put(currentMobPublicID, (byte) 0);
        //causes an instant update
        lightTimer.put(currentMobPublicID, 1f);
        animationTimer.put(currentMobPublicID, 0f);
        timer.put(currentMobPublicID, 0f);
        onGround.put(currentMobPublicID, false);
        stand.put(currentMobPublicID, false);
        hurtTimer.put(currentMobPublicID, 0f);
        health.put(currentMobPublicID, getMobDefinitionBaseHealth(newMobID));
        deathRotation.put(currentMobPublicID, 0);
        hurtAdder.put(currentMobPublicID, (byte) 0);

        currentMobPublicID++;
        globalMobs++;
    }

    public static void deleteMob(int newMobID){
        mobID.remove(newMobID);
        pos.remove(newMobID);
        oldFlooredPos.remove(newMobID);
        oldPos.remove(newMobID);
        inertia.remove(newMobID);
        rotation.remove(newMobID);
        smoothRotation.remove(newMobID);
        bodyRotations.remove(newMobID);
        light.remove(newMobID);
        lightTimer.remove(newMobID);
        animationTimer.remove(newMobID);
        timer.remove(newMobID);
        onGround.remove(newMobID);
        stand.remove(newMobID);
        hurtTimer.remove(newMobID);
        health.remove(newMobID);
        deathRotation.remove(newMobID);
        hurtAdder.remove(newMobID);

        globalMobs--;
    }

    //getters start here

    public static IntSet getMobKeys(){
        return mobID.keySet();
    }

    public static int getNumberOfMobs(){
        return globalMobs;
    }

    public static int getMobID(int ID){
        return mobID.get(ID);
    }

    public static Vector3d getMobPos(int ID){
        return pos.get(ID);
    }

    public static Vector3i getMobOldFlooredPos(int ID){
        return oldFlooredPos.get(ID);
    }

    public static Vector3d getMobOldPos(int ID){
        return oldPos.get(ID);
    }

    public static Vector3f getMobInertia(int ID){
        return inertia.get(ID);
    }

    public static float getMobRotation(int ID){
        return rotation.get(ID);
    }

    public static float getMobSmoothRotation(int ID){
        return smoothRotation.get(ID);
    }

    public static Vector3f[] getMobBodyRotations(int ID){
        return bodyRotations.get(ID);
    }

    public static byte getMobLight(int ID){
        return light.get(ID);
    }

    public static float getMobLightTimer(int ID){
        return lightTimer.get(ID);
    }

    public static float getMobAnimationTimer(int ID){
        return animationTimer.get(ID);
    }

    public static float getMobTimer(int ID){
        return timer.get(ID);
    }

    public static boolean getIfMobOnGround(int ID){
        return onGround.get(ID);
    }

    public static boolean getIfMobStanding(int ID){
        return stand.get(ID);
    }

    public static float getMobHurtTimer(int ID){
        return hurtTimer.get(ID);
    }

    public static byte getMobHealth(int ID){
        return health.get(ID);
    }

    public static float getMobDeathRotation(int ID){
        return deathRotation.get(ID);
    }

    public static byte getMobHurtAdder(int ID){
        return hurtAdder.get(ID);
    }


    //setters start here

    //useful for mobs like sheep
    public static void setMobID(int ID, int newID){
        mobID.put(ID, newID);
    }

    public static void setMobPos(int ID, double posX, double posY, double posZ){
        //an interesting usage of object orientation - continues on below
        pos.get(ID).set(posX,posY,posZ);
    }

    public static void setMobOldFlooredPos(int ID, int posX, int posY, int posZ){
        oldFlooredPos.get(ID).set(posX,posY,posZ);
    }

    public static void setMobOldPos(int ID, double posX, double posY, double posZ){
        oldPos.get(ID).set(posX,posY,posZ);
    }

    public static void setMobInertia(int ID, float inertiaX, float inertiaY, float inertiaZ){
        inertia.get(ID).set(inertiaX,inertiaY,inertiaZ);
    }

    public static void setMobRotation(int ID, float newRotation){
        rotation.put(ID, newRotation);
    }

    public static void setMobSmoothRotation(int ID, float newSmoothRotation){
        smoothRotation.put(ID, newSmoothRotation);
    }

    public static void setMobBodyRotations(int ID, Vector3f[] vectorArray){
        int count = 0;
        for (Vector3f aVector : bodyRotations.get(ID)) {
            aVector.set(vectorArray[count]);
            count++;
        }
    }

    public static void setMobLight(int ID, byte newLight){
        light.put(ID, newLight);
    }

    public static void setMobLightTimer(int ID, float newLightTimer){
        lightTimer.put(ID, newLightTimer);
    }

    public static void setMobAnimationTimer(int ID, float newAnimationTimer){
        animationTimer.put(ID,newAnimationTimer);
    }

    public static void setMobTimer(int ID, float newTimer){
        timer.put(ID, newTimer);
    }

    public static void setIfMobOnGround(int ID, boolean newOnGround){
        onGround.put(ID, newOnGround);
    }

    public static void setIfMobStanding(int ID, boolean isMobStanding){
        stand.put(ID, isMobStanding);
    }

    public static void setMobHurtTimer(int ID, float newHurtTimer){
        hurtTimer.put(ID, newHurtTimer);
    }

    public static void setMobHealth(int ID, byte newHealth){
        health.put(ID, newHealth);
    }

    public static void setMobDeathRotation(int ID, float newDeathRotation){
        deathRotation.put(ID, newDeathRotation);
    }

    public static void setMobHurtAdder(int ID, byte newHurtAdder){
        hurtAdder.put(ID, newHurtAdder);
    }

}
