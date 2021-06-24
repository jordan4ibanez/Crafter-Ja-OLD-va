package game.chunk;

import it.unimi.dsi.fastutil.ints.Int2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedDeque;

import static engine.Window.windowShouldClose;
import static game.blocks.BlockDefinition.*;
import static game.chunk.Chunk.*;
import static game.chunk.ChunkMeshGenerationHandler.addToChunkMeshQueue;
import static game.light.Light.getCurrentGlobalLightLevel;

public class ChunkMeshGenerator implements Runnable{
    //DO NOT CHANGE THE DATA CONTAINER
    private static final ConcurrentLinkedDeque<Vector3i> generationQueue = new ConcurrentLinkedDeque<>();

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

    //cache data
    //these are held on the heap
    private final static byte maxLight = 15;

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


    private static void pollQueue(){
        if (!generationQueue.isEmpty()) {

            //long startTime = System.nanoTime();

            Vector3i updateRawData;
            try {
                updateRawData = generationQueue.pop();
            } catch (Exception ignore){
                return; //don't crash basically
            }

            //don't crash
            if (updateRawData == null){
                return;
            }

            ChunkObject thisChunk = getChunk(updateRawData.x, updateRawData.z);

            //don't bother if the chunk doesn't exist
            if (thisChunk == null) {
                return;
            }

            //raw data extracted into stack primitives
            int chunkX  = updateRawData.x;
            int chunkZ  = updateRawData.z;
            int yHeight = updateRawData.y;



            //neighbor chunks
            ChunkObject chunkNeighborXPlus  = getChunk(chunkX + 1, chunkZ);
            ChunkObject chunkNeighborXMinus = getChunk(chunkX - 1, chunkZ);
            ChunkObject chunkNeighborZPlus  = getChunk(chunkX, chunkZ + 1);
            ChunkObject chunkNeighborZMinus = getChunk(chunkX, chunkZ - 1);

            //todo: finalize these variables

            //thank you FastUtil for existing

            //normal block mesh data
            Int2FloatLinkedOpenHashMap positions    = new Int2FloatLinkedOpenHashMap();
            int positionsCounter                    = 0;
            Int2FloatLinkedOpenHashMap textureCoord = new Int2FloatLinkedOpenHashMap();
            int textureCoordCounter                 = 0;
            Int2IntLinkedOpenHashMap indices        = new Int2IntLinkedOpenHashMap();
            int indicesCounter                      = 0;
            Int2FloatLinkedOpenHashMap light        = new Int2FloatLinkedOpenHashMap();
            int lightCounter                        = 0;

            int indicesCount = 0;

            //liquid block mesh data
            /*
            final LinkedList<Float> liquidPositions = new LinkedList<>();
            final LinkedList<Float> liquidTextureCoord = new LinkedList<>();
            final LinkedList<Integer> liquidIndices = new LinkedList<>();
            final LinkedList<Float> liquidLight = new LinkedList<>();

            int liquidIndicesCount = 0;
             */

            //allFaces block mesh data
            Int2FloatLinkedOpenHashMap allFacesPositions    = new Int2FloatLinkedOpenHashMap();
            int allFacesPositionsCounter                    = 0;
            Int2FloatLinkedOpenHashMap allFacesTextureCoord = new Int2FloatLinkedOpenHashMap();
            int allFacesTextureCoordCounter                 = 0;
            Int2IntLinkedOpenHashMap allFacesIndices        = new Int2IntLinkedOpenHashMap();
            int allFacesIndicesCounter                      = 0;
            Int2FloatLinkedOpenHashMap allFacesLight        = new Int2FloatLinkedOpenHashMap();
            int allFacesLightCounter                        = 0;

            int allFacesIndicesCount = 0;



            byte chunkLightLevel = getCurrentGlobalLightLevel();

            //reduces lookup time
            byte[] blockData = thisChunk.block;
            byte[] rotationData = thisChunk.rotation;
            byte[] lightData = thisChunk.light;

            float lightValue;
            float[] textureWorker;
            byte neighborBlock;
            byte thisBlock;
            byte thisBlockDrawType;
            byte thisRotation;
            int x,y,z,i; //dump all of this into the stack

            for (x = 0; x < 16; x++) { ;
                for (z = 0; z < 16; z++) {
                    for (y = yHeight * 16; y < (yHeight + 1) * 16; y++) {

                        thisBlock = blockData[posToIndex(x, y, z)];

                        if (thisBlock > 0) {

                            //only need to look this data up if it's not air
                            thisBlockDrawType = getBlockDrawType(thisBlock);
                            thisRotation = rotationData[posToIndex(x, y, z)];

                            //todo --------------------------------------- THE LIQUID DRAWTYPE

                            /*
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
                                        lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z + 1)]);
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
                                        lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z - 1)]);
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
                                        lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x +1, y, z)]);
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
                                        lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x - 1, y, z)]);
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
                                        lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y + 1, z)]);
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
                                        lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y - 1, z)]);
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


                            else */


                            //todo --------------------------------------- THE NORMAL DRAWTYPE (standard blocks)
                            switch (thisBlockDrawType) {
                                case 1: {
                                    if (z + 1 > 15) {
                                        neighborBlock = getNeighborBlock(chunkNeighborZPlus, x, y, 0);
                                    } else {
                                        neighborBlock = blockData[posToIndex(x, y, z + 1)];
                                    }

                                    if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
                                        //front

                                        positions.put(positionsCounter, 1f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + z);
                                        positionsCounter++;

                                        //front
                                        if (z + 1 > 15) {
                                            lightValue = getNeighborLight(chunkNeighborZPlus, x, y, 0);
                                        } else {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z + 1)]);
                                        }

                                        lightValue = convertLight(lightValue / maxLight);

                                        //front
                                        for (i = 0; i < 12; i++) {
                                            light.put(lightCounter, lightValue);
                                            lightCounter++;
                                        }

                                        //front
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 1 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 3 + indicesCount);
                                        indicesCounter++;

                                        indicesCount += 4;

                                        textureWorker = getFrontTexturePoints(thisBlock, thisRotation);
                                        //front
                                        textureCoord.put(textureCoordCounter, textureWorker[1]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[2]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[0]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[2]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[0]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[3]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[1]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[3]);
                                        textureCoordCounter++;
                                    }

                                    if (z - 1 < 0) {
                                        neighborBlock = getNeighborBlock(chunkNeighborZMinus, x, y, 15);
                                    } else {
                                        neighborBlock = blockData[posToIndex(x, y, z - 1)];
                                    }

                                    if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
                                        //back
                                        positions.put(positionsCounter, 0f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + z);
                                        positionsCounter++;

                                        //back
                                        if (z - 1 < 0) {
                                            lightValue = getNeighborLight(chunkNeighborZMinus, x, y, 15);
                                        } else {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z - 1)]);
                                        }

