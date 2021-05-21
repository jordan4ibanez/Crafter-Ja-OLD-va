package engine.settings;

import static engine.disk.Disk.loadSettingsFromDisk;
import static engine.disk.Disk.saveSettingsToDisk;

public class Settings {
    private static SettingsObject settings;

    //false for production (debugInfo)
    private static boolean debugInfo = false;
    private static boolean vSync;
    private static boolean graphicsMode; //true = fancy, false = fast
    private static boolean lazyChunkLoad;
    private static int renderDistance;

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

            lazyChunkLoad = false;
            settings.lazyChunkLoad = false;

            //save default values
            saveSettingsToDisk(settings);

        } else {

            //dump new settings in
            settings = loadedSettings;

            vSync = settings.vSync;
            graphicsMode = settings.graphicsMode;
            renderDistance = settings.renderDistance;
            lazyChunkLoad = settings.lazyChunkLoad;
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
    public static void setRenderDistance(int newRenderDistance){
        renderDistance = newRenderDistance;
    }

    //vsync
    public static void setSettingsVsync(boolean truth){
        vSync = true;
        settings.vSync = true;
    }
    public static boolean getSettingsVsync(){
        return vSync;
    }

    //lazy chunk loading
    public static void setSettingsLazyChunkLoad(boolean truth){
        lazyChunkLoad = truth;
        settings.lazyChunkLoad = truth;
    }
    public static boolean getSettingsLazyChunkLoad(){
        return lazyChunkLoad;
    }

    //graphics mode
    public static void setGraphicsMode(boolean newGraphicsMode){
        graphicsMode = newGraphicsMode;
    }
    public static boolean getGraphicsMode(){
        return graphicsMode;
    }
}
