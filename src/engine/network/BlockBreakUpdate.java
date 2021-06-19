package engine.network;

import org.joml.Vector3i;

public class BlockBreakUpdate {
    public Vector3i pos;

    public BlockBreakUpdate(){

    }

    public BlockBreakUpdate(Vector3i pos){
        this.pos = pos;
    }
}
