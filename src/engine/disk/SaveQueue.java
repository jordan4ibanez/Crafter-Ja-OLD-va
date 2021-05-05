package engine.disk;

import com.fasterxml.jackson.databind.ObjectMapper;
import game.chunk.ChunkObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;


import static engine.Window.windowShouldClose;

public class SaveQueue {

    public static Deque<ChunkObject> saveQueue;

    public static void startSaveThread(){
        new Thread(() -> {
            final ObjectMapper mapper = new ObjectMapper();
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

                        mapper.writeValue(new File("Worlds/world1/" + savingObject.ID + ".chunk"), savingObject);
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


            mapper.writeValue(new File("Worlds/world1/" + savingObject.ID + ".chunk"), savingObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
