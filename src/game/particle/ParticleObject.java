package game.particle;

import engine.graphics.Mesh;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class ParticleObject {
    public final Vector3d pos = new Vector3d();
    public final Vector3i oldFlooredPos = new Vector3i();
    public final Vector3f inertia = new Vector3f();
    public final Mesh mesh;
    public float light;
    public float timer;
    public float lightUpdateTimer;
    public int key;

    public ParticleObject(Vector3d pos, Vector3f inertia, Mesh mesh, int key){
        this.pos.set(pos.x,pos.y,pos.z);
        //set to impossible position to initialize a rebuild
        this.oldFlooredPos.set(0,-10,0);
        this.inertia.set(inertia.x, inertia.y, inertia.z);
        this.mesh = mesh;
        this.timer = (float)Math.random()*2f;
        this.key = key;
        this.light = 15f;
        this.lightUpdateTimer = 0;
    }

    public ParticleObject(double x, double y, double z, Vector3f inertia, Mesh mesh, int key){
        this.pos.set(x,y,z);
        //set to impossible position to initialize a rebuild
        this.oldFlooredPos.set(0,-10,0);
        this.inertia.set(inertia.x, inertia.y, inertia.z);
        this.mesh = mesh;
        this.timer = (float)Math.random()*2f;
        this.key = key;
        this.light = 15f;
        this.lightUpdateTimer = 0;
    }

    public ParticleObject(double x, double y, double z, float inertiaX, float inertiaY, float inertiaZ, Mesh mesh, int key){
        this.pos.set(x,y,z);
        //set to impossible position to initialize a rebuild
        this.oldFlooredPos.set(0,-10,0);
        this.inertia.set(inertiaX, inertiaY, inertiaZ);
        this.mesh = mesh;
        this.timer = (float)Math.random()*2f;
        this.key = key;
        this.light = 15f;
        this.lightUpdateTimer = 0;
    }
}
