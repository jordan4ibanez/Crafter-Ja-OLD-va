package game.chunk;

import org.joml.Vector3i;

//https://stackoverflow.com/a/34363187
//ported from C++ to Java

public class ChunkMath {

    private final static int xMax = 16;
    private final static int yMax = 128;

    public static int posToIndex( int x, int y, int z ) {
        return (z * xMax * yMax) + (y * xMax) + x;
    }

    //keep this for if it's needed in the future
    //for ABMs or something
    public static Vector3i indexToPos( int i ) {
        final int z = i / (xMax * yMax);
        i -= (z * xMax * yMax);
        final int y = i / xMax;
        final int x = i % xMax;
        return new Vector3i( x, y, z);
    }
}

