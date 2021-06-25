package engine;

import static game.chunk.Chunk.cleanChunkDataMemory;
import static game.item.ItemEntity.cleanItemMemory;

public class MemorySweeper {
    public static void sweepMemory(){
        System.out.println("sweeping memory!");
        cleanItemMemory();
        cleanChunkDataMemory();
    }
}
