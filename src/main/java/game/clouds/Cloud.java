package game.clouds;

import engine.FastNoise;
import engine.graphics.Mesh;
import org.joml.Vector2i;

import static engine.time.Time.getDelta;
import static game.clouds.CloudMesh.buildCloud2DMesh;
import static game.clouds.CloudMesh.buildCloud3DMesh;

public class Cloud {

    //this holds the data from when the clouds are generated overhead
    private static final boolean[][] cloudData = new boolean[16][16];

    private static final Vector2i cloudPos = new Vector2i(0,0);

    private static int cloudOffset = 0;
    private static float cloudScroll = 0f;

    public static void cleanCloudMemory(){
        cloudScroll = 0;
        cloudOffset = 0;
        cloudPos.x = 0;
        cloudPos.y = 0;
    }

    public static void makeCloudsMove(){
        cloudScroll += getDelta();

        if (cloudScroll >= cloudScale){

            cloudScroll -= cloudScale;

            cloudOffset--;

            generateCloudData();
        }
    }

    public static float getCloudScroll(){
        return cloudScroll;
    }

    public static void setCloudPos(int x, int z){
        cloudPos.set(x,z);
        generateCloudData();
    }

    public static Vector2i getCloudPos(){
        return cloudPos;
    }

    final private static float cloudScale = 16f;

    public static float getCloudScale(){
        return cloudScale;
    }

    private static final Mesh cloud2DMesh = buildCloud2DMesh();
    private static final Mesh cloud3DMesh = buildCloud3DMesh();

    public static Mesh getCloud2DMesh(){
        return cloud2DMesh;
    }

    public static Mesh getCloud3DMesh(){
        return cloud3DMesh;
    }


    public static boolean[][] getCloudData(){
        return cloudData;
    }

    private final static FastNoise noise = new FastNoise();


    public static void generateCloudData(){
        for (byte x = 0; x < 16; x++){
            for (byte z = 0; z < 16; z++){
                cloudData[x][z] = noise.GetWhiteNoise(x + cloudPos.x, z + cloudPos.y + cloudOffset) > 0.6f;
            }
        }
    }


}
