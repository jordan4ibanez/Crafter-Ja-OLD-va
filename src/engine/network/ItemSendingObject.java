package engine.network;

import org.joml.Vector3d;

public class ItemSendingObject {
    public Vector3d pos;
    public String name;
    public int ID;

    //null initializer
    public ItemSendingObject(){

    }

    public ItemSendingObject(Vector3d pos, int ID, String name){
        this.pos = pos;
        this.ID = ID;
        this.name = name;
    }
}
