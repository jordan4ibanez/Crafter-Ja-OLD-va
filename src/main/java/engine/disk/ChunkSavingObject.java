package engine.disk;

import org.joml.Vector2i;
import org.joml.Vector3d;

public class ChunkSavingObject {
    public Vector2i pos;
    public byte[] block;
    public byte[] rotation;
    public byte[] light;
    public byte[] heightMap;

    public ChunkSavingObject(Vector2i pos, byte[] block, byte[] rotation, byte[] light, byte[] heightMap){
        this.pos = pos;
        this.block = block;
        this.rotation = rotation;
        this.light = light;
        this.heightMap = heightMap;
    }
}
