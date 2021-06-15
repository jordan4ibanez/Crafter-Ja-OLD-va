package engine.network;

import org.joml.Vector3i;

public class BlockPlacingReceiver {
    public Vector3i receivedPos;
    public int ID;
    public byte rotation;

    public BlockPlacingReceiver(){

    }

    public BlockPlacingReceiver(Vector3i newReceivedPos, int ID, byte rotation){
        this.receivedPos = newReceivedPos;
        this.ID = ID;
        this.rotation = rotation;
    }
}
