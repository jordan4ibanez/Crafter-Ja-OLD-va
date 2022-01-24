package game.clouds;

import engine.FastNoise;
import engine.graphics.Mesh;
import engine.time.Delta;
import org.joml.Vector2i;

public class Cloud {

    private Delta delta;

    public void setDelta(Delta delta){
        if (this.delta == null){
            this.delta = delta;
        }
    }

    private final CloudMesh cloudMesh = new CloudMesh(this);

    //this holds the data from when the clouds are generated overhead
    private final boolean[][] cloudData = new boolean[16][16];

    private final Vector2i cloudPos = new Vector2i(0,0);

    private int cloudOffset = 0;
    private float cloudScroll = 0f;

    public void cleanCloudMemory(){
        cloudScroll = 0;
        cloudOffset = 0;
        cloudPos.x = 0;
        cloudPos.y = 0;
    }

    public void makeCloudsMove(){
        cloudScroll += delta.getDelta();

        if (cloudScroll >= cloudScale){

            cloudScroll -= cloudScale;

            cloudOffset--;

            generateCloudData();
        }
    }

    public float getCloudScroll(){
        return cloudScroll;
    }

    public void setCloudPos(int x, int z){
        cloudPos.set(x,z);
        generateCloudData();
    }

    public Vector2i getCloudPos(){
        return cloudPos;
    }

    final private float cloudScale = 16f;

    public float getCloudScale(){
        return cloudScale;
    }

    private final Mesh cloud2DMesh = this.cloudMesh.buildCloud2DMesh();
    private final Mesh cloud3DMesh = this.cloudMesh.buildCloud3DMesh();

    public Mesh getCloud2DMesh(){
        return cloud2DMesh;
    }

    public Mesh getCloud3DMesh(){
        return cloud3DMesh;
    }


    public boolean[][] getCloudData(){
        return cloudData;
    }

    private final FastNoise noise = new FastNoise();


    public void generateCloudData(){
        for (byte x = 0; x < 16; x++){
            for (byte z = 0; z < 16; z++){
                cloudData[x][z] = noise.GetWhiteNoise(x + cloudPos.x, z + cloudPos.y + cloudOffset) > 0.6f;
            }
        }
    }


}
