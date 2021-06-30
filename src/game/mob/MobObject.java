package game.mob;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static engine.FancyMath.randomDirFloat;
import static game.mob.Mob.getMobDefinition;

public class MobObject {
    //the mobDefinition ID
    public byte ID;

    //the global reference to the object in the list
    public final int globalID;

    public final Vector3d pos;
    public final Vector3i oldFlooredPos = new Vector3i(0,-1,0);
    public final  Vector3d oldPos;
    public final Vector3f inertia;
    public final float width;
    public final float height;
    public float rotation;
    public float smoothRotation;
    public final Vector3f[] bodyOffsets;
    public final Vector3f[] bodyRotations;
    public byte light = 0;
    public float lightTimer = 1; //causes an instant update

    public float animationTimer;
    public float timer;
    public boolean onGround;
    public boolean stand;

    public float hurtTimer = 0f;
    public byte health;
    public float deathRotation = 0;
    public byte hurtAdder = 0;


    public MobObject(Vector3d pos, Vector3f inertia, byte ID, int globalID){
        this.pos = pos;
        this.oldPos = new Vector3d(pos);
        this.inertia = inertia;

        this.timer = 0f;
        this.animationTimer = 0f;

        //inheritance to prevent lookup every frame
        this.height = getMobDefinition(ID).height;
        this.width = getMobDefinition(ID).width;

        this.rotation = (float)(Math.toDegrees(Math.PI * Math.random() * randomDirFloat()));
        this.smoothRotation = 0f;

        //stop memory leak with thorough clone
        Vector3f[] offSets = getMobDefinition(ID).bodyOffsets;
        this.bodyOffsets = new Vector3f[offSets.length];
        byte count = 0;
        for (Vector3f thisOffset : offSets.clone()){
            this.bodyOffsets[count] = new Vector3f(thisOffset);
            count++;
        }

        //stop memory leak with thorough clone
        Vector3f[] bodyRotations = getMobDefinition(ID).bodyRotations;
        this.bodyRotations = new Vector3f[bodyRotations.length];
        byte count2 = 0;
        for (Vector3f thisRotation : bodyRotations.clone()){
            this.bodyRotations[count2] = new Vector3f(thisRotation);
            count2++;
        }

        this.ID = ID;

        this.globalID = globalID;

        this.health = getMobDefinition(ID).baseHealth;
    }
}
