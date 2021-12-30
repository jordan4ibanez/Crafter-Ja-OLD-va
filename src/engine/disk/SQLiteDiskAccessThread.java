package engine.disk;

import game.chunk.ChunkData;
import org.joml.Vector2i;

import java.sql.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static engine.disk.SQLiteDeserializer.byteDeserialize;
import static engine.disk.SQLiteSerializer.byteSerialize;
import static game.chunk.Chunk.*;

public class SQLiteDiskAccessThread implements Runnable {

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


    public boolean saveChunk(int x, int z){

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
        return true;
    }


    public ChunkData loadChunk(int x, int z){
        try {
            Statement statement = connection.createStatement();
            ResultSet resultTest = statement.executeQuery("SELECT * FROM WORLD WHERE ID ='" + x + "-" + z + "';");

            //found a chunk
            if (resultTest.next()) {

                System.out.println("FOUND ONE!");

                ChunkData newChunk = new ChunkData();

                newChunk.x = x;
                newChunk.z = z;
                newChunk.block = byteDeserialize(resultTest.getString("BLOCK"));
                newChunk.rotation = byteDeserialize(resultTest.getString("ROTATION"));
                newChunk.light = byteDeserialize(resultTest.getString("LIGHT"));
                newChunk.heightMap = byteDeserialize(resultTest.getString("HEIGHTMAP"));

                return newChunk;
            }
            //did not find a chunk
            else {
                return null;
            }
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }

        return null;
    }


    //connection closer
    public void closeWorldDataBase(){
        try {
            connection.close();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }

    }

    @Override
    public void run() {
        running.set(true);

        System.out.println("NUMBER 5 IS ALIVE");


    }
}
