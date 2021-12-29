package engine.disk;

import com.fasterxml.jackson.databind.ObjectMapper;
import engine.Utils;
import org.joml.Vector2i;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;

import static engine.Utils.loadResource;
import static engine.Utils.saveResource;
import static engine.Window.windowShouldClose;
import static game.chunk.Chunk.*;

public class SaveQueue {

    /*

    //public static Deque<ChunkSavingObject> saveQueue;
    public static void startSaveThread(){
        new Thread(() -> {

            final ObjectMapper mapper = new ObjectMapper();
            saveQueue = new ArrayDeque<>();

            while(!windowShouldClose()) {
                if (!saveQueue.isEmpty()) {
                    try {

                        ChunkSavingObject thisChunk = saveQueue.pop();


                        String stringedChunk = mapper.writeValueAsString(thisChunk);

                        saveResource("Worlds/world" + currentActiveWorld + "/" + thisChunk.x + " " + thisChunk.z + ".chunk", stringedChunk);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    public static void saveChunk(Vector2i key){
        ChunkSavingObject saveData = new ChunkSavingObject();
        saveData.x = key.x;
        saveData.z = key.y;
        //todo: test if .clone() is not needed
        saveData.b = getBlockData(key).clone();
        saveData.h = getHeightMapData(key).clone();
        saveData.l = getLightData(key).clone();
        saveData.r = getRotationData(key).clone();


        saveData = null;
        //sqlite save goes here

        //saveQueue.add(saveData);
    }

    public static void instantSave(Vector2i key){

        ObjectMapper mapper = new ObjectMapper();
        try {
            ChunkSavingObject saveData = new ChunkSavingObject();
            saveData.x = key.x;
            saveData.z = key.y;
            //todo: test if .clone() is not needed
            saveData.b = getBlockData(key).clone();
            saveData.h = getHeightMapData(key).clone();
            saveData.l = getLightData(key).clone();
            saveData.r = getRotationData(key).clone();

            String stringedChunk = mapper.writeValueAsString(saveData);

            //learned from https://www.journaldev.com/966/java-gzip-example-compress-decompress-file
            ByteArrayInputStream bais = new ByteArrayInputStream(stringedChunk.getBytes());
            FileOutputStream fos = new FileOutputStream("Worlds/world" + currentActiveWorld + "/" + key.x + " " + key.y + ".chunk");
            GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
            byte[] buffer = new byte[4096];
            int len;
            while((len=bais.read(buffer)) != -1){
                gzipOS.write(buffer, 0, len);
            }
            //close resources
            gzipOS.close();
            fos.close();
            bais.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    */
}
