package game.entity.itemEntity;

import game.blocks.BlockDefinitionContainer;
import org.joml.Vector3i;

public interface ItemModifier {
    default void onPlace(Vector3i pos, Vector3i posAbove, BlockDefinitionContainer blockDefinitionContainer){
        System.out.println("placing interface worked");
    }
    default void onPickUp(Vector3i pos, Vector3i posAbove, BlockDefinitionContainer blockDefinitionContainer){
        System.out.println("I'm picked up woooo");
    }

}
