package game.entity.mob;

import it.unimi.dsi.fastutil.ints.*;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import engine.FancyMath.randomDirFloat;
import game.entity.mob.MobDefinition.getMobDefinitionBaseHealth;
import game.entity.mob.MobDefinition.getMobDefinitionBodyRotations;

final public class MobObject {
    //todo: ADD MOBS TO MEMORY SWEEPER

    //this is the counter to count how many mobs are spawned into the world
    private int globalMobs = 0;

    //the mobDefinition ID
    private final Int2IntOpenHashMap mobID = new Int2IntOpenHashMap();

    //the mobs exist as a collection of variables
    private final Int2ObjectOpenHashMap<Vector3d> pos = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<Vector3i> oldFlooredPos = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<Vector3d> oldPos = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<Vector3f> inertia = new Int2ObjectOpenHashMap<>();
    private final Int2FloatOpenHashMap rotation = new Int2FloatOpenHashMap();
    private final Int2FloatOpenHashMap smoothRotation = new Int2FloatOpenHashMap();
    private final Int2ObjectOpenHashMap<Vector3f[]> bodyRotations = new Int2ObjectOpenHashMap<>();
    private final Int2ByteOpenHashMap light = new Int2ByteOpenHashMap();
    private final Int2FloatOpenHashMap lightTimer = new Int2FloatOpenHashMap();
    private final Int2FloatOpenHashMap animationTimer = new Int2FloatOpenHashMap();
    private final Int2FloatOpenHashMap timer = new Int2FloatOpenHashMap();
    private final Int2BooleanOpenHashMap onGround = new Int2BooleanOpenHashMap();
    private final Int2BooleanOpenHashMap stand = new Int2BooleanOpenHashMap();
    private final Int2FloatOpenHashMap hurtTimer = new Int2FloatOpenHashMap();
    private final Int2ByteOpenHashMap health = new Int2ByteOpenHashMap();
    private final Int2FloatOpenHashMap deathRotation = new Int2FloatOpenHashMap();
    private final Int2ByteOpenHashMap hurtAdder = new Int2ByteOpenHashMap();
    private final Int2FloatOpenHashMap deathTimer = new Int2FloatOpenHashMap();

    private int currentMobPublicID = 0;

    public void spawnMob(int newMobID, double posX, double posY, double posZ, float inertiaX, float inertiaY, float inertiaZ){

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
        Vector3f[] cloningArray = getMobDefinitionBodyRotations(newMobID);
        Vector3f[] newArray = new Vector3f[cloningArray.length];
        //deep clone
        int indexer = 0;
        for (Vector3f thisVec : cloningArray){
            newArray[indexer] = new Vector3f(thisVec.x, thisVec.y, thisVec.z);
            indexer++;
        }
        bodyRotations.put(currentMobPublicID, newArray);
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
        deathTimer.put(currentMobPublicID, 0f);

        currentMobPublicID++;
        globalMobs++;
    }

    public void deleteMob(int newMobID){
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
        deathTimer.remove(newMobID);

        globalMobs--;
    }

    //getters start here

    public IntSet getMobKeys(){
        return mobID.keySet();
    }

    public int getNumberOfMobs(){
        return globalMobs;
    }

    public int getMobID(int ID){
        return mobID.get(ID);
    }

    public Vector3d getMobPos(int ID){
        return pos.get(ID);
    }

    public Vector3i getMobOldFlooredPos(int ID){
        return oldFlooredPos.get(ID);
    }

    public Vector3d getMobOldPos(int ID){
        return oldPos.get(ID);
    }

    public Vector3f getMobInertia(int ID){
        return inertia.get(ID);
    }

    public float getMobRotation(int ID){
        return rotation.get(ID);
    }

    public float getMobSmoothRotation(int ID){
        return smoothRotation.get(ID);
    }

    public Vector3f[] getMobBodyRotations(int ID){
        return bodyRotations.get(ID);
    }

    public byte getMobLight(int ID){
        return light.get(ID);
    }

    public float getMobLightTimer(int ID){
        return lightTimer.get(ID);
    }

    public float getMobAnimationTimer(int ID){
        return animationTimer.get(ID);
    }

    public float getMobTimer(int ID){
        return timer.get(ID);
    }

    public boolean getIfMobOnGround(int ID){
        return onGround.get(ID);
    }

    public boolean getIfMobStanding(int ID){
        return stand.get(ID);
    }

    public float getMobHurtTimer(int ID){
        return hurtTimer.get(ID);
    }

    public byte getMobHealth(int ID){
        return health.get(ID);
    }

    public float getMobDeathRotation(int ID){
        return deathRotation.get(ID);
    }

    public byte getMobHurtAdder(int ID){
        return hurtAdder.get(ID);
    }

    public float getMobDeathTimer(int ID){
        return deathTimer.get(ID);
    }

    //setters start here

    //useful for mobs like sheep
    public void setMobID(int ID, int newID){
        mobID.put(ID, newID);
    }

    public void setMobPos(int ID, double posX, double posY, double posZ){
        //an interesting usage of object orientation - continues on below
        pos.get(ID).set(posX,posY,posZ);
    }

    public void setMobOldFlooredPos(int ID, int posX, int posY, int posZ){
        oldFlooredPos.get(ID).set(posX,posY,posZ);
    }

    public void setMobOldPos(int ID, double posX, double posY, double posZ){
        oldPos.get(ID).set(posX,posY,posZ);
    }

    public void setMobInertia(int ID, float inertiaX, float inertiaY, float inertiaZ){
        inertia.get(ID).set(inertiaX,inertiaY,inertiaZ);
    }

    public void setMobRotation(int ID, float newRotation){
        rotation.put(ID, newRotation);
    }

    public void setMobSmoothRotation(int ID, float newSmoothRotation){
        smoothRotation.put(ID, newSmoothRotation);
    }

    public void setMobBodyRotations(int ID, Vector3f[] vectorArray){
        int count = 0;
        for (Vector3f aVector : bodyRotations.get(ID)) {
            aVector.set(vectorArray[count]);
            count++;
        }
    }

    public void setMobLight(int ID, byte newLight){
        light.put(ID, newLight);
    }

    public void setMobLightTimer(int ID, float newLightTimer){
        lightTimer.put(ID, newLightTimer);
    }

    public void setMobAnimationTimer(int ID, float newAnimationTimer){
        animationTimer.put(ID,newAnimationTimer);
    }

    public void setMobTimer(int ID, float newTimer){
        timer.put(ID, newTimer);
    }

    public void setIfMobOnGround(int ID, boolean newOnGround){
        onGround.put(ID, newOnGround);
    }

    public void setIfMobStanding(int ID, boolean isMobStanding){
        stand.put(ID, isMobStanding);
    }

    public void setMobHurtTimer(int ID, float newHurtTimer){
        hurtTimer.put(ID, newHurtTimer);
    }

    public void setMobHealth(int ID, byte newHealth){
        health.put(ID, newHealth);
    }

    public void setMobDeathRotation(int ID, float newDeathRotation){
        deathRotation.put(ID, newDeathRotation);
    }

    public void setMobHurtAdder(int ID, byte newHurtAdder){
        hurtAdder.put(ID, newHurtAdder);
    }

    public void setMobDeathTimer(int ID, float newDeathTimer){
        deathTimer.put(ID, newDeathTimer);
    }

}
