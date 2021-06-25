package game.particle;

import engine.graphics.Mesh;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.*;

import static engine.time.Time.getDelta;
import static game.blocks.BlockDefinition.*;
import static game.chunk.ChunkMeshGenerationHandler.getTextureAtlas;
import static game.collision.ParticleCollision.applyParticleInertia;

public class Particle {
    private final static Int2ObjectArrayMap<ParticleObject> particles = new Int2ObjectArrayMap<>();

    private static int currentID = 0;

    public static void createParticle(Vector3d pos, Vector3f inertia, int blockID){
        particles.put(currentID, new ParticleObject(pos, inertia, createParticleMesh(blockID), currentID));
        currentID++;
    }

    public static void cleanParticleMemory(){
        for (ParticleObject particleObject : particles.values()){
            particleObject.mesh.cleanUp(false);
        }

        particles.clear();
    }

    private static final Deque<Integer> deletionQueue = new ArrayDeque<>();

    public static void particlesOnStep(){

        double delta = getDelta();

        for (ParticleObject thisParticle : particles.values()){
            applyParticleInertia(thisParticle.pos, thisParticle.inertia, true,true,true);

            thisParticle.timer += delta;

            if (thisParticle.timer > 1f){
                deletionQueue.add(thisParticle.key);
            }
        }

        while (!deletionQueue.isEmpty()) {

            int key = deletionQueue.pop();

            ParticleObject thisParticle = particles.get(key);

            if (thisParticle != null && thisParticle.mesh != null) {
                thisParticle.mesh.cleanUp(false);
            }
            particles.remove(key);
        }

    }

    public static Object[] getAllParticles(){
        return particles.values().toArray();
    }

    private static Mesh createParticleMesh(int blockID) {

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
