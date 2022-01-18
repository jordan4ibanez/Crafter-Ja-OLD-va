package game.chunk;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import static engine.graphics.Mesh.createMesh;
import static engine.graphics.Texture.createTexture;
import static engine.time.Time.getDelta;
import static game.chunk.Chunk.*;

public class ChunkMeshGenerationHandler {

    private static final Deque<ChunkMeshDataObject> queue = new ConcurrentLinkedDeque<>();

    public static void addToChunkMeshQueue(ChunkMeshDataObject chunkMeshDataObject){
        queue.add(chunkMeshDataObject);
    }

    private static final int textureAtlas = createTexture("textures/textureAtlas.png");

    public static int getTextureAtlas(){
        return textureAtlas;
    }

    private static final float goalTimer = 0.0003f;

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

                //System.out.println("ChunkMesh Setting QueueSize: " + queue.size());

                ChunkMeshDataObject newChunkMeshData = queue.pop();

                if (newChunkMeshData != null) {

                    if (!newChunkMeshData.normalMeshIsNull) {
                        setChunkNormalMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, createMesh(newChunkMeshData.positionsArray, newChunkMeshData.lightArray, newChunkMeshData.indicesArray, newChunkMeshData.textureCoordArray, textureAtlas));
                    } else {
                        setChunkNormalMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, 0);
                    }


                    if (!newChunkMeshData.liquidMeshIsNull) {
                        setChunkLiquidMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, createMesh(newChunkMeshData.liquidPositionsArray, newChunkMeshData.liquidLightArray, newChunkMeshData.liquidIndicesArray, newChunkMeshData.liquidTextureCoordArray, textureAtlas));
                    } else {
                        setChunkLiquidMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, 0);
                    }

                    if (!newChunkMeshData.allFacesMeshIsNull) {
                        setChunkAllFacesMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, createMesh(newChunkMeshData.allFacesPositionsArray, newChunkMeshData.allFacesLightArray, newChunkMeshData.allFacesIndicesArray, newChunkMeshData.allFacesTextureCoordArray, textureAtlas));
                    } else {
                        setChunkAllFacesMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, 0);
                    }


                    //todo: test if nullifying data then object reduces heap memory gc sweep pause

                }
            } else {
                return;
            }
        }
    }
}