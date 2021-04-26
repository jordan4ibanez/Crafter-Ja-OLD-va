package engine.disk;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite {
    public static void databaseConnect(){
        Connection conn = null;
        try{
            String url = "jdbc:sqlite:" + System.getProperty("user.dir") + "/Worlds/world1/worldData.db";
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite database has been established");
        } catch (SQLException e){
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null){
                    Statement test = conn.createStatement();
                    
                    conn.close();
                }
            } catch (SQLException ex){
                System.out.println(ex.getMessage());
            }
        }
    }
}
