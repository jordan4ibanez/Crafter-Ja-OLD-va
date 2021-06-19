package engine.network;

import org.joml.Vector3i;

public class BlockPlaceUpdate {
    public Vector3i pos;
    public int ID;
    public byte rot;

    public BlockPlaceUpdate(){

    }

    public BlockPlaceUpdate(Vector3i newReceivedPos, int ID, byte rot){
        this.pos = newReceivedPos;
        this.ID = ID;
        this.rot = rot;
    }
}
