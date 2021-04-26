package game.chunk;

public class ChunkUpdate {
    int x;
    int z;
    int y;
    float timer;
    String key;

    public ChunkUpdate(int x, int z, int y){
        this.x = x;
        this.z = z;
        this.y = y;
        this.timer = 0;
        this.key = x + " " + z + " " + y;
    }
}
