package game.chunk;

import org.joml.Vector3i;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

import static engine.time.Time.getDelta;
import static game.chunk.Chunk.chunkStackContainsBlock;
import static game.chunk.ChunkMeshGenerator.generateChunkMesh;

public class ChunkUpdateHandler {

    //!!!WARNING!!! This needs to ALWAYS check if value exists, or freeze can occur !!!WARNING!!!
    private static final ConcurrentLinkedDeque<Vector3i> queue = new ConcurrentLinkedDeque<>();

    public static void chunkUpdate( int x, int z , int y){
        if (!queue.contains(new Vector3i(x, y, z))) {
            queue.add(new Vector3i(x, y, z));
        }
    }

    private static final Random random = new Random();

    //private static final float goalTimer = 0.0003f;

    //private static float chunkUpdateTimer = 0f;

    //todo: make this interact with the updates button in the menu
    private static final int MAX_UPDATES_PER_FRAME = 8;

    public static void chunkUpdater() {

        //chunkUpdateTimer += getDelta();
        //int updateAmount = 0;

        //if (chunkUpdateTimer >= goalTimer){
            //updateAmount = (int)(Math.ceil(chunkUpdateTimer / goalTimer));

            //chunkUpdateTimer = 0;
        //}

        for (int i = 0; i < MAX_UPDATES_PER_FRAME; i++) {
            if (!queue.isEmpty()) {

                Vector3i key;
                try {
                    Object[] queueAsArray = queue.toArray();
                    key = (Vector3i) queueAsArray[random.nextInt(queueAsArray.length)];

                    if (key == null){
                        return;
                    }
                } catch (Exception ignored){
                    continue; //let's just keep going
                }

                //sometimes it is null
                if (chunkStackContainsBlock(key.x, key.z, key.y)) {
                    generateChunkMesh(key.x, key.z, key.y);
                }

                //can attempt to remove null, so it's okay
                queue.remove(key);
            } else {
                //stop loop
                return;
            }
        }
    }
}
