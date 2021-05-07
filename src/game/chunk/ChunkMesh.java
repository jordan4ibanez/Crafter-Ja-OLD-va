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

            if (!newChunkMeshData.meshIsNull){
                setChunkMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, new Mesh(newChunkMeshData.positionsArray, newChunkMeshData.lightArray, newChunkMeshData.indicesArray, newChunkMeshData.textureCoordArray, textureAtlas));
            } else {
                setChunkMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, null);
            }
        }

        //if (count > 0) {
        //    System.out.println("amount of chunk meshes created:" + count);
        //}
    }

    public static void generateChunkMesh(int chunkX, int chunkZ, int yHeight) {
        //let's use all the cpu threads to the limit
        //new Thread(() -> {
            //normal block stuff
            final float[] positions = new float[152_472];
            int positionsCount = 0;

            final float[] textureCoord = new float[152_472];
            int textureCoordCount = 0;

            final int[] indices = new int[152_472];
            int indicesTableCount = 0;
            int indicesCount = 0;

            final float[] light = new float[152_472];
            int lightCount = 0;

            ChunkObject thisChunk;
            int thisBlock;
            byte thisRotation;

            thisChunk = getChunk(chunkX, chunkZ);

            if (thisChunk == null) {
                return;
            }
            if (thisChunk.mesh == null) {
                return;
            }

            for (int x = 0; x < 16; x++) {
                int realX = (chunkX * 16) + x;
                for (int z = 0; z < 16; z++) {
                    int realZ = (chunkZ * 16) + z;
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

                                if (neighborBlock >= 0 && !getBlockDrawType(neighborBlock).equals("normal")) {
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
                                    float frontLight = getLight(realX, y, realZ + 1) / maxLight;

                                    frontLight = convertLight(frontLight);

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

                                if (neighborBlock >= 0 && !getBlockDrawType(neighborBlock).equals("normal")) {
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
                                    float backLight = getLight(realX, y, realZ - 1) / maxLight;
                                    backLight = convertLight(backLight);
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

                                if (neighborBlock >= 0 && !getBlockDrawType(neighborBlock).equals("normal")) {
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
                                    float rightLight = getLight(realX + 1, y, realZ) / maxLight;
                                    rightLight = convertLight(rightLight);
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

                                if (neighborBlock >= 0 && !getBlockDrawType(neighborBlock).equals("normal")) {
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
                                    float leftLight = getLight(realX - 1, y, realZ) / maxLight;
                                    leftLight = convertLight(leftLight);
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

                                //y doesn't need a check since it has no neighbors
                                if (y + 1 < 128) {
                                    neighborBlock = thisChunk.block[posToIndex(x, y + 1, z)];
                                }

                                if (y == 127 || (neighborBlock >= 0 && !getBlockDrawType(neighborBlock).equals("normal"))) {
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
                                    float topLight = getLight(realX, y + 1, realZ) / maxLight;
                                    topLight = convertLight(topLight);
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

                                //doesn't need a neighbor chunk, chunks are 2D
                                if (y - 1 > 0) {
                                    neighborBlock = thisChunk.block[posToIndex(x, y - 1, z)];
                                }

                                //don't render bottom of world
                                if (y != 0 && neighborBlock >= 0 && !getBlockDrawType(neighborBlock).equals("normal")) {
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
                                    float bottomLight = getLight(realX, y - 1, realZ) / maxLight;
                                    bottomLight = convertLight(bottomLight);
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
                            }

                            //todo --------------------------------------- THE NORMAL DRAWTYPE
                            else if (getBlockDrawType(thisBlock).equals("normal")) {

                                int neighborBlock;

                                if (z + 1 > 15) {
                                    neighborBlock = getBlock(realX, y, realZ + 1);
                                } else {
                                    neighborBlock = thisChunk.block[posToIndex(x, y, z + 1)];
                                }

                                if (neighborBlock >= 0 && (!getBlockDrawType(neighborBlock).equals("normal") || getIfLiquid(neighborBlock))) {
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
                                    float frontLight = getLight(realX, y, realZ + 1) / maxLight;

                                    frontLight = convertLight(frontLight);

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
                                if (neighborBlock >= 0 && (!getBlockDrawType(neighborBlock).equals("normal") || getIfLiquid(neighborBlock))) {
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
                                    float backLight = getLight(realX, y, realZ - 1) / maxLight;
                                    backLight = convertLight(backLight);
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

                                if (neighborBlock >= 0 && (!getBlockDrawType(neighborBlock).equals("normal") || getIfLiquid(neighborBlock))) {
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
                                    float rightLight = getLight(realX + 1, y, realZ) / maxLight;
                                    rightLight = convertLight(rightLight);
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

                                if (neighborBlock >= 0 && (!getBlockDrawType(neighborBlock).equals("normal") || getIfLiquid(neighborBlock))) {
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
                                    float leftLight = getLight(realX - 1, y, realZ) / maxLight;
                                    leftLight = convertLight(leftLight);
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

                                if (y == 127 || neighborBlock > -1 && ((neighborBlock >= 0 && !getBlockDrawType(neighborBlock).equals("normal") || getIfLiquid(neighborBlock)))) {
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
                                    float topLight = getLight(realX, y + 1, realZ) / maxLight;
                                    topLight = convertLight(topLight);
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
                                if (y != 0 && neighborBlock >= 0 && (!getBlockDrawType(neighborBlock).equals("normal") || getIfLiquid(neighborBlock))) {
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
                                    float bottomLight = getLight(realX, y - 1, realZ) / maxLight;
                                    bottomLight = convertLight(bottomLight);
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
                                    float frontLight = getLight(realX, y, realZ + 1) / maxLight;

                                    frontLight = convertLight(frontLight);

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
                                    float backLight = getLight(realX, y, realZ - 1) / maxLight;
                                    backLight = convertLight(backLight);
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
                                    float rightLight = getLight(realX + 1, y, realZ) / maxLight;
                                    rightLight = convertLight(rightLight);
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
                                    float leftLight = getLight(realX - 1, y, realZ) / maxLight;
                                    leftLight = convertLight(leftLight);
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
                                    float topLight = getLight(realX, y + 1, realZ) / maxLight;
                                    topLight = convertLight(topLight);
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
                                    float bottomLight = getLight(realX, y - 1, realZ) / maxLight;
                                    bottomLight = convertLight(bottomLight);
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
                newChunkData.meshIsNull = true;
            }


            String keyName = chunkX + " " + chunkZ + " " + yHeight;
            //finally add it into the queue to be popped
            queue.put(keyName, newChunkData);

            //done, thread dies
        //}).start();
    }

    public static float convertLight(float lightByte){
        return (float) Math.pow(Math.pow(lightByte, 1.5), 1.5);
    }
}
