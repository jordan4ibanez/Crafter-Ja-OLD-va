package game.blocks;

import org.joml.Vector3f;

public interface BlockModifier {
    default public void onDig(Vector3f pos) throws Exception {
//        System.out.println("digging interface worked");
    }

    default public void onPlace(Vector3f pos) {

    }

    default public void onRightClick(Vector3f pos){

    }
}
