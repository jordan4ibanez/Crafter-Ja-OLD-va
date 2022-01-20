package engine;

import static game.chat.Chat.cleanChatMemory;
import static game.chunk.Chunk.cleanChunkDataMemory;
import static game.clouds.Cloud.cleanCloudMemory;
import static game.crafting.Inventory.cleanInventoryMemory;
import static game.falling.FallingEntity.cleanFallingEntities;
import static game.entity.item.ItemEntity.cleanItemMemory;
import static game.entity.particle.Particle.cleanParticleMemory;
import static game.player.OtherPlayers.cleanOtherPLayerMemory;

public class MemorySweeper {
    public static void cleanMemory(){
        //System.out.println("sweeping memory!");
        cleanItemMemory();
        cleanChunkDataMemory();
        cleanChatMemory();
        cleanCloudMemory();
        cleanInventoryMemory();
        cleanFallingEntities();
        cleanParticleMemory();
        cleanOtherPLayerMemory();
    }
}
