package game.chunk;

import org.joml.Vector3i;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;

import static engine.Window.windowShouldClose;
import static game.blocks.BlockDefinition.*;
import static game.chunk.Chunk.getChunk;
import static game.chunk.ChunkMeshGenerationHandler.addToChunkMeshQueue;
import static game.light.Light.getCurrentGlobalLightLevel;

public class ChunkMeshGenerator implements Runnable{

    private static final Deque<Vector3i> generationQueue = new ArrayDeque<>();

    public void run() {
        while (!windowShouldClose()) {
            pollQueue();
        }
    }

    public static void generateChunkMesh(int chunkX, int chunkZ, int yHeight) {
        generationQueue.add(new Vector3i(chunkX,yHeight, chunkZ));
    }

    public static void instantGeneration(int chunkX, int chunkZ, int yHeight){
        generationQueue.addFirst(new Vector3i(chunkX,yHeight, chunkZ));
    }

    //cache data
    //these are held on the heap
    private final static byte maxLight = 15;

    private static void pollQueue(){

        boolean solved = false;

        while (!generationQueue.isEmpty() && !solved) {

            //long startTime = System.nanoTime();

            Vector3i updateRawData;
            try {
                updateRawData = generationQueue.pop();
            } catch (Exception exp){
                return; //don't crash, basically
            }


            ChunkObject thisChunk = getChunk(updateRawData.x, updateRawData.z);

            //don't bother if the chunk doesn't exist
            if (thisChunk == null) {
                return;
            }

            int chunkX = updateRawData.x;
            int chunkZ = updateRawData.z;
            int yHeight = updateRawData.y;

            ChunkObject chunkNeighborXPlus = getChunk(chunkX + 1, chunkZ);
            ChunkObject chunkNeighborXMinus = getChunk(chunkX - 1, chunkZ);
            ChunkObject chunkNeighborZPlus = getChunk(chunkX, chunkZ + 1);
            ChunkObject chunkNeighborZMinus = getChunk(chunkX, chunkZ - 1);

            //normal block mesh data
            final LinkedList<Float> positions = new LinkedList<>();
            final LinkedList<Float> textureCoord = new LinkedList<>();
            final LinkedList<Integer> indices = new LinkedList<>();
            final LinkedList<Float> light = new LinkedList<>();

            //liquid block mesh data
            final LinkedList<Float> liquidPositions = new LinkedList<>();
            final LinkedList<Float> liquidTextureCoord = new LinkedList<>();
            final LinkedList<Integer> liquidIndices = new LinkedList<>();
            final LinkedList<Float> liquidLight = new LinkedList<>();

            //allFaces block mesh data
            final LinkedList<Float> allFacesPositions = new LinkedList<>();
            final LinkedList<Float> allFacesTextureCoord = new LinkedList<>();
            final LinkedList<Integer> allFacesIndices = new LinkedList<>();
            final LinkedList<Float> allFacesLight = new LinkedList<>();

            //normal block mesh data
            int indicesCount = 0;

            //liquid block mesh data
            int liquidIndicesCount = 0;

            //allFaces block mesh data
            int allFacesIndicesCount = 0;

            byte chunkLightLevel = getCurrentGlobalLightLevel();

            //reduces lookup time
            //reduces lookup time
            int[] blockData = thisChunk.block;
            byte[] rotationData = thisChunk.rotation;
            byte[] naturalLightData = thisChunk.naturalLight;
            byte[] torchLightData = thisChunk.torchLight;

            int x;
            for (x = 0; x < 16; x++) {
                int z;
                for (z = 0; z < 16; z++) {
                    int y;
                    for (y = yHeight * 16; y < (yHeight + 1) * 16; y++) {

                        int thisBlock = blockData[posToIndex(x, y, z)];
                        byte thisRotation = rotationData[posToIndex(x, y, z)];

                        if (thisBlock > 0) {

                            //todo --------------------------------------- THE LIQUID DRAWTYPE

                            float lightValue;
                            float[] textureWorker;
                            int neighborBlock;
                            if (getIfLiquid(thisBlock)) {

                                if (z + 1 > 15) {
                                    neighborBlock = getNeighborBlock(chunkNeighborZPlus, x, y, 0);
                                } else {
                                    neighborBlock = blockData[posToIndex(x, y, z + 1)];
                                }

                                if (neighborBlock >= 0 && getBlockDrawType(neighborBlock) != 1) {
                                    //front
                                    liquidPositions.add(1f + x);
                                    liquidPositions.add(1f + y);
                                    liquidPositions.add(1f + z);
                                    liquidPositions.add(0f + x);
                                    liquidPositions.add(1f + y);
                                    liquidPositions.add(1f + z);
                                    liquidPositions.add(0f + x);
                                    liquidPositions.add(0f + y);
                                    liquidPositions.add(1f + z);
                                    liquidPositions.add(1f + x);
                                    liquidPositions.add(0f + y);
                                    liquidPositions.add(1f + z);



                                    //front
                                    if (z + 1 > 15) {
                                        lightValue = getNeighborLight(chunkNeighborZPlus, x, y,0);
                                    } else {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x, y, z + 1)], torchLightData[posToIndex(x, y, z + 1)]);
                                    }

                                    lightValue = convertLight(lightValue / maxLight);

                                    //front
                                    for (int i = 0; i < 12; i++) {
                                        liquidLight.add(lightValue);
                                    }

                                    //front
                                    liquidIndices.add(liquidIndicesCount);
                                    liquidIndices.add(1 + liquidIndicesCount);
                                    liquidIndices.add(2 + liquidIndicesCount);
                                    liquidIndices.add(liquidIndicesCount);
                                    liquidIndices.add(2 + liquidIndicesCount);
                                    liquidIndices.add(3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;

                                    textureWorker = getFrontTexturePoints(thisBlock, thisRotation);
                                    //front
                                    liquidTextureCoord.add(textureWorker[1]);
                                    liquidTextureCoord.add(textureWorker[2]);
                                    liquidTextureCoord.add(textureWorker[0]);
                                    liquidTextureCoord.add(textureWorker[2]);
                                    liquidTextureCoord.add(textureWorker[0]);
                                    liquidTextureCoord.add(textureWorker[3]);
                                    liquidTextureCoord.add(textureWorker[1]);
                                    liquidTextureCoord.add(textureWorker[3]);
                                }


                                if (z - 1 < 0) {
                                    neighborBlock = getNeighborBlock(chunkNeighborZMinus, x, y, 15);
                                } else {
                                    neighborBlock = blockData[posToIndex(x, y, z - 1)];
                                }

                                if (neighborBlock >= 0 && getBlockDrawType(neighborBlock) != 1) {
                                    //back
                                    liquidPositions.add(0f + x);
                                    liquidPositions.add(1f + y);
                                    liquidPositions.add(0f + z);
                                    liquidPositions.add(1f + x);
                                    liquidPositions.add(1f + y);
                                    liquidPositions.add(0f + z);
                                    liquidPositions.add(1f + x);
                                    liquidPositions.add(0f + y);
                                    liquidPositions.add(0f + z);
                                    liquidPositions.add(0f + x);
                                    liquidPositions.add(0f + y);
                                    liquidPositions.add(0f + z);

                                    //back
                                    if (z - 1 < 0) {
                                        lightValue = getNeighborLight(chunkNeighborZMinus, x, y,15);
                                    } else {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x, y, z - 1)], torchLightData[posToIndex(x, y, z - 1)]);
                                    }

                                    lightValue = convertLight(lightValue / maxLight);
                                    //back
                                    for (int i = 0; i < 12; i++) {
                                        liquidLight.add(lightValue);
                                    }

                                    //back
                                    liquidIndices.add(liquidIndicesCount);
                                    liquidIndices.add(1 + liquidIndicesCount);
                                    liquidIndices.add(2 + liquidIndicesCount);
                                    liquidIndices.add(liquidIndicesCount);
                                    liquidIndices.add(2 + liquidIndicesCount);
                                    liquidIndices.add(3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;

                                    textureWorker = getBackTexturePoints(thisBlock, thisRotation);
                                    //back
                                    liquidTextureCoord.add(textureWorker[1]);
                                    liquidTextureCoord.add(textureWorker[2]);
                                    liquidTextureCoord.add(textureWorker[0]);
                                    liquidTextureCoord.add(textureWorker[2]);
                                    liquidTextureCoord.add(textureWorker[0]);
                                    liquidTextureCoord.add(textureWorker[3]);
                                    liquidTextureCoord.add(textureWorker[1]);
                                    liquidTextureCoord.add(textureWorker[3]);
                                }

                                if (x + 1 > 15) {
                                    neighborBlock = getNeighborBlock(chunkNeighborXPlus, 0, y, z);
                                } else {
                                    neighborBlock = blockData[posToIndex(x + 1, y, z)];
                                }

                                if (neighborBlock >= 0 && getBlockDrawType(neighborBlock) != 1) {
                                    //right
                                    liquidPositions.add(1f + x);
                                    liquidPositions.add(1f + y);
                                    liquidPositions.add(0f + z);
                                    liquidPositions.add(1f + x);
                                    liquidPositions.add(1f + y);
                                    liquidPositions.add(1f + z);
                                    liquidPositions.add(1f + x);
                                    liquidPositions.add(0f + y);
                                    liquidPositions.add(1f + z);
                                    liquidPositions.add(1f + x);
                                    liquidPositions.add(0f + y);
                                    liquidPositions.add(0f + z);

                                    //right

                                    if (x + 1 > 15) {
                                        lightValue = getNeighborLight(chunkNeighborXPlus, 0, y, z);
                                    } else {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x +1, y, z)], torchLightData[posToIndex(x +1, y, z)]);
                                    }

                                    lightValue = convertLight(lightValue / maxLight);
                                    //right
                                    for (int i = 0; i < 12; i++) {
                                        liquidLight.add(lightValue);
                                    }


                                    //right
                                    liquidIndices.add(liquidIndicesCount);
                                    liquidIndices.add(1 + liquidIndicesCount);
                                    liquidIndices.add(2 + liquidIndicesCount);
                                    liquidIndices.add(liquidIndicesCount);
                                    liquidIndices.add(2 + liquidIndicesCount);
                                    liquidIndices.add(3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;

                                    textureWorker = getRightTexturePoints(thisBlock, thisRotation);
                                    //right
                                    liquidTextureCoord.add(textureWorker[1]);
                                    liquidTextureCoord.add(textureWorker[2]);
                                    liquidTextureCoord.add(textureWorker[0]);
                                    liquidTextureCoord.add(textureWorker[2]);
                                    liquidTextureCoord.add(textureWorker[0]);
                                    liquidTextureCoord.add(textureWorker[3]);
                                    liquidTextureCoord.add(textureWorker[1]);
                                    liquidTextureCoord.add(textureWorker[3]);
                                }

                                if (x - 1 < 0) {
                                    neighborBlock = getNeighborBlock(chunkNeighborXMinus, 15, y, z);
                                } else {
                                    neighborBlock = blockData[posToIndex(x - 1, y, z)];
                                }

                                if (neighborBlock >= 0 && getBlockDrawType(neighborBlock) != 1) {
                                    //left
                                    liquidPositions.add(0f + x);
                                    liquidPositions.add(1f + y);
                                    liquidPositions.add(1f + z);
                                    liquidPositions.add(0f + x);
                                    liquidPositions.add(1f + y);
                                    liquidPositions.add(0f + z);
                                    liquidPositions.add(0f + x);
                                    liquidPositions.add(0f + y);
                                    liquidPositions.add(0f + z);
                                    liquidPositions.add(0f + x);
                                    liquidPositions.add(0f + y);
                                    liquidPositions.add(1f + z);

                                    //left

                                    if (x - 1 < 0) {
                                        lightValue = getNeighborLight(chunkNeighborXMinus, 15, y, z);
                                    } else {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x - 1, y, z)], torchLightData[posToIndex(x - 1, y, z)]);
                                    }

                                    lightValue = convertLight(lightValue / maxLight);
                                    //left
                                    for (int i = 0; i < 12; i++) {
                                        liquidLight.add(lightValue);
                                    }


                                    //left
                                    liquidIndices.add(liquidIndicesCount);
                                    liquidIndices.add(1 + liquidIndicesCount);
                                    liquidIndices.add(2 + liquidIndicesCount);
                                    liquidIndices.add(liquidIndicesCount);
                                    liquidIndices.add(2 + liquidIndicesCount);
                                    liquidIndices.add(3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;

                                    textureWorker = getLeftTexturePoints(thisBlock, thisRotation);
                                    //left
                                    liquidTextureCoord.add(textureWorker[1]);
                                    liquidTextureCoord.add(textureWorker[2]);
                                    liquidTextureCoord.add(textureWorker[0]);
                                    liquidTextureCoord.add(textureWorker[2]);
                                    liquidTextureCoord.add(textureWorker[0]);
                                    liquidTextureCoord.add(textureWorker[3]);
                                    liquidTextureCoord.add(textureWorker[1]);
                                    liquidTextureCoord.add(textureWorker[3]);
                                }

                                //y doesn't need a check since it has no neighbors
                                if (y + 1 < 128) {
                                    neighborBlock = blockData[posToIndex(x, y + 1, z)];
                                }

                                if (y == 127 || (neighborBlock >= 0 && getBlockDrawType(neighborBlock) != 1)) {
                                    //top
                                    liquidPositions.add(0f + x);
                                    liquidPositions.add(1f + y);
                                    liquidPositions.add(0f + z);
                                    liquidPositions.add(0f + x);
                                    liquidPositions.add(1f + y);
                                    liquidPositions.add(1f + z);
                                    liquidPositions.add(1f + x);
                                    liquidPositions.add(1f + y);
                                    liquidPositions.add(1f + z);
                                    liquidPositions.add(1f + x);
                                    liquidPositions.add(1f + y);
                                    liquidPositions.add(0f + z);


                                    //top

                                    //y doesn't need a check since it has no neighbors
                                    if (y + 1 < 128) {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x, y + 1, z)], torchLightData[posToIndex(x, y + 1, z)]);
                                    } else {
                                        lightValue = maxLight;
                                    }

                                    lightValue = convertLight(lightValue / maxLight);
                                    //top
                                    for (int i = 0; i < 12; i++) {
                                        liquidLight.add(lightValue);
                                    }


                                    //top
                                    liquidIndices.add(liquidIndicesCount);
                                    liquidIndices.add(1 + liquidIndicesCount);
                                    liquidIndices.add(2 + liquidIndicesCount);
                                    liquidIndices.add(liquidIndicesCount);
                                    liquidIndices.add(2 + liquidIndicesCount);
                                    liquidIndices.add(3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;

                                    textureWorker = getTopTexturePoints(thisBlock);
                                    //top
                                    liquidTextureCoord.add(textureWorker[1]);
                                    liquidTextureCoord.add(textureWorker[2]);
                                    liquidTextureCoord.add(textureWorker[0]);
                                    liquidTextureCoord.add(textureWorker[2]);
                                    liquidTextureCoord.add(textureWorker[0]);
                                    liquidTextureCoord.add(textureWorker[3]);
                                    liquidTextureCoord.add(textureWorker[1]);
                                    liquidTextureCoord.add(textureWorker[3]);
                                }

                                //doesn't need a neighbor chunk, chunks are 2D
                                if (y - 1 > 0) {
                                    neighborBlock = blockData[posToIndex(x, y - 1, z)];
                                }

                                //don't render bottom of world
                                if (y != 0 && neighborBlock >= 0 && getBlockDrawType(neighborBlock) != 1) {
                                    //bottom
                                    liquidPositions.add(0f + x);
                                    liquidPositions.add(0f + y);
                                    liquidPositions.add(1f + z);
                                    liquidPositions.add(0f + x);
                                    liquidPositions.add(0f + y);
                                    liquidPositions.add(0f + z);
                                    liquidPositions.add(1f + x);
                                    liquidPositions.add(0f + y);
                                    liquidPositions.add(0f + z);
                                    liquidPositions.add(1f + x);
                                    liquidPositions.add(0f + y);
                                    liquidPositions.add(1f + z);

                                    //bottom

                                    //doesn't need a neighbor chunk, chunks are 2D
                                    if (y - 1 > 0) {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x, y - 1, z)], torchLightData[posToIndex(x, y - 1, z)]);
                                    } else {
                                        lightValue = 0;
                                    }

                                    lightValue = convertLight(lightValue / maxLight);
                                    //bottom
                                    for (int i = 0; i < 12; i++) {
                                        liquidLight.add(lightValue);
                                    }

                                    //bottom
                                    liquidIndices.add(liquidIndicesCount);
                                    liquidIndices.add(1 + liquidIndicesCount);
                                    liquidIndices.add(2 + liquidIndicesCount);
                                    liquidIndices.add(liquidIndicesCount);
                                    liquidIndices.add(2 + liquidIndicesCount);
                                    liquidIndices.add(3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;

                                    textureWorker = getBottomTexturePoints(thisBlock);
                                    //bottom
                                    liquidTextureCoord.add(textureWorker[1]);
                                    liquidTextureCoord.add(textureWorker[2]);
                                    liquidTextureCoord.add(textureWorker[0]);
                                    liquidTextureCoord.add(textureWorker[2]);
                                    liquidTextureCoord.add(textureWorker[0]);
                                    liquidTextureCoord.add(textureWorker[3]);
                                    liquidTextureCoord.add(textureWorker[1]);
                                    liquidTextureCoord.add(textureWorker[3]);
                                }
                            }
                            //todo --------------------------------------- THE NORMAL DRAWTYPE (standard blocks)
                            else if (getBlockDrawType(thisBlock) == 1) {

                                if (z + 1 > 15) {
                                    neighborBlock = getNeighborBlock(chunkNeighborZPlus, x, y, 0);
                                } else {
                                    neighborBlock = blockData[posToIndex(x, y, z + 1)];
                                }

                                if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
                                    //front
                                    positions.add(1f + x);
                                    positions.add(1f + y);
                                    positions.add(1f + z);
                                    positions.add(0f + x);
                                    positions.add(1f + y);
                                    positions.add(1f + z);
                                    positions.add(0f + x);
                                    positions.add(0f + y);
                                    positions.add(1f + z);
                                    positions.add(1f + x);
                                    positions.add(0f + y);
                                    positions.add(1f + z);

                                    //front
                                    if (z + 1 > 15) {
                                        lightValue = getNeighborLight(chunkNeighborZPlus, x, y,0);
                                    } else {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x, y, z + 1)], torchLightData[posToIndex(x, y, z + 1)]);
                                    }

                                    lightValue = convertLight(lightValue / maxLight);

                                    //front
                                    for (int i = 0; i < 12; i++) {
                                        light.add(lightValue);
                                    }

                                    //front
                                    indices.add(indicesCount);
                                    indices.add(1 + indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(3 + indicesCount);
                                    indicesCount += 4;

                                    textureWorker = getFrontTexturePoints(thisBlock, thisRotation);
                                    //front
                                    textureCoord.add(textureWorker[1]);
                                    textureCoord.add(textureWorker[2]);
                                    textureCoord.add(textureWorker[0]);
                                    textureCoord.add(textureWorker[2]);
                                    textureCoord.add(textureWorker[0]);
                                    textureCoord.add(textureWorker[3]);
                                    textureCoord.add(textureWorker[1]);
                                    textureCoord.add(textureWorker[3]);
                                }

                                if (z - 1 < 0) {
                                    neighborBlock = getNeighborBlock(chunkNeighborZMinus, x, y, 15);
                                } else {
                                    neighborBlock = blockData[posToIndex(x, y, z - 1)];
                                }

                                if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
                                    //back
                                    positions.add(0f + x);
                                    positions.add(1f + y);
                                    positions.add(0f + z);
                                    positions.add(1f + x);
                                    positions.add(1f + y);
                                    positions.add(0f + z);
                                    positions.add(1f + x);
                                    positions.add(0f + y);
                                    positions.add(0f + z);
                                    positions.add(0f + x);
                                    positions.add(0f + y);
                                    positions.add(0f + z);

                                    //back
                                    if (z - 1 < 0) {
                                        lightValue = getNeighborLight(chunkNeighborZMinus, x, y,15);
                                    } else {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x, y, z - 1)], torchLightData[posToIndex(x, y, z - 1)]);
                                    }

                                    lightValue = convertLight(lightValue / maxLight);
                                    //back
                                    for (int i = 0; i < 12; i++) {
                                        light.add(lightValue);
                                    }

                                    //back
                                    indices.add(indicesCount);
                                    indices.add(1 + indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(3 + indicesCount);
                                    indicesCount += 4;

                                    textureWorker = getBackTexturePoints(thisBlock, thisRotation);
                                    //back
                                    textureCoord.add(textureWorker[1]);
                                    textureCoord.add(textureWorker[2]);
                                    textureCoord.add(textureWorker[0]);
                                    textureCoord.add(textureWorker[2]);
                                    textureCoord.add(textureWorker[0]);
                                    textureCoord.add(textureWorker[3]);
                                    textureCoord.add(textureWorker[1]);
                                    textureCoord.add(textureWorker[3]);
                                }

                                if (x + 1 > 15) {
                                    neighborBlock = getNeighborBlock(chunkNeighborXPlus, 0, y, z);
                                } else {
                                    neighborBlock = blockData[posToIndex(x + 1, y, z)];
                                }

                                if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
                                    //right
                                    positions.add(1f + x);
                                    positions.add(1f + y);
                                    positions.add(0f + z);
                                    positions.add(1f + x);
                                    positions.add(1f + y);
                                    positions.add(1f + z);
                                    positions.add(1f + x);
                                    positions.add(0f + y);
                                    positions.add(1f + z);
                                    positions.add(1f + x);
                                    positions.add(0f + y);
                                    positions.add(0f + z);

                                    //right
                                    if (x + 1 > 15) {
                                        lightValue = getNeighborLight(chunkNeighborXPlus, 0, y, z);
                                    } else {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x + 1, y, z)], torchLightData[posToIndex(x + 1, y, z)]);
                                    }

                                    lightValue = convertLight(lightValue / maxLight);
                                    //right
                                    for (int i = 0; i < 12; i++) {
                                        light.add(lightValue);
                                    }

                                    //right
                                    indices.add(indicesCount);
                                    indices.add(1 + indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(3 + indicesCount);
                                    indicesCount += 4;

                                    textureWorker = getRightTexturePoints(thisBlock, thisRotation);
                                    //right
                                    textureCoord.add(textureWorker[1]);
                                    textureCoord.add(textureWorker[2]);
                                    textureCoord.add(textureWorker[0]);
                                    textureCoord.add(textureWorker[2]);
                                    textureCoord.add(textureWorker[0]);
                                    textureCoord.add(textureWorker[3]);
                                    textureCoord.add(textureWorker[1]);
                                    textureCoord.add(textureWorker[3]);
                                }

                                if (x - 1 < 0) {
                                    neighborBlock = getNeighborBlock(chunkNeighborXMinus, 15, y, z);
                                } else {
                                    neighborBlock = blockData[posToIndex(x - 1, y, z)];
                                }

                                if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
                                    //left
                                    positions.add(0f + x);
                                    positions.add(1f + y);
                                    positions.add(1f + z);
                                    positions.add(0f + x);
                                    positions.add(1f + y);
                                    positions.add(0f + z);
                                    positions.add(0f + x);
                                    positions.add(0f + y);
                                    positions.add(0f + z);
                                    positions.add(0f + x);
                                    positions.add(0f + y);
                                    positions.add(1f + z);

                                    //left
                                    if (x - 1 < 0) {
                                        lightValue = getNeighborLight(chunkNeighborXMinus, 15, y, z);
                                    } else {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x - 1, y, z)], torchLightData[posToIndex(x - 1, y, z)]);
                                    }

                                    lightValue = convertLight(lightValue / maxLight);
                                    //left
                                    for (int i = 0; i < 12; i++) {
                                        light.add(lightValue);
                                    }

                                    //left
                                    indices.add(indicesCount);
                                    indices.add(1 + indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(3 + indicesCount);
                                    indicesCount += 4;

                                    textureWorker = getLeftTexturePoints(thisBlock, thisRotation);
                                    //left
                                    textureCoord.add(textureWorker[1]);
                                    textureCoord.add(textureWorker[2]);
                                    textureCoord.add(textureWorker[0]);
                                    textureCoord.add(textureWorker[2]);
                                    textureCoord.add(textureWorker[0]);
                                    textureCoord.add(textureWorker[3]);
                                    textureCoord.add(textureWorker[1]);
                                    textureCoord.add(textureWorker[3]);
                                }

                                if (y + 1 < 128) {
                                    neighborBlock = blockData[posToIndex(x, y + 1, z)];
                                }

                                if (y == 127 || neighborBlock > -1 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
                                    //top
                                    positions.add(0f + x);
                                    positions.add(1f + y);
                                    positions.add(0f + z);
                                    positions.add(0f + x);
                                    positions.add(1f + y);
                                    positions.add(1f + z);
                                    positions.add(1f + x);
                                    positions.add(1f + y);
                                    positions.add(1f + z);
                                    positions.add(1f + x);
                                    positions.add(1f + y);
                                    positions.add(0f + z);

                                    //top
                                    if (y + 1 < 128) {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x, y + 1, z)], torchLightData[posToIndex(x, y + 1, z)]);
                                    } else {
                                        lightValue = maxLight;
                                    }

                                    lightValue = convertLight(lightValue / maxLight);

                                    //top
                                    for (int i = 0; i < 12; i++) {
                                        light.add(lightValue);
                                    }

                                    //top
                                    indices.add(indicesCount);
                                    indices.add(1 + indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(3 + indicesCount);
                                    indicesCount += 4;

                                    textureWorker = getTopTexturePoints(thisBlock);
                                    //top
                                    textureCoord.add(textureWorker[1]);
                                    textureCoord.add(textureWorker[2]);
                                    textureCoord.add(textureWorker[0]);
                                    textureCoord.add(textureWorker[2]);
                                    textureCoord.add(textureWorker[0]);
                                    textureCoord.add(textureWorker[3]);
                                    textureCoord.add(textureWorker[1]);
                                    textureCoord.add(textureWorker[3]);
                                }

                                if (y - 1 > 0) {
                                    neighborBlock = blockData[posToIndex(x, y - 1, z)];
                                }

                                if (y != 0 && neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
                                    //bottom
                                    positions.add(0f + x);
                                    positions.add(0f + y);
                                    positions.add(1f + z);
                                    positions.add(0f + x);
                                    positions.add(0f + y);
                                    positions.add(0f + z);
                                    positions.add(1f + x);
                                    positions.add(0f + y);
                                    positions.add(0f + z);
                                    positions.add(1f + x);
                                    positions.add(0f + y);
                                    positions.add(1f + z);

                                    //bottom
                                    if (y - 1 > 0) {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x, y - 1, z)], torchLightData[posToIndex(x, y - 1, z)]);
                                    } else {
                                        lightValue = 0;
                                    }

                                    lightValue = convertLight(lightValue / maxLight);
                                    //bottom
                                    for (int i = 0; i < 12; i++) {
                                        light.add(lightValue);
                                    }

                                    //bottom
                                    indices.add(indicesCount);
                                    indices.add(1 + indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(3 + indicesCount);
                                    indicesCount += 4;

                                    textureWorker = getBottomTexturePoints(thisBlock);
                                    //bottom
                                    textureCoord.add(textureWorker[1]);
                                    textureCoord.add(textureWorker[2]);
                                    textureCoord.add(textureWorker[0]);
                                    textureCoord.add(textureWorker[2]);
                                    textureCoord.add(textureWorker[0]);
                                    textureCoord.add(textureWorker[3]);
                                    textureCoord.add(textureWorker[1]);
                                    textureCoord.add(textureWorker[3]);
                                }

                            }
                            //todo --------------------------------------- THE ALLFACES DRAWTYPE

                            else if (getBlockDrawType(thisBlock) == 4) {
                                {
                                    //front
                                    allFacesPositions.add(1f + x);
                                    allFacesPositions.add(1f + y);
                                    allFacesPositions.add(1f + z);
                                    allFacesPositions.add(0f + x);
                                    allFacesPositions.add(1f + y);
                                    allFacesPositions.add(1f + z);
                                    allFacesPositions.add(0f + x);
                                    allFacesPositions.add(0f + y);
                                    allFacesPositions.add(1f + z);
                                    allFacesPositions.add(1f + x);
                                    allFacesPositions.add(0f + y);
                                    allFacesPositions.add(1f + z);

                                    //front
                                    if (z + 1 > 15) {
                                        lightValue = getNeighborLight(chunkNeighborZPlus, x, y,0);
                                    } else {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x, y, z + 1)], torchLightData[posToIndex(x, y, z + 1)]);
                                    }

                                    lightValue = convertLight(lightValue / maxLight);

                                    //front
                                    for (int i = 0; i < 12; i++) {
                                       allFacesLight.add(lightValue);
                                    }


                                    //front
                                    allFacesIndices.add(allFacesIndicesCount);
                                    allFacesIndices.add(1 + allFacesIndicesCount);
                                    allFacesIndices.add(2 + allFacesIndicesCount);
                                    allFacesIndices.add(allFacesIndicesCount);
                                    allFacesIndices.add(2 + allFacesIndicesCount);
                                    allFacesIndices.add(3 + allFacesIndicesCount);
                                    allFacesIndicesCount += 4;

                                    textureWorker = getFrontTexturePoints(thisBlock, thisRotation);
                                    //front
                                    allFacesTextureCoord.add(textureWorker[1]);
                                    allFacesTextureCoord.add(textureWorker[2]);
                                    allFacesTextureCoord.add(textureWorker[0]);
                                    allFacesTextureCoord.add(textureWorker[2]);
                                    allFacesTextureCoord.add(textureWorker[0]);
                                    allFacesTextureCoord.add(textureWorker[3]);
                                    allFacesTextureCoord.add(textureWorker[1]);
                                    allFacesTextureCoord.add(textureWorker[3]);
                                }

                                {
                                    //back
                                    allFacesPositions.add(0f + x);
                                    allFacesPositions.add(1f + y);
                                    allFacesPositions.add(0f + z);
                                    allFacesPositions.add(1f + x);
                                    allFacesPositions.add(1f + y);
                                    allFacesPositions.add(0f + z);
                                    allFacesPositions.add(1f + x);
                                    allFacesPositions.add(0f + y);
                                    allFacesPositions.add(0f + z);
                                    allFacesPositions.add(0f + x);
                                    allFacesPositions.add(0f + y);
                                    allFacesPositions.add(0f + z);

                                    //back

                                    if (z - 1 < 0) {
                                        lightValue = getNeighborLight(chunkNeighborZMinus, x, y,15);
                                    } else {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x, y, z - 1)], torchLightData[posToIndex(x, y, z - 1)]);
                                    }

                                    lightValue = convertLight(lightValue / maxLight);
                                    //back
                                    for (int i = 0; i < 12; i++) {
                                       allFacesLight.add(lightValue);
                                    }

                                    //back
                                    allFacesIndices.add(allFacesIndicesCount);
                                    allFacesIndices.add(1 + allFacesIndicesCount);
                                    allFacesIndices.add(2 + allFacesIndicesCount);
                                    allFacesIndices.add(allFacesIndicesCount);
                                    allFacesIndices.add(2 + allFacesIndicesCount);
                                    allFacesIndices.add(3 + allFacesIndicesCount);
                                    allFacesIndicesCount += 4;

                                    textureWorker = getBackTexturePoints(thisBlock, thisRotation);
                                    //back
                                    allFacesTextureCoord.add(textureWorker[1]);
                                    allFacesTextureCoord.add(textureWorker[2]);
                                    allFacesTextureCoord.add(textureWorker[0]);
                                    allFacesTextureCoord.add(textureWorker[2]);
                                    allFacesTextureCoord.add(textureWorker[0]);
                                    allFacesTextureCoord.add(textureWorker[3]);
                                    allFacesTextureCoord.add(textureWorker[1]);
                                    allFacesTextureCoord.add(textureWorker[3]);
                                }

                                {
                                    //right
                                    allFacesPositions.add(1f + x);
                                    allFacesPositions.add(1f + y);
                                    allFacesPositions.add(0f + z);
                                    allFacesPositions.add(1f + x);
                                    allFacesPositions.add(1f + y);
                                    allFacesPositions.add(1f + z);
                                    allFacesPositions.add(1f + x);
                                    allFacesPositions.add(0f + y);
                                    allFacesPositions.add(1f + z);
                                    allFacesPositions.add(1f + x);
                                    allFacesPositions.add(0f + y);
                                    allFacesPositions.add(0f + z);

                                    //right

                                    if (x + 1 > 15) {
                                        lightValue = getNeighborLight(chunkNeighborXPlus, 0, y, z);
                                    } else {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x + 1, y, z)], torchLightData[posToIndex(x + 1, y, z)]);
                                    }

                                    lightValue = convertLight(lightValue / maxLight);
                                    //right
                                    for (int i = 0; i < 12; i++) {
                                       allFacesLight.add(lightValue);
                                    }

                                    //right
                                    allFacesIndices.add(allFacesIndicesCount);
                                    allFacesIndices.add(1 + allFacesIndicesCount);
                                    allFacesIndices.add(2 + allFacesIndicesCount);
                                    allFacesIndices.add(allFacesIndicesCount);
                                    allFacesIndices.add(2 + allFacesIndicesCount);
                                    allFacesIndices.add(3 + allFacesIndicesCount);
                                    allFacesIndicesCount += 4;

                                    textureWorker = getRightTexturePoints(thisBlock, thisRotation);
                                    //right
                                    allFacesTextureCoord.add(textureWorker[1]);
                                    allFacesTextureCoord.add(textureWorker[2]);
                                    allFacesTextureCoord.add(textureWorker[0]);
                                    allFacesTextureCoord.add(textureWorker[2]);
                                    allFacesTextureCoord.add(textureWorker[0]);
                                    allFacesTextureCoord.add(textureWorker[3]);
                                    allFacesTextureCoord.add(textureWorker[1]);
                                    allFacesTextureCoord.add(textureWorker[3]);
                                }

                                {
                                    //left
                                    allFacesPositions.add(0f + x);
                                    allFacesPositions.add(1f + y);
                                    allFacesPositions.add(1f + z);
                                    allFacesPositions.add(0f + x);
                                    allFacesPositions.add(1f + y);
                                    allFacesPositions.add(0f + z);
                                    allFacesPositions.add(0f + x);
                                    allFacesPositions.add(0f + y);
                                    allFacesPositions.add(0f + z);
                                    allFacesPositions.add(0f + x);
                                    allFacesPositions.add(0f + y);
                                    allFacesPositions.add(1f + z);

                                    //left

                                    if (x - 1 < 0) {
                                        lightValue = getNeighborLight(chunkNeighborXMinus, 15, y, z);
                                    } else {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x - 1, y, z)], torchLightData[posToIndex(x - 1, y, z)]);
                                    }

                                    lightValue = convertLight(lightValue / maxLight);
                                    //left
                                    for (int i = 0; i < 12; i++) {
                                       allFacesLight.add(lightValue);
                                    }

                                    //left
                                    allFacesIndices.add(allFacesIndicesCount);
                                    allFacesIndices.add(1 + allFacesIndicesCount);
                                    allFacesIndices.add(2 + allFacesIndicesCount);
                                    allFacesIndices.add(allFacesIndicesCount);
                                    allFacesIndices.add(2 + allFacesIndicesCount);
                                    allFacesIndices.add(3 + allFacesIndicesCount);
                                    allFacesIndicesCount += 4;

                                    textureWorker = getLeftTexturePoints(thisBlock, thisRotation);
                                    //left
                                    allFacesTextureCoord.add(textureWorker[1]);
                                    allFacesTextureCoord.add(textureWorker[2]);
                                    allFacesTextureCoord.add(textureWorker[0]);
                                    allFacesTextureCoord.add(textureWorker[2]);
                                    allFacesTextureCoord.add(textureWorker[0]);
                                    allFacesTextureCoord.add(textureWorker[3]);
                                    allFacesTextureCoord.add(textureWorker[1]);
                                    allFacesTextureCoord.add(textureWorker[3]);
                                }

                                 {
                                    //top
                                    allFacesPositions.add(0f + x);
                                    allFacesPositions.add(1f + y);
                                    allFacesPositions.add(0f + z);
                                    allFacesPositions.add(0f + x);
                                    allFacesPositions.add(1f + y);
                                    allFacesPositions.add(1f + z);
                                    allFacesPositions.add(1f + x);
                                    allFacesPositions.add(1f + y);
                                    allFacesPositions.add(1f + z);
                                    allFacesPositions.add(1f + x);
                                    allFacesPositions.add(1f + y);
                                    allFacesPositions.add(0f + z);

                                    //top

                                    if (y + 1 < 128) {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x, y + 1, z)], torchLightData[posToIndex(x, y + 1, z)]);
                                    } else {
                                        lightValue = maxLight;
                                    }

                                     lightValue = convertLight(lightValue / maxLight);

                                    //top
                                    for (int i = 0; i < 12; i++) {
                                       allFacesLight.add(lightValue);
                                    }

                                    //top
                                    allFacesIndices.add(allFacesIndicesCount);
                                    allFacesIndices.add(1 + allFacesIndicesCount);
                                    allFacesIndices.add(2 + allFacesIndicesCount);
                                    allFacesIndices.add(allFacesIndicesCount);
                                    allFacesIndices.add(2 + allFacesIndicesCount);
                                    allFacesIndices.add(3 + allFacesIndicesCount);
                                    allFacesIndicesCount += 4;

                                     textureWorker = getTopTexturePoints(thisBlock);
                                    //top
                                    allFacesTextureCoord.add(textureWorker[1]);
                                    allFacesTextureCoord.add(textureWorker[2]);
                                    allFacesTextureCoord.add(textureWorker[0]);
                                    allFacesTextureCoord.add(textureWorker[2]);
                                    allFacesTextureCoord.add(textureWorker[0]);
                                    allFacesTextureCoord.add(textureWorker[3]);
                                    allFacesTextureCoord.add(textureWorker[1]);
                                    allFacesTextureCoord.add(textureWorker[3]);
                                 }


                                if (y != 0) {
                                    //bottom
                                    allFacesPositions.add(0f + x);
                                    allFacesPositions.add(0f + y);
                                    allFacesPositions.add(1f + z);
                                    allFacesPositions.add(0f + x);
                                    allFacesPositions.add(0f + y);
                                    allFacesPositions.add(0f + z);
                                    allFacesPositions.add(1f + x);
                                    allFacesPositions.add(0f + y);
                                    allFacesPositions.add(0f + z);
                                    allFacesPositions.add(1f + x);
                                    allFacesPositions.add(0f + y);
                                    allFacesPositions.add(1f + z);

                                    //bottom

                                    if (y - 1 > 0) {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x, y - 1, z)], torchLightData[posToIndex(x, y - 1, z)]);
                                    } else {
                                        lightValue = 0;
                                    }

                                    lightValue = convertLight(lightValue / maxLight);
                                    //bottom
                                    for (int i = 0; i < 12; i++) {
                                       allFacesLight.add(lightValue);
                                    }

                                    //bottom
                                    allFacesIndices.add(allFacesIndicesCount);
                                    allFacesIndices.add(1 + allFacesIndicesCount);
                                    allFacesIndices.add(2 + allFacesIndicesCount);
                                    allFacesIndices.add(allFacesIndicesCount);
                                    allFacesIndices.add(2 + allFacesIndicesCount);
                                    allFacesIndices.add(3 + allFacesIndicesCount);
                                    allFacesIndicesCount += 4;

                                    textureWorker = getBottomTexturePoints(thisBlock);
                                    //bottom
                                    allFacesTextureCoord.add(textureWorker[1]);
                                    allFacesTextureCoord.add(textureWorker[2]);
                                    allFacesTextureCoord.add(textureWorker[0]);
                                    allFacesTextureCoord.add(textureWorker[2]);
                                    allFacesTextureCoord.add(textureWorker[0]);
                                    allFacesTextureCoord.add(textureWorker[3]);
                                    allFacesTextureCoord.add(textureWorker[1]);
                                    allFacesTextureCoord.add(textureWorker[3]);
                                }

                            }
                            //todo: ---------------------------------------------------------- the block box draw type
                            else {
                                for (double[] thisBlockBox : getBlockShape(thisBlock, thisRotation)) {
                                    // 0, 1, 2, 3, 4, 5
                                    //-x,-y,-z, x, y, z
                                    // 0, 0, 0, 1, 1, 1

                                    //front
                                    positions.add((float) thisBlockBox[3] + x);
                                    positions.add((float) thisBlockBox[4] + y);
                                    positions.add((float) thisBlockBox[5] + z);
                                    positions.add((float) thisBlockBox[0] + x);
                                    positions.add((float) thisBlockBox[4] + y);
                                    positions.add((float) thisBlockBox[5] + z);
                                    positions.add((float) thisBlockBox[0] + x);
                                    positions.add((float) thisBlockBox[1] + y);
                                    positions.add((float) thisBlockBox[5] + z);
                                    positions.add((float) thisBlockBox[3] + x);
                                    positions.add((float) thisBlockBox[1] + y);
                                    positions.add((float) thisBlockBox[5] + z);

                                    //front
                                    if (z + 1 > 15) {
                                        lightValue = getNeighborLight(chunkNeighborZPlus, x, y,0);
                                    } else {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x, y, z + 1)], torchLightData[posToIndex(x, y, z + 1)]);
                                    }

                                    lightValue = convertLight(lightValue / maxLight);

                                    //front
                                    for (int i = 0; i < 12; i++) {
                                        light.add(lightValue);
                                    }

                                    //front
                                    indices.add(indicesCount);
                                    indices.add(1 + indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(3 + indicesCount);
                                    indicesCount += 4;

                                    // 0, 1,  2, 3
                                    //-x,+x, -y,+y

                                    textureWorker = getFrontTexturePoints(thisBlock, thisRotation);

                                    //front
                                    textureCoord.add(textureWorker[1] - ((1 - (float) thisBlockBox[3]) / 32f)); //x positive
                                    textureCoord.add(textureWorker[2] + ((1 - (float) thisBlockBox[4]) / 32f)); //y positive
                                    textureCoord.add(textureWorker[0] - ((0 - (float) thisBlockBox[0]) / 32f)); //x negative
                                    textureCoord.add(textureWorker[2] + ((1 - (float) thisBlockBox[4]) / 32f)); //y positive
                                    textureCoord.add(textureWorker[0] - ((0 - (float) thisBlockBox[0]) / 32f)); //x negative
                                    textureCoord.add(textureWorker[3] - (((float) thisBlockBox[1]) / 32f));   //y negative
                                    textureCoord.add(textureWorker[1] - ((1 - (float) thisBlockBox[3]) / 32f)); //x positive
                                    textureCoord.add(textureWorker[3] - (((float) thisBlockBox[1]) / 32f));   //y negative


                                    //back
                                    positions.add((float) thisBlockBox[0] + x);
                                    positions.add((float) thisBlockBox[4] + y);
                                    positions.add((float) thisBlockBox[2] + z);
                                    positions.add((float) thisBlockBox[3] + x);
                                    positions.add((float) thisBlockBox[4] + y);
                                    positions.add((float) thisBlockBox[2] + z);
                                    positions.add((float) thisBlockBox[3] + x);
                                    positions.add((float) thisBlockBox[1] + y);
                                    positions.add((float) thisBlockBox[2] + z);
                                    positions.add((float) thisBlockBox[0] + x);
                                    positions.add((float) thisBlockBox[1] + y);
                                    positions.add((float) thisBlockBox[2] + z);


                                    //back
                                    if (z - 1 < 0) {
                                        lightValue = getNeighborLight(chunkNeighborZMinus, x, y,15);
                                    } else {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x, y, z - 1)], torchLightData[posToIndex(x, y, z - 1)]);
                                    }


                                    lightValue = convertLight(lightValue / maxLight);
                                    //back
                                    for (int i = 0; i < 12; i++) {
                                        light.add(lightValue);
                                    }

                                    //back
                                    indices.add(indicesCount);
                                    indices.add(1 + indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(3 + indicesCount);
                                    indicesCount += 4;

                                    textureWorker = getBackTexturePoints(thisBlock, thisRotation);

                                    //back
                                    textureCoord.add(textureWorker[1] - ((1 - (float) thisBlockBox[0]) / 32f));
                                    textureCoord.add(textureWorker[2] + ((1 - (float) thisBlockBox[4]) / 32f));
                                    textureCoord.add(textureWorker[0] - ((0 - (float) thisBlockBox[3]) / 32f));
                                    textureCoord.add(textureWorker[2] + ((1 - (float) thisBlockBox[4]) / 32f));
                                    textureCoord.add(textureWorker[0] - ((0 - (float) thisBlockBox[3]) / 32f));
                                    textureCoord.add(textureWorker[3] - (((float) thisBlockBox[1]) / 32f));
                                    textureCoord.add(textureWorker[1] - ((1 - (float) thisBlockBox[0]) / 32f));
                                    textureCoord.add(textureWorker[3] - (((float) thisBlockBox[1]) / 32f));


                                    //right
                                    positions.add((float) thisBlockBox[3] + x);
                                    positions.add((float) thisBlockBox[4] + y);
                                    positions.add((float) thisBlockBox[2] + z);
                                    positions.add((float) thisBlockBox[3] + x);
                                    positions.add((float) thisBlockBox[4] + y);
                                    positions.add((float) thisBlockBox[5] + z);
                                    positions.add((float) thisBlockBox[3] + x);
                                    positions.add((float) thisBlockBox[1] + y);
                                    positions.add((float) thisBlockBox[5] + z);
                                    positions.add((float) thisBlockBox[3] + x);
                                    positions.add((float) thisBlockBox[1] + y);
                                    positions.add((float) thisBlockBox[2] + z);

                                    //right
                                    if (x + 1 > 15) {
                                        lightValue = getNeighborLight(chunkNeighborXPlus, 0, y, z);
                                    } else {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x + 1, y, z)], torchLightData[posToIndex(x + 1, y, z)]);
                                    }
                                    lightValue = convertLight(lightValue / maxLight);
                                    //right
                                    for (int i = 0; i < 12; i++) {
                                        light.add(lightValue);
                                    }

                                    //right
                                    indices.add(indicesCount);
                                    indices.add(1 + indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(3 + indicesCount);
                                    indicesCount += 4;

                                    // 0, 1, 2, 3, 4, 5
                                    //-x,-y,-z, x, y, z
                                    // 0, 0, 0, 1, 1, 1

                                    // 0, 1,  2, 3
                                    //-x,+x, -y,+y


                                    textureWorker = getRightTexturePoints(thisBlock, thisRotation);
                                    //right
                                    textureCoord.add(textureWorker[1] - ((1 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord.add(textureWorker[2] + ((1 - (float) thisBlockBox[4]) / 32f));
                                    textureCoord.add(textureWorker[0] - ((0 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord.add(textureWorker[2] + ((1 - (float) thisBlockBox[4]) / 32f));
                                    textureCoord.add(textureWorker[0] - ((0 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord.add(textureWorker[3] - (((float) thisBlockBox[1]) / 32f));
                                    textureCoord.add(textureWorker[1] - ((1 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord.add(textureWorker[3] - (((float) thisBlockBox[1]) / 32f));


                                    //left
                                    positions.add((float) thisBlockBox[0] + x);
                                    positions.add((float) thisBlockBox[4] + y);
                                    positions.add((float) thisBlockBox[5] + z);
                                    positions.add((float) thisBlockBox[0] + x);
                                    positions.add((float) thisBlockBox[4] + y);
                                    positions.add((float) thisBlockBox[2] + z);
                                    positions.add((float) thisBlockBox[0] + x);
                                    positions.add((float) thisBlockBox[1] + y);
                                    positions.add((float) thisBlockBox[2] + z);
                                    positions.add((float) thisBlockBox[0] + x);
                                    positions.add((float) thisBlockBox[1] + y);
                                    positions.add((float) thisBlockBox[5] + z);


                                    //left
                                    if (x - 1 < 0) {
                                        lightValue = getNeighborLight(chunkNeighborXMinus, 15, y, z);
                                    } else {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x - 1, y, z)], torchLightData[posToIndex(x - 1, y, z)]);
                                    }
                                    lightValue = convertLight(lightValue / maxLight);
                                    //left
                                    for (int i = 0; i < 12; i++) {
                                        light.add(lightValue);
                                    }

                                    //left
                                    indices.add(indicesCount);
                                    indices.add(1 + indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(3 + indicesCount);
                                    indicesCount += 4;

                                    textureWorker = getLeftTexturePoints(thisBlock, thisRotation);
                                    //left
                                    textureCoord.add(textureWorker[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord.add(textureWorker[2] + ((1 - (float) thisBlockBox[4]) / 32f));
                                    textureCoord.add(textureWorker[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord.add(textureWorker[2] + ((1 - (float) thisBlockBox[4]) / 32f));
                                    textureCoord.add(textureWorker[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord.add(textureWorker[3] - (((float) thisBlockBox[1]) / 32f));
                                    textureCoord.add(textureWorker[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord.add(textureWorker[3] - (((float) thisBlockBox[1]) / 32f));


                                    //top
                                    positions.add((float) thisBlockBox[0] + x);
                                    positions.add((float) thisBlockBox[4] + y);
                                    positions.add((float) thisBlockBox[2] + z);
                                    positions.add((float) thisBlockBox[0] + x);
                                    positions.add((float) thisBlockBox[4] + y);
                                    positions.add((float) thisBlockBox[5] + z);
                                    positions.add((float) thisBlockBox[3] + x);
                                    positions.add((float) thisBlockBox[4] + y);
                                    positions.add((float) thisBlockBox[5] + z);
                                    positions.add((float) thisBlockBox[3] + x);
                                    positions.add((float) thisBlockBox[4] + y);
                                    positions.add((float) thisBlockBox[2] + z);

                                    //top
                                    if (y + 1 < 128) {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x, y + 1, z)], torchLightData[posToIndex(x, y + 1, z)]);
                                    } else {
                                        lightValue = maxLight;
                                    }
                                    lightValue = convertLight(lightValue / maxLight);
                                    //top
                                    for (int i = 0; i < 12; i++) {
                                        light.add(lightValue);
                                    }

                                    //top
                                    indices.add(indicesCount);
                                    indices.add(1 + indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(3 + indicesCount);
                                    indicesCount += 4;

                                    // 0, 1, 2, 3, 4, 5
                                    //-x,-y,-z, x, y, z
                                    // 0, 0, 0, 1, 1, 1

                                    // 0, 1,  2, 3
                                    //-x,+x, -y,+y

                                    textureWorker = getTopTexturePoints(thisBlock);
                                    //top
                                    textureCoord.add(textureWorker[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord.add(textureWorker[2] + ((1 - (float) thisBlockBox[0]) / 32f));
                                    textureCoord.add(textureWorker[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord.add(textureWorker[2] + ((1 - (float) thisBlockBox[0]) / 32f));
                                    textureCoord.add(textureWorker[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord.add(textureWorker[3] - (((float) thisBlockBox[3]) / 32f));
                                    textureCoord.add(textureWorker[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord.add(textureWorker[3] - (((float) thisBlockBox[3]) / 32f));


                                    //bottom
                                    positions.add((float) thisBlockBox[0] + x);
                                    positions.add((float) thisBlockBox[1] + y);
                                    positions.add((float) thisBlockBox[5] + z);
                                    positions.add((float) thisBlockBox[0] + x);
                                    positions.add((float) thisBlockBox[1] + y);
                                    positions.add((float) thisBlockBox[2] + z);
                                    positions.add((float) thisBlockBox[3] + x);
                                    positions.add((float) thisBlockBox[1] + y);
                                    positions.add((float) thisBlockBox[2] + z);
                                    positions.add((float) thisBlockBox[3] + x);
                                    positions.add((float) thisBlockBox[1] + y);
                                    positions.add((float) thisBlockBox[5] + z);

                                    //bottom
                                    if (y - 1 > 0) {
                                        lightValue = calculateBlockLight(chunkLightLevel, naturalLightData[posToIndex(x, y - 1, z)], torchLightData[posToIndex(x, y - 1, z)]);
                                    } else {
                                        lightValue = 0;
                                    }
                                    lightValue = convertLight(lightValue / maxLight);
                                    //bottom
                                    for (int i = 0; i < 12; i++) {
                                        light.add(lightValue);
                                    }

                                    //bottom
                                    indices.add(indicesCount);
                                    indices.add(1 + indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(indicesCount);
                                    indices.add(2 + indicesCount);
                                    indices.add(3 + indicesCount);
                                    indicesCount += 4;


                                    // 0, 1, 2, 3, 4, 5
                                    //-x,-y,-z, x, y, z
                                    // 0, 0, 0, 1, 1, 1

                                    // 0, 1,  2, 3
                                    //-x,+x, -y,+y

                                    textureWorker = getBottomTexturePoints(thisBlock);
                                    //bottom
                                    textureCoord.add(textureWorker[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord.add(textureWorker[2] + ((1 - (float) thisBlockBox[0]) / 32f));
                                    textureCoord.add(textureWorker[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord.add(textureWorker[2] + ((1 - (float) thisBlockBox[0]) / 32f));
                                    textureCoord.add(textureWorker[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord.add(textureWorker[3] - (((float) thisBlockBox[3]) / 32f));
                                    textureCoord.add(textureWorker[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord.add(textureWorker[3] - (((float) thisBlockBox[3]) / 32f));
                                }
                            }
                        }
                        //todo: ------------------------------------------------------------------------------------------------=-=-=-=
                    }
                }
            }


            /*
            long endTime = System.nanoTime();
            long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
            double seconds = (double) duration / 1_000_000_000.0;
            System.out.println("This took: " + seconds + " seconds to generate chunk mesh");

             */

            ChunkMeshDataObject newChunkData = new ChunkMeshDataObject();

            newChunkData.chunkX = chunkX;
            newChunkData.chunkZ = chunkZ;
            newChunkData.yHeight = yHeight;


            //Thread cache
            int workerCounter = 0;

            if (!positions.isEmpty()) {

                //convert all ArrayLists<>() into primitive[]
                float[] positionsArray = new float[positions.size()];
                for (Float data : positions) {
                    //auto casted from Float to float
                    positionsArray[workerCounter] = data;
                    workerCounter++;
                }

                workerCounter = 0;

                float[] lightArray = new float[light.size()];
                for (Float data : light) {
                    //auto casted from Float to float
                    lightArray[workerCounter] = data;
                    workerCounter++;
                }

                workerCounter = 0;

                int[] indicesArray = new int[indices.size()];
                for (Integer data : indices) {
                    //auto casted from Integer to int
                    indicesArray[workerCounter] = data;
                    workerCounter++;
                }

                workerCounter = 0;

                float[] textureCoordArray = new float[textureCoord.size()];
                for (Float data : textureCoord) {
                    //auto casted from Float to float
                    textureCoordArray[workerCounter] = data;
                    workerCounter++;
                }


                //pass data to container object
                newChunkData.positionsArray = positionsArray;
                newChunkData.lightArray = lightArray;
                newChunkData.indicesArray = indicesArray;
                newChunkData.textureCoordArray = textureCoordArray;
            } else {
                //inform the container object that this chunk is null for this part of it
                newChunkData.normalMeshIsNull = true;
            }

            workerCounter = 0;

            if (!liquidPositions.isEmpty()) {

                float[] liquidPositionsArray = new float[liquidPositions.size()];
                for (Float data : liquidPositions) {
                    //auto casted from Float to float
                    liquidPositionsArray[workerCounter] = data;
                    workerCounter++;
                }

                workerCounter = 0;

                float[] liquidLightArray = new float[liquidLight.size()];
                for (Float data : liquidLight) {
                    //auto casted from Float to float
                    liquidLightArray[workerCounter] = data;
                    workerCounter++;
                }

                workerCounter = 0;

                int[] liquidIndicesArray = new int[liquidIndices.size()];
                for (Integer data : liquidIndices) {
                    //auto casted from Integer to int
                    liquidIndicesArray[workerCounter] = data;
                    workerCounter++;
                }

                workerCounter = 0;

                float[] liquidTextureCoordArray = new float[liquidTextureCoord.size()];
                for (Float data : liquidTextureCoord) {
                    //auto casted from Float to float
                    liquidTextureCoordArray[workerCounter] = data;
                    workerCounter++;
                }

                //pass data to container object
                newChunkData.liquidPositionsArray = liquidPositionsArray;
                newChunkData.liquidLightArray = liquidLightArray;
                newChunkData.liquidIndicesArray = liquidIndicesArray;
                newChunkData.liquidTextureCoordArray = liquidTextureCoordArray;
            } else {
                //inform the container object that this chunk is null for this part of it
                newChunkData.liquidMeshIsNull = true;
            }

            workerCounter = 0;

            if (!allFacesPositions.isEmpty()) {

                float[] allFacesPositionsArray = new float[allFacesPositions.size()];
                for (Float data : allFacesPositions) {
                    //auto casted from Float to float
                    allFacesPositionsArray[workerCounter] = data;
                    workerCounter++;
                }

                workerCounter = 0;

                float[] allFacesLightArray = new float[allFacesLight.size()];
                for (Float data : allFacesLight) {
                    //auto casted from Float to float
                    allFacesLightArray[workerCounter] = data;
                    workerCounter++;
                }

                workerCounter = 0;

                int[] allFacesIndicesArray = new int[allFacesIndices.size()];
                for (Integer data : allFacesIndices) {
                    //auto casted from Integer to int
                    allFacesIndicesArray[workerCounter] = data;
                    workerCounter++;
                }

                workerCounter = 0;

                float[] allFacesTextureCoordArray = new float[allFacesTextureCoord.size()];
                for (Float data : allFacesTextureCoord) {
                    //auto casted from Float to float
                    allFacesTextureCoordArray[workerCounter] = data;
                    workerCounter++;
                }

                //pass data to container object
                newChunkData.allFacesPositionsArray = allFacesPositionsArray;
                newChunkData.allFacesLightArray = allFacesLightArray;
                newChunkData.allFacesIndicesArray = allFacesIndicesArray;
                newChunkData.allFacesTextureCoordArray = allFacesTextureCoordArray;
            } else {
                //inform the container object that this chunk is null for this part of it
                newChunkData.allFacesMeshIsNull = true;
            }


            //clear linked lists
            positions.clear();
            textureCoord.clear();
            indices.clear();
            light.clear();
            liquidPositions.clear();
            liquidTextureCoord.clear();
            liquidIndices.clear();
            liquidLight.clear();
            allFacesPositions.clear();
            allFacesTextureCoord.clear();
            allFacesIndices.clear();
            allFacesLight.clear();


            //finally add it into the queue to be popped
            addToChunkMeshQueue(new Vector3i(chunkX,chunkZ,yHeight), newChunkData);


            //solved - stop looping
            solved = true;
        }
    }

    private static byte calculateBlockLight(byte chunkLightLevel, byte naturalLightLocal, byte torchLight){
        byte naturalLightOfBlock = naturalLightLocal;

        if (naturalLightOfBlock > chunkLightLevel){
            naturalLightOfBlock = chunkLightLevel;
        }

        if (naturalLightOfBlock > torchLight){
            return naturalLightOfBlock;
        } else {
            return torchLight;
        }
    }

    private static int getNeighborBlock(ChunkObject neighborChunk, int x, int y, int z){
        if (neighborChunk == null){
            return 0;
        }
        if (neighborChunk.block == null){
            return 0;
        }
        return neighborChunk.block[posToIndex(x,y,z)];
    }

    private static byte getNeighborLight(ChunkObject neighborChunk, int x, int y, int z){
        if (neighborChunk == null){
            return 0;
        }
        if (neighborChunk.block == null){
            return 0;
        }

        int index = posToIndex(x, y, z);

        byte naturalLightOfBlock = neighborChunk.naturalLight[index];

        byte currentGlobalLightLevel = getCurrentGlobalLightLevel();

        if (naturalLightOfBlock > currentGlobalLightLevel){
            naturalLightOfBlock = currentGlobalLightLevel;
        }

        byte torchLight = neighborChunk.torchLight[index];

        if (naturalLightOfBlock > torchLight){
            return naturalLightOfBlock;
        } else {
            return torchLight;
        }
    }

    private static float convertLight(float lightByte){
        return (float) Math.pow(Math.pow(lightByte, 1.5), 1.5);
    }

    //this is an internal duplicate specific to this thread
    //private final static int xMax = 16;
    //private final static int yMax = 128;
    //private final static int length = xMax * yMax; //2048
    private static int posToIndex( int x, int y, int z ) {
        return (z * 2048) + (y * 16) + x;
    }

}