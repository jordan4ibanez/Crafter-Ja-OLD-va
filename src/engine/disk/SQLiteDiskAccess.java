package engine.disk;

import game.chunk.ChunkData;
import org.joml.Vector2i;

import java.sql.*;
import java.util.Arrays;

import static engine.disk.SQLiteDeserializer.byteDeserialize;
import static engine.disk.SQLiteSerializer.byteSerialize;
import static game.chunk.Chunk.*;

public class SQLiteDiskAccess {

    private static String url;

    private static Connection connection;

    private static DatabaseMetaData meta;



    public static void connectWorldDataBase(String worldName){
        //databases are automatically created with the JBDC driver

        //database parameters
        url = "jdbc:sqlite:" + System.getProperty("user.dir") +  "/Worlds/" + worldName + "/map.db";

        /*
        //THIS IS DEBUG CODE FOR SQL

        //assign blank byte array
        byte[] test = new byte[55];
        //random values
        test[0] = 15;
        test[54] = 12;
        test[53] = 20;
        test[1] = 5;

        //debug output

        String myString = byteSerialize(test);
        System.out.println(myString);

        byte[] outPut = byteDeserialize(myString);

        //debug output of created array
        //was byte[] then String and now byte[]
        //saved from game -> into database -> loaded into game
        System.out.println(Arrays.toString(outPut));
         */



        try {
            //database connection, static private
            connection = DriverManager.getConnection(url);

            if (connection != null){
                //metadata testing
                meta = connection.getMetaData();

                //turn on Write Ahead Log for performance
                Statement statement = connection.createStatement();
                String sql = "PRAGMA journal_mode=WAL";
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


            /*
            if (false) {
                String sql = "INSERT INTO WORLD " +
                        "(ID,BLOCK,ROTATION,LIGHT,HEIGHTMAP) " +
                        "VALUES ('5', 'Alle324', '2544', '44Texas', '15000.00' );";
                statement.executeUpdate(sql);

                statement.close();
            }

            //resultSet = statement.executeQuery("SELECT 2 FROM WORLD");
             */


        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }


    public static boolean saveChunk(int x, int z){

        if (true ) {
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
        return true;
    }


    public static ChunkData loadChunk(int x, int z){
        try {
            Statement statement = connection.createStatement();
            ResultSet resultTest = statement.executeQuery("SELECT * FROM WORLD WHERE ID = " + x + " " + z + ";");

            //found a chunk
            if (resultTest.next()) {

                String name = resultTest.getString("ID");
                String name2 = resultTest.getString("BLOCK");

                System.out.println(name);
                System.out.println(name2);
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
    public static void closeWorldDataBase(){
        try {
            connection.close();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

}
