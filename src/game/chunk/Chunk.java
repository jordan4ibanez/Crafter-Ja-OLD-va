package game.chunk;

import engine.graphics.Mesh;
import engine.network.ChunkRequest;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static engine.FancyMath.getDistance;
import static engine.time.Time.getDelta;
import static engine.disk.Disk.*;
import static engine.disk.SaveQueue.instantSave;
import static engine.disk.SaveQueue.saveChunk;
import static engine.network.Networking.getIfMultiplayer;
import static engine.network.Networking.sendOutChunkRequest;
import static engine.settings.Settings.getRenderDistance;
import static game.blocks.BlockDefinition.onDigCall;
import static game.blocks.BlockDefinition.onPlaceCall;
import static game.chunk.BiomeGenerator.addChunkToBiomeGeneration;
import static game.chunk.ChunkMath.posToIndex;
import static game.chunk.ChunkMeshGenerator.generateChunkMesh;
import static game.chunk.ChunkMeshGenerator.instantGeneration;
import static game.chunk.ChunkUpdateHandler.chunkUpdate;
import static game.light.Light.*;
import static game.player.Player.*;

public class Chunk {

    //DO NOT CHANGE THE DATA CONTAINER - but this is left here for people to experiment with
    private static final Object2ObjectOpenHashMap<Vector2i, ChunkObject> map = new Object2ObjectOpenHashMap<>();
    //private static final ConcurrentHashMap<Vector2i, ChunkObject> map = new ConcurrentHashMap<>();

    public static ChunkObject[] getMap(){
        return map.values().toArray(new ChunkObject[0]);
    }

    public static ChunkObject getChunk(int x, int z){
        return map.get(new Vector2i(x,z));
    }

