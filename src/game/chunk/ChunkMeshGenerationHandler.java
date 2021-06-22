package game.chunk;

import engine.graphics.Mesh;
import engine.graphics.Texture;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static engine.Time.getDelta;
import static game.chunk.Chunk.*;

public class ChunkMeshGenerationHandler {

    private static final ConcurrentHashMap<String, ChunkMeshDataObject> queue = new ConcurrentHashMap<>();

    public static void addToChunkMeshQueue(String keyName, ChunkMeshDataObject chunkMeshDataObject){
        queue.put(keyName, chunkMeshDataObject);
    }

    private static Texture textureAtlas;

    static {
        try {
            textureAtlas = new Texture("textures/textureAtlas.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Texture getTextureAtlas(){
        return textureAtlas;
    }

    private static final Random random = new Random();

    private static final float goalTimer = 0.00005f;//goalTimerArray[getSettingsChunkLoad()];

    private static float chunkUpdateTimer = 0;

    public static void popChunkMeshQueue(){

        chunkUpdateTimer += getDelta();
        int updateAmount = 0;

        if (chunkUpdateTimer >= goalTimer){
            updateAmount = (int)(Math.ceil(chunkUpdateTimer / goalTimer));
            chunkUpdateTimer = 0;
        }

        for (int i = 0; i < updateAmount; i++) {
            if (!queue.isEmpty()) {
                Object[] queueAsArray = queue.keySet().toArray();
                String thisKey = (String) queueAsArray[random.nextInt(queueAsArray.length)];

                ChunkMeshDataObject newChunkMeshData = queue.get(thisKey);

                String keyName = newChunkMeshData.chunkX + " " + newChunkMeshData.chunkZ + " " + newChunkMeshData.yHeight;

                queue.remove(keyName);

                if (!newChunkMeshData.normalMeshIsNull) {
                    setChunkNormalMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, new Mesh(newChunkMeshData.positionsArray, newChunkMeshData.lightArray, newChunkMeshData.indicesArray, newChunkMeshData.textureCoordArray, textureAtlas));
                } else {
                    setChunkNormalMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, null);
                }

                if (!newChunkMeshData.liquidMeshIsNull) {
                    setChunkLiquidMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, new Mesh(newChunkMeshData.liquidPositionsArray, newChunkMeshData.liquidLightArray, newChunkMeshData.liquidIndicesArray, newChunkMeshData.liquidTextureCoordArray, textureAtlas));
                } else {
                    setChunkLiquidMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, null);
                }

                if (!newChunkMeshData.allFacesMeshIsNull) {
                    setChunkAllFacesMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, new Mesh(newChunkMeshData.allFacesPositionsArray, newChunkMeshData.allFacesLightArray, newChunkMeshData.allFacesIndicesArray, newChunkMeshData.allFacesTextureCoordArray, textureAtlas));
                } else {
                    setChunkAllFacesMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, null);
                }
            }
        }
    }
}
