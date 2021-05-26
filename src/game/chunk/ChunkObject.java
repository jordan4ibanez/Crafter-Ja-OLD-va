package game.chunk;

import engine.graphics.Mesh;

public class ChunkObject {
    public String ID;

    public int x;
    public int z;

    public byte lightLevel = 15;

    public int [] block = new int[128 * 16 * 16];
    public byte[] rotation = new byte[128 * 16 * 16];
    public byte[] light = new byte[128 * 16 * 16];
    public byte[][] heightMap  = new byte[16][16];

    public Mesh[] normalMesh = new Mesh[8];
    public Mesh[] liquidMesh = new Mesh[8];
    public Mesh[] allFacesMesh = new Mesh[8];

    public boolean modified = false;

    public ChunkObject(){

    }

    public ChunkObject(int x, int z){
        this.ID = x + " " + z;

        this.x = x;
        this.z = z;
    }
}
