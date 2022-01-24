package game.entity.particle;

import engine.graphics.Mesh;
import game.entity.Entity;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;

public class Particle extends Entity {
    private final Mesh mesh;

    public Particle(Vector3d pos, Vector3f inertia, byte blockID){
        super(4,4,4,4,4,4);

        mesh[thisID] = createParticleMesh(blockID);
    }

    public void cleanParticleMemory(){
        //System.out.println("particles are now shrinking to: " + initialSize);
        //reset value
        currentSize = initialSize;

        //clean up OpenGL memory
        for (Mesh thisMesh : mesh){
            if (thisMesh != null) {
                thisMesh.cleanUp(false);
            }
        }

        //clear memory for GC
        Arrays.fill(exists, false);
        Arrays.fill(position, null);
        Arrays.fill(oldFlooredPosition, null);
        Arrays.fill(inertia, null);
        Arrays.fill(mesh, null);
        Arrays.fill(light, (byte) 0);
        Arrays.fill(timer, 0);
        Arrays.fill(lightUpdateTimer, 0);

        //reset memory
        exists             = new boolean[initialSize];
        position           = new Vector3d[initialSize];
        oldFlooredPosition = new Vector3i[initialSize];
        inertia            = new Vector3f[initialSize];
        mesh               = new Mesh[initialSize];
        light              = new byte[initialSize];
        timer              = new float[initialSize];
        lightUpdateTimer   = new float[initialSize];
    }

    private final Vector3i currentFlooredPos = new Vector3i();

    private final Deque<Integer> deletionQueue = new ArrayDeque<>();

    public void particlesOnStep(){

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
            mesh[key].cleanUp(false);

            exists[key] = false;
            position[key] = null;
            oldFlooredPosition[key] = null;
            inertia[key] = null;
            mesh[key] = null;
            light[key] = 0;
            timer[key] = 0;
            lightUpdateTimer[key] = 0;
        }

    }

    public boolean[] getParticleExistence(){
        return exists;
    }

    public byte getParticleLight(int key){
        return light[key];
    }

    public double getParticlePosX(int key){
        return position[key].x;
    }
    public double getParticlePosY(int key){
        return position[key].y;
    }
    public double getParticlePosZ(int key){
        return position[key].z;
    }

    public Mesh getParticleMesh(int key){
        return mesh[key];
    }

    private Mesh createParticleMesh(byte blockID) {

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