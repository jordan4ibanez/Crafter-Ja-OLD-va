package game.chunk;

import engine.FastNoise;
import engine.graph.Mesh;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static engine.disk.Disk.*;
import static engine.disk.SaveQueue.instantSave;
import static engine.disk.SaveQueue.saveChunk;
import static game.Crafter.getChunkRenderDistance;
import static game.chunk.ChunkMath.indexToPos;
import static game.chunk.ChunkMath.posToIndex;
import static game.chunk.ChunkMesh.generateChunkMesh;
import static game.chunk.ChunkUpdateHandler.chunkUpdate;
import static game.light.Light.*;
import static game.player.Player.getPlayerPos;

public class Chunk {

    private static final int chunkArrayLength = 128 * 16 * 16;

    private static final ConcurrentHashMap<String, ChunkObject> map = new ConcurrentHashMap<>();

    public static Collection<ChunkObject> getMap(){
        return map.values();
    }

    public static ChunkObject getChunk(int x, int z){
        return map.get(x + " " + z);
    }

    public static void setChunkMesh(int chunkX, int chunkZ, int yHeight, Mesh newMesh){
        ChunkObject thisChunk = map.get(chunkX + " " + chunkZ);
        if (thisChunk == null){
            if (newMesh != null) {
                newMesh.cleanUp(false);
            }
            return;
        }
        if (thisChunk.mesh == null){
            if (newMesh != null) {
                newMesh.cleanUp(false);
            }
            return;
        }

        if (thisChunk.mesh[yHeight] != null){
            thisChunk.mesh[yHeight].cleanUp(false);
            thisChunk.mesh[yHeight] = null;
        }
        thisChunk.mesh[yHeight] = newMesh;
    }

    private static float saveTimer = 0f;
    public static void globalChunkSaveToDisk(){
        saveTimer += 0.001f;

        if (saveTimer >= 3f){
            savePlayerPos(getPlayerPos());
            for (ChunkObject thisChunk : map.values()){
                if (thisChunk.modified) {
                    saveChunk(thisChunk);
                    thisChunk.modified = false;
                }
            }
            saveTimer = 0f;
        }
    }

    //this is for testing the day/night cycle
    public static void testLightCycleFlood(){
        byte currentLightLevel = getCurrentGlobalLightLevel();

        for (ChunkObject thisChunk : map.values()){

            if (thisChunk.lightLevel != currentLightLevel){

                floodChunkWithNewGlobalLight(thisChunk, thisChunk.lightLevel, currentLightLevel);

                thisChunk.lightLevel = currentLightLevel;

            }
        }
    }

    //this is for testing the day/night cycle
    private static void floodChunkWithNewGlobalLight(ChunkObject thisChunk, byte oldLight, byte newLight){

        /* this causes SERIOUS lag
            Vector3i thisPos = indexToPos(i);

            int blockX = (int)(thisPos.x + (16d*thisChunk.x));
            int blockZ = (int)(thisPos.z + (16d*thisChunk.z));

            if (thisChunk.block[i] == 0 && !underSunLight(blockX, thisPos.y, blockZ)) {
                lightFloodFill(blockX, thisPos.y, blockZ);
            }
             */
        //if (thisChunk.light[i] == oldLight) {
        //  System.out.println("somtthing brkun");
        //}
        Arrays.fill(thisChunk.light, newLight);

        for (int y = 0; y < 8; y++) {
            generateChunkMesh(thisChunk.x, thisChunk.z, y);
        }
    }

    public static void globalFinalChunkSaveToDisk(){
        for (ChunkObject thisChunk : map.values()){
            instantSave(thisChunk);
            thisChunk.modified = false;
        }
    }

    public static boolean chunkStackContainsBlock(int chunkX, int chunkZ, int yHeight){
        ChunkObject thisChunk = map.get(chunkX + " " + chunkZ);
        if (thisChunk == null || thisChunk.block == null){
            return false;
        }
        for (int i = 0; i < chunkArrayLength; i++) {
            if (thisChunk.block[i] != 0){
                return true;
            }
        }
        return false;
    }

    public static Mesh getChunkMesh(int chunkX, int chunkZ, int yHeight){
        ChunkObject thisChunk = map.get(chunkX + " " + chunkZ);
        if (thisChunk == null){
            return null;
        }
        if (thisChunk.mesh == null){
            return null;
        }
        if (thisChunk.mesh[yHeight] != null){
            return thisChunk.mesh[yHeight];
        }
        return null;
    }


