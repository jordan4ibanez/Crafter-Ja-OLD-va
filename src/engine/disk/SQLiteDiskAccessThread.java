package engine.disk;

import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.sql.*;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static game.chunk.BiomeGenerator.addChunkToBiomeGeneration;
import static game.chunk.Chunk.*;
import static game.chunk.ChunkUpdateHandler.chunkUpdate;

public class SQLiteDiskAccessThread implements Runnable {

    //class fields
    private String url;
    private Connection connection;
    private DatabaseMetaData meta;
    private static final ConcurrentLinkedDeque<Vector2i> chunksToLoad = new ConcurrentLinkedDeque<>();

    private final AtomicBoolean running = new AtomicBoolean(false);

    public void createWorldDataBase(String worldName){
        //databases are automatically created with the JBDC driver

        //database parameters
        url = "jdbc:sqlite:" + System.getProperty("user.dir") +  "/Worlds/" + worldName + "/map.db";

        try {
            //database connection, static private
            connection = DriverManager.getConnection(url);

            if (connection != null){

                System.out.println("SQLITE IS CONNECTED TO WORLD: " + worldName);
                //metadata testing
                meta = connection.getMetaData();

                boolean test = connection.getAutoCommit();

                Statement statement = connection.createStatement();
                //increase SQLite cache size - 256MB
                String sql = "PRAGMA cache_size = -256000;";
                statement.executeUpdate(sql);
                //the following is for game usage, this is not a file server
                //OFF - turn off synchronization technically faster - NORMAL - can be used on servers and regular games
                //this will corrupt chunks if the computer crashes or loses power
                sql = "PRAGMA synchronous = NORMAL;";
                statement.executeUpdate(sql);
                //turn off journaling - WAL IS VERY UNSAFE - WAL will also make the disconnection hang
                //this is a database rollback journal that is being turned off
                //DELETE | TRUNCATE | PERSIST | MEMORY | WAL | OFF are the options
                sql = "PRAGMA journal_mode = OFF;";
                statement.executeUpdate(sql);
                //use the RAM for temp storage
                sql = "PRAGMA temp_store = 2";
                statement.executeUpdate(sql);
                //exclusive locking mode (single user)
                //this does not release the .LOCK
                sql = "PRAGMA locking_mode = EXCLUSIVE;";
                statement.executeUpdate(sql);
                statement.close();

            }
        } catch (SQLException e){
            //something has to go very wrong for this to happen
            System.out.println(e.getMessage());
        }


        try {

            assert connection != null;

            //make the world table
            Statement statement = connection.createStatement();

            ResultSet resultSet = meta.getTables(null,"PUBLIC",null, new String[] {"TABLE"});

            boolean found = false;

            //check if there is a world table
            while (resultSet.next()) {

                String name = resultSet.getString("TABLE_NAME");

                //found the world table
                if (name.equals("WORLD")){
                    found = true;
                    break;
                }
            }

            //create the world table if this is a new database
            if (!found){

                System.out.println("CREATING WORLD TABLE!");

                String sql = "CREATE TABLE WORLD " +
                        "(ID TEXT PRIMARY KEY  NOT NULL," +
                        "BLOCK           TEXT  NOT NULL," +
                        "ROTATION        TEXT  NOT NULL," +
                        "LIGHT           TEXT  NOT NULL," +
                        "HEIGHTMAP       TEXT  NOT NULL)";
                statement.executeUpdate(sql);
                statement.close();
            }

            //close resources
            resultSet.close();
            statement.close();


            //make the player inventory table
            Statement statement2 = connection.createStatement();

            ResultSet resultSet2 = meta.getTables(null,"PUBLIC",null, new String[] {"TABLE"});

            boolean found2 = false;

            //check if there is a world table
            while (resultSet2.next()) {

                String name = resultSet2.getString("TABLE_NAME");

                //found the world table
                if (name.equals("PLAYER_DATA")){
                    found2 = true;
                    break;
                }
            }

            //create the world table if this is a new database
            if (!found2){

                System.out.println("CREATING WORLD TABLE!");

                String sql = "CREATE TABLE PLAYER_DATA " +
                        "(ID TEXT PRIMARY KEY  NOT NULL," +
                        "INVENTORY       TEXT  NOT NULL," +
                        "AMOUNT          TEXT  NOT NULL," +
                        "POS             TEXT  NOT NULL," +
                        "HEALTH          TEXT  NOT NULL)";
                statement2.executeUpdate(sql);
                statement2.close();
            }

            //close resources
            resultSet2.close();
            statement2.close();


        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    //begin player data

    //these shall stay in sync
    //they are bulk added in THIS thread
    //cannot access anything from it until it has run through the next iteration
    private static final ConcurrentLinkedDeque<String> playerToSave         = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<String[][]> playerInventory    = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<int[][]> playerInventoryCount = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<Vector3d> playerPos          = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<Byte> playerHealth           = new ConcurrentLinkedDeque<>();

    public void addPlayerToSave(String playerName, String[][] inventoryToSave, int[][] inventoryCount, Vector3d newPlayerPos, byte newPlayerHealth){
        playerToSave.add(playerName);

        //deep clone - remove pointer
        String[][] clonedInventory = new String[inventoryToSave.length][inventoryToSave[0].length];
        for (int i = 0; i < inventoryToSave.length; i++) {
            System.arraycopy(inventoryToSave[i], 0, clonedInventory[i], 0, inventoryToSave[0].length);
        }
        playerInventory.add(clonedInventory);

        //deep clone - remove pointer
        int[][] clonedCount = new int[inventoryCount.length][inventoryCount[0].length];
        for (int i = 0; i < inventoryCount.length; i++) {
            System.arraycopy(inventoryCount[i], 0, clonedCount[i], 0, inventoryCount[0].length);
        }
        playerInventoryCount.add(clonedCount);

        playerPos.add(new Vector3d(newPlayerPos.x, newPlayerPos.y, newPlayerPos.z));
        playerHealth.add(newPlayerHealth);
    }

    private void tryToSavePlayer(){
        if (!playerToSave.isEmpty() && !playerInventory.isEmpty() && !playerInventoryCount.isEmpty() && !playerPos.isEmpty() && !playerHealth.isEmpty()) {
            try {
                String poppedPlayer = playerToSave.pop();

                Statement statement = connection.createStatement();

                String sql = "INSERT OR REPLACE INTO PLAYER_DATA " +
                        "(ID,INVENTORY,AMOUNT,POS,HEALTH) " +
                        "VALUES ('" +
                        poppedPlayer + "','" + //ID
                        byteSerialize(chunksToSaveBlock.pop()) + "','" +//INVENTORY STRING ARRAY
                        byteSerialize(chunksToSaveRotation.pop()) + "','" +//INVENTORY AMOUNT BYTE ARRAY
                        byteSerialize(chunksToSaveLight.pop()) + "','" +//POS VECTOR DATA
                        byteSerialize(chunksToSaveHeightMap.pop()) + //PLAYER HEALTH DATA
                        "');";
                statement.executeUpdate(sql);
                statement.close();


            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    //begin world data

    //these shall stay in sync
    //they are bulk added in THIS thread
    //cannot access anything from it until it has run through the next iteration
    /*
    private static final ConcurrentLinkedDeque<Vector2i> chunksToSaveKey     = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<byte[]> chunksToSaveBlock     = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<byte[]> chunksToSaveRotation  = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<byte[]> chunksToSaveLight     = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<byte[]> chunksToSaveHeightMap = new ConcurrentLinkedDeque<>();
     */

    public void addSaveChunk(int x, int z, byte[] blockData, byte[] rotationData, byte[] lightData, byte[] heightMap ){
        chunksToSaveKey.add(new Vector2i(x,z));
        chunksToSaveBlock.add(blockData);
        chunksToSaveRotation.add(rotationData);
        chunksToSaveLight.add(lightData);
        chunksToSaveHeightMap.add(heightMap);
    }

    private void tryToSaveChunk(){

        if (!chunksToSaveKey.isEmpty() && !chunksToSaveBlock.isEmpty() && !chunksToSaveRotation.isEmpty() && !chunksToSaveLight.isEmpty() && !chunksToSaveHeightMap.isEmpty()) {
            try {
                Vector2i poppedVector = chunksToSaveKey.pop();

                int x = poppedVector.x;
                int z = poppedVector.y;

                Statement statement = connection.createStatement();

                String sql = "INSERT OR REPLACE INTO WORLD " +
                        "(ID,BLOCK,ROTATION,LIGHT,HEIGHTMAP) " +
                        "VALUES ('" +
                        x + "-" + z + "','" + //ID
                        byteSerialize(chunksToSaveBlock.pop()) + "','" +//BLOCK ARRAY
                        byteSerialize(chunksToSaveRotation.pop()) + "','" +//ROTATION DATA
                        byteSerialize(chunksToSaveLight.pop()) + "','" +//LIGHT DATA
                        byteSerialize(chunksToSaveHeightMap.pop()) + //HEIGHT DATA
                        "');";
                statement.executeUpdate(sql);
                statement.close();


            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    //do this so that the main thread does not hang
    public void addLoadChunk(int x, int z){
        Vector2i key = new Vector2i(x,z);
        //do not allow loading chunks more than once!
        if (!chunksToLoad.contains(key)){
            chunksToLoad.add(key);
        }
    }

    public void tryToLoadChunk(){
        if (!chunksToLoad.isEmpty()) {
            try {

                Vector2i poppedVector = chunksToLoad.pop();

                int x = poppedVector.x;
                int z = poppedVector.y;


                Statement statement = connection.createStatement();
                ResultSet resultTest = statement.executeQuery("SELECT * FROM WORLD WHERE ID ='" + x + "-" + z + "';");

                //found a chunk
                if (resultTest.next()) {

                    //System.out.println("LOADING CHUNK FROM DATABASE!");

                    //automatically set the chunk in memory
                    setChunk(x, z,
                            byteDeserialize(resultTest.getString("BLOCK")),
                            byteDeserialize(resultTest.getString("ROTATION")),
                            byteDeserialize(resultTest.getString("LIGHT")),
                            byteDeserialize(resultTest.getString("HEIGHTMAP"))
                    );

                    //dump everything into the chunk updater
                    for (int i = 0; i < 8; i++) {
                        chunkUpdate(x, z, i);
                    }

                }

                //did not find a chunk - create a new one
                else {
                    //System.out.println("generate chunk here");
                    addChunkToBiomeGeneration(x, z);
                }

                statement.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    //connection closer
    private void closeWorldDataBase(){
        try {
            connection.close();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }


    public void start() {
        System.out.println("started SQL thread!");
        //thread fields
        Thread worker = new Thread(this);
        worker.start();
    }

    public void stop(){
        running.set(false);
    }


    @Override
    public void run() {
        running.set(true);

        //do not shut down until all chunks are saved!
        while(running.get() || !chunksToSaveKey.isEmpty()) {
            //System.out.println("NUMBER 5 IS ALIVE");
            if (running.get()) {
                tryToLoadChunk();
            }
            tryToSaveChunk();
        }

        System.out.println("CLOSING WORLD DATABASE!");
        closeWorldDataBase();
    }

    //deserializers
    private static byte[] byteDeserialize(String serializedArray){

        //turn string into array for easier access
        char[] charArray = serializedArray.toCharArray();

        //start at one to auto-add in the last item
        int numberOfThings = 1;

        //iterate number of elements, this is why the stringed array contains only commas and numbers
        for (char c : charArray){
            if (c == ','){
                numberOfThings++;
            }
        }

        //create new blank array
        byte[] outPut = new byte[numberOfThings];

        //create a new string builder
        StringBuilder decode = new StringBuilder();

        //start index at 0
        int index = 0;

        //auto-flush indexes
        for (int i = 0; i <= charArray.length; i++){
            //flush the number to the array
            if (i == (charArray.length) || charArray[i] == ','){
                outPut[index] = Byte.parseByte(decode.toString());
                decode.setLength(0);
                //tick up index
                index++;
            } else {
                decode.append(charArray[i]);
            }
        }

        return outPut;
    }

    private static int[] intDeserialize(String serializedArray){

        //turn string into array for easier access
        char[] charArray = serializedArray.toCharArray();

        //start at one to auto-add in the last item
        int numberOfThings = 1;

        //iterate number of elements, this is why the stringed array contains only commas and numbers
        for (char c : charArray){
            if (c == ','){
                numberOfThings++;
            }
        }

        //create new blank array
        int[] outPut = new int[numberOfThings];

        //create a new string builder
        StringBuilder decode = new StringBuilder();

        //start index at 0
        int index = 0;

        //auto-flush indexes
        for (int i = 0; i <= charArray.length; i++){
            //flush the number to the array
            if (i == (charArray.length) || charArray[i] == ','){
                outPut[index] = Byte.parseByte(decode.toString());
                decode.setLength(0);
                //tick up index
                index++;
            } else {
                decode.append(charArray[i]);
            }
        }

        return outPut;
    }


    //serializer
    private static String stringArrayArraySerialize(String[][] inputStringArray){

        StringBuilder output = new StringBuilder();
        int outerCount = 0;
        int outerGoal = inputStringArray.length - 1;
        for (String[] baseArray : inputStringArray){
            int innerCount = 0;
            int innerGoal = baseArray.length - 1;
            for (String stringInArray : baseArray){
                output.append(stringInArray);
                if (innerCount != innerGoal){
                    output.append(",");
                }
                innerCount++;
            }

            if (outerCount != outerGoal){
                //add on the array "?" to indicate new array
                output.append("?");
            }
            outerCount++;
        }

        return output.toString();
    }


    private static String byteSerialize(byte[] bytes){

        //build a raw custom string type to hold data, data elements only separated by commas
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < bytes.length; i++){
            str.append(bytes[i]);
            if (i != bytes.length - 1){
                str.append(",");
            }
        }

        return str.toString();
    }

    private static String intSerialize(int[] ints){

        //build a raw custom string type to hold data, data elements only separated by commas
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < ints.length; i++){
            str.append(ints[i]);
            if (i != ints.length - 1){
                str.append(",");
            }
        }

        return str.toString();
    }
}
