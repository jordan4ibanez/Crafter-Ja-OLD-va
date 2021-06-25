package game.chunk;

import game.blocks.BlockDefinition;
import game.blocks.BlockShape;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.concurrent.ConcurrentLinkedDeque;

import static engine.Window.windowShouldClose;
import static game.chunk.Chunk.*;
import static game.chunk.ChunkMeshGenerationHandler.addToChunkMeshQueue;
import static game.light.Light.getCurrentGlobalLightLevel;

public class ChunkMeshGenerator implements Runnable{
    //DO NOT CHANGE THE DATA CONTAINER
    private static final ConcurrentLinkedDeque<Vector3i> generationQueue = new ConcurrentLinkedDeque<>();

    //holds BlockDefinition data - on this thread
    private static BlockDefinition[] blockIDs;

    //holds the blockshape data - on this thread
    private static BlockShape[] blockShapeMap;


    public static void passChunkMeshThreadData(BlockDefinition[] newBlockIDs, BlockShape[] newBlockShapeMap){
        //remove pointer data
        blockIDs = newBlockIDs.clone();
        blockShapeMap = newBlockShapeMap.clone();
    }

    public void run() {
        while (!windowShouldClose()) {
            pollQueue();
        }
    }

    public static void generateChunkMesh(int chunkX, int chunkZ, int yHeight) {
        if (!generationQueue.contains(new Vector3i(chunkX,yHeight, chunkZ))) {
            generationQueue.add(new Vector3i(chunkX, yHeight, chunkZ));
        }
    }

    public static void instantGeneration(int chunkX, int chunkZ, int yHeight){
        //replace the data basically
        generationQueue.remove(new Vector3i(chunkX,yHeight, chunkZ));
        generationQueue.addFirst(new Vector3i(chunkX,yHeight, chunkZ));
    }

    private static void pollQueue() {
        if (!generationQueue.isEmpty()) {

            //long startTime = System.nanoTime();

            Vector3i updateRawData;
            try {
                updateRawData = generationQueue.pop();
            } catch (Exception ignore) {
                return; //don't crash basically
            }

            //don't crash
            if (updateRawData == null) {
                return;
            }

            ChunkObject thisChunk = getChunk(updateRawData.x, updateRawData.z);

            //don't bother if the chunk doesn't exist
            if (thisChunk == null) {
                return;
            }

            //raw data extracted into stack primitives
            final int chunkX = updateRawData.x;
            final int chunkZ = updateRawData.z;
            final int yHeight = updateRawData.y;

            //neighbor chunks
            ChunkObject chunkNeighborXPlus = getChunk(chunkX + 1, chunkZ);
            ChunkObject chunkNeighborXMinus = getChunk(chunkX - 1, chunkZ);
            ChunkObject chunkNeighborZPlus = getChunk(chunkX, chunkZ + 1);
            ChunkObject chunkNeighborZMinus = getChunk(chunkX, chunkZ - 1);

            //normal block mesh data
            final HyperFloatArray positions = new HyperFloatArray();
            final HyperFloatArray textureCoord = new HyperFloatArray();
            final HyperIntArray indices = new HyperIntArray();
            final HyperFloatArray light = new HyperFloatArray();
            int indicesCount = 0;

            //liquid block mesh data
            final HyperFloatArray liquidPositions = new HyperFloatArray();
            final HyperFloatArray liquidTextureCoord = new HyperFloatArray();
            final HyperIntArray liquidIndices = new HyperIntArray();
            final HyperFloatArray liquidLight = new HyperFloatArray();
            int liquidIndicesCount = 0;


            //allFaces block mesh data
            final HyperFloatArray allFacesPositions = new HyperFloatArray();
            final HyperFloatArray allFacesTextureCoord = new HyperFloatArray();
            final HyperIntArray allFacesIndices = new HyperIntArray();
            final HyperFloatArray allFacesLight = new HyperFloatArray();
            int allFacesIndicesCount = 0;

            //current global light level
            byte chunkLightLevel = getCurrentGlobalLightLevel();

            //reduces lookup time
            byte[] blockData = thisChunk.block;
            byte[] rotationData = thisChunk.rotation;
            byte[] lightData = thisChunk.light;

            byte thisBlock;
            byte thisBlockDrawType;
            byte thisRotation;

            //loop through ystack
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = yHeight * 16; y < (yHeight + 1) * 16; y++) {

                        thisBlock = blockData[posToIndex(x, y, z)];

                        //only if not air
                        if (thisBlock > 0) {

                            //only need to look this data up if it's not air
                            thisBlockDrawType = getBlockDrawType(thisBlock);
                            thisRotation = rotationData[posToIndex(x, y, z)];

                            switch (thisBlockDrawType) {

                                //normal
                                case 1 -> indicesCount = calculateNormal(x, y, z, thisBlock, thisRotation, indicesCount, blockData, positions, light, indices, textureCoord, chunkNeighborZPlus, chunkNeighborZMinus, chunkNeighborXMinus, chunkNeighborXPlus, chunkLightLevel, lightData);

                                //allfaces
                                case 4 -> allFacesIndicesCount = calculateAllFaces(x, y, z, thisBlock, thisRotation, allFacesIndicesCount, allFacesPositions, allFacesLight, allFacesIndices, allFacesTextureCoord, chunkNeighborZPlus, chunkNeighborZMinus, chunkNeighborXMinus, chunkNeighborXPlus, chunkLightLevel, lightData);

                                //torch
                                case 7 -> indicesCount = calculateTorchLike(x, y, z, thisBlock, thisRotation, indicesCount, positions, light, indices, textureCoord, chunkNeighborZPlus, chunkNeighborZMinus, chunkNeighborXMinus, chunkNeighborXPlus, chunkLightLevel, lightData);

                                //liquid
                                case 8 -> liquidIndicesCount = calculateLiquids(x, y, z, thisBlock, thisRotation, liquidIndicesCount, blockData, chunkNeighborZPlus, chunkNeighborZMinus, chunkNeighborXMinus, chunkNeighborXPlus, liquidPositions, liquidLight, liquidIndices, liquidTextureCoord, chunkLightLevel, lightData);

                                //blockbox
                                default -> indicesCount = calculateBlockBox(x, y, z, thisBlock, thisRotation, indicesCount, positions, light, indices, textureCoord, chunkNeighborZPlus, chunkNeighborZMinus, chunkNeighborXMinus, chunkNeighborXPlus, chunkLightLevel, lightData);
                            }
                        }
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


            if (positions.size() > 0) {
                //pass data to container object
                newChunkData.positionsArray    = positions.values();
                newChunkData.lightArray        = light.values();
                newChunkData.indicesArray      = indices.values();
                newChunkData.textureCoordArray = textureCoord.values();
            } else {
                //inform the container object that this chunk is null for this part of it
                newChunkData.normalMeshIsNull = true;
            }

            if (liquidPositions.size() > 0){
                newChunkData.liquidPositionsArray    = liquidPositions.values();
                newChunkData.liquidLightArray        = liquidLight.values();
                newChunkData.liquidIndicesArray      = liquidIndices.values();
                newChunkData.liquidTextureCoordArray = liquidTextureCoord.values();
            } else {
                //inform the container object that this chunk is null for this part of it
                newChunkData.liquidMeshIsNull = true;
            }

            if (allFacesPositions.size() > 0) {
                //pass data to container object
                newChunkData.allFacesPositionsArray = allFacesPositions.values();
                newChunkData.allFacesLightArray = allFacesLight.values();
                newChunkData.allFacesIndicesArray = allFacesIndices.values();
                newChunkData.allFacesTextureCoordArray = allFacesTextureCoord.values();
            } else {
                //inform the container object that this chunk is null for this part of it
                newChunkData.allFacesMeshIsNull = true;
            }


            //clear data so GC doesn't have to
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
            addToChunkMeshQueue(newChunkData);
        }
    }


