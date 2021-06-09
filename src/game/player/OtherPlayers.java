package game.player;

import engine.network.PlayerPosObject;
import org.joml.Vector3d;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class OtherPlayers {
    private static final ConcurrentHashMap<String, PlayerPosObject> otherPlayers = new ConcurrentHashMap<>();

    public static Collection<PlayerPosObject> getOtherPlayers(){
        return otherPlayers.values();
    }

    public static void updateOtherPlayer(PlayerPosObject thisPlayerObject){
        //create new object
        if (otherPlayers.get(thisPlayerObject.name) == null){
            otherPlayers.put(thisPlayerObject.name, thisPlayerObject);
        } else {
            PlayerPosObject currentUpdating = otherPlayers.get(thisPlayerObject.name);
            currentUpdating.pos = new Vector3d(thisPlayerObject.pos);
            currentUpdating.cameraRot = thisPlayerObject.cameraRot;
        }
    }
}
