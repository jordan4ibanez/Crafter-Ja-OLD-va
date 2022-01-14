package engine.disk;

import org.joml.Vector3d;

import java.util.concurrent.ConcurrentLinkedDeque;

import static game.chunk.Chunk.globalFinalChunkSaveToDisk;
import static game.crafting.InventoryObject.*;
import static game.player.Player.*;
import static game.player.Player.setPlayerHealth;

//this handles the thread object and tells it what to do
public class SQLiteDiskHandler {

    private static boolean hasPolledLoadingPlayer = false;

    //null, asleep, non-existent, etc
    private static SQLiteDiskAccessThread sqLiteDiskAccessThread;

    //these shall stay in sync
    //received from the SQLiteDataAccess thread
    //cannot access anything from it until it has run through the next iteration
    private static final ConcurrentLinkedDeque<String> playerData           = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<String[][]> playerInventory    = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<int[][]> playerInventoryCount  = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<Vector3d> playerPos            = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<Byte> playerHealth             = new ConcurrentLinkedDeque<>();

    //this mirrors the object's call
    public static void connectWorldDataBase(String worldName){
        sqLiteDiskAccessThread = new SQLiteDiskAccessThread();
        //this is needed to create the WORLD table
        sqLiteDiskAccessThread.createWorldDataBase(worldName);
        sqLiteDiskAccessThread.addPlayerToLoad("singleplayer");
        sqLiteDiskAccessThread.start();
    }

    //closes the world's database, kills the thread, removes the object pointer
    public static void closeWorldDataBase(){
        //DUMP EVERYTHING IN!
        globalFinalChunkSaveToDisk();
        savePlayerData("singleplayer");
        sqLiteDiskAccessThread.stop();
        hasPolledLoadingPlayer = false;
    }

    public static void receiveDataFromSQLiteDiskAccessThread(String playerName, String[][] thisPlayerInventory, int[][] thisPlayerCount, Vector3d thisPlayerPos, byte thisPlayerHealth){
        playerData.add(playerName);

        //deep clone - remove pointer
        String[][] clonedInventory = new String[thisPlayerInventory.length][thisPlayerInventory[0].length];
        for (int i = 0; i < thisPlayerInventory.length; i++) {
            System.arraycopy(thisPlayerInventory[i], 0, clonedInventory[i], 0, thisPlayerInventory[0].length);
        }
        playerInventory.add(clonedInventory);

        //deep clone - remove pointer
        int[][] clonedCount = new int[thisPlayerCount.length][thisPlayerCount[0].length];
        for (int i = 0; i < thisPlayerCount.length; i++) {
            System.arraycopy(thisPlayerCount[i], 0, clonedCount[i], 0, thisPlayerCount[0].length);
        }
        playerInventoryCount.add(clonedCount);

        //remove pointer
        Vector3d newPlayerPos = new Vector3d(thisPlayerPos.x, thisPlayerPos.y, thisPlayerPos.z);

        playerPos.add(newPlayerPos);
        playerHealth.add(thisPlayerHealth);
    }

    public static void pollReceivingPlayerDataFromSQLiteThread(){
        if (hasPolledLoadingPlayer){
            return;
        }
        if (!playerData.isEmpty() && !playerInventory.isEmpty() && !playerInventoryCount.isEmpty() && !playerPos.isEmpty() && !playerHealth.isEmpty()){

            String name = playerData.pop();

            String[][] newPlayerInventory = playerInventory.pop();
            int[][] newPlayerInventoryCount = playerInventoryCount.pop();
            Vector3d newPlayerPos = playerPos.pop();
            byte newPlayerHealth = playerHealth.pop();

            //set inventory
            for (int y = 0; y < newPlayerInventory.length; y++){
                for (int x = 0; x < newPlayerInventory[0].length; x++){
                    setInventoryItem("main", x, y, newPlayerInventory[y][x], newPlayerInventoryCount[y][x]);
                    //setInventoryItem("main", x, y, "dirt", 10);
                }
            }

            newPlayerPos.y = 128;
            setPlayerPos(newPlayerPos);
            setPlayerHealth(newPlayerHealth);

            hasPolledLoadingPlayer = true;
        }
    }

    public static void savePlayerData(String name){
        sqLiteDiskAccessThread.addPlayerToSave(name, getInventoryAsArray("main").clone(), getInventoryCountAsArray("main").clone(), getPlayerPos(), (byte) getPlayerHealth());
    }

    public static void loadChunk(int x, int z){
        sqLiteDiskAccessThread.addLoadChunk(x,z);
    }

    public static void saveChunk(int x, int z, byte[] blockData, byte[] rotationData, byte[] lightData, byte[] heightMap){
        sqLiteDiskAccessThread.addSaveChunk(x,z,blockData,rotationData,lightData,heightMap);
    }


}
