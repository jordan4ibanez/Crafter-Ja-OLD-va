package engine.disk;

import game.chunk.ChunkObject;

import java.util.ArrayDeque;
import java.util.Deque;

import static engine.Window.windowShouldClose;

public class SaveQueue {

    public static Deque<ChunkObject> saveQueue;

    public static void startSaveThread(){
        new Thread(() -> {
//            final ObjectMapper mapper = new ObjectMapper();
            float timer = 0f;
            ChunkObject thisChunk;
            saveQueue = new ArrayDeque<>();
            while(!windowShouldClose()) {
                timer += 0.01f;
                if (timer >= 3f) {
                    timer = 0f;
                    if (!saveQueue.isEmpty()) {
//                        System.out.println("starting new save thread " + Math.random());
//                        try {
                            thisChunk = saveQueue.pop();
//                            mapper.writeValue(new File("Worlds/world1/" + thisChunk.ID + ".chunk"), thisChunk);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                    }
                }
            }
        }).start();
    }

    public static void saveChunk(ChunkObject thisChunk){
        saveQueue.add(thisChunk);
    }

    public static void instantSave(ChunkObject thisChunk){
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            mapper.writeValue(new File("Worlds/world1/" + thisChunk.ID + ".chunk"), thisChunk);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
