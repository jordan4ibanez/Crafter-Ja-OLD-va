package game.mob;

import org.joml.Vector3f;

public interface MobInterface {
    public default void onTick(MobObject thisObject){
    }
}
