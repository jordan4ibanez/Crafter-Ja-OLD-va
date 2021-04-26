package game.chunk;

import engine.FastNoise;
import engine.graph.Mesh;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static engine.disk.Disk.*;
import static engine.disk.SaveQueue.instantSave;
import static engine.disk.SaveQueue.saveChunk;
import static game.Crafter.getChunkRenderDistance;
import static game.chunk.ChunkMesh.generateChunkMesh;
import static game.chunk.ChunkUpdateHandler.chunkUpdate;
import static game.light.Light.lightFloodFill;
import static game.player.Player.getPlayerPos;

public class Chunk {

    private static final Map<String, ChunkObject> map = new HashMap<>();

    public static Collection<ChunkObject> getMap(){
        return map.values();
    }

    public static ChunkObject getChunk(int x, int z){
        return map.get(x + " " + z);
    }

    public static void setChunkMesh(int chunkX, int chunkZ, int yHeight, Mesh newMesh){
        ChunkObject thisChunk = map.get(chunkX + " " + chunkZ);
        if (thisChunk == null){
            newMesh.cleanUp(false);
            return;
        }
        if (thisChunk.mesh == null){
            newMesh.cleanUp(false);
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

        if (saveTimer >= 10f){
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
        for (int y = yHeight * 16; y < (yHeight+1) * 16; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (thisChunk.block[y][x][z] != 0){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void setChunkLiquidMesh(int chunkX, int chunkZ, int yHeight, Mesh newMesh){
        ChunkObject thisChunk = map.get(chunkX + " " + chunkZ);
        if (thisChunk == null){
            newMesh.cleanUp(false);
            return;
        }
        if (thisChunk.liquidMesh == null){
            newMesh.cleanUp(false);
            return;
        }
        if (thisChunk.liquidMesh[yHeight] != null){
            thisChunk.liquidMesh[yHeight].cleanUp(false);
        }
        thisChunk.liquidMesh[yHeight] = newMesh;
    }

    public static void setChunkBlockBoxMesh(int chunkX, int chunkZ, int yHeight, Mesh newMesh){
        ChunkObject thisChunk = map.get(chunkX + " " + chunkZ);
        if (thisChunk == null){
            newMesh.cleanUp(false);
            return;
        }
        if (thisChunk.blockBoxMesh == null){
            newMesh.cleanUp(false);
            return;
        }
        if (thisChunk.blockBoxMesh[yHeight] != null){
            thisChunk.blockBoxMesh[yHeight].cleanUp(false);
        }
        thisChunk.blockBoxMesh[yHeight] = newMesh;
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

    public static Mesh getChunkLiquidMesh(int chunkX, int chunkZ, int yHeight){
        ChunkObject thisChunk = map.get(chunkX + " " + chunkZ);
        if (thisChunk == null){
            return null;
        }
        if (thisChunk.liquidMesh == null){
            return null;
        }
        if (thisChunk.liquidMesh[yHeight] != null){
            return thisChunk.liquidMesh[yHeight];
        }
        return null;
    }

    public static int getHeightMap(int x, int z){
        int chunkX = (int)Math.floor(x/16f);
        int chunkZ = (int)Math.floor(z/16f);
        int blockX = (int)(x - (16f*chunkX));
        int blockZ = (int)(z - (16f*chunkZ));
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
        int chunkX = (int)Math.floor(x/16f);
        int chunkZ = (int)Math.floor(z/16f);
        int blockX = (int)(x - (16f*chunkX));
        int blockZ = (int)(z - (16f*chunkZ));
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
        int chunkX = (int)Math.floor(x/16f);
        int chunkZ = (int)Math.floor(z/16f);
        int blockX = (int)(x - (16f*chunkX));
        int blockZ = (int)(z - (16f*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);
        if (thisChunk == null){
            return -1;
        }
        if (thisChunk.block == null){
            return -1;
        }
        return thisChunk.block[y][blockX][blockZ];
    }

    public static byte getBlockRotation(int x, int y, int z){
        if (y > 127 || y < 0){
            return -1;
        }
        int chunkX = (int)Math.floor(x/16f);
        int chunkZ = (int)Math.floor(z/16f);
        int blockX = (int)(x - (16f*chunkX));
        int blockZ = (int)(z - (16f*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);
        if (thisChunk == null){
            return 0;
        }
        if (thisChunk.block == null){
            return 0;
        }
        return thisChunk.rotation[y][blockX][blockZ];
    }

    public static void setBlock(int x,int y,int z, int newBlock, int rot){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16f);
        int chunkX = (int)Math.floor(x/16f);
        int chunkZ = (int)Math.floor(z/16f);
        int blockX = (int)(x - (16f*chunkX));
        int blockZ = (int)(z - (16f*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);

        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }
        thisChunk.block[y][blockX][blockZ] = newBlock;
        thisChunk.rotation[y][blockX][blockZ] = (byte)rot;
        if (newBlock == 0){
            if (thisChunk.heightMap[blockX][blockZ] == y){
                for (int yCheck = thisChunk.heightMap[blockX][blockZ]; yCheck > 0; yCheck--){
                    if (thisChunk.block[yCheck][blockX][blockZ] != 0){
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
        int yPillar = (int)Math.floor(y/16f);
        int chunkX = (int)Math.floor(x/16f);
        int chunkZ = (int)Math.floor(z/16f);
        int blockX = (int)(x - (16f*chunkX));
        int blockZ = (int)(z - (16f*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);

        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }
        thisChunk.light[y][blockX][blockZ] = newLight;
        chunkUpdate(chunkX,chunkZ,yPillar);
        updateNeighbor(chunkX, chunkZ,blockX,y,blockZ);
    }


    public static void digBlock(int x,int y,int z){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16f);
        int chunkX = (int)Math.floor(x/16f);
        int chunkZ = (int)Math.floor(z/16f);
        int blockX = (int)(x - (16f*chunkX));
        int blockZ = (int)(z - (16f*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);

        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }
        thisChunk.block[y][blockX][blockZ] = 0;
        thisChunk.rotation[y][blockX][blockZ] = 0;

        if (thisChunk.heightMap[blockX][blockZ] == y){
            for (int yCheck = thisChunk.heightMap[blockX][blockZ]; yCheck > 0; yCheck--){
                if (thisChunk.block[yCheck][blockX][blockZ] != 0){
                    thisChunk.heightMap[blockX][blockZ] = (byte) yCheck;
                    break;
                }
            }
        }
        lightFloodFill(x, y, z);
        thisChunk.modified = true;
        generateChunkMesh(chunkX,chunkZ,yPillar);//instant update
        instantUpdateNeighbor(chunkX, chunkZ,blockX,y,blockZ);//instant update
    }

    public static void placeBlock(int x,int y,int z, int ID, int rot){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16f);
        int chunkX = (int)Math.floor(x/16f);
        int chunkZ = (int)Math.floor(z/16f);
        int blockX = (int)(x - (16f*chunkX));
        int blockZ = (int)(z - (16f*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);

        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }
        thisChunk.block[y][blockX][blockZ] = ID;
        thisChunk.rotation[y][blockX][blockZ] = (byte) rot;

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
        int chunkX = (int)Math.floor(x/16f);
        int chunkZ = (int)Math.floor(z/16f);
        int blockX = (int)(x - (16f*chunkX));
        int blockZ = (int)(z - (16f*chunkZ));
        String key = chunkX + " " + chunkZ;
        ChunkObject thisChunk = map.get(key);
        if (thisChunk == null){
            return 0;
        }
        if (thisChunk.light == null){
            return 0;
        }
        return thisChunk.light[y][blockX][blockZ];
    }

    private static void instantUpdateNeighbor(int chunkX, int chunkZ, int x, int y, int z){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16f);
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
        int yPillar = (int)Math.floor(y/16f);
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
                thisChunk = null;
                queueCounter++;
            }
        }
        for (String thisString : deletionQueue.values()){
            map.remove(thisString);
        }
        deletionQueue.clear();
    }

    private static final FastNoise noise = new FastNoise();
    private static final int heightAdder = 40;
    private static final byte dirtHeight = 4;
    private static final byte waterHeight = 50;

    public static void genBiome(int chunkX, int chunkZ) {
        short currBlock;
        byte height;


        ChunkObject loadedChunk = loadChunkFromDisk(chunkX, chunkZ);

        if (loadedChunk != null){
            map.put(chunkX + " " + chunkZ, loadedChunk);
        }else {
            ChunkObject thisChunk = map.get(chunkX + " " + chunkZ);
            if (thisChunk == null) {
                map.put(chunkX + " " + chunkZ, new ChunkObject(chunkX, chunkZ));
            } else {
                return;
            }

            thisChunk = map.get(chunkX + " " + chunkZ);

            thisChunk.modified = true;

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    boolean gennedSand = false;
                    boolean gennedWater = false;
                    boolean gennedGrass = false;
                    float dirtHeightRandom = (float) Math.floor(Math.random() * 2f);

                    height = (byte) (Math.abs(noise.GetCubicFractal((chunkX * 16) + x, (chunkZ * 16) + z)) * 127 + heightAdder);

                    for (int y = 127; y >= 0; y--) {

                        //bedrock
                        if (y <= 0 + dirtHeightRandom) {
                            currBlock = 5;
                            //grass gen
                        } else if (y == height && y >= waterHeight) {

                            if (y <= waterHeight + 1) {
                                currBlock = 20;
                                gennedSand = true;
                            } else {
                                currBlock = 2;
                                gennedGrass = true;
                            }
                            //dirt/sand gen
                        } else if (y < height && y >= height - dirtHeight - dirtHeightRandom) {
                            if (gennedSand || gennedWater) {
                                gennedSand = true;
                                currBlock = 20;
                            } else {
                                currBlock = 1;
                            }

                            //stone gen
                        } else if (y < height - dirtHeight) {
                            if (y <= 30 && y > 0) {
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
                            if (y <= waterHeight) {
                                currBlock = 7;
                                gennedWater = true;
                            } else {
                                currBlock = 0;
                            }
                        }

                        thisChunk.block[y][x][z] = currBlock;

                        if (height >= waterHeight) {
                            thisChunk.heightMap[x][z] = height;
                        } else {
                            thisChunk.heightMap[x][z] = waterHeight;
                        }

                        if (gennedSand || gennedGrass) {
                            thisChunk.light[y][x][z] = 0;
                        } else {
                            thisChunk.light[y][x][z] = 15;
                        }
                    }
                }
            }
        }
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
