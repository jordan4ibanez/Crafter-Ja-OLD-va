package game.collision;

import org.joml.Vector3f;

public class CollisionMath {

    public static Vector3f floorPos( Vector3f pos){
        pos.x = (float)Math.floor(pos.x);
        pos.y = (float)Math.floor(pos.y);
        pos.z = (float)Math.floor(pos.z);
        return pos;
    }
}
