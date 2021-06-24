package game.chunk;

//this container is specifically optimized to the most I can get them to for ChunkMeshGenerator
//cache happy
public class HyperIntArray {
    private static final int growthRate = 1000;

    int[] dataContainer = new int[growthRate];

    int currentPos = 0;
    int maxSize = growthRate;

    private void put(int data){
        if (currentPos == maxSize - 1){
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
        return currentPos + 1;
    }

    private void grow(){
        int[] newContainer = new int[maxSize + growthRate];

        if (maxSize >= 0) System.arraycopy(dataContainer, 0, newContainer, 0, maxSize);

        dataContainer = null; //send to GC
        dataContainer = newContainer;

        maxSize += growthRate;
    }

    public int[] values(){
        int[] returningContainer = new int[currentPos + 1];
        System.arraycopy(dataContainer, 0, returningContainer, 0, currentPos + 1);
        return returningContainer;
    }

    public void clear(){
        dataContainer = null;
    }
}
