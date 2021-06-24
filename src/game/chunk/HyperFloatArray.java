package game.chunk;

//this container is specifically optimized to the most I can get them to for ChunkMeshGenerator
//cache happy
public class HyperFloatArray {
    private static final int growthRate = 1000;

    float[] dataContainer = new float[growthRate];

    int currentPos = 0;
    int maxSize = growthRate;


    //this is old delete it
    private void put(float data){
        if (currentPos == maxSize){
            grow();
        }
        dataContainer[currentPos] = data;
        currentPos++;
    }

    //for positions
    public void pack(float a, float b, float c, float d, float e, float f, float g, float h, float i, float j, float k, float l){
        put(a);
        put(b);
        put(c);
        put(d);
        put(e);
        put(f);
        put(g);
        put(h);
        put(i);
        put(j);
        put(k);
        put(l);
    }

    //for texture coord
    public void pack(float a, float b, float c, float d, float e, float f, float g, float h){
        put(a);
        put(b);
        put(c);
        put(d);
        put(e);
        put(f);
        put(g);
        put(h);
    }

    //for light data
    public void pack(float a){
        for (byte i = 0; i < 12; i++){
            put(a);
        }
    }



    public int size(){
        return currentPos;
    }

    private void grow(){
        float[] newContainer = new float[maxSize + growthRate];

        if (maxSize >= 0) System.arraycopy(dataContainer, 0, newContainer, 0, maxSize);

        dataContainer = null; //send to GC
        dataContainer = newContainer;

        maxSize += growthRate;
    }

    public float[] values(){
        float[] returningContainer = new float[currentPos + 1];
        System.arraycopy(dataContainer, 0, returningContainer, 0, currentPos + 1);
        return returningContainer;
    }

    public void clear(){
        dataContainer = null;
    }
}
