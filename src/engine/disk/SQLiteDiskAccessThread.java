package engine.disk;

import engine.graphics.Mesh;
import game.chunk.ChunkData;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.sql.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static engine.disk.SQLiteDeserializer.byteDeserialize;
import static engine.disk.SQLiteSerializer.byteSerialize;
import static game.chunk.BiomeGenerator.addChunkToBiomeGeneration;
import static game.chunk.Chunk.*;
import static game.chunk.ChunkUpdateHandler.chunkUpdate;

public class SQLiteDiskAccessThread implements Runnable {

    //class fields
    private String url;
    private Connection connection;
    private DatabaseMetaData meta;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public void connectWorldDataBase(String worldName){
        //databases are automatically created with the JBDC driver

        //database parameters
        url = "jdbc:sqlite:" + System.getProperty("user.dir") +  "/Worlds/" + worldName + "/map.db";

        try {
            //database connection, static private
            connection = DriverManager.getConnection(url);

            if (connection != null){
                //metadata testing
                meta = connection.getMetaData();

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


        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }


    public void saveChunk(int x, int z){

        try {

            Statement statement = connection.createStatement();

            Vector2i key = new Vector2i(x,z);

            String sql = "INSERT OR REPLACE INTO WORLD " +
                    "(ID,BLOCK,ROTATION,LIGHT,HEIGHTMAP) " +
                    "VALUES ('" +
                    x + "-" + z + "','" + //ID
                    byteSerialize(getBlockData(key)) + "','" +//BLOCK ARRAY
                    byteSerialize(getRotationData(key)) + "','" +//ROTATION DATA
                    byteSerialize(getLightData(key)) + "','" +//LIGHT DATA
                    byteSerialize(getHeightMapData(key))+ //HEIGHT DATA
                    "');";
            statement.executeUpdate(sql);
            statement.close();


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static final ConcurrentLinkedDeque<Vector2i> chunksToLoad = new ConcurrentLinkedDeque<>();

    //do this so that the main thread does not hang
    public void addLoadChunk(int x, int z){
        chunksToLoad.add(new Vector2i(x,z));
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

                    System.out.println("LOADING CHUNK FROM DATABASE!");

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
                    System.out.println("generate chunk here");
                    addChunkToBiomeGeneration(x, z);
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    //connection closer
    public void closeWorldDataBase(){
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

        while(running.get() ) {
            //System.out.println("NUMBER 5 IS ALIVE");
            tryToLoadChunk();

            System.out.println("remember to make saving chunks non-blocking");

        }

        System.out.println("CLOSING WORLD DATABASE!");
        closeWorldDataBase();
    }
}
