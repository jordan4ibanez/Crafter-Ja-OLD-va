package engine.settings;

import static engine.Window.setVSync;
import static engine.disk.Disk.loadSettingsFromDisk;
import static engine.disk.Disk.saveSettingsToDisk;
import static game.chunk.Chunk.generateNewChunks;
import static game.chunk.ChunkUpdateHandler.updateChunkLoadingSpeed;
import static org.lwjgl.glfw.GLFW.*;

public class Settings {

    //todo: fix crashing when players mess with Settings.conf

    private static SettingsObject settings;

    //false for production (debugInfo)
    private static boolean debugInfo = false;
    private static boolean vSync;
    private static boolean graphicsMode; //true = fancy, false = fast
    private static byte chunkLoading;
    private static int renderDistance;

    private static int keyForward;
    private static int keyBack;
    private static int keyLeft;
    private static int keyRight;
    private static int keySneak;
    private static int keyDrop;
    private static int keyJump;
    private static int keyInventory;

    public static void loadSettings(){

        SettingsObject loadedSettings = loadSettingsFromDisk();

        if (loadedSettings == null) {
            //default if no settings set

            //instantiate new object
            settings = new SettingsObject();

            vSync = true;
            settings.vSync = true;

            graphicsMode = true;
            settings.graphicsMode = true;

            renderDistance = 5;
            settings.renderDistance = 5;

            //lower slower, higher faster
            chunkLoading = 2;
            settings.chunkLoading = 2;

            keyForward = GLFW_KEY_W;
            settings.keyForward = GLFW_KEY_W;

            keyBack = GLFW_KEY_S;
            settings.keyBack = GLFW_KEY_S;

            keyLeft = GLFW_KEY_A;
            settings.keyLeft = GLFW_KEY_A;

            keyRight = GLFW_KEY_D;
            settings.keyRight = GLFW_KEY_D;

            keySneak = GLFW_KEY_LEFT_SHIFT;
            settings.keySneak = GLFW_KEY_LEFT_SHIFT;

            keyDrop = GLFW_KEY_Q;
            settings.keyDrop = GLFW_KEY_Q;

            keyJump = GLFW_KEY_SPACE;
            settings.keyJump = GLFW_KEY_SPACE;

            keyInventory = GLFW_KEY_E;
            settings.keyInventory = GLFW_KEY_E;

            //save default values
            saveSettingsToDisk(settings);

        } else {

            //dump new settings in
            settings = loadedSettings;

            vSync = settings.vSync;
            graphicsMode = settings.graphicsMode;
            renderDistance = settings.renderDistance;
            chunkLoading = settings.chunkLoading;

            keyForward = settings.keyForward;
            keyBack = settings.keyBack;
            keyLeft = settings.keyLeft;
            keyRight = settings.keyRight;
            keySneak = settings.keySneak;
            keyDrop = settings.keyDrop;
            keyJump = settings.keyJump;
            keyInventory = settings.keyInventory;
        }
    }

    //debug info
    public static boolean getDebugInfo(){
        return debugInfo;
    }
    public static void setDebugInfo(boolean truth){
        debugInfo = truth;
    }
    public static void invertDebugInfoBoolean(){ //todo: remove this crap
        debugInfo = !debugInfo;
    }

    //render distance
    public static int getRenderDistance(){
        return renderDistance;
    }
    public static void setRenderDistance(int newRenderDistance, boolean inGame){
        renderDistance = newRenderDistance;
        settings.renderDistance = newRenderDistance;
        if (inGame) {
            generateNewChunks();
        }
    }

    //vsync
    public static void setSettingsVsync(boolean truth){
        vSync = truth;
        settings.vSync = truth;
        setVSync(truth);
    }
    public static boolean getSettingsVsync(){
        return vSync;
    }

    //lazy chunk loading
    public static void setSettingsChunkLoad(byte truth){
        chunkLoading = truth;
        settings.chunkLoading = truth;
        updateChunkLoadingSpeed();
    }
    public static byte getSettingsChunkLoad(){
        return chunkLoading;
    }

    //graphics mode
    public static void setGraphicsMode(boolean newGraphicsMode){
        graphicsMode = newGraphicsMode;
        settings.graphicsMode = newGraphicsMode;
    }
    public static boolean getGraphicsMode(){
        return graphicsMode;
    }

    //input keys

    //forward
    public static void setKeyForward(int newKey){
        keyForward = newKey;
        settings.keyForward = newKey;
    }
    public static int getKeyForward(){
        return keyForward;
    }

    //back
    public static void setKeyBack(int newKey){
        keyBack = newKey;
        settings.keyBack = newKey;
    }
    public static int getKeyBack(){
        return keyBack;
    }

    //left
    public static void setKeyLeft(int newKey){
        keyLeft = newKey;
        settings.keyLeft = newKey;
    }
    public static int getKeyLeft(){
        return keyLeft;
    }

    //right
    public static void setKeyRight(int newKey){
        keyRight = newKey;
        settings.keyRight = newKey;
    }
    public static int getKeyRight(){
        return keyRight;
    }

    //sneak
    public static void setKeySneak(int newKey){
        keySneak = newKey;
        settings.keySneak = newKey;
    }
    public static int getKeySneak(){
        return keySneak;
    }

    //drop
    public static void setKeyDrop(int newKey){
        keyDrop = newKey;
        settings.keyDrop = newKey;
    }
    public static int getKeyDrop(){
        return keyDrop;
    }

    //jump
    public static void setKeyJump(int newKey){
        keyJump = newKey;
        settings.keyJump = newKey;
    }
    public static int getKeyJump(){
        return keyJump;
    }

    //inventory
    public static void setKeyInventory(int newKey){
        keyInventory = newKey;
        settings.keyInventory = newKey;
    }
    public static int getKeyInventory(){
        return keyInventory;
    }

    //settings saving
    public static void saveSettings(){
        saveSettingsToDisk(settings);
    }
}
