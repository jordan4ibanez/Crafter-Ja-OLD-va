package game.item;

import org.joml.Vector3i;

public interface ItemModifier {
    default public void onPlace(int posX, int posY, int posZ, int pointedThingAboveX, int pointedThingAboveY, int pointedThingAboveZ) {
        System.out.println("placing interface worked");
    }

}
