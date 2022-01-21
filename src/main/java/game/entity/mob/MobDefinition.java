package game.entity.mob;

import engine.graphics.Mesh;
import org.joml.Vector3f;


final public class MobDefinition {
    private final int numberOfMobDefinitions = 9;
    private int count = 0;

    private final Mesh[][] bodyMeshes = new Mesh[numberOfMobDefinitions][0];
    private final MobInterface[] mobInterface = new MobInterface[numberOfMobDefinitions];
    private final String[] mobName = new String[numberOfMobDefinitions];
    private final Vector3f[][] bodyOffsets = new Vector3f[numberOfMobDefinitions][0];
    private final Vector3f[][] bodyRotations = new Vector3f[numberOfMobDefinitions][0];
    private final float[] height = new float[numberOfMobDefinitions];
    private final float[] width = new float[numberOfMobDefinitions];
    private final String[] hurtSound = new String[numberOfMobDefinitions];
    private final byte[] baseHealth = new byte[numberOfMobDefinitions];
    private final boolean[] backFaceCulling = new boolean[numberOfMobDefinitions];


    public void registerMob(String name, String newHurtSound, boolean newBackFaceCulling, byte newBaseHealth, Mesh[] newBodyMeshes,Vector3f[] newBodyOffsets,Vector3f[] newBodyRotations, float newHeight, float newWidth, MobInterface newMobInterface){
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
    public void registerMobs(){
        Human.registerHumanMob();
        Pig.registerPigMob();
        Zombie.registerZombieMob();
        Exploder.registerExploderMob();
        Skeleton.registerSkeletonMob();
        Sheep.registerSheepMob();
        Chicken.registerChickenMob();
        Cow.registerCowMob();
    }

    public Mesh[] getMobDefinitionBodyMeshes(int ID){
        return bodyMeshes[ID];
    }
    public MobInterface getMobDefinitionInterface(int ID){
        return mobInterface[ID];
    }
    public String getMobDefinitionName(int ID){
        return mobName[ID];
    }
    public Vector3f[] getMobDefinitionBodyOffsets(int ID){
        return bodyOffsets[ID];
    }
    public Vector3f[] getMobDefinitionBodyRotations(int ID){
        return bodyRotations[ID];
    }
    public float getMobDefinitionHeight(int ID){
        return height[ID];
    }
    public float getMobDefinitionWidth(int ID){
        return width[ID];
    }
    public String getMobDefinitionHurtSound(int ID){
        return hurtSound[ID];
    }
    public byte getMobDefinitionBaseHealth(int ID){
        return baseHealth[ID];
    }
    public boolean getMobBackFaceCulling(int ID){
        return backFaceCulling[ID];
    }

}
