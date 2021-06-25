package engine;

import static game.chat.Chat.cleanChatMemory;
import static game.chunk.Chunk.cleanChunkDataMemory;
import static game.clouds.Cloud.cleanCloudMemory;
import static game.crafting.Inventory.cleanInventoryMemory;
import static game.falling.FallingEntity.cleanFallingEntities;
import static game.item.ItemEntity.cleanItemMemory;
import static game.particle.Particle.cleanParticleMemory;

public class MemorySweeper {
    public static void sweepMemory(){
        System.out.println("sweeping memory!");
        cleanItemMemory();
        cleanChunkDataMemory();
        cleanChatMemory();
        cleanCloudMemory();
        cleanInventoryMemory();
        cleanFallingEntities();
        cleanParticleMemory();
    }
}
