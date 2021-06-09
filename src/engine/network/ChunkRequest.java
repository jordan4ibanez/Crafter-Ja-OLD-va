package engine.network;
public class ChunkRequest {
    public String name;
    public int x;
    public int z;

    //null creation
    public ChunkRequest(){
    }

    //data creation
    public ChunkRequest(int x, int z, String name){
        this.name = name;
        this.x = x;
        this.z = z;
    }
}
