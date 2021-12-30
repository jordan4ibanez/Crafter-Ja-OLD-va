package game.chunk;

import engine.graphics.Mesh;
import engine.network.ChunkRequest;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static engine.FancyMath.getDistance;
import static engine.disk.Disk.*;
import static engine.disk.SQLiteDiskHandler.loadChunk;
import static engine.disk.SQLiteDiskHandler.saveChunk;
import static engine.network.Networking.getIfMultiplayer;
import static engine.network.Networking.sendOutChunkRequest;
import static engine.settings.Settings.getRenderDistance;
import static engine.time.Time.getDelta;
import static game.blocks.BlockDefinition.*;
import static game.chunk.BiomeGenerator.addChunkToBiomeGeneration;
import static game.chunk.ChunkMath.posToIndex;
import static game.chunk.ChunkMath.posToIndex2D;
import static game.chunk.ChunkMeshGenerator.generateChunkMesh;
import static game.chunk.ChunkMeshGenerator.instantGeneration;
import static game.chunk.ChunkUpdateHandler.chunkUpdate;
import static game.light.Light.*;
import static game.player.Player.*;

public class Chunk {

    //DO NOT CHANGE THE DATA CONTAINER
    //private static final ConcurrentHashMap<Vector2i, ChunkMeshObject> mapMeshes = new ConcurrentHashMap<>();

    //this one holds keys for look ups
    private static final ConcurrentHashMap<Vector2i, Vector2i> chunkKeys    = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Vector2i, byte[]> blocks         = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Vector2i, byte[]> rotations      = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Vector2i, byte[]> lights         = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Vector2i, byte[]> heightmaps   = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Vector2i, Boolean> saveToDisk    = new ConcurrentHashMap<>();

    //mesh data can only be held on the main thread, so it can use faster containers
    private static final HashMap<Vector2i, Mesh[]> normalMeshes   = new HashMap<>();
    private static final HashMap<Vector2i, Mesh[]> liquidMeshes   = new HashMap<>();
    private static final HashMap<Vector2i, Mesh[]> allFaceMeshes  = new HashMap<>();

    private static final Vector2i[] keyArray = new Vector2i[0];

    public static Vector2i[] getChunkKeys(){
        return chunkKeys.keySet().toArray(keyArray);
    }

    public static Vector2i getChunkKey(Vector2i key){
        return chunkKeys.get(key);
    }
    public static Vector2i getChunkKey(int x, int z){
        return chunkKeys.get(new Vector2i(x,z));
    }

