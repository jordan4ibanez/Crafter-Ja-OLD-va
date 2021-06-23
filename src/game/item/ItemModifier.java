package game.item;

import org.joml.Vector3i;

public interface ItemModifier {
    default public void onPlace(Vector3i pos, Vector3i pointedThingAbove) {
        System.out.println("placing interface worked");
    }

}
