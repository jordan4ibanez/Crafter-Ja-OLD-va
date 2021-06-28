package game.chunk;

import engine.graphics.Mesh;
//a simple, small container object
public class ChunkMeshObject {
    public int x;
    public int z;
    public Mesh[] normalMesh = new Mesh[8];
    public Mesh[] liquidMesh = new Mesh[8];
    public Mesh[] allFacesMesh = new Mesh[8];

    public ChunkMeshObject(int x, int z){
        this.x = x;
        this.z = z;
    }
}
