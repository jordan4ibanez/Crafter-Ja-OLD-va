package game.chunk;

import java.util.HashMap;
import java.util.Map;

import static game.chunk.Chunk.chunkStackContainsBlock;
import static game.chunk.ChunkMesh.generateChunkMesh;

public class ChunkUpdateHandler {

    private static final Map<String, ChunkUpdate> queue = new HashMap<>();

    public static void chunkUpdate( int x, int z , int y){
        String keyName = x + " " + z + " " + y;
        if (queue.get(keyName) == null) {
            queue.put(keyName, new ChunkUpdate(x, z, y));
        }
    }
    private static final HashMap<Integer, String> deletionQueue = new HashMap<>();
    public static void chunkUpdater() {
        int currentDeletionCount = 0;
        for (ChunkUpdate thisUpdate : queue.values()) {
            if (!chunkStackContainsBlock(thisUpdate.x, thisUpdate.z, thisUpdate.y)){
                deletionQueue.put(currentDeletionCount, thisUpdate.key);
                currentDeletionCount++;
            } else {
                generateChunkMesh(thisUpdate.x, thisUpdate.z, thisUpdate.y);
                queue.remove(thisUpdate.key);
                break;
            }
        }

        for (String thisKey : deletionQueue.values()){
            queue.remove(thisKey);
        }

        deletionQueue.clear();
    }
}
