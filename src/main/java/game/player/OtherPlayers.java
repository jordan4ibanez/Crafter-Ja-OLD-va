package game.player;

import engine.network.PlayerPosObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.concurrent.ConcurrentLinkedDeque;

public class OtherPlayers {

    private static final ConcurrentLinkedDeque<PlayerPosObject> playerUpdates = new ConcurrentLinkedDeque<>();

    private static final Int2ObjectArrayMap<PlayerObject> otherPlayers = new Int2ObjectArrayMap<>();

    public static Object[] getOtherPlayers(){
        return otherPlayers.values().toArray();
    }


    public static void cleanOtherPLayerMemory(){
        playerUpdates.clear();
        otherPlayers.clear();
    }

    public static void addNewPlayerUpdateData(PlayerPosObject playerPosObject){
        playerUpdates.add(playerPosObject);
    }

    public static void popPlayerUpdateQueue(){
        while (!playerUpdates.isEmpty()){
            PlayerPosObject update = playerUpdates.pop();
            if (update != null){
                updateOtherPlayer(update);
            }
        }
    }

    public static void updateOtherPlayer(PlayerPosObject thisPlayerObject){
        //create new PlayerObject
        if (otherPlayers.get(thisPlayerObject.ID) == null){
            PlayerObject newPlayer = new PlayerObject();
            newPlayer.ID = thisPlayerObject.ID;
            newPlayer.name = thisPlayerObject.name;

            newPlayer.pos = new Vector3d(thisPlayerObject.pos);
            newPlayer.goalPos = new Vector3d(thisPlayerObject.pos);

            newPlayer.camRot = new Vector3f(thisPlayerObject.cameraRot);
            newPlayer.goalCamRot = new Vector3f(thisPlayerObject.cameraRot);

            otherPlayers.put(thisPlayerObject.ID, newPlayer);
        //get and expose data to new player data
        } else {
            PlayerObject updatingPlayer = otherPlayers.get(thisPlayerObject.ID);
            updatingPlayer.goalPos = new Vector3d(thisPlayerObject.pos); //will be interpolated
            updatingPlayer.camRot = thisPlayerObject.cameraRot;
        }
    }
}