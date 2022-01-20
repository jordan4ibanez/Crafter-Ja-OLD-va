package game.mob;

import engine.graphics.Mesh;
import org.joml.Vector3f;

import static game.mob.Chicken.registerChickenMob;
import static game.mob.Cow.registerCowMob;
import static game.mob.Exploder.registerExploderMob;
import static game.mob.Human.registerHumanMob;
import static game.mob.Pig.registerPigMob;
import static game.mob.Sheep.registerSheepMob;
import static game.mob.Skeleton.registerSkeletonMob;
import static game.mob.Zombie.registerZombieMob;


final public class MobDefinition {
    private static final int numberOfMobDefinitions = 9;
    private static int count = 0;

    private static final Mesh[][] bodyMeshes = new Mesh[numberOfMobDefinitions][0];
    private static final MobInterface[] mobInterface = new MobInterface[numberOfMobDefinitions];
    private static final String[] mobName = new String[numberOfMobDefinitions];
    private static final Vector3f[][] bodyOffsets = new Vector3f[numberOfMobDefinitions][0];
    private static final Vector3f[][] bodyRotations = new Vector3f[numberOfMobDefinitions][0];
    private static final float[] height = new float[numberOfMobDefinitions];
    private static final float[] width = new float[numberOfMobDefinitions];
    private static final String[] hurtSound = new String[numberOfMobDefinitions];
    private static final byte[] baseHealth = new byte[numberOfMobDefinitions];
    private static final boolean[] backFaceCulling = new boolean[numberOfMobDefinitions];


    public static void registerMob(String name, String newHurtSound, boolean newBackFaceCulling, byte newBaseHealth, Mesh[] newBodyMeshes,Vector3f[] newBodyOffsets,Vector3f[] newBodyRotations, float newHeight, float newWidth, MobInterface newMobInterface){
        bodyMeshes[count] = newBodyMeshes;
        mobInterface[count] = newMobInterface;
        mobName[count] = name;
        bodyOffsets[count] = newBodyOffsets;
        bodyRotations[count] = newBodyRotations;
        height[count] = newHeight;
        width[count] = newWidth;
        hurtSound[count] = newHurtSound;
        baseHealth[count] = newBaseHealth;
        backFaceCulling[count] = newBackFaceCulling;

        count++;
    }

    //entry point
    //todo: make this not a confusing linkage
    public static void registerMobs(){
        registerHumanMob();
        registerPigMob();
        registerZombieMob();
        registerExploderMob();
        registerSkeletonMob();
        registerSheepMob();
        registerChickenMob();
        registerCowMob();
    }

    public static Mesh[] getMobDefinitionBodyMeshes(int ID){
        return bodyMeshes[ID];
    }
    public static MobInterface getMobDefinitionInterface(int ID){
        return mobInterface[ID];
    }
    public static String getMobDefinitionName(int ID){
        return mobName[ID];
    }
    public static Vector3f[] getMobDefinitionBodyOffsets(int ID){
        return bodyOffsets[ID];
    }
    public static Vector3f[] getMobDefinitionBodyRotations(int ID){
        return bodyRotations[ID];
    }
    public static float getMobDefinitionHeight(int ID){
        return height[ID];
    }
    public static float getMobDefinitionWidth(int ID){
        return width[ID];
    }
    public static String getMobDefinitionHurtSound(int ID){
        return hurtSound[ID];
    }
    public static byte getMobDefinitionBaseHealth(int ID){
        return baseHealth[ID];
    }
    public static boolean getMobBackFaceCulling(int ID){
        return backFaceCulling[ID];
    }

}
