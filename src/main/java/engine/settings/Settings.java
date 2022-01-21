package engine.settings;

import engine.Window.setVSync;
import engine.disk.Disk.loadSettingsFromDisk;
import engine.disk.Disk.saveSettingsToDisk;
import game.chunk.Chunk.generateNewChunks;
import org.lwjgl.glfw.GLFW.*;

public class Settings {

    //todo: fix crashing when players mess with Settings.conf

    private SettingsObject settings;

    //false for production (debugInfo)
    private boolean debugInfo = false;
    private boolean vSync;
    private boolean graphicsMode; //true = fancy, false = fast
    private byte chunkLoading;
    private int renderDistance;

    private int keyForward;
    private int keyBack;
    private int keyLeft;
    private int keyRight;
    private int keySneak;
    private int keyDrop;
    private int keyJump;
    private int keyInventory;

    public void loadSettings(){

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
    public boolean getDebugInfo(){
        return debugInfo;
    }
    public void setDebugInfo(boolean truth){
        debugInfo = truth;
    }
    public void invertDebugInfoBoolean(){ //todo: remove this crap
        debugInfo = !debugInfo;
    }

    //render distance
    public int getRenderDistance(){
        return renderDistance;
    }
    public void setRenderDistance(int newRenderDistance, boolean inGame){
        renderDistance = newRenderDistance;
        settings.renderDistance = newRenderDistance;
        if (inGame) {
            generateNewChunks();
        }
    }

    //vsync
    public void setSettingsVsync(boolean truth){
        vSync = truth;
        settings.vSync = truth;
        setVSync(truth);
    }
    public boolean getSettingsVsync(){
        return vSync;
    }

    //lazy chunk loading
    public void setSettingsChunkLoad(byte truth){
        chunkLoading = truth;
        settings.chunkLoading = truth;
    }
    public byte getSettingsChunkLoad(){
        return chunkLoading;
    }

    //graphics mode
    public void setGraphicsMode(boolean newGraphicsMode){
        graphicsMode = newGraphicsMode;
        settings.graphicsMode = newGraphicsMode;
    }
    public boolean getGraphicsMode(){
        return graphicsMode;
    }

    //input keys

    //forward
    public void setKeyForward(int newKey){
        keyForward = newKey;
        settings.keyForward = newKey;
    }
    public int getKeyForward(){
        return keyForward;
    }

    //back
    public void setKeyBack(int newKey){
        keyBack = newKey;
        settings.keyBack = newKey;
    }
    public int getKeyBack(){
        return keyBack;
    }

    //left
    public void setKeyLeft(int newKey){
        keyLeft = newKey;
        settings.keyLeft = newKey;
    }
    public int getKeyLeft(){
        return keyLeft;
    }

    //right
    public void setKeyRight(int newKey){
        keyRight = newKey;
        settings.keyRight = newKey;
    }
    public int getKeyRight(){
        return keyRight;
    }

    //sneak
    public void setKeySneak(int newKey){
        keySneak = newKey;
        settings.keySneak = newKey;
    }
    public int getKeySneak(){
        return keySneak;
    }

    //drop
    public void setKeyDrop(int newKey){
        keyDrop = newKey;
        settings.keyDrop = newKey;
    }
    public int getKeyDrop(){
        return keyDrop;
    }

    //jump
    public void setKeyJump(int newKey){
        keyJump = newKey;
        settings.keyJump = newKey;
    }
    public int getKeyJump(){
        return keyJump;
    }

    //inventory
    public void setKeyInventory(int newKey){
        keyInventory = newKey;
        settings.keyInventory = newKey;
    }
    public int getKeyInventory(){
        return keyInventory;
    }

    //settings saving
    public void saveSettings(){
        saveSettingsToDisk(settings);
    }
}
