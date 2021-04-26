package game.particle;

import engine.graph.Mesh;
import engine.graph.Texture;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static game.blocks.BlockDefinition.*;
import static game.chunk.ChunkMesh.getTextureAtlas;
import static game.collision.ParticleCollision.applyParticleInertia;

public class Particle {
    private final static Map<Integer, ParticleObject> particles = new HashMap<>();

    private static int currentID = 0;

    public static void createParticle(Vector3f pos, Vector3f inertia, int blockID){
        particles.put(currentID, new ParticleObject(pos, inertia, createParticleMesh(blockID), currentID));
        currentID++;
    }

    public static void particlesOnStep(){
        for (ParticleObject thisParticle : particles.values()){
            applyParticleInertia(thisParticle.pos, thisParticle.inertia, true,true,true);
            thisParticle.timer += 0.01f;
            if (thisParticle.timer > 10f){
                particles.remove(thisParticle.key);
                return;
            }
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


        ArrayList positions = new ArrayList();
        ArrayList textureCoord = new ArrayList();
        ArrayList indices = new ArrayList();
        ArrayList light = new ArrayList();


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
        indices.add(0 + indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(0 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);

        indicesCount += 4;

        //-x +x   -y +y
        // 0  1    2  3

        int selection = (int)Math.floor(Math.random()*6f);

        float[] texturePoints;

        switch (selection){
            case 0:
                texturePoints = getFrontTexturePoints(blockID,(byte) 0);
                break;
            case 1:
                texturePoints = getBackTexturePoints(blockID,(byte) 0);
                break;
            case 2:
                texturePoints = getRightTexturePoints(blockID,(byte) 0);
                break;
            case 3:
                texturePoints = getLeftTexturePoints(blockID,(byte) 0);
                break;
            case 4:
                texturePoints = getTopTexturePoints(blockID);
                break;
            case 5:
                texturePoints = getBottomTexturePoints(blockID);
                break;
            default:
                texturePoints = getFrontTexturePoints(blockID,(byte) 0);
        }

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
            positionsArray[i] = (float) positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = (float) light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = (int) indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = (float) textureCoord.get(i);
        }

        return new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray,getTextureAtlas());
    }
}
