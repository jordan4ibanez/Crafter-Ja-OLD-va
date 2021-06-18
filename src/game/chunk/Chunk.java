package game.chunk;

import engine.FastNoise;
import engine.graphics.Mesh;
import engine.network.ChunkRequest;
import org.joml.Vector3i;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static engine.FancyMath.getDistance;
import static engine.Time.getDelta;
import static engine.disk.Disk.*;
import static engine.disk.SaveQueue.instantSave;
import static engine.disk.SaveQueue.saveChunk;
import static engine.network.Networking.sendOutChunkRequest;
import static engine.settings.Settings.getRenderDistance;
import static engine.settings.Settings.getSettingsChunkLoad;
import static game.chunk.ChunkMath.posToIndex;
import static game.chunk.ChunkMeshGenerator.generateChunkMesh;
import static game.chunk.ChunkMeshGenerator.instantGeneration;
import static game.chunk.ChunkUpdateHandler.chunkUpdate;
import static game.light.Light.*;
import static game.player.Player.*;

public class Chunk {

    private static final ConcurrentHashMap<String, ChunkObject> map = new ConcurrentHashMap<>();

    public static Collection<ChunkObject> getMap(){
        return map.values();
    }

    public static ChunkObject getChunk(int x, int z){
        return map.get(x + " " + z);
    }



    //multiplayer chunk update
    public static void setChunk(int x, int z, ChunkObject newChunk){
        if (map.get(x + " " + z) != null){
            map.remove(x + " " + z);
        }

        map.put(x + " " + z, newChunk);

        for (int y = 0; y < 8; y++) {
            chunkUpdate(x, z, y);
        }

        fullNeighborUpdate(x,z);
    }

    public static void initialChunkPayload(){
        //create the initial map in memory
        int chunkRenderDistance = getRenderDistance();
        Vector3i currentChunk = getPlayerCurrentChunk();
        for (int x = -chunkRenderDistance + currentChunk.x; x < chunkRenderDistance + currentChunk.x; x++){
            for (int z = -chunkRenderDistance + currentChunk.z; z< chunkRenderDistance + currentChunk.z; z++){
                if (getChunkDistanceFromPlayer(x,z) <= chunkRenderDistance){
                    genBiome(x,z);
                    for (int y = 0; y < 8; y++){
                        chunkUpdate(x,z,y);
                        //generateChunkMesh(x,z,y); <- this one causes serious startup lag for slow pcs
                    }
                }
            }
        }
    }

    public static void initialChunkPayloadMultiplayer(){
        //create the initial map in memory
        int chunkRenderDistance = getRenderDistance();
        Vector3i currentChunk = getPlayerCurrentChunk();
        for (int x = -chunkRenderDistance + currentChunk.x; x < chunkRenderDistance + currentChunk.x; x++){
            for (int z = -chunkRenderDistance + currentChunk.z; z< chunkRenderDistance + currentChunk.z; z++){
                if (getChunkDistanceFromPlayer(x,z) <= chunkRenderDistance){
                    sendOutChunkRequest(new ChunkRequest(x,z, getPlayerName()));
                }
            }
        }
    }

    private static double getChunkDistanceFromPlayer(int x, int z){
        Vector3i currentChunk = getPlayerCurrentChunk();
        return Math.max(getDistance(0,0,currentChunk.z, 0, 0, z), getDistance(currentChunk.x,0,0, x, 0, 0));
    }

    public static void setChunkNormalMesh(int chunkX, int chunkZ, int yHeight, Mesh newMesh){
        ChunkObject thisChunk = map.get(chunkX + " " + chunkZ);
        if (thisChunk == null){
            if (newMesh != null) {
                newMesh.cleanUp(false);
            }
            return;
        }
        if (thisChunk.normalMesh == null){
            if (newMesh != null) {
                newMesh.cleanUp(false);
            }
            return;
        }

        if (thisChunk.normalMesh[yHeight] != null){
            thisChunk.normalMesh[yHeight].cleanUp(false);
            thisChunk.normalMesh[yHeight] = null;
        }
        thisChunk.normalMesh[yHeight] = newMesh;
    }

    public static void setChunkLiquidMesh(int chunkX, int chunkZ, int yHeight, Mesh newMesh){
        ChunkObject thisChunk = map.get(chunkX + " " + chunkZ);
        if (thisChunk == null){
            if (newMesh != null) {
                newMesh.cleanUp(false);
            }
            return;
        }
        if (thisChunk.liquidMesh == null){
            if (newMesh != null) {
                newMesh.cleanUp(false);
            }
            return;
        }

        if (thisChunk.liquidMesh[yHeight] != null){
            thisChunk.liquidMesh[yHeight].cleanUp(false);
            thisChunk.liquidMesh[yHeight] = null;
        }
        thisChunk.liquidMesh[yHeight] = newMesh;
    }

