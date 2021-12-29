package game.chunk;
//the bare minimum "tuple" return
public class ChunkData {
    public int x;
    public int z;
    public byte [] block = new byte[32768];
    public byte[] rotation = new byte[32768];
    public byte[] light = new byte[32768];
    public byte[] heightMap  = new byte[256];
}
