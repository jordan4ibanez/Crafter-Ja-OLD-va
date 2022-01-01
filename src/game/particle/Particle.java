package game.particle;

import engine.graphics.Mesh;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Set;

import static engine.time.Time.getDelta;
import static game.blocks.BlockDefinition.*;
import static game.chunk.Chunk.getLight;
import static game.chunk.ChunkMeshGenerationHandler.getTextureAtlas;
import static game.collision.ParticleCollision.applyParticleInertia;

public class Particle {
    //this is an abstraction of particle objects
    //they exist, but only implicitly
    //this list is synced on the main thread
    private static final HashMap<Integer, Vector3d> position = new HashMap<>();
    private static final HashMap<Integer, Vector3i> oldFlooredPosition = new HashMap<>();
    private static final HashMap<Integer, Vector3f> inertia = new HashMap<>();
    private static final HashMap<Integer, Mesh> mesh = new HashMap<>();

    private static final HashMap<Integer, Byte> light = new HashMap<>();
    private static final HashMap<Integer, Float> timer = new HashMap<>();
    private static final HashMap<Integer, Float> lightUpdateTimer = new HashMap<>();

    private static int currentID = 0;

    public static void createParticle(double posX, double posY, double posZ, float inertiaX, float inertiaY, float inertiaZ, byte blockID){
        position.put(currentID, new Vector3d(posX,posY,posZ));
        oldFlooredPosition.put(currentID, new Vector3i(0,-10,0));
        inertia.put(currentID, new Vector3f(inertiaX,inertiaY,inertiaZ));
        mesh.put(currentID,createParticleMesh(blockID));

        light.put(currentID, (byte) 15); //this should probably check automagically
        timer.put(currentID, (float)Math.random()*2f);
        lightUpdateTimer.put(currentID, 0f);

        currentID++;
    }

    public static void cleanParticleMemory(){
        for (Mesh thisMesh : mesh.values()){
            thisMesh.cleanUp(false);
        }

        position.clear();
        oldFlooredPosition.clear();
        inertia.clear();
        mesh.clear();
        light.clear();
        timer.clear();
        lightUpdateTimer.clear();
    }

    private static final Vector3i currentFlooredPos = new Vector3i();

    private static final Deque<Integer> deletionQueue = new ArrayDeque<>();

    public static void particlesOnStep(){

        double delta = getDelta();

        for (int i : position.keySet()){
            applyParticleInertia(position.get(i),inertia.get(i), true,true);

            float newTimer = (float) (timer.get(i) + delta);
            timer.put(i,newTimer);

            currentFlooredPos.set((int)Math.floor(position.get(i).x), (int)Math.floor(position.get(i).y), (int)Math.floor(position.get(i).z));

            //poll local light every quarter second
            if (lightUpdateTimer.get(i) >= 0.25f || !currentFlooredPos.equals(oldFlooredPosition.get(i))){

                light.put(i, getLight(currentFlooredPos.x, currentFlooredPos.y, currentFlooredPos.z));

                lightUpdateTimer.put(i,0f);
            }

            if (newTimer > 1f){
                deletionQueue.add(i);
            }

            oldFlooredPosition.get(i).set(currentFlooredPos.x,currentFlooredPos.y,currentFlooredPos.z);
        }

        while (!deletionQueue.isEmpty()) {

            int key = deletionQueue.pop();

            //this must delete the pointers in the C and OpenGL stack
            if (mesh.get(key) != null) {
                mesh.get(key).cleanUp(false);
            }

            position.remove(key);
            oldFlooredPosition.remove(key);
            inertia.remove(key);
            mesh.remove(key);
            light.remove(key);
            timer.remove(key);
            lightUpdateTimer.remove(key);
        }

    }

    public static Set<Integer> getParticleKeys(){
        return position.keySet();
    }

    public static byte getParticleLight(int key){
        return light.get(key);
    }

    public static double getParticlePosX(int key){
        return position.get(key).x;
    }
    public static double getParticlePosY(int key){
        return position.get(key).y;
    }
    public static double getParticlePosZ(int key){
        return position.get(key).z;
    }

    public static Mesh getParticleMesh(int key){
        return mesh.get(key);
    }

    private static Mesh createParticleMesh(byte blockID) {

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

        return new Mesh(positions, light, indices, textureCoord, getTextureAtlas());
    }
}