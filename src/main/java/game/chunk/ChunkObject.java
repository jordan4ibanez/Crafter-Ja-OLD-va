package game.chunk;

import engine.graphics.Mesh;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkObject {
    private final byte[] block     = new byte[32768];
    private final byte[] rotation  = new byte[32768];
    private final byte[] light     = new byte[32768];
    private final byte[] heightMap = new byte[32768];

    private final Mesh[] normalMesh  = new Mesh[8];
    private final Mesh[] liquidMesh  = new Mesh[8];
    private final Mesh[] allFaceMesh = new Mesh[8];

    private boolean saveToDisk = false;
    private float hover        = -128;

    public Mesh getNormalMesh(int yHeight){
        return normalMesh[yHeight];
    }

    public Mesh getLiquidMesh(int yHeight){
        return liquidMesh[yHeight];
    }

    public Mesh getAllFaceMesh(int yHeight){
        return allFaceMesh[yHeight];
    }

    public Mesh[] getNormalMeshArray(){
        return normalMesh;
    }

    public Mesh[] getLiquidMeshArray(){
        return liquidMesh;
    }

    public Mesh[] getAllFaceMeshArray(){
        return allFaceMesh;
    }

    public Vector2i getKey() {
        return this.key;
    }

    public byte[] getBlock() {
        return this.block;
    }

    public byte[] getRotation() {
        return this.rotation;
    }

    public byte[] getLight() {
        return this.light;
    }

    public byte[] getHeightMap() {
        return this.heightMap;
    }

    public boolean isSaveToDisk() {
        return this.saveToDisk;
    }

    public void setSaveToDisk(boolean truth){
        this.saveToDisk = truth;
    }

    public float getHover(){
        return this.hover;
    }

    public void setHover(float hover){
        this.hover = hover;
    }
}
