package game.item;

import game.blocks.BlockDefinitionContainer;
import org.joml.Vector3i;

public interface ItemModifier {
    default public void onPlace(Vector3i pos, Vector3i pointedThingAbove, BlockDefinitionContainer definitionContainer) {
        System.out.println("placing interface worked");
    }

}
