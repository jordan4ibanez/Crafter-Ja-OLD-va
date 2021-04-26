package game.chunk;

import engine.graph.Mesh;

public class ChunkObject {
    public String ID;

    public int x;
    public int z;

    public int [][][] block;
    public byte[][][] rotation;
    public byte[][][] light;
    public byte[][] heightMap;

    public Mesh[] mesh;
    public Mesh[] liquidMesh;
    public Mesh[] blockBoxMesh;
    public boolean modified = false;

    public ChunkObject(){

    }

    public ChunkObject(int x, int z){
        this.ID = x + " " + z;

        this.x = x;
        this.z = z;

        this.block    = new int [128][16][16];
        this.rotation = new byte[128][16][16];
        this.light    = new byte[128][16][16];
        this.heightMap = new byte[16][16];

        this.mesh       = new Mesh[8];
        this.liquidMesh = new Mesh[8];
        this.blockBoxMesh = new Mesh[8];
    }
}
