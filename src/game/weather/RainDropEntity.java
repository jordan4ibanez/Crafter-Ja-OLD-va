package game.weather;

import org.joml.Vector3d;
import org.joml.Vector3f;

public class RainDropEntity {
    public Vector3d pos;
    public Vector3f inertia;
    public float timer;
    public int key;

    public RainDropEntity(Vector3d pos, Vector3f inertia, int key){
        this.pos = pos;
        this.inertia = inertia;
        this.timer = (float)Math.random()*2f;
        this.key = key;
    }
}
