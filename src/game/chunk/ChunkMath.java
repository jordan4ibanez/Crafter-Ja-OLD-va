package game.chunk;

import org.joml.Vector3i;

public class ChunkMath {
    /*
    These class methods are taken from Minetest, specifically the lua side of the Voxel Manipulator
    Just converted from lua to java, with fixed variables
    It is licensed under GPL V3
    https://github.com/minetest/minetest/blob/master/builtin/game/voxelarea.lua
    https://github.com/minetest/minetest/blob/master/LICENSE.txt (for the license, it's too long to paste here while I'm working with it for now)
     */

    private static final int yStride = 16;
    private static final int zStride = 16 * 128;

    public static int posToIndex(int x, int y, int z){
        return z * zStride +
                y * yStride +
                x;
    }

    public static Vector3i indexToPos(int i){
        Vector3i position = new Vector3i();
        position.z = (i / zStride);
        i %= zStride;
        position.y = (i / yStride);
        i %= yStride;
        position.x = i;
        return position;
    }
}
