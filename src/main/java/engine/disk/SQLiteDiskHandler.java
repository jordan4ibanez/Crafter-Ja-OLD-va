package engine.disk;

import game.chunk.BiomeGenerator;
import game.chunk.Chunk;
import game.crafting.Inventory;
import game.crafting.InventoryLogic;
import game.player.Player;
import org.joml.Vector2i;

import java.util.concurrent.ConcurrentLinkedDeque;

public class SQLiteDiskHandler {
    private final SQLiteDiskAccessThread sqLiteDiskAccessThread;
    private BiomeGenerator biomeGenerator;
    private Chunk chunk;
    private Player player;
    private InventoryLogic inventoryLogic;

    private boolean hasPolledLoadingPlayer = false;
    private final ConcurrentLinkedDeque<PlayerDataObject> playerData = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<PrimitiveChunkObject> loadingChunkData = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<Vector2i> generatingChunks = new ConcurrentLinkedDeque<>();

    public SQLiteDiskHandler(){
        this.sqLiteDiskAccessThread = new SQLiteDiskAccessThread(this);
    }

    public void setBiomeGenerator(BiomeGenerator biomeGenerator){
        if (this.biomeGenerator == null){
            this.biomeGenerator = biomeGenerator;
        }
    }
    public void setChunk(Chunk chunk){
        if (this.chunk == null){
            this.chunk = chunk;
        }
    }
    public void setPlayer(Player player){
        if (this.player == null){
            this.player = player;
        }
    }
    public void setInventoryLogic(InventoryLogic inventoryLogic){
        if (this.inventoryLogic == null){
            this.inventoryLogic = inventoryLogic;
        }
    }

    //this mirrors the object's call
    public void connectWorldDataBase(String worldName){
        //this is needed to create the WORLD table
        this.sqLiteDiskAccessThread.createWorldDataBase(worldName);
        this.sqLiteDiskAccessThread.addPlayerToLoad("singleplayer");
        this.sqLiteDiskAccessThread.start();
    }

    //closes the world's database, kills the thread, removes the object pointer
    public void closeWorldDataBase(){
        //dump it all in to SQLite thread boi
        this.chunk.globalFinalChunkSaveToDisk();
        this.savePlayerData("singleplayer");
        this.sqLiteDiskAccessThread.stop();
        this.hasPolledLoadingPlayer = false;
    }

    public void sendPlayerData(PlayerDataObject player){
        playerData.add(player);
    }

    public void poll(){

        //chunk data
        if (!this.loadingChunkData.isEmpty()){
            PrimitiveChunkObject chunkObject = loadingChunkData.pop();
            this.chunk.addNewChunk(chunkObject);
        }

        if (!this.generatingChunks.isEmpty()){
            Vector2i pos = generatingChunks.pop();
            this.biomeGenerator.addChunkToBiomeGeneration(pos);
        }


        //player data
        if (this.hasPolledLoadingPlayer){
            return;
        }
        if (this.playerData.isEmpty()) {
            return;
        }

        PlayerDataObject player = playerData.pop();

        //set inventory
        for (int y = 0; y < player.inventory.length; y++){
            for (int x = 0; x < player.inventory[0].length; x++){
                inventoryLogic.getInventory().getMain().setItem(x, y, player.inventory[y][x], player.count[y][x]);
                //setInventoryItem("main", x, y, "dirt", 10);
            }
        }

        this.player.setPos(player.pos);
        this.player.setPlayerHealth(player.health);

        this.hasPolledLoadingPlayer = true;

    }

    public void savePlayerData(String name){
        this.sqLiteDiskAccessThread.addPlayerToSave(name, inventoryLogic.getInventory().getMain().getInventoryAsArray(), inventoryLogic.getInventory().getMain().getCountAsArray(), player.getPlayerPos(), (byte) player.getPlayerHealth());
    }

    public void loadChunk(Vector2i key){
        this.sqLiteDiskAccessThread.addLoadChunk(key);
    }

    public void saveChunk(Vector2i pos, byte[] blockData, byte[] rotationData, byte[] lightData, byte[] heightMap){
        this.sqLiteDiskAccessThread.addSaveChunk(pos, blockData,rotationData,lightData,heightMap);
    }

    public void setChunk(PrimitiveChunkObject primitiveChunkObject){
        this.loadingChunkData.add(primitiveChunkObject);
    }

    public void addChunkToBiomeGenerator(Vector2i pos){
        this.generatingChunks.add(new Vector2i(pos));
    }


}
