package game.clouds;

import engine.FastNoise;
import engine.graphics.Mesh;
import org.joml.Vector2i;

import static engine.time.Time.getDelta;
import static game.clouds.CloudMesh.buildCloud2DMesh;
import static game.clouds.CloudMesh.buildCloud3DMesh;
import static game.light.Light.getCurrentGlobalLightLevel;

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
        cloudPos.x = x;
        cloudPos.y = z;
        generateCloudData();
    }

    public static Vector2i getCloudPos(){
        return new Vector2i(cloudPos); //clone memory pointer
    }

    final private static float cloudScale = 16f;

    public static float getCloudScale(){
        return cloudScale;
    }

    public static void scrollClouds(){

    }

    private static Mesh cloud2DMesh = buildCloud2DMesh(getCurrentGlobalLightLevel());
    private static Mesh cloud3DMesh = buildCloud3DMesh(getCurrentGlobalLightLevel());

    public static void rebuildCloudMeshes(){
        if (cloud2DMesh != null){
            cloud2DMesh.cleanUp(false);
        }
        if (cloud3DMesh != null){
            cloud3DMesh.cleanUp(false);
        }
        cloud2DMesh = buildCloud2DMesh(getCurrentGlobalLightLevel());
        cloud3DMesh = buildCloud3DMesh(getCurrentGlobalLightLevel());
    }

    public static Mesh getCloud2DMesh(){
        return cloud2DMesh;
    }

    public static Mesh getCloud3DMesh(){
        return cloud3DMesh;
    }



    private static boolean getIfCloud(byte x, byte z){
        //return  cloudData[cloudPosToIndex(x,z)];
        return cloudData[x][z];
    }

    public static boolean[][] getCloudData(){
        return cloudData;
    }


    //private static short cloudPosToIndex(byte x, byte z){
        //return (short) ((z * 16) + x);
    //}

    private final static FastNoise noise = new FastNoise();


    public static void generateCloudData(){
        for (byte x = 0; x < 16; x++){
            for (byte z = 0; z < 16; z++){
                cloudData[x][z] = noise.GetWhiteNoise(x + cloudPos.x, z + cloudPos.y + cloudOffset) > 0.6f;
            }
        }
    }


}
