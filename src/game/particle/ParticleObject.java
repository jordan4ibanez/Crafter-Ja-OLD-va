package game.particle;

import engine.graphics.Mesh;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class ParticleObject {
    public final Vector3d pos;
    public final Vector3i oldFlooredPos;
    public final Vector3f inertia;
    public final Mesh mesh;
    public float light;
    public float timer;
    public float lightUpdateTimer;
    public int key;

    public ParticleObject(Vector3d pos, Vector3f inertia, Mesh mesh, int key){
        this.pos = pos;
        //set to impossible position to initialize a rebuild
        this.oldFlooredPos = new Vector3i(0,-10,0);
        this.inertia = inertia;
        this.mesh = mesh;
        this.timer = (float)Math.random()*2f;
        this.key = key;
        this.light = 15f;
        this.lightUpdateTimer = 0;
    }
}
