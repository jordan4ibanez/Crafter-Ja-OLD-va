package game.falling;

import engine.graphics.Mesh;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class FallingEntityObject {
    public Vector3d pos;
    public Vector3f inertia;
    public int mesh;
    public int key;
    public byte ID;

    public FallingEntityObject(Vector3d pos, Vector3f inertia, int mesh, int key, byte ID){
        this.pos = pos;
        this.inertia = inertia;
        this.mesh = mesh;
        this.key = key;
        this.ID = ID;
    }
}
