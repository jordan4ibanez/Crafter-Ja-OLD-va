package game.chunk;

import org.joml.Vector3i;

public class ChunkMath {

    //private final static int xMax = 16;
    //private final static int yMax = 128;
    //private final static int length = xMax * yMax; // 2048
    public static int posToIndex( int x, int y, int z ) {
        return (z * 2048) + (y * 16) + x;
    }

    //make the inverse of this eventually
    public static int posToIndex2D(int x, int z){
        return (z * 16) + x;
    }

    //keep this for if it's needed in the future
    //for ABMs or something
    public static Vector3i indexToPos( int i ) {
        final int z = i / 2048;
        i -= (z * 2048);
        final int y = i / 16;
        final int x = i % 16;
        return new Vector3i( x, y, z);
    }
}

