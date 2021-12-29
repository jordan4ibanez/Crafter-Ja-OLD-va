package engine.disk;

public class SQLiteSerializer {
    public static String crafterByteSerializer(byte[] bytes){

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

    public static String crafterIntSerializer(int[] ints){

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
