package game.particle;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;

import static engine.graphics.Mesh.cleanUpMesh;
import static engine.graphics.Mesh.createMesh;
import static engine.time.Time.getDelta;
import static game.blocks.BlockDefinition.*;
import static game.chunk.Chunk.getLight;
import static game.chunk.ChunkMeshGenerationHandler.getTextureAtlas;
import static game.collision.ParticleCollision.applyParticleInertia;

public class Particle {
    //this is an abstraction of particle objects
    //they exist, but only implicitly
    //this list is synced on the main thread
    private static final int initialSize = 10;
    private static int currentSize = 10;

    private static boolean[]  exists             = new boolean[initialSize];
    private static Vector3d[] position           = new Vector3d[initialSize];
    private static Vector3i[] oldFlooredPosition = new Vector3i[initialSize];
    private static Vector3f[] inertia            = new Vector3f[initialSize];
    private static int[]      mesh               = new int[initialSize];
    private static byte[]     light              = new byte[initialSize];
    private static float[]    timer              = new float[initialSize];
    private static float[]    lightUpdateTimer   = new float[initialSize];

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
        Vector3i[] newOldFlooredPosition = new Vector3i[currentSize];
        Vector3f[] newInertia            = new Vector3f[currentSize];
        int[]      newMesh               = new int[currentSize];
        byte[]     newLight              = new byte[currentSize];
        float[]    newTimer              = new float[currentSize];
        float[]    newLightUpdateTimer   = new float[currentSize];

        //clone data
        System.arraycopy(exists, 0, newExists, 0, exists.length);
        for (int i = 0; i < position.length; i++){
            newPosition[i] = new Vector3d(position[i]);
        }
        for (int i = 0; i < oldFlooredPosition.length; i++){
            newOldFlooredPosition[i] = new Vector3i(oldFlooredPosition[i]);
        }
        for (int i = 0; i < inertia.length; i++){
            newInertia[i] = new Vector3f(inertia[i]);
        }
        System.arraycopy(mesh, 0, newMesh, 0, exists.length);
        System.arraycopy(light, 0, newLight, 0, exists.length);
        System.arraycopy(timer, 0, newTimer, 0, exists.length);
        System.arraycopy(lightUpdateTimer, 0, newLightUpdateTimer, 0, exists.length);

        //set data
        exists = newExists;
        position = newPosition;
        oldFlooredPosition = newOldFlooredPosition;
        inertia = newInertia;
        mesh = newMesh;
        light = newLight;
        timer = newTimer;
        lightUpdateTimer = newLightUpdateTimer;

