package game.chunk;

import engine.disk.PrimitiveChunkObject;
import engine.disk.Disk;
import engine.graphics.Mesh;
import engine.settings.Settings;
import engine.time.Delta;
import game.blocks.BlockDefinitionContainer;
import game.light.Light;
import game.player.Player;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

import static java.lang.Math.hypot;
import static java.lang.Math.max;
import static java.util.Objects.requireNonNull;
import static org.joml.Math.floor;

public class Chunk {
    private Disk disk;
    private ChunkUpdateHandler chunkUpdateHandler;

    private Player player;
    private final BlockDefinitionContainer blockDefinitionContainer = new BlockDefinitionContainer();
    private Light light;
    private ChunkMeshGenerator chunkMeshGenerator;

    private Settings settings;
    private Delta delta;
    private float saveTimer = 0f;
    private final ConcurrentLinkedQueue<ChunkObject> map = new ConcurrentLinkedQueue<>();

    public Chunk(){
    }

    public void setDelta(Delta delta){
        if (this.delta == null){
            this.delta = delta;
        }
    }

    public void setPlayer(Player player){
        if (this.player == null){
            this.player = player;
        }
    }

    public void setSettings(Settings settings){
        if (this.settings == null){
            this.settings = settings;
        }
    }

    public void setSqLiteDiskHandler(Disk disk){
        if (this.disk == null) {
            this.disk = disk;
        }
    }

    public void setChunkUpdateHandler(ChunkUpdateHandler chunkUpdateHandler){
        if (this.chunkUpdateHandler == null){
            this.chunkUpdateHandler = chunkUpdateHandler;
        }
    }

    public void setLight(Light light){
        if (this.light == null){
            this.light = light;
        }
    }

    public void setChunkMeshGenerator(ChunkMeshGenerator chunkMeshGenerator){
        if (this.chunkMeshGenerator == null){
            this.chunkMeshGenerator = chunkMeshGenerator;
        }
    }

    public Collection<ChunkObject> getAllChunks(){
        return map;
    }

    public ChunkObject getChunk(Vector2i newPos){
        return map.stream()
                .filter(chunkObject -> newPos.equals(chunkObject.getPos()))
                .findFirst()
                .orElse(null);
    }

    public float hover(float thisHover){
        if (thisHover < 0f){
            thisHover += (float)delta.getDelta() * 50f;
            if (thisHover >= 0f){
                thisHover = 0f;
            }
        }
        return thisHover;
    }

    public void doChunksHoveringUpThing(){
        map.forEach(chunkObject -> chunkObject.setHover(hover(chunkObject.getHover())));
    }

    public void initialChunkPayload(){
        //create the initial map in memory
        int chunkRenderDistance = settings.getRenderDistance();
        Vector2i currentChunk = player.getPlayerCurrentChunk();
        IntStream.range(-chunkRenderDistance + currentChunk.x, chunkRenderDistance + currentChunk.x)
                .forEach(x -> IntStream
                        .range(-chunkRenderDistance + currentChunk.y, chunkRenderDistance + currentChunk.y)
                        .filter(y -> getChunkDistanceFromPlayer(x,y) <= chunkRenderDistance)
                        .forEach(y -> loadChunk(x, y))
                );
    }

    private double getChunkDistanceFromPlayer(int x, int z){
        Vector2i currentChunk = player.getPlayerCurrentChunk();
        return max(getDistance(0, currentChunk.y, 0, z), getDistance(currentChunk.x, 0, x, 0));
    }

    public void setChunkNormalMesh(int chunkX, int chunkZ, int yHeight, Mesh newMesh){
        map.stream()
                .filter(chunkObject -> chunkObject.getPos().equals(chunkX, chunkZ))
                .findFirst()
                .ifPresentOrElse(
                        chunkObject -> chunkObject.replaceOrSetNormalMesh(yHeight, newMesh),
                        this::nothing
                );
    }

