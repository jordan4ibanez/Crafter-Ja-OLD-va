package game.chunk;

import engine.highPerformanceContainers.HyperFloatArray;
import engine.highPerformanceContainers.HyperIntArray;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.concurrent.ConcurrentLinkedDeque;

import static engine.Window.windowShouldClose;
import static game.blocks.BlockDefinition.getMaxIDs;
import static game.chunk.Chunk.*;
import static game.chunk.ChunkMeshGenerationHandler.addToChunkMeshQueue;

//note: this entire class is held on it's own thread, be careful with OpenGL context or switch to Vulkan so it can dump data into the GPU
public class ChunkMeshGenerator implements Runnable{
    //DO NOT CHANGE THE DATA CONTAINER
    private static final ConcurrentLinkedDeque<Vector3i> generationQueue = new ConcurrentLinkedDeque<>();


    private final static Vector3i key = new Vector3i();

    private static final byte maxIDs = getMaxIDs();

    //holds the blockshape data - on this thread
    //maybe a dynamic type would be better for this for api usage in the future?
    private final static float[][][] blockShapeMap = new float[(byte)10][0][0];

    //holds BlockDefinition data - on this thread
    private static final byte[] drawTypes = new byte[maxIDs];
    private static final float[][] frontTextures = new float[maxIDs][0];  //front
    private static final float[][] backTextures = new float[maxIDs][0];   //back
    private static final float[][] rightTextures = new float[maxIDs][0];  //right
    private static final float[][] leftTextures = new float[maxIDs][0];   //left
    private static final float[][] topTextures = new float[maxIDs][0];    //top
    private static final float[][] bottomTextures = new float[maxIDs][0]; //bottom
    private static final boolean[] isLiquids = new boolean[maxIDs];

    private static byte currentGlobalLightLevel = 15;

    //normal block mesh data
    private static final HyperFloatArray positions = new HyperFloatArray(24);
    private static final HyperFloatArray textureCoord = new HyperFloatArray(16);
    private static final HyperIntArray indices = new HyperIntArray(12);
    private static final HyperFloatArray light = new HyperFloatArray(24);

    //liquid block mesh data
    private static final HyperFloatArray liquidPositions = new HyperFloatArray(24);
    private static final HyperFloatArray liquidTextureCoord = new HyperFloatArray(16);
    private static final HyperIntArray liquidIndices = new HyperIntArray(12);
    private static final HyperFloatArray liquidLight = new HyperFloatArray(24);

    //allFaces block mesh data
    private static final HyperFloatArray allFacesPositions = new HyperFloatArray(24);
    private static final HyperFloatArray allFacesTextureCoord = new HyperFloatArray(16);
    private static final HyperIntArray allFacesIndices = new HyperIntArray(12);
    private static final HyperFloatArray allFacesLight = new HyperFloatArray(24);



    public static void passChunkMeshThreadData(byte[] dupeDrawTypes, float[][] dupeFrontTextures, float[][] dupeBackTextures, float[][] dupeRightTextures, float[][] dupeLeftTextures, float[][] dupeTopTextures, float[][] dupeBottomTextures, boolean[] dupeIsLiquids, float[][][]dupeBlockShapeMap){
        //copy data
        System.arraycopy(dupeDrawTypes, 0, drawTypes, 0, dupeDrawTypes.length);
        System.arraycopy(dupeFrontTextures, 0, frontTextures, 0, dupeFrontTextures.length);
        System.arraycopy(dupeBackTextures, 0, backTextures, 0, dupeBackTextures.length);
        System.arraycopy(dupeRightTextures, 0, rightTextures, 0, dupeRightTextures.length);
        System.arraycopy(dupeLeftTextures, 0, leftTextures, 0, dupeLeftTextures.length);
        System.arraycopy(dupeTopTextures, 0, topTextures, 0, dupeTopTextures.length);
        System.arraycopy(dupeBottomTextures, 0, bottomTextures, 0, dupeBottomTextures.length);
        System.arraycopy(dupeIsLiquids, 0, isLiquids, 0, dupeIsLiquids.length);

        //copy block shape map
        for (int i = 0; i < dupeBlockShapeMap.length; i++){
            float[][] baseOfShape = dupeBlockShapeMap[i];
            blockShapeMap[i] = new float[baseOfShape.length][0];
            for (int q = 0; q < baseOfShape.length; q++){
                float[] actualShape = baseOfShape[q];
                blockShapeMap[i][q] = new float[actualShape.length];
                System.arraycopy(actualShape, 0, blockShapeMap[i][q], 0, actualShape.length);
            }
        }

    }

