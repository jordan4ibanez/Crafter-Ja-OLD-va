package game.particle;

import engine.graph.Mesh;
import org.joml.Vector3f;

public class ParticleObject {
    public Vector3f pos;
    public Vector3f inertia;
    public Mesh mesh;
    public float timer;
    public int key;

    public ParticleObject(Vector3f pos, Vector3f inertia, Mesh mesh, int key){
        this.pos = pos;
        this.inertia = inertia;
        this.mesh = mesh;
        this.timer = (float)Math.random()*2f;
        this.key = key;
    }
}
