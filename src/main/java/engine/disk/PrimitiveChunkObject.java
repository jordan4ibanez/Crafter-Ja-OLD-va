package engine.disk;

import org.joml.Vector2i;

public class PrimitiveChunkObject {
    public Vector2i pos;
    public byte[] block;
    public byte[] rotation;
    public byte[] light;
    public byte[] heightMap;
}
