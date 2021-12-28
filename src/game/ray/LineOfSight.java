package game.ray;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static game.blocks.BlockDefinition.isBlockWalkable;
import static game.chunk.Chunk.getBlock;

public class LineOfSight {
    private final static Vector3d newPos   = new Vector3d();
    private final static Vector3d realNewPos = new Vector3d();
    private final static Vector3d lastPos  = new Vector3d();
    private final static Vector3d cachePos = new Vector3d();
    private final static Vector3f dir = new Vector3f();

    public static boolean getLineOfSight(Vector3d pos1, Vector3d pos2){
        dir.set(pos2.x - pos1.x, pos2.y-pos1.y, pos2.z - pos1.z).normalize();

        //this does not have to be perfect, can afford float imprecision
        for(float step = 0f; step <= pos1.distance(pos2) ; step += 0.01f) {

            cachePos.x = dir.x * step;
            cachePos.y = dir.y * step;
            cachePos.z = dir.z * step;

            newPos.x = Math.floor(pos1.x + cachePos.x);
            newPos.y = Math.floor(pos1.y + cachePos.y);
            newPos.z = Math.floor(pos1.z + cachePos.z);

            realNewPos.x = pos1.x + cachePos.x;
            realNewPos.y = pos1.y + cachePos.y;
            realNewPos.z = pos1.z + cachePos.z;

            //stop wasting cpu resources
            if (!newPos.equals(lastPos)) {
                byte foundBlock = getBlock((int) newPos.x, (int) newPos.y, (int) newPos.z);
                if (foundBlock > 0 && isBlockWalkable(foundBlock)) {
                    return false;
                }
            }
            lastPos.set(newPos);
        }

        return true;
    }
}
