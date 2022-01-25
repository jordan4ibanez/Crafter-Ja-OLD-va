package game.entity.mob;

import engine.graphics.Mesh;
import org.joml.Vector3f;


final public class MobDefinition {

    private final Mesh[] bodyMeshes;
    private final MobInterface mobInterface;
    private final String mobName;
    private final Vector3f[] bodyOffsets;
    private final Vector3f[] bodyRotations;
    private final float height;
    private final float width;
    private final String hurtSound;
    private final byte baseHealth;
    private final boolean backFaceCulling;
    
    public MobDefinition(String name, String newHurtSound, boolean newBackFaceCulling, byte newBaseHealth, Mesh[] newBodyMeshes,Vector3f[] newBodyOffsets,Vector3f[] newBodyRotations, float newHeight, float newWidth, MobInterface newMobInterface){
        bodyMeshes = newBodyMeshes;
        mobInterface = newMobInterface;
        mobName = name;
        bodyOffsets = newBodyOffsets;
        bodyRotations = newBodyRotations;
        height = newHeight;
        width = newWidth;
        hurtSound = newHurtSound;
        baseHealth = newBaseHealth;
        backFaceCulling = newBackFaceCulling;
    }

    //entry point
    //todo: make this not a confusing linkage
    /*
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
     */

    public Mesh[] getBodyMeshes(){
        return bodyMeshes;
    }
    public MobInterface getInterface(){
        return mobInterface;
    }
    public String getMobDefinitionName(){
        return mobName;
    }
    public Vector3f[] getBodyOffsets(){
        return bodyOffsets;
    }
    public Vector3f[] getBodyRotations(){
        return bodyRotations;
    }
    public float getHeight(){
        return height;
    }
    public float getWidth(){
        return width;
    }
    public String getHurtSound(){
        return hurtSound;
    }
    public byte getBaseHealth(){
        return baseHealth;
    }
    public boolean getBackFaceCulling(){
        return backFaceCulling;
    }

}
