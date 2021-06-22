package game.chunk;

import org.joml.Vector3i;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

import static engine.Time.getDelta;
import static game.chunk.Chunk.chunkStackContainsBlock;
import static game.chunk.ChunkMeshGenerator.generateChunkMesh;

public class ChunkUpdateHandler {


    private final static ConcurrentLinkedDeque<Vector3i> queue = new ConcurrentLinkedDeque<>();


    public static void chunkUpdate( int x, int z , int y){

        Vector3i key = new Vector3i(x, y, z);

        if (!queue.contains(key)) {
            queue.add(new Vector3i(key));
        }
    }

    private static final Random random = new Random();

    private static final float goalTimer = 0.00001f;

    private static float chunkUpdateTimer = 0f;

    public static void chunkUpdater() {

        chunkUpdateTimer += getDelta();
        int updateAmount = 0;

        if (chunkUpdateTimer >= goalTimer){
            updateAmount = (int)(Math.ceil(chunkUpdateTimer / goalTimer));
            chunkUpdateTimer = 0;
        }

        for (int i = 0; i < updateAmount; i++) {
            if (!queue.isEmpty()) {

                Object[] queueAsArray = queue.toArray();
                Vector3i key = (Vector3i) queueAsArray[random.nextInt(queueAsArray.length)];

                if (chunkStackContainsBlock(key.x, key.z, key.y)) {
                    generateChunkMesh(key.x, key.z, key.y);
                }

                queue.remove(key);
            }
        }
    }
}