    public void run() {
        //run until game is closed - should only be run in game
        while (!windowShouldClose()) {
            pollQueue();
        }
    }

    public static void generateChunkMesh(int chunkX, int chunkZ, int yHeight) {
        //do not add duplicates
        if (!generationQueue.contains(new Vector3i(chunkX,yHeight, chunkZ))) {
            generationQueue.add(new Vector3i(chunkX, yHeight, chunkZ));
        }
    }

    public static void instantGeneration(int chunkX, int chunkZ, int yHeight){
        //replace the data basically
        generationQueue.remove(new Vector3i(chunkX,yHeight, chunkZ));
        generationQueue.addFirst(new Vector3i(chunkX,yHeight, chunkZ));
    }

    //allows main thread to set the local byte of light to this runnable thread
    public static void setChunkThreadCurrentGlobalLightLevel(byte newLight){
        currentGlobalLightLevel = newLight;
    }


    //this polls for chunk meshes to update
    private static void pollQueue() {
        if (!generationQueue.isEmpty()) {

            try {
                key.set(generationQueue.pop());
            } catch (Exception ignore) {
                return; //don't crash basically
            }

            byte[] blockData = getBlockDataClone(key.x, key.z);
            byte[] rotationData = getRotationDataClone(key.x, key.z);
            byte[] lightData = getLightDataClone(key.x, key.z);

            //don't bother if the chunk doesn't exist
            if (blockData == null || rotationData == null || lightData == null) {
                return;
            }

            //raw data extracted into stack primitives
            final int chunkX = key.x;
            final int chunkZ = key.z;
            final int yHeight = key.y;

            //neighbor chunks
            byte[] chunkNeighborXPlusBlockData  = getBlockDataClone(key.x + 1, key.z);
            byte[] chunkNeighborXMinusBlockData = getBlockDataClone(key.x - 1, key.z);
            byte[] chunkNeighborZPlusBlockData  = getBlockDataClone(key.x, key.z + 1);
            byte[] chunkNeighborZMinusBlockData = getBlockDataClone(key.x, key.z - 1);

            byte[] chunkNeighborXPlusLightData  = getLightDataClone(key.x + 1, key.z);
            byte[] chunkNeighborXMinusLightData = getLightDataClone(key.x - 1, key.z);
            byte[] chunkNeighborZPlusLightData  = getLightDataClone(key.x, key.z + 1);
            byte[] chunkNeighborZMinusLightData = getLightDataClone(key.x, key.z - 1);

            int indicesCount = 0;

            int liquidIndicesCount = 0;

            int allFacesIndicesCount = 0;

            //current global light level - dumped into the stack
            byte chunkLightLevel = currentGlobalLightLevel;

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
                                case 1 -> indicesCount = calculateNormal(x, y, z, thisBlock, thisRotation, indicesCount, blockData, chunkNeighborZPlusBlockData,chunkNeighborZPlusLightData, chunkNeighborZMinusBlockData,chunkNeighborZMinusLightData, chunkNeighborXPlusBlockData,chunkNeighborXPlusLightData, chunkNeighborXMinusBlockData,chunkNeighborXMinusLightData, chunkLightLevel, lightData);

                                //allfaces
                                case 4 -> allFacesIndicesCount = calculateAllFaces(x, y, z, thisBlock, thisRotation, allFacesIndicesCount, chunkNeighborZPlusLightData, chunkNeighborZMinusLightData, chunkNeighborXPlusLightData, chunkNeighborXMinusLightData, chunkLightLevel, lightData);

                                //torch
                                case 7 -> indicesCount = calculateTorchLike(x, y, z, thisBlock, thisRotation, indicesCount, chunkNeighborZPlusLightData, chunkNeighborZMinusLightData, chunkNeighborXPlusLightData, chunkNeighborXMinusLightData, chunkLightLevel, lightData);

                                //liquid
                                case 8 -> liquidIndicesCount = calculateLiquids(x, y, z, thisBlock, thisRotation, liquidIndicesCount, blockData, chunkNeighborZPlusBlockData,chunkNeighborZPlusLightData, chunkNeighborZMinusBlockData,chunkNeighborZMinusLightData, chunkNeighborXPlusBlockData,chunkNeighborXPlusLightData, chunkNeighborXMinusBlockData,chunkNeighborXMinusLightData, chunkLightLevel, lightData);

                                //blockbox
                                default -> indicesCount = calculateBlockBox(x, y, z, thisBlock, thisRotation, indicesCount, chunkNeighborZPlusLightData, chunkNeighborZMinusLightData, chunkNeighborXPlusLightData, chunkNeighborXMinusLightData, chunkLightLevel, lightData);
                            }
                        }
                    }
                }
            }


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


            //reset the data so it can be reused
            positions.reset();
            textureCoord.reset();
            indices.reset();
            light.reset();
            liquidPositions.reset();
            liquidTextureCoord.reset();
            liquidIndices.reset();
            liquidLight.reset();
            allFacesPositions.reset();
            allFacesTextureCoord.reset();
            allFacesIndices.reset();
            allFacesLight.reset();

            //finally add it into the queue to be popped
            addToChunkMeshQueue(newChunkData);
        }
    }


    private static int calculateBlockBox(int x, int y, int z, byte thisBlock, byte thisRotation, int indicesCount, byte[] chunkNeighborZPlusLightData, byte[] chunkNeighborZMinusLightData, byte[] chunkNeighborXPlusLightData, byte[] chunkNeighborXMinusLightData, byte chunkLightLevel, byte[] lightData){
        for (float[] thisBlockBox : getBlockShape(thisBlock, thisRotation)) {
            //front
            positions.pack(thisBlockBox[3] + x, thisBlockBox[4] + y, thisBlockBox[5] + z, thisBlockBox[0] + x, thisBlockBox[4] + y, thisBlockBox[5] + z, thisBlockBox[0] + x, thisBlockBox[1] + y, thisBlockBox[5] + z, thisBlockBox[3] + x, thisBlockBox[1] + y, thisBlockBox[5] + z);

            //front
            float lightValue;
            byte realLight;
            if (z + 1 > 15) {
                realLight = getNeighborLight(chunkLightLevel, chunkNeighborZPlusLightData, x, y, 0);
            } else {
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z + 1)]);
            }

            lightValue = convertLight(realLight);

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
                realLight = getNeighborLight(chunkLightLevel, chunkNeighborZMinusLightData, x, y, 15);
            } else {
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z - 1)]);
            }


            lightValue = convertLight(realLight);
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
                realLight = getNeighborLight(chunkLightLevel, chunkNeighborXPlusLightData, 0, y, z);
            } else {
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x + 1, y, z)]);
            }
            lightValue = convertLight(realLight);

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
                realLight = getNeighborLight(chunkLightLevel, chunkNeighborXMinusLightData, 15, y, z);
            } else {
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x - 1, y, z)]);
            }
            lightValue = convertLight(realLight);

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
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y + 1, z)]);
            } else {
                realLight = chunkLightLevel;
            }
            lightValue = convertLight(realLight);

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
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y - 1, z)]);
            } else {
                realLight = 0;
            }
            lightValue = convertLight(realLight);

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

    private static int calculateLiquids(int x, int y, int z, byte thisBlock, byte thisRotation, int liquidIndicesCount, byte[] blockData, byte[] chunkNeighborZPlusBlockData, byte[] chunkNeighborZPlusLightData, byte[] chunkNeighborZMinusBlockData, byte[] chunkNeighborZMinusLightData, byte[] chunkNeighborXPlusBlockData, byte[] chunkNeighborXPlusLightData, byte[] chunkNeighborXMinusBlockData, byte[] chunkNeighborXMinusLightData, byte chunkLightLevel, byte[] lightData){
        byte neighborBlock;

        if (z + 1 > 15) {
            neighborBlock = getNeighborBlock(chunkNeighborZPlusBlockData, x, y, 0);
        } else {
            neighborBlock = blockData[posToIndex(x, y, z + 1)];
        }
        byte neighborDrawtype = getBlockDrawType(neighborBlock);

        float lightValue;
        byte realLight;
        float[] textureWorker;
        if ((neighborDrawtype == 0 || neighborDrawtype > 1) && neighborDrawtype != 8) {
            //front
            liquidPositions.pack(1f + x, 1f + y, 1f + z, 0f + x, 1f + y, 1f + z, 0f + x, 0f + y, 1f + z, 1f + x, 0f + y, 1f + z);

            //front
            if (z + 1 > 15) {
                realLight = getNeighborLight(chunkLightLevel, chunkNeighborZPlusLightData, x, y, 0);
            } else {
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z + 1)]);
            }

            lightValue = convertLight(realLight);

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
            neighborBlock = getNeighborBlock(chunkNeighborZMinusBlockData, x, y, 15);
        } else {
            neighborBlock = blockData[posToIndex(x, y, z - 1)];
        }
        neighborDrawtype = getBlockDrawType(neighborBlock);

        if ((neighborDrawtype == 0 || neighborDrawtype > 1) && neighborDrawtype != 8) {
            //back
            liquidPositions.pack(0f + x, 1f + y, 0f + z, 1f + x, 1f + y, 0f + z, 1f + x, 0f + y, 0f + z, 0f + x, 0f + y, 0f + z);

            //back
            if (z - 1 < 0) {
                realLight = getNeighborLight(chunkLightLevel, chunkNeighborZMinusLightData, x, y, 15);
            } else {
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z - 1)]);
            }

            lightValue = convertLight(realLight);

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
            neighborBlock = getNeighborBlock(chunkNeighborXPlusBlockData, 0, y, z);
        } else {
            neighborBlock = blockData[posToIndex(x + 1, y, z)];
        }
        neighborDrawtype = getBlockDrawType(neighborBlock);

        if ((neighborDrawtype == 0 || neighborDrawtype > 1) && neighborDrawtype != 8) {
            //right
            liquidPositions.pack(1f + x, 1f + y, 0f + z, 1f + x, 1f + y, 1f + z, 1f + x, 0f + y, 1f + z, 1f + x, 0f + y, 0f + z);

            //right

            if (x + 1 > 15) {
                realLight = getNeighborLight(chunkLightLevel, chunkNeighborXPlusLightData, 0, y, z);
            } else {
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x + 1, y, z)]);
            }

            lightValue = convertLight(realLight);

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
            neighborBlock = getNeighborBlock(chunkNeighborXMinusBlockData, 15, y, z);
        } else {
            neighborBlock = blockData[posToIndex(x - 1, y, z)];
        }
        neighborDrawtype = getBlockDrawType(neighborBlock);

        if ((neighborDrawtype == 0 || neighborDrawtype > 1) && neighborDrawtype != 8) {
            //left
            liquidPositions.pack(0f + x, 1f + y, 1f + z, 0f + x, 1f + y, 0f + z, 0f + x, 0f + y, 0f + z, 0f + x, 0f + y, 1f + z);

            //left

            if (x - 1 < 0) {
                realLight = getNeighborLight(chunkLightLevel, chunkNeighborXMinusLightData, 15, y, z);
            } else {
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x - 1, y, z)]);
            }

            lightValue = convertLight(realLight);
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
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y + 1, z)]);
            } else {
                realLight = chunkLightLevel;
            }

            lightValue = convertLight(realLight);

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
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y - 1, z)]);
            } else {
                realLight = 0;
            }

            lightValue = convertLight(realLight);
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

    private static int calculateNormal(int x, int y, int z, byte thisBlock, byte thisRotation, int indicesCount, byte[] blockData, byte[] chunkNeighborZPlusBlockData, byte[] chunkNeighborZPlusLightData, byte[] chunkNeighborZMinusBlockData, byte[] chunkNeighborZMinusLightData, byte[] chunkNeighborXPlusBlockData, byte[] chunkNeighborXPlusLightData, byte[] chunkNeighborXMinusBlockData, byte[] chunkNeighborXMinusLightData, byte chunkLightLevel, byte[] lightData){

        byte neighborBlock;
        if (z + 1 > 15) {
            neighborBlock = getNeighborBlock(chunkNeighborZPlusBlockData, x, y, 0);
        } else {
            neighborBlock = blockData[posToIndex(x, y, z + 1)];
        }

        float lightValue;
        byte realLight;
        float[] textureWorker;
        if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
            //front

            positions.pack(1f + x, 1f + y, 1f + z, 0f + x, 1f + y, 1f + z, 0f + x, 0f + y, 1f + z, 1f + x, 0f + y, 1f + z);

            //front
            if (z + 1 > 15) {
                realLight = getNeighborLight(chunkLightLevel, chunkNeighborZPlusLightData, x, y, 0);
            } else {
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z + 1)]);
            }

            lightValue = convertLight(realLight);

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
            neighborBlock = getNeighborBlock(chunkNeighborZMinusBlockData, x, y, 15);
        } else {
            neighborBlock = blockData[posToIndex(x, y, z - 1)];
        }

        if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
            //back
            positions.pack(0f + x, 1f + y, 0f + z, 1f + x, 1f + y, 0f + z, 1f + x, 0f + y, 0f + z, 0f + x, 0f + y, 0f + z);

            //back
            if (z - 1 < 0) {
                realLight = getNeighborLight(chunkLightLevel, chunkNeighborZMinusLightData, x, y, 15);
            } else {
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z - 1)]);
            }

            lightValue = convertLight(realLight);
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
            neighborBlock = getNeighborBlock(chunkNeighborXPlusBlockData, 0, y, z);
        } else {
            neighborBlock = blockData[posToIndex(x + 1, y, z)];
        }

        if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
            //right
            positions.pack(1f + x, 1f + y, 0f + z, 1f + x, 1f + y, 1f + z, 1f + x, 0f + y, 1f + z, 1f + x, 0f + y, 0f + z);

            //right
            if (x + 1 > 15) {
                realLight = getNeighborLight(chunkLightLevel, chunkNeighborXPlusLightData, 0, y, z);
            } else {
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x + 1, y, z)]);
            }

            lightValue = convertLight(realLight);
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
            neighborBlock = getNeighborBlock(chunkNeighborXMinusBlockData, 15, y, z);
        } else {
            neighborBlock = blockData[posToIndex(x - 1, y, z)];
        }

        if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
            //left
            positions.pack(0f + x, 1f + y, 1f + z, 0f + x, 1f + y, 0f + z, 0f + x, 0f + y, 0f + z, 0f + x, 0f + y, 1f + z);

            //left
            if (x - 1 < 0) {
                realLight = getNeighborLight(chunkLightLevel, chunkNeighborXMinusLightData, 15, y, z);
            } else {
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x - 1, y, z)]);
            }

            lightValue = convertLight(realLight);
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
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y + 1, z)]);
            } else {
                realLight = chunkLightLevel;
            }

            lightValue = convertLight(realLight);

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
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y - 1, z)]);
            } else {
                realLight = 0;
            }

            lightValue = convertLight(realLight);
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


    private static int calculateTorchLike(int x, int y, int z, byte thisBlock, byte thisRotation, int indicesCount, byte[] chunkNeighborZPlusLightData, byte[] chunkNeighborZMinusLightData, byte[] chunkNeighborXPlusLightData, byte[] chunkNeighborXMinusLightData, byte chunkLightLevel, byte[] lightData){
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
        byte realLight;
        if (z + 1 > 15) {
            realLight = getNeighborLight(chunkLightLevel, chunkNeighborZPlusLightData, x, y, 0);
        } else {
            realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z + 1)]);
        }

        lightValue = convertLight(realLight);

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
            realLight = getNeighborLight(chunkLightLevel, chunkNeighborZMinusLightData, x, y, 15);
        } else {
            realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z - 1)]);
        }

        lightValue = convertLight(realLight);

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
            realLight = getNeighborLight(chunkLightLevel, chunkNeighborXPlusLightData, 0, y, z);
        } else {
            realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x + 1, y, z)]);
        }

        lightValue = convertLight(realLight);

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
            realLight = getNeighborLight(chunkLightLevel, chunkNeighborXMinusLightData, 15, y, z);
        } else {
            realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x - 1, y, z)]);
        }

        lightValue = convertLight(realLight);

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
            realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y + 1, z)]);
        } else {
            realLight = chunkLightLevel;
        }

        lightValue = convertLight(realLight);

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
            realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y - 1, z)]);
        } else {
            realLight = 0;
        }

        lightValue = convertLight(realLight);

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

    private static int calculateAllFaces(int x, int y, int z, byte thisBlock, byte thisRotation, int allFacesIndicesCount, byte[] chunkNeighborZPlusLightData, byte[] chunkNeighborZMinusLightData, byte[] chunkNeighborXPlusLightData, byte[] chunkNeighborXMinusLightData, byte chunkLightLevel, byte[] lightData){

        float[] textureWorker;

        //front
        allFacesPositions.pack(1f + x, 1f + y, 1f + z, 0f + x, 1f + y, 1f + z, 0f + x, 0f + y, 1f + z, 1f + x, 0f + y, 1f + z);

        //front
        float lightValue;

        byte realLight;

        if (z + 1 > 15) {
            realLight = getNeighborLight(chunkLightLevel, chunkNeighborZPlusLightData, x, y, 0);
        } else {
            realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z + 1)]);
        }

        lightValue = convertLight(realLight);

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
            realLight = getNeighborLight(chunkLightLevel, chunkNeighborZMinusLightData, x, y, 15);
        } else {
            realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z - 1)]);
        }

        lightValue = convertLight(realLight);
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
            realLight = getNeighborLight(chunkLightLevel, chunkNeighborXPlusLightData, 0, y, z);
        } else {
            realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x + 1, y, z)]);
        }

        lightValue = convertLight(realLight);

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
            realLight = getNeighborLight(chunkLightLevel, chunkNeighborXMinusLightData, 15, y, z);
        } else {
            realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x - 1, y, z)]);
        }

        lightValue = convertLight(realLight);

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
            realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y + 1, z)]);
        } else {
            realLight = chunkLightLevel;
        }

        lightValue = convertLight(realLight);

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
                realLight = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y - 1, z)]);
            } else {
                realLight = 0;
            }

            lightValue = convertLight(realLight);

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

    private static byte getNeighborBlock(byte[] neighborChunkBlockData, int x, int y, int z){
        if (neighborChunkBlockData == null){
            return 0;
        }
        return neighborChunkBlockData[posToIndex(x,y,z)];
    }

    private static byte getNeighborLight(byte currentGlobalLightLevel, byte[] neighborChunkLightData, int x, int y, int z){
        if (neighborChunkLightData == null){
            return 0;
        }

        int index = posToIndex(x, y, z);

        byte naturalLightOfBlock = getByteNaturalLight(neighborChunkLightData[index]);

        if (naturalLightOfBlock > currentGlobalLightLevel){
            naturalLightOfBlock = currentGlobalLightLevel;
        }

        byte torchLight = getByteTorchLight(neighborChunkLightData[index]);

        if (naturalLightOfBlock > torchLight){
            return naturalLightOfBlock;
        } else {
            return torchLight;
        }
    }


    //this is an internal duplicate specific to this thread
    private static float convertLight(byte lightValue){
        return (float) Math.pow(1.25, lightValue)/28.42171f;
    }

    private static int posToIndex( int x, int y, int z ) {
        return (z * 2048) + (y * 16) + x;
    }


    private static byte getBlockDrawType(byte ID){
        return drawTypes[ID];
    }

    public static float[] getFrontTexturePoints(byte ID, byte rotation){
        return switch (rotation) {
            case 1 -> rightTextures[ID];
            case 2 -> backTextures[ID];
            case 3 -> leftTextures[ID];
            default -> frontTextures[ID];
        };
    }
    public static float[] getBackTexturePoints(byte ID, byte rotation){
        return switch (rotation) {
            case 1 -> leftTextures[ID];
            case 2 -> frontTextures[ID];
            case 3 -> rightTextures[ID];
            default -> backTextures[ID];
        };

    }
    public static float[] getRightTexturePoints(byte ID, byte rotation){
        return switch (rotation) {
            case 1 -> backTextures[ID];
            case 2 -> leftTextures[ID];
            case 3 -> frontTextures[ID];
            default -> rightTextures[ID];
        };
    }
    public static float[] getLeftTexturePoints(byte ID, byte rotation){
        return switch (rotation) {
            case 1 -> frontTextures[ID];
            case 2 -> rightTextures[ID];
            case 3 -> backTextures[ID];
            default -> leftTextures[ID];
        };
    }

    private static float[] getTopTexturePoints(int ID){
        return topTextures[ID];
    }
    private static float[] getBottomTexturePoints(int ID){
        return bottomTextures[ID];
    }
    private static boolean getIfLiquid(int ID){
        return isLiquids[ID];
    }


    private static float[][] getBlockShape(byte ID, byte rot){

        byte drawType = drawTypes[ID];

        float[][] newBoxes = new float[blockShapeMap[drawType].length][6];


        int index = 0;

        //automated as base, since it's the same
        switch (rot) {
            case 0 -> {
                for (float[] thisShape : blockShapeMap[drawType]) {
                    System.arraycopy(thisShape, 0, newBoxes[index], 0, 6);
                    index++;
                }
            }
            case 1 -> {
                for (float[] thisShape : blockShapeMap[drawType]) {

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
                for (float[] thisShape : blockShapeMap[drawType]) {

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
                for (float[] thisShape : blockShapeMap[drawType]) {
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