    public void setChunkLiquidMesh(int chunkX, int chunkZ, int yHeight, Mesh newMesh){
        map.stream()
                .filter(chunkObject -> chunkObject.getPos().equals(chunkX, chunkZ))
                .findFirst()
                .ifPresentOrElse(
                        chunkObject -> chunkObject.replaceOrSetLiquidMesh(yHeight, newMesh),
                        this::nothing
                );
    }

    public void setChunkAllFacesMesh(int chunkX, int chunkZ, int yHeight, Mesh newMesh){
        map.stream()
                .filter(chunkObject -> chunkObject.getPos().equals(chunkX, chunkZ))
                .findFirst()
                .ifPresentOrElse(
                        chunkObject -> chunkObject.replaceOrSetAllFaceMesh(yHeight, newMesh),
                        this::nothing
                );
    }

    private ChunkObject getChunk(int x, int z){
        return map.stream()
                .filter(chunk -> chunk.getPos().equals(x, z))
                .findFirst()
                .orElse(null);
    }

    public boolean chunkExists(Vector2i pos){
        return map.stream()
                .anyMatch(chunkObject -> chunkObject.getPos().equals(pos));
    }

    private void nothing(){}

    private void internalChunkAdd(PrimitiveChunkObject primitiveChunkObject){
        map.add(new ChunkObject(primitiveChunkObject));
        //System.out.println(Arrays.deepToString(map.toArray()));
        //send to chunk object generator
        IntStream.range(0, 8).forEach(y -> chunkUpdateHandler.chunkUpdate(primitiveChunkObject.pos.x,primitiveChunkObject.pos.y,y));
    }

    public void addNewChunk(PrimitiveChunkObject primitiveChunkObject){
        map.stream()
                .filter(chunkObject -> chunkObject.getPos().equals(primitiveChunkObject.pos))
                .findFirst()
                .ifPresentOrElse(
                        chunkObject -> nothing(),
                        () -> internalChunkAdd(primitiveChunkObject)
                );
    }

    private void chunkSaver(ChunkObject chunkObject){
        disk.saveChunk(chunkObject);
        chunkObject.setSaveToDisk(false);
    }

    public void globalChunkSaveToDisk(){
        this.saveTimer += this.delta.getDelta();
        //save interval is 16 seconds
        if (saveTimer < 16){
            return;
        }
        map.stream()
                .filter(ChunkObject::getSaveToDisk)
                .forEach(this::chunkSaver);
        saveTimer = 0f;
    }

    private void floodLight(Vector2i pos){
        IntStream.range(0,8).forEach(y ->chunkUpdateHandler.chunkUpdate(pos.x, pos.y, y));
    }
    //this re-generates chunk meshes with the light level
    public void floodChunksWithNewLight(){
        map.forEach(chunkObject -> floodLight(chunkObject.getPos()));
    }

    public void globalFinalChunkSaveToDisk(){
        map.forEach(this::chunkSaver);
        map.clear();
    }

    public boolean underSunLight(Vector3i pos){
        if (pos.y > 127 || pos.y < 0){
            return false;
        }
        int chunkX = (int)floor(pos.x/16d);
        int chunkZ = (int)floor(pos.z/16d);
        int blockX = (int)(pos.x - (16d*chunkX));
        int blockZ = (int)(pos.z - (16d*chunkZ));

        return requireNonNull(map.stream()
                .filter(chunkObject -> chunkObject.getPos().equals(chunkX, chunkZ))
                .findFirst()
                .orElse(null))
                .getHeightMap()[posToIndex2D(blockX,blockZ)] < pos.y + 1;
    }

    public byte getBlock(Vector3i pos){
        if (pos.y > 127 || pos.y < 0){
            return -1;
        }
        int chunkX = (int)floor(pos.x/16d);
        int chunkZ = (int)floor(pos.z/16d);
        int blockX = (int)(pos.x - (16d*chunkX));
        int blockZ = (int)(pos.z - (16d*chunkZ));

        ChunkObject chunk = map.stream()
                .filter(chunkObject -> chunkObject.getPos().equals(chunkX, chunkZ))
                .findFirst()
                .orElse(null);
        if (chunk == null){
            return -1;
        }

        return chunk.getBlock()[posToIndex(blockX, pos.y, blockZ)];
    }

