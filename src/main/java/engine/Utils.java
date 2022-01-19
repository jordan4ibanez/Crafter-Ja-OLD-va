package engine;

import org.lwjgl.BufferUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.lwjgl.BufferUtils.createByteBuffer;

public class Utils {

    //load plain text file
    public static String loadResource(String fileName) throws Exception{
        
        File text = new File(fileName);
        
        Scanner scanner = new Scanner(text, StandardCharsets.UTF_8.name());
        
        String result = scanner.useDelimiter("\\A").next();
        
        return result;
    }

    //save string as plain text
    public static void saveResource(String fileName, String data) throws IOException{
        FileWriter writer = new FileWriter(fileName);
        
        try (BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            bufferedWriter.write(data);
        }
    }

    public static List<String> readAllLines(String fileName) throws Exception {
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Class.forName(Utils.class.getName()).getResourceAsStream(fileName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
        }
        return list;
    }

    public static float[] listToArray(List<Float> list) {
        int size = list != null ? list.size() : 0;
        
        assert list != null;
        
        float[] floatArr = new float[size];
        for (int i = 0; i < size; i++) {
            floatArr[i] = list.get(i);
        }
        return floatArr;
    }

    @SuppressWarnings("empty-statement")
    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException{
        ByteBuffer buffer;

        Path path = Paths.get(resource);

        if(Files.isReadable(path)){
            try (SeekableByteChannel fc = Files.newByteChannel(path)){
                buffer = createByteBuffer((int)fc.size()+1);

                while (fc.read(buffer) != -1);
            }
        } else{
            try(InputStream source = Utils.class.getResourceAsStream(resource); ReadableByteChannel rbc = Channels.newChannel(source)){
                buffer = createByteBuffer(bufferSize);

                while(true){
                    int bytes = rbc.read(buffer);
                    if(bytes == -1){
                        break;
                    }
                    if(buffer.remaining() == 0){
                        buffer = resizeBuffer(buffer, buffer.capacity() * 2);
                    }
                }
            }
        }

        buffer.flip();
        return buffer;
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity){
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }
}
