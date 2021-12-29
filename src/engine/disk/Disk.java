package engine.disk;

import com.fasterxml.jackson.databind.ObjectMapper;
import engine.settings.SettingsObject;
import game.chunk.ChunkData;
import org.joml.Vector3d;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class Disk {

    private static byte currentActiveWorld = 1; //failsafe

    public static void updateSaveQueueCurrentActiveWorld(byte newWorld){
        currentActiveWorld = newWorld;
    }

    public static byte getCurrentActiveWorld(){
        return currentActiveWorld;
    }

    public static void setCurrentActiveWorld(byte newWorld){
        currentActiveWorld = newWorld;
        updateSaveQueueCurrentActiveWorld(newWorld);
        createAlphaWorldFolder();

        System.out.println("CURRENT WORLD IS: " + newWorld);
    }

    //https://stackoverflow.com/a/24734290
    //https://stackoverflow.com/a/3758880
    public static String worldSize(byte world) {
        long bytes = 0;

        Path thisWorld = Paths.get("Worlds/world" + world);

        if (!Files.isDirectory(Paths.get("Worlds/world" + world))){
            return "";
        }

        try {
            bytes = Files.walk(thisWorld).mapToLong( p -> p.toFile().length() ).sum();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return " (" + String.format("%.1f %cB", bytes / 1000.0, ci.current()) + ")";
    }

    public static void createWorldsDir(){
        try {
            Files.createDirectories(Paths.get("Worlds"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createAlphaWorldFolder(){
        try {
            Files.createDirectories(Paths.get("Worlds/world" + currentActiveWorld));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateWorldsPathToAvoidCrash(){
        createWorldsDir();
        createAlphaWorldFolder();
    }


    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static SettingsObject loadSettingsFromDisk(){
        File file = new File("Settings.conf");

        if (!file.canRead()){
            return null;
        }

        SettingsObject settings = null;

        try {
            settings = objectMapper.readValue(file, SettingsObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return settings;
    }

    public static void saveSettingsToDisk(SettingsObject settingsObject){
        try {
            objectMapper.writeValue(new File("Settings.conf"), settingsObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static ChunkData loadChunkFromDisk(int x, int z) throws IOException {

        //System.out.println("loading!!");
        String key = x + " " + z;
        String dir = "Worlds/world" + currentActiveWorld + "/" + key + ".chunk";

        ChunkSavingObject thisChunkLoaded = null;

        File test = new File(dir);

         //cannot read file
        if (!test.canRead()){
            return(null);
        }



        try {
            thisChunkLoaded = objectMapper.readValue(test.toString(), ChunkSavingObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (thisChunkLoaded == null){
            return null;
        }

        if (thisChunkLoaded.b == null){
            return null;
        }


        ChunkData abstractedChunk = new ChunkData();

        abstractedChunk.x = thisChunkLoaded.x;
        abstractedChunk.z = thisChunkLoaded.z;
        abstractedChunk.block = thisChunkLoaded.b;
        abstractedChunk.rotation = thisChunkLoaded.r;
        abstractedChunk.light = thisChunkLoaded.l;
        abstractedChunk.heightMap = thisChunkLoaded.h;

        //a test to see if there are other problems besides object mapper
        return(null);

        //return(abstractedChunk);
    }

    public static void savePlayerPos(Vector3d pos){
        SpecialSavingVector3d tempPos = new SpecialSavingVector3d();
        tempPos.x = pos.x;
        tempPos.y = pos.y;
        tempPos.z = pos.z;
        try {
            objectMapper.writeValue(new File("Worlds/world" + currentActiveWorld + "/playerPos.data"), tempPos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Vector3d loadPlayerPos(){

        File test = new File("Worlds/world" + currentActiveWorld + "/playerPos.data");

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
