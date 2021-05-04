package game.item;

import org.joml.Vector3d;
import org.joml.Vector3f;

public interface ItemModifier {
    default public void onPlace(Vector3d pos) {
        System.out.println("placing interface worked");
    }

}
