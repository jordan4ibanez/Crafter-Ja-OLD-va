package game.blocks;

public interface BlockModifier {

    default void onDig(double posX, double posY, double posZ) {

    }

    default void onPlace(int posX, int posY, int posZ) {

    }

    default void onRightClick(int posX, int posY, int posZ){

    }
}
