package game.chunk;

import engine.graph.Mesh;
import engine.graph.Texture;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static game.chunk.Chunk.*;
import static game.blocks.BlockDefinition.*;
import static game.chunk.ChunkMath.posToIndex;

public class ChunkMesh {

    private static final ConcurrentHashMap<String, ChunkMeshDataObject> queue = new ConcurrentHashMap<>();


    private static Texture textureAtlas;

    static {
        try {
            textureAtlas = new Texture("textures/textureAtlas.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Texture getTextureAtlas(){
        return textureAtlas;
    }

    private final static float maxLight = 15;

    private static final Random random = new Random();

    public static void popChunkMeshQueue(){

        int count = 0;

        while (!queue.isEmpty() && count < 10) {
            count ++;


            Object[] queueAsArray = queue.keySet().toArray();
            Object thisKey = queueAsArray[random.nextInt(queueAsArray.length)];

            ChunkMeshDataObject newChunkMeshData = queue.get(thisKey);

            String keyName = newChunkMeshData.chunkX + " " + newChunkMeshData.chunkZ + " " + newChunkMeshData.yHeight;

            queue.remove(keyName);

            if (!newChunkMeshData.normalMeshIsNull){
                setChunkNormalMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, new Mesh(newChunkMeshData.positionsArray, newChunkMeshData.lightArray, newChunkMeshData.indicesArray, newChunkMeshData.textureCoordArray, textureAtlas));
            } else {
                setChunkNormalMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, null);
            }

            if (!newChunkMeshData.liquidMeshIsNull){
                setChunkLiquidMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, new Mesh(newChunkMeshData.liquidPositionsArray, newChunkMeshData.liquidLightArray, newChunkMeshData.liquidIndicesArray, newChunkMeshData.liquidTextureCoordArray, textureAtlas));
            } else {
                setChunkLiquidMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, null);
            }
        }

        //if (count > 0) {
        //    System.out.println("amount of chunk meshes created:" + count);
        //}
    }

    public static void generateChunkMesh(int chunkX, int chunkZ, int yHeight) {
        //long startTime = System.nanoTime();
        //let's use all the cpu threads to the limit
        new Thread(() -> {

            //normal block stuff

            final float[] positions = new float[101_648];
            int positionsCount = 0;

            final float[] textureCoord = new float[101_648];
            int textureCoordCount = 0;

            final int[] indices = new int[101_648];
            int indicesTableCount = 0;
            int indicesCount = 0;

            final float[] light = new float[101_648];
            int lightCount = 0;

            //liquid block stuff

            final float[] liquidPositions = new float[50_824];
            int liquidPositionsCount = 0;

            final float[] liquidTextureCoord = new float[50_824];
            int liquidTextureCoordCount = 0;

            final int[] liquidIndices = new int[50_824];
            int liquidIndicesTableCount = 0;
            int liquidIndicesCount = 0;

            final float[] liquidLight = new float[50_824];
            int liquidLightCount = 0;

            int thisBlock;
            byte thisRotation;

            ChunkObject thisChunk = getChunk(chunkX, chunkZ);

            if (thisChunk == null) {
                return;
            }

            int realX;
            int realZ;

            for (int x = 0; x < 16; x++) {
                realX = (chunkX * 16) + x;
                for (int z = 0; z < 16; z++) {
                    realZ = (chunkZ * 16) + z;
                    for (int y = yHeight * 16; y < (yHeight + 1) * 16; y++) {

                        thisBlock = thisChunk.block[posToIndex(x, y, z)];
                        thisRotation = thisChunk.rotation[posToIndex(x, y, z)];

                        if (thisBlock > 0) {

                            //todo --------------------------------------- THE LIQUID DRAWTYPE
                            if (getIfLiquid(thisBlock)) {

                                int neighborBlock;

                                if (z + 1 > 15) {
                                    neighborBlock = getBlock(realX, y, realZ + 1);
                                } else {
                                    neighborBlock = thisChunk.block[posToIndex(x, y, z + 1)];
                                }

                                if (neighborBlock >= 0 && getBlockDrawType(neighborBlock) != 1) {
                                    //front
                                    liquidPositions[liquidPositionsCount + 0] = (1f + x);
                                    liquidPositions[liquidPositionsCount + 1] = (1f + y);
                                    liquidPositions[liquidPositionsCount + 2] = (1f + z);

                                    liquidPositions[liquidPositionsCount + 3] = (0f + x);
                                    liquidPositions[liquidPositionsCount + 4] = (1f + y);
                                    liquidPositions[liquidPositionsCount + 5] = (1f + z);

                                    liquidPositions[liquidPositionsCount + 6] = (0f + x);
                                    liquidPositions[liquidPositionsCount + 7] = (0f + y);
                                    liquidPositions[liquidPositionsCount + 8] = (1f + z);

                                    liquidPositions[liquidPositionsCount + 9] = (1f + x);
                                    liquidPositions[liquidPositionsCount + 10] = (0f + y);
                                    liquidPositions[liquidPositionsCount + 11] = (1f + z);

                                    liquidPositionsCount += 12;


                                    //front

                                    float frontliquidLight;

                                    if (z + 1 > 15) {
                                        frontliquidLight = getLight(realX, y, realZ + 1);
                                    } else {
                                        frontliquidLight = thisChunk.light[posToIndex(x,y,z + 1)];
                                    }

                                    frontliquidLight = convertLight(frontliquidLight / maxLight);

                                    //front
                                    for (int i = 0; i < 12; i++) {
                                        liquidLight[liquidLightCount + i] = (frontliquidLight);
                                    }

                                    liquidLightCount += 12;

                                    //front
                                    liquidIndices[liquidIndicesTableCount + 0] = (0 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 1] = (1 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 2] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 3] = (0 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 4] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 5] = (3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;
                                    liquidIndicesTableCount += 6;

                                    float[] textureFront = getFrontTexturePoints(thisBlock, thisRotation);
                                    //front
                                    liquidTextureCoord[liquidTextureCoordCount + 0] = (textureFront[1]);
                                    liquidTextureCoord[liquidTextureCoordCount + 1] = (textureFront[2]);
                                    liquidTextureCoord[liquidTextureCoordCount + 2] = (textureFront[0]);
                                    liquidTextureCoord[liquidTextureCoordCount + 3] = (textureFront[2]);
                                    liquidTextureCoord[liquidTextureCoordCount + 4] = (textureFront[0]);
                                    liquidTextureCoord[liquidTextureCoordCount + 5] = (textureFront[3]);
                                    liquidTextureCoord[liquidTextureCoordCount + 6] = (textureFront[1]);
                                    liquidTextureCoord[liquidTextureCoordCount + 7] = (textureFront[3]);
                                    liquidTextureCoordCount += 8;
                                }


                                if (z - 1 < 0) {
                                    neighborBlock = getBlock(realX, y, realZ - 1);
                                } else {
                                    neighborBlock = thisChunk.block[posToIndex(x, y, z - 1)];
                                }

                                if (neighborBlock >= 0 && getBlockDrawType(neighborBlock) != 1) {
                                    //back
                                    liquidPositions[liquidPositionsCount + 0] = (0f + x);
                                    liquidPositions[liquidPositionsCount + 1] = (1f + y);
                                    liquidPositions[liquidPositionsCount + 2] = (0f + z);

                                    liquidPositions[liquidPositionsCount + 3] = (1f + x);
                                    liquidPositions[liquidPositionsCount + 4] = (1f + y);
                                    liquidPositions[liquidPositionsCount + 5] = (0f + z);

                                    liquidPositions[liquidPositionsCount + 6] = (1f + x);
                                    liquidPositions[liquidPositionsCount + 7] = (0f + y);
                                    liquidPositions[liquidPositionsCount + 8] = (0f + z);

                                    liquidPositions[liquidPositionsCount + 9] = (0f + x);
                                    liquidPositions[liquidPositionsCount + 10] = (0f + y);
                                    liquidPositions[liquidPositionsCount + 11] = (0f + z);

                                    liquidPositionsCount += 12;

                                    //back
                                    float backliquidLight;

                                    if (z - 1 < 0) {
                                        backliquidLight = getLight(realX, y, realZ - 1);
                                    } else {
                                        backliquidLight = thisChunk.light[posToIndex(x,y,z - 1)];
                                    }

                                    backliquidLight = convertLight(backliquidLight / maxLight);
                                    //back
                                    for (int i = 0; i < 12; i++) {
                                        liquidLight[liquidLightCount + i] = (backliquidLight);
                                    }

                                    liquidLightCount += 12;

                                    //back
                                    liquidIndices[liquidIndicesTableCount + 0] = (0 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 1] = (1 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 2] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 3] = (0 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 4] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 5] = (3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;
                                    liquidIndicesTableCount += 6;

                                    float[] textureBack = getBackTexturePoints(thisBlock, thisRotation);
                                    //back
                                    liquidTextureCoord[liquidTextureCoordCount + 0] = (textureBack[1]);
                                    liquidTextureCoord[liquidTextureCoordCount + 1] = (textureBack[2]);
                                    liquidTextureCoord[liquidTextureCoordCount + 2] = (textureBack[0]);
                                    liquidTextureCoord[liquidTextureCoordCount + 3] = (textureBack[2]);
                                    liquidTextureCoord[liquidTextureCoordCount + 4] = (textureBack[0]);
                                    liquidTextureCoord[liquidTextureCoordCount + 5] = (textureBack[3]);
                                    liquidTextureCoord[liquidTextureCoordCount + 6] = (textureBack[1]);
                                    liquidTextureCoord[liquidTextureCoordCount + 7] = (textureBack[3]);
                                    liquidTextureCoordCount += 8;
                                }

                                if (x + 1 > 15) {
                                    neighborBlock = getBlock(realX + 1, y, realZ);
                                } else {
                                    neighborBlock = thisChunk.block[posToIndex(x + 1, y, z)];
                                }

                                if (neighborBlock >= 0 && getBlockDrawType(neighborBlock) != 1) {
                                    //right
                                    liquidPositions[liquidPositionsCount + 0] = (1f + x);
                                    liquidPositions[liquidPositionsCount + 1] = (1f + y);
                                    liquidPositions[liquidPositionsCount + 2] = (0f + z);

                                    liquidPositions[liquidPositionsCount + 3] = (1f + x);
                                    liquidPositions[liquidPositionsCount + 4] = (1f + y);
                                    liquidPositions[liquidPositionsCount + 5] = (1f + z);

                                    liquidPositions[liquidPositionsCount + 6] = (1f + x);
                                    liquidPositions[liquidPositionsCount + 7] = (0f + y);
                                    liquidPositions[liquidPositionsCount + 8] = (1f + z);

                                    liquidPositions[liquidPositionsCount + 9] = (1f + x);
                                    liquidPositions[liquidPositionsCount + 10] = (0f + y);
                                    liquidPositions[liquidPositionsCount + 11] = (0f + z);

                                    liquidPositionsCount += 12;

                                    //right

                                    float rightliquidLight;

                                    if (x + 1 > 15) {
                                        rightliquidLight = getLight(realX + 1, y, realZ);
                                    } else {
                                        rightliquidLight = thisChunk.light[posToIndex(x+1,y,z)];
                                    }

                                    rightliquidLight = convertLight(rightliquidLight / maxLight);
                                    //right
                                    for (int i = 0; i < 12; i++) {
                                        liquidLight[liquidLightCount + i] = (rightliquidLight);
                                    }

                                    liquidLightCount += 12;

                                    //right
                                    liquidIndices[liquidIndicesTableCount + 0] = (0 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 1] = (1 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 2] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 3] = (0 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 4] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 5] = (3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;
                                    liquidIndicesTableCount += 6;

                                    float[] textureRight = getRightTexturePoints(thisBlock, thisRotation);
                                    //right
                                    liquidTextureCoord[liquidTextureCoordCount + 0] = (textureRight[1]);
                                    liquidTextureCoord[liquidTextureCoordCount + 1] = (textureRight[2]);
                                    liquidTextureCoord[liquidTextureCoordCount + 2] = (textureRight[0]);
                                    liquidTextureCoord[liquidTextureCoordCount + 3] = (textureRight[2]);
                                    liquidTextureCoord[liquidTextureCoordCount + 4] = (textureRight[0]);
                                    liquidTextureCoord[liquidTextureCoordCount + 5] = (textureRight[3]);
                                    liquidTextureCoord[liquidTextureCoordCount + 6] = (textureRight[1]);
                                    liquidTextureCoord[liquidTextureCoordCount + 7] = (textureRight[3]);
                                    liquidTextureCoordCount += 8;
                                }

                                if (x - 1 < 0) {
                                    neighborBlock = getBlock(realX - 1, y, realZ);
                                } else {
                                    neighborBlock = thisChunk.block[posToIndex(x - 1, y, z)];
                                }

                                if (neighborBlock >= 0 && getBlockDrawType(neighborBlock) != 1) {
                                    //left
                                    liquidPositions[liquidPositionsCount + 0] = (0f + x);
                                    liquidPositions[liquidPositionsCount + 1] = (1f + y);
                                    liquidPositions[liquidPositionsCount + 2] = (1f + z);

                                    liquidPositions[liquidPositionsCount + 3] = (0f + x);
                                    liquidPositions[liquidPositionsCount + 4] = (1f + y);
                                    liquidPositions[liquidPositionsCount + 5] = (0f + z);

                                    liquidPositions[liquidPositionsCount + 6] = (0f + x);
                                    liquidPositions[liquidPositionsCount + 7] = (0f + y);
                                    liquidPositions[liquidPositionsCount + 8] = (0f + z);

                                    liquidPositions[liquidPositionsCount + 9] = (0f + x);
                                    liquidPositions[liquidPositionsCount + 10] = (0f + y);
                                    liquidPositions[liquidPositionsCount + 11] = (1f + z);

                                    liquidPositionsCount += 12;

                                    //left

                                    float leftliquidLight;

                                    if (x - 1 < 0) {
                                        leftliquidLight = getLight(realX - 1, y, realZ);
                                    } else {
                                        leftliquidLight = thisChunk.light[posToIndex(x - 1, y, z)];
                                    }

                                    leftliquidLight = convertLight(leftliquidLight / maxLight);
                                    //left
                                    for (int i = 0; i < 12; i++) {
                                        liquidLight[liquidLightCount + i] = (leftliquidLight);
                                    }

                                    liquidLightCount += 12;

                                    //left
                                    liquidIndices[liquidIndicesTableCount + 0] = (0 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 1] = (1 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 2] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 3] = (0 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 4] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 5] = (3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;
                                    liquidIndicesTableCount += 6;

                                    float[] textureLeft = getLeftTexturePoints(thisBlock, thisRotation);
                                    //left
                                    liquidTextureCoord[liquidTextureCoordCount + 0] = (textureLeft[1]);
                                    liquidTextureCoord[liquidTextureCoordCount + 1] = (textureLeft[2]);
                                    liquidTextureCoord[liquidTextureCoordCount + 2] = (textureLeft[0]);
                                    liquidTextureCoord[liquidTextureCoordCount + 3] = (textureLeft[2]);
                                    liquidTextureCoord[liquidTextureCoordCount + 4] = (textureLeft[0]);
                                    liquidTextureCoord[liquidTextureCoordCount + 5] = (textureLeft[3]);
                                    liquidTextureCoord[liquidTextureCoordCount + 6] = (textureLeft[1]);
                                    liquidTextureCoord[liquidTextureCoordCount + 7] = (textureLeft[3]);
                                    liquidTextureCoordCount += 8;
                                }

                                //y doesn't need a check since it has no neighbors
                                if (y + 1 < 128) {
                                    neighborBlock = thisChunk.block[posToIndex(x, y + 1, z)];
                                }

                                if (y == 127 || (neighborBlock >= 0 && getBlockDrawType(neighborBlock) != 1)) {
                                    //top
                                    liquidPositions[liquidPositionsCount + 0] = (0f + x);
                                    liquidPositions[liquidPositionsCount + 1] = (1f + y);
                                    liquidPositions[liquidPositionsCount + 2] = (0f + z);

                                    liquidPositions[liquidPositionsCount + 3] = (0f + x);
                                    liquidPositions[liquidPositionsCount + 4] = (1f + y);
                                    liquidPositions[liquidPositionsCount + 5] = (1f + z);

                                    liquidPositions[liquidPositionsCount + 6] = (1f + x);
                                    liquidPositions[liquidPositionsCount + 7] = (1f + y);
                                    liquidPositions[liquidPositionsCount + 8] = (1f + z);

                                    liquidPositions[liquidPositionsCount + 9] = (1f + x);
                                    liquidPositions[liquidPositionsCount + 10] = (1f + y);
                                    liquidPositions[liquidPositionsCount + 11] = (0f + z);

                                    liquidPositionsCount += 12;

                                    //top
                                    float topliquidLight;

                                    //y doesn't need a check since it has no neighbors
                                    if (y + 1 < 128) {
                                        topliquidLight = thisChunk.light[posToIndex(x, y + 1, z)];
                                    } else {
                                        topliquidLight = maxLight;
                                    }

                                    topliquidLight = convertLight(topliquidLight / maxLight);
                                    //top
                                    for (int i = 0; i < 12; i++) {
                                        liquidLight[liquidLightCount + i] = (topliquidLight);
                                    }

                                    liquidLightCount += 12;

                                    //top
                                    liquidIndices[liquidIndicesTableCount + 0] = (0 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 1] = (1 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 2] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 3] = (0 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 4] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 5] = (3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;
                                    liquidIndicesTableCount += 6;

                                    float[] textureTop = getTopTexturePoints(thisBlock);
                                    //top
                                    liquidTextureCoord[liquidTextureCoordCount + 0] = (textureTop[1]);
                                    liquidTextureCoord[liquidTextureCoordCount + 1] = (textureTop[2]);
                                    liquidTextureCoord[liquidTextureCoordCount + 2] = (textureTop[0]);
                                    liquidTextureCoord[liquidTextureCoordCount + 3] = (textureTop[2]);
                                    liquidTextureCoord[liquidTextureCoordCount + 4] = (textureTop[0]);
                                    liquidTextureCoord[liquidTextureCoordCount + 5] = (textureTop[3]);
                                    liquidTextureCoord[liquidTextureCoordCount + 6] = (textureTop[1]);
                                    liquidTextureCoord[liquidTextureCoordCount + 7] = (textureTop[3]);
                                    liquidTextureCoordCount += 8;
                                }

                                //doesn't need a neighbor chunk, chunks are 2D
                                if (y - 1 > 0) {
                                    neighborBlock = thisChunk.block[posToIndex(x, y - 1, z)];
                                }

                                //don't render bottom of world
                                if (y != 0 && neighborBlock >= 0 && getBlockDrawType(neighborBlock) != 1) {
                                    //bottom
                                    liquidPositions[liquidPositionsCount + 0] = (0f + x);
                                    liquidPositions[liquidPositionsCount + 1] = (0f + y);
                                    liquidPositions[liquidPositionsCount + 2] = (1f + z);

                                    liquidPositions[liquidPositionsCount + 3] = (0f + x);
                                    liquidPositions[liquidPositionsCount + 4] = (0f + y);
                                    liquidPositions[liquidPositionsCount + 5] = (0f + z);

                                    liquidPositions[liquidPositionsCount + 6] = (1f + x);
                                    liquidPositions[liquidPositionsCount + 7] = (0f + y);
                                    liquidPositions[liquidPositionsCount + 8] = (0f + z);

                                    liquidPositions[liquidPositionsCount + 9] = (1f + x);
                                    liquidPositions[liquidPositionsCount + 10] = (0f + y);
                                    liquidPositions[liquidPositionsCount + 11] = (1f + z);

                                    liquidPositionsCount += 12;

                                    //bottom

                                    float bottomliquidLight;

                                    //doesn't need a neighbor chunk, chunks are 2D
                                    if (y - 1 > 0) {
                                        bottomliquidLight = thisChunk.light[posToIndex(x, y - 1, z)];
                                    } else {
                                        bottomliquidLight = 0;
                                    }

                                    bottomliquidLight = convertLight(bottomliquidLight / maxLight);
                                    //bottom
                                    for (int i = 0; i < 12; i++) {
                                        liquidLight[liquidLightCount + i] = (bottomliquidLight);
                                    }

                                    liquidLightCount += 12;

                                    //bottom
                                    liquidIndices[liquidIndicesTableCount + 0] = (0 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 1] = (1 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 2] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 3] = (0 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 4] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 5] = (3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;
                                    liquidIndicesTableCount += 6;

                                    float[] textureBottom = getBottomTexturePoints(thisBlock);
                                    //bottom
                                    liquidTextureCoord[liquidTextureCoordCount + 0] = (textureBottom[1]);
                                    liquidTextureCoord[liquidTextureCoordCount + 1] = (textureBottom[2]);
                                    liquidTextureCoord[liquidTextureCoordCount + 2] = (textureBottom[0]);
                                    liquidTextureCoord[liquidTextureCoordCount + 3] = (textureBottom[2]);
                                    liquidTextureCoord[liquidTextureCoordCount + 4] = (textureBottom[0]);
                                    liquidTextureCoord[liquidTextureCoordCount + 5] = (textureBottom[3]);
                                    liquidTextureCoord[liquidTextureCoordCount + 6] = (textureBottom[1]);
                                    liquidTextureCoord[liquidTextureCoordCount + 7] = (textureBottom[3]);
                                    liquidTextureCoordCount += 8;
                                }
                            }

                            //todo --------------------------------------- THE NORMAL DRAWTYPE (standard blocks)
                            else if (getBlockDrawType(thisBlock) == 1) {

                                int neighborBlock;

                                if (z + 1 > 15) {
                                    neighborBlock = getBlock(realX, y, realZ + 1);
                                } else {
                                    neighborBlock = thisChunk.block[posToIndex(x, y, z + 1)];
                                }

                                if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
                                    //front
                                    positions[positionsCount + 0] = (1f + x);
                                    positions[positionsCount + 1] = (1f + y);
                                    positions[positionsCount + 2] = (1f + z);

                                    positions[positionsCount + 3] = (0f + x);
                                    positions[positionsCount + 4] = (1f + y);
                                    positions[positionsCount + 5] = (1f + z);

                                    positions[positionsCount + 6] = (0f + x);
                                    positions[positionsCount + 7] = (0f + y);
                                    positions[positionsCount + 8] = (1f + z);

                                    positions[positionsCount + 9] = (1f + x);
                                    positions[positionsCount + 10] = (0f + y);
                                    positions[positionsCount + 11] = (1f + z);

                                    positionsCount += 12;

                                    //front
                                    float frontLight;

                                    if (z + 1 > 15) {
                                        frontLight = getLight(realX, y, realZ + 1);
                                    } else {
                                        frontLight = thisChunk.light[posToIndex(x, y, z + 1)];
                                    }

                                    frontLight = convertLight(frontLight / maxLight);

                                    //front
                                    for (int i = 0; i < 12; i++) {
                                        light[i + lightCount] = (frontLight);
                                    }

                                    lightCount += 12;


                                    //front
                                    indices[indicesTableCount + 0] = (0 + indicesCount);
                                    indices[indicesTableCount + 1] = (1 + indicesCount);
                                    indices[indicesTableCount + 2] = (2 + indicesCount);
                                    indices[indicesTableCount + 3] = (0 + indicesCount);
                                    indices[indicesTableCount + 4] = (2 + indicesCount);
                                    indices[indicesTableCount + 5] = (3 + indicesCount);

                                    indicesCount += 4;
                                    indicesTableCount += 6;

                                    float[] textureFront = getFrontTexturePoints(thisBlock, thisRotation);
                                    //front
                                    textureCoord[textureCoordCount + 0] = (textureFront[1]);
                                    textureCoord[textureCoordCount + 1] = (textureFront[2]);
                                    textureCoord[textureCoordCount + 2] = (textureFront[0]);
                                    textureCoord[textureCoordCount + 3] = (textureFront[2]);
                                    textureCoord[textureCoordCount + 4] = (textureFront[0]);
                                    textureCoord[textureCoordCount + 5] = (textureFront[3]);
                                    textureCoord[textureCoordCount + 6] = (textureFront[1]);
                                    textureCoord[textureCoordCount + 7] = (textureFront[3]);
                                    textureCoordCount += 8;
                                }

                                if (z - 1 < 0) {
                                    neighborBlock = getBlock(realX, y, realZ - 1);
                                } else {
                                    neighborBlock = thisChunk.block[posToIndex(x, y, z - 1)];
                                }

                                if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
                                    //back
                                    positions[positionsCount + 0] = (0f + x);
                                    positions[positionsCount + 1] = (1f + y);
                                    positions[positionsCount + 2] = (0f + z);

                                    positions[positionsCount + 3] = (1f + x);
                                    positions[positionsCount + 4] = (1f + y);
                                    positions[positionsCount + 5] = (0f + z);

                                    positions[positionsCount + 6] = (1f + x);
                                    positions[positionsCount + 7] = (0f + y);
                                    positions[positionsCount + 8] = (0f + z);

                                    positions[positionsCount + 9] = (0f + x);
                                    positions[positionsCount + 10] = (0f + y);
                                    positions[positionsCount + 11] = (0f + z);

                                    positionsCount += 12;

                                    //back
                                    float backLight;

                                    if (z - 1 < 0) {
                                        backLight = getLight(realX, y, realZ - 1);
                                    } else {
                                        backLight = thisChunk.light[posToIndex(x, y, z - 1)];
                                    }

                                    backLight = convertLight(backLight / maxLight);
                                    //back
                                    for (int i = 0; i < 12; i++) {
                                        light[i + lightCount] = (backLight);
                                    }

                                    lightCount += 12;

                                    //back
                                    indices[indicesTableCount + 0] = (0 + indicesCount);
                                    indices[indicesTableCount + 1] = (1 + indicesCount);
                                    indices[indicesTableCount + 2] = (2 + indicesCount);
                                    indices[indicesTableCount + 3] = (0 + indicesCount);
                                    indices[indicesTableCount + 4] = (2 + indicesCount);
                                    indices[indicesTableCount + 5] = (3 + indicesCount);
                                    indicesCount += 4;
                                    indicesTableCount += 6;

                                    float[] textureBack = getBackTexturePoints(thisBlock, thisRotation);
                                    //back
                                    textureCoord[textureCoordCount + 0] = (textureBack[1]);
                                    textureCoord[textureCoordCount + 1] = (textureBack[2]);
                                    textureCoord[textureCoordCount + 2] = (textureBack[0]);
                                    textureCoord[textureCoordCount + 3] = (textureBack[2]);
                                    textureCoord[textureCoordCount + 4] = (textureBack[0]);
                                    textureCoord[textureCoordCount + 5] = (textureBack[3]);
                                    textureCoord[textureCoordCount + 6] = (textureBack[1]);
                                    textureCoord[textureCoordCount + 7] = (textureBack[3]);
                                    textureCoordCount += 8;
                                }

                                if (x + 1 > 15) {
                                    neighborBlock = getBlock(realX + 1, y, realZ);
                                } else {
                                    neighborBlock = thisChunk.block[posToIndex(x + 1, y, z)];
                                }

                                if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
                                    //right
                                    positions[positionsCount + 0] = (1f + x);
                                    positions[positionsCount + 1] = (1f + y);
                                    positions[positionsCount + 2] = (0f + z);

                                    positions[positionsCount + 3] = (1f + x);
                                    positions[positionsCount + 4] = (1f + y);
                                    positions[positionsCount + 5] = (1f + z);

                                    positions[positionsCount + 6] = (1f + x);
                                    positions[positionsCount + 7] = (0f + y);
                                    positions[positionsCount + 8] = (1f + z);

                                    positions[positionsCount + 9] = (1f + x);
                                    positions[positionsCount + 10] = (0f + y);
                                    positions[positionsCount + 11] = (0f + z);

                                    positionsCount += 12;

                                    //right
                                    float rightLight;

                                    if (x + 1 > 15) {
                                        rightLight = getLight(realX + 1, y, realZ);
                                    } else {
                                        rightLight = thisChunk.light[posToIndex(x + 1, y, z)];
                                    }

                                    rightLight = convertLight(rightLight / maxLight);
                                    //right
                                    for (int i = 0; i < 12; i++) {
                                        light[i + lightCount] = (rightLight);
                                    }

                                    lightCount += 12;

                                    //right
                                    indices[indicesTableCount + 0] = (0 + indicesCount);
                                    indices[indicesTableCount + 1] = (1 + indicesCount);
                                    indices[indicesTableCount + 2] = (2 + indicesCount);
                                    indices[indicesTableCount + 3] = (0 + indicesCount);
                                    indices[indicesTableCount + 4] = (2 + indicesCount);
                                    indices[indicesTableCount + 5] = (3 + indicesCount);
                                    indicesCount += 4;
                                    indicesTableCount += 6;

                                    float[] textureRight = getRightTexturePoints(thisBlock, thisRotation);
                                    //right
                                    textureCoord[textureCoordCount + 0] = (textureRight[1]);
                                    textureCoord[textureCoordCount + 1] = (textureRight[2]);
                                    textureCoord[textureCoordCount + 2] = (textureRight[0]);
                                    textureCoord[textureCoordCount + 3] = (textureRight[2]);
                                    textureCoord[textureCoordCount + 4] = (textureRight[0]);
                                    textureCoord[textureCoordCount + 5] = (textureRight[3]);
                                    textureCoord[textureCoordCount + 6] = (textureRight[1]);
                                    textureCoord[textureCoordCount + 7] = (textureRight[3]);
                                    textureCoordCount += 8;
                                }

                                if (x - 1 < 0) {
                                    neighborBlock = getBlock(realX - 1, y, realZ);
                                } else {
                                    neighborBlock = thisChunk.block[posToIndex(x - 1, y, z)];
                                }

                                if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
                                    //left
                                    positions[positionsCount + 0] = (0f + x);
                                    positions[positionsCount + 1] = (1f + y);
                                    positions[positionsCount + 2] = (1f + z);

                                    positions[positionsCount + 3] = (0f + x);
                                    positions[positionsCount + 4] = (1f + y);
                                    positions[positionsCount + 5] = (0f + z);

                                    positions[positionsCount + 6] = (0f + x);
                                    positions[positionsCount + 7] = (0f + y);
                                    positions[positionsCount + 8] = (0f + z);

                                    positions[positionsCount + 9] = (0f + x);
                                    positions[positionsCount + 10] = (0f + y);
                                    positions[positionsCount + 11] = (1f + z);

                                    positionsCount += 12;

                                    //left
                                    float leftLight;

                                    if (x - 1 < 0) {
                                        leftLight = getLight(realX - 1, y, realZ);
                                    } else {
                                        leftLight = thisChunk.light[posToIndex(x - 1, y, z)];
                                    }

                                    leftLight = convertLight(leftLight / maxLight);
                                    //left
                                    for (int i = 0; i < 12; i++) {
                                        light[i + lightCount] = (leftLight);
                                    }

                                    lightCount += 12;

                                    //left
                                    indices[indicesTableCount + 0] = (0 + indicesCount);
                                    indices[indicesTableCount + 1] = (1 + indicesCount);
                                    indices[indicesTableCount + 2] = (2 + indicesCount);
                                    indices[indicesTableCount + 3] = (0 + indicesCount);
                                    indices[indicesTableCount + 4] = (2 + indicesCount);
                                    indices[indicesTableCount + 5] = (3 + indicesCount);
                                    indicesCount += 4;
                                    indicesTableCount += 6;

                                    float[] textureLeft = getLeftTexturePoints(thisBlock, thisRotation);
                                    //left
                                    textureCoord[textureCoordCount + 0] = (textureLeft[1]);
                                    textureCoord[textureCoordCount + 1] = (textureLeft[2]);
                                    textureCoord[textureCoordCount + 2] = (textureLeft[0]);
                                    textureCoord[textureCoordCount + 3] = (textureLeft[2]);
                                    textureCoord[textureCoordCount + 4] = (textureLeft[0]);
                                    textureCoord[textureCoordCount + 5] = (textureLeft[3]);
                                    textureCoord[textureCoordCount + 6] = (textureLeft[1]);
                                    textureCoord[textureCoordCount + 7] = (textureLeft[3]);
                                    textureCoordCount += 8;
                                }

                                if (y + 1 < 128) {
                                    neighborBlock = thisChunk.block[posToIndex(x, y + 1, z)];
                                }

                                if (y == 127 || neighborBlock > -1 && ((neighborBlock >= 0 && getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock)))) {
                                    //top
                                    positions[positionsCount + 0] = (0f + x);
                                    positions[positionsCount + 1] = (1f + y);
                                    positions[positionsCount + 2] = (0f + z);

                                    positions[positionsCount + 3] = (0f + x);
                                    positions[positionsCount + 4] = (1f + y);
                                    positions[positionsCount + 5] = (1f + z);

                                    positions[positionsCount + 6] = (1f + x);
                                    positions[positionsCount + 7] = (1f + y);
                                    positions[positionsCount + 8] = (1f + z);

                                    positions[positionsCount + 9] = (1f + x);
                                    positions[positionsCount + 10] = (1f + y);
                                    positions[positionsCount + 11] = (0f + z);

                                    positionsCount += 12;

                                    //top
                                    float topLight;

                                    if (y + 1 < 128) {
                                        topLight = thisChunk.light[posToIndex(x, y + 1, z)];
                                    } else {
                                        topLight = maxLight;
                                    }

                                    topLight = convertLight(topLight / maxLight);

                                    //top
                                    for (int i = 0; i < 12; i++) {
                                        light[i + lightCount] = (topLight);
                                    }

                                    lightCount += 12;

                                    //top
                                    indices[indicesTableCount + 0] = (0 + indicesCount);
                                    indices[indicesTableCount + 1] = (1 + indicesCount);
                                    indices[indicesTableCount + 2] = (2 + indicesCount);
                                    indices[indicesTableCount + 3] = (0 + indicesCount);
                                    indices[indicesTableCount + 4] = (2 + indicesCount);
                                    indices[indicesTableCount + 5] = (3 + indicesCount);
                                    indicesCount += 4;
                                    indicesTableCount += 6;

                                    float[] textureTop = getTopTexturePoints(thisBlock);
                                    //top
                                    textureCoord[textureCoordCount + 0] = (textureTop[1]);
                                    textureCoord[textureCoordCount + 1] = (textureTop[2]);
                                    textureCoord[textureCoordCount + 2] = (textureTop[0]);
                                    textureCoord[textureCoordCount + 3] = (textureTop[2]);
                                    textureCoord[textureCoordCount + 4] = (textureTop[0]);
                                    textureCoord[textureCoordCount + 5] = (textureTop[3]);
                                    textureCoord[textureCoordCount + 6] = (textureTop[1]);
                                    textureCoord[textureCoordCount + 7] = (textureTop[3]);
                                    textureCoordCount += 8;
                                }

                                if (y - 1 > 0) {
                                    neighborBlock = thisChunk.block[posToIndex(x, y - 1, z)];
                                }

                                if (y != 0 && neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
                                    //bottom
                                    positions[positionsCount + 0] = (0f + x);
                                    positions[positionsCount + 1] = (0f + y);
                                    positions[positionsCount + 2] = (1f + z);

                                    positions[positionsCount + 3] = (0f + x);
                                    positions[positionsCount + 4] = (0f + y);
                                    positions[positionsCount + 5] = (0f + z);

                                    positions[positionsCount + 6] = (1f + x);
                                    positions[positionsCount + 7] = (0f + y);
                                    positions[positionsCount + 8] = (0f + z);

                                    positions[positionsCount + 9] = (1f + x);
                                    positions[positionsCount + 10] = (0f + y);
                                    positions[positionsCount + 11] = (1f + z);

                                    positionsCount += 12;

                                    //bottom
                                    float bottomLight;

                                    if (y - 1 > 0) {
                                        bottomLight = thisChunk.light[posToIndex(x, y - 1, z)];
                                    } else {
                                        bottomLight = 0;
                                    }

                                    bottomLight = convertLight(bottomLight / maxLight);
                                    //bottom
                                    for (int i = 0; i < 12; i++) {
                                        light[i + lightCount] = (bottomLight);
                                    }

                                    lightCount += 12;

                                    //bottom
                                    indices[indicesTableCount + 0] = (0 + indicesCount);
                                    indices[indicesTableCount + 1] = (1 + indicesCount);
                                    indices[indicesTableCount + 2] = (2 + indicesCount);
                                    indices[indicesTableCount + 3] = (0 + indicesCount);
                                    indices[indicesTableCount + 4] = (2 + indicesCount);
                                    indices[indicesTableCount + 5] = (3 + indicesCount);
                                    indicesCount += 4;
                                    indicesTableCount += 6;

                                    float[] textureBottom = getBottomTexturePoints(thisBlock);
                                    //bottom
                                    textureCoord[textureCoordCount + 0] = (textureBottom[1]);
                                    textureCoord[textureCoordCount + 1] = (textureBottom[2]);
                                    textureCoord[textureCoordCount + 2] = (textureBottom[0]);
                                    textureCoord[textureCoordCount + 3] = (textureBottom[2]);
                                    textureCoord[textureCoordCount + 4] = (textureBottom[0]);
                                    textureCoord[textureCoordCount + 5] = (textureBottom[3]);
                                    textureCoord[textureCoordCount + 6] = (textureBottom[1]);
                                    textureCoord[textureCoordCount + 7] = (textureBottom[3]);
                                    textureCoordCount += 8;
                                }
                                //todo: ---------------------------------------------------------- the block box draw type
                            } else {
                                for (double[] thisBlockBox : getBlockShape(thisBlock, thisRotation)) {
                                    // 0, 1, 2, 3, 4, 5
                                    //-x,-y,-z, x, y, z
                                    // 0, 0, 0, 1, 1, 1

                                    //front
                                    positions[positionsCount + 0] = ((float) thisBlockBox[3] + x);
                                    positions[positionsCount + 1] = ((float) thisBlockBox[4] + y);
                                    positions[positionsCount + 2] = ((float) thisBlockBox[5] + z);

                                    positions[positionsCount + 3] = ((float) thisBlockBox[0] + x);
                                    positions[positionsCount + 4] = ((float) thisBlockBox[4] + y);
                                    positions[positionsCount + 5] = ((float) thisBlockBox[5] + z);

                                    positions[positionsCount + 6] = ((float) thisBlockBox[0] + x);
                                    positions[positionsCount + 7] = ((float) thisBlockBox[1] + y);
                                    positions[positionsCount + 8] = ((float) thisBlockBox[5] + z);

                                    positions[positionsCount + 9] = ((float) thisBlockBox[3] + x);
                                    positions[positionsCount + 10] = ((float) thisBlockBox[1] + y);
                                    positions[positionsCount + 11] = ((float) thisBlockBox[5] + z);

                                    positionsCount += 12;

                                    //front
                                    float frontLight = getLight(realX, y, realZ + 1);

                                    frontLight = convertLight(frontLight / maxLight);

                                    //front
                                    for (int i = 0; i < 12; i++) {
                                        light[lightCount + i] = (frontLight);
                                    }

                                    lightCount += 12;

                                    //front
                                    indices[indicesTableCount + 0] = (0 + indicesCount);
                                    indices[indicesTableCount + 1] = (1 + indicesCount);
                                    indices[indicesTableCount + 2] = (2 + indicesCount);
                                    indices[indicesTableCount + 3] = (0 + indicesCount);
                                    indices[indicesTableCount + 4] = (2 + indicesCount);
                                    indices[indicesTableCount + 5] = (3 + indicesCount);
                                    indicesCount += 4;
                                    indicesTableCount += 6;

                                    // 0, 1,  2, 3
                                    //-x,+x, -y,+y

                                    float[] textureFront = getFrontTexturePoints(thisBlock, thisRotation);

                                    //front
                                    textureCoord[textureCoordCount + 0] = (textureFront[1] - ((1 - (float) thisBlockBox[3]) / 32f)); //x positive
                                    textureCoord[textureCoordCount + 1] = (textureFront[2] + ((1 - (float) thisBlockBox[4]) / 32f)); //y positive
                                    textureCoord[textureCoordCount + 2] = (textureFront[0] - ((0 - (float) thisBlockBox[0]) / 32f)); //x negative
                                    textureCoord[textureCoordCount + 3] = (textureFront[2] + ((1 - (float) thisBlockBox[4]) / 32f)); //y positive

                                    textureCoord[textureCoordCount + 4] = (textureFront[0] - ((0 - (float) thisBlockBox[0]) / 32f)); //x negative
                                    textureCoord[textureCoordCount + 5] = (textureFront[3] - (((float) thisBlockBox[1]) / 32f));   //y negative
                                    textureCoord[textureCoordCount + 6] = (textureFront[1] - ((1 - (float) thisBlockBox[3]) / 32f)); //x positive
                                    textureCoord[textureCoordCount + 7] = (textureFront[3] - (((float) thisBlockBox[1]) / 32f));   //y negative

                                    textureCoordCount += 8;


                                    //back
                                    positions[positionsCount + 0] = ((float) thisBlockBox[0] + x);
                                    positions[positionsCount + 1] = ((float) thisBlockBox[4] + y);
                                    positions[positionsCount + 2] = ((float) thisBlockBox[2] + z);

                                    positions[positionsCount + 3] = ((float) thisBlockBox[3] + x);
                                    positions[positionsCount + 4] = ((float) thisBlockBox[4] + y);
                                    positions[positionsCount + 5] = ((float) thisBlockBox[2] + z);

                                    positions[positionsCount + 6] = ((float) thisBlockBox[3] + x);
                                    positions[positionsCount + 7] = ((float) thisBlockBox[1] + y);
                                    positions[positionsCount + 8] = ((float) thisBlockBox[2] + z);

                                    positions[positionsCount + 9] = ((float) thisBlockBox[0] + x);
                                    positions[positionsCount + 10] = ((float) thisBlockBox[1] + y);
                                    positions[positionsCount + 11] = ((float) thisBlockBox[2] + z);

                                    positionsCount += 12;


                                    //back
                                    float backLight = getLight(realX, y, realZ - 1);
                                    backLight = convertLight(backLight / maxLight);
                                    //back
                                    for (int i = 0; i < 12; i++) {
                                        light[lightCount + i] = (backLight);
                                    }

                                    lightCount += 12;

                                    //back
                                    indices[indicesTableCount + 0] = (0 + indicesCount);
                                    indices[indicesTableCount + 1] = (1 + indicesCount);
                                    indices[indicesTableCount + 2] = (2 + indicesCount);
                                    indices[indicesTableCount + 3] = (0 + indicesCount);
                                    indices[indicesTableCount + 4] = (2 + indicesCount);
                                    indices[indicesTableCount + 5] = (3 + indicesCount);
                                    indicesCount += 4;
                                    indicesTableCount += 6;

                                    float[] textureBack = getBackTexturePoints(thisBlock, thisRotation);

                                    // 0, 1, 2, 3, 4, 5
                                    //-x,-y,-z, x, y, z
                                    // 0, 0, 0, 1, 1, 1

                                    // 0, 1,  2, 3
                                    //-x,+x, -y,+y


                                    //back
                                    textureCoord[textureCoordCount + 0] = (textureBack[1] - ((1 - (float) thisBlockBox[0]) / 32f));
                                    textureCoord[textureCoordCount + 1] = (textureBack[2] + ((1 - (float) thisBlockBox[4]) / 32f));
                                    textureCoord[textureCoordCount + 2] = (textureBack[0] - ((0 - (float) thisBlockBox[3]) / 32f));
                                    textureCoord[textureCoordCount + 3] = (textureBack[2] + ((1 - (float) thisBlockBox[4]) / 32f));

                                    textureCoord[textureCoordCount + 4] = (textureBack[0] - ((0 - (float) thisBlockBox[3]) / 32f));
                                    textureCoord[textureCoordCount + 5] = (textureBack[3] - (((float) thisBlockBox[1]) / 32f));
                                    textureCoord[textureCoordCount + 6] = (textureBack[1] - ((1 - (float) thisBlockBox[0]) / 32f));
                                    textureCoord[textureCoordCount + 7] = (textureBack[3] - (((float) thisBlockBox[1]) / 32f));
                                    textureCoordCount += 8;


                                    //right
                                    positions[positionsCount + 0] = ((float) thisBlockBox[3] + x);
                                    positions[positionsCount + 1] = ((float) thisBlockBox[4] + y);
                                    positions[positionsCount + 2] = ((float) thisBlockBox[2] + z);

                                    positions[positionsCount + 3] = ((float) thisBlockBox[3] + x);
                                    positions[positionsCount + 4] = ((float) thisBlockBox[4] + y);
                                    positions[positionsCount + 5] = ((float) thisBlockBox[5] + z);

                                    positions[positionsCount + 6] = ((float) thisBlockBox[3] + x);
                                    positions[positionsCount + 7] = ((float) thisBlockBox[1] + y);
                                    positions[positionsCount + 8] = ((float) thisBlockBox[5] + z);

                                    positions[positionsCount + 9] = ((float) thisBlockBox[3] + x);
                                    positions[positionsCount + 10] = ((float) thisBlockBox[1] + y);
                                    positions[positionsCount + 11] = ((float) thisBlockBox[2] + z);

                                    positionsCount += 12;

                                    //right
                                    float rightLight = getLight(realX + 1, y, realZ);
                                    rightLight = convertLight(rightLight / maxLight);
                                    //right
                                    for (int i = 0; i < 12; i++) {
                                        light[lightCount + i] = (rightLight);
                                    }

                                    lightCount += 12;

                                    //right
                                    indices[indicesTableCount + 0] = (0 + indicesCount);
                                    indices[indicesTableCount + 1] = (1 + indicesCount);
                                    indices[indicesTableCount + 2] = (2 + indicesCount);
                                    indices[indicesTableCount + 3] = (0 + indicesCount);
                                    indices[indicesTableCount + 4] = (2 + indicesCount);
                                    indices[indicesTableCount + 5] = (3 + indicesCount);
                                    indicesCount += 4;
                                    indicesTableCount += 6;


                                    // 0, 1, 2, 3, 4, 5
                                    //-x,-y,-z, x, y, z
                                    // 0, 0, 0, 1, 1, 1

                                    // 0, 1,  2, 3
                                    //-x,+x, -y,+y


                                    float[] textureRight = getRightTexturePoints(thisBlock, thisRotation);
                                    //right
                                    textureCoord[textureCoordCount + 0] = (textureRight[1] - ((1 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord[textureCoordCount + 1] = (textureRight[2] + ((1 - (float) thisBlockBox[4]) / 32f));
                                    textureCoord[textureCoordCount + 2] = (textureRight[0] - ((0 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord[textureCoordCount + 3] = (textureRight[2] + ((1 - (float) thisBlockBox[4]) / 32f));

                                    textureCoord[textureCoordCount + 4] = (textureRight[0] - ((0 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord[textureCoordCount + 5] = (textureRight[3] - (((float) thisBlockBox[1]) / 32f));
                                    textureCoord[textureCoordCount + 6] = (textureRight[1] - ((1 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord[textureCoordCount + 7] = (textureRight[3] - (((float) thisBlockBox[1]) / 32f));
                                    textureCoordCount += 8;


                                    //left
                                    positions[positionsCount + 0] = ((float) thisBlockBox[0] + x);
                                    positions[positionsCount + 1] = ((float) thisBlockBox[4] + y);
                                    positions[positionsCount + 2] = ((float) thisBlockBox[5] + z);

                                    positions[positionsCount + 3] = ((float) thisBlockBox[0] + x);
                                    positions[positionsCount + 4] = ((float) thisBlockBox[4] + y);
                                    positions[positionsCount + 5] = ((float) thisBlockBox[2] + z);

                                    positions[positionsCount + 6] = ((float) thisBlockBox[0] + x);
                                    positions[positionsCount + 7] = ((float) thisBlockBox[1] + y);
                                    positions[positionsCount + 8] = ((float) thisBlockBox[2] + z);

                                    positions[positionsCount + 9] = ((float) thisBlockBox[0] + x);
                                    positions[positionsCount + 10] = ((float) thisBlockBox[1] + y);
                                    positions[positionsCount + 11] = ((float) thisBlockBox[5] + z);

                                    positionsCount += 12;

                                    //left
                                    float leftLight = getLight(realX - 1, y, realZ);
                                    leftLight = convertLight(leftLight / maxLight);
                                    //left
                                    for (int i = 0; i < 12; i++) {
                                        light[lightCount + i] = (leftLight);
                                    }

                                    lightCount += 12;

                                    //left
                                    indices[indicesTableCount + 0] = (0 + indicesCount);
                                    indices[indicesTableCount + 1] = (1 + indicesCount);
                                    indices[indicesTableCount + 2] = (2 + indicesCount);
                                    indices[indicesTableCount + 3] = (0 + indicesCount);
                                    indices[indicesTableCount + 4] = (2 + indicesCount);
                                    indices[indicesTableCount + 5] = (3 + indicesCount);
                                    indicesCount += 4;
                                    indicesTableCount += 6;

                                    float[] textureLeft = getLeftTexturePoints(thisBlock, thisRotation);
                                    //left
                                    textureCoord[textureCoordCount + 0] = (textureLeft[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord[textureCoordCount + 1] = (textureLeft[2] + ((1 - (float) thisBlockBox[4]) / 32f));
                                    textureCoord[textureCoordCount + 2] = (textureLeft[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord[textureCoordCount + 3] = (textureLeft[2] + ((1 - (float) thisBlockBox[4]) / 32f));

                                    textureCoord[textureCoordCount + 4] = (textureLeft[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord[textureCoordCount + 5] = (textureLeft[3] - (((float) thisBlockBox[1]) / 32f));
                                    textureCoord[textureCoordCount + 6] = (textureLeft[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord[textureCoordCount + 7] = (textureLeft[3] - (((float) thisBlockBox[1]) / 32f));
                                    textureCoordCount += 8;


                                    //top
                                    positions[positionsCount + 0] = ((float) thisBlockBox[0] + x);
                                    positions[positionsCount + 1] = ((float) thisBlockBox[4] + y);
                                    positions[positionsCount + 2] = ((float) thisBlockBox[2] + z);

                                    positions[positionsCount + 3] = ((float) thisBlockBox[0] + x);
                                    positions[positionsCount + 4] = ((float) thisBlockBox[4] + y);
                                    positions[positionsCount + 5] = ((float) thisBlockBox[5] + z);

                                    positions[positionsCount + 6] = ((float) thisBlockBox[3] + x);
                                    positions[positionsCount + 7] = ((float) thisBlockBox[4] + y);
                                    positions[positionsCount + 8] = ((float) thisBlockBox[5] + z);

                                    positions[positionsCount + 9] = ((float) thisBlockBox[3] + x);
                                    positions[positionsCount + 10] = ((float) thisBlockBox[4] + y);
                                    positions[positionsCount + 11] = ((float) thisBlockBox[2] + z);

                                    positionsCount += 12;

                                    //top
                                    float topLight = getLight(realX, y + 1, realZ);
                                    topLight = convertLight(topLight / maxLight);
                                    //top
                                    for (int i = 0; i < 12; i++) {
                                        light[lightCount + i] = (topLight);
                                    }

                                    lightCount += 12;

                                    //top
                                    indices[indicesTableCount + 0] = (0 + indicesCount);
                                    indices[indicesTableCount + 1] = (1 + indicesCount);
                                    indices[indicesTableCount + 2] = (2 + indicesCount);
                                    indices[indicesTableCount + 3] = (0 + indicesCount);
                                    indices[indicesTableCount + 4] = (2 + indicesCount);
                                    indices[indicesTableCount + 5] = (3 + indicesCount);
                                    indicesCount += 4;
                                    indicesTableCount += 6;

                                    // 0, 1, 2, 3, 4, 5
                                    //-x,-y,-z, x, y, z
                                    // 0, 0, 0, 1, 1, 1

                                    // 0, 1,  2, 3
                                    //-x,+x, -y,+y

                                    float[] textureTop = getTopTexturePoints(thisBlock);
                                    //top
                                    textureCoord[textureCoordCount + 0] = (textureTop[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord[textureCoordCount + 1] = (textureTop[2] + ((1 - (float) thisBlockBox[0]) / 32f));
                                    textureCoord[textureCoordCount + 2] = (textureTop[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord[textureCoordCount + 3] = (textureTop[2] + ((1 - (float) thisBlockBox[0]) / 32f));

                                    textureCoord[textureCoordCount + 4] = (textureTop[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord[textureCoordCount + 5] = (textureTop[3] - (((float) thisBlockBox[3]) / 32f));
                                    textureCoord[textureCoordCount + 6] = (textureTop[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord[textureCoordCount + 7] = (textureTop[3] - (((float) thisBlockBox[3]) / 32f));
                                    textureCoordCount += 8;


                                    //bottom
                                    positions[positionsCount + 0] = ((float) thisBlockBox[0] + x);
                                    positions[positionsCount + 1] = ((float) thisBlockBox[1] + y);
                                    positions[positionsCount + 2] = ((float) thisBlockBox[5] + z);

                                    positions[positionsCount + 3] = ((float) thisBlockBox[0] + x);
                                    positions[positionsCount + 4] = ((float) thisBlockBox[1] + y);
                                    positions[positionsCount + 5] = ((float) thisBlockBox[2] + z);

                                    positions[positionsCount + 6] = ((float) thisBlockBox[3] + x);
                                    positions[positionsCount + 7] = ((float) thisBlockBox[1] + y);
                                    positions[positionsCount + 8] = ((float) thisBlockBox[2] + z);

                                    positions[positionsCount + 9] = ((float) thisBlockBox[3] + x);
                                    positions[positionsCount + 10] = ((float) thisBlockBox[1] + y);
                                    positions[positionsCount + 11] = ((float) thisBlockBox[5] + z);

                                    positionsCount += 12;

                                    //bottom
                                    float bottomLight = getLight(realX, y - 1, realZ);
                                    bottomLight = convertLight(bottomLight / maxLight);
                                    //bottom
                                    for (int i = 0; i < 12; i++) {
                                        light[lightCount + i] = (bottomLight);
                                    }

                                    lightCount += 12;

                                    //bottom
                                    indices[indicesTableCount + 0] = (0 + indicesCount);
                                    indices[indicesTableCount + 1] = (1 + indicesCount);
                                    indices[indicesTableCount + 2] = (2 + indicesCount);
                                    indices[indicesTableCount + 3] = (0 + indicesCount);
                                    indices[indicesTableCount + 4] = (2 + indicesCount);
                                    indices[indicesTableCount + 5] = (3 + indicesCount);
                                    indicesCount += 4;
                                    indicesTableCount += 6;


                                    // 0, 1, 2, 3, 4, 5
                                    //-x,-y,-z, x, y, z
                                    // 0, 0, 0, 1, 1, 1

                                    // 0, 1,  2, 3
                                    //-x,+x, -y,+y

                                    float[] textureBottom = getBottomTexturePoints(thisBlock);
                                    //bottom
                                    textureCoord[textureCoordCount + 0] = (textureBottom[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord[textureCoordCount + 1] = (textureBottom[2] + ((1 - (float) thisBlockBox[0]) / 32f));
                                    textureCoord[textureCoordCount + 2] = (textureBottom[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord[textureCoordCount + 3] = (textureBottom[2] + ((1 - (float) thisBlockBox[0]) / 32f));

                                    textureCoord[textureCoordCount + 4] = (textureBottom[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord[textureCoordCount + 5] = (textureBottom[3] - (((float) thisBlockBox[3]) / 32f));
                                    textureCoord[textureCoordCount + 6] = (textureBottom[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord[textureCoordCount + 7] = (textureBottom[3] - (((float) thisBlockBox[3]) / 32f));
                                    textureCoordCount += 8;
                                }
                            }
                        }
                        //todo: ------------------------------------------------------------------------------------------------=-=-=-=
                    }
                }
            }


            ChunkMeshDataObject newChunkData = new ChunkMeshDataObject();

            newChunkData.chunkX = chunkX;
            newChunkData.chunkZ = chunkZ;
            newChunkData.yHeight = yHeight;

            if (positionsCount > 0) {
//        convert the position objects into usable array
                float[] positionsArray = new float[positionsCount];
                if (positionsCount >= 0) System.arraycopy(positions, 0, positionsArray, 0, positionsCount);

                //convert the light objects into usable array
                float[] lightArray = new float[lightCount];
                if (lightCount >= 0) System.arraycopy(light, 0, lightArray, 0, lightCount);

                //convert the indices objects into usable array
                int[] indicesArray = new int[indicesTableCount];
                if (indicesTableCount >= 0) System.arraycopy(indices, 0, indicesArray, 0, indicesTableCount);

                //convert the textureCoord objects into usable array
                float[] textureCoordArray = new float[textureCoordCount];
                if (textureCoordCount >= 0) System.arraycopy(textureCoord, 0, textureCoordArray, 0, textureCoordCount);

                //pass data to container object
                newChunkData.positionsArray = positionsArray;
                newChunkData.lightArray = lightArray;
                newChunkData.indicesArray = indicesArray;
                newChunkData.textureCoordArray = textureCoordArray;

                //setChunkMesh(chunkX, chunkZ, yHeight, new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, textureAtlas));
            } else {
                //setChunkMesh(chunkX, chunkZ, yHeight, null);
                //inform the container object that this chunk is null for this part of it
                newChunkData.normalMeshIsNull = true;
            }


            if (liquidPositionsCount > 0) {
//        convert the position objects into usable array
                float[] liquidPositionsArray = new float[liquidPositionsCount];
                if (liquidPositionsCount >= 0) System.arraycopy(liquidPositions, 0, liquidPositionsArray, 0, liquidPositionsCount);

                //convert the light objects into usable array
                float[] liquidLightArray = new float[liquidLightCount];
                if (liquidLightCount >= 0) System.arraycopy(liquidLight, 0, liquidLightArray, 0, liquidLightCount);

                //convert the indices objects into usable array
                int[] liquidIndicesArray = new int[liquidIndicesTableCount];
                if (liquidIndicesTableCount >= 0) System.arraycopy(liquidIndices, 0, liquidIndicesArray, 0, liquidIndicesTableCount);

                //convert the textureCoord objects into usable array
                float[] liquidTextureCoordArray = new float[liquidTextureCoordCount];
                if (liquidTextureCoordCount >= 0) System.arraycopy(liquidTextureCoord, 0, liquidTextureCoordArray, 0, liquidTextureCoordCount);

                //pass data to container object
                newChunkData.liquidPositionsArray = liquidPositionsArray;
                newChunkData.liquidLightArray = liquidLightArray;
                newChunkData.liquidIndicesArray = liquidIndicesArray;
                newChunkData.liquidTextureCoordArray = liquidTextureCoordArray;

                //setChunkMesh(chunkX, chunkZ, yHeight, new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, textureAtlas));
            } else {
                //setChunkMesh(chunkX, chunkZ, yHeight, null);
                //inform the container object that this chunk is null for this part of it
                newChunkData.liquidMeshIsNull = true;
            }




            String keyName = chunkX + " " + chunkZ + " " + yHeight;
            //finally add it into the queue to be popped
            queue.put(keyName, newChunkData);

            //long endTime = System.nanoTime();
            //double duration = (double)(endTime - startTime) /  1_000_000_000d;  //divide by 1000000 to get milliseconds.
            //System.out.println("This took: " + duration + " seconds");
            //done, thread dies
        }).start();
    }

    public static float convertLight(float lightByte){
        return (float) Math.pow(Math.pow(lightByte, 1.5), 1.5);
    }
}
