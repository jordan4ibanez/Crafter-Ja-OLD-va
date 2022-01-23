package engine.disk;

import game.chunk.BiomeGenerator;
import game.chunk.Chunk;
import game.crafting.Inventory;
import game.player.Player;
import org.joml.Vector2i;

import java.util.concurrent.ConcurrentLinkedDeque;

public class SQLiteDiskHandler {
    private final SQLiteDiskAccessThread sqLiteDiskAccessThread;
    private final BiomeGenerator biomeGenerator;
    private final Chunk chunk;
    private final Player player;
    private final Inventory inventory;

    private boolean hasPolledLoadingPlayer = false;
    private final ConcurrentLinkedDeque<PlayerDataObject> playerData = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<PrimitiveChunkObject> loadingChunkData = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<Vector2i> generatingChunks = new ConcurrentLinkedDeque<>();

    public SQLiteDiskHandler(Chunk chunk, BiomeGenerator biomeGenerator, Player player, Inventory inventory){
        this.chunk = chunk;
        //threads directly share pointers with each other
        this.sqLiteDiskAccessThread = new SQLiteDiskAccessThread(this);
        this.biomeGenerator = biomeGenerator;
        this.player = player;
        this.inventory = inventory;
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
                inventory.getMain().setItem(x, y, player.inventory[y][x], player.count[y][x]);
                //setInventoryItem("main", x, y, "dirt", 10);
            }
        }

        this.player.setPos(player.pos);
        this.player.setPlayerHealth(player.health);

        this.hasPolledLoadingPlayer = true;

    }

    public void savePlayerData(String name){
        this.sqLiteDiskAccessThread.addPlayerToSave(name, inventory.getMain().getInventoryAsArray(), inventory.getMain().getCountAsArray(), player.getPlayerPos(), (byte) player.getPlayerHealth());
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
