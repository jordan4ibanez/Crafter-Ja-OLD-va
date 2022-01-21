package engine.disk;

import org.joml.Vector3d;

public class PlayerDataObject {
    public String name;
    public String[][] inventory;
    public int[][] count;
    public Vector3d pos;
    public byte health;

    public PlayerDataObject(String name, String[][] inventory, int[][] count, Vector3d pos, byte health){
        this.name = name;
        this.inventory = inventory;
        this.count = count;
        this.pos = pos;
        this.health = health;
    }
}
