package engine.disk;

import org.joml.Vector2i;

public record PrimitiveChunkObject(Vector2i pos, byte[] block, byte[] rotation, byte[] light, byte[] heightMap) {}
