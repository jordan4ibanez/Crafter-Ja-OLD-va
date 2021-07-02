package engine.highPerformanceContainers;

//this container is specifically optimized for ChunkMeshGenerator
//cache happy
public class HyperIntArray {
    //growth rate defines how big the cache trim is
    //defines the initial size, limit of currentPos before it expands
    //and when it does expand, the array will grow to CURRENTSIZE + growthRate
    private final int growthRate;

    int[] dataContainer;

    int currentPos = 0;
    int maxSize;

    public HyperIntArray(int growthRate){
        this.growthRate = growthRate;
        dataContainer = new int[growthRate];
        maxSize = growthRate;
    }

    private void put(int data){
        if (currentPos == maxSize){
            grow();
        }
        dataContainer[currentPos] = data;
        currentPos++;
    }

    //for the indices
    public void pack(int a, int b, int c, int d, int e, int f) {
        put(a);
        put(b);
        put(c);
        put(d);
        put(e);
        put(f);
    }

    public int size(){
        return currentPos;
    }

    private void grow(){
        int[] newContainer = new int[maxSize + growthRate];

        if (maxSize >= 0) System.arraycopy(dataContainer, 0, newContainer, 0, maxSize);

        dataContainer = null; //send to GC
        dataContainer = newContainer;

        maxSize += growthRate;
    }


    public int[] values(){
        int[] returningContainer = new int[currentPos];
        System.arraycopy(dataContainer, 0, returningContainer, 0, currentPos);
        return returningContainer;
    }

    //is used to qualify the object for GC
    public void clear(){
        dataContainer = null;
    }

    //is used to recycle the array - just resets the index to 0
    public void reset(){
        currentPos = 0;
    }
}
