package engine;

import static game.chat.Chat.cleanChatMemory;
import static game.chunk.Chunk.cleanChunkDataMemory;
import static game.item.ItemEntity.cleanItemMemory;

public class MemorySweeper {
    public static void sweepMemory(){
        System.out.println("sweeping memory!");
        cleanItemMemory();
        cleanChunkDataMemory();
        cleanChatMemory();
    }
}
