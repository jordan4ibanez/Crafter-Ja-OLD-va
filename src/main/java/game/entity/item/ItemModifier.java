package game.entity.item;

import game.blocks.BlockDefinitionContainer;
import org.joml.Vector3i;

public interface ItemModifier {
    default void onPlace(ItemEntity itemEntity){
        System.out.println("placing interface worked");
    }
    default void onPickUp(ItemEntity itemEntity){
        System.out.println("I'm picked up woooo");
    }

}