                                        lightValue = convertLight(lightValue / maxLight);
                                        //back
                                        for (i = 0; i < 12; i++) {
                                            light.put(lightCounter, lightValue);
                                            lightCounter++;
                                        }

                                        //back
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 1 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 3 + indicesCount);
                                        indicesCounter++;

                                        indicesCount += 4;

                                        textureWorker = getBackTexturePoints(thisBlock, thisRotation);
                                        //back
                                        textureCoord.put(textureCoordCounter, textureWorker[1]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[2]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[0]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[2]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[0]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[3]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[1]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[3]);
                                        textureCoordCounter++;
                                    }

                                    if (x + 1 > 15) {
                                        neighborBlock = getNeighborBlock(chunkNeighborXPlus, 0, y, z);
                                    } else {
                                        neighborBlock = blockData[posToIndex(x + 1, y, z)];
                                    }

                                    if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
                                        //right
                                        positions.put(positionsCounter, 1f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + z);
                                        positionsCounter++;

                                        //right
                                        if (x + 1 > 15) {
                                            lightValue = getNeighborLight(chunkNeighborXPlus, 0, y, z);
                                        } else {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x + 1, y, z)]);
                                        }

                                        lightValue = convertLight(lightValue / maxLight);
                                        //right
                                        for (i = 0; i < 12; i++) {
                                            light.put(lightCounter, lightValue);
                                            lightCounter++;
                                        }

                                        //right
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 1 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 3 + indicesCount);
                                        indicesCounter++;

                                        indicesCount += 4;

                                        textureWorker = getRightTexturePoints(thisBlock, thisRotation);
                                        //right
                                        textureCoord.put(textureCoordCounter, textureWorker[1]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[2]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[0]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[2]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[0]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[3]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[1]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[3]);
                                        textureCoordCounter++;
                                    }

                                    if (x - 1 < 0) {
                                        neighborBlock = getNeighborBlock(chunkNeighborXMinus, 15, y, z);
                                    } else {
                                        neighborBlock = blockData[posToIndex(x - 1, y, z)];
                                    }

                                    if (neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
                                        //left
                                        positions.put(positionsCounter, 0f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + z);
                                        positionsCounter++;

                                        //left
                                        if (x - 1 < 0) {
                                            lightValue = getNeighborLight(chunkNeighborXMinus, 15, y, z);
                                        } else {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x - 1, y, z)]);
                                        }

                                        lightValue = convertLight(lightValue / maxLight);
                                        //left
                                        for (i = 0; i < 12; i++) {
                                            light.put(lightCounter, lightValue);
                                            lightCounter++;
                                        }

                                        //left
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 1 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 3 + indicesCount);
                                        indicesCounter++;

                                        indicesCount += 4;

                                        textureWorker = getLeftTexturePoints(thisBlock, thisRotation);
                                        //left
                                        textureCoord.put(textureCoordCounter, textureWorker[1]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[2]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[0]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[2]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[0]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[3]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[1]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[3]);
                                        textureCoordCounter++;
                                    }

                                    if (y + 1 < 128) {
                                        neighborBlock = blockData[posToIndex(x, y + 1, z)];
                                    }

                                    if (y == 127 || neighborBlock > -1 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
                                        //top
                                        positions.put(positionsCounter, 0f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + z);
                                        positionsCounter++;

                                        //top
                                        if (y + 1 < 128) {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y + 1, z)]);
                                        } else {
                                            lightValue = maxLight;
                                        }

                                        lightValue = convertLight(lightValue / maxLight);

                                        //top
                                        for (i = 0; i < 12; i++) {
                                            light.put(lightCounter, lightValue);
                                            lightCounter++;
                                        }

                                        //top
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 1 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 3 + indicesCount);
                                        indicesCounter++;

                                        indicesCount += 4;

                                        textureWorker = getTopTexturePoints(thisBlock);
                                        //top
                                        textureCoord.put(textureCoordCounter, textureWorker[1]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[2]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[0]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[2]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[0]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[3]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[1]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[3]);
                                        textureCoordCounter++;
                                    }

                                    if (y - 1 > 0) {
                                        neighborBlock = blockData[posToIndex(x, y - 1, z)];
                                    }

                                    if (y != 0 && neighborBlock >= 0 && (getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock))) {
                                        //bottom
                                        positions.put(positionsCounter, 0f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 0f + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, 1f + z);
                                        positionsCounter++;

                                        //bottom
                                        if (y - 1 > 0) {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y - 1, z)]);
                                        } else {
                                            lightValue = 0;
                                        }

                                        lightValue = convertLight(lightValue / maxLight);
                                        //bottom
                                        for (i = 0; i < 12; i++) {
                                            light.put(lightCounter, lightValue);
                                            lightCounter++;
                                        }

                                        //bottom
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 1 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 3 + indicesCount);
                                        indicesCounter++;

                                        indicesCount += 4;

                                        textureWorker = getBottomTexturePoints(thisBlock);
                                        //bottom
                                        textureCoord.put(textureCoordCounter, textureWorker[1]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[2]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[0]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[2]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[0]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[3]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[1]);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, textureWorker[3]);
                                        textureCoordCounter++;
                                    }
                                }
                                break;
                                //todo --------------------------------------- THE ALLFACES DRAWTYPE
                                case 4: {

                                    {
                                        //front
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + z);
                                        allFacesPositionsCounter++;

                                        //front
                                        if (z + 1 > 15) {
                                            lightValue = getNeighborLight(chunkNeighborZPlus, x, y, 0);
                                        } else {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z + 1)]);
                                        }

                                        lightValue = convertLight(lightValue / maxLight);

                                        //front
                                        for (i = 0; i < 12; i++) {
                                            allFacesLight.put(allFacesLightCounter, lightValue);
                                            allFacesLightCounter++;
                                        }


                                        //front
                                        allFacesIndices.put(allFacesIndicesCounter, allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 1 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 2 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 2 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 3 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;

                                        allFacesIndicesCount += 4;

                                        textureWorker = getFrontTexturePoints(thisBlock, thisRotation);
                                        //front
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[1]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[2]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[0]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[2]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[0]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[3]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[1]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[3]);
                                        allFacesTextureCoordCounter++;
                                    }

                                    {
                                        //back
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + z);
                                        allFacesPositionsCounter++;

                                        //back

                                        if (z - 1 < 0) {
                                            lightValue = getNeighborLight(chunkNeighborZMinus, x, y, 15);
                                        } else {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z - 1)]);
                                        }

                                        lightValue = convertLight(lightValue / maxLight);
                                        //back
                                        for (i = 0; i < 12; i++) {
                                            allFacesLight.put(allFacesLightCounter, lightValue);
                                            allFacesLightCounter++;
                                        }

                                        //back
                                        allFacesIndices.put(allFacesIndicesCounter, allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 1 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 2 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 2 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 3 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;

                                        allFacesIndicesCount += 4;

                                        textureWorker = getBackTexturePoints(thisBlock, thisRotation);
                                        //back
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[1]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[2]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[0]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[2]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[0]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[3]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[1]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[3]);
                                        allFacesTextureCoordCounter++;
                                    }

                                    {
                                        //right
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + z);
                                        allFacesPositionsCounter++;

                                        //right

                                        if (x + 1 > 15) {
                                            lightValue = getNeighborLight(chunkNeighborXPlus, 0, y, z);
                                        } else {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x + 1, y, z)]);
                                        }

                                        lightValue = convertLight(lightValue / maxLight);
                                        //right
                                        for (i = 0; i < 12; i++) {
                                            allFacesLight.put(allFacesLightCounter, lightValue);
                                            allFacesLightCounter++;
                                        }

                                        //right
                                        allFacesIndices.put(allFacesIndicesCounter, allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 1 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 2 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 2 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 3 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;

                                        allFacesIndicesCount += 4;

                                        textureWorker = getRightTexturePoints(thisBlock, thisRotation);
                                        //right
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[1]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[2]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[0]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[2]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[0]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[3]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[1]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[3]);
                                        allFacesTextureCoordCounter++;
                                    }

                                    {
                                        //left
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + z);
                                        allFacesPositionsCounter++;

                                        //left

                                        if (x - 1 < 0) {
                                            lightValue = getNeighborLight(chunkNeighborXMinus, 15, y, z);
                                        } else {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x - 1, y, z)]);
                                        }

                                        lightValue = convertLight(lightValue / maxLight);
                                        //left
                                        for (i = 0; i < 12; i++) {
                                            allFacesLight.put(allFacesLightCounter, lightValue);
                                            allFacesLightCounter++;
                                        }

                                        //left
                                        allFacesIndices.put(allFacesIndicesCounter, allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 1 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 2 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 2 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 3 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;

                                        allFacesIndicesCount += 4;

                                        textureWorker = getLeftTexturePoints(thisBlock, thisRotation);
                                        //left
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[1]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[2]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[0]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[2]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[0]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[3]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[1]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[3]);
                                        allFacesTextureCoordCounter++;
                                    }

                                    {
                                        //top
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + z);
                                        allFacesPositionsCounter++;

                                        //top

                                        if (y + 1 < 128) {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y + 1, z)]);
                                        } else {
                                            lightValue = maxLight;
                                        }

                                        lightValue = convertLight(lightValue / maxLight);

                                        //top
                                        for (i = 0; i < 12; i++) {
                                            allFacesLight.put(allFacesLightCounter, lightValue);
                                            allFacesLightCounter++;
                                        }

                                        //top
                                        allFacesIndices.put(allFacesIndicesCounter, allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 1 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 2 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 2 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 3 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;

                                        allFacesIndicesCount += 4;

                                        textureWorker = getTopTexturePoints(thisBlock);
                                        //top
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[1]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[2]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[0]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[2]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[0]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[3]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[1]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[3]);
                                        allFacesTextureCoordCounter++;
                                    }


                                    if (y != 0) {
                                        //bottom
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + z);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + x);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 0f + y);
                                        allFacesPositionsCounter++;
                                        allFacesPositions.put(allFacesPositionsCounter, 1f + z);
                                        allFacesPositionsCounter++;

                                        //bottom

                                        if (y - 1 > 0) {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y - 1, z)]);
                                        } else {
                                            lightValue = 0;
                                        }

                                        lightValue = convertLight(lightValue / maxLight);
                                        //bottom
                                        for (i = 0; i < 12; i++) {
                                            allFacesLight.put(allFacesLightCounter, lightValue);
                                            allFacesLightCounter++;
                                        }

                                        //bottom
                                        allFacesIndices.put(allFacesIndicesCounter, allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 1 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 2 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 2 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;
                                        allFacesIndices.put(allFacesIndicesCounter, 3 + allFacesIndicesCount);
                                        allFacesIndicesCounter++;

                                        allFacesIndicesCount += 4;

                                        textureWorker = getBottomTexturePoints(thisBlock);
                                        //bottom
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[1]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[2]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[0]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[2]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[0]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[3]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[1]);
                                        allFacesTextureCoordCounter++;
                                        allFacesTextureCoord.put(allFacesTextureCoordCounter, textureWorker[3]);
                                        allFacesTextureCoordCounter++;
                                    }
                                }
                                break;
                                //TODO: ------------------------------------- TORCHLIKE DRAWTYPE

                                case 7: {


                                    System.out.println("running!");

                                    //yes, this is a manually constructed model

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

                                    textureWorker = getFrontTexturePoints(thisBlock, thisRotation);


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
                                    {
                                        //z is the constant
                                        //front
                                        positions.put(positionsCounter, tfr.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, tfr.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, tfr.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, tfl.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, tfl.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, tfl.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfl.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfl.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfl.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfr.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfr.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfr.z + z);
                                        positionsCounter++;

                                        //front
                                        if (z + 1 > 15) {
                                            lightValue = getNeighborLight(chunkNeighborZPlus, x, y, 0);
                                        } else {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z + 1)]);
                                        }

                                        lightValue = convertLight(lightValue / maxLight);

                                        //front
                                        for (i = 0; i < 12; i++) {
                                            light.put(lightCounter, lightValue);
                                            lightCounter++;
                                        }

                                        //front
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 1 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 3 + indicesCount);
                                        indicesCounter++;

                                        indicesCount += 4;


                                        //front
                                        textureCoord.put(textureCoordCounter, sizeXHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeYLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeXLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeYLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeXLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeYHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeXHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeYHigh);
                                        textureCoordCounter++;
                                    }


                                    //back
                                    {
                                        //z is the constant
                                        //back
                                        positions.put(positionsCounter, trl.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, trl.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, trl.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, trr.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, trr.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, trr.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, brr.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, brr.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, brr.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, brl.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, brl.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, brl.z + z);
                                        positionsCounter++;

                                        //back

                                        if (z - 1 < 0) {
                                            lightValue = getNeighborLight(chunkNeighborZMinus, x, y, 15);
                                        } else {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z - 1)]);
                                        }

                                        lightValue = convertLight(lightValue / maxLight);
                                        //back
                                        for (i = 0; i < 12; i++) {
                                            light.put(lightCounter, lightValue);
                                            lightCounter++;
                                        }

                                        //back
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 1 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 3 + indicesCount);
                                        indicesCounter++;

                                        indicesCount += 4;


                                        //back
                                        textureCoord.put(textureCoordCounter, sizeXHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeYLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeXLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeYLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeXLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeYHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeXHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeYHigh);
                                        textureCoordCounter++;
                                    }


                                    {
                                        //x is the constant
                                        //right
                                        positions.put(positionsCounter, trr.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, trr.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, trr.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, tfr.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, tfr.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, tfr.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfr.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfr.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfr.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, brr.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, brr.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, brr.z + z);
                                        positionsCounter++;

                                        //right

                                        if (x + 1 > 15) {
                                            lightValue = getNeighborLight(chunkNeighborXPlus, 0, y, z);
                                        } else {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x + 1, y, z)]);
                                        }

                                        lightValue = convertLight(lightValue / maxLight);
                                        //right
                                        for (i = 0; i < 12; i++) {
                                            light.put(lightCounter, lightValue);
                                            lightCounter++;
                                        }

                                        //right
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 1 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 3 + indicesCount);
                                        indicesCounter++;

                                        indicesCount += 4;

                                        //right
                                        textureCoord.put(textureCoordCounter, sizeXHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeYLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeXLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeYLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeXLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeYHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeXHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeYHigh);
                                        textureCoordCounter++;
                                    }


                                    {
                                        //x is the constant
                                        //left
                                        positions.put(positionsCounter, tfl.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, tfl.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, tfl.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, trl.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, trl.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, trl.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, brl.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, brl.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, brl.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfl.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfl.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfl.z + z);
                                        positionsCounter++;

                                        //left
                                        if (x - 1 < 0) {
                                            lightValue = getNeighborLight(chunkNeighborXMinus, 15, y, z);
                                        } else {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x - 1, y, z)]);
                                        }

                                        lightValue = convertLight(lightValue / maxLight);
                                        //left
                                        for (i = 0; i < 12; i++) {
                                            light.put(lightCounter, lightValue);
                                            lightCounter++;
                                        }

                                        //left
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 1 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 3 + indicesCount);
                                        indicesCounter++;

                                        indicesCount += 4;

                                        //left
                                        textureCoord.put(textureCoordCounter, sizeXHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeYLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeXLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeYLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeXLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeYHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeXHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, sizeYHigh);
                                        textureCoordCounter++;
                                    }

                                    {
                                        //y is constant
                                        //top
                                        positions.put(positionsCounter, trl.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, trl.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, trl.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, tfl.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, tfl.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, tfl.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, tfr.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, tfr.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, tfr.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, trr.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, trr.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, trr.z + z);
                                        positionsCounter++;

                                        //top
                                        if (y + 1 < 128) {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y + 1, z)]);
                                        } else {
                                            lightValue = maxLight;
                                        }

                                        lightValue = convertLight(lightValue / maxLight);

                                        //top
                                        for (i = 0; i < 12; i++) {
                                            light.put(lightCounter, lightValue);
                                            lightCounter++;
                                        }

                                        //top
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 1 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 3 + indicesCount);
                                        indicesCounter++;

                                        indicesCount += 4;

                                        //top
                                        textureCoord.put(textureCoordCounter, topSizeXHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, topSizeYLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, topSizeXLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, topSizeYLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, topSizeXLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, topSizeYHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, topSizeXHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, topSizeYHigh);
                                        textureCoordCounter++;
                                    }


                                    {
                                        //y is constant
                                        //bottom
                                        positions.put(positionsCounter, brl.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, brl.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, brl.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, brr.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, brr.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, brr.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfr.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfr.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfr.z + z);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfl.x + x);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfl.y + y);
                                        positionsCounter++;
                                        positions.put(positionsCounter, bfl.z + z);
                                        positionsCounter++;

                                        //bottom
                                        if (y - 1 > 0) {
                                            lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y - 1, z)]);
                                        } else {
                                            lightValue = 0;
                                        }

                                        lightValue = convertLight(lightValue / maxLight);
                                        //bottom
                                        for (i = 0; i < 12; i++) {
                                            light.put(lightCounter, lightValue);
                                            lightCounter++;
                                        }

                                        //bottom
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 1 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 2 + indicesCount);
                                        indicesCounter++;
                                        indices.put(indicesCounter, 3 + indicesCount);
                                        indicesCounter++;

                                        indicesCount += 4;

                                        //bottom
                                        textureCoord.put(textureCoordCounter, bottomSizeXHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, bottomSizeYLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, bottomSizeXLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, bottomSizeYLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, bottomSizeXLow);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, bottomSizeYHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, bottomSizeXHigh);
                                        textureCoordCounter++;
                                        textureCoord.put(textureCoordCounter, bottomSizeYHigh);
                                        textureCoordCounter++;
                                    }

                                } //end of case 7
                                break;
                            } //end of switch

                        /*
                        }//REMOVE THIS BRACKET BOI

                                //todo: ---------------------------------------------------------- the block box draw type
                            }else {
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
                                        lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z + 1)]);
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
                                        lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y, z - 1)]);
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
                                        lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x + 1, y, z)]);
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
                                        lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x - 1, y, z)]);
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
                                        lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y + 1, z)]);
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
                                        lightValue = calculateBlockLight(chunkLightLevel, lightData[posToIndex(x, y - 1, z)]);
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

                         */
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


            if (positionsCounter > 0) {
                //pass data to container object
                newChunkData.positionsArray    = positions.values().toFloatArray();
                newChunkData.lightArray        = light.values().toFloatArray();
                newChunkData.indicesArray      = indices.values().toIntArray();
                newChunkData.textureCoordArray = textureCoord.values().toFloatArray();
            } else {
                //inform the container object that this chunk is null for this part of it
                newChunkData.normalMeshIsNull = true;
            }

            /*
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

             */



            if (allFacesPositionsCounter > 0) {
                //pass data to container object
                newChunkData.allFacesPositionsArray = allFacesPositions.values().toFloatArray();
                newChunkData.allFacesLightArray = allFacesLight.values().toFloatArray();
                newChunkData.allFacesIndicesArray = allFacesIndices.values().toIntArray();
                newChunkData.allFacesTextureCoordArray = allFacesTextureCoord.values().toFloatArray();
            } else {
                //inform the container object that this chunk is null for this part of it
                newChunkData.allFacesMeshIsNull = true;
            }



            //clear linked lists
            positions.clear();
            textureCoord.clear();
            indices.clear();
            light.clear();

            /*
            liquidPositions.clear();
            liquidTextureCoord.clear();
            liquidIndices.clear();
            liquidLight.clear();
             */
            allFacesPositions.clear();
            allFacesTextureCoord.clear();
            allFacesIndices.clear();
            allFacesLight.clear();


            //finally add it into the queue to be popped
            addToChunkMeshQueue(newChunkData);
        }
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

}