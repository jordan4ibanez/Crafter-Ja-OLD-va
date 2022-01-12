package game.mob;

import it.unimi.dsi.fastutil.ints.*;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static engine.FancyMath.randomDirFloat;
import static game.mob.Mob.getMobDefinition;
import static game.mob.MobDefinition.getMobBaseHealth;
import static game.mob.MobDefinition.getMobBodyRotations;

final public class MobObject {
    //todo: ADD MOBS TO MEMORY SWEEPER

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
        //stop memory leak with thorough clone
        bodyRotations.put(currentMobPublicID, getMobBodyRotations(newMobID).clone());
        light.put(currentMobPublicID, (byte) 0);
        //causes an instant update
        lightTimer.put(currentMobPublicID, 1f);
        animationTimer.put(currentMobPublicID, 0f);
        timer.put(currentMobPublicID, 0f);
        onGround.put(currentMobPublicID, false);
        stand.put(currentMobPublicID, false);
        hurtTimer.put(currentMobPublicID, 0f);
        health.put(currentMobPublicID, getMobBaseHealth(newMobID));
        deathRotation.put(currentMobPublicID, 0);
        hurtAdder.put(currentMobPublicID, (byte) 0);

        currentMobPublicID++;
    }
}
