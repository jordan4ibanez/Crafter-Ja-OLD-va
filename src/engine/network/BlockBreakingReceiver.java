package engine.network;

import org.joml.Vector3i;

public class BlockBreakingReceiver {
    public Vector3i receivedPos;

    public BlockBreakingReceiver(){

    }

    public BlockBreakingReceiver(Vector3i newReceivedPos){
        this.receivedPos = newReceivedPos;
    }
}
