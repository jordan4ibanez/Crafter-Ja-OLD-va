package game.chunk;

import org.joml.Vector3i;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ChunkUpdateHandler {

    private final ConcurrentLinkedDeque<Vector3i> queue = new ConcurrentLinkedDeque<>();

    public void chunkUpdate( int x, int z , int y){
        if (!queue.contains(new Vector3i(x, y, z))) {
            queue.add(new Vector3i(x, y, z));
        }
    }

    private final Random random = new Random();

    public void chunkUpdater() {
        for (int i = 0; i < 200; i++) {
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
