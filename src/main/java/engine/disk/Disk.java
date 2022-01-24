package engine.disk;

import com.fasterxml.jackson.databind.ObjectMapper;
import engine.settings.SettingsObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class Disk {

    private SQLiteDiskHandler sqLiteDiskHandler;

    public Disk(){

    }

    public void setSqLiteDiskHandler(SQLiteDiskHandler sqLiteDiskHandler){
        if (this.sqLiteDiskHandler == null){
            this.sqLiteDiskHandler = sqLiteDiskHandler;
        }
    }

    private byte currentActiveWorld = 1; //failsafe

    public void updateSaveQueueCurrentActiveWorld(byte newWorld){
        currentActiveWorld = newWorld;
    }

    public byte getCurrentActiveWorld(){
        return currentActiveWorld;
    }

    //this is the main entry point for disk access to the world from the main menu
    public void setCurrentActiveWorld(byte newWorld){
        currentActiveWorld = newWorld;
        updateSaveQueueCurrentActiveWorld(newWorld);
        createAlphaWorldFolder();
        //eh no where to really put this so just gonna stick it here
        sqLiteDiskHandler.connectWorldDataBase("world" + currentActiveWorld);
        System.out.println("CURRENT WORLD IS: " + newWorld);
    }

    //https://stackoverflow.com/a/24734290
    //https://stackoverflow.com/a/3758880
    public String worldSize(byte world) {
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

    public void createWorldsDir(){
        try {
            Files.createDirectories(Paths.get("Worlds"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createAlphaWorldFolder(){
        try {
            Files.createDirectories(Paths.get("Worlds/world" + currentActiveWorld));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateWorldsPathToAvoidCrash(){
        createWorldsDir();
        createAlphaWorldFolder();
    }


    private final ObjectMapper objectMapper = new ObjectMapper();


    public SettingsObject loadSettingsFromDisk(){
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

    public void saveSettingsToDisk(SettingsObject settingsObject){
        try {
            objectMapper.writeValue(new File("Settings.conf"), settingsObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
