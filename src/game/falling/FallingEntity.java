package game.falling;

import org.joml.Vector3f;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static game.blocks.BlockDefinition.getBlockDefinition;
import static game.blocks.BlockDefinition.getBlockName;
import static game.chunk.Chunk.placeBlock;
import static game.chunk.Chunk.setBlock;
import static game.collision.Collision.applyInertia;
import static game.item.ItemDefinition.getItemDefinition;

public class FallingEntity {
    private final static Map<Integer, FallingEntityObject> objects = new HashMap<>();
    private static int currentID = 0;

    public static void addFallingEntity(Vector3f pos, Vector3f inertia, int blockID){
        objects.put(currentID, new FallingEntityObject(pos, inertia, getItemDefinition(getBlockName(blockID)).mesh, currentID));
        currentID++;
    }

    public static void fallingEntityOnStep(){
        for (FallingEntityObject thisObject : objects.values()){
            applyInertia(thisObject.pos, thisObject.inertia, false, 0.45f, 1f, true, false, true, false);
            if (thisObject.inertia.y == 0){
                placeBlock((int)Math.floor(thisObject.pos.x), (int)Math.floor(thisObject.pos.y), (int)Math.floor(thisObject.pos.z), 23, 0);
                objects.remove(thisObject.key);
                return;
            }
        }
    }

    public static Collection<FallingEntityObject> getFallingEntities(){
        return objects.values();
    }
}
