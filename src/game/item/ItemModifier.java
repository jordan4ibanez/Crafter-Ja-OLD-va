package game.item;

import org.joml.Vector3f;

public interface ItemModifier {
    default public void onPlace(Vector3f pos) {
        System.out.println("placing interface worked");
    }

}
