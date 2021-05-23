package game.chunk;

import engine.graph.Mesh;
import engine.graph.Texture;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static engine.Time.getDelta;
import static engine.settings.Settings.getSettingsChunkLoad;
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

    //the higher this is set, the lazier chunk mesh loading gets
    //set it too high, and chunk mesh loading barely works
    private static final float[] goalTimerArray = new float[]{
            0.05f, //SNAIL
            0.025f, //SLOWER
            0.009f, //NORMAL
            0.004f, //FASTER
            0.002f, //INSANE
            0.0005f, //FUTURE PC
    };

    private static float goalTimer = goalTimerArray[getSettingsChunkLoad()];

    public static void updateChunkMeshLoadingSpeed() {
        goalTimer = goalTimerArray[getSettingsChunkLoad()];
    }

    private static float chunkUpdateTimer = 0;

    public static void popChunkMeshQueue(){

        chunkUpdateTimer += getDelta();
        int updateAmount = 0;

        if (chunkUpdateTimer >= goalTimer){
            updateAmount = (int)(Math.ceil(chunkUpdateTimer / goalTimer));
            chunkUpdateTimer = 0;
        }

        for (int i = 0; i < updateAmount; i++) {
            if (!queue.isEmpty()) {
                Object[] queueAsArray = queue.keySet().toArray();
                String thisKey = (String) queueAsArray[random.nextInt(queueAsArray.length)];

                ChunkMeshDataObject newChunkMeshData = queue.get(thisKey);

                String keyName = newChunkMeshData.chunkX + " " + newChunkMeshData.chunkZ + " " + newChunkMeshData.yHeight;

                queue.remove(keyName);

                if (!newChunkMeshData.normalMeshIsNull) {
                    setChunkNormalMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, new Mesh(newChunkMeshData.positionsArray, newChunkMeshData.lightArray, newChunkMeshData.indicesArray, newChunkMeshData.textureCoordArray, textureAtlas));
                } else {
                    setChunkNormalMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, null);
                }

                if (!newChunkMeshData.liquidMeshIsNull) {
                    setChunkLiquidMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, new Mesh(newChunkMeshData.liquidPositionsArray, newChunkMeshData.liquidLightArray, newChunkMeshData.liquidIndicesArray, newChunkMeshData.liquidTextureCoordArray, textureAtlas));
                } else {
                    setChunkLiquidMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, null);
                }

                if (!newChunkMeshData.allFacesMeshIsNull) {
                    setChunkAllFacesMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, new Mesh(newChunkMeshData.allFacesPositionsArray, newChunkMeshData.allFacesLightArray, newChunkMeshData.allFacesIndicesArray, newChunkMeshData.allFacesTextureCoordArray, textureAtlas));
                } else {
                    setChunkAllFacesMesh(newChunkMeshData.chunkX, newChunkMeshData.chunkZ, newChunkMeshData.yHeight, null);
                }
            }
        }
    }

    public static void generateChunkMesh(int chunkX, int chunkZ, int yHeight) {
        //long startTime = System.nanoTime();
        //let's use all the cpu threads to the limit
        new Thread(() -> {

            ChunkObject thisChunk = getChunk(chunkX, chunkZ);

            //don't bother if the chunk doesn't exist
            if (thisChunk == null) {
                return;
            }

            //normal block stuff
            final List<Float> positions = new ArrayList<>();
            final List<Float> textureCoord = new ArrayList<>();
            final List<Integer> indices = new ArrayList<>();
            final List<Float> light = new ArrayList<>();
            int indicesCount = 0; //this is needed to create tris

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

            //allFaces block stuff

            final float[] allFacesPositions = new float[50_824];
            int allFacesPositionsCount = 0;

            final float[] allFacesTextureCoord = new float[50_824];
            int allFacesTextureCoordCount = 0;

            final int[] allFacesIndices = new int[50_824];
            int allFacesIndicesTableCount = 0;
            int allFacesIndicesCount = 0;

            final float[] allFacesLight = new float[50_824];
            int allFacesLightCount = 0;

            //cache data
            int thisBlock;
            byte thisRotation;
            int realX;
            int realZ;
            float lightValue;

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
                                    liquidPositions[liquidPositionsCount] = (1f + x);
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
                                    liquidIndices[liquidIndicesTableCount] = (liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 1] = (1 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 2] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 3] = (liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 4] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 5] = (3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;
                                    liquidIndicesTableCount += 6;

                                    float[] textureFront = getFrontTexturePoints(thisBlock, thisRotation);
                                    //front
                                    liquidTextureCoord[liquidTextureCoordCount] = (textureFront[1]);
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
                                    liquidPositions[liquidPositionsCount] = (0f + x);
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
                                    liquidIndices[liquidIndicesTableCount] = (liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 1] = (1 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 2] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 3] = (liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 4] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 5] = (3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;
                                    liquidIndicesTableCount += 6;

                                    float[] textureBack = getBackTexturePoints(thisBlock, thisRotation);
                                    //back
                                    liquidTextureCoord[liquidTextureCoordCount] = (textureBack[1]);
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
                                    liquidPositions[liquidPositionsCount] = (1f + x);
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
                                    liquidIndices[liquidIndicesTableCount] = (liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 1] = (1 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 2] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 3] = (liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 4] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 5] = (3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;
                                    liquidIndicesTableCount += 6;

                                    float[] textureRight = getRightTexturePoints(thisBlock, thisRotation);
                                    //right
                                    liquidTextureCoord[liquidTextureCoordCount] = (textureRight[1]);
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
                                    liquidPositions[liquidPositionsCount] = (0f + x);
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
                                    liquidIndices[liquidIndicesTableCount] = (liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 1] = (1 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 2] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 3] = (liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 4] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 5] = (3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;
                                    liquidIndicesTableCount += 6;

                                    float[] textureLeft = getLeftTexturePoints(thisBlock, thisRotation);
                                    //left
                                    liquidTextureCoord[liquidTextureCoordCount] = (textureLeft[1]);
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
                                    liquidPositions[liquidPositionsCount] = (0f + x);
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
                                    liquidIndices[liquidIndicesTableCount] = (liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 1] = (1 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 2] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 3] = (liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 4] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 5] = (3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;
                                    liquidIndicesTableCount += 6;

                                    float[] textureTop = getTopTexturePoints(thisBlock);
                                    //top
                                    liquidTextureCoord[liquidTextureCoordCount] = (textureTop[1]);
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
                                    liquidPositions[liquidPositionsCount] = (0f + x);
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
                                    liquidIndices[liquidIndicesTableCount] = (liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 1] = (1 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 2] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 3] = (liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 4] = (2 + liquidIndicesCount);
                                    liquidIndices[liquidIndicesTableCount + 5] = (3 + liquidIndicesCount);
                                    liquidIndicesCount += 4;
                                    liquidIndicesTableCount += 6;

                                    float[] textureBottom = getBottomTexturePoints(thisBlock);
                                    //bottom
                                    liquidTextureCoord[liquidTextureCoordCount] = (textureBottom[1]);
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
                                        lightValue = getLight(realX, y, realZ + 1);
                                    } else {
                                        lightValue = thisChunk.light[posToIndex(x, y, z + 1)];
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

                                    float[] textureFront = getFrontTexturePoints(thisBlock, thisRotation);
                                    //front
                                    textureCoord.add(textureFront[1]);
                                    textureCoord.add(textureFront[2]);
                                    textureCoord.add(textureFront[0]);
                                    textureCoord.add(textureFront[2]);
                                    textureCoord.add(textureFront[0]);
                                    textureCoord.add(textureFront[3]);
                                    textureCoord.add(textureFront[1]);
                                    textureCoord.add(textureFront[3]);
                                }

                                if (z - 1 < 0) {
                                    neighborBlock = getBlock(realX, y, realZ - 1);
                                } else {
                                    neighborBlock = thisChunk.block[posToIndex(x, y, z - 1)];
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
                                        lightValue = getLight(realX, y, realZ - 1);
                                    } else {
                                        lightValue = thisChunk.light[posToIndex(x, y, z - 1)];
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

                                    float[] textureBack = getBackTexturePoints(thisBlock, thisRotation);
                                    //back
                                    textureCoord.add(textureBack[1]);
                                    textureCoord.add(textureBack[2]);
                                    textureCoord.add(textureBack[0]);
                                    textureCoord.add(textureBack[2]);
                                    textureCoord.add(textureBack[0]);
                                    textureCoord.add(textureBack[3]);
                                    textureCoord.add(textureBack[1]);
                                    textureCoord.add(textureBack[3]);
                                }

                                if (x + 1 > 15) {
                                    neighborBlock = getBlock(realX + 1, y, realZ);
                                } else {
                                    neighborBlock = thisChunk.block[posToIndex(x + 1, y, z)];
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
                                        lightValue = getLight(realX + 1, y, realZ);
                                    } else {
                                        lightValue = thisChunk.light[posToIndex(x + 1, y, z)];
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

                                    float[] textureRight = getRightTexturePoints(thisBlock, thisRotation);
                                    //right
                                    textureCoord.add(textureRight[1]);
                                    textureCoord.add(textureRight[2]);
                                    textureCoord.add(textureRight[0]);
                                    textureCoord.add(textureRight[2]);
                                    textureCoord.add(textureRight[0]);
                                    textureCoord.add(textureRight[3]);
                                    textureCoord.add(textureRight[1]);
                                    textureCoord.add(textureRight[3]);
                                }

                                if (x - 1 < 0) {
                                    neighborBlock = getBlock(realX - 1, y, realZ);
                                } else {
                                    neighborBlock = thisChunk.block[posToIndex(x - 1, y, z)];
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
                                        lightValue = getLight(realX - 1, y, realZ);
                                    } else {
                                        lightValue = thisChunk.light[posToIndex(x - 1, y, z)];
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

                                    float[] textureLeft = getLeftTexturePoints(thisBlock, thisRotation);
                                    //left
                                    textureCoord.add(textureLeft[1]);
                                    textureCoord.add(textureLeft[2]);
                                    textureCoord.add(textureLeft[0]);
                                    textureCoord.add(textureLeft[2]);
                                    textureCoord.add(textureLeft[0]);
                                    textureCoord.add(textureLeft[3]);
                                    textureCoord.add(textureLeft[1]);
                                    textureCoord.add(textureLeft[3]);
                                }

                                if (y + 1 < 128) {
                                    neighborBlock = thisChunk.block[posToIndex(x, y + 1, z)];
                                }

                                if (y == 127 || neighborBlock > -1 && ((neighborBlock >= 0 && getBlockDrawType(neighborBlock) != 1 || getIfLiquid(neighborBlock)))) {
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
                                        lightValue = thisChunk.light[posToIndex(x, y + 1, z)];
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

                                    float[] textureTop = getTopTexturePoints(thisBlock);
                                    //top
                                    textureCoord.add(textureTop[1]);
                                    textureCoord.add(textureTop[2]);
                                    textureCoord.add(textureTop[0]);
                                    textureCoord.add(textureTop[2]);
                                    textureCoord.add(textureTop[0]);
                                    textureCoord.add(textureTop[3]);
                                    textureCoord.add(textureTop[1]);
                                    textureCoord.add(textureTop[3]);
                                }

                                if (y - 1 > 0) {
                                    neighborBlock = thisChunk.block[posToIndex(x, y - 1, z)];
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
                                        lightValue = thisChunk.light[posToIndex(x, y - 1, z)];
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

                                    float[] textureBottom = getBottomTexturePoints(thisBlock);
                                    //bottom
                                    textureCoord.add(textureBottom[1]);
                                    textureCoord.add(textureBottom[2]);
                                    textureCoord.add(textureBottom[0]);
                                    textureCoord.add(textureBottom[2]);
                                    textureCoord.add(textureBottom[0]);
                                    textureCoord.add(textureBottom[3]);
                                    textureCoord.add(textureBottom[1]);
                                    textureCoord.add(textureBottom[3]);
                                }

                            }
                            //todo --------------------------------------- THE ALLFACES DRAWTYPE
                            else if (getBlockDrawType(thisBlock) == 4) {
                                {
                                    //front
                                    allFacesPositions[allFacesPositionsCount] = (1f + x);
                                    allFacesPositions[allFacesPositionsCount + 1] = (1f + y);
                                    allFacesPositions[allFacesPositionsCount + 2] = (1f + z);

                                    allFacesPositions[allFacesPositionsCount + 3] = (0f + x);
                                    allFacesPositions[allFacesPositionsCount + 4] = (1f + y);
                                    allFacesPositions[allFacesPositionsCount + 5] = (1f + z);

                                    allFacesPositions[allFacesPositionsCount + 6] = (0f + x);
                                    allFacesPositions[allFacesPositionsCount + 7] = (0f + y);
                                    allFacesPositions[allFacesPositionsCount + 8] = (1f + z);

                                    allFacesPositions[allFacesPositionsCount + 9] = (1f + x);
                                    allFacesPositions[allFacesPositionsCount + 10] = (0f + y);
                                    allFacesPositions[allFacesPositionsCount + 11] = (1f + z);

                                    allFacesPositionsCount += 12;

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
                                       allFacesLight[i + allFacesLightCount] = (frontLight);
                                    }

                                    allFacesLightCount += 12;


                                    //front
                                    allFacesIndices[allFacesIndicesTableCount] = (allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 1] = (1 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 2] = (2 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 3] = (allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 4] = (2 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 5] = (3 + allFacesIndicesCount);

                                    allFacesIndicesCount += 4;
                                    allFacesIndicesTableCount += 6;

                                    float[] textureFront = getFrontTexturePoints(thisBlock, thisRotation);
                                    //front
                                    allFacesTextureCoord[allFacesTextureCoordCount] = (textureFront[1]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 1] = (textureFront[2]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 2] = (textureFront[0]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 3] = (textureFront[2]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 4] = (textureFront[0]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 5] = (textureFront[3]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 6] = (textureFront[1]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 7] = (textureFront[3]);
                                    allFacesTextureCoordCount += 8;
                                }

                                {
                                    //back
                                    allFacesPositions[allFacesPositionsCount] = (0f + x);
                                    allFacesPositions[allFacesPositionsCount + 1] = (1f + y);
                                    allFacesPositions[allFacesPositionsCount + 2] = (0f + z);

                                    allFacesPositions[allFacesPositionsCount + 3] = (1f + x);
                                    allFacesPositions[allFacesPositionsCount + 4] = (1f + y);
                                    allFacesPositions[allFacesPositionsCount + 5] = (0f + z);

                                    allFacesPositions[allFacesPositionsCount + 6] = (1f + x);
                                    allFacesPositions[allFacesPositionsCount + 7] = (0f + y);
                                    allFacesPositions[allFacesPositionsCount + 8] = (0f + z);

                                    allFacesPositions[allFacesPositionsCount + 9] = (0f + x);
                                    allFacesPositions[allFacesPositionsCount + 10] = (0f + y);
                                    allFacesPositions[allFacesPositionsCount + 11] = (0f + z);

                                    allFacesPositionsCount += 12;

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
                                       allFacesLight[i + allFacesLightCount] = (backLight);
                                    }

                                    allFacesLightCount += 12;

                                    //back
                                    allFacesIndices[allFacesIndicesTableCount] = (allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 1] = (1 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 2] = (2 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 3] = (allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 4] = (2 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 5] = (3 + allFacesIndicesCount);
                                    allFacesIndicesCount += 4;
                                    allFacesIndicesTableCount += 6;

                                    float[] textureBack = getBackTexturePoints(thisBlock, thisRotation);
                                    //back
                                    allFacesTextureCoord[allFacesTextureCoordCount] = (textureBack[1]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 1] = (textureBack[2]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 2] = (textureBack[0]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 3] = (textureBack[2]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 4] = (textureBack[0]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 5] = (textureBack[3]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 6] = (textureBack[1]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 7] = (textureBack[3]);
                                    allFacesTextureCoordCount += 8;
                                }

                                {
                                    //right
                                    allFacesPositions[allFacesPositionsCount] = (1f + x);
                                    allFacesPositions[allFacesPositionsCount + 1] = (1f + y);
                                    allFacesPositions[allFacesPositionsCount + 2] = (0f + z);

                                    allFacesPositions[allFacesPositionsCount + 3] = (1f + x);
                                    allFacesPositions[allFacesPositionsCount + 4] = (1f + y);
                                    allFacesPositions[allFacesPositionsCount + 5] = (1f + z);

                                    allFacesPositions[allFacesPositionsCount + 6] = (1f + x);
                                    allFacesPositions[allFacesPositionsCount + 7] = (0f + y);
                                    allFacesPositions[allFacesPositionsCount + 8] = (1f + z);

                                    allFacesPositions[allFacesPositionsCount + 9] = (1f + x);
                                    allFacesPositions[allFacesPositionsCount + 10] = (0f + y);
                                    allFacesPositions[allFacesPositionsCount + 11] = (0f + z);

                                    allFacesPositionsCount += 12;

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
                                       allFacesLight[i + allFacesLightCount] = (rightLight);
                                    }

                                    allFacesLightCount += 12;

                                    //right
                                    allFacesIndices[allFacesIndicesTableCount] = (allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 1] = (1 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 2] = (2 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 3] = (allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 4] = (2 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 5] = (3 + allFacesIndicesCount);
                                    allFacesIndicesCount += 4;
                                    allFacesIndicesTableCount += 6;

                                    float[] textureRight = getRightTexturePoints(thisBlock, thisRotation);
                                    //right
                                    allFacesTextureCoord[allFacesTextureCoordCount] = (textureRight[1]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 1] = (textureRight[2]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 2] = (textureRight[0]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 3] = (textureRight[2]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 4] = (textureRight[0]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 5] = (textureRight[3]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 6] = (textureRight[1]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 7] = (textureRight[3]);
                                    allFacesTextureCoordCount += 8;
                                }

                                {
                                    //left
                                    allFacesPositions[allFacesPositionsCount] = (0f + x);
                                    allFacesPositions[allFacesPositionsCount + 1] = (1f + y);
                                    allFacesPositions[allFacesPositionsCount + 2] = (1f + z);

                                    allFacesPositions[allFacesPositionsCount + 3] = (0f + x);
                                    allFacesPositions[allFacesPositionsCount + 4] = (1f + y);
                                    allFacesPositions[allFacesPositionsCount + 5] = (0f + z);

                                    allFacesPositions[allFacesPositionsCount + 6] = (0f + x);
                                    allFacesPositions[allFacesPositionsCount + 7] = (0f + y);
                                    allFacesPositions[allFacesPositionsCount + 8] = (0f + z);

                                    allFacesPositions[allFacesPositionsCount + 9] = (0f + x);
                                    allFacesPositions[allFacesPositionsCount + 10] = (0f + y);
                                    allFacesPositions[allFacesPositionsCount + 11] = (1f + z);

                                    allFacesPositionsCount += 12;

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
                                       allFacesLight[i + allFacesLightCount] = (leftLight);
                                    }

                                    allFacesLightCount += 12;

                                    //left
                                    allFacesIndices[allFacesIndicesTableCount] = (allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 1] = (1 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 2] = (2 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 3] = (allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 4] = (2 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 5] = (3 + allFacesIndicesCount);
                                    allFacesIndicesCount += 4;
                                    allFacesIndicesTableCount += 6;

                                    float[] textureLeft = getLeftTexturePoints(thisBlock, thisRotation);
                                    //left
                                    allFacesTextureCoord[allFacesTextureCoordCount] = (textureLeft[1]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 1] = (textureLeft[2]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 2] = (textureLeft[0]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 3] = (textureLeft[2]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 4] = (textureLeft[0]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 5] = (textureLeft[3]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 6] = (textureLeft[1]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 7] = (textureLeft[3]);
                                    allFacesTextureCoordCount += 8;
                                }

                                 {
                                    //top
                                    allFacesPositions[allFacesPositionsCount] = (0f + x);
                                    allFacesPositions[allFacesPositionsCount + 1] = (1f + y);
                                    allFacesPositions[allFacesPositionsCount + 2] = (0f + z);

                                    allFacesPositions[allFacesPositionsCount + 3] = (0f + x);
                                    allFacesPositions[allFacesPositionsCount + 4] = (1f + y);
                                    allFacesPositions[allFacesPositionsCount + 5] = (1f + z);

                                    allFacesPositions[allFacesPositionsCount + 6] = (1f + x);
                                    allFacesPositions[allFacesPositionsCount + 7] = (1f + y);
                                    allFacesPositions[allFacesPositionsCount + 8] = (1f + z);

                                    allFacesPositions[allFacesPositionsCount + 9] = (1f + x);
                                    allFacesPositions[allFacesPositionsCount + 10] = (1f + y);
                                    allFacesPositions[allFacesPositionsCount + 11] = (0f + z);

                                    allFacesPositionsCount += 12;

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
                                       allFacesLight[i + allFacesLightCount] = (topLight);
                                    }

                                    allFacesLightCount += 12;

                                    //top
                                    allFacesIndices[allFacesIndicesTableCount] = (allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 1] = (1 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 2] = (2 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 3] = (allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 4] = (2 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 5] = (3 + allFacesIndicesCount);
                                    allFacesIndicesCount += 4;
                                    allFacesIndicesTableCount += 6;

                                    float[] textureTop = getTopTexturePoints(thisBlock);
                                    //top
                                    allFacesTextureCoord[allFacesTextureCoordCount] = (textureTop[1]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 1] = (textureTop[2]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 2] = (textureTop[0]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 3] = (textureTop[2]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 4] = (textureTop[0]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 5] = (textureTop[3]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 6] = (textureTop[1]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 7] = (textureTop[3]);
                                    allFacesTextureCoordCount += 8;

                                 }


                                if (y != 0) {
                                    //bottom
                                    allFacesPositions[allFacesPositionsCount] = (0f + x);
                                    allFacesPositions[allFacesPositionsCount + 1] = (0f + y);
                                    allFacesPositions[allFacesPositionsCount + 2] = (1f + z);

                                    allFacesPositions[allFacesPositionsCount + 3] = (0f + x);
                                    allFacesPositions[allFacesPositionsCount + 4] = (0f + y);
                                    allFacesPositions[allFacesPositionsCount + 5] = (0f + z);

                                    allFacesPositions[allFacesPositionsCount + 6] = (1f + x);
                                    allFacesPositions[allFacesPositionsCount + 7] = (0f + y);
                                    allFacesPositions[allFacesPositionsCount + 8] = (0f + z);

                                    allFacesPositions[allFacesPositionsCount + 9] = (1f + x);
                                    allFacesPositions[allFacesPositionsCount + 10] = (0f + y);
                                    allFacesPositions[allFacesPositionsCount + 11] = (1f + z);

                                    allFacesPositionsCount += 12;

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
                                       allFacesLight[i + allFacesLightCount] = (bottomLight);
                                    }

                                    allFacesLightCount += 12;

                                    //bottom
                                    allFacesIndices[allFacesIndicesTableCount] = (allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 1] = (1 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 2] = (2 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 3] = (allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 4] = (2 + allFacesIndicesCount);
                                    allFacesIndices[allFacesIndicesTableCount + 5] = (3 + allFacesIndicesCount);
                                    allFacesIndicesCount += 4;
                                    allFacesIndicesTableCount += 6;

                                    float[] textureBottom = getBottomTexturePoints(thisBlock);
                                    //bottom
                                    allFacesTextureCoord[allFacesTextureCoordCount] = (textureBottom[1]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 1] = (textureBottom[2]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 2] = (textureBottom[0]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 3] = (textureBottom[2]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 4] = (textureBottom[0]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 5] = (textureBottom[3]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 6] = (textureBottom[1]);
                                    allFacesTextureCoord[allFacesTextureCoordCount + 7] = (textureBottom[3]);
                                    allFacesTextureCoordCount += 8;
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
                                        lightValue = getLight(realX, y, realZ + 1);
                                    } else {
                                        lightValue = thisChunk.light[posToIndex(x, y, z + 1)];
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

                                    float[] textureFront = getFrontTexturePoints(thisBlock, thisRotation);

                                    //front
                                    textureCoord.add(textureFront[1] - ((1 - (float) thisBlockBox[3]) / 32f)); //x positive
                                    textureCoord.add(textureFront[2] + ((1 - (float) thisBlockBox[4]) / 32f)); //y positive
                                    textureCoord.add(textureFront[0] - ((0 - (float) thisBlockBox[0]) / 32f)); //x negative
                                    textureCoord.add(textureFront[2] + ((1 - (float) thisBlockBox[4]) / 32f)); //y positive

                                    textureCoord.add(textureFront[0] - ((0 - (float) thisBlockBox[0]) / 32f)); //x negative
                                    textureCoord.add(textureFront[3] - (((float) thisBlockBox[1]) / 32f));   //y negative
                                    textureCoord.add(textureFront[1] - ((1 - (float) thisBlockBox[3]) / 32f)); //x positive
                                    textureCoord.add(textureFront[3] - (((float) thisBlockBox[1]) / 32f));   //y negative


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
                                        lightValue = getLight(realX, y, realZ - 1);
                                    } else {
                                        lightValue = thisChunk.light[posToIndex(x, y, z - 1)];
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

                                    float[] textureBack = getBackTexturePoints(thisBlock, thisRotation);

                                    // 0, 1, 2, 3, 4, 5
                                    //-x,-y,-z, x, y, z
                                    // 0, 0, 0, 1, 1, 1

                                    // 0, 1,  2, 3
                                    //-x,+x, -y,+y


                                    //back
                                    textureCoord.add(textureBack[1] - ((1 - (float) thisBlockBox[0]) / 32f));
                                    textureCoord.add(textureBack[2] + ((1 - (float) thisBlockBox[4]) / 32f));
                                    textureCoord.add(textureBack[0] - ((0 - (float) thisBlockBox[3]) / 32f));
                                    textureCoord.add(textureBack[2] + ((1 - (float) thisBlockBox[4]) / 32f));

                                    textureCoord.add(textureBack[0] - ((0 - (float) thisBlockBox[3]) / 32f));
                                    textureCoord.add(textureBack[3] - (((float) thisBlockBox[1]) / 32f));
                                    textureCoord.add(textureBack[1] - ((1 - (float) thisBlockBox[0]) / 32f));
                                    textureCoord.add(textureBack[3] - (((float) thisBlockBox[1]) / 32f));


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
                                        lightValue = getLight(realX + 1, y, realZ);
                                    } else {
                                        lightValue = thisChunk.light[posToIndex(x + 1, y, z)];
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


                                    float[] textureRight = getRightTexturePoints(thisBlock, thisRotation);
                                    //right
                                    textureCoord.add(textureRight[1] - ((1 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord.add(textureRight[2] + ((1 - (float) thisBlockBox[4]) / 32f));
                                    textureCoord.add(textureRight[0] - ((0 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord.add(textureRight[2] + ((1 - (float) thisBlockBox[4]) / 32f));

                                    textureCoord.add(textureRight[0] - ((0 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord.add(textureRight[3] - (((float) thisBlockBox[1]) / 32f));
                                    textureCoord.add(textureRight[1] - ((1 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord.add(textureRight[3] - (((float) thisBlockBox[1]) / 32f));


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
                                        lightValue = getLight(realX - 1, y, realZ);
                                    } else {
                                        lightValue = thisChunk.light[posToIndex(x - 1, y, z)];
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

                                    float[] textureLeft = getLeftTexturePoints(thisBlock, thisRotation);
                                    //left
                                    textureCoord.add(textureLeft[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord.add(textureLeft[2] + ((1 - (float) thisBlockBox[4]) / 32f));
                                    textureCoord.add(textureLeft[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord.add(textureLeft[2] + ((1 - (float) thisBlockBox[4]) / 32f));

                                    textureCoord.add(textureLeft[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord.add(textureLeft[3] - (((float) thisBlockBox[1]) / 32f));
                                    textureCoord.add(textureLeft[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord.add(textureLeft[3] - (((float) thisBlockBox[1]) / 32f));


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
                                        lightValue = thisChunk.light[posToIndex(x, y + 1, z)];
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

                                    float[] textureTop = getTopTexturePoints(thisBlock);
                                    //top
                                    textureCoord.add(textureTop[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord.add(textureTop[2] + ((1 - (float) thisBlockBox[0]) / 32f));
                                    textureCoord.add(textureTop[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord.add(textureTop[2] + ((1 - (float) thisBlockBox[0]) / 32f));

                                    textureCoord.add(textureTop[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord.add(textureTop[3] - (((float) thisBlockBox[3]) / 32f));
                                    textureCoord.add(textureTop[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord.add(textureTop[3] - (((float) thisBlockBox[3]) / 32f));


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
                                        lightValue = thisChunk.light[posToIndex(x, y - 1, z)];
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

                                    float[] textureBottom = getBottomTexturePoints(thisBlock);
                                    //bottom
                                    textureCoord.add(textureBottom[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord.add(textureBottom[2] + ((1 - (float) thisBlockBox[0]) / 32f));
                                    textureCoord.add(textureBottom[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord.add(textureBottom[2] + ((1 - (float) thisBlockBox[0]) / 32f));

                                    textureCoord.add(textureBottom[0] - ((0 - (float) thisBlockBox[2]) / 32f));
                                    textureCoord.add(textureBottom[3] - (((float) thisBlockBox[3]) / 32f));
                                    textureCoord.add(textureBottom[1] - ((1 - (float) thisBlockBox[5]) / 32f));
                                    textureCoord.add(textureBottom[3] - (((float) thisBlockBox[3]) / 32f));
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


            int workerCounter = 0;

            if (positions.size() > 0) {
//        convert the position objects into usable array
                float[] positionsArray = new float[positions.size()];
                for (Float data : positions){
                    //auto casted from Float to float
                    positionsArray[workerCounter] = data;
                    workerCounter++;
                }

                workerCounter = 0;

                //convert the light objects into usable array
                float[] lightArray = new float[light.size()];
                for (Float data : light){
                    //auto casted from Float to float
                    lightArray[workerCounter] = data;
                    workerCounter++;
                }

                workerCounter = 0;

                //convert the indices objects into usable array
                int[] indicesArray = new int[indices.size()];
                for (Integer data : indices){
                    //auto casted from Integer to int
                    indicesArray[workerCounter] = data;
                    workerCounter++;
                }

                workerCounter = 0;

                //convert the textureCoord objects into usable array
                float[] textureCoordArray = new float[textureCoord.size()];
                for (Float data : textureCoord){
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

            if (allFacesPositionsCount > 0) {
//        convert the position objects into usable array
                float[] allFacesPositionsArray = new float[allFacesPositionsCount];
                if (allFacesPositionsCount >= 0) System.arraycopy(allFacesPositions, 0, allFacesPositionsArray, 0, allFacesPositionsCount);

                //convert the light objects into usable array
                float[] allFacesLightArray = new float[allFacesLightCount];
                if (allFacesLightCount >= 0) System.arraycopy(allFacesLight, 0, allFacesLightArray, 0, allFacesLightCount);

                //convert the indices objects into usable array
                int[] allFacesIndicesArray = new int[allFacesIndicesTableCount];
                if (allFacesIndicesTableCount >= 0) System.arraycopy(allFacesIndices, 0, allFacesIndicesArray, 0, allFacesIndicesTableCount);

                //convert the textureCoord objects into usable array
                float[] allFacesTextureCoordArray = new float[allFacesTextureCoordCount];
                if (allFacesTextureCoordCount >= 0) System.arraycopy(allFacesTextureCoord, 0, allFacesTextureCoordArray, 0, allFacesTextureCoordCount);

                //pass data to container object
                newChunkData.allFacesPositionsArray = allFacesPositionsArray;
                newChunkData.allFacesLightArray = allFacesLightArray;
                newChunkData.allFacesIndicesArray = allFacesIndicesArray;
                newChunkData.allFacesTextureCoordArray = allFacesTextureCoordArray;

                //setChunkMesh(chunkX, chunkZ, yHeight, new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, textureAtlas));
            } else {
                //setChunkMesh(chunkX, chunkZ, yHeight, null);
                //inform the container object that this chunk is null for this part of it
                newChunkData.allFacesMeshIsNull = true;
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
