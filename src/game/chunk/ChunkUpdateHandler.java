package game.chunk;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static engine.Time.getDelta;
import static engine.settings.Settings.getSettingsChunkLoad;
import static game.chunk.Chunk.chunkStackContainsBlock;
import static game.chunk.Chunk.updateChunkUnloadingSpeed;
import static game.chunk.ChunkMesh.generateChunkMesh;
import static game.chunk.ChunkMesh.updateChunkMeshLoadingSpeed;

public class ChunkUpdateHandler {

    private static final ConcurrentHashMap<String, ChunkUpdate> queue = new ConcurrentHashMap<>();

    public static void chunkUpdate( int x, int z , int y){
        String keyName = x + " " + z + " " + y;
        if (queue.get(keyName) == null) {
            queue.put(keyName, new ChunkUpdate(x, z, y));
        }
    }

    private static final Random random = new Random();

    //the higher this is set, the lazier chunk mesh loading gets
    //set it too high, and chunk mesh loading barely works
    private static final float[] goalTimerArray = new float[]{
            0.05f, //SNAIL
            0.025f, //SLOWER
            0.009f, //NORMAL
            0.004f, //FASTER
            0.002f, //INSANE
            0.0005f, //FUTURE PC
    };

    private static float goalTimer = goalTimerArray[getSettingsChunkLoad()];

    public static void updateChunkLoadingSpeed(){
        goalTimer = goalTimerArray[getSettingsChunkLoad()];
        //also update unload speed timer
        updateChunkUnloadingSpeed();
        //also update chunk mesh loading speed timer
        updateChunkMeshLoadingSpeed();
    }

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
                String key = "";

                Object[] queueAsArray = queue.keySet().toArray();
                String thisKey = (String)queueAsArray[random.nextInt(queueAsArray.length)];

                ChunkUpdate thisUpdate = queue.get(thisKey);

                if (!chunkStackContainsBlock(thisUpdate.x, thisUpdate.z, thisUpdate.y)) {
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
}
