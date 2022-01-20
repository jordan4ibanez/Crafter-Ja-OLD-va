package game.falling;

import engine.graphics.Mesh;
import org.joml.Vector3d;
import org.joml.Vector3f;


import java.util.Arrays;

import static game.chunk.Chunk.getLight;
import static game.chunk.Chunk.placeBlock;
import static game.collision.Collision.applyInertia;

public class FallingEntity {
    private static final int initialSize = 10;
    private static int currentSize = 10;

    private static boolean[] exists = new boolean[initialSize];
    private static Vector3d[] position = new Vector3d[initialSize];
    private static Vector3f[] inertia = new Vector3f[initialSize];
    private static byte[] light = new byte[initialSize];
    private static float[] lightUpdateTimer = new float[initialSize];
    private static Mesh[] mesh = new Mesh[initialSize];
    private static byte[] ID = new byte[initialSize];

    private static int getFreeSlot(){
        for (int i = 0; i < currentSize; i++){
            if (!exists[i]){
                return i;
            }
        }

        //inlined container growth
        return growContainer();
    }

    private static int growContainer(){
        //System.out.println("particle table is growing to: " + (currentSize + 10));
        //ints are only created if arrays need to expand
        //can return current size because it is +1 index of the old size
        int returningSize = currentSize;
        currentSize += 10;

        //new arrays are only created if arrays need to expand
        boolean[]  newExists             = new boolean[currentSize];
        Vector3d[] newPosition           = new Vector3d[currentSize];
        Vector3f[] newInertia            = new Vector3f[currentSize];
        Mesh[]      newMesh              = new Mesh[currentSize];
        byte[]     newLight              = new byte[currentSize];
        float[]    newLightUpdateTimer   = new float[currentSize];
        byte[]      newID                = new byte[currentSize];

        //clone data
        System.arraycopy(exists, 0, newExists, 0, exists.length);
        for (int i = 0; i < position.length; i++){
            newPosition[i] = new Vector3d(position[i]);
        }
        for (int i = 0; i < inertia.length; i++){
            newInertia[i] = new Vector3f(inertia[i]);
        }

        System.arraycopy(mesh, 0, newMesh, 0, exists.length);
        System.arraycopy(light, 0, newLight, 0, exists.length);
        System.arraycopy(lightUpdateTimer, 0, newLightUpdateTimer, 0, exists.length);
        System.arraycopy(ID, 0, newID, 0, ID.length);

        //set data
        exists = newExists;
        position = newPosition;
        inertia = newInertia;
        mesh = newMesh;
        light = newLight;
        lightUpdateTimer = newLightUpdateTimer;
        ID = newID;

        return returningSize;
    }

    public static void createFallingEntity(double posX, double posY, double posZ, float inertiaX, float inertiaY, float inertiaZ, byte newID){
        int thisID = getFreeSlot();
        exists[thisID] = true;
        position[thisID] = new Vector3d(posX, posY, posZ);
        inertia[thisID] = new Vector3f(inertiaX, inertiaY, inertiaZ);
        light[thisID] = getLight((int) Math.floor(posX), (int) Math.floor(posY), (int) Math.floor(posZ));
        ID[thisID] = newID;
    }


    public static void fallingEntityOnStep(){
        int index = 0;
        for (boolean thisExists : exists){
            if (!thisExists){
                index++;
                continue;
            }
            boolean onGround = applyInertia(position[index], inertia[index], false, 0.45f, 1f, true, false, true, false, false);
            if (inertia[index].y == 0 || onGround){
                placeBlock((int)Math.floor(position[index].x), (int)Math.floor(position[index].y), (int)Math.floor(position[index].z), ID[index], (byte) 0);
                destroyFallingEntity(index);
            }
            index++;
        }
    }

    private static void destroyFallingEntity( int thisIndex){

        mesh[thisIndex].cleanUp(false);

        exists[thisIndex] = false;
        position[thisIndex] = null;
        inertia[thisIndex] = null;
        light[thisIndex] = 0;
        lightUpdateTimer[thisIndex] = 0;
        mesh[thisIndex] = null;
        ID[thisIndex] = 0;
    }

    public static void cleanFallingEntities() {
        int index = 0;
        //clean openGL memory
        for (boolean thisExists : exists) {
            if (!thisExists){
                index++;
                continue;
            }
            mesh[index].cleanUp(false);
            index++;
        }

        //clear memory for GC
        Arrays.fill(exists, false);
        Arrays.fill(position, null);
        Arrays.fill(inertia, null);
        Arrays.fill(mesh, null);
        Arrays.fill(light, (byte) 0);
        Arrays.fill(lightUpdateTimer, 0);
        Arrays.fill(ID, (byte) 0);


        //reset memory
        exists             = new boolean[initialSize];
        position           = new Vector3d[initialSize];
        inertia            = new Vector3f[initialSize];
        mesh               = new Mesh[initialSize];
        light              = new byte[initialSize];
        lightUpdateTimer   = new float[initialSize];
        ID                 = new byte[initialSize];
    }

    public static boolean[] getFallingEntities(){
        return exists;
    }

    public static Vector3d getFallingEntityPos(int index){
        return position[index];
    }

    public static byte getFallingEntityBlockID(int index){
        return ID[index];
    }

    public static byte getFallingEntityLight(int index){
        return light[index];
    }
}
