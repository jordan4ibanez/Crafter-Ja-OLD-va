package game.chunk;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.joml.Vector3i;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

import static engine.time.Time.getDelta;
import static game.chunk.Chunk.chunkStackContainsBlock;
import static game.chunk.ChunkMeshGenerator.generateChunkMesh;

public class ChunkUpdateHandler {

    private static final ObjectOpenHashSet<Vector3i> queue = new ObjectOpenHashSet<>();

    public static void chunkUpdate( int x, int z , int y){
        //queue.remove(new Vector3i(x, y, z));
        queue.add(new Vector3i(x, y, z));
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

                //sometimes it is null
                if (key != null && chunkStackContainsBlock(key.x, key.z, key.y)) {
                    generateChunkMesh(key.x, key.z, key.y);
                }

                //can attempt to remove null, so it's okay
                queue.remove(key);
            } else {
                return;
            }
        }
    }
}
