package game.chunk;

import engine.graphics.Mesh;
import org.joml.Vector2i;
import org.joml.Vector3i;

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

    //internal 3D/2D to 1D calculations

    //internal chunk math
    private Vector3i indexToPos(int i ) {
        final int z = i / 2048;
        i -= (z * 2048);
        final int y = i / 16;
        final int x = i % 16;
        return new Vector3i( x, y, z);
    }

    private int posToIndex2D(int x, int z){
        return (z * 16) + x;
    }

    private int posToIndex2D(Vector2i pos){
        return (pos.y * 16) + pos.x;
    }

    private int posToIndex( int x, int y, int z ) {
        return (z * 2048) + (y * 16) + x;
    }

    private int posToIndex( Vector3i pos ) {
        return (pos.z * 2048) + (pos.y * 16) + pos.x;
    }
}
