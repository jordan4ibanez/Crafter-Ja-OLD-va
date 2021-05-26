package game.particle;

import engine.graphics.Mesh;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class ParticleObject {
    public Vector3d pos;
    public Vector3f inertia;
    public Mesh mesh;
    public float timer;
    public int key;

    public ParticleObject(Vector3d pos, Vector3f inertia, Mesh mesh, int key){
        this.pos = pos;
        this.inertia = inertia;
        this.mesh = mesh;
        this.timer = (float)Math.random()*2f;
        this.key = key;
    }
}
