package game.chunk;

import engine.Window;
import engine.highPerformanceContainers.HyperFloatArray;
import engine.highPerformanceContainers.HyperIntArray;
import game.blocks.BlockDefinitionContainer;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.concurrent.ConcurrentLinkedDeque;

public class ChunkMeshGenerator implements Runnable{

    private final BlockDefinitionContainer blockDefinitionContainer = new BlockDefinitionContainer();

    private Window window;
    private ChunkUpdateHandler chunkUpdateHandler;
    private Chunk chunk;

    public ChunkMeshGenerator(){
    }

    public void setWindow(Window window){
        if (this.window == null){
            this.window = window;
        }
    }

    public void setChunkUpdateHandler(ChunkUpdateHandler chunkUpdateHandler){
        if (this.chunkUpdateHandler == null){
            this.chunkUpdateHandler = chunkUpdateHandler;
        }
    }

    public void setChunk(Chunk chunk){
        if (this.chunk == null) {
            this.chunk = chunk;
        }
    }


    //all data held on this thread

    private final ConcurrentLinkedDeque<Vector3i> generationQueue = new ConcurrentLinkedDeque<>();

    private final Vector3i key = new Vector3i();

    private byte currentLightLevel = 15;

    //normal block mesh data
    private final HyperFloatArray positions = new HyperFloatArray(24);
    private final HyperFloatArray textureCoord = new HyperFloatArray(16);
    private final HyperIntArray indices = new HyperIntArray(12);
    private final HyperFloatArray light = new HyperFloatArray(24);

    //liquid block mesh data
    private final HyperFloatArray liquidPositions = new HyperFloatArray(24);
    private final HyperFloatArray liquidTextureCoord = new HyperFloatArray(16);
    private final HyperIntArray liquidIndices = new HyperIntArray(12);
    private final HyperFloatArray liquidLight = new HyperFloatArray(24);

    //allFaces block mesh data
    private final HyperFloatArray allFacesPositions = new HyperFloatArray(24);
    private final HyperFloatArray allFacesTextureCoord = new HyperFloatArray(16);
    private final HyperIntArray allFacesIndices = new HyperIntArray(12);
    private final HyperFloatArray allFacesLight = new HyperFloatArray(24);


