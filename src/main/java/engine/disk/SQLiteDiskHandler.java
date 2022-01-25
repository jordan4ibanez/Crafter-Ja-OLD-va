package engine.disk;

import com.fasterxml.jackson.databind.ObjectMapper;
import engine.settings.SettingsObject;
import game.chunk.BiomeGenerator;
import game.chunk.Chunk;
import game.crafting.Inventory;
import game.crafting.InventoryLogic;
import game.player.Player;
import org.joml.Vector2i;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SQLiteDiskHandler {
    private final SQLiteDiskAccessThread sqLiteDiskAccessThread;
    private BiomeGenerator biomeGenerator;
    private Chunk chunk;
    private Player player;
    private InventoryLogic inventoryLogic;

    private boolean hasPolledLoadingPlayer = false;
    private final ConcurrentLinkedDeque<PlayerDataObject> playerData = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<PrimitiveChunkObject> loadingChunkData = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<Vector2i> generatingChunks = new ConcurrentLinkedDeque<>();

    public SQLiteDiskHandler(){
        this.sqLiteDiskAccessThread = new SQLiteDiskAccessThread(this);
    }

    public void setBiomeGenerator(BiomeGenerator biomeGenerator){
        if (this.biomeGenerator == null){
            this.biomeGenerator = biomeGenerator;
            new Thread(biomeGenerator).start();
        }
    }
    public void setChunk(Chunk chunk){
        if (this.chunk == null){
            this.chunk = chunk;
        }
    }
    public void setPlayer(Player player){
        if (this.player == null){
            this.player = player;
        }
    }
    public void setInventoryLogic(InventoryLogic inventoryLogic){
        if (this.inventoryLogic == null){
            this.inventoryLogic = inventoryLogic;
        }
    }

    //this mirrors the object's call
    public void connectWorldDataBase(String worldName){
        //this is needed to create the WORLD table
        this.sqLiteDiskAccessThread.createWorldDataBase(worldName);
        this.sqLiteDiskAccessThread.addPlayerToLoad("singleplayer");
        this.sqLiteDiskAccessThread.start();
    }

    //closes the world's database, kills the thread, removes the object pointer
    public void closeWorldDataBase(){
        //dump it all in to SQLite thread boi
        this.chunk.globalFinalChunkSaveToDisk();
        this.savePlayerData("singleplayer");
        this.sqLiteDiskAccessThread.stop();
        this.hasPolledLoadingPlayer = false;
    }

    public void sendPlayerData(PlayerDataObject player){
        playerData.add(player);
    }

    public void poll(){

        //chunk data
        if (!this.loadingChunkData.isEmpty()){
            PrimitiveChunkObject chunkObject = loadingChunkData.pop();
            this.chunk.addNewChunk(chunkObject);
        }

        if (!this.generatingChunks.isEmpty()){
            Vector2i pos = generatingChunks.pop();
            this.biomeGenerator.addChunkToBiomeGeneration(pos);
        }


        //player data
        if (this.hasPolledLoadingPlayer){
            return;
        }
        if (this.playerData.isEmpty()) {
            return;
        }

        PlayerDataObject player = playerData.pop();

        //set inventory
        for (int y = 0; y < player.inventory.length; y++){
            for (int x = 0; x < player.inventory[0].length; x++){
                inventoryLogic.getInventory().getMain().setItem(x, y, player.inventory[y][x], player.count[y][x]);
                //setInventoryItem("main", x, y, "dirt", 10);
            }
        }

        this.player.setPos(player.pos);
        this.player.setPlayerHealth(player.health);

        this.hasPolledLoadingPlayer = true;

    }

    public void savePlayerData(String name){
        this.sqLiteDiskAccessThread.addPlayerToSave(name, inventoryLogic.getInventory().getMain().getInventoryAsArray(), inventoryLogic.getInventory().getMain().getCountAsArray(), player.getPlayerPos(), (byte) player.getPlayerHealth());
    }

    public void loadChunk(Vector2i key){
        this.sqLiteDiskAccessThread.addLoadChunk(key);
    }

    public void saveChunk(Vector2i pos, byte[] blockData, byte[] rotationData, byte[] lightData, byte[] heightMap){
        this.sqLiteDiskAccessThread.addSaveChunk(pos, blockData,rotationData,lightData,heightMap);
    }

    public void setChunk(PrimitiveChunkObject primitiveChunkObject){
        this.loadingChunkData.add(primitiveChunkObject);
    }

    public void addChunkToBiomeGenerator(Vector2i pos){
        this.generatingChunks.add(new Vector2i(pos));
    }

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
