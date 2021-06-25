package game.mob;

import engine.graphics.Mesh;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.FancyMath.randomDirFloat;
import static game.mob.Mob.getMobDefinition;

public class MobObject {
    //the mobDefinition ID
    public final byte ID;

    //the global reference to the object in the list
    public final int globalID;

    public final Vector3d pos;
    public final  Vector3d lastPos;
    public final Vector3f inertia;
    public final float width;
    public final float height;
    public float rotation;
    public float smoothRotation;
    public final Vector3f[] bodyOffsets;
    public final Vector3f[] bodyRotations;

    public float animationTimer;
    public float timer;
    public boolean onGround;
    public boolean stand;

    public float hurtTimer = 0f;
    public int health;
    public float deathRotation = 0;


    public MobObject(Vector3d pos, Vector3f inertia, byte ID, int globalID){
        this.pos = pos;
        this.lastPos = new Vector3d(pos);
        this.inertia = inertia;

        this.timer = 0f;
        this.animationTimer = 0f;

        //inheritance to prevent lookup every frame
        this.height = getMobDefinition(ID).height;
        this.width = getMobDefinition(ID).width;

        this.rotation = (float)(Math.toDegrees(Math.PI * Math.random() * randomDirFloat()));
        this.smoothRotation = 0f;

        this.bodyOffsets = getMobDefinition(ID).bodyOffsets.clone();
        this.bodyRotations = getMobDefinition(ID).bodyRotations.clone();

        this.ID = ID;

        this.globalID = globalID;

        this.health = getMobDefinition(ID).baseHealth;
    }
}
