package engine.disk;

public class SQLiteDeserializer {
    public static byte[] byteDeserialize(String serializedArray){

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

    public static int[] intDeserialize(String serializedArray){

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
}
