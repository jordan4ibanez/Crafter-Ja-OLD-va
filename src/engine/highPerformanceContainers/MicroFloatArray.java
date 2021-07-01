package engine.highPerformanceContainers;

public class MicroFloatArray {
    //growth rate defines how big the cache trim is
    //defines the initial size, limit of currentPos before it expands
    //and when it does expand, the array will grow to CURRENTSIZE + growthRate
    private final int growthRate;

    float[] dataContainer;

    int currentPos = 0;
    int maxSize;

    public MicroFloatArray(int growthRate){
        this.growthRate = growthRate;
        dataContainer = new float[growthRate];
        maxSize = growthRate;
    }


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
        float[] returningContainer = new float[currentPos];
        System.arraycopy(dataContainer, 0, returningContainer, 0, currentPos);
        return returningContainer;
    }

    public void clear(){
        dataContainer = null;
    }
}
