package engine.disk;

import org.joml.Vector2i;

public class PrimitiveChunkObject {
    public Vector2i pos;
    public byte[] block;
    public byte[] rotation;
    public byte[] light;
    public byte[] heightMap;

    public PrimitiveChunkObject(Vector2i pos, byte[] block, byte[] rotation, byte[] light, byte[] heightMap){
        this.pos = pos;
        this.block = block;
        this.rotation = rotation;
        this.light = light;
        this.heightMap = heightMap;
    }
}
