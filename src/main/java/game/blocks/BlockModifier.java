package game.blocks;

import game.chunk.Chunk;

public interface BlockModifier {

    default void onDig(double posX, double posY, double posZ, Chunk chunk) {

    }

    default void onPlace(int posX, int posY, int posZ, Chunk chunk) {

    }

    default void onRightClick(int posX, int posY, int posZ, Chunk chunk){

    }
}
