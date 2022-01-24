package game.chunk;

import engine.graphics.Mesh;
import engine.graphics.Texture;
import engine.time.Delta;
import org.joml.Vector3i;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ChunkUpdateHandler {
    private Chunk chunk;
    private Delta delta;

    private final ConcurrentLinkedDeque<Vector3i> generationQueue = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<ChunkMeshData> dataQueue = new ConcurrentLinkedDeque<>();

    private final Texture textureAtlas = new Texture("textures/textureAtlas.png");
    private float chunkUpdateTimer = 0;

    public ChunkUpdateHandler(Chunk chunk, Delta delta){

    }

    public void setChunk(Chunk chunk){
        if (this.chunk == null) {
            this.chunk = chunk;
        }
    }
    public void setDelta(Delta delta){
        if (this.delta == null){
            this.delta = delta;
        }
    }

    public void chunkUpdate( int x, int z , int y){
        if (!generationQueue.contains(new Vector3i(x, y, z))) {
            generationQueue.add(new Vector3i(x, y, z));
        }
    }

    private final Random random = new Random();

    public void chunkUpdater() {
        if (generationQueue.isEmpty()){
            return;
        }
        for (int i = 0; i < 200; i++) {
            if (generationQueue.isEmpty()) {
                return;
            }

            Vector3i key;
            try {
                Object[] queueAsArray = generationQueue.toArray();
                key = (Vector3i) queueAsArray[random.nextInt(queueAsArray.length)];

                if (key == null){
                    return;
                }
            } catch (Exception ignored){
                continue; //let's just keep going
            }

            //sometimes it is null
            /*
            if (this.chunk.chunkStackContainsBlock(key.x, key.z, key.y)) {
                generateChunkMesh(key.x, key.z, key.y);
            }
             */

            //can attempt to remove null, so it's okay
            generationQueue.remove(key);
        }
    }


    //chunk mesh generator handling

    public void addToChunkMeshQueue(ChunkMeshData chunkMeshData){
        dataQueue.add(chunkMeshData);
    }


    public void popChunkMeshQueue(){

        if (dataQueue.isEmpty()){
            return;
        }

        chunkUpdateTimer += delta.getDelta();
        int updateAmount = 0;

        float goalTimer = 0.0003f;
        if (chunkUpdateTimer >= goalTimer){
            updateAmount = (int)(Math.ceil(chunkUpdateTimer / goalTimer));
            chunkUpdateTimer = 0;
        }


        for (int i = 0; i < updateAmount; i++) {
            if (dataQueue.isEmpty()) {
                return;
            }

            //System.out.println("ChunkMesh Setting QueueSize: " + queue.size());

            ChunkMeshData newChunkMeshData = dataQueue.pop();

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
        }
    }
}
