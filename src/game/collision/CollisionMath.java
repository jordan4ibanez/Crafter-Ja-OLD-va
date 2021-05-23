package game.collision;

import org.joml.Vector3d;

public class CollisionMath {

    public static Vector3d floorPos(Vector3d pos){
        pos.x = Math.floor(pos.x);
        pos.y = Math.floor(pos.y);
        pos.z = Math.floor(pos.z);
        return pos;
    }
}
