package game.particle;

import engine.graphics.Mesh;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.*;

import static engine.Time.getDelta;
import static game.blocks.BlockDefinition.*;
import static game.chunk.ChunkMesh.getTextureAtlas;
import static game.collision.ParticleCollision.applyParticleInertia;

public class Particle {
    private final static Map<Integer, ParticleObject> particles = new HashMap<>();

    private static int currentID = 0;

    public static void createParticle(Vector3d pos, Vector3f inertia, int blockID){
        particles.put(currentID, new ParticleObject(pos, inertia, createParticleMesh(blockID), currentID));
        currentID++;
    }

    private static final Deque<Integer> deletionQueue = new ArrayDeque<>();

    public static void particlesOnStep(){

        float delta = getDelta();

        for (ParticleObject thisParticle : particles.values()){

            applyParticleInertia(thisParticle.pos, thisParticle.inertia, true,true,true);

            thisParticle.timer += delta;

            if (thisParticle.timer > 1f){
                deletionQueue.add(thisParticle.key);
            }
        }

        while (!deletionQueue.isEmpty()) {
            Integer key = deletionQueue.pop();
            ParticleObject deletingParticle = particles.get(key);
            if (deletingParticle != null && deletingParticle.mesh != null) {
                deletingParticle.mesh.cleanUp(false);
            }

            particles.remove(key);
        }

    }

    public static Collection<ParticleObject> getAllParticles(){
        return particles.values();
    }

    private static Mesh createParticleMesh(int blockID) {

        float textureScale = (float)Math.ceil(Math.random() * 3f);
        float pixelScale = (float)(int)textureScale / 25f;

        float pixelX = (float)Math.floor(Math.random()*(16f-(textureScale+1f)));
        float pixelY = (float)Math.floor(Math.random()*(16f-(textureScale+1f)));

        float pixelXMin = pixelX/16f/32f;
        float pixelXMax = (pixelX+textureScale)/16f/32f;

        float pixelYMin = pixelY/16f/32f;
        float pixelYMax = (pixelY+textureScale)/16f/32f;


        ArrayList<Float> positions = new ArrayList<>();
        ArrayList<Float> textureCoord = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();
        ArrayList<Float> light = new ArrayList<>();


        int indicesCount = 0;


        //front
        positions.add(pixelScale);
        positions.add(pixelScale*2);
        positions.add(0f);

        positions.add(-pixelScale);
        positions.add(pixelScale*2);
        positions.add(0f);

        positions.add(-pixelScale);
        positions.add(0f);
        positions.add(0f);

        positions.add(pixelScale);
        positions.add(0f);
        positions.add(0f);

        //front
        float frontLight = 1f;//getLight(x, y, z + 1, chunkX, chunkZ) / maxLight;

        //front
        for (int i = 0; i < 12; i++) {
            light.add(frontLight);
        }
        //front
        indices.add(0);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(0);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);

        //-x +x   -y +y
        // 0  1    2  3

        int selection = (int)Math.floor(Math.random()*6f);

        float[] texturePoints = switch (selection) {
            case 1 -> getBackTexturePoints(blockID, (byte) 0);
            case 2 -> getRightTexturePoints(blockID, (byte) 0);
            case 3 -> getLeftTexturePoints(blockID, (byte) 0);
            case 4 -> getTopTexturePoints(blockID);
            case 5 -> getBottomTexturePoints(blockID);
            default -> getFrontTexturePoints(blockID, (byte) 0);
        };

        // 0, 1,  2, 3
        //-x,+x, -y,+y

        //front
        textureCoord.add(texturePoints[0] + pixelXMax);//1
        textureCoord.add(texturePoints[2] + pixelYMin);//2
        textureCoord.add(texturePoints[0] + pixelXMin);//0
        textureCoord.add(texturePoints[2] + pixelYMin);//2
        textureCoord.add(texturePoints[0] + pixelXMin);//0
        textureCoord.add(texturePoints[2] + pixelYMax);//3
        textureCoord.add(texturePoints[0] + pixelXMax);//1
        textureCoord.add(texturePoints[2] + pixelYMax);//3


        //convert the position objects into usable array
        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i] = positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = textureCoord.get(i);
        }

        return new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray,getTextureAtlas());
    }
}