    public byte getBlockRotation(Vector3i pos){
        if (pos.y > 127 || pos.y < 0){
            return -1;
        }
        int chunkX = (int)floor(pos.x/16d);
        int chunkZ = (int)floor(pos.z/16d);
        int blockX = (int)(pos.x - (16d*chunkX));
        int blockZ = (int)(pos.z - (16d*chunkZ));

        return requireNonNull(map.stream()
                .filter(chunkObject -> chunkObject.getPos().equals(chunkX, chunkZ))
                .findFirst()
                .orElse(null))
                .getRotation()[posToIndex(blockX, pos.y, blockZ)];
    }

    public void setBlock(Vector3i pos, byte newBlock, byte rot){
        if (pos.y > 127 || pos.y < 0){
            return;
        }
        int yPillar = (int)floor(pos.y/16d);
        int chunkX = (int)floor(pos.x/16d);
        int chunkZ = (int)floor(pos.z/16d);
        int blockX = (int)(pos.x - (16d*chunkX));
        int blockZ = (int)(pos.z - (16d*chunkZ));

        int iPos = posToIndex(blockX,pos.y, blockZ);

        ChunkObject chunkObject = getChunk(chunkX,chunkZ);

        if (chunkObject == null){
            return;
        }

        chunkObject.getBlock()[iPos] = newBlock;
        chunkObject.getRotation()[iPos] = rot;

        byte[] heightMapData = chunkObject.getHeightMap();
        byte[] blockData = chunkObject.getBlock();

        if (newBlock == 0) {
            if (heightMapData[posToIndex2D(blockX, blockZ)] == pos.y) {
                heightMapData[posToIndex2D(blockX, blockZ)] = (byte) IntStream.range(heightMapData[posToIndex2D(blockX, blockZ)], 0)
                        .filter(y -> blockData[posToIndex(blockX, y, blockZ)] != 0)
                        .findFirst().getAsInt();
            }
        } else {
            if (heightMapData[posToIndex2D(blockX, blockZ)] < pos.y) {
                heightMapData[posToIndex2D(blockX, blockZ)] = (byte) pos.y;
            }
        }
        chunkObject.setSaveToDisk(true);
        this.chunkUpdateHandler.chunkUpdate(chunkX,chunkZ,yPillar);
        updateNeighbor(chunkX, chunkZ,blockX,pos.y,blockZ);
    }

    public void setNaturalLight(Vector3i pos, byte newLight){
        if (pos.y > 127 || pos.y < 0){
            return;
        }
        int yPillar = (int)floor(pos.y/16d);
        int chunkX = (int)floor(pos.x/16d);
        int chunkZ = (int)floor(pos.z/16d);
        int blockX = (int)(pos.x - (16d*chunkX));
        int blockZ = (int)(pos.z - (16d*chunkZ));
        ChunkObject chunkObject = getChunk(chunkX,chunkZ);
        if (chunkObject == null){
            return;
        }
        byte[] lightData = chunkObject.getLight();
        lightData[posToIndex(blockX, pos.y, blockZ)] = setByteNaturalLight(lightData[posToIndex(blockX, pos.y, blockZ)],newLight);
        this.chunkUpdateHandler.chunkUpdate(chunkX,chunkZ,yPillar);
        updateNeighbor(chunkX, chunkZ,blockX,pos.y,blockZ);
    }

