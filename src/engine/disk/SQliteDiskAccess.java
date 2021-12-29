package engine.disk;

import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Vector;

public class SQliteDiskAccess {

    private static String url;

    private static Connection connection;

    private static DatabaseMetaData meta;



    public static void connectWorldDataBase(String worldName){
        //databases are automatically created with the JBDC driver

        //database parameters
        url = "jdbc:sqlite:" + System.getProperty("user.dir") +  "/Worlds/" + worldName + "/map.db";


        //THIS IS DEBUG CODE FOR SQL

        //assign blank byte array
        byte[] test = new byte[55];
        //random values
        test[0] = 15;
        test[54] = 12;
        test[53] = 20;
        test[1] = 5;
        //build a raw custom string type to hold data, data elements only separated by commas
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < test.length; i++){
            str.append(test[i]);
            if (i != test.length - 1){
                str.append(",");
            }
        }

        //debug output
        String myString = str.toString();
        System.out.println(str.toString());

        //start at one to auto-add in the last item
        int numberOfThings = 1;

        //iterate number of elements, this is why the stringed array contains only commas and numbers
        for (char c : myString.toCharArray()){
            if (c == ','){
                numberOfThings++;
            }
        }

        //create new blank array
        byte[] outPut = new byte[numberOfThings];

        //create a new string builder
        StringBuilder decode = new StringBuilder();

        int index = 0;
        //auto-flush indexes
        char[] charArray = myString.toCharArray();
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

        //debug output of created array
        //was byte[] then String and now byte[]
        //saved from game -> into database -> loaded into game
        System.out.println(Arrays.toString(outPut));



        try {
            //database connection, static private
            connection = DriverManager.getConnection(url);

            if (connection != null){
                //metadata testing
                meta = connection.getMetaData();

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

            while (resultSet.next()) {

                String name = resultSet.getString("TABLE_NAME");
                
                //found the world table
                if (name.equals("WORLD")){
                    found = true;
                    break;
                }
            }

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


        } catch (Exception e){
            System.out.println(e.getMessage());
        }



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
