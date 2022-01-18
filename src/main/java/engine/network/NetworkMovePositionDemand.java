package engine.network;

import org.joml.Vector3d;

public class NetworkMovePositionDemand {
    public Vector3d newPos;

    public NetworkMovePositionDemand(){

    }

    public NetworkMovePositionDemand(Vector3d newPos){
        this.newPos = newPos;
    }
}
