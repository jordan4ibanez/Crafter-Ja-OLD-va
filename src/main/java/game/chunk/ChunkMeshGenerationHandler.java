package game.chunk;

import engine.graphics.Mesh;
import engine.graphics.Texture;
import engine.time.Delta;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ChunkMeshGenerationHandler {
    private final Chunk chunk;
    private final Delta delta;
    private final Deque<ChunkMeshDataObject> queue = new ConcurrentLinkedDeque<>();
    private final Texture textureAtlas = new Texture("textures/textureAtlas.png");
    private float chunkUpdateTimer = 0;

    public ChunkMeshGenerationHandler(Chunk chunk, Delta delta){
        this.chunk = chunk;
        this.delta = delta;
    }

    public void addToChunkMeshQueue(ChunkMeshDataObject chunkMeshDataObject){
        queue.add(chunkMeshDataObject);
    }


    public void popChunkMeshQueue(){

        chunkUpdateTimer += delta.getDelta();
        int updateAmount = 0;

        float goalTimer = 0.0003f;
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
                        chunk.setChunkNormalMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, new Mesh(newChunkMeshData.positionsArray, newChunkMeshData.lightArray, newChunkMeshData.indicesArray, newChunkMeshData.textureCoordArray, textureAtlas));
                    } else {
                        chunk.setChunkNormalMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, null);
                    }


                    if (!newChunkMeshData.liquidMeshIsNull) {
                        chunk.setChunkLiquidMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, new Mesh(newChunkMeshData.liquidPositionsArray, newChunkMeshData.liquidLightArray, newChunkMeshData.liquidIndicesArray, newChunkMeshData.liquidTextureCoordArray, textureAtlas));
                    } else {
                        chunk.setChunkLiquidMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, null);
                    }

                    if (!newChunkMeshData.allFacesMeshIsNull) {
                        chunk.setChunkAllFacesMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, new Mesh(newChunkMeshData.allFacesPositionsArray, newChunkMeshData.allFacesLightArray, newChunkMeshData.allFacesIndicesArray, newChunkMeshData.allFacesTextureCoordArray, textureAtlas));
                    } else {
                        chunk.setChunkAllFacesMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, null);
                    }
                }
            } else {
                return;
            }
        }
    }
}
