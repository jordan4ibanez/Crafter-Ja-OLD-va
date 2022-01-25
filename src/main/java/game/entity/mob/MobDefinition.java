package game.entity.mob;

import engine.graphics.Mesh;
import org.joml.Vector3f;


final public class MobDefinition {


    private final Mesh[] bodyMeshes;
    private final MobInterface mobInterface;
    private final String name;
    private final Vector3f[] bodyOffsets;
    private final Vector3f[] bodyRotations;
    private final float height;
    private final float width;
    private final String hurtSound;
    private final byte baseHealth;
    private final boolean backFaceCulling;

    public MobDefinition(String name, String hurtSound, boolean backFaceCulling, byte baseHealth, Mesh[] bodyMeshes,Vector3f[] bodyOffsets, Vector3f[] bodyRotations, float height, float width, MobInterface mobInterface){
        this.bodyMeshes = bodyMeshes;
        this.mobInterface = mobInterface;
        this.name = name;
        this.bodyOffsets = bodyOffsets;
        this.bodyRotations = bodyRotations;
        this.height = height;
        this.width = width;
        this.hurtSound = hurtSound;
        this.baseHealth = baseHealth;
        this.backFaceCulling = backFaceCulling;
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
        return name;
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
