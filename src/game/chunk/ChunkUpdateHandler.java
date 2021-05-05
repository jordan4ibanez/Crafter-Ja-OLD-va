package game.chunk;

import java.util.concurrent.ConcurrentHashMap;

import static game.chunk.Chunk.chunkStackContainsBlock;
import static game.chunk.ChunkMesh.generateChunkMesh;

public class ChunkUpdateHandler {

    private static final ConcurrentHashMap<String, ChunkUpdate> queue = new ConcurrentHashMap<String, ChunkUpdate>();

    public static void chunkUpdate( int x, int z , int y){
        String keyName = x + " " + z + " " + y;
        if (queue.get(keyName) == null) {
            queue.put(keyName, new ChunkUpdate(x, z, y));
        }
    }

    public static void chunkUpdater() {
        if (!queue.isEmpty()){
            String key = "";
            ChunkUpdate thisUpdate = queue.get(queue.keySet().toArray()[0]);
            if (!chunkStackContainsBlock(thisUpdate.x, thisUpdate.z, thisUpdate.y)){
                key = thisUpdate.key;
            } else {
                generateChunkMesh(thisUpdate.x, thisUpdate.z, thisUpdate.y);
                queue.remove(thisUpdate.key);
            }
            if (!key.equals("")) {
                queue.remove(key);
            }
        }
    }
}
