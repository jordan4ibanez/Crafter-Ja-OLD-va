package game.falling;

import engine.graph.Mesh;
import org.joml.Vector3f;

public class FallingEntityObject {
    public Vector3f pos;
    public Vector3f inertia;
    public Mesh mesh;
    public int key;

    public FallingEntityObject(Vector3f pos, Vector3f inertia, Mesh mesh, int key){
        this.pos = pos;
        this.inertia = inertia;
        this.mesh = mesh;
        this.key = key;
    }
}
