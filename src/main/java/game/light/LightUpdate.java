package game.light;

public class LightUpdate {
    public int x;
    public int y;
    public int z;
    public byte level;

    public LightUpdate(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public LightUpdate(int x, int y, int z, byte level){
        this.x = x;
        this.y = y;
        this.z = z;
        this.level = level;
    }
}
