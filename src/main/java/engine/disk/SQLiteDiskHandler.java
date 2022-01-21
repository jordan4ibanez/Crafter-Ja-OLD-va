package engine.disk;

import game.chunk.Chunk;

import java.util.concurrent.ConcurrentLinkedDeque;

public class SQLiteDiskHandler {

    private final SQLiteDiskAccessThread sqLiteDiskAccessThread;
    private final Chunk chunk;

    public SQLiteDiskHandler(Chunk chunk){
        this.chunk = chunk;
        //threads directly share pointers with each other
        this.sqLiteDiskAccessThread = new SQLiteDiskAccessThread(this);
    }

    private boolean hasPolledLoadingPlayer = false;

    private final ConcurrentLinkedDeque<PlayerDataObject> playerData = new ConcurrentLinkedDeque<>();

    //this mirrors the object's call
    public void connectWorldDataBase(String worldName){
        //this is needed to create the WORLD table
        sqLiteDiskAccessThread.createWorldDataBase(worldName);
        sqLiteDiskAccessThread.addPlayerToLoad("singleplayer");
        sqLiteDiskAccessThread.start();
    }

    //closes the world's database, kills the thread, removes the object pointer
    public void closeWorldDataBase(){
        //dump it all in to SQLite thread boi
        chunk.globalFinalChunkSaveToDisk();
        savePlayerData("singleplayer");
        sqLiteDiskAccessThread.stop();
        hasPolledLoadingPlayer = false;
    }

    public void sendPlayerData(PlayerDataObject player){
        playerData.add(player);
    }

    public void pollSQLiteThread(){
        if (hasPolledLoadingPlayer){
            return;
        }
        if (!playerData.isEmpty()){

            PlayerDataObject player = playerData.pop();

            //set inventory
            for (int y = 0; y < player.inventory.length; y++){
                for (int x = 0; x < player.inventory[0].length; x++){
                    setInventoryItem("main", x, y, newPlayerInventory[y][x], newPlayerInventoryCount[y][x]);
                    //setInventoryItem("main", x, y, "dirt", 10);
                }
            }
            setPlayerPos(newPlayerPos);
            setPlayerHealth(newPlayerHealth);

            hasPolledLoadingPlayer = true;
        }
    }

    public void savePlayerData(String name){
        sqLiteDiskAccessThread.addPlayerToSave(name, getInventoryAsArray("main"), getInventoryCountAsArray("main"), getPlayerPos(), (byte) getPlayerHealth());
    }

    public void loadChunk(int x, int z){
        sqLiteDiskAccessThread.addLoadChunk(x,z);
    }

    public void saveChunk(int x, int z, byte[] blockData, byte[] rotationData, byte[] lightData, byte[] heightMap){
        sqLiteDiskAccessThread.addSaveChunk(x,z,blockData,rotationData,lightData,heightMap);
    }


}
