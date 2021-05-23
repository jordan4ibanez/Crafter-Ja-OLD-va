package game.chunk;

import org.joml.Vector3i;


/*
License for this code:

Minetest
Copyright (C) 2010-2018 celeron55, Perttu Ahola <celeron55@gmail.com>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation; either version 2.1 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

public class ChunkMath {
    /*
    These class methods are taken from Minetest, specifically the lua side of the Voxel Manipulator
    Just converted from lua to java, with fixed variables
    It is licensed under GPL V3
    https://github.com/minetest/minetest/blob/master/builtin/game/voxelarea.lua
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