    private static int calculateBlockBox(int x, int y, int z, byte thisBlock, byte thisRotation, int indicesCount, HyperFloatArray positions, HyperFloatArray light, HyperIntArray indices, HyperFloatArray textureCoord, ChunkObject chunkNeighborZPlus, ChunkObject chunkNeighborZMinus, ChunkObject chunkNeighborXMinus, ChunkObject chunkNeighborXPlus, byte chunkLightLevel, byte[] lightData){
        for (float[] thisBlockBox : getBlockShape(thisBlock, thisRotation)) {
            //front
            positions.pack(thisBlockBox[3] + x, thisBlockBox[4] + y, thisBlockBox[5] + z, thisBlockBox[0] + x, thisBlockBox[4] + y, thisBlockBox[5] + z, thisBlockBox[0] + x, thisBlockBox[1] + y, thisBlockBox[5] + z, thisBlockBox[3] + x, thisBlockBox[1] + y, thisBlockBox[5] + z);

            //front
            float lightValue;
            if (z + 1 > 15) {
                lightValue = getNeighborLight(chunkNeighborZPlus, x, y, 0);
            } else {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z + 1)]);
            }

            lightValue = convertLight(lightValue / (float) 15);

            //front
            light.pack(lightValue);


            //front
            indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);
            indicesCount += 4;


            float[] textureWorker = getFrontTexturePoints(thisBlock, thisRotation);

            //front
            textureCoord.pack(textureWorker[1] - ((1 - thisBlockBox[3]) / 32f), textureWorker[2] + ((1 - thisBlockBox[4]) / 32f), textureWorker[0] - ((0 - thisBlockBox[0]) / 32f), textureWorker[2] + ((1 - thisBlockBox[4]) / 32f), textureWorker[0] - ((0 - thisBlockBox[0]) / 32f), textureWorker[3] - (thisBlockBox[1] / 32f), textureWorker[1] - ((1 - thisBlockBox[3]) / 32f), textureWorker[3] - (thisBlockBox[1] / 32f));

            //back
            positions.pack(thisBlockBox[0] + x, thisBlockBox[4] + y, thisBlockBox[2] + z, thisBlockBox[3] + x, thisBlockBox[4] + y, thisBlockBox[2] + z, thisBlockBox[3] + x, thisBlockBox[1] + y, thisBlockBox[2] + z, thisBlockBox[0] + x, thisBlockBox[1] + y, thisBlockBox[2] + z);


            //back
            if (z - 1 < 0) {
                lightValue = getNeighborLight(chunkNeighborZMinus, x, y, 15);
            } else {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z - 1)]);
            }


            lightValue = convertLight(lightValue / (float) 15);
            //back
            light.pack(lightValue);

            //back
            indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

            indicesCount += 4;

            textureWorker = getBackTexturePoints(thisBlock, thisRotation);

            //back
            textureCoord.pack(textureWorker[1] - ((1 - thisBlockBox[0]) / 32f), textureWorker[2] + ((1 - thisBlockBox[4]) / 32f), textureWorker[0] - ((0 - thisBlockBox[3]) / 32f), textureWorker[2] + ((1 - thisBlockBox[4]) / 32f), textureWorker[0] - ((0 - thisBlockBox[3]) / 32f), textureWorker[3] - (thisBlockBox[1] / 32f), textureWorker[1] - ((1 - thisBlockBox[0]) / 32f), textureWorker[3] - (thisBlockBox[1] / 32f));


            //right
            positions.pack(thisBlockBox[3] + x, thisBlockBox[4] + y, thisBlockBox[2] + z, thisBlockBox[3] + x, thisBlockBox[4] + y, thisBlockBox[5] + z, thisBlockBox[3] + x, thisBlockBox[1] + y, thisBlockBox[5] + z, thisBlockBox[3] + x, thisBlockBox[1] + y, thisBlockBox[2] + z);

            //right
            if (x + 1 > 15) {
                lightValue = getNeighborLight(chunkNeighborXPlus, 0, y, z);
            } else {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x + 1, y, z)]);
            }
            lightValue = convertLight(lightValue / (float) 15);

            //right
            light.pack(lightValue);

            //right
            indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

            indicesCount += 4;


            textureWorker = getRightTexturePoints(thisBlock, thisRotation);
            //right
            textureCoord.pack(textureWorker[1] - ((1f - thisBlockBox[2]) / 32f), textureWorker[2] + ((1f - thisBlockBox[4]) / 32f), textureWorker[0] - ((0f - thisBlockBox[5]) / 32f), textureWorker[2] + ((1f - thisBlockBox[4]) / 32f), textureWorker[0] - ((0f - thisBlockBox[5]) / 32f), textureWorker[3] - ((thisBlockBox[1]) / 32f), textureWorker[1] - ((1f - thisBlockBox[2]) / 32f), textureWorker[3] - ((thisBlockBox[1]) / 32f));


            //left
            positions.pack(thisBlockBox[0] + x, thisBlockBox[4] + y, thisBlockBox[5] + z, thisBlockBox[0] + x, thisBlockBox[4] + y, thisBlockBox[2] + z, thisBlockBox[0] + x, thisBlockBox[1] + y, thisBlockBox[2] + z, thisBlockBox[0] + x, thisBlockBox[1] + y, thisBlockBox[5] + z);


            //left
            if (x - 1 < 0) {
                lightValue = getNeighborLight(chunkNeighborXMinus, 15, y, z);
            } else {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x - 1, y, z)]);
            }
            lightValue = convertLight(lightValue / (float) 15);

            //left
            light.pack(lightValue);

            //left
            indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

            indicesCount += 4;

            textureWorker = getLeftTexturePoints(thisBlock, thisRotation);
            //left
            textureCoord.pack(textureWorker[1] - ((1f - thisBlockBox[5]) / 32f), textureWorker[2] + ((1f - thisBlockBox[4]) / 32f), textureWorker[0] - ((0f - thisBlockBox[2]) / 32f), textureWorker[2] + ((1f - thisBlockBox[4]) / 32f), textureWorker[0] - ((0f - thisBlockBox[2]) / 32f), textureWorker[3] - ((thisBlockBox[1]) / 32f), textureWorker[1] - ((1f - thisBlockBox[5]) / 32f), textureWorker[3] - ((thisBlockBox[1]) / 32f));


            //top
            positions.pack(thisBlockBox[0] + x, thisBlockBox[4] + y, thisBlockBox[2] + z, thisBlockBox[0] + x, thisBlockBox[4] + y, thisBlockBox[5] + z, thisBlockBox[3] + x, thisBlockBox[4] + y, thisBlockBox[5] + z, thisBlockBox[3] + x, thisBlockBox[4] + y, thisBlockBox[2] + z);

            //top
            if (y + 1 < 128) {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y + 1, z)]);
            } else {
                lightValue = (float) 15;
            }
            lightValue = convertLight(lightValue / (float) 15);

            //top
            light.pack(lightValue);

            //top
            indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

            indicesCount += 4;


            textureWorker = getTopTexturePoints(thisBlock);
            //top
            textureCoord.pack(textureWorker[1] - ((1f - thisBlockBox[5]) / 32f), textureWorker[2] + ((1f - thisBlockBox[0]) / 32f), textureWorker[0] - ((0f - thisBlockBox[2]) / 32f), textureWorker[2] + ((1f - thisBlockBox[0]) / 32f), textureWorker[0] - ((0f - thisBlockBox[2]) / 32f), textureWorker[3] - ((thisBlockBox[3]) / 32f), textureWorker[1] - ((1f - thisBlockBox[5]) / 32f), textureWorker[3] - ((thisBlockBox[3]) / 32f));


            //bottom
            positions.pack(thisBlockBox[0] + x, thisBlockBox[1] + y, thisBlockBox[5] + z, thisBlockBox[0] + x, thisBlockBox[1] + y, thisBlockBox[2] + z, thisBlockBox[3] + x, thisBlockBox[1] + y, thisBlockBox[2] + z, thisBlockBox[3] + x, thisBlockBox[1] + y, thisBlockBox[5] + z);

            //bottom
            if (y - 1 > 0) {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y - 1, z)]);
            } else {
                lightValue = 0;
            }
            lightValue = convertLight(lightValue / (float) 15);

            //bottom
            light.pack(lightValue);

            //bottom
            indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

            indicesCount += 4;


            textureWorker = getBottomTexturePoints(thisBlock);
            //bottom
            textureCoord.pack(textureWorker[1] - ((1f - thisBlockBox[5]) / 32f), textureWorker[2] + ((1f - thisBlockBox[0]) / 32f), textureWorker[0] - ((0f - thisBlockBox[2]) / 32f), textureWorker[2] + ((1f - thisBlockBox[0]) / 32f), textureWorker[0] - ((0f - thisBlockBox[2]) / 32f), textureWorker[3] - (((thisBlockBox[3]) / 32f)), textureWorker[1] - ((1f - thisBlockBox[5]) / 32f), textureWorker[3] - ((thisBlockBox[3]) / 32f));
        }

        return indicesCount;
    }

    private static int calculateLiquids(int x, int y, int z, byte thisBlock, byte thisRotation, int liquidIndicesCount, byte[] blockData, ChunkObject chunkNeighborZPlus, ChunkObject chunkNeighborZMinus, ChunkObject chunkNeighborXMinus, ChunkObject chunkNeighborXPlus, HyperFloatArray liquidPositions, HyperFloatArray liquidLight, HyperIntArray liquidIndices, HyperFloatArray liquidTextureCoord, byte chunkLightLevel, byte[] lightData){
        byte neighborBlock;

        if (z + 1 > 15) {
            neighborBlock = getNeighborBlock(chunkNeighborZPlus, x, y, 0);
        } else {
            neighborBlock = blockData[posToIndex(x, y, z + 1)];
        }
        byte neighborDrawtype = getBlockDrawType(neighborBlock);

        float lightValue;
        float[] textureWorker;
        if ((neighborDrawtype == 0 || neighborDrawtype > 1) && neighborDrawtype != 8) {
            //front
            liquidPositions.pack(1f + x, 1f + y, 1f + z, 0f + x, 1f + y, 1f + z, 0f + x, 0f + y, 1f + z, 1f + x, 0f + y, 1f + z);

            //front
            if (z + 1 > 15) {
                lightValue = getNeighborLight(chunkNeighborZPlus, x, y, 0);
            } else {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z + 1)]);
            }

            lightValue = convertLight(lightValue / (float) 15);

            //front
            liquidLight.pack(lightValue);


            //front
            liquidIndices.pack(liquidIndicesCount, 1 + liquidIndicesCount, 2 + liquidIndicesCount, liquidIndicesCount, 2 + liquidIndicesCount, 3 + liquidIndicesCount);

            liquidIndicesCount += 4;

            textureWorker = getFrontTexturePoints(thisBlock, thisRotation);
            //front
            liquidTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]
            );
        }


        if (z - 1 < 0) {
            neighborBlock = getNeighborBlock(chunkNeighborZMinus, x, y, 15);
        } else {
            neighborBlock = blockData[posToIndex(x, y, z - 1)];
        }
        neighborDrawtype = getBlockDrawType(neighborBlock);

        if ((neighborDrawtype == 0 || neighborDrawtype > 1) && neighborDrawtype != 8) {
            //back
            liquidPositions.pack(0f + x, 1f + y, 0f + z, 1f + x, 1f + y, 0f + z, 1f + x, 0f + y, 0f + z, 0f + x, 0f + y, 0f + z);

            //back
            if (z - 1 < 0) {
                lightValue = getNeighborLight(chunkNeighborZMinus, x, y, 15);
            } else {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z - 1)]);
            }

            lightValue = convertLight(lightValue / (float) 15);

            //back
            liquidLight.pack(lightValue);

            //back
            liquidIndices.pack(liquidIndicesCount, 1 + liquidIndicesCount, 2 + liquidIndicesCount, liquidIndicesCount, 2 + liquidIndicesCount, 3 + liquidIndicesCount);

            liquidIndicesCount += 4;

            textureWorker = getBackTexturePoints(thisBlock, thisRotation);
            //back
            liquidTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        if (x + 1 > 15) {
            neighborBlock = getNeighborBlock(chunkNeighborXPlus, 0, y, z);
        } else {
            neighborBlock = blockData[posToIndex(x + 1, y, z)];
        }
        neighborDrawtype = getBlockDrawType(neighborBlock);

        if ((neighborDrawtype == 0 || neighborDrawtype > 1) && neighborDrawtype != 8) {
            //right
            liquidPositions.pack(1f + x, 1f + y, 0f + z, 1f + x, 1f + y, 1f + z, 1f + x, 0f + y, 1f + z, 1f + x, 0f + y, 0f + z);

            //right

            if (x + 1 > 15) {
                lightValue = getNeighborLight(chunkNeighborXPlus, 0, y, z);
            } else {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x + 1, y, z)]);
            }

            lightValue = convertLight(lightValue / (float) 15);

            //right
            liquidLight.pack(lightValue);

            //right
            liquidIndices.pack(liquidIndicesCount, 1 + liquidIndicesCount, 2 + liquidIndicesCount, liquidIndicesCount, 2 + liquidIndicesCount, 3 + liquidIndicesCount);

            liquidIndicesCount += 4;

            textureWorker = getRightTexturePoints(thisBlock, thisRotation);
            //right
            liquidTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        if (x - 1 < 0) {
            neighborBlock = getNeighborBlock(chunkNeighborXMinus, 15, y, z);
        } else {
            neighborBlock = blockData[posToIndex(x - 1, y, z)];
        }
        neighborDrawtype = getBlockDrawType(neighborBlock);

        if ((neighborDrawtype == 0 || neighborDrawtype > 1) && neighborDrawtype != 8) {
            //left
            liquidPositions.pack(0f + x, 1f + y, 1f + z, 0f + x, 1f + y, 0f + z, 0f + x, 0f + y, 0f + z, 0f + x, 0f + y, 1f + z);

            //left

            if (x - 1 < 0) {
                lightValue = getNeighborLight(chunkNeighborXMinus, 15, y, z);
            } else {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x - 1, y, z)]);
            }

            lightValue = convertLight(lightValue / (float) 15);
            //left

            liquidLight.pack(lightValue);

            //left
            liquidIndices.pack(liquidIndicesCount, 1 + liquidIndicesCount, 2 + liquidIndicesCount, liquidIndicesCount, 2 + liquidIndicesCount, 3 + liquidIndicesCount);

            liquidIndicesCount += 4;

            textureWorker = getLeftTexturePoints(thisBlock, thisRotation);
            //left
            liquidTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        //y doesn't need a check since it has no neighbors
        if (y + 1 < 128) {
            neighborBlock = blockData[posToIndex(x, y + 1, z)];
        }
        neighborDrawtype = getBlockDrawType(neighborBlock);

        if (y == 127 || ((neighborDrawtype == 0 || neighborDrawtype > 1) && neighborDrawtype != 8)) {
            //top
            liquidPositions.pack(0f + x, 1f + y, 0f + z, 0f + x, 1f + y, 1f + z, 1f + x, 1f + y, 1f + z, 1f + x, 1f + y, 0f + z);

            //top

            //y doesn't need a check since it has no neighbors
            if (y + 1 < 128) {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y + 1, z)]);
            } else {
                lightValue = (float) 15;
            }

            lightValue = convertLight(lightValue / (float) 15);

            //top
            liquidLight.pack(lightValue);

            //top
            liquidIndices.pack(liquidIndicesCount, 1 + liquidIndicesCount, 2 + liquidIndicesCount, liquidIndicesCount, 2 + liquidIndicesCount, 3 + liquidIndicesCount);

            liquidIndicesCount += 4;

            textureWorker = getTopTexturePoints(thisBlock);
            //top
            liquidTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        //doesn't need a neighbor chunk, chunks are 2D
        if (y - 1 > 0) {
            neighborBlock = blockData[posToIndex(x, y - 1, z)];
        }
        neighborDrawtype = getBlockDrawType(neighborBlock);

        //don't render bottom of world
        if (y != 0 && ((neighborDrawtype == 0 || neighborDrawtype > 1) && neighborDrawtype != 8)) {
            //bottom
            liquidPositions.pack(0f + x, 0f + y, 1f + z, 0f + x, 0f + y, 0f + z, 1f + x, 0f + y, 0f + z, 1f + x, 0f + y, 1f + z);

            //bottom
            //doesn't need a neighbor chunk, chunks are 2D
            if (y - 1 > 0) {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y - 1, z)]);
            } else {
                lightValue = 0;
            }

            lightValue = convertLight(lightValue / (float) 15);
            //bottom

            liquidLight.pack(lightValue);


            //bottom
            liquidIndices.pack(liquidIndicesCount, 1 + liquidIndicesCount, 2 + liquidIndicesCount, liquidIndicesCount, 2 + liquidIndicesCount, 3 + liquidIndicesCount);

            liquidIndicesCount += 4;

            textureWorker = getBottomTexturePoints(thisBlock);
            //bottom
            liquidTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        return liquidIndicesCount;
    }

    private static int calculateNormal(int x, int y, int z, byte thisBlock, byte thisRotation, int indicesCount, byte[] blockData, HyperFloatArray positions, HyperFloatArray light, HyperIntArray indices, HyperFloatArray textureCoord, ChunkObject chunkNeighborZPlus, ChunkObject chunkNeighborZMinus, ChunkObject chunkNeighborXMinus, ChunkObject chunkNeighborXPlus, byte chunkLightLevel, byte[] lightData){

        byte neighborBlock;
        if (z + 1 > 15) {
            neighborBlock = getNeighborBlock(chunkNeighborZPlus, x, y, 0);
        } else {
            neighborBlock = blockData[posToIndex(x, y, z + 1)];
        }

        float lightValue;
        float[] textureWorker;
        if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
            //front

            positions.pack(1f + x, 1f + y, 1f + z, 0f + x, 1f + y, 1f + z, 0f + x, 0f + y, 1f + z, 1f + x, 0f + y, 1f + z);

            //front
            if (z + 1 > 15) {
                lightValue = getNeighborLight(chunkNeighborZPlus, x, y, 0);
            } else {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z + 1)]);
            }

            lightValue = convertLight(lightValue / (float) 15);

            //front
            light.pack(lightValue);


            //front
            indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

            indicesCount += 4;

            textureWorker = getFrontTexturePoints(thisBlock, thisRotation);
            //front
            textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        if (z - 1 < 0) {
            neighborBlock = getNeighborBlock(chunkNeighborZMinus, x, y, 15);
        } else {
            neighborBlock = blockData[posToIndex(x, y, z - 1)];
        }

        if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
            //back
            positions.pack(0f + x, 1f + y, 0f + z, 1f + x, 1f + y, 0f + z, 1f + x, 0f + y, 0f + z, 0f + x, 0f + y, 0f + z);

            //back
            if (z - 1 < 0) {
                lightValue = getNeighborLight(chunkNeighborZMinus, x, y, 15);
            } else {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z - 1)]);
            }

            lightValue = convertLight(lightValue / (float) 15);
            //back
            light.pack(lightValue);

            //back
            indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

            indicesCount += 4;

            textureWorker = getBackTexturePoints(thisBlock, thisRotation);
            //back
            textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        if (x + 1 > 15) {
            neighborBlock = getNeighborBlock(chunkNeighborXPlus, 0, y, z);
        } else {
            neighborBlock = blockData[posToIndex(x + 1, y, z)];
        }

        if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
            //right
            positions.pack(1f + x, 1f + y, 0f + z, 1f + x, 1f + y, 1f + z, 1f + x, 0f + y, 1f + z, 1f + x, 0f + y, 0f + z);

            //right
            if (x + 1 > 15) {
                lightValue = getNeighborLight(chunkNeighborXPlus, 0, y, z);
            } else {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x + 1, y, z)]);
            }

            lightValue = convertLight(lightValue / (float) 15);
            //right
            light.pack(lightValue);

            //right
            indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

            indicesCount += 4;

            textureWorker = getRightTexturePoints(thisBlock, thisRotation);
            //right
            textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        if (x - 1 < 0) {
            neighborBlock = getNeighborBlock(chunkNeighborXMinus, 15, y, z);
        } else {
            neighborBlock = blockData[posToIndex(x - 1, y, z)];
        }

        if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
            //left
            positions.pack(0f + x, 1f + y, 1f + z, 0f + x, 1f + y, 0f + z, 0f + x, 0f + y, 0f + z, 0f + x, 0f + y, 1f + z);

            //left
            if (x - 1 < 0) {
                lightValue = getNeighborLight(chunkNeighborXMinus, 15, y, z);
            } else {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x - 1, y, z)]);
            }

            lightValue = convertLight(lightValue / (float) 15);
            //left
            light.pack(lightValue);

            //left
            indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

            indicesCount += 4;

            textureWorker = getLeftTexturePoints(thisBlock, thisRotation);
            //left
            textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        if (y + 1 < 128) {
            neighborBlock = blockData[posToIndex(x, y + 1, z)];
        }

        if (y == 127 || neighborBlock > -1 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
            //top
            positions.pack(0f + x, 1f + y, 0f + z, 0f + x, 1f + y, 1f + z, 1f + x, 1f + y, 1f + z, 1f + x, 1f + y, 0f + z);

            //top
            if (y + 1 < 128) {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y + 1, z)]);
            } else {
                lightValue = (float) 15;
            }

            lightValue = convertLight(lightValue / (float) 15);

            //top
            light.pack(lightValue);

            //top
            indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

            indicesCount += 4;

            textureWorker = getTopTexturePoints(thisBlock);
            //top
            textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        if (y - 1 > 0) {
            neighborBlock = blockData[posToIndex(x, y - 1, z)];
        }

        if (y != 0 && neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
            //bottom
            positions.pack(0f + x, 0f + y, 1f + z, 0f + x, 0f + y, 0f + z, 1f + x, 0f + y, 0f + z, 1f + x, 0f + y, 1f + z);

            //bottom
            if (y - 1 > 0) {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y - 1, z)]);
            } else {
                lightValue = 0;
            }

            lightValue = convertLight(lightValue / (float) 15);
            //bottom
            light.pack(lightValue);

            //bottom
            indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

            indicesCount += 4;

            textureWorker = getBottomTexturePoints(thisBlock);
            //bottom
            textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        return indicesCount;
    }

    //require 0.125 width
    private static final float largeXZ = 0.5625f; //large
    private static final float smallXZ = 0.4375f; //small

    private static final float largeY = 0.625f;
    private static final float smallY = 0.0f;

    private static final Vector3f trl = new Vector3f(); //top rear left
    private static final Vector3f trr = new Vector3f(); //top rear right
    private static final Vector3f tfl = new Vector3f(); //top front left
    private static final Vector3f tfr = new Vector3f(); //top front right

    private static final Vector3f brl = new Vector3f(); //bottom rear left
    private static final Vector3f brr = new Vector3f(); //bottom rear right - also cold
    private static final Vector3f bfl = new Vector3f(); //bottom front left
    private static final Vector3f bfr = new Vector3f(); //bottom front right

    private static final float pixel = (1f / 32f / 16f);


    private static int calculateTorchLike(int x, int y, int z, byte thisBlock, byte thisRotation, int indicesCount, HyperFloatArray positions, HyperFloatArray light, HyperIntArray indices, HyperFloatArray textureCoord, ChunkObject chunkNeighborZPlus, ChunkObject chunkNeighborZMinus, ChunkObject chunkNeighborXMinus, ChunkObject chunkNeighborXPlus, byte chunkLightLevel, byte[] lightData){
        switch (thisRotation) {
            //+x dir
            case 0 -> {

                //top rear left
                trl.x = smallXZ + 0.3f;
                trl.y = largeY - 0.025f;
                trl.z = smallXZ;

                //top rear right
                trr.x = largeXZ + 0.3f;
                trr.y = largeY + 0.025f;
                trr.z = smallXZ;

                //top front left
                tfl.x = smallXZ + 0.3f;
                tfl.y = largeY - 0.025f;
                tfl.z = largeXZ;

                //top front right
                tfr.x = largeXZ + 0.3f;
                tfr.y = largeY + 0.025f;
                tfr.z = largeXZ;

                //bottom rear left
                brl.x = smallXZ + 0.5f;
                brl.y = smallY - 0.025f;
                brl.z = smallXZ;

                //bottom rear right - also cold
                brr.x = largeXZ + 0.5f;
                brr.y = smallY + 0.025f;
                brr.z = smallXZ;

                //bottom front left
                bfl.x = smallXZ + 0.5f;
                bfl.y = smallY - 0.025f;
                bfl.z = largeXZ;

                //bottom front right
                bfr.x = largeXZ + 0.5f;
                bfr.y = smallY + 0.025f;
                bfr.z = largeXZ;

            }

            //-x dir
            case 1 -> {

                //top rear left
                trl.x = smallXZ - 0.3f;
                trl.y = largeY + 0.025f;
                trl.z = smallXZ;

                //top rear right
                trr.x = largeXZ - 0.3f;
                trr.y = largeY - 0.025f;
                trr.z = smallXZ;

                //top front left
                tfl.x = smallXZ - 0.3f;
                tfl.y = largeY + 0.025f;
                tfl.z = largeXZ;

                //top front right
                tfr.x = largeXZ - 0.3f;
                tfr.y = largeY - 0.025f;
                tfr.z = largeXZ;

                //bottom rear left
                brl.x = smallXZ - 0.5f;
                brl.y = smallY + 0.025f;
                brl.z = smallXZ;

                //bottom rear right - also cold
                brr.x = largeXZ - 0.5f;
                brr.y = smallY - 0.025f;
                brr.z = smallXZ;

                //bottom front left
                bfl.x = smallXZ - 0.5f;
                bfl.y = smallY + 0.025f;
                bfl.z = largeXZ;

                //bottom front right
                bfr.x = largeXZ - 0.5f;
                bfr.y = smallY - 0.025f;
                bfr.z = largeXZ;

            }

            //+z dir
            case 2 -> {

                //top rear left
                trl.x = smallXZ;
                trl.y = largeY - 0.025f;
                trl.z = smallXZ + 0.3f;

                //top rear right
                trr.x = largeXZ;
                trr.y = largeY - 0.025f;
                trr.z = smallXZ + 0.3f;

                //top front left
                tfl.x = smallXZ;
                tfl.y = largeY + 0.025f;
                tfl.z = largeXZ + 0.3f;

                //top front right
                tfr.x = largeXZ;
                tfr.y = largeY + 0.025f;
                tfr.z = largeXZ + 0.3f;

                //bottom rear left
                brl.x = smallXZ;
                brl.y = smallY - 0.025f;
                brl.z = smallXZ + 0.5f;

                //bottom rear right - also cold
                brr.x = largeXZ;
                brr.y = smallY - 0.025f;
                brr.z = smallXZ + 0.5f;

                //bottom front left
                bfl.x = smallXZ;
                bfl.y = smallY + 0.025f;
                bfl.z = largeXZ + 0.5f;

                //bottom front right
                bfr.x = largeXZ;
                bfr.y = smallY + 0.025f;
                bfr.z = largeXZ + 0.5f;

            }

            //-z dir
            case 3 -> {

                //top rear left
                trl.x = smallXZ;
                trl.y = largeY + 0.025f;
                trl.z = smallXZ - 0.3f;

                //top rear right
                trr.x = largeXZ;
                trr.y = largeY + 0.025f;
                trr.z = smallXZ - 0.3f;

                //top front left
                tfl.x = smallXZ;
                tfl.y = largeY - 0.025f;
                tfl.z = largeXZ - 0.3f;

                //top front right
                tfr.x = largeXZ;
                tfr.y = largeY - 0.025f;
                tfr.z = largeXZ - 0.3f;

                //bottom rear left
                brl.x = smallXZ;
                brl.y = smallY + 0.025f;
                brl.z = smallXZ - 0.5f;

                //bottom rear right - also cold
                brr.x = largeXZ;
                brr.y = smallY + 0.025f;
                brr.z = smallXZ - 0.5f;

                //bottom front left
                bfl.x = smallXZ;
                bfl.y = smallY - 0.025f;
                bfl.z = largeXZ - 0.5f;

                //bottom front right
                bfr.x = largeXZ;
                bfr.y = smallY - 0.025f;
                bfr.z = largeXZ - 0.5f;
            }

            //floor
            case 4 -> {
                //top rear left
                trl.x = smallXZ;
                trl.y = largeY;
                trl.z = smallXZ;

                //top rear right
                trr.x = largeXZ;
                trr.y = largeY;
                trr.z = smallXZ;

                //top front left
                tfl.x = smallXZ;
                tfl.y = largeY;
                tfl.z = largeXZ;

                //top front right
                tfr.x = largeXZ;
                tfr.y = largeY;
                tfr.z = largeXZ;


                //bottom rear left
                brl.x = smallXZ;
                brl.y = smallY;
                brl.z = smallXZ;

                //bottom rear right - also cold
                brr.x = largeXZ;
                brr.y = smallY;
                brr.z = smallXZ;

                //bottom front left
                bfl.x = smallXZ;
                bfl.y = smallY;
                bfl.z = largeXZ;

                //bottom front right
                bfr.x = largeXZ;
                bfr.y = smallY;
                bfr.z = largeXZ;
            }
        }

        float[] textureWorker = getFrontTexturePoints(thisBlock, thisRotation);


        //assume 16 pixels wide
        float sizeXLow = textureWorker[0] + (pixel * 7f);
        float sizeXHigh = textureWorker[0] + (pixel * 9f); //duplicates to work from same coordinate (it's easier for me this way)
        float sizeYLow = textureWorker[2] + (pixel * 6f);
        float sizeYHigh = textureWorker[2] + (pixel * 16f);

        float topSizeXLow = textureWorker[0] + (pixel * 7f);
        float topSizeXHigh = textureWorker[0] + (pixel * 9f);
        float topSizeYLow = textureWorker[2] + (pixel * 4f);
        float topSizeYHigh = textureWorker[2] + (pixel * 6f);

        float bottomSizeXLow = textureWorker[0] + (pixel * 7f);
        float bottomSizeXHigh = textureWorker[0] + (pixel * 9f);
        float bottomSizeYLow = textureWorker[2] + (pixel * 2f);
        float bottomSizeYHigh = textureWorker[2] + (pixel * 4f);


        //this is pulled out of normal

        //thisRotation
        //front

        //z is the constant
        //front
        positions.pack(tfr.x + x, tfr.y + y, tfr.z + z, tfl.x + x, tfl.y + y, tfl.z + z, bfl.x + x, bfl.y + y, bfl.z + z, bfr.x + x, bfr.y + y, bfr.z + z);

        //front
        float lightValue;
        if (z + 1 > 15) {
            lightValue = getNeighborLight(chunkNeighborZPlus, x, y, 0);
        } else {
            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z + 1)]);
        }

        lightValue = convertLight(lightValue / (float) 15);

        //front
        light.pack(lightValue);

        //front
        indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

        indicesCount += 4;


        //front
        textureCoord.pack(sizeXHigh, sizeYLow, sizeXLow, sizeYLow, sizeXLow, sizeYHigh, sizeXHigh, sizeYHigh);



        //back

        //z is the constant
        //back
        positions.pack(trl.x + x, trl.y + y, trl.z + z, trr.x + x, trr.y + y, trr.z + z, brr.x + x, brr.y + y, brr.z + z, brl.x + x, brl.y + y, brl.z + z);

        //back

        if (z - 1 < 0) {
            lightValue = getNeighborLight(chunkNeighborZMinus, x, y, 15);
        } else {
            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z - 1)]);
        }

        lightValue = convertLight(lightValue / (float) 15);

        //back
        light.pack(lightValue);

        //back
        indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

        indicesCount += 4;


        //back
        textureCoord.pack(sizeXHigh, sizeYLow, sizeXLow, sizeYLow, sizeXLow, sizeYHigh, sizeXHigh, sizeYHigh);



        //x is the constant
        //right
        positions.pack(trr.x + x, trr.y + y, trr.z + z, tfr.x + x, tfr.y + y, tfr.z + z, bfr.x + x, bfr.y + y, bfr.z + z, brr.x + x, brr.y + y, brr.z + z);

        //right

        if (x + 1 > 15) {
            lightValue = getNeighborLight(chunkNeighborXPlus, 0, y, z);
        } else {
            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x + 1, y, z)]);
        }

        lightValue = convertLight(lightValue / (float) 15);

        //right
        light.pack(lightValue);

        //right
        indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

        indicesCount += 4;

        //right
        textureCoord.pack(sizeXHigh, sizeYLow, sizeXLow, sizeYLow, sizeXLow, sizeYHigh, sizeXHigh, sizeYHigh);




        //x is the constant
        //left
        positions.pack(tfl.x + x, tfl.y + y, tfl.z + z, trl.x + x, trl.y + y, trl.z + z, brl.x + x, brl.y + y, brl.z + z, bfl.x + x, bfl.y + y, bfl.z + z);

        //left
        if (x - 1 < 0) {
            lightValue = getNeighborLight(chunkNeighborXMinus, 15, y, z);
        } else {
            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x - 1, y, z)]);
        }

        lightValue = convertLight(lightValue / (float) 15);

        //left
        light.pack(lightValue);

        //left
        indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

        indicesCount += 4;

        //left
        textureCoord.pack(sizeXHigh, sizeYLow, sizeXLow, sizeYLow, sizeXLow, sizeYHigh, sizeXHigh, sizeYHigh);


        //y is constant
        //top
        positions.pack(trl.x + x, trl.y + y, trl.z + z, tfl.x + x, tfl.y + y, tfl.z + z, tfr.x + x, tfr.y + y, tfr.z + z, trr.x + x, trr.y + y, trr.z + z);

        //top
        if (y + 1 < 128) {
            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y + 1, z)]);
        } else {
            lightValue = (float) 15;
        }

        lightValue = convertLight(lightValue / (float) 15);

        //top
        light.pack(lightValue);

        //top
        indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

        indicesCount += 4;

        //top
        textureCoord.pack(topSizeXHigh, topSizeYLow, topSizeXLow, topSizeYLow, topSizeXLow, topSizeYHigh, topSizeXHigh, topSizeYHigh);


        //y is constant
        //bottom
        positions.pack(brl.x + x, brl.y + y, brl.z + z, brr.x + x, brr.y + y, brr.z + z, bfr.x + x, bfr.y + y, bfr.z + z, bfl.x + x, bfl.y + y, bfl.z + z);

        //bottom
        if (y - 1 > 0) {
            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y - 1, z)]);
        } else {
            lightValue = 0;
        }

        lightValue = convertLight(lightValue / (float) 15);

        //bottom
        light.pack(lightValue);

        //bottom
        indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

        indicesCount += 4;

        //bottom
        textureCoord.pack(bottomSizeXHigh, bottomSizeYLow, bottomSizeXLow, bottomSizeYLow, bottomSizeXLow, bottomSizeYHigh, bottomSizeXHigh, bottomSizeYHigh);
        //end of case 7

        return indicesCount;
    }

    private static int calculateAllFaces(int x, int y, int z, byte thisBlock, byte thisRotation, int allFacesIndicesCount, HyperFloatArray allFacesPositions, HyperFloatArray allFacesLight, HyperIntArray allFacesIndices, HyperFloatArray allFacesTextureCoord, ChunkObject chunkNeighborZPlus, ChunkObject chunkNeighborZMinus, ChunkObject chunkNeighborXMinus, ChunkObject chunkNeighborXPlus, byte chunkLightLevel, byte[] lightData){

        float[] textureWorker;

        //front
        allFacesPositions.pack(1f + x, 1f + y, 1f + z, 0f + x, 1f + y, 1f + z, 0f + x, 0f + y, 1f + z, 1f + x, 0f + y, 1f + z);

        //front
        float lightValue;
        if (z + 1 > 15) {
            lightValue = getNeighborLight(chunkNeighborZPlus, x, y, 0);
        } else {
            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z + 1)]);
        }

        lightValue = convertLight(lightValue / (float) 15);

        //front
        allFacesLight.pack(lightValue);

        //front
        allFacesIndices.pack(allFacesIndicesCount, 1 + allFacesIndicesCount, 2 + allFacesIndicesCount, allFacesIndicesCount, 2 + allFacesIndicesCount, 3 + allFacesIndicesCount);

        allFacesIndicesCount += 4;

        textureWorker = getFrontTexturePoints(thisBlock, thisRotation);
        //front
        allFacesTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);


        //back

        allFacesPositions.pack(0f + x, 1f + y, 0f + z, 1f + x, 1f + y, 0f + z, 1f + x, 0f + y, 0f + z, 0f + x, 0f + y, 0f + z);

        //back

        if (z - 1 < 0) {
            lightValue = getNeighborLight(chunkNeighborZMinus, x, y, 15);
        } else {
            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z - 1)]);
        }

        lightValue = convertLight(lightValue / (float) 15);
        //back

        allFacesLight.pack(lightValue);

        //back
        allFacesIndices.pack(allFacesIndicesCount, 1 + allFacesIndicesCount, 2 + allFacesIndicesCount, allFacesIndicesCount, 2 + allFacesIndicesCount, 3 + allFacesIndicesCount);

        allFacesIndicesCount += 4;

        textureWorker = getBackTexturePoints(thisBlock, thisRotation);
        //back
        allFacesTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);

        //right
        allFacesPositions.pack(1f + x, 1f + y, 0f + z, 1f + x, 1f + y, 1f + z, 1f + x, 0f + y, 1f + z, 1f + x, 0f + y, 0f + z);

        //right

        if (x + 1 > 15) {
            lightValue = getNeighborLight(chunkNeighborXPlus, 0, y, z);
        } else {
            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x + 1, y, z)]);
        }

        lightValue = convertLight(lightValue / (float) 15);

        //right
        allFacesLight.pack(lightValue);


        //right
        allFacesIndices.pack(allFacesIndicesCount, 1 + allFacesIndicesCount, 2 + allFacesIndicesCount, allFacesIndicesCount, 2 + allFacesIndicesCount, 3 + allFacesIndicesCount);

        allFacesIndicesCount += 4;

        textureWorker = getRightTexturePoints(thisBlock, thisRotation);
        //right
        allFacesTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);


        //left
        allFacesPositions.pack(0f + x, 1f + y, 1f + z, 0f + x, 1f + y, 0f + z, 0f + x, 0f + y, 0f + z, 0f + x, 0f + y, 1f + z);

        //left

        if (x - 1 < 0) {
            lightValue = getNeighborLight(chunkNeighborXMinus, 15, y, z);
        } else {
            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x - 1, y, z)]);
        }

        lightValue = convertLight(lightValue / (float) 15);

        //left
        allFacesLight.pack(lightValue);

        //left
        allFacesIndices.pack(allFacesIndicesCount, 1 + allFacesIndicesCount, 2 + allFacesIndicesCount, allFacesIndicesCount, 2 + allFacesIndicesCount, 3 + allFacesIndicesCount);

        allFacesIndicesCount += 4;

        textureWorker = getLeftTexturePoints(thisBlock, thisRotation);
        //left
        allFacesTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);


        //top
        allFacesPositions.pack(0f + x, 1f + y, 0f + z, 0f + x, 1f + y, 1f + z, 1f + x, 1f + y, 1f + z, 1f + x, 1f + y, 0f + z);

        //top

        if (y + 1 < 128) {
            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y + 1, z)]);
        } else {
            lightValue = (float) 15;
        }

        lightValue = convertLight(lightValue / (float) 15);

        //top
        allFacesLight.pack(lightValue);

        //top
        allFacesIndices.pack(allFacesIndicesCount, 1 + allFacesIndicesCount, 2 + allFacesIndicesCount, allFacesIndicesCount, 2 + allFacesIndicesCount, 3 + allFacesIndicesCount);

        allFacesIndicesCount += 4;

        textureWorker = getTopTexturePoints(thisBlock);
        //top
        allFacesTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);



        if (y != 0) {
            //bottom
            allFacesPositions.pack(0f + x, 0f + y, 1f + z, 0f + x, 0f + y, 0f + z, 1f + x, 0f + y, 0f + z, 1f + x, 0f + y, 1f + z);

            //bottom

            if (y - 1 > 0) {
                lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y - 1, z)]);
            } else {
                lightValue = 0;
            }

            lightValue = convertLight(lightValue / (float) 15);

            //bottom
            allFacesLight.pack(lightValue);

            //bottom
            allFacesIndices.pack(allFacesIndicesCount, 1 + allFacesIndicesCount, 2 + allFacesIndicesCount, allFacesIndicesCount, 2 + allFacesIndicesCount, 3 + allFacesIndicesCount);

            allFacesIndicesCount += 4;

            textureWorker = getBottomTexturePoints(thisBlock);
            //bottom
            allFacesTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        return allFacesIndicesCount;
    }

    private static byte calculateBlockLight(byte chunkLightLevel, byte lightData){
        byte naturalLightOfBlock = getByteNaturalLight(lightData);

        if (naturalLightOfBlock > chunkLightLevel){
            naturalLightOfBlock = chunkLightLevel;
        }

        byte torchLight = getByteTorchLight(lightData);

        if (naturalLightOfBlock > torchLight){
            return naturalLightOfBlock;
        } else {
            return torchLight;
        }
    }

    private static byte getNeighborBlock(ChunkObject neighborChunk, int x, int y, int z){
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

        byte naturalLightOfBlock = getByteNaturalLight(neighborChunk.light[index]);

        byte currentGlobalLightLevel = getCurrentGlobalLightLevel();

        if (naturalLightOfBlock > currentGlobalLightLevel){
            naturalLightOfBlock = currentGlobalLightLevel;
        }

        byte torchLight = getByteTorchLight(neighborChunk.light[index]);

        if (naturalLightOfBlock > torchLight){
            return naturalLightOfBlock;
        } else {
            return torchLight;
        }
    }


    private static boolean chunkStackContainsBlock(ChunkObject thisChunk, int yHeight){
        if (thisChunk == null || thisChunk.block == null){
            return false;
        }
        for (int x = 0; x < 16; x++){
            for (int z = 0; z < 16; z++){
                for (int y = yHeight * 16; y < (yHeight + 1) * 16; y++){
                    if (thisChunk.block[posToIndex(x,y,z)] != 0){
                        return true;
                    }
                }
            }
        }
        return false;
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


    //these are cloned methods from the main thread

    private static byte getBlockDrawType(byte ID){
        return blockIDs[ID].drawType;
    }

    private static float[] getFrontTexturePoints(int ID, byte rotation){
        return switch (rotation) {
            case 1 -> blockIDs[ID].rightTexture;
            case 2 -> blockIDs[ID].backTexture;
            case 3 -> blockIDs[ID].leftTexture;
            default -> blockIDs[ID].frontTexture;
        };
    }
    private static float[] getBackTexturePoints(int ID, byte rotation){
        return switch (rotation) {
            case 1 -> blockIDs[ID].leftTexture;
            case 2 -> blockIDs[ID].frontTexture;
            case 3 -> blockIDs[ID].rightTexture;
            default -> blockIDs[ID].backTexture;
        };

    }
    private static float[] getRightTexturePoints(int ID, byte rotation){
        return switch (rotation) {
            case 1 -> blockIDs[ID].backTexture;
            case 2 -> blockIDs[ID].leftTexture;
            case 3 -> blockIDs[ID].frontTexture;
            default -> blockIDs[ID].rightTexture;
        };
    }
    private static float[] getLeftTexturePoints(int ID, byte rotation){
        return switch (rotation) {
            case 1 -> blockIDs[ID].frontTexture;
            case 2 -> blockIDs[ID].rightTexture;
            case 3 -> blockIDs[ID].backTexture;
            default -> blockIDs[ID].leftTexture;
        };
    }

    private static float[] getTopTexturePoints(int ID){
        return blockIDs[ID].topTexture;
    }
    private static float[] getBottomTexturePoints(int ID){
        return blockIDs[ID].bottomTexture;
    }

    private static boolean getIfLiquid(int ID){
        return blockIDs[ID].isLiquid;
    }


    private static float[][] getBlockShape(byte ID, byte rot){

        float[][] newBoxes = new float[blockShapeMap[blockIDs[ID].drawType].getBoxes().length][6];


        int index = 0;

        //automated as base, since it's the same
        switch (rot) {
            case 0 -> {
                for (float[] thisShape : blockShapeMap[blockIDs[ID].drawType].getBoxes()) {
                    System.arraycopy(thisShape, 0, newBoxes[index], 0, 6);
                    index++;
                }
            }
            case 1 -> {
                for (float[] thisShape : blockShapeMap[blockIDs[ID].drawType].getBoxes()) {

                    float blockDiffZ = 1f - thisShape[5];
                    float widthZ = thisShape[5] - thisShape[2];

                    newBoxes[index][0] = blockDiffZ;
                    newBoxes[index][1] = thisShape[1];//-y
                    newBoxes[index][2] = thisShape[0]; // -z

                    newBoxes[index][3] = blockDiffZ + widthZ;
                    newBoxes[index][4] = thisShape[4];//+y
                    newBoxes[index][5] = thisShape[3]; //+z
                    index++;
                }
            }
            case 2 -> {
                for (float[] thisShape : blockShapeMap[blockIDs[ID].drawType].getBoxes()) {

                    float blockDiffZ = 1f - thisShape[5];
                    float widthZ = thisShape[5] - thisShape[2];

                    float blockDiffX = 1f - thisShape[3];
                    float widthX = thisShape[3] - thisShape[0];

                    newBoxes[index][0] = blockDiffX;
                    newBoxes[index][1] = thisShape[1];//-y
                    newBoxes[index][2] = blockDiffZ; // -z

                    newBoxes[index][3] = blockDiffX + widthX;
                    newBoxes[index][4] = thisShape[4];//+y
                    newBoxes[index][5] = blockDiffZ + widthZ; //+z
                    index++;
                }
            }
            case 3 -> {
                for (float[] thisShape : blockShapeMap[blockIDs[ID].drawType].getBoxes()) {
                    float blockDiffX = 1f - thisShape[3];
                    float widthX = thisShape[3] - thisShape[0];

                    newBoxes[index][0] = thisShape[2];
                    newBoxes[index][1] = thisShape[1];//-y
                    newBoxes[index][2] = blockDiffX; // -z

                    newBoxes[index][3] = thisShape[5];
                    newBoxes[index][4] = thisShape[4];//+y
                    newBoxes[index][5] = blockDiffX + widthX; //+z
                    index++;
                }
            }
        }
        return newBoxes;
    }
}