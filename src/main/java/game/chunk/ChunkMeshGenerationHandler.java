package game.chunk;

import engine.graphics.Mesh;
import engine.graphics.Texture;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import static engine.time.Time.getDelta;
import static game.chunk.Chunk.*;

public class ChunkMeshGenerationHandler {

    private static final Deque<ChunkMeshDataObject> queue = new ConcurrentLinkedDeque<>();

    public static void addToChunkMeshQueue(ChunkMeshDataObject chunkMeshDataObject){
        queue.add(chunkMeshDataObject);
    }

    private static final Texture textureAtlas = new Texture("textures/textureAtlas.png");

    public static Texture getTextureAtlas(){
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
            } else {
                return;
            }
        }
    }
}
