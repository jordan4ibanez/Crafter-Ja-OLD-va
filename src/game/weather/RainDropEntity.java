package game.weather;

import engine.graph.Mesh;
import org.joml.Vector3f;

public class RainDropEntity {
    public Vector3f pos;
    public Vector3f inertia;
    public float timer;
    public int key;

    public RainDropEntity(Vector3f pos, Vector3f inertia, int key){
        this.pos = pos;
        this.inertia = inertia;
        this.timer = (float)Math.random()*2f;
        this.key = key;
    }
}
