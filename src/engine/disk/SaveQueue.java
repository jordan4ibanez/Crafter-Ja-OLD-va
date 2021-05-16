package engine.disk;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import game.chunk.ChunkObject;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.zip.GZIPOutputStream;


import static engine.Window.windowShouldClose;

public class SaveQueue {

    public static Deque<ChunkObject> saveQueue;

    public static void startSaveThread(){
        new Thread(() -> {
            final ObjectMapper mapper = new ObjectMapper();
            final ObjectWriter writer = null;

            ChunkObject thisChunk;
            saveQueue = new ArrayDeque<>();
            while(!windowShouldClose()) {
                if (!saveQueue.isEmpty()) {
                    try {
                        thisChunk = saveQueue.pop();

                        ChunkSavingObject savingObject = new ChunkSavingObject();

                        savingObject.ID = thisChunk.ID;
                        savingObject.x = thisChunk.x;
                        savingObject.z = thisChunk.z;
                        savingObject.block = thisChunk.block;
                        savingObject.rotation = thisChunk.rotation;
                        savingObject.light = thisChunk.light;
                        savingObject.heightMap = thisChunk.heightMap;
                        savingObject.lightLevel = thisChunk.lightLevel;

                        String stringedChunk = mapper.writeValueAsString(savingObject);

                        //crate new file if does not exist
                        File test = new File("Worlds/world1/" + savingObject.ID + ".chunk");
                        if (!test.canRead()){
                            File newFile = new File("Worlds/world1/" + savingObject.ID + ".chunk");
                            newFile.createNewFile();
                        }

                        //learned from https://www.journaldev.com/966/java-gzip-example-compress-decompress-file
                        ByteArrayInputStream bais = new ByteArrayInputStream(stringedChunk.getBytes());
                        FileOutputStream fos = new FileOutputStream("Worlds/world1/" + savingObject.ID + ".chunk");
                        GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
                        byte[] buffer = new byte[1024];
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
            }
        }).start();
    }

    public static void saveChunk(ChunkObject thisChunk){
        saveQueue.add(thisChunk);
    }

    public static void instantSave(ChunkObject thisChunk){
        ObjectMapper mapper = new ObjectMapper();
        try {
            //System.out.println("SAVED WORLD!");
            ChunkSavingObject savingObject = new ChunkSavingObject();

            savingObject.ID = thisChunk.ID;
            savingObject.x = thisChunk.x;
            savingObject.z = thisChunk.z;
            savingObject.block = thisChunk.block;
            savingObject.rotation = thisChunk.rotation;
            savingObject.light = thisChunk.light;
            savingObject.heightMap = thisChunk.heightMap;

            String stringedChunk = mapper.writeValueAsString(savingObject);

            //crate new file if does not exist
            File test = new File("Worlds/world1/" + savingObject.ID + ".chunk");
            if (!test.canRead()){
                File newFile = new File("Worlds/world1/" + savingObject.ID + ".chunk");
                newFile.createNewFile();
            }

            //learned from https://www.journaldev.com/966/java-gzip-example-compress-decompress-file
            ByteArrayInputStream bais = new ByteArrayInputStream(stringedChunk.getBytes());
            FileOutputStream fos = new FileOutputStream("Worlds/world1/" + savingObject.ID + ".chunk");
            GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
            byte[] buffer = new byte[1024];
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
}