    public static void setChunkAllFacesMesh(int chunkX, int chunkZ, int yHeight, Mesh newMesh){
        ChunkObject thisChunk = map.get(chunkX + " " + chunkZ);
        if (thisChunk == null){
            if (newMesh != null) {
                newMesh.cleanUp(false);
            }
            return;
        }
        if (thisChunk.allFacesMesh == null){
            if (newMesh != null) {
                newMesh.cleanUp(false);
            }
            return;
        }

        if (thisChunk.allFacesMesh[yHeight] != null){
            thisChunk.allFacesMesh[yHeight].cleanUp(false);
            thisChunk.allFacesMesh[yHeight] = null;
        }
        thisChunk.allFacesMesh[yHeight] = newMesh;
    }

    private static float saveTimer = 0f;
    public static void globalChunkSaveToDisk(){
        saveTimer += getDelta();
        //save interval is 3 seconds
        if (saveTimer >= 3f){
            updateWorldsPathToAvoidCrash();
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
        for (ChunkObject thisChunk : map.values()){
            for (int y = 0; y < 8; y++) {
                chunkUpdate(thisChunk.x, thisChunk.z, y);
            }
        }
    }

    public static void globalFinalChunkSaveToDisk(){
        updateWorldsPathToAvoidCrash();
        for (ChunkObject thisChunk : map.values()){
            instantSave(thisChunk);
            thisChunk.modified = false;
        }
        map.clear();
    }

    public static boolean chunkStackContainsBlock(int chunkX, int chunkZ, int yHeight){
        ChunkObject thisChunk = map.get(chunkX + " " + chunkZ);
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

    public static void setNaturalLight(int x, int y, int z, byte newLight){
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
        thisChunk.naturalLight[posToIndex(blockX, y, blockZ)] = newLight;
        chunkUpdate(chunkX,chunkZ,yPillar);
        updateNeighbor(chunkX, chunkZ,blockX,y,blockZ);
    }

    public static void setTorchLight(int x,int y,int z, byte newLight){
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
        thisChunk.torchLight[posToIndex(blockX, y, blockZ)] = newLight;
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
        thisChunk.naturalLight[posToIndex(blockX, y, blockZ)] = getImmediateLight(x,y,z);
        instantGeneration(chunkX,chunkZ,yPillar);
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
        instantGeneration(chunkX,chunkZ,yPillar);
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
        if (thisChunk.naturalLight == null){
            return 0;
        }


        int index = posToIndex(blockX, y, blockZ);

        byte naturalLightOfBlock = thisChunk.naturalLight[index];

        byte currentGlobalLightLevel = getCurrentGlobalLightLevel();

        if (naturalLightOfBlock > currentGlobalLightLevel){
            naturalLightOfBlock = currentGlobalLightLevel;
        }

        byte torchLight = thisChunk.torchLight[index];

        if (naturalLightOfBlock > torchLight){
            return naturalLightOfBlock;
        } else {
            return torchLight;
        }
    }

    public static byte getNaturalLight(int x,int y,int z){
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
        if (thisChunk.naturalLight == null){
            return 0;
        }

        return thisChunk.naturalLight[posToIndex(blockX, y, blockZ)];
    }

    public static byte getTorchLight(int x,int y,int z){
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
        if (thisChunk.torchLight == null){
            return 0;
        }


        int index = posToIndex(blockX, y, blockZ);

        return thisChunk.torchLight[index];
    }

    private static void instantUpdateNeighbor(int chunkX, int chunkZ, int x, int y, int z){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16d);
        switch (y) {
            case 112, 96, 80, 64, 48, 32, 16 -> generateChunkMesh(chunkX, chunkZ, yPillar - 1);
            case 111, 95, 79, 63, 47, 31, 15 -> generateChunkMesh(chunkX, chunkZ, yPillar + 1);
        }
        if (x == 15){ //update neighbor
            instantGeneration(chunkX+1, chunkZ, yPillar);
        }
        if (x == 0){
            instantGeneration(chunkX-1, chunkZ, yPillar);
        }
        if (z == 15){
            instantGeneration(chunkX, chunkZ+1, yPillar);
        }
        if (z == 0){
            instantGeneration(chunkX, chunkZ-1, yPillar);
        }
    }

    private static void updateNeighbor(int chunkX, int chunkZ, int x, int y, int z){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16d);
        switch (y) {
            case 112, 96, 80, 64, 48, 32, 16 -> chunkUpdate(chunkX, chunkZ, yPillar - 1);
            case 111, 95, 79, 63, 47, 31, 15 -> chunkUpdate(chunkX, chunkZ, yPillar + 1);
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


    public static void generateNewChunks(){
        //create the initial map in memory
        int chunkRenderDistance = getRenderDistance();
        Vector3i currentChunk = getPlayerCurrentChunk();
        String currChunk;
        //scan for not-generated/loaded chunks
        for (int x = -chunkRenderDistance + currentChunk.x; x < chunkRenderDistance + currentChunk.x; x++){
            for (int z = -chunkRenderDistance + currentChunk.z; z< chunkRenderDistance + currentChunk.z; z++){
                if (getChunkDistanceFromPlayer(x,z) <= chunkRenderDistance){
                    currChunk = x + " " + z;
                    if (map.get(currChunk) == null){
                        genBiome(x,z);
                        for (int y = 0; y < 8; y++) {
                            chunkUpdate(x, z, y);
                        }
                        fullNeighborUpdate(x, z);
                    }
                }
            }
        }
        //scan map for out of range chunks
        for (ChunkObject thisChunk : map.values()){
            if (getChunkDistanceFromPlayer(thisChunk.x,thisChunk.z) > chunkRenderDistance){
                addChunkToDeletionQueue(thisChunk.x,thisChunk.z);
            }
        }
    }

    public static void requestNewChunks(){
        //create the initial map in memory
        int chunkRenderDistance = getRenderDistance();
        Vector3i currentChunk = getPlayerCurrentChunk();
        String currChunk;
        //scan for not-generated/loaded chunks
        for (int x = -chunkRenderDistance + currentChunk.x; x < chunkRenderDistance + currentChunk.x; x++){
            for (int z = -chunkRenderDistance + currentChunk.z; z< chunkRenderDistance + currentChunk.z; z++){
                if (getChunkDistanceFromPlayer(x,z) <= chunkRenderDistance){
                    currChunk = x + " " + z;
                    if (map.get(currChunk) == null){
                        sendOutChunkRequest(new ChunkRequest(x,z, getPlayerName()));
                    }
                }
            }
        }
    }

    private static final Deque<String> deletionQueue = new ArrayDeque<>();

    private static void addChunkToDeletionQueue(int chunkX, int chunkZ) {
        deletionQueue.add(chunkX + " " + chunkZ);
    }

    //the higher this is set, the lazier chunk deletion gets
    //set it too high, and chunk deletion barely works
    private static final float[] goalTimerArray = new float[]{
            0.05f, //SNAIL
            0.025f, //SLOWER
            0.009f, //NORMAL
            0.004f, //FASTER
            0.002f, //INSANE
            0.0001f, //FUTURE PC
    };

    private static final float goalTimer = 0.0001f;//goalTimerArray[getSettingsChunkLoad()];

    private static float chunkDeletionTimer = 0f;


    public static void processOldChunks() {

        chunkDeletionTimer += getDelta();

        int updateAmount = 0;

        if (chunkDeletionTimer >= goalTimer){
            updateAmount = (int)(Math.ceil(chunkDeletionTimer / goalTimer));
            chunkDeletionTimer = 0;
        }

        for (int i = 0; i < updateAmount; i++) {
            if (!deletionQueue.isEmpty()) {
                String key = deletionQueue.pop();
                ChunkObject thisChunk = map.get(key);
                if (thisChunk == null) {
                    return;
                }
                if (thisChunk.normalMesh != null) {
                    for (int y = 0; y < 8; y++) {
                        if (thisChunk.normalMesh[y] != null) {
                            thisChunk.normalMesh[y].cleanUp(false);
                            thisChunk.normalMesh[y] = null;
                        }
                    }
                }
                if (thisChunk.liquidMesh != null) {
                    for (int y = 0; y < 8; y++) {
                        if (thisChunk.liquidMesh[y] != null) {
                            thisChunk.liquidMesh[y].cleanUp(false);
                            thisChunk.liquidMesh[y] = null;
                        }
                    }
                }
                if (thisChunk.modified) {
                    saveChunk(thisChunk);
                }
                map.remove(key);
            }
        }
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

            ChunkObject loadedChunk = null;
            try {
                loadedChunk = loadChunkFromDisk(chunkX, chunkZ);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                                thisChunk.naturalLight[posToIndex(generationX, generationY, generationZ)] = 0;
                            } else {
                                thisChunk.naturalLight[posToIndex(generationX, generationY, generationZ)] = 15;
                            }
                        }
                    }
                }

                //check for trees outside chunk borders (simulated chunk generation)
                for (generationX = -3; generationX < 16 + 3; generationX++) {
                    for (generationZ = -3; generationZ < 16 + 3; generationZ++) {

                        //only check outside
                        if (generationX < 0 || generationX > 15 || generationZ < 0 || generationZ > 15) {

                            float realPosX = (float) ((chunkX * 16d) + (double) generationX);
                            float realPosZ = (float) ((chunkZ * 16d) + (double) generationZ);

                            height = (byte) (Math.abs(noise.GetPerlin(realPosX, realPosZ) * noiseMultiplier + heightAdder) + (byte) 1);

                            if (height > waterHeight + 1) {

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
            if (thisChunk.normalMesh != null){
                for (Mesh thisMesh : thisChunk.normalMesh){
                    if (thisMesh != null){
                        thisMesh.cleanUp(true);
                    }
                }
            }

            if (thisChunk.liquidMesh != null){
                for (Mesh thisMesh : thisChunk.liquidMesh){
                    if (thisMesh != null){
                        thisMesh.cleanUp(true);
                    }
                }
            }
        }
    }
}
