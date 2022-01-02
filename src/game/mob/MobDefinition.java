package game.mob;

import engine.graphics.Mesh;
import org.joml.Vector3f;

public class MobDefinition {
    public final int[] bodyMeshes;
    public final MobInterface mobInterface;
    public final String mobName;
    public final Vector3f[] bodyOffsets;
    public final Vector3f[] bodyRotations;
    public final float height;
    public final float width;
    public final String hurtSound;
    public final byte baseHealth;
    public final boolean backFaceCulling;

    public MobDefinition(String name, String hurtSound, boolean backFaceCulling, byte baseHealth, int[] bodyMeshes,Vector3f[] bodyOffsets,Vector3f[] bodyRotations, float height, float width, MobInterface mobInterface){
        this.bodyMeshes = bodyMeshes;
        this.mobInterface = mobInterface;
        this.mobName = name;
        this.bodyOffsets = bodyOffsets;
        this.bodyRotations = bodyRotations;
        this.height = height;
        this.width = width;
        this.hurtSound = hurtSound;
        this.baseHealth = baseHealth;
        this.backFaceCulling = backFaceCulling;
    }
}
