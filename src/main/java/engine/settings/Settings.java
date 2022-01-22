package engine.settings;

import engine.Window;
import engine.disk.Disk;
import game.chunk.Chunk;

import static org.lwjgl.glfw.GLFW.*;

public class Settings {

    private final Window window;
    private final Disk disk;
    private final SettingsObject settingsObject;
    private Chunk chunk;
    private boolean debugInfo; //assigned false

    public Settings(Disk disk, Window window){
        this.disk = disk;
        this.window = window;

        SettingsObject loadedSettings = disk.loadSettingsFromDisk();

        if (loadedSettings == null) {
            this.settingsObject = new SettingsObject();
            this.settingsObject.vSync = true;
            this.settingsObject.graphicsMode = true;
            this.settingsObject.renderDistance = 5;
            this.settingsObject.keyForward = GLFW_KEY_W;
            this.settingsObject.keyBack = GLFW_KEY_S;
            this.settingsObject.keyLeft = GLFW_KEY_A;
            this.settingsObject.keyRight = GLFW_KEY_D;
            this.settingsObject.keySneak = GLFW_KEY_LEFT_SHIFT;
            this.settingsObject.keyDrop = GLFW_KEY_Q;
            this.settingsObject.keyJump = GLFW_KEY_SPACE;
            this.settingsObject.keyInventory = GLFW_KEY_E;

            //save default values
            disk.saveSettingsToDisk(settingsObject);

        } else {
            //dump new settings in
            this.settingsObject = loadedSettings;
            //manually update vsync as it's loaded in
            window.setVSync(this.settingsObject.vSync);
        }
    }

    public void setChunk(Chunk chunk){
        if (this.chunk == null) {
            this.chunk = chunk;
        }
    }

    //debug info
    public boolean getDebugInfo(){
        return this.debugInfo;
    }

    public void setDebugInfo(boolean truth){
        debugInfo = truth;
    }

    //render distance
    public int getRenderDistance(){
        return this.settingsObject.renderDistance;
    }

    public void setRenderDistance(int newRenderDistance, boolean inGame){
        this.settingsObject.renderDistance = newRenderDistance;
        if (inGame) {
            chunk.generateNewChunks();
        }
    }

    public void setSettingsVsync(boolean truth){
        this.settingsObject.vSync = truth;
        window.setVSync(truth);
    }

    public boolean getSettingsVsync(){
        return this.settingsObject.vSync;
    }

    public void setGraphicsMode(boolean newGraphicsMode){
        this.settingsObject.graphicsMode = newGraphicsMode;
    }

    public boolean getGraphicsMode(){
        return this.settingsObject.graphicsMode;
    }

    public void setKeyForward(int newKey){
        this.settingsObject.keyForward = newKey;
    }

    public int getKeyForward(){
        return this.settingsObject.keyForward;
    }

    public void setKeyBack(int newKey){
        this.settingsObject.keyBack = newKey;
    }

    public int getKeyBack(){
        return this.settingsObject.keyBack;
    }

    public void setKeyLeft(int newKey){
        this.settingsObject.keyLeft = newKey;
    }

    public int getKeyLeft(){
        return this.settingsObject.keyLeft;
    }

    public void setKeyRight(int newKey){
        this.settingsObject.keyRight = newKey;
    }

    public int getKeyRight(){
        return this.settingsObject.keyRight;
    }

    public void setKeySneak(int newKey){
        this.settingsObject.keySneak = newKey;
    }

    public int getKeySneak(){
        return this.settingsObject.keySneak;
    }

    public void setKeyDrop(int newKey){
        this.settingsObject.keyDrop = newKey;
    }

    public int getKeyDrop(){
        return this.settingsObject.keyDrop;
    }

    public void setKeyJump(int newKey){
        this.settingsObject.keyJump = newKey;
    }

    public int getKeyJump(){
        return this.settingsObject.keyJump;
    }

    public void setKeyInventory(int newKey){
        this.settingsObject.keyInventory = newKey;
    }

    public int getKeyInventory(){
        return this.settingsObject.keyInventory;
    }

    public void saveSettings(){
        disk.saveSettingsToDisk(settingsObject);
    }
}