    //overload part 1
    public static byte[] getBlockData(int x, int z){
        return blocks.get(new Vector2i(x,z));
    }
    public static byte[] getRotationData(int x, int z){
        return rotations.get(new Vector2i(x,z));
    }
    public static byte[] getLightData(int x, int z){
        return lights.get(new Vector2i(x,z));
    }
    public static byte[] getHeightMapData(int x, int z){
        return heightmaps.get(new Vector2i(x,z));
    }
    //overload part 2
    public static byte[] getBlockData(Vector2i key){
        return blocks.get(key);
    }
    public static byte[] getRotationData(Vector2i key){
        return rotations.get(key);
    }
    public static byte[] getLightData(Vector2i key){
        return lights.get(key);
    }
    public static byte[] getHeightMapData(Vector2i key){
        return heightmaps.get(key);
    }
    //overload part 3 - immutable clones
    public static byte[] getBlockDataClone(int x, int z){
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] blockData = blocks.get(new Vector2i(x,z));
        if (blockData == null){
            return null;
        }
        return blockData.clone();
    }
    public static byte[] getRotationDataClone(int x, int z){
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] rotationData = rotations.get(new Vector2i(x,z));
        if (rotationData == null){
            return null;
        }
        return rotationData.clone();
    }
    public static byte[] getLightDataClone(int x, int z){
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] lightData =  lights.get(new Vector2i(x,z));
        if (lightData == null){
            return null;
        }
        return lightData.clone();
    }
    public static byte[] getHeightMapDataClone(int x, int z){
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] heightMapData = heightmaps.get(new Vector2i(x,z));
        if (heightMapData == null){
            return null;
        }
        return heightMapData.clone();
    }


    public static AbstractMap<Vector2i, Mesh[]> getNormalMeshes(){
        return normalMeshes;
    }

    public static AbstractMap<Vector2i, Mesh[]> getLiquidMeshes(){
        return liquidMeshes;
    }

    public static AbstractMap<Vector2i, Mesh[]> getAllFaceMeshes(){
        return allFaceMeshes;
    }



    //multiplayer chunk update
    public static void setChunk(int x, int z, byte[] blockData, byte[] rotationData, byte[] lightData, byte[] heightMapData) {
        // THIS CREATES A NEW OBJECT IN MEMORY! - it is necessary though, this needs a rework :L
        Vector2i key = new Vector2i(x, z);

        //don't allow old vertex data to leak - instead clone primitives
        Vector2i gottenChunk = chunkKeys.get(key);

        if (gottenChunk != null) {
            //todo: see if not doing .clone() causes memory leak - would reduce memory lookups
            blocks.replace(key, blockData.clone());
            rotations.replace(key, rotationData.clone());
            lights.replace(key, lightData.clone());
            heightmaps.replace(key,heightMapData.clone());
        } else {
            chunkKeys.put(key,key);
            blocks.put(key, blockData.clone());
            rotations.put(key, rotationData.clone());
            lights.put(key, lightData.clone());
            heightmaps.put(key,heightMapData.clone());
            //null meshes
            normalMeshes.put(key, new Mesh[8]);
            liquidMeshes.put(key, new Mesh[8]);
            allFaceMeshes.put(key, new Mesh[8]);
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
        // THIS CREATES A NEW OBJECT IN MEMORY!
        Mesh[] meshArray = normalMeshes.get(new Vector2i(chunkX, chunkZ));

        if (meshArray == null){
            if (newMesh != null) {
                newMesh.cleanUp(false);
            }
            return;
        }
        if (meshArray[yHeight] != null){
            meshArray[yHeight].cleanUp(false);
        }
        meshArray[yHeight] = newMesh;
    }

    public static void setChunkLiquidMesh(int chunkX, int chunkZ, int yHeight, Mesh newMesh){
        // THIS CREATES A NEW OBJECT IN MEMORY!
        Mesh[] meshArray = liquidMeshes.get(new Vector2i(chunkX, chunkZ));
        if (meshArray == null){
            if (newMesh != null) {
                newMesh.cleanUp(false);
            }
            return;
        }
        if (meshArray[yHeight] != null){
            meshArray[yHeight].cleanUp(false);
        }
        meshArray[yHeight] = newMesh;
    }

    public static void setChunkAllFacesMesh(int chunkX, int chunkZ, int yHeight, Mesh newMesh){
        // THIS CREATES A NEW OBJECT IN MEMORY!
        Mesh[] meshArray = allFaceMeshes.get(new Vector2i(chunkX, chunkZ));
        if (meshArray == null){
            if (newMesh != null) {
                newMesh.cleanUp(false);
            }
            return;
        }
        if (meshArray[yHeight] != null){
            meshArray[yHeight].cleanUp(false);
            meshArray[yHeight] = null;
        }
        meshArray[yHeight] = newMesh;
    }

    private static float saveTimer = 0f;
    public static void globalChunkSaveToDisk(){
        saveTimer += getDelta();
        //save interval is 16 seconds
        if (saveTimer >= 16f){
            updateWorldsPathToAvoidCrash();
            savePlayerPos(getPlayerPos());
            for (Vector2i key : chunkKeys.values()){
                Boolean needsToBeSaved = saveToDisk.get(key);
                if (needsToBeSaved != null && needsToBeSaved) { //null is also no or false
                    saveChunk(key.x, key.y,blocks.get(key).clone(), rotations.get(key).clone(), lights.get(key).clone(), heightmaps.get(key).clone());
                    saveToDisk.replace(key,false);
                }
            }
            saveTimer = 0f;
        }
    }

    //this re-generates chunk meshes with the light level
    public static void floodChunksWithNewLight(){
        for (Vector2i key : chunkKeys.values()){
            for (int y = 0; y < 8; y++) {
                chunkUpdate(key.x, key.y, y);
            }
        }
    }

    public static void globalFinalChunkSaveToDisk(){
        updateWorldsPathToAvoidCrash();
        for (Vector2i thisKey : chunkKeys.values()){
            //instantSave(thisKey);
            saveChunk(thisKey.x, thisKey.y,blocks.get(thisKey).clone(), rotations.get(thisKey).clone(), lights.get(thisKey).clone(), heightmaps.get(thisKey).clone());
            saveToDisk.replace(thisKey, false);
        }

        chunkKeys.clear();
        blocks.clear();
        lights.clear();
        heightmaps.clear();
        saveToDisk.clear();
    }

    public static boolean chunkStackContainsBlock(int chunkX, int chunkZ, int yHeight){
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] blockData = blocks.get(new Vector2i(chunkX, chunkZ));

        if (blockData == null){
            return false;
        }

        for (int x = 0; x < 16; x++){
            for (int z = 0; z < 16; z++){
                for (int y = yHeight * 16; y < (yHeight + 1) * 16; y++){
                    if (blockData[posToIndex(x,y,z)] != 0){
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

        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] heightMapData = heightmaps.get(new Vector2i(chunkX, chunkZ));

        if (heightMapData == null){
            return 555; //todo, handle this better
        }

        return heightMapData[posToIndex2D(blockX,blockZ)];
    }

    public static boolean underSunLight(int x, int y, int z){
        if (y > 127 || y < 0){
            return false;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] heightMapData = heightmaps.get(new Vector2i(chunkX, chunkZ));
        if (heightMapData == null){
            return false;
        }
        return heightMapData[posToIndex2D(blockX,blockZ)] < y + 1;
    }

    //overloaded block getter
    public static byte getBlock(int x,int y,int z){
        if (y > 127 || y < 0){
            return -1;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] blockData = blocks.get(new Vector2i(chunkX, chunkZ));
        if (blockData == null){
            return -1;
        }
        return blockData[posToIndex(blockX, y, blockZ)];
    }
    //overloaded block getter
    public static byte getBlock(Vector3i pos){
        if (pos.y > 127 || pos.y < 0){
            return -1;
        }
        int chunkX = (int)Math.floor(pos.x/16d);
        int chunkZ = (int)Math.floor(pos.z/16d);
        int blockX = (int)(pos.x - (16d*chunkX));
        int blockZ = (int)(pos.z - (16d*chunkZ));
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] blockData = blocks.get(new Vector2i(chunkX, chunkZ));
        if (blockData == null){
            return -1;
        }
        return blockData[posToIndex(blockX, pos.y, blockZ)];
    }

    //overloaded getter for rotation
    public static byte getBlockRotation(int x, int y, int z){
        if (y > 127 || y < 0){
            return -1;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] rotationData = rotations.get(new Vector2i(chunkX, chunkZ));
        if (rotationData == null){
            return 0;
        }
        return rotationData[posToIndex(blockX, y, blockZ)];
    }
    //overloaded getter for rotation
    public static byte getBlockRotation(Vector3i pos){
        if (pos.y > 127 || pos.y < 0){
            return -1;
        }
        int chunkX = (int)Math.floor(pos.x/16d);
        int chunkZ = (int)Math.floor(pos.z/16d);
        int blockX = (int)(pos.x - (16d*chunkX));
        int blockZ = (int)(pos.z - (16d*chunkZ));
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] rotationData = rotations.get(new Vector2i(chunkX, chunkZ));
        if (rotationData == null){
            return 0;
        }
        return rotationData[posToIndex(blockX, pos.y, blockZ)];
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
        // THIS CREATES A NEW OBJECT IN MEMORY!
        Vector2i key = new Vector2i(chunkX, chunkZ);
        byte[] blockData = blocks.get(key);
        byte[] rotationData = rotations.get(key);
        byte[] heightMapData = heightmaps.get(key);

        if (blockData == null || rotationData == null){
            return;
        }

        blockData[posToIndex(blockX,y, blockZ)] = newBlock;
        rotationData[posToIndex(blockX,y, blockZ)] = rot;

        if (newBlock == 0){
            if (heightMapData[posToIndex2D(blockX,blockZ)] == y){
                for (int yCheck = heightMapData[posToIndex2D(blockX,blockZ)]; yCheck > 0; yCheck--){
                    if (blockData[posToIndex(blockX, yCheck, blockZ)] != 0){
                        heightMapData[posToIndex2D(blockX,blockZ)] = (byte) yCheck;
                        break;
                    }
                }
            }
        } else {
            if (heightMapData[posToIndex2D(blockX,blockZ)] < y){
                heightMapData[posToIndex2D(blockX,blockZ)] = (byte) y;
            }
        }
        saveToDisk.replace(key, true);
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
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] lightData = lights.get(new Vector2i(chunkX, chunkZ));
        if (lightData == null){
            return;
        }
        lightData[posToIndex(blockX, y, blockZ)] = setByteNaturalLight(lightData[posToIndex(blockX, y, blockZ)],newLight);
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
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] lightData = lights.get(new Vector2i(chunkX, chunkZ));
        if (lightData == null){
            return;
        }
        lightData[posToIndex(blockX, y, blockZ)] = setByteTorchLight(lightData[posToIndex(blockX, y, blockZ)], newLight);
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
        // THIS CREATES A NEW OBJECT IN MEMORY!
        Vector2i key = new Vector2i(chunkX, chunkZ);

        byte[] blockData = blocks.get(key);
        byte[] rotationData = rotations.get(key);
        byte[] heightMapData = heightmaps.get(key);
        byte[] lightData = lights.get(key);

        if (blockData == null || rotationData == null || heightMapData == null){
            return;
        }

        byte oldBlock = blockData[posToIndex(blockX, y, blockZ)];

        blockData[posToIndex(blockX, y, blockZ)] = 0;
        rotationData[posToIndex(blockX, y, blockZ)] = 0;
        if (heightMapData[posToIndex2D(blockX,blockZ)] == y){
            for (int yCheck = heightMapData[posToIndex2D(blockX,blockZ)]; yCheck > 0; yCheck--){
                if (blockData[posToIndex(blockX, yCheck, blockZ)] != 0){
                    heightMapData[posToIndex2D(blockX,blockZ)] = (byte) yCheck;
                    break;
                }
            }
        }

        lightFloodFill(x, y, z);
        torchFloodFill(x,y,z);

        saveToDisk.replace(key, true);

        lightData[posToIndex(blockX, y, blockZ)] = setByteNaturalLight(lightData[posToIndex(blockX, y, blockZ)], getImmediateLight(x,y,z));

        if (!getIfMultiplayer()) {
            onDigCall(oldBlock, x, y, z);
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
        // THIS CREATES A NEW OBJECT IN MEMORY!
        Vector2i key = new Vector2i(chunkX, chunkZ);

        byte[] blockData = blocks.get(key);
        byte[] rotationData = rotations.get(key);
        byte[] heightMapData = heightmaps.get(key);

        if (blockData == null || rotationData == null || heightMapData == null){
            return;
        }

        blockData[posToIndex(blockX, y, blockZ)] = ID;
        rotationData[posToIndex(blockX, y, blockZ)] =  rot;

        System.out.println("ADD A LIGHT PROPAGATES OR TRANSLUCENT THING TO PLACE BLOCK!");
        //todo: replace isBlockWalkable with isBlockTranslucent or something!
        if (isBlockWalkable(ID) && heightMapData[posToIndex2D(blockX,blockZ)] < y){
            heightMapData[posToIndex2D(blockX,blockZ)] = (byte) y;
        }

        lightFloodFill(x, y, z);
        torchFloodFill(x,y,z);

        saveToDisk.replace(key, true);

        if (!getIfMultiplayer()) {
            // THIS CREATES A NEW OBJECT IN MEMORY!
            onPlaceCall(ID, x, y, z);
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
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] lightData = lights.get(new Vector2i(chunkX, chunkZ));

        if (lightData == null){
            return 0;
        }

        int index = posToIndex(blockX, y, blockZ);

        byte naturalLightOfBlock = getByteNaturalLight(lightData[index]);

        byte currentGlobalLightLevel = getCurrentGlobalLightLevel();

        if (naturalLightOfBlock > currentGlobalLightLevel){
            naturalLightOfBlock = currentGlobalLightLevel;
        }

        byte torchLight = getByteTorchLight(lightData[index]);

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
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] lightData = lights.get(new Vector2i(chunkX,chunkZ));

        if (lightData == null){
            return 0;
        }

        return getByteNaturalLight(lightData[posToIndex(blockX, y, blockZ)]);
    }

    public static byte getTorchLight(int x,int y,int z){
        if (y > 127 || y < 0){
            return 0;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] lightData = lights.get(new Vector2i(chunkX,chunkZ));

        if (lightData == null){
            return 0;
        }

        return getByteTorchLight(lightData[posToIndex(blockX, y, blockZ)]);
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
        // THIS CREATES A NEW OBJECT IN MEMORY!
        if (chunkKeys.get(new Vector2i(chunkX + 1, chunkZ)) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX+1, chunkZ, y);
            }
        }

        // THIS CREATES A NEW OBJECT IN MEMORY!
        if (chunkKeys.get(new Vector2i(chunkX-1, chunkZ)) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX-1, chunkZ, y);
            }
        }

        // THIS CREATES A NEW OBJECT IN MEMORY!
        if (chunkKeys.get(new Vector2i(chunkX, chunkZ+1)) != null){
            for (int y = 0; y < 8; y++){
                chunkUpdate(chunkX, chunkZ+1, y);
            }
        }

        // THIS CREATES A NEW OBJECT IN MEMORY!
        if (chunkKeys.get(new Vector2i(chunkX, chunkZ-1)) != null){
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
                    // THIS CREATES A NEW OBJECT IN MEMORY!
                    if (chunkKeys.get(new Vector2i(x,z)) == null){
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
        for (Vector2i key : chunkKeys.values()){
            if (getChunkDistanceFromPlayer(key.x,key.y) > chunkRenderDistance){
                addChunkToDeletionQueue(key.x,key.y);
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
                    // THIS CREATES A NEW OBJECT IN MEMORY!
                    if (chunkKeys.get(new Vector2i(x,z)) == null){
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


                //clean up mesh data
                Mesh[] normalMeshData = normalMeshes.get(key);

                for (Mesh meshData : normalMeshData){
                    if (meshData != null){
                        meshData.cleanUp(false);
                    }
                }

                Mesh[] liquidMeshData = liquidMeshes.get(key);

                for (Mesh meshData : liquidMeshData){
                    if (meshData != null){
                        meshData.cleanUp(false);
                    }
                }

                Mesh[] allFacesMeshData = allFaceMeshes.get(key);

                for (Mesh meshData : allFacesMeshData){
                    if (meshData != null){
                        meshData.cleanUp(false);
                    }
                }

                saveChunk(key.x, key.y,blocks.get(key).clone(), rotations.get(key).clone(), lights.get(key).clone(), heightmaps.get(key).clone());

                chunkKeys.remove(key);
                blocks.remove(key);
                rotations.remove(key);
                heightmaps.remove(key);
                lights.remove(key);

                normalMeshes.remove(key);
                liquidMeshes.remove(key);
                allFaceMeshes.remove(key);
            }
        }
    }

    //returns -1 if fails
    public static int getMobSpawnYPos(int x, int z){
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));

        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] blockData = blocks.get(new Vector2i(chunkX, chunkZ));

        if (blockData == null){
            return 0;
        }

        //simple algorithm for now
        for (int y = 127; y >= 0; y--){
            if (blockData[posToIndex(blockX,y, blockZ)] == 0){
                if (blockData[posToIndex(blockX,y-1, blockZ)] != 0 && blockData[posToIndex(blockX,y+1, blockZ)] == 0){
                    return y;
                }
            }
        }

        return -1;
    }

    //todo: this needs a new name
    //this dispatches to the SQLite thread, checks if it exists in the data base
    //then it either deserializes it or it tells the chunk mesh generator thread
    //to create a new one if it doesn't exist
    public static void genBiome(int chunkX, int chunkZ) {
        loadChunk(chunkX,chunkZ);
    }

    public static void cleanChunkDataMemory(){

        for (Mesh[] meshArray : normalMeshes.values()){
            for (Mesh meshData : meshArray){
                if (meshData != null){
                    meshData.cleanUp(false);
                }
            }
        }
        for (Mesh[] meshArray : liquidMeshes.values()){
            for (Mesh meshData : meshArray){
                if (meshData != null){
                    meshData.cleanUp(false);
                }
            }
        }
        for (Mesh[] meshArray : allFaceMeshes.values()){
            for (Mesh meshData : meshArray){
                if (meshData != null){
                    meshData.cleanUp(false);
                }
            }
        }


        blocks.clear();
        rotations.clear();
        lights.clear();
        heightmaps.clear();
        saveToDisk.clear();

        normalMeshes.clear();
        liquidMeshes.clear();
        allFaceMeshes.clear();
    }
}