        return returningSize;
    }

    public static void createParticle(double posX, double posY, double posZ, float inertiaX, float inertiaY, float inertiaZ, byte blockID){
        int thisID = getFreeSlot();
        exists[thisID] = true;
        position[thisID] = new Vector3d(posX, posY, posZ);
        oldFlooredPosition[thisID] = new Vector3i(0,-10,0);
        inertia[thisID] = new Vector3f(inertiaX,inertiaY,inertiaZ);
        mesh[thisID] = createParticleMesh(blockID);
        light[thisID] = (byte) 15; //this should probably check automagically
        timer[thisID] = (float)Math.random()*2f;
        lightUpdateTimer[thisID] = 0f;
    }

    public static void cleanParticleMemory(){
        //System.out.println("particles are now shrinking to: " + initialSize);
        //reset value
        currentSize = initialSize;

        //clean up OpenGL memory
        for (int thisMesh : mesh){
            cleanUpMesh(thisMesh, false);
        }

        //clear memory for GC
        Arrays.fill(exists, false);
        Arrays.fill(position, null);
        Arrays.fill(oldFlooredPosition, null);
        Arrays.fill(inertia, null);
        Arrays.fill(mesh, 0);
        Arrays.fill(light, (byte) 0);
        Arrays.fill(timer, 0);
        Arrays.fill(lightUpdateTimer, 0);

        //reset memory
        exists             = new boolean[initialSize];
        position           = new Vector3d[initialSize];
        oldFlooredPosition = new Vector3i[initialSize];
        inertia            = new Vector3f[initialSize];
        mesh               = new int[initialSize];
        light              = new byte[initialSize];
        timer              = new float[initialSize];
        lightUpdateTimer   = new float[initialSize];
    }

    private static final Vector3i currentFlooredPos = new Vector3i();

    private static final Deque<Integer> deletionQueue = new ArrayDeque<>();

    public static void particlesOnStep(){

        double delta = getDelta();

        int currentID = 0;

        for (boolean particleExists : exists){
            if (!particleExists){
                currentID++;
                continue;
            }
            applyParticleInertia(position[currentID], inertia[currentID], true,true);

            float newTimer = (float) (timer[currentID] + delta);
            timer[currentID] = newTimer;

            currentFlooredPos.set((int)Math.floor(position[currentID].x), (int)Math.floor(position[currentID].y), (int)Math.floor(position[currentID].z));

            //poll local light every quarter second
            if (lightUpdateTimer[currentID] >= 0.25f || !currentFlooredPos.equals(oldFlooredPosition[currentID])){

                light[currentID] = getLight(currentFlooredPos.x, currentFlooredPos.y, currentFlooredPos.z);

                lightUpdateTimer[currentID] = 0f;
            }

            if (newTimer > 1f){
                deletionQueue.add(currentID);
            }

            oldFlooredPosition[currentID].set(currentFlooredPos.x,currentFlooredPos.y,currentFlooredPos.z);
            currentID++;
        }

        while (!deletionQueue.isEmpty()) {

            int key = deletionQueue.pop();

            //this must delete the pointers in the C and OpenGL stack
            cleanUpMesh(mesh[key],false);

            exists[key] = false;
            position[key] = null;
            oldFlooredPosition[key] = null;
            inertia[key] = null;
            mesh[key] = 0;
            light[key] = 0;
            timer[key] = 0;
            lightUpdateTimer[key] = 0;
        }

    }

    public static boolean[] getParticleExistence(){
        return exists;
    }

    public static byte getParticleLight(int key){
        return light[key];
    }

    public static double getParticlePosX(int key){
        return position[key].x;
    }
    public static double getParticlePosY(int key){
        return position[key].y;
    }
    public static double getParticlePosZ(int key){
        return position[key].z;
    }

    public static int getParticleMesh(int key){
        return mesh[key];
    }

    private static int createParticleMesh(byte blockID) {

        final float textureScale = (float)Math.ceil(Math.random() * 3f);
        final float pixelScale = (float)(int)textureScale / 25f;

        final float pixelX = (float)Math.floor(Math.random()*(16f-(textureScale+1f)));
        final float pixelY = (float)Math.floor(Math.random()*(16f-(textureScale+1f)));

        final float pixelXMin = pixelX/16f/32f;
        final float pixelXMax = (pixelX+textureScale)/16f/32f;

        final float pixelYMin = pixelY/16f/32f;
        final float pixelYMax = (pixelY+textureScale)/16f/32f;


        final float[] positions    = new float[12];
        final float[] textureCoord = new float[8];
        final int[] indices        = new int[6];
        final float[] light        = new float[12];

        //front
        positions[0]  = (pixelScale);
        positions[1]  = (pixelScale*2);
        positions[2]  = (0f);
        positions[3]  = (-pixelScale);
        positions[4]  = (pixelScale*2);
        positions[5]  = (0f);
        positions[6]  = (-pixelScale);
        positions[7]  = (0f);
        positions[8]  = (0f);
        positions[9]  = (pixelScale);
        positions[10] = (0f);
        positions[11] = (0f);
        
        //front
        for (int i = 0; i < 12; i++) {
            light[i] = 1;
        }
        //front
        indices[0] = (0);
        indices[1] = (1);
        indices[2] = (2);
        indices[3] = (0);
        indices[4] = (2);
        indices[5] = (3);

        final int selection = (int)Math.floor(Math.random()*6f);

        float[] texturePoints = switch (selection) {
            case 1 -> getBackTexturePoints(blockID, (byte) 0);
            case 2 -> getRightTexturePoints(blockID, (byte) 0);
            case 3 -> getLeftTexturePoints(blockID, (byte) 0);
            case 4 -> getTopTexturePoints(blockID);
            case 5 -> getBottomTexturePoints(blockID);
            default -> getFrontTexturePoints(blockID, (byte) 0);
        };


        //front
        textureCoord[0] = (texturePoints[0] + pixelXMax);//1
        textureCoord[1] = (texturePoints[2] + pixelYMin);//2
        textureCoord[2] = (texturePoints[0] + pixelXMin);//0
        textureCoord[3] = (texturePoints[2] + pixelYMin);//2
        textureCoord[4] = (texturePoints[0] + pixelXMin);//0
        textureCoord[5] = (texturePoints[2] + pixelYMax);//3
        textureCoord[6] = (texturePoints[0] + pixelXMax);//1
        textureCoord[7] = (texturePoints[2] + pixelYMax);//3

        return createMesh(positions, light, indices, textureCoord, getTextureAtlas());
    }
}