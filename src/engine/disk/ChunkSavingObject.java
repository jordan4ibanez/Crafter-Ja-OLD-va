package engine.disk;

public class ChunkSavingObject {
    public String ID;

    public int x;
    public int z;

    public int [][][] block;
    public byte[][][] rotation;
    public byte[][][] light;
    public byte[][] heightMap;

    public boolean modified = false;
}
