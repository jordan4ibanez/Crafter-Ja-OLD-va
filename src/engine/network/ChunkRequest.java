package engine.network;
public class ChunkRequest {
    public int x;
    public int z;

    //null creation
    public ChunkRequest(){
    }

    //data creation
    public ChunkRequest(int x, int z){
        this.x = x;
        this.z = z;
    }
}
