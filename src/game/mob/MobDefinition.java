package game.mob;

import engine.graphics.Mesh;
import org.joml.Vector3f;

public class MobDefinition {
    public Mesh[] bodyMeshes;
    public MobInterface mobInterface;
    public String mobDefinitionKey;
    public Vector3f[] bodyOffsets;
    public Vector3f[] bodyRotations;
    public float height;
    public float width;
    public String hurtSound;
    public int baseHealth;

    public MobDefinition(String name, String hurtSound, int baseHealth, Mesh[] bodyMeshes,Vector3f[] bodyOffsets,Vector3f[] bodyRotations, float height, float width, MobInterface mobInterface){
        this.bodyMeshes = bodyMeshes;
        this.mobInterface = mobInterface;
        this.mobDefinitionKey = name;
        this.bodyOffsets = bodyOffsets;
        this.bodyRotations = bodyRotations;
        this.height = height;
        this.width = width;
        this.hurtSound = hurtSound;
        this.baseHealth = baseHealth;
    }
}
