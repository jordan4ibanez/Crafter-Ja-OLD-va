package game.blocks;

import org.joml.Vector3d;
import org.joml.Vector3i;

public interface BlockModifier {
    default public void onDig(Vector3d pos) {

    }

    default public void onPlace(Vector3i pos) {

    }

    default public void onRightClick(Vector3i pos){

    }
}
