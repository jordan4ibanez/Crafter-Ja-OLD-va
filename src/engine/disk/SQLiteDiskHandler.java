package engine.disk;

import static engine.disk.Disk.savePlayerPos;
import static game.chunk.Chunk.globalFinalChunkSaveToDisk;
import static game.player.Player.getPlayerPos;

//this handles the thread object and tells it what to do
public class SQLiteDiskHandler {

    //null, asleep, non-existent, etc
    private static SQLiteDiskAccessThread sqLiteDiskAccessThread;

    //this mirrors the object's call
    public static void connectWorldDataBase(String worldName){
        sqLiteDiskAccessThread = new SQLiteDiskAccessThread();
        //this is needed to create the WORLD table
        sqLiteDiskAccessThread.createWorldDataBase(worldName);
        sqLiteDiskAccessThread.start();
    }

    //closes the world's database, kills the thread, removes the object pointer
    public static void closeWorldDataBase(){
        //DUMP EVERYTHING IN!
        globalFinalChunkSaveToDisk();
        savePlayerPos(getPlayerPos());
        sqLiteDiskAccessThread.stop();

        //nullify, this thread will stop itself
        //this avoids an accidental pointer mix up
        //sqLiteDiskAccessThread = null;
    }


    public static void loadChunk(int x, int z){
        sqLiteDiskAccessThread.addLoadChunk(x,z);
    }

    public static void saveChunk(int x, int z, byte[] blockData, byte[] rotationData, byte[] lightData, byte[] heightMap){
        //double startTime = System.nanoTime();
        sqLiteDiskAccessThread.addSaveChunk(x,z,blockData,rotationData,lightData,heightMap);
        //double endTime = System.nanoTime();

        //double duration = (endTime - startTime) / 1000000d;

        //System.out.println(duration);
    }


}
