package engine.network;

import org.joml.Vector3i;

public class BlockPlacingReceiver {
    public Vector3i receivedPos;

    public BlockPlacingReceiver(){

    }
    
    public BlockPlacingReceiver(Vector3i newReceivedPos){
        this.receivedPos = newReceivedPos;
    }
}