    //multiplayer chunk update
    public static void setChunk(ChunkObject newChunk) {

        if (newChunk == null){
            return;
        }

        int x = newChunk.x;
        int z = newChunk.z;

        //don't allow old vertex data to leak - instead clone primitives
        ChunkObject gottenChunk = map.get(new Vector2i(x, z));
        if (gottenChunk != null) {
            gottenChunk.block = newChunk.block.clone();
            gottenChunk.light = newChunk.light.clone();
            gottenChunk.heightMap = newChunk.heightMap.clone();
            gottenChunk.rotation = newChunk.rotation.clone();
        } else {
            map.put(new Vector2i(x, z), newChunk);
        }

        for (int y = 0; y < 8; y++) {
            chunkUpdate(x, z, y);
        }

        fullNeighborUpdate(x, z);
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
        ChunkObject thisChunk = map.get(new Vector2i(chunkX, chunkZ));
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
        ChunkObject thisChunk = map.get(new Vector2i(chunkX, chunkZ));
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
        ChunkObject thisChunk = map.get(new Vector2i(chunkX, chunkZ));
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

    //this re-generates chunk meshes with the light level
    public static void floodChunksWithNewLight(){
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
        ChunkObject thisChunk = map.get(new Vector2i(chunkX, chunkZ));
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
        ChunkObject thisChunk = map.get(new Vector2i(chunkX, chunkZ));
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
        ChunkObject thisChunk = map.get(new Vector2i(chunkX, chunkZ));
        if (thisChunk == null){
            return false;
        }
        if (thisChunk.block == null){
            return false;
        }
        return thisChunk.heightMap[blockX][blockZ] < y + 1;
    }

    public static byte getBlock(int x,int y,int z){
        if (y > 127 || y < 0){
            return -1;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        ChunkObject thisChunk = map.get(new Vector2i(chunkX, chunkZ));
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
        ChunkObject thisChunk = map.get(new Vector2i(chunkX, chunkZ));
        if (thisChunk == null){
            return 0;
        }
        if (thisChunk.block == null){
            return 0;
        }
        return thisChunk.rotation[posToIndex(blockX, y, blockZ)];
    }

    public static void setBlock(int x,int y,int z, byte newBlock, byte rot){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16d);
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        ChunkObject thisChunk = map.get(new Vector2i(chunkX, chunkZ));
        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }
        thisChunk.block[posToIndex(blockX, y, blockZ)] = newBlock;
        thisChunk.rotation[posToIndex(blockX, y, blockZ)] = rot;
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
        ChunkObject thisChunk = map.get(new Vector2i(chunkX, chunkZ));
        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }
        thisChunk.light[posToIndex(blockX, y, blockZ)] = setByteNaturalLight(thisChunk.light[posToIndex(blockX, y, blockZ)],newLight);
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
        ChunkObject thisChunk = map.get(new Vector2i(chunkX, chunkZ));
        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }

        thisChunk.light[posToIndex(blockX, y, blockZ)] = setByteTorchLight(thisChunk.light[posToIndex(blockX, y, blockZ)], newLight);
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
        ChunkObject thisChunk = map.get(new Vector2i(chunkX, chunkZ));
        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }

        byte oldBlock = thisChunk.block[posToIndex(blockX, y, blockZ)];

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
        torchFloodFill(x,y,z);

        thisChunk.modified = true;

        thisChunk.light[posToIndex(blockX, y, blockZ)] = setByteNaturalLight(thisChunk.light[posToIndex(blockX, y, blockZ)], getImmediateLight(x,y,z));

        if (!getIfMultiplayer()) {
            onDigCall(oldBlock, new Vector3d(x, y, z));
        }

        instantGeneration(chunkX,chunkZ,yPillar);
        instantUpdateNeighbor(chunkX, chunkZ,blockX,y,blockZ);//instant update
    }

    public static void placeBlock(int x,int y,int z, byte ID, byte rot){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16d);
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (x - (16*chunkX));
        int blockZ = (z - (16*chunkZ));
        ChunkObject thisChunk = map.get(new Vector2i(chunkX, chunkZ));
        if (thisChunk == null){
            return;
        }
        if (thisChunk.block == null){
            return;
        }
        thisChunk.block[posToIndex(blockX, y, blockZ)] = ID;
        thisChunk.rotation[posToIndex(blockX, y, blockZ)] =  rot;
        if (thisChunk.heightMap[blockX][blockZ] < y){
            thisChunk.heightMap[blockX][blockZ] = (byte) y;
        }

        lightFloodFill(x, y, z);
        torchFloodFill(x,y,z);

        thisChunk.modified = true;

        if (!getIfMultiplayer()) {
            onPlaceCall(ID, new Vector3d(x, y, z));
        }

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

        ChunkObject thisChunk = map.get(new Vector2i(chunkX, chunkZ));

        if (thisChunk == null){
            return 0;
        }
        if (thisChunk.light == null){
            return 0;
        }


        int index = posToIndex(blockX, y, blockZ);

        byte naturalLightOfBlock = getByteNaturalLight(thisChunk.light[index]);

        byte currentGlobalLightLevel = getCurrentGlobalLightLevel();

        if (naturalLightOfBlock > currentGlobalLightLevel){
            naturalLightOfBlock = currentGlobalLightLevel;
        }

        byte torchLight = getByteTorchLight(thisChunk.light[index]);

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

        ChunkObject thisChunk = map.get(new Vector2i(chunkX, chunkZ));

        if (thisChunk == null){
            return 0;
        }
        if (thisChunk.light == null){
            return 0;
        }

        return getByteNaturalLight(thisChunk.light[posToIndex(blockX, y, blockZ)]);
    }

    public static byte getTorchLight(int x,int y,int z){
        if (y > 127 || y < 0){
            return 0;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));

        ChunkObject thisChunk = map.get(new Vector2i(chunkX, chunkZ));

        if (thisChunk == null){
            return 0;
        }
        if (thisChunk.light == null){
            return 0;
        }

        int index = posToIndex(blockX, y, blockZ);

        return getByteTorchLight(thisChunk.light[index]);
    }


    //Thanks a lot Lars!!
    public static byte getByteTorchLight(byte input){
        return (byte) (input & ((1 << 4) - 1));
    }
    public static byte getByteNaturalLight(byte input){
        return (byte) (((1 << 4) - 1) & input >> 4);
    }

    public static byte setByteTorchLight(byte input, byte newValue){
        byte naturalLight = getByteNaturalLight(input);
        return (byte) (naturalLight << 4 | newValue);
    }

    public static byte setByteNaturalLight(byte input, byte newValue){
        byte torchLight = getByteTorchLight(input);
        return (byte) (newValue << 4 | torchLight);
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

        if (map.get(new Vector2i(chunkX + 1, chunkZ)) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX+1, chunkZ, y);
            }
        }


        if (map.get(new Vector2i(chunkX-1, chunkZ)) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX-1, chunkZ, y);
            }
        }


        if (map.get(new Vector2i(chunkX, chunkZ+1)) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX, chunkZ+1, y);
            }
        }


        if (map.get(new Vector2i(chunkX, chunkZ-1)) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX, chunkZ-1, y);
            }
        }
    }

    public static void generateNewChunks(){
        //create the initial map in memory
        int chunkRenderDistance = getRenderDistance();
        Vector3i currentChunk = getPlayerCurrentChunk();
        //scan for not-generated/loaded chunks
        for (int x = -chunkRenderDistance + currentChunk.x; x < chunkRenderDistance + currentChunk.x; x++){
            for (int z = -chunkRenderDistance + currentChunk.z; z< chunkRenderDistance + currentChunk.z; z++){
                if (getChunkDistanceFromPlayer(x,z) <= chunkRenderDistance){
                    if (map.get(new Vector2i(x,z)) == null){
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
        //scan for not-generated/loaded chunks
        for (int x = -chunkRenderDistance + currentChunk.x; x < chunkRenderDistance + currentChunk.x; x++){
            for (int z = -chunkRenderDistance + currentChunk.z; z< chunkRenderDistance + currentChunk.z; z++){
                if (getChunkDistanceFromPlayer(x,z) <= chunkRenderDistance){
                    if (map.get(new Vector2i(x,z)) == null){
                        sendOutChunkRequest(new ChunkRequest(x,z, getPlayerName()));
                    }
                }
            }
        }
    }

    private static final Deque<Vector2i> deletionQueue = new ArrayDeque<>();

    private static void addChunkToDeletionQueue(int chunkX, int chunkZ) {
        deletionQueue.add(new Vector2i(chunkX, chunkZ));
    }

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
                Vector2i key = deletionQueue.pop();
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

    public static void genBiome(int chunkX, int chunkZ) {
        ChunkObject loadedChunk = null;
        try {
            loadedChunk = loadChunkFromDisk(chunkX, chunkZ);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (loadedChunk != null) {
            map.put(new Vector2i(chunkX, chunkZ), loadedChunk);

            //dump everything into the chunk updater
            for (int i = 0; i < 8; i++) {
                chunkUpdate(loadedChunk.x, loadedChunk.z, i); //delayed
            }
        } else {
            addChunkToBiomeGeneration(chunkX,chunkZ);
        }
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
