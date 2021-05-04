package game.falling;

import engine.graph.Mesh;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class FallingEntityObject {
    public Vector3d pos;
    public Vector3f inertia;
    public Mesh mesh;
    public int key;

    public FallingEntityObject(Vector3d pos, Vector3f inertia, Mesh mesh, int key){
        this.pos = pos;
        this.inertia = inertia;
        this.mesh = mesh;
        this.key = key;
    }
}
