package engine.disk;

import java.sql.*;

public class SQliteDiskAccess {

    private static String url;

    private static Connection connection;

    private static DatabaseMetaData meta;

    public static void connectWorldDataBase(String worldName){
        //databases are automatically created with the JBDC driver

        //database parameters
        url = "jdbc:sqlite:" + System.getProperty("user.dir") +  "/Worlds/" + worldName + "/map.db";

        //create a world database if it does not exist
        try {
            //generic connection in class
            connection = DriverManager.getConnection(url);

            if (connection != null){
                //metadata testing
                meta = connection.getMetaData();

            }
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public static void closeWorldDataBase(){
        try {
            connection.close();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

}
