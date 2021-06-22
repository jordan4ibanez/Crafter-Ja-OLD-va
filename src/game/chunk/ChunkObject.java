package game.chunk;

import engine.graphics.Mesh;

public class ChunkObject {

    //private final static int arraySize = 128 * 16 * 16;

    public int x;
    public int z;

    public int [] block = new int[32768];
    public byte[] rotation = new byte[32768];
    public byte[] naturalLight = new byte[32768];
    public byte[] torchLight = new byte[32768];
    public byte[][] heightMap  = new byte[16][16];

    public Mesh[] normalMesh = new Mesh[8];
    public Mesh[] liquidMesh = new Mesh[8];
    public Mesh[] allFacesMesh = new Mesh[8];

    public boolean modified = false;

    public ChunkObject(){

    }

    public ChunkObject(int x, int z){

        this.x = x;
        this.z = z;
    }
}
