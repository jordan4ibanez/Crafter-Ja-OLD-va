package engine.disk;

import org.joml.Vector2i;
import org.joml.Vector3d;

import java.sql.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class SQLiteDiskAccessThread implements Runnable {
    private final SQLiteDiskHandler sqLiteDiskHandler;

    //external pointer to other thread's object held internal
    public SQLiteDiskAccessThread(SQLiteDiskHandler sqLiteDiskHandler){
        this.sqLiteDiskHandler = sqLiteDiskHandler;
    }

    private Connection connection;
    private DatabaseMetaData meta;

    private final ConcurrentLinkedDeque<Vector2i> chunksToLoad = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<String> playersToLoad  = new ConcurrentLinkedDeque<>();

    private final AtomicBoolean running = new AtomicBoolean(false);

    public void createWorldDataBase(String worldName){
        //databases are automatically created with the JBDC driver

        //database parameters
        //class fields
        String url = "jdbc:sqlite:" + System.getProperty("user.dir") + "/Worlds/" + worldName + "/map.db";

        try {
            //database connection, private
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
    private final ConcurrentLinkedDeque<String> playerToSave         = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<String[][]> playerInventory    = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<int[][]> playerInventoryCount = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<Vector3d> playerPos          = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<Byte> playerHealth           = new ConcurrentLinkedDeque<>();

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
        System.out.println(newPlayerHealth);
        playerHealth.add(newPlayerHealth);
    }

    private boolean tryToSavePlayer(){
        if (playerToSave.isEmpty() || playerInventory.isEmpty() || playerInventoryCount.isEmpty() || playerPos.isEmpty() || playerHealth.isEmpty()) {
            return true;
        }
        try {
            String poppedPlayer = playerToSave.pop();

            Statement statement = connection.createStatement();

            String sql = "INSERT OR REPLACE INTO PLAYER_DATA " +
                    "(ID,INVENTORY,AMOUNT,POS,HEALTH) " +
                    "VALUES ('" +
                    poppedPlayer + "','" + //ID
                    stringArrayArraySerialize(playerInventory.pop()) + "','" +//INVENTORY STRING ARRAY
                    intArrayArraySerialize(playerInventoryCount.pop()) + "','" +//INVENTORY AMOUNT BYTE ARRAY
                    serializeVector3d(playerPos.pop()) + "','" +//POS VECTOR DATA
                    playerHealth.pop() + //PLAYER HEALTH DATA
                    "');";
            statement.executeUpdate(sql);
            statement.close();


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    //do this so that the main thread does not hang
    public void addPlayerToLoad(String name){
        //do not allow loading players more than once!
        if (!playersToLoad.contains(name)){
            playersToLoad.add(name);
        }
    }


    private boolean tryToLoadPlayer(){
        if (playersToLoad.isEmpty()) {
            return true;
        }
        try {

            String poppedPlayer = playersToLoad.pop();

            Statement statement = connection.createStatement();
            ResultSet resultTest = statement.executeQuery("SELECT * FROM PLAYER_DATA WHERE ID ='" + poppedPlayer + "';");

            //found a player - send main thread their data
            if (resultTest.next()) {
                //automatically set the player's data
                String[][] loadedInventory = stringArrayArrayDeserialize((resultTest.getString("INVENTORY")));
                int[][] loadedCount = intArrayArrayDeserialize(resultTest.getString("AMOUNT"));
                Vector3d playerPos = deserializeVector3d(resultTest.getString("POS"));
                byte playerHealth = Byte.parseByte(resultTest.getString("HEALTH"));

                sqLiteDiskHandler.sendPlayerData(new PlayerDataObject("singleplayer", loadedInventory, loadedCount, playerPos, playerHealth));

            //send main thread a blank player
            } else {
                //players just kind of drop from the sky ¯\_(ツ)_/¯
                sqLiteDiskHandler.sendPlayerData(new PlayerDataObject("singleplayer", new String[4][9], new int[4][9], new Vector3d(0,100,0), (byte) 20));
            }

            //did not find player

            statement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    //begin world data

    //these shall stay in sync
    //they are bulk added in THIS thread
    //cannot access anything from it until it has run through the next iteration
    private final ConcurrentLinkedDeque<Vector2i> chunksToSaveKey     = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<byte[]> chunksToSaveBlock     = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<byte[]> chunksToSaveRotation  = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<byte[]> chunksToSaveLight     = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<byte[]> chunksToSaveHeightMap = new ConcurrentLinkedDeque<>();

    public void addSaveChunk(int x, int z, byte[] blockData, byte[] rotationData, byte[] lightData, byte[] heightMap ){
        chunksToSaveKey.add(new Vector2i(x,z));
        chunksToSaveBlock.add(blockData);
        chunksToSaveRotation.add(rotationData);
        chunksToSaveLight.add(lightData);
        chunksToSaveHeightMap.add(heightMap);
    }

    private boolean tryToSaveChunk(){

        if (chunksToSaveKey.isEmpty() || chunksToSaveBlock.isEmpty() || chunksToSaveRotation.isEmpty() || chunksToSaveLight.isEmpty() || chunksToSaveHeightMap.isEmpty()) {
            return true;
        }

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

        return false;
    }

    //do this so that the main thread does not hang
    public void addLoadChunk(int x, int z){
        Vector2i key = new Vector2i(x,z);
        //do not allow loading chunks more than once!
        if (!chunksToLoad.contains(key)){
            chunksToLoad.add(key);
        }
    }

    private boolean tryToLoadChunk(){
        if (chunksToLoad.isEmpty()) {
            return true;
        }

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

        return false;
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

    private boolean sleepLock(boolean current, boolean input){
        if (!current){
            return false;
        }

        return input;
    }


    @Override
    public void run() {
        running.set(true);

        //do not shut down until all chunks are saved!
        while(running.get() || !chunksToSaveKey.isEmpty() || !playerToSave.isEmpty()) {
            boolean needsToSleep = true;

            if (running.get()) {
                needsToSleep = sleepLock(needsToSleep, tryToLoadChunk());
                needsToSleep = sleepLock(needsToSleep, tryToLoadPlayer());
            }
            needsToSleep = sleepLock(needsToSleep, tryToSaveChunk());
            needsToSleep = sleepLock(needsToSleep, tryToSavePlayer());
            if (needsToSleep){
                try {
                    //System.out.println("sleeping");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } //else {
                //System.out.println("I'm AWAKE! ");
            //}
        }

        System.out.println("CLOSING WORLD DATABASE!");
        closeWorldDataBase();
    }

    //deserializers
    private Vector3d deserializeVector3d(String serializedVector){
        //turn string into array for easier access
        char[] charArray = serializedVector.toCharArray();

        Vector3d outputVector3d = new Vector3d();

        //x = 0, y = 1, z = 2
        int currentIndex = 0;
        StringBuilder thisStringBuilder = new StringBuilder();

        for (int i = 0; i <= charArray.length; i++){
            if (i == (charArray.length) || charArray[i] == ','){
                switch (currentIndex) {
                    case 0 -> outputVector3d.x = Double.parseDouble(thisStringBuilder.toString());
                    case 1 -> outputVector3d.y = Double.parseDouble(thisStringBuilder.toString());
                    case 2 -> outputVector3d.z = Double.parseDouble(thisStringBuilder.toString());
                }
                thisStringBuilder.setLength(0);
                currentIndex++;
            } else {
                thisStringBuilder.append(charArray[i]);
            }
        }

        return outputVector3d;
    }

    private String[][] stringArrayArrayDeserialize(String serializedArrayArray){
        //figure out the data structure, width and height
        int newIndexCount = 1;
        int newBracketCount = 1;
        boolean newBracketFound = false;
        for (char thisChar : serializedArrayArray.toCharArray()){
            if (!newBracketFound){
                if (thisChar == ','){
                    newIndexCount++;
                }
            }

            if (thisChar == '?'){
                newBracketCount++;
                newBracketFound = true;
            }
        }

        String[][] outPutStringArray = new String[newBracketCount][newIndexCount];

        int currentBracket = 0;
        int currentIndex = 0;

        char[] charArray = serializedArrayArray.toCharArray();

        StringBuilder currentStringBuilder = new StringBuilder();

        //auto-flush indexes
        for (int i = 0; i <= charArray.length; i++){
            if (i < charArray.length) {
                if (charArray[i] == ',') {
                    //don't put in non-null strings ""
                    if (!currentStringBuilder.toString().equals("")) {
                        outPutStringArray[currentBracket][currentIndex] = currentStringBuilder.toString();
                    }
                    currentStringBuilder.setLength(0);
                    currentIndex++;
                } else if (charArray[i] == '?') {
                    //don't put in non-null strings ""
                    if (!currentStringBuilder.toString().equals("")) {
                        outPutStringArray[currentBracket][currentIndex] = currentStringBuilder.toString();
                    }
                    currentStringBuilder.setLength(0);
                    currentIndex = 0;
                    currentBracket++;
                } else {
                    currentStringBuilder.append(charArray[i]);
                }
            } else {
                //don't put in non-null strings ""
                if (!currentStringBuilder.toString().equals("")) {
                    outPutStringArray[currentBracket][currentIndex] = currentStringBuilder.toString();
                }
            }
        }

        return outPutStringArray;
    }

    private int[][] intArrayArrayDeserialize(String serializedArrayArray){
        //figure out the data structure, width and height
        int newIndexCount = 1;
        int newBracketCount = 1;
        boolean newBracketFound = false;
        for (char thisChar : serializedArrayArray.toCharArray()){
            if (!newBracketFound){
                if (thisChar == ','){
                    newIndexCount++;
                }
            }

            if (thisChar == '?'){
                newBracketCount++;
                newBracketFound = true;
            }
        }

        int[][] outPutStringArray = new int[newBracketCount][newIndexCount];

        int currentBracket = 0;
        int currentIndex = 0;

        char[] charArray = serializedArrayArray.toCharArray();

        StringBuilder currentStringBuilder = new StringBuilder();

        //auto-flush indexes
        for (int i = 0; i <= charArray.length; i++){
            if (i < charArray.length) {
                if (charArray[i] == ',') {
                    //don't put in non-null strings ""
                    if (!currentStringBuilder.toString().equals("")) {
                        outPutStringArray[currentBracket][currentIndex] = Integer.parseInt(currentStringBuilder.toString());
                    }
                    currentStringBuilder.setLength(0);
                    currentIndex++;
                } else if (charArray[i] == '?') {
                    //don't put in non-null strings ""
                    if (!currentStringBuilder.toString().equals("")) {
                        outPutStringArray[currentBracket][currentIndex] = Integer.parseInt(currentStringBuilder.toString());
                    }
                    currentStringBuilder.setLength(0);
                    currentIndex = 0;
                    currentBracket++;
                } else {
                    currentStringBuilder.append(charArray[i]);
                }
            } else {
                //don't put in non-null strings ""
                if (!currentStringBuilder.toString().equals("")) {
                    outPutStringArray[currentBracket][currentIndex] = Integer.parseInt(currentStringBuilder.toString());
                }
            }
        }

        return outPutStringArray;
    }


    private byte[] byteDeserialize(String serializedArray){

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

    private int[] intDeserialize(String serializedArray){

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
    private String serializeVector3d(Vector3d inputVec3d){
        return inputVec3d.x + "," + inputVec3d.y + "," + inputVec3d.z;
    }

    private String stringArrayArraySerialize(String[][] inputStringArray){

        StringBuilder output = new StringBuilder();
        int outerCount = 0;
        int outerGoal = inputStringArray.length - 1;
        for (String[] baseArray : inputStringArray){
            int innerCount = 0;
            int innerGoal = baseArray.length - 1;
            for (String stringInArray : baseArray){
                if (stringInArray != null) {
                    output.append(stringInArray);
                }
                if (innerCount != innerGoal){
                    //indicates a new index has started
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

    private String intArrayArraySerialize(int[][] inputStringArray){
        StringBuilder output = new StringBuilder();
        int outerCount = 0;
        int outerGoal = inputStringArray.length - 1;
        for (int[] baseArray : inputStringArray){
            int innerCount = 0;
            int innerGoal = baseArray.length - 1;
            for (int stringInArray : baseArray){
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

    private String byteSerialize(byte[] bytes){

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

    private String intSerialize(int[] ints){

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
