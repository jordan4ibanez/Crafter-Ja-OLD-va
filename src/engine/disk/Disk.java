package engine.disk;

import com.fasterxml.jackson.databind.ObjectMapper;
import game.chunk.ChunkObject;
import org.joml.Vector3d;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Disk {

    public static void initializeWorldHandling(){
        createWorldsDir();
        createAlphaWorldFolder();
    }

    private static void createWorldsDir(){
        try {
            Files.createDirectories(Paths.get("Worlds"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createAlphaWorldFolder(){
        try {
            Files.createDirectories(Paths.get("Worlds/world1"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static ObjectMapper objectMapper = new ObjectMapper();

    public static ChunkObject loadChunkFromDisk(int x, int z){

        //System.out.println("loading!!");
        String key = x + " " + z;

        ChunkSavingObject thisChunkLoaded = null;

        File test = new File("Worlds/world1/" + key + ".chunk");

        if (!test.canRead()){
            //System.out.println("FAILED TO LOAD A CHUNK!");
            return(null);
        }

        try {
            thisChunkLoaded = objectMapper.readValue(test, ChunkSavingObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (thisChunkLoaded != null && thisChunkLoaded.block == null){
            return null;
        }

        ChunkObject abstractedChunk = new ChunkObject();

        abstractedChunk.ID = thisChunkLoaded.ID;
        abstractedChunk.x = thisChunkLoaded.x;
        abstractedChunk.z = thisChunkLoaded.z;
        abstractedChunk.block = thisChunkLoaded.block;
        abstractedChunk.rotation = thisChunkLoaded.rotation;
        abstractedChunk.light = thisChunkLoaded.light;
        abstractedChunk.heightMap = thisChunkLoaded.heightMap;
        abstractedChunk.lightLevel = thisChunkLoaded.lightLevel;

        return(abstractedChunk);
    }

    public static void savePlayerPos(Vector3d pos){
        SpecialSavingVector3d tempPos = new SpecialSavingVector3d();
        tempPos.x = pos.x;
        tempPos.y = pos.y;
        tempPos.z = pos.z;
        try {
            objectMapper.writeValue(new File("Worlds/world1/playerPos.data"), tempPos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Vector3d loadPlayerPos(){

        File test = new File("Worlds/world1/playerPos.data");

        Vector3d thisPos = new Vector3d(0,100,0);

        if (!test.canRead()){
            return thisPos;
        }

        //this needs a special object class because
        //vector3f has a 4th variable which jackson cannot
        //understand
        SpecialSavingVector3d tempPos = null;

        try {
            tempPos = objectMapper.readValue(test, SpecialSavingVector3d.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (tempPos != null){
            thisPos.x = tempPos.x;
            thisPos.y = tempPos.y;
            thisPos.z = tempPos.z;
        }

        return thisPos;
    }
}
