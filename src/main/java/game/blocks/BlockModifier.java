package game.blocks;

public interface BlockModifier {

    default public void onDig(double posX, double posY, double posZ) {

    }

    default public void onPlace(int posX, int posY, int posZ) {

    }

    default public void onRightClick(int posX, int posY, int posZ){

    }
}