    public static int getHeightMap(int x, int z){
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);
        if (thisChunk == null){
            return 555;
        }
        if (thisChunk.block == null){
            return 555;
        }

        return thisChunk.heightMap[blockX][blockZ];
    }

    public static boolean underSunLight(int x, int y, int z){
        if (y > 127 || y < 0){
            return false;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));

        String key = chunkX + " " + chunkZ;

        ChunkObject thisChunk = map.get(key);

        if (thisChunk == null){
            return false;
        }

        if (thisChunk.block == null){
            return false;
        }

        return thisChunk.heightMap[blockX][blockZ] < y + 1;
    }

    public static int getBlock(int x,int y,int z){
        if (y > 127 || y < 0){
            return -1;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));

        String key = chunkX + " " + chunkZ;

        ChunkObject thisChunk = map.get(key);
        if (thisChunk == null){
            return -1;
        }

        if (thisChunk.block == null){
            return -1;
        }

        return thisChunk.block[posToIndex(blockX, y, blockZ)];
    }

    public static byte getBlockRotation(int x, int y, int z){
        if (y > 127 || y < 0){
            return -1;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);
        if (thisChunk == null){
            return 0;
        }
        if (thisChunk.block == null){
            return 0;
        }
        return thisChunk.rotation[posToIndex(blockX, y, blockZ)];
    }

    public static void setBlock(int x,int y,int z, int newBlock, int rot){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16d);
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);

        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }

        thisChunk.block[posToIndex(blockX, y, blockZ)] = newBlock;
        thisChunk.rotation[posToIndex(blockX, y, blockZ)] = (byte)rot;

        if (newBlock == 0){
            if (thisChunk.heightMap[blockX][blockZ] == y){
                for (int yCheck = thisChunk.heightMap[blockX][blockZ]; yCheck > 0; yCheck--){
                    if (thisChunk.block[posToIndex(blockX, yCheck, blockZ)] != 0){
                        thisChunk.heightMap[blockX][blockZ] = (byte) yCheck;
                        break;
                    }
                }
            }
        } else {
            if (thisChunk.heightMap[blockX][blockZ] < y){
                thisChunk.heightMap[blockX][blockZ] = (byte) y;
            }
        }

        thisChunk.modified = true;

        chunkUpdate(chunkX,chunkZ,yPillar);
        updateNeighbor(chunkX, chunkZ,blockX,y,blockZ);
    }

    public static void setLight(int x,int y,int z, byte newLight){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16d);
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));

        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);

        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }
        thisChunk.light[posToIndex(blockX, y, blockZ)] = newLight;
        chunkUpdate(chunkX,chunkZ,yPillar);
        updateNeighbor(chunkX, chunkZ,blockX,y,blockZ);
    }


    public static void digBlock(int x,int y,int z){
        if (y > 127 || y < 0){
            return;
        }

        int yPillar = (int)Math.floor(y/16d);

        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));

        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);

        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }
        thisChunk.block[posToIndex(blockX, y, blockZ)] = 0;
        thisChunk.rotation[posToIndex(blockX, y, blockZ)] = 0;

        if (thisChunk.heightMap[blockX][blockZ] == y){

            for (int yCheck = thisChunk.heightMap[blockX][blockZ]; yCheck > 0; yCheck--){

                if (thisChunk.block[posToIndex(blockX, yCheck, blockZ)] != 0){
                    thisChunk.heightMap[blockX][blockZ] = (byte) yCheck;
                    break;
                }
            }
        }
        lightFloodFill(x, y, z);
        thisChunk.modified = true;

        thisChunk.light[posToIndex(blockX, y, blockZ)] = getImmediateLight(x,y,z);

        generateChunkMesh(chunkX,chunkZ,yPillar);//instant update
        instantUpdateNeighbor(chunkX, chunkZ,blockX,y,blockZ);//instant update
    }

    public static void placeBlock(int x,int y,int z, int ID, int rot){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16d);
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);

        int blockX = (x - (16*chunkX));
        int blockZ = (z - (16*chunkZ));

        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);

        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }

        thisChunk.block[posToIndex(blockX, y, blockZ)] = ID;
        thisChunk.rotation[posToIndex(blockX, y, blockZ)] = (byte) rot;

        if (thisChunk.heightMap[blockX][blockZ] < y){
            thisChunk.heightMap[blockX][blockZ] = (byte) y;
        }

        lightFloodFill(x, y, z);
        thisChunk.modified = true;

        generateChunkMesh(chunkX,chunkZ,yPillar);//instant update
        instantUpdateNeighbor(chunkX, chunkZ,blockX,y,blockZ);//instant update
    }

    public static byte getLight(int x,int y,int z){
        if (y > 127 || y < 0){
            return 0;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));

        String key = chunkX + " " + chunkZ;

        ChunkObject thisChunk = map.get(key);
        if (thisChunk == null){
            return 0;
        }
        if (thisChunk.light == null){
            return 0;
        }

        return thisChunk.light[posToIndex(blockX, y, blockZ)];
    }

    private static void instantUpdateNeighbor(int chunkX, int chunkZ, int x, int y, int z){
        if (y > 127 || y < 0){
            return;
        }

        int yPillar = (int)Math.floor(y/16d);
        switch (y){
            case 112:
            case 96:
            case 80:
            case 64:
            case 48:
            case 32:
            case 16:
                generateChunkMesh(chunkX, chunkZ, yPillar-1);
                break;
            case 111:
            case 95:
            case 79:
            case 63:
            case 47:
            case 31:
            case 15:
                generateChunkMesh(chunkX, chunkZ, yPillar+1);
                break;
        }
        if (x == 15){ //update neighbor
            generateChunkMesh(chunkX+1, chunkZ, yPillar);
        }
        if (x == 0){
            generateChunkMesh(chunkX-1, chunkZ, yPillar);
        }
        if (z == 15){
            generateChunkMesh(chunkX, chunkZ+1, yPillar);
        }
        if (z == 0){
            generateChunkMesh(chunkX, chunkZ-1, yPillar);
        }
    }

    private static void updateNeighbor(int chunkX, int chunkZ, int x, int y, int z){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16d);
        switch (y){
            case 112:
            case 96:
            case 80:
            case 64:
            case 48:
            case 32:
            case 16:
                chunkUpdate(chunkX, chunkZ, yPillar-1);
                break;
            case 111:
            case 95:
            case 79:
            case 63:
            case 47:
            case 31:
            case 15:
                chunkUpdate(chunkX, chunkZ, yPillar+1);
                break;
        }
        if (x == 15){ //update neighbor
            chunkUpdate(chunkX+1, chunkZ, yPillar);
        }
        if (x == 0){
            chunkUpdate(chunkX-1, chunkZ, yPillar);
        }
        if (z == 15){
            chunkUpdate(chunkX, chunkZ+1, yPillar);
        }
        if (z == 0){
            chunkUpdate(chunkX, chunkZ-1, yPillar);
        }
    }

    private static void fullNeighborUpdate(int chunkX, int chunkZ){
        if (map.get(chunkX+1 + " " + chunkZ) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX+1, chunkZ, y);
            }
        }
        if (map.get(chunkX-1 + " " + chunkZ) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX-1, chunkZ, y);
            }
        }
        if (map.get(chunkX + " " + (chunkZ+1)) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX, chunkZ+1, y);
            }
        }

        if (map.get(chunkX + " " + (chunkZ-1)) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX, chunkZ-1, y);
            }
        }
    }


    public static void generateNewChunks(int currentChunkX, int currentChunkZ, int dirX, int dirZ){
        //long startTime = System.nanoTime();


        HashMap<Integer, int[]> neighborQueue = new HashMap<>();

        int neighborQueueCount = 0;
        if (dirX != 0){
            for (int z = -getChunkRenderDistance() + currentChunkZ; z < getChunkRenderDistance() + currentChunkZ; z++){
                if (map.get((currentChunkX + (getChunkRenderDistance() * dirX)) + " " + z) == null) {
                    genBiome(currentChunkX + (getChunkRenderDistance() * dirX), z);
                    neighborQueue.put(neighborQueueCount, new int[]{currentChunkX + (getChunkRenderDistance() * dirX), z});
                    neighborQueueCount++;
                }
            }
        }
        if (dirZ != 0){
            for (int x = -getChunkRenderDistance() + currentChunkX; x < getChunkRenderDistance() + currentChunkX; x++){
                if (map.get( x + " " + (currentChunkZ + (getChunkRenderDistance() * dirZ))) == null) {
                    genBiome(x, currentChunkZ + (getChunkRenderDistance() * dirZ));
                    neighborQueue.put(neighborQueueCount, new int[]{x, currentChunkZ + (getChunkRenderDistance() * dirZ)});
                    neighborQueueCount++;
                }
            }
        }


        for (int[] thisArray : neighborQueue.values()){
            for (int y = 0; y < 8; y++) {
                chunkUpdate(thisArray[0], thisArray[1], y);
            }
            fullNeighborUpdate(thisArray[0], thisArray[1]);
        }



        deleteOldChunks(currentChunkX+dirX, currentChunkZ+dirZ);

        //long endTime = System.nanoTime();
        //double duration = (double)(endTime - startTime) /  1_000_000_000d;  //divide by 1000000 to get milliseconds.

        //System.out.println("This took: " + duration + " seconds");
    }

    private final static HashMap<Integer, String> deletionQueue = new HashMap<>();

    private static void deleteOldChunks(int chunkX, int chunkZ){
        int queueCounter = 0;
        for (ChunkObject thisChunk : map.values()){
            if (Math.abs(thisChunk.z - chunkZ) > getChunkRenderDistance() || Math.abs(thisChunk.x - chunkX) > getChunkRenderDistance()){
                if (thisChunk.mesh != null){
                    for (int y = 0; y < 8; y++) {
                        if (thisChunk.mesh[y] != null){
                            thisChunk.mesh[y].cleanUp(false);
                            thisChunk.mesh[y] = null;
                        }
                    }
                }

                if (thisChunk.modified) {
                    saveChunk(thisChunk);
                }

                deletionQueue.put(queueCounter, thisChunk.x + " " + thisChunk.z);
                queueCounter++;
            }
        }
        for (String thisString : deletionQueue.values()){
            map.remove(thisString);
        }
        deletionQueue.clear();
    }


    private final static int seed = 532_444_432;

    public static void genBiome(int chunkX, int chunkZ) {

        new Thread(() -> {
            final double heightAdder = 70;
            final byte dirtHeight = 4;
            final byte waterHeight = 50;
            final FastNoise noise = new FastNoise();
            final int noiseMultiplier = 50;

            noise.SetSeed(seed);

            double dirtHeightRandom;
            boolean gennedSand;
            boolean gennedWater;
            boolean gennedGrass;
            byte generationX;
            byte generationY;
            byte generationZ;
            int currBlock;
            byte height;

            ChunkObject loadedChunk = loadChunkFromDisk(chunkX, chunkZ);

            if (loadedChunk != null) {
                map.put(chunkX + " " + chunkZ, loadedChunk);

                //dump everything into the chunk updater
                for (int i = 0; i < 8; i++) {
                    chunkUpdate(loadedChunk.x, loadedChunk.z, i); //delayed
                }

            } else {

                ChunkObject thisChunk = map.get(chunkX + " " + chunkZ);

                if (thisChunk == null) {
                    thisChunk = new ChunkObject(chunkX,chunkZ);
                    map.put(chunkX + " " + chunkZ, thisChunk);
                }

                thisChunk.modified = true;

                //biome max 128 trees
                Vector3i[] treePosArray = new Vector3i[128];
                byte treeIndex = 0;

                //standard generation
                for (generationX = 0; generationX < 16; generationX++) {
                    for (generationZ = 0; generationZ < 16; generationZ++) {
                        gennedSand = false;
                        gennedWater = false;
                        gennedGrass = false;
                        dirtHeightRandom = Math.floor(Math.random() * 2d);

                        float realPosX = (float)((chunkX * 16d) + (double) generationX);
                        float realPosZ = (float)((chunkZ * 16d) + (double) generationZ);

                        height = (byte) (Math.abs(noise.GetPerlin(realPosX, realPosZ) * noiseMultiplier + heightAdder));

                        //catch ultra deep oceans
                        if (height < 6) {
                            height = 6;
                        }

                        //y column
                        for (generationY = 127; generationY >= 0; generationY--) {

                            //don't overwrite
                            currBlock = thisChunk.block[posToIndex(generationX, generationY, generationZ)];

                            if (currBlock != 0){
                                //System.out.println("overwriting!!!");
                            }

                            //bedrock
                            if (generationY <= 0 + dirtHeightRandom) {
                                currBlock = 5;
                                //grass gen
                            } else if (generationY == height && generationY >= waterHeight) {

                                if (generationY <= waterHeight + 1) {
                                    currBlock = 20;
                                    gennedSand = true;
                                } else {
                                    currBlock = 2;
                                    gennedGrass = true;
                                }
                                //tree gen
                            }else if (generationY == height + 1 && generationY > waterHeight + 1){

                                float noiseTest2 = Math.abs(noise.GetWhiteNoise(realPosX, generationY,realPosZ));

                                //add tree to queue
                                if (noiseTest2 > 0.98f){
                                    treePosArray[treeIndex] = new Vector3i(generationX, generationY, generationZ);
                                    treeIndex++;
                                }
                                //dirt/sand gen
                            } else if (generationY < height && generationY >= height - dirtHeight - dirtHeightRandom) {
                                if (gennedSand || gennedWater) {
                                    gennedSand = true;
                                    currBlock = 20;
                                } else {
                                    currBlock = 1;
                                }

                                //stone gen
                            } else if (generationY < height - dirtHeight) {
                                if (generationY <= 30 && generationY > 0) {
                                    if (Math.random() > 0.95) {
                                        currBlock = (short) Math.floor(8 + (Math.random() * 8));
                                    } else {
                                        currBlock = 3;
                                    }
                                } else {
                                    currBlock = 3;
                                }
                                //water gen
                            } else {
                                if (generationY <= waterHeight) {
                                    currBlock = 7;
                                    gennedWater = true;
                                }
                            }

                            thisChunk.block[posToIndex(generationX, generationY, generationZ)] = currBlock;

                            if (height >= waterHeight) {
                                thisChunk.heightMap[generationX][generationZ] = height;
                            } else {
                                thisChunk.heightMap[generationX][generationZ] = waterHeight;
                            }

                            if (gennedSand || gennedGrass) {
                                thisChunk.light[posToIndex(generationX, generationY, generationZ)] = 0;
                            } else {
                                thisChunk.light[posToIndex(generationX, generationY, generationZ)] = getCurrentGlobalLightLevel();
                            }
                        }
                    }
                }

                //check for trees outside chunk borders (simulated chunk generation)
                for (generationX = 0 - 3; generationX < 16 + 3; generationX++) {
                    for (generationZ = 0 - 3; generationZ < 16 + 3; generationZ++) {

                        //only check outside
                        if (generationX < 0 || generationX > 15 || generationZ < 0 || generationZ > 15) {

                            float realPosX = (float) ((chunkX * 16d) + (double) generationX);
                            float realPosZ = (float) ((chunkZ * 16d) + (double) generationZ);

                            height = (byte) (Math.abs(noise.GetPerlin(realPosX, realPosZ) * noiseMultiplier + heightAdder) + (byte) 1);

                            if (height >= 0 && height <= 127 && height > waterHeight + 1) {

                                float noiseTest2 = Math.abs(noise.GetWhiteNoise(realPosX, height, realPosZ));

                                //add tree to queue
                                if (noiseTest2 > 0.98f){
                                    treePosArray[treeIndex] = new Vector3i(generationX, height, generationZ);
                                    treeIndex++;
                                }

                            }
                        }
                    }
                }

                //generate tree cores
                for (int i = 0; i < treeIndex; i++){
                    Vector3i basePos = treePosArray[i];
                    //generate stumps
                    for (int y = 0; y < 4; y++){
                        //stay within borders
                        if (y + treePosArray[i].y < 127 && basePos.x >= 0 && basePos.x <= 15 && basePos.z >= 0 && basePos.z <= 15){
                            thisChunk.block[posToIndex(basePos.x,basePos.y + y, basePos.z)] = 25;
                        }
                    }
                }

                //generate tree leaves
                for (int i = 0; i < treeIndex; i++){
                    Vector3i basePos = treePosArray[i];
                    byte treeWidth = 0;
                    for (int y = 5; y > 1; y--){
                        for (int x = -treeWidth; x <= treeWidth; x++){
                            for (int z = -treeWidth; z <= treeWidth; z++) {

                                if (    basePos.x + x >= 0 && basePos.x + x <= 15 &&
                                        basePos.y + y >= 0 && basePos.y + y <= 127 &&
                                        basePos.z + z >= 0 && basePos.z + z <= 15) {

                                    int index = posToIndex(basePos.x + x, basePos.y + y, basePos.z + z);

                                    if (thisChunk.block[index] == 0) {
                                        thisChunk.block[index] = 26;
                                    }
                                }
                            }
                        }
                        if (treeWidth < 2) {
                            treeWidth++;
                        }
                    }

                }

                //todo: add in blank chunk boolean

                //dump everything into the chunk updater
                for (int i = 0; i < 8; i++) {
                    //generateChunkMesh(thisChunk.x, thisChunk.z, i); //instant
                    chunkUpdate(thisChunk.x, thisChunk.z, i); //delayed
                }

                instantSave(thisChunk);
            }
        }).start();
    }

    public static void cleanUp(){
        for (ChunkObject thisChunk : map.values()){
            if (thisChunk == null){
                continue;
            }
            if (thisChunk.mesh != null){
                for (Mesh thisMesh : thisChunk.mesh){
                    if (thisMesh != null){
                        thisMesh.cleanUp(true);
                    }
                }
            }
        }
    }
}
