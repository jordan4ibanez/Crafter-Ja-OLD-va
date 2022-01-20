package game.chunk;

import org.joml.Vector3i;

public class ChunkMath {

    //private final static int xMax = 16;
    //private final static int yMax = 128;
    //private final static int length = xMax * yMax; // 2048
    public int posToIndex( int x, int y, int z ) {
        return (z * 2048) + (y * 16) + x;
    }

    //make the inverse of this eventually
    public int posToIndex2D(int x, int z){
        return (z * 16) + x;
    }

    //keep this for if it's needed in the future
    //for ABMs or something

}

