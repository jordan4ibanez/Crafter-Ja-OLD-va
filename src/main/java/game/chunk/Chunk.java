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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

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
    private final ConcurrentHashMap<Vector2i, ChunkObject> map = new ConcurrentHashMap<>();

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
        return map.values();
    }

    public ChunkObject getChunk(Vector2i pos){
        return map.get(pos);
    }


    public void doChunksHoveringUpThing(Delta delta){
        for (ChunkObject thisChunk : map.values()){
            float thisHover = thisChunk.getHover();
            if (thisHover < 0f){
                thisHover += (float)delta.getDelta() * 50f;
                if (thisHover >= 0f){
                    thisHover = 0f;
                }
                thisChunk.setHover(thisHover);
            }
        }
    }

    public void initialChunkPayload(){
        //create the initial map in memory
        int chunkRenderDistance = settings.getRenderDistance();
        Vector2i currentChunk = player.getPlayerCurrentChunk();
        for (int x = -chunkRenderDistance + currentChunk.x; x < chunkRenderDistance + currentChunk.x; x++){
            for (int z = -chunkRenderDistance + currentChunk.y; z< chunkRenderDistance + currentChunk.y; z++){
                if (getChunkDistanceFromPlayer(x,z) <= chunkRenderDistance){
                    genBiome(x,z);
                    for (int y = 0; y < 8; y++){
                        this.chunkUpdateHandler.chunkUpdate(x,z,y);
                    }
                }
            }
        }
    }

    /*
    public void initialChunkPayloadMultiplayer(){
        //create the initial map in memory
        int chunkRenderDistance = settings.getRenderDistance();
        Vector3i currentChunk = getPlayerCurrentChunk();
        for (int x = -chunkRenderDistance + currentChunk.x; x < chunkRenderDistance + currentChunk.x; x++){
            for (int z = -chunkRenderDistance + currentChunk.z; z< chunkRenderDistance + currentChunk.z; z++){
                if (getChunkDistanceFromPlayer(x,z) <= chunkRenderDistance){
                    sendOutChunkRequest(new ChunkRequest(x,z, getPlayerName()));
                }
            }
        }
    }
     */

    private double getChunkDistanceFromPlayer(int x, int z){
        Vector2i currentChunk = player.getPlayerCurrentChunk();
        return Math.max(getDistance(0,0,currentChunk.y, 0, 0, z), getDistance(currentChunk.x,0,0, x, 0, 0));
    }

    public void setChunkNormalMesh(int chunkX, int chunkZ, int yHeight, Mesh newMesh){
        map.get(new Vector2i(chunkX, chunkZ)).replaceOrSetNormalMesh(yHeight, newMesh);
    }

    public void setChunkLiquidMesh(int chunkX, int chunkZ, int yHeight, Mesh newMesh){
        map.get(new Vector2i(chunkX, chunkZ)).replaceOrSetLiquidMesh(yHeight, newMesh);
    }

    public void setChunkAllFacesMesh(int chunkX, int chunkZ, int yHeight, Mesh newMesh){
        map.get(new Vector2i(chunkX, chunkZ)).replaceOrSetAllFaceMesh(yHeight,newMesh);
    }

    public boolean chunkExists(Vector2i pos){
        return map.get(pos) != null;
    }

    public void addNewChunk(PrimitiveChunkObject primitiveChunkObject){
        ChunkObject gottenChunk = map.get(primitiveChunkObject.pos);
        if (gottenChunk == null){
            System.out.println("I AM PUTTING LE CHUNK IN LE MAP BOI");
            map.put(new Vector2i(primitiveChunkObject.pos), new ChunkObject(primitiveChunkObject));
            //send to chunk object generator
        }
    }

    public void globalChunkSaveToDisk(){
        this.saveTimer += this.delta.getDelta();
        //save interval is 16 seconds
        if (this.saveTimer >= 16f){
            for (ChunkObject chunk : map.values()){
                if (chunk.getSaveToDisk()) {
                    disk.saveChunk(chunk.getPos(),chunk.getBlock().clone(), chunk.getRotation().clone(), chunk.getLight().clone(), chunk.getHeightMap().clone());
                    chunk.setSaveToDisk(false);
                }
            }
            saveTimer = 0f;
        }
    }

    //this re-generates chunk meshes with the light level
    public void floodChunksWithNewLight(){
        for (ChunkObject thisChunk : map.values()){
            for (int y = 0; y < 8; y++) {
                Vector2i pos = thisChunk.getPos();
                this.chunkUpdateHandler.chunkUpdate(pos.x, pos.y, y);
            }
        }
    }

    public void globalFinalChunkSaveToDisk(){
        for (ChunkObject chunk : map.values()){
            disk.saveChunk(chunk.getPos(),chunk.getBlock().clone(), chunk.getRotation().clone(), chunk.getLight().clone(), chunk.getHeightMap().clone());
            chunk.setSaveToDisk(false);
        }
        map.clear();
    }

    public boolean chunkStackContainsBlock(int chunkX, int chunkZ, int yHeight){
        byte[] blockData = map.get(new Vector2i(chunkX, chunkZ)).getBlock();
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


    public int getHeightMap(Vector2i pos){
        int chunkX = (int)Math.floor(pos.x/16d);
        int chunkZ = (int)Math.floor(pos.y/16d);
        int blockX = (int)(pos.x - (16d*chunkX));
        int blockZ = (int)(pos.y - (16d*chunkZ));

        byte[] heightMapData = map.get(new Vector2i(chunkX, chunkZ)).getHeightMap();

        if (heightMapData == null){
            return 555;
        }

        return heightMapData[posToIndex2D(blockX,blockZ)];
    }

    public boolean underSunLight(Vector3i pos){
        if (pos.y > 127 || pos.y < 0){
            return false;
        }
        int chunkX = (int)Math.floor(pos.x/16d);
        int chunkZ = (int)Math.floor(pos.z/16d);
        int blockX = (int)(pos.x - (16d*chunkX));
        int blockZ = (int)(pos.z - (16d*chunkZ));
        byte[] heightMapData = map.get(new Vector2i(chunkX, chunkZ)).getHeightMap();
        if (heightMapData == null){
            return false;
        }
        return heightMapData[posToIndex2D(blockX,blockZ)] < pos.y + 1;
    }

    //overloaded block getter
    public byte getBlock(Vector3i pos){
        if (pos.y > 127 || pos.y < 0){
            return -1;
        }
        int chunkX = (int)Math.floor(pos.x/16d);
        int chunkZ = (int)Math.floor(pos.z/16d);
        int blockX = (int)(pos.x - (16d*chunkX));
        int blockZ = (int)(pos.z - (16d*chunkZ));
        // THIS CREATES A NEW OBJECT IN MEMORY!
        ChunkObject chunk = map.get(new Vector2i(chunkX, chunkZ));
        if (chunk == null){
            return -1;
        }
        byte[] blockData = chunk.getBlock();
        if (blockData == null){
            return -1;
        }
        return blockData[posToIndex(blockX, pos.y, blockZ)];
    }

    //overloaded getter for rotation
    public byte getBlockRotation(Vector3i pos){
        if (pos.y > 127 || pos.y < 0){
            return -1;
        }
        int chunkX = (int)Math.floor(pos.x/16d);
        int chunkZ = (int)Math.floor(pos.z/16d);
        int blockX = (int)(pos.x - (16d*chunkX));
        int blockZ = (int)(pos.z - (16d*chunkZ));
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] rotationData = map.get(new Vector2i(chunkX, chunkZ)).getRotation();
        if (rotationData == null){
            return 0;
        }
        return rotationData[posToIndex(blockX, pos.y, blockZ)];
    }

    public void setBlock(Vector3i pos, byte newBlock, byte rot){
        if (pos.y > 127 || pos.y < 0){
            return;
        }
        int yPillar = (int)Math.floor(pos.y/16d);
        int chunkX = (int)Math.floor(pos.x/16d);
        int chunkZ = (int)Math.floor(pos.z/16d);
        int blockX = (int)(pos.x - (16d*chunkX));
        int blockZ = (int)(pos.z - (16d*chunkZ));

        Vector2i key = new Vector2i(chunkX, chunkZ);

        byte[] blockData = map.get(key).getBlock();
        byte[] rotationData = map.get(key).getRotation();
        byte[] heightMapData = map.get(key).getHeightMap();

        if (blockData == null || rotationData == null){
            return;
        }

        blockData[posToIndex(blockX,pos.y, blockZ)] = newBlock;
        rotationData[posToIndex(blockX,pos.y, blockZ)] = rot;

        if (newBlock == 0){
            if (heightMapData[posToIndex2D(blockX,blockZ)] ==  pos.y){
                for (int yCheck = heightMapData[posToIndex2D(blockX,blockZ)]; yCheck > 0; yCheck--){
                    if (blockData[posToIndex(blockX, yCheck, blockZ)] != 0){
                        heightMapData[posToIndex2D(blockX,blockZ)] = (byte) yCheck;
                        break;
                    }
                }
            }
        } else {
            if (heightMapData[posToIndex2D(blockX,blockZ)] < pos.y){
                heightMapData[posToIndex2D(blockX,blockZ)] = (byte) pos.y;
            }
        }
        map.get(key).setSaveToDisk(true);
        this.chunkUpdateHandler.chunkUpdate(chunkX,chunkZ,yPillar);
        updateNeighbor(chunkX, chunkZ,blockX,pos.y,blockZ);
    }

    public void setNaturalLight(Vector3i pos, byte newLight){
        if (pos.y > 127 || pos.y < 0){
            return;
        }
        int yPillar = (int)Math.floor(pos.y/16d);
        int chunkX = (int)Math.floor(pos.x/16d);
        int chunkZ = (int)Math.floor(pos.z/16d);
        int blockX = (int)(pos.x - (16d*chunkX));
        int blockZ = (int)(pos.z - (16d*chunkZ));
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] lightData = map.get(new Vector2i(chunkX, chunkZ)).getLight();
        if (lightData == null){
            return;
        }
        lightData[posToIndex(blockX, pos.y, blockZ)] = setByteNaturalLight(lightData[posToIndex(blockX, pos.y, blockZ)],newLight);
        this.chunkUpdateHandler.chunkUpdate(chunkX,chunkZ,yPillar);
        updateNeighbor(chunkX, chunkZ,blockX,pos.y,blockZ);
    }

    public void setTorchLight(int x,int y,int z, byte newLight){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16d);
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] lightData = map.get(new Vector2i(chunkX, chunkZ)).getLight();
        if (lightData == null){
            return;
        }
        lightData[posToIndex(blockX, y, blockZ)] = setByteTorchLight(lightData[posToIndex(blockX, y, blockZ)], newLight);
        this.chunkUpdateHandler.chunkUpdate(chunkX,chunkZ,yPillar);
        updateNeighbor(chunkX, chunkZ,blockX,y,blockZ);
    }


    public void digBlock(Vector3i pos){
        if (pos.y > 127 || pos.y < 0){
            return;
        }
        int yPillar = (int)Math.floor(pos.y/16d);
        int chunkX = (int)Math.floor(pos.x/16d);
        int chunkZ = (int)Math.floor(pos.z/16d);
        int blockX = (int)(pos.x - (16d*chunkX));
        int blockZ = (int)(pos.z - (16d*chunkZ));
        Vector2i key = new Vector2i(chunkX, chunkZ);

        byte[] blockData = map.get(key).getBlock();
        byte[] rotationData = map.get(key).getRotation();
        byte[] heightMapData = map.get(key).getHeightMap();
        byte[] lightData = map.get(key).getLight();

        if (blockData == null || rotationData == null || heightMapData == null){
            return;
        }

        byte oldBlock = blockData[posToIndex(blockX, pos.y, blockZ)];

        blockData[posToIndex(blockX, pos.y, blockZ)] = 0;
        rotationData[posToIndex(blockX, pos.y, blockZ)] = 0;
        if (heightMapData[posToIndex2D(blockX,blockZ)] == pos.y){
            for (int yCheck = heightMapData[posToIndex2D(blockX,blockZ)]; yCheck > 0; yCheck--){
                if (blockData[posToIndex(blockX, yCheck, blockZ)] != 0){
                    heightMapData[posToIndex2D(blockX,blockZ)] = (byte) yCheck;
                    break;
                }
            }
        }

        light.lightFloodFill(pos.x, pos.y, pos.z);
        light.torchFloodFill(pos.x, pos.y, pos.z);

        map.get(key).setSaveToDisk(true);

        lightData[posToIndex(blockX, pos.y, blockZ)] = setByteNaturalLight(lightData[posToIndex(blockX, pos.y, blockZ)], light.getImmediateLight(pos.x,pos.y,pos.z));

        /*
        if (!getIfMultiplayer()) {
            onDigCall(oldBlock, x, y, z);
        }
         */

        chunkMeshGenerator.instantGeneration(chunkX,chunkZ,yPillar);
        instantUpdateNeighbor(chunkX, chunkZ,blockX,pos.y,blockZ);//instant update
    }

    public void placeBlock(Vector3i pos, byte ID, byte rot){
        if (pos.y > 127 || pos.y < 0){
            return;
        }
        int yPillar = (int)Math.floor(pos.y/16d);
        int chunkX = (int)Math.floor(pos.x/16d);
        int chunkZ = (int)Math.floor(pos.z/16d);
        int blockX = (pos.x - (16*chunkX));
        int blockZ = (pos.z - (16*chunkZ));

        Vector2i key = new Vector2i(chunkX, chunkZ);

        byte[] blockData = map.get(key).getBlock();
        byte[] rotationData = map.get(key).getRotation();
        byte[] heightMapData = map.get(key).getHeightMap();

        if (blockData == null || rotationData == null || heightMapData == null){
            return;
        }

        blockData[posToIndex(blockX, pos.y, blockZ)] = ID;
        rotationData[posToIndex(blockX, pos.y, blockZ)] =  rot;

        System.out.println("ADD A LIGHT PROPAGATES OR TRANSLUCENT THING TO PLACE BLOCK!");

        if (blockDefinitionContainer.getWalkable(ID) && heightMapData[posToIndex2D(blockX,blockZ)] < pos.y){
            heightMapData[posToIndex2D(blockX,blockZ)] = (byte) pos.y;
        }

        light.lightFloodFill(pos.x, pos.y, pos.z);
        light.torchFloodFill(pos.x, pos.y, pos.z);

        map.get(key).setSaveToDisk(true);

        /*
        if (!getIfMultiplayer()) {
            // THIS CREATES A NEW OBJECT IN MEMORY!
            onPlaceCall(ID, x, y, z);
        }
         */

        chunkMeshGenerator.instantGeneration(chunkX,chunkZ,yPillar);
        instantUpdateNeighbor(chunkX, chunkZ,blockX,pos.y,blockZ);//instant update
    }

    public byte getLight(int x,int y,int z){
        if (y > 127 || y < 0){
            return 0;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));

        ChunkObject chunk = map.get(new Vector2i(chunkX, chunkZ));

        if (chunk == null){
            return 0;
        }

        byte[] lightData = chunk.getLight();

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
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));

        byte[] lightData = map.get(new Vector2i(chunkX,chunkZ)).getLight();

        if (lightData == null){
            return 0;
        }

        return getByteNaturalLight(lightData[posToIndex(blockX, y, blockZ)]);
    }

    public byte getTorchLight(int x,int y,int z){
        if (y > 127 || y < 0){
            return 0;
        }
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));
        
        byte[] lightData = map.get(new Vector2i(chunkX,chunkZ)).getLight();

        if (lightData == null){
            return 0;
        }

        return getByteTorchLight(lightData[posToIndex(blockX, y, blockZ)]);
    }

    private void instantUpdateNeighbor(int chunkX, int chunkZ, int x, int y, int z){
        if (y > 127 || y < 0){
            return;
        }
        int yPillar = (int)Math.floor(y/16d);
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
        int yPillar = (int)Math.floor(y/16d);
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

    private void fullNeighborUpdate(int chunkX, int chunkZ){

        if (map.get(new Vector2i(chunkX + 1, chunkZ)) != null){
            for (int y = 0; y < 8; y++){
                this.chunkUpdateHandler.chunkUpdate(chunkX+1, chunkZ, y);
            }
        }

        if (map.get(new Vector2i(chunkX-1, chunkZ)) != null){
            for (int y = 0; y < 8; y++){
                this.chunkUpdateHandler.chunkUpdate(chunkX-1, chunkZ, y);
            }
        }

        if (map.get(new Vector2i(chunkX, chunkZ+1)) != null){
            for (int y = 0; y < 8; y++){
                this.chunkUpdateHandler.chunkUpdate(chunkX, chunkZ+1, y);
            }
        }

        if (map.get(new Vector2i(chunkX, chunkZ-1)) != null){
            for (int y = 0; y < 8; y++){
                this.chunkUpdateHandler.chunkUpdate(chunkX, chunkZ-1, y);
            }
        }
    }

    public void generateNewChunks(){
        //create the initial map in memory
        int chunkRenderDistance = settings.getRenderDistance();
        Vector2i currentChunk = player.getPlayerCurrentChunk();
        //scan for not-generated/loaded chunks
        for (int x = -chunkRenderDistance + currentChunk.x; x < chunkRenderDistance + currentChunk.x; x++){
            for (int z = -chunkRenderDistance + currentChunk.y; z< chunkRenderDistance + currentChunk.y; z++){
                if (getChunkDistanceFromPlayer(x,z) <= chunkRenderDistance){
                    // THIS CREATES A NEW OBJECT IN MEMORY!
                    if (map.get(new Vector2i(x,z)) == null){
                        genBiome(x,z);
                        for (int y = 0; y < 8; y++) {
                            this.chunkUpdateHandler.chunkUpdate(x, z, y);
                        }
                        fullNeighborUpdate(x, z);
                    }
                }
            }
        }

        //scan map for out of range chunks
        for (ChunkObject chunk : map.values()){
            if (getChunkDistanceFromPlayer(chunk.getPos().x,chunk.getPos().y) > chunkRenderDistance){
                addChunkToDeletionQueue(chunk.getPos().x,chunk.getPos().y);
            }
        }
    }

    /*
    public void requestNewChunks(){
        //create the initial map in memory
        int chunkRenderDistance = settings.getRenderDistance();
        Vector3i currentChunk = getPlayerCurrentChunk();
        //scan for not-generated/loaded chunks
        for (int x = -chunkRenderDistance + currentChunk.x; x < chunkRenderDistance + currentChunk.x; x++){
            for (int z = -chunkRenderDistance + currentChunk.z; z< chunkRenderDistance + currentChunk.z; z++){
                if (getChunkDistanceFromPlayer(x,z) <= chunkRenderDistance){
                    // THIS CREATES A NEW OBJECT IN MEMORY!
                    if (map.get(new Vector2i(x,z)) == null){
                        sendOutChunkRequest(new ChunkRequest(x,z, getPlayerName()));
                    }
                }
            }
        }
    }
     */

    private final Deque<Vector2i> deletionQueue = new ArrayDeque<>();

    private void addChunkToDeletionQueue(int chunkX, int chunkZ) {
        deletionQueue.add(new Vector2i(chunkX, chunkZ));
    }

    private float chunkDeletionTimer = 0f;


    public void processOldChunks() {

        chunkDeletionTimer += delta.getDelta();

        int updateAmount = 0;

        //goalTimerArray[getSettingsChunkLoad()];
        float goalTimer = 0.0001f;
        if (chunkDeletionTimer >= goalTimer){
            updateAmount = (int)(Math.ceil(chunkDeletionTimer / goalTimer));
            chunkDeletionTimer = 0;
        }

        for (int i = 0; i < updateAmount; i++) {
            if (!deletionQueue.isEmpty()) {
                Vector2i key = deletionQueue.pop();


                //clean up mesh data
                Mesh[] normalMeshData = map.get(key).getNormalMeshArray();

                for (Mesh meshData : normalMeshData){
                    if (meshData != null) {
                        meshData.cleanUp(false);
                    }
                }

                Mesh[] liquidMeshData = map.get(key).getLiquidMeshArray();

                for (Mesh meshData : liquidMeshData){
                    if (meshData != null) {
                        meshData.cleanUp(false);
                    }
                }

                Mesh[] allFacesMeshData = map.get(key).getAllFaceMeshArray();

                for (Mesh meshData : allFacesMeshData){
                    if (meshData != null) {
                        meshData.cleanUp(false);
                    }
                }

                disk.saveChunk(map.get(key).getPos(),map.get(key).getBlock().clone(), map.get(key).getRotation().clone(), map.get(key).getLight().clone(), map.get(key).getHeightMap().clone());

                map.remove(key);
            }
        }
    }

    //returns -1 if fails
    public int getMobSpawnYPos(int x, int z){
        int chunkX = (int)Math.floor(x/16d);
        int chunkZ = (int)Math.floor(z/16d);
        int blockX = (int)(x - (16d*chunkX));
        int blockZ = (int)(z - (16d*chunkZ));

        // THIS CREATES A NEW OBJECT IN MEMORY!
        byte[] blockData = map.get(new Vector2i(chunkX, chunkZ)).getBlock();

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

    //todo: this needs a new name
    //this dispatches to the SQLite thread, checks if it exists in the data base
    //then it either deserializes it or it tells the chunk mesh generator thread
    //to create a new one if it doesn't exist
    public void genBiome(int chunkX, int chunkZ) {
        disk.loadChunk(new Vector2i(chunkX,chunkZ));
    }

    public void cleanChunkDataMemory(){
        for (ChunkObject chunk : map.values()) {
            for (Mesh mesh : chunk.getNormalMeshArray()) {
                if (mesh != null) {
                    mesh.cleanUp(false);
                }
            }
            for (Mesh mesh : chunk.getLiquidMeshArray()) {
                if (mesh != null) {
                    mesh.cleanUp(false);
                }
            }
            for (Mesh mesh : chunk.getAllFaceMeshArray()) {
                if (mesh != null) {
                    mesh.cleanUp(false);
                }
            }
        }
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

    private double getDistance(double x1, double y1, double z1, double x2, double y2, double z2){
        return Math.hypot((x1 - x2), Math.hypot((y1 - y2),(z1 - z2)));
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