    public void run() {
        //run until game is closed - should only be run in game
        while (!window.shouldClose()) {
            if (pollQueue()) {
                try {
                    //thread needs to sleep quicker to avoid lag
                    //System.out.println("sleeping");
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } //else {
                //System.out.println("I'M AWAKE!");
            //}
        }
    }

    public void generateChunkMesh(int chunkX, int chunkZ, int yHeight) {
        //do not add duplicates
        if (!generationQueue.contains(new Vector3i(chunkX,yHeight, chunkZ))) {
            generationQueue.add(new Vector3i(chunkX, yHeight, chunkZ));
        }
    }

    public void instantGeneration(int chunkX, int chunkZ, int yHeight){
        //replace the data basically
        generationQueue.remove(new Vector3i(chunkX,yHeight, chunkZ));
        generationQueue.addFirst(new Vector3i(chunkX,yHeight, chunkZ));
    }

    //allows main thread to set the local byte of light to this runnable thread
    public void setLightLevel(byte newLight){
        currentLightLevel = newLight;
    }


    //this polls for chunk meshes to update
    private boolean pollQueue() {
        if (generationQueue.isEmpty()) {
            return true;
        }

        try {
            key.set(generationQueue.pop());
        } catch (Exception ignore) {
            return false; //don't crash basically
        }

        Vector2i key2D = new Vector2i(key.x, key.z);

        byte[] blockData    = chunk.getBlockData(key2D);
        byte[] rotationData = chunk.getRotationData(key2D);
        byte[] lightData    = chunk.getLightData(key2D);

        //don't bother if the chunk doesn't exist
        if (blockData == null || rotationData == null || lightData == null) {
            return false;
        }

        //raw data extracted into stack primitives
        final int chunkX = key.x;
        final int chunkZ = key.z;
        final int yHeight = key.y;

        //neighbor chunks
        byte[] chunkNeighborXPlusBlockData  = chunk.getBlockData(new Vector2i(key.x + 1, key.z));
        byte[] chunkNeighborXMinusBlockData = chunk.getBlockData(new Vector2i(key.x - 1, key.z));
        byte[] chunkNeighborZPlusBlockData  = chunk.getBlockData(new Vector2i(key.x, key.z + 1));
        byte[] chunkNeighborZMinusBlockData = chunk.getBlockData(new Vector2i(key.x, key.z - 1));

        byte[] chunkNeighborXPlusLightData  = chunk.getLightData(new Vector2i(key.x + 1, key.z));
        byte[] chunkNeighborXMinusLightData = chunk.getLightData(new Vector2i(key.x - 1, key.z));
        byte[] chunkNeighborZPlusLightData  = chunk.getLightData(new Vector2i(key.x, key.z + 1));
        byte[] chunkNeighborZMinusLightData = chunk.getLightData(new Vector2i(key.x, key.z - 1));

        int indicesCount = 0;

        int liquidIndicesCount = 0;

        int allFacesIndicesCount = 0;

        //current global light level - dumped into the stack
        byte chunkLightLevel = currentLightLevel;

        byte thisBlock;
        byte thisBlockDrawType;
        byte thisRotation;

        //loop through ystack
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = yHeight * 16; y < (yHeight + 1) * 16; y++) {

                    thisBlock = blockData[posToIndex(x, y, z)];

                    //only if not air
                    if (thisBlock <= 0) {
                        continue;
                    }

                    //only need to look this data up if it's not air
                    thisBlockDrawType = blockDefinitionContainer.getDrawType(thisBlock);
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


        ChunkMeshData newChunkData = new ChunkMeshData();

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
        chunkUpdateHandler.addToChunkMeshQueue(newChunkData);
        return false;
    }


    private int calculateBlockBox(int x, int y, int z, byte thisBlock, byte thisRotation, int indicesCount, byte[] chunkNeighborZPlusLightData, byte[] chunkNeighborZMinusLightData, byte[] chunkNeighborXPlusLightData, byte[] chunkNeighborXMinusLightData, byte chunkLightLevel, byte[] lightData){
        for (float[] thisBlockBox : blockDefinitionContainer.getShape(thisBlock, thisRotation)) {
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


            float[] textureWorker = blockDefinitionContainer.getFrontTexturePoints(thisBlock, thisRotation);

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

            textureWorker = blockDefinitionContainer.getBackTexturePoints(thisBlock, thisRotation);

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


            textureWorker = blockDefinitionContainer.getRightTexturePoints(thisBlock, thisRotation);
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

            textureWorker = blockDefinitionContainer.getLeftTexturePoints(thisBlock, thisRotation);
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


            textureWorker = blockDefinitionContainer.getTopTexturePoints(thisBlock);
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


            textureWorker = blockDefinitionContainer.getBottomTexturePoints(thisBlock);
            //bottom
            textureCoord.pack(textureWorker[1] - ((1f - thisBlockBox[5]) / 32f), textureWorker[2] + ((1f - thisBlockBox[0]) / 32f), textureWorker[0] - ((0f - thisBlockBox[2]) / 32f), textureWorker[2] + ((1f - thisBlockBox[0]) / 32f), textureWorker[0] - ((0f - thisBlockBox[2]) / 32f), textureWorker[3] - (((thisBlockBox[3]) / 32f)), textureWorker[1] - ((1f - thisBlockBox[5]) / 32f), textureWorker[3] - ((thisBlockBox[3]) / 32f));
        }

        return indicesCount;
    }

    private int calculateLiquids(int x, int y, int z, byte thisBlock, byte thisRotation, int liquidIndicesCount, byte[] blockData, byte[] chunkNeighborZPlusBlockData, byte[] chunkNeighborZPlusLightData, byte[] chunkNeighborZMinusBlockData, byte[] chunkNeighborZMinusLightData, byte[] chunkNeighborXPlusBlockData, byte[] chunkNeighborXPlusLightData, byte[] chunkNeighborXMinusBlockData, byte[] chunkNeighborXMinusLightData, byte chunkLightLevel, byte[] lightData){
        byte neighborBlock;

        if (z + 1 > 15) {
            neighborBlock = getNeighborBlock(chunkNeighborZPlusBlockData, x, y, 0);
        } else {
            neighborBlock = blockData[posToIndex(x, y, z + 1)];
        }
        byte neighborDrawtype = blockDefinitionContainer.getDrawType(neighborBlock);

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

            textureWorker = blockDefinitionContainer.getFrontTexturePoints(thisBlock, thisRotation);
            //front
            liquidTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]
            );
        }


        if (z - 1 < 0) {
            neighborBlock = getNeighborBlock(chunkNeighborZMinusBlockData, x, y, 15);
        } else {
            neighborBlock = blockData[posToIndex(x, y, z - 1)];
        }
        neighborDrawtype = blockDefinitionContainer.getDrawType(neighborBlock);

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

            textureWorker = blockDefinitionContainer.getBackTexturePoints(thisBlock, thisRotation);
            //back
            liquidTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        if (x + 1 > 15) {
            neighborBlock = getNeighborBlock(chunkNeighborXPlusBlockData, 0, y, z);
        } else {
            neighborBlock = blockData[posToIndex(x + 1, y, z)];
        }
        neighborDrawtype = blockDefinitionContainer.getDrawType(neighborBlock);

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

            textureWorker = blockDefinitionContainer.getRightTexturePoints(thisBlock, thisRotation);
            //right
            liquidTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        if (x - 1 < 0) {
            neighborBlock = getNeighborBlock(chunkNeighborXMinusBlockData, 15, y, z);
        } else {
            neighborBlock = blockData[posToIndex(x - 1, y, z)];
        }
        neighborDrawtype = blockDefinitionContainer.getDrawType(neighborBlock);

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

            textureWorker = blockDefinitionContainer.getLeftTexturePoints(thisBlock, thisRotation);
            //left
            liquidTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        //y doesn't need a check since it has no neighbors
        if (y + 1 < 128) {
            neighborBlock = blockData[posToIndex(x, y + 1, z)];
        }
        neighborDrawtype = blockDefinitionContainer.getDrawType(neighborBlock);

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

            textureWorker =blockDefinitionContainer.getTopTexturePoints(thisBlock);
            //top
            liquidTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        //doesn't need a neighbor chunk, chunks are 2D
        if (y - 1 > 0) {
            neighborBlock = blockData[posToIndex(x, y - 1, z)];
        }
        neighborDrawtype = blockDefinitionContainer.getDrawType(neighborBlock);

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

            textureWorker = blockDefinitionContainer.getBottomTexturePoints(thisBlock);
            //bottom
            liquidTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        return liquidIndicesCount;
    }

    private int calculateNormal(int x, int y, int z, byte thisBlock, byte thisRotation, int indicesCount, byte[] blockData, byte[] chunkNeighborZPlusBlockData, byte[] chunkNeighborZPlusLightData, byte[] chunkNeighborZMinusBlockData, byte[] chunkNeighborZMinusLightData, byte[] chunkNeighborXPlusBlockData, byte[] chunkNeighborXPlusLightData, byte[] chunkNeighborXMinusBlockData, byte[] chunkNeighborXMinusLightData, byte chunkLightLevel, byte[] lightData){

        byte neighborBlock;
        if (z + 1 > 15) {
            neighborBlock = getNeighborBlock(chunkNeighborZPlusBlockData, x, y, 0);
        } else {
            neighborBlock = blockData[posToIndex(x, y, z + 1)];
        }

        float lightValue;
        byte realLight;
        float[] textureWorker;
        if (neighborBlock >= 0 && (blockDefinitionContainer.getDrawType(neighborBlock) != 1 || blockDefinitionContainer.getIfLiquid(neighborBlock))) {
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

            textureWorker = blockDefinitionContainer.getFrontTexturePoints(thisBlock, thisRotation);
            //front
            textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        if (z - 1 < 0) {
            neighborBlock = getNeighborBlock(chunkNeighborZMinusBlockData, x, y, 15);
        } else {
            neighborBlock = blockData[posToIndex(x, y, z - 1)];
        }

        if (neighborBlock >= 0 && (blockDefinitionContainer.getDrawType(neighborBlock) != 1 || blockDefinitionContainer.getIfLiquid(neighborBlock))) {
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

            textureWorker = blockDefinitionContainer.getBackTexturePoints(thisBlock, thisRotation);
            //back
            textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        if (x + 1 > 15) {
            neighborBlock = getNeighborBlock(chunkNeighborXPlusBlockData, 0, y, z);
        } else {
            neighborBlock = blockData[posToIndex(x + 1, y, z)];
        }

        if (neighborBlock >= 0 && (blockDefinitionContainer.getDrawType(neighborBlock) != 1 || blockDefinitionContainer.getIfLiquid(neighborBlock))) {
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

            textureWorker = blockDefinitionContainer.getRightTexturePoints(thisBlock, thisRotation);
            //right
            textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        if (x - 1 < 0) {
            neighborBlock = getNeighborBlock(chunkNeighborXMinusBlockData, 15, y, z);
        } else {
            neighborBlock = blockData[posToIndex(x - 1, y, z)];
        }

        if (neighborBlock >= 0 && (blockDefinitionContainer.getDrawType(neighborBlock) != 1 || blockDefinitionContainer.getIfLiquid(neighborBlock))) {
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

            textureWorker = blockDefinitionContainer.getLeftTexturePoints(thisBlock, thisRotation);
            //left
            textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        if (y + 1 < 128) {
            neighborBlock = blockData[posToIndex(x, y + 1, z)];
        }

        if (y == 127 || neighborBlock > -1 && (blockDefinitionContainer.getDrawType(neighborBlock) != 1 || blockDefinitionContainer.getIfLiquid(neighborBlock))) {
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

            textureWorker = blockDefinitionContainer.getTopTexturePoints(thisBlock);
            //top
            textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        if (y - 1 > 0) {
            neighborBlock = blockData[posToIndex(x, y - 1, z)];
        }

        if (y != 0 && neighborBlock >= 0 && (blockDefinitionContainer.getDrawType(neighborBlock) != 1 || blockDefinitionContainer.getIfLiquid(neighborBlock))) {
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

            textureWorker = blockDefinitionContainer.getBottomTexturePoints(thisBlock);
            //bottom
            textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        return indicesCount;
    }

    private final Vector3f trl = new Vector3f(); //top rear left
    private final Vector3f trr = new Vector3f(); //top rear right
    private final Vector3f tfl = new Vector3f(); //top front left
    private final Vector3f tfr = new Vector3f(); //top front right

    private final Vector3f brl = new Vector3f(); //bottom rear left
    private final Vector3f brr = new Vector3f(); //bottom rear right - also cold
    private final Vector3f bfl = new Vector3f(); //bottom front left
    private final Vector3f bfr = new Vector3f(); //bottom front right


    private int calculateTorchLike(int x, int y, int z, byte thisBlock, byte thisRotation, int indicesCount, byte[] chunkNeighborZPlusLightData, byte[] chunkNeighborZMinusLightData, byte[] chunkNeighborXPlusLightData, byte[] chunkNeighborXMinusLightData, byte chunkLightLevel, byte[] lightData){
        //require 0.125 width
        //large
        float largeXZ = 0.5625f;
        //small
        float smallXZ = 0.4375f;
        float largeY = 0.625f;
        float smallY = 0.0f;
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

        float[] textureWorker = blockDefinitionContainer.getFrontTexturePoints(thisBlock, thisRotation);


        //assume 16 pixels wide
        float pixel = (1f / 32f / 16f);
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

    private int calculateAllFaces(int x, int y, int z, byte thisBlock, byte thisRotation, int allFacesIndicesCount, byte[] chunkNeighborZPlusLightData, byte[] chunkNeighborZMinusLightData, byte[] chunkNeighborXPlusLightData, byte[] chunkNeighborXMinusLightData, byte chunkLightLevel, byte[] lightData){

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

        textureWorker = blockDefinitionContainer.getFrontTexturePoints(thisBlock, thisRotation);
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

        textureWorker = blockDefinitionContainer.getBackTexturePoints(thisBlock, thisRotation);
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

        textureWorker = blockDefinitionContainer.getRightTexturePoints(thisBlock, thisRotation);
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

        textureWorker = blockDefinitionContainer.getLeftTexturePoints(thisBlock, thisRotation);
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

        textureWorker = blockDefinitionContainer.getTopTexturePoints(thisBlock);
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

            textureWorker = blockDefinitionContainer.getBottomTexturePoints(thisBlock);
            //bottom
            allFacesTextureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);
        }

        return allFacesIndicesCount;
    }

    private byte calculateBlockLight(byte chunkLightLevel, byte lightData){
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

    private byte getNeighborBlock(byte[] neighborChunkBlockData, int x, int y, int z){
        if (neighborChunkBlockData == null){
            return 0;
        }
        return neighborChunkBlockData[posToIndex(x,y,z)];
    }

    private byte getNeighborLight(byte currentGlobalLightLevel, byte[] neighborChunkLightData, int x, int y, int z){
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
    private float convertLight(byte lightValue){
        return (float) Math.pow(1.25, lightValue)/28.42171f;
    }

    private int posToIndex( int x, int y, int z ) {
        return (z * 2048) + (y * 16) + x;
    }

    private byte getByteTorchLight(byte input){
        return (byte) (input & ((1 << 4) - 1));
    }
    private byte getByteNaturalLight(byte input){
        return (byte) (((1 << 4) - 1) & input >> 4);
    }
}