package engine;

import static game.chat.Chat.cleanChatMemory;

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
