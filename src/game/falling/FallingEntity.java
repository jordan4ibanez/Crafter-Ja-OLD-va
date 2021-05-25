package game.falling;

import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static game.chunk.Chunk.placeBlock;
import static game.collision.Collision.applyInertia;
import static game.item.ItemDefinition.createItemBlockMeshReturnsMesh;

public class FallingEntity {
    private final static Map<Integer, FallingEntityObject> objects = new HashMap<>();
    private static int currentID = 0;

    public static void addFallingEntity(Vector3d pos, Vector3f inertia, int blockID){
        objects.put(currentID, new FallingEntityObject(pos, inertia, createItemBlockMeshReturnsMesh(blockID), currentID, blockID));
        currentID++;
    }

    public static void fallingEntityOnStep(){
        for (FallingEntityObject thisObject : objects.values()){
            boolean onGround = applyInertia(thisObject.pos, thisObject.inertia, false, 0.45f, 1f, true, false, true, false, false);
            if (thisObject.inertia.y == 0 || onGround){
                placeBlock((int)Math.floor(thisObject.pos.x), (int)Math.floor(thisObject.pos.y), (int)Math.floor(thisObject.pos.z), thisObject.ID, 0);
                if (thisObject.mesh != null){
                    thisObject.mesh.cleanUp(false);
                }
                objects.remove(thisObject.key);
                return;
            }
        }
    }

    public static Collection<FallingEntityObject> getFallingEntities(){
        return objects.values();
    }
}
