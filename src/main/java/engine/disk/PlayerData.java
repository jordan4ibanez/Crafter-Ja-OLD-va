package engine.disk;

import org.joml.Vector3d;

public class PlayerData {
    public String name;
    public String[][] inventory;
    public int[][] count;
    public Vector3d pos;
    public byte health;

    public PlayerData(String name, String[][] inventory, int[][] count, Vector3d pos, byte health){
        this.name = name;
        this.inventory = inventory;
        this.count = count;
        this.pos = pos;
        this.health = health;
    }
}