    public void setTorchLight(int x,int y,int z, byte newLight){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)floor(y/16d);
        int chunkX = (int)floor(x/16d);
        int chunkZ = (int)floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        ChunkObject chunkObject = getChunk(chunkX,chunkZ);
        if (chunkObject == null){
            return;
        }
        byte[] lightData = chunkObject.getLight();
        lightData[posToIndex(blockX, y, blockZ)] = setByteTorchLight(lightData[posToIndex(blockX, y, blockZ)], newLight);
        this.chunkUpdateHandler.chunkUpdate(chunkX,chunkZ,yPillar);
        updateNeighbor(chunkX, chunkZ,blockX,y,blockZ);
    }


    public void digBlock(Vector3i pos){
        if (pos.y > 127 || pos.y < 0){
            return;
        }
        int yPillar = (int)floor(pos.y/16d);
        int chunkX = (int)floor(pos.x/16d);
        int chunkZ = (int)floor(pos.z/16d);
        int blockX = (int)(pos.x - (16d*chunkX));
        int blockZ = (int)(pos.z - (16d*chunkZ));

        ChunkObject chunkObject = getChunk(chunkX,chunkZ);

        if (chunkObject == null){
            return;
        }

        byte[] blockData = chunkObject.getBlock();
        byte[] rotationData = chunkObject.getRotation();
        byte[] heightMapData = chunkObject.getHeightMap();
        byte[] lightData = chunkObject.getLight();

        blockData[posToIndex(blockX, pos.y, blockZ)] = 0;
        rotationData[posToIndex(blockX, pos.y, blockZ)] = 0;

        if (heightMapData[posToIndex2D(blockX, blockZ)] == pos.y) {
            heightMapData[posToIndex2D(blockX, blockZ)] = (byte) IntStream.range(heightMapData[posToIndex2D(blockX, blockZ)], 0)
                    .filter(y -> blockData[posToIndex(blockX, y, blockZ)] != 0)
                    .findFirst().getAsInt();
        }


        light.lightFloodFill(pos.x, pos.y, pos.z);
        light.torchFloodFill(pos.x, pos.y, pos.z);

        chunkObject.setSaveToDisk(true);

        lightData[posToIndex(blockX, pos.y, blockZ)] = setByteNaturalLight(lightData[posToIndex(blockX, pos.y, blockZ)], light.getImmediateLight(pos.x,pos.y,pos.z));

        chunkMeshGenerator.instantGeneration(chunkX,chunkZ,yPillar);
        instantUpdateNeighbor(chunkX, chunkZ,blockX,pos.y,blockZ);//instant update
    }

    public void placeBlock(Vector3i pos, byte ID, byte rot){
        if (pos.y > 127 || pos.y < 0){
            return;
        }
        int yPillar = (int)floor(pos.y/16d);
        int chunkX = (int)floor(pos.x/16d);
        int chunkZ = (int)floor(pos.z/16d);
        int blockX = (pos.x - (16*chunkX));
        int blockZ = (pos.z - (16*chunkZ));

        ChunkObject chunkObject = getChunk(chunkX,chunkZ);

        if (chunkObject == null){
            return;
        }

        byte[] blockData = chunkObject.getBlock();
        byte[] rotationData = chunkObject.getRotation();
        byte[] heightMapData = chunkObject.getHeightMap();

        blockData[posToIndex(blockX, pos.y, blockZ)] = ID;
        rotationData[posToIndex(blockX, pos.y, blockZ)] =  rot;

        System.out.println("ADD A LIGHT PROPAGATES OR TRANSLUCENT THING TO PLACE BLOCK!");

        if (blockDefinitionContainer.getWalkable(ID) && heightMapData[posToIndex2D(blockX,blockZ)] < pos.y){
            heightMapData[posToIndex2D(blockX,blockZ)] = (byte) pos.y;
        }

        light.lightFloodFill(pos.x, pos.y, pos.z);
        light.torchFloodFill(pos.x, pos.y, pos.z);

        chunkObject.setSaveToDisk(true);

        chunkMeshGenerator.instantGeneration(chunkX,chunkZ,yPillar);
        instantUpdateNeighbor(chunkX, chunkZ,blockX,pos.y,blockZ);//instant update
    }

    public byte getLight(int x,int y,int z){
        if (y > 127 || y < 0){
            return 0;
        }
        int chunkX = (int)floor(x/16d);
        int chunkZ = (int)floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));

        ChunkObject chunkObject = getChunk(chunkX,chunkZ);

        if (chunkObject == null){
            return 0;
        }

        byte[] lightData = chunkObject.getLight();

        if (lightData == null){
            return 0;
        }

        int index = posToIndex(blockX, y, blockZ);

        byte naturalLightOfBlock = getByteNaturalLight(lightData[index]);

        byte currentGlobalLightLevel = light.getCurrentGlobalLightLevel();

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

    public byte getNaturalLight(int x,int y,int z){
        if (y > 127 || y < 0){
            return 0;
        }
        int chunkX = (int)floor(x/16d);
        int chunkZ = (int)floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        ChunkObject chunkObject = getChunk(chunkX, chunkZ);
        if (chunkObject == null) {
            return 0;
        }
        return getByteNaturalLight(chunkObject.getLight()[posToIndex(blockX, y, blockZ)]);
    }

    public byte getTorchLight(int x,int y,int z){
        if (y > 127 || y < 0){
            return 0;
        }
        int chunkX = (int)floor(x/16d);
        int chunkZ = (int)floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        ChunkObject chunkObject = getChunk(chunkX, chunkZ);
        if (chunkObject == null){
            return 0;
        }
        return getByteTorchLight(chunkObject.getLight()[posToIndex(blockX, y, blockZ)]);
    }

    private void instantUpdateNeighbor(int chunkX, int chunkZ, int x, int y, int z){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)floor(y/16d);
        switch (y) {
            case 112, 96, 80, 64, 48, 32, 16 -> chunkMeshGenerator.generateChunkMesh(chunkX, chunkZ, yPillar - 1);
            case 111, 95, 79, 63, 47, 31, 15 -> chunkMeshGenerator.generateChunkMesh(chunkX, chunkZ, yPillar + 1);
        }
        if (x == 15){ //update neighbor
            chunkMeshGenerator.instantGeneration(chunkX+1, chunkZ, yPillar);
        }
        if (x == 0){
            chunkMeshGenerator.instantGeneration(chunkX-1, chunkZ, yPillar);
        }
        if (z == 15){
            chunkMeshGenerator.instantGeneration(chunkX, chunkZ+1, yPillar);
        }
        if (z == 0){
            chunkMeshGenerator.instantGeneration(chunkX, chunkZ-1, yPillar);
        }
    }

    private void updateNeighbor(int chunkX, int chunkZ, int x, int y, int z){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)floor(y/16d);
        switch (y) {
            case 112, 96, 80, 64, 48, 32, 16 -> this.chunkUpdateHandler.chunkUpdate(chunkX, chunkZ, yPillar - 1);
            case 111, 95, 79, 63, 47, 31, 15 -> this.chunkUpdateHandler.chunkUpdate(chunkX, chunkZ, yPillar + 1);
        }
        if (x == 15){ //update neighbor
            this.chunkUpdateHandler.chunkUpdate(chunkX+1, chunkZ, yPillar);
        }
        if (x == 0){
            this.chunkUpdateHandler.chunkUpdate(chunkX-1, chunkZ, yPillar);
        }
        if (z == 15){
            this.chunkUpdateHandler.chunkUpdate(chunkX, chunkZ+1, yPillar);
        }
        if (z == 0){
            this.chunkUpdateHandler.chunkUpdate(chunkX, chunkZ-1, yPillar);
        }
    }

    public void fullNeighborUpdate(int chunkX, int chunkZ){
        if (getChunk(chunkX + 1, chunkZ) != null){
            IntStream.range(0,8).forEach(y->this.chunkUpdateHandler.chunkUpdate(chunkX+1, chunkZ, y));
        }
        if (getChunk(chunkX-1, chunkZ) != null){
            IntStream.range(0,8).forEach(y->this.chunkUpdateHandler.chunkUpdate(chunkX-1, chunkZ, y));
        }
        if (getChunk(chunkX, chunkZ+1) != null){
            IntStream.range(0,8).forEach(y->this.chunkUpdateHandler.chunkUpdate(chunkX, chunkZ+1, y));
        }
        if (getChunk(chunkX, chunkZ-1) != null){
            IntStream.range(0,8).forEach(y->this.chunkUpdateHandler.chunkUpdate(chunkX, chunkZ-1, y));
        }
    }

    public void generateNewChunks(){
        //create the initial map in memory
        int chunkRenderDistance = settings.getRenderDistance();
        Vector2i currentChunk = player.getPlayerCurrentChunk();

        IntStream.range(-chunkRenderDistance + currentChunk.x, chunkRenderDistance + currentChunk.x)
                .forEach(x -> IntStream
                        .range(-chunkRenderDistance + currentChunk.y, chunkRenderDistance + currentChunk.y)
                        .filter(y -> getChunkDistanceFromPlayer(x,y) <= chunkRenderDistance)
                        .forEach(y -> loadChunk(x, y))
                );
        //scan map for out of range chunks
        map.stream()
                .filter(chunk -> getChunkDistanceFromPlayer(chunk.getPos().x,chunk.getPos().y) > chunkRenderDistance)
                .forEach(this::deleteChunk);
    }

    public void deleteChunk(ChunkObject chunk){
        if (chunk == null){
            return;
        }
        //clean up mesh data
        deleteChunkMesh(chunk);
        disk.saveChunk(chunk.getPos(),chunk.getBlock().clone(), chunk.getRotation().clone(), chunk.getLight().clone(), chunk.getHeightMap().clone());
        map.remove(chunk);
    }

    //returns -1 if fails
    public int getMobSpawnYPos(int x, int z){
        int chunkX = (int)floor(x/16d);
        int chunkZ = (int)floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));

        ChunkObject chunk = getChunk(chunkX, chunkZ);

        if (chunk == null){
            return -1;
        }

        byte[] blockData = chunk.getBlock();

        if (blockData == null){
            return -1;
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

    //this dispatches to the SQLite thread, checks if it exists in the database
    //then it either deserializes it or it tells the chunk mesh generator thread
    //to create a new one if it doesn't exist
    public void loadChunk(int chunkX, int chunkZ) {
        disk.loadChunk(new Vector2i(chunkX,chunkZ));
        fullNeighborUpdate(chunkX, chunkZ);
    }

    private void cleanUpMesh(Mesh mesh){
        if (mesh == null){
            return;
        }
        mesh.cleanUp(false);
    }

    private void deleteChunkMesh(ChunkObject chunk){
        Arrays.stream(chunk.getNormalMeshArray())
                .sequential()
                .forEach(
                        this::cleanUpMesh
                );
        Arrays.stream(chunk.getLiquidMeshArray())
                .sequential()
                .forEach(
                        this::cleanUpMesh
                );
        Arrays.stream(chunk.getAllFaceMeshArray())
                .sequential()
                .forEach(
                        this::cleanUpMesh
                );
    }

    public void cleanChunkDataMemory(){
        map.forEach(this::deleteChunkMesh);
        map.clear();
    }

    //chunk math
    //private final static int xMax = 16;
    //private final static int yMax = 128;
    //private final static int length = xMax * yMax; // 2048
    private int posToIndex( int x, int y, int z ) {
        return (z * 2048) + (y * 16) + x;
    }

    //make the inverse of this eventually
    private int posToIndex2D(int x, int z){
        return (z * 16) + x;
    }

    private double getDistance(double x1, double z1, double x2, double z2){
        return hypot((x1 - x2), hypot(0,(z1 - z2)));
    }

    private byte getByteTorchLight(byte input){
        return (byte) (input & ((1 << 4) - 1));
    }
    private byte getByteNaturalLight(byte input){
        return (byte) (((1 << 4) - 1) & input >> 4);
    }

    private byte setByteTorchLight(byte input, byte newValue){
        byte naturalLight = getByteNaturalLight(input);
        return (byte) (naturalLight << 4 | newValue);
    }

    private byte setByteNaturalLight(byte input, byte newValue){
        byte torchLight = getByteTorchLight(input);
        return (byte) (newValue << 4 | torchLight);
    }
}
