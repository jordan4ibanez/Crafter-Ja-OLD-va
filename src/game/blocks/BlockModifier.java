package game.blocks;

import org.joml.Vector3d;
import org.joml.Vector3f;

public interface BlockModifier {
    default public void onDig(Vector3d pos) throws Exception {

    }

    default public void onPlace(Vector3d pos) {

    }

    default public void onRightClick(Vector3d pos){

    }
}
