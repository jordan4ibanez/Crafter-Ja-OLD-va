package engine;

import engine.graph.Mesh;
import engine.graph.Texture;
import game.item.Item;
import org.joml.Vector2d;
import org.joml.Vector3f;

import java.util.ArrayList;

import static engine.MouseInput.*;
import static engine.Renderer.getWindowScale;
import static engine.Renderer.getWindowSize;
import static engine.Window.*;
import static engine.sound.SoundAPI.playSound;
import static game.Crafter.getVersionName;
import static game.player.Inventory.*;
import static game.player.Player.*;
import static org.lwjgl.glfw.GLFW.*;

public class Hud {
    private final static float scale = 1f;

    private static final float FONT_WIDTH = 216f;
    private static final float LETTER_WIDTH = 6f;

    private static final float FONT_HEIGHT = 16f;
    private static final float LETTER_HEIGHT = 8f;

    private static Vector3f playerScale = new Vector3f(0.7f,0.8f,0.7f);
    private static Vector3f playerRot = new Vector3f(0,0,0);

    public static Vector3f getPlayerHudRotation(){
        return playerRot;
    }

    private static Vector3f versionInfoPos = new Vector3f(-5f,8f,-14f);
    private static Vector3f versionInfoShadowPos = new Vector3f(-4.9f,7.9f,-14f);

    private static boolean paused = false;

    public static boolean isPaused(){
        return paused;
    }

    public static void setPaused(boolean truth){
        paused = truth;
    }

    private static Texture fontTextureAtlas;
    private static Texture hotBar;
    private static Texture selection;
    private static Texture mainInventory;
    private static Texture worldSelection;
    private static Texture crossHair;
    private static Texture playerTexture;
    private static Texture inventorySlot;
    private static Texture inventorySlotSelected;
    private static Texture button;
    private static Texture buttonSelected;
    private static Texture buttonPushed;
    private static Texture menuBg;
    private static Texture miningCrack;


    private static Mesh thisHotBarMesh;
    private static Mesh thisSelectionMesh;
    private static Mesh thisInventoryMesh;
    private static Mesh thisWorldSelectionMesh;
    private static Mesh thisCrossHairMesh;
    private static Mesh playerMesh;
    private static Mesh versionInfoText;
    private static Mesh versionInfoTextShadow;
    private static Mesh inventorySelectionMesh;
    private static Mesh wieldHandMesh;
    private static Mesh inventorySlotMesh;
    private static Mesh inventorySlotSelectedMesh;
    private static Mesh buttonMesh;
    private static Mesh buttonSelectedMesh;
    private static Mesh buttonPushedMesh;
    private static Mesh menuBgMesh;
    private static Mesh continueMesh;
    private static Mesh toggleVsyncMesh;
    private static Mesh quitGameMesh;
    private static Mesh miningCrackMesh;


    public static void initializeHudAtlas() throws Exception {
        fontTextureAtlas = new Texture("textures/font.png");
        hotBar = new Texture("textures/hotbar.png");
        selection = new Texture("textures/hotbar_selected.png");
        mainInventory = new Texture("textures/inventory.png");
        worldSelection = new Texture("textures/selection.png");
        crossHair = new Texture("textures/crosshair.png");
        playerTexture = new Texture("textures/player.png");
        inventorySlot = new Texture("textures/inventory_slot.png");
        inventorySlotSelected = new Texture("textures/inventory_slot_selected.png");
        button = new Texture("textures/button.png");
        buttonSelected = new Texture("textures/button_selected.png");
        buttonPushed = new Texture("textures/button_pushed.png");
        menuBg = new Texture("textures/menu_bg.png");
        miningCrack = new Texture("textures/crack_anylength.png");
    }

    public static Mesh getHotBarMesh(){
        return thisHotBarMesh;
    }

    public static Mesh getSelectionMesh(){
        return thisSelectionMesh;
    }

    public static Mesh getInventoryMesh(){
        return thisInventoryMesh;
    }

    public static Mesh getWorldSelectionMesh(){
        return thisWorldSelectionMesh;
    }

    public static Mesh getCrossHairMesh(){
        return thisCrossHairMesh;
    }

    public static Mesh getPlayerMesh(){
        return playerMesh;
    }

    public static Vector3f getPlayerHudRot(){
        return playerRot;
    }

    public static Vector3f getPlayerHudScale(){
        return playerScale;
    }

    public static Mesh getVersionInfoText(){
        return versionInfoText;
    }

    public static Mesh getVersionInfoTextShadow(){
        return versionInfoTextShadow;
    }

    public static Vector3f getVersionInfoPos(){
        return versionInfoPos;
    }

    public static Vector3f getVersionInfoShadowPos(){
        return versionInfoShadowPos;
    }

    public static Mesh getInventorySelectionMesh(){
        return inventorySelectionMesh;
    }

    public static Mesh getWieldHandMesh(){
        return wieldHandMesh;
    }

    public static Mesh getButtonMesh(){
        return buttonMesh;
    }
    public static Mesh getButtonSelectedMesh(){
        return buttonSelectedMesh;
    }
    public static Mesh getButtonPushedMesh(){
        return buttonPushedMesh;
    }
    public static Mesh getMenuBgMesh(){
        return menuBgMesh;
    }

    public static Mesh getContinueMesh(){
        return continueMesh;
    }
    public static Mesh getToggleVsyncMesh(){
        return toggleVsyncMesh;
    }
    public static Mesh getQuitGameMesh(){
        return quitGameMesh;
    }

    public static Mesh getInventorySlotMesh(){
        return inventorySlotMesh;
    }
    public static Mesh getInventorySlotSelectedMesh(){
        return inventorySlotSelectedMesh;
    }

    public static Mesh getMiningCrackMesh(){
        return miningCrackMesh;
    }

    public static void createHud(){
        createDebugHotbar();
        createInventory();
        createSelection();
        createWorldSelectionMesh();
        createCrossHair();
        versionInfoText = createCustomHudText(getVersionName(), 1,1,1);
        versionInfoTextShadow = createCustomHudText(getVersionName(), 0,0,0);
        createPlayerMesh();
        createInventorySelection();
        createWieldHandMesh();
        createInventorySlot();
        createInventorySlotSelected();
        createMenuBg();
        createButtons();
        rebuildMiningMesh(0);

        continueMesh = createCustomHudText("CONTINUE", 1,1,1);
        toggleVsyncMesh = createCustomHudText("VSYNC:ON", 1,1,1);
        quitGameMesh = createCustomHudText("QUIT", 1,1,1);
    }

    public static void toggleVsyncMesh(){
        if (isvSync()) {
            toggleVsyncMesh = createCustomHudText("VSYNC:ON", 1, 1, 1);
            System.out.println("vsync on");
        } else {
            toggleVsyncMesh = createCustomHudText("VSYNC:OFF", 1, 1, 1);
            System.out.println("vsync off");
        }
    }

    private static void createDebugHotbar(){
        ArrayList positions = new ArrayList();
        ArrayList textureCoord = new ArrayList();
        ArrayList indices = new ArrayList();
        ArrayList light = new ArrayList();


        int indicesCount = 0;

        //front
        positions.add(0.5f);
        positions.add(0.06043956043f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(0.06043956043f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(-0.06043956043f);
        positions.add(0f);

        positions.add(0.5f);
        positions.add(-0.06043956043f);
        positions.add(0f);
        //front
        float frontLight = 1f;//getLight(x, y, z + 1, chunkX, chunkZ) / maxLight;

        //front
        for (int i = 0; i < 12; i++) {
            light.add(frontLight);
        }
        //front
        indices.add(0 + indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(0 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);

        indicesCount += 4;

        //-x +x   -y +y
        // 0  1    2  3

        //front
        textureCoord.add(1f);//1
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(1f);//3
        textureCoord.add(1f);//1
        textureCoord.add(1f);//3


        //convert the position objects into usable array
        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i] = (float) positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = (float) light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = (int) indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = (float) textureCoord.get(i);
        }

        thisHotBarMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, hotBar);
    }

    private static void createSelection(){
        ArrayList positions = new ArrayList();
        ArrayList textureCoord = new ArrayList();
        ArrayList indices = new ArrayList();
        ArrayList light = new ArrayList();


        int indicesCount = 0;


        //front
        positions.add(0.5f);
        positions.add(0.5f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(0.5f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(-0.5f);
        positions.add(0f);

        positions.add(0.5f);
        positions.add(-0.5f);
        positions.add(0f);
        //front
        float frontLight = 1f;//getLight(x, y, z + 1, chunkX, chunkZ) / maxLight;

        //front
        for (int i = 0; i < 12; i++) {
            light.add(frontLight);
        }
        //front
        indices.add(0 + indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(0 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);

        indicesCount += 4;

        //-x +x   -y +y
        // 0  1    2  3

        //front
        textureCoord.add(1f);//1
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(1f);//3
        textureCoord.add(1f);//1
        textureCoord.add(1f);//3


        //convert the position objects into usable array
        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i] = (float) positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = (float) light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = (int) indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = (float) textureCoord.get(i);
        }
        thisSelectionMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, selection);
    }

    private static void createMenuBg(){
        ArrayList positions = new ArrayList();
        ArrayList textureCoord = new ArrayList();
        ArrayList indices = new ArrayList();
        ArrayList light = new ArrayList();


        int indicesCount = 0;


        //front
        positions.add(0.5f);
        positions.add(0.5f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(0.5f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(-0.5f);
        positions.add(0f);

        positions.add(0.5f);
        positions.add(-0.5f);
        positions.add(0f);
        //front
        float frontLight = 1f;//getLight(x, y, z + 1, chunkX, chunkZ) / maxLight;

        //front
        for (int i = 0; i < 12; i++) {
            light.add(frontLight);
        }
        //front
        indices.add(0 + indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(0 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);

        indicesCount += 4;

        //-x +x   -y +y
        // 0  1    2  3

        //front
        textureCoord.add(1f);//1
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(1f);//3
        textureCoord.add(1f);//1
        textureCoord.add(1f);//3


        //convert the position objects into usable array
        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i] = (float) positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = (float) light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = (int) indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = (float) textureCoord.get(i);
        }
        menuBgMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, menuBg);
    }

    private static void createButtons(){
        ArrayList positions = new ArrayList();
        ArrayList textureCoord = new ArrayList();
        ArrayList indices = new ArrayList();
        ArrayList light = new ArrayList();


        int indicesCount = 0;


        //front
        positions.add(0.5f);
        positions.add(0.125f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(0.125f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(-0.125f);
        positions.add(0f);

        positions.add(0.5f);
        positions.add(-0.125f);
        positions.add(0f);
        //front
        float frontLight = 1f;//getLight(x, y, z + 1, chunkX, chunkZ) / maxLight;

        //front
        for (int i = 0; i < 12; i++) {
            light.add(frontLight);
        }
        //front
        indices.add(0 + indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(0 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);

        indicesCount += 4;

        //-x +x   -y +y
        // 0  1    2  3

        //front
        textureCoord.add(1f);//1
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(1f);//3
        textureCoord.add(1f);//1
        textureCoord.add(1f);//3


        //convert the position objects into usable array
        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i] = (float) positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = (float) light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = (int) indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = (float) textureCoord.get(i);
        }
        buttonMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, button);
        buttonSelectedMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, buttonSelected);
        buttonPushedMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, buttonPushed);
    }

    private static void createInventorySlot(){
        ArrayList positions = new ArrayList();
        ArrayList textureCoord = new ArrayList();
        ArrayList indices = new ArrayList();
        ArrayList light = new ArrayList();

        int indicesCount = 0;

        //front
        positions.add(0.5f);
        positions.add(0.5f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(0.5f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(-0.5f);
        positions.add(0f);

        positions.add(0.5f);
        positions.add(-0.5f);
        positions.add(0f);
        //front
        float frontLight = 1f;//getLight(x, y, z + 1, chunkX, chunkZ) / maxLight;

        //front
        for (int i = 0; i < 12; i++) {
            light.add(frontLight);
        }
        //front
        indices.add(0 + indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(0 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);

        indicesCount += 4;

        //-x +x   -y +y
        // 0  1    2  3

        //front
        textureCoord.add(1f);//1
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(1f);//3
        textureCoord.add(1f);//1
        textureCoord.add(1f);//3


        //convert the position objects into usable array
        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i] = (float) positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = (float) light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = (int) indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = (float) textureCoord.get(i);
        }

        inventorySlotMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, inventorySlot);
    }

    private static void createInventorySlotSelected(){
        ArrayList positions = new ArrayList();
        ArrayList textureCoord = new ArrayList();
        ArrayList indices = new ArrayList();
        ArrayList light = new ArrayList();

        int indicesCount = 0;

        //front
        positions.add(0.5f);
        positions.add(0.5f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(0.5f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(-0.5f);
        positions.add(0f);

        positions.add(0.5f);
        positions.add(-0.5f);
        positions.add(0f);
        //front
        float frontLight = 1f;//getLight(x, y, z + 1, chunkX, chunkZ) / maxLight;

        //front
        for (int i = 0; i < 12; i++) {
            light.add(frontLight);
        }
        //front
        indices.add(0 + indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(0 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);

        indicesCount += 4;

        //-x +x   -y +y
        // 0  1    2  3

        //front
        textureCoord.add(1f);//1
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(1f);//3
        textureCoord.add(1f);//1
        textureCoord.add(1f);//3


        //convert the position objects into usable array
        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i] = (float) positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = (float) light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = (int) indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = (float) textureCoord.get(i);
        }

        inventorySlotSelectedMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, inventorySlotSelected);
    }

    private static void createInventory(){
        ArrayList positions = new ArrayList();
        ArrayList textureCoord = new ArrayList();
        ArrayList indices = new ArrayList();
        ArrayList light = new ArrayList();

        int indicesCount = 0;

        //front
        positions.add(0.5f);
        positions.add(0.46335697399f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(0.46335697399f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(-0.46335697399f);
        positions.add(0f);

        positions.add(0.5f);
        positions.add(-0.46335697399f);
        positions.add(0f);
        //front
        float frontLight = 1f;//getLight(x, y, z + 1, chunkX, chunkZ) / maxLight;

        //front
        for (int i = 0; i < 12; i++) {
            light.add(frontLight);
        }
        //front
        indices.add(0 + indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(0 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);

        indicesCount += 4;

        //-x +x   -y +y
        // 0  1    2  3

        //front
        textureCoord.add(1f);//1
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(1f);//3
        textureCoord.add(1f);//1
        textureCoord.add(1f);//3


        //convert the position objects into usable array
        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i] = (float) positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = (float) light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = (int) indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = (float) textureCoord.get(i);
        }

        thisInventoryMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, mainInventory);
    }

    public static void rebuildMiningMesh(int level) {

        float min = -0.0001f;
        float max = 1.0001f;
        int indicesCount = 0;

        ArrayList positions = new ArrayList();
        ArrayList textureCoord = new ArrayList();
        ArrayList indices = new ArrayList();
        ArrayList light = new ArrayList();

        float maxLevels = 9;

        float textureMin = (float)level/maxLevels;
        float textureMax = (float)(level+1)/maxLevels;

        //front
        positions.add(max);
        positions.add(max);
        positions.add(max);

        positions.add(min);
        positions.add(max);
        positions.add(max);

        positions.add(min);
        positions.add(min);
        positions.add(max);

        positions.add(max);
        positions.add(min);
        positions.add(max);

        //front
        float frontLight = 1f;

        //front
        for (int i = 0; i < 12; i++) {
            light.add(frontLight);
        }
        //front
        indices.add(0 + indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(0 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);
        indicesCount += 4;



        //-x +x  -y +y
        // 0  1   2  3
        //front
        textureCoord.add(1f);//1
        textureCoord.add(textureMin);//2
        textureCoord.add(0f);//0
        textureCoord.add(textureMin);//2
        textureCoord.add(0f);//0
        textureCoord.add(textureMax);//3
        textureCoord.add(1f);//1
        textureCoord.add(textureMax);//3


        //todo///////////////////////////////////////////////////////

        //back
        positions.add(min);
        positions.add(max);
        positions.add(min);

        positions.add(max);
        positions.add(max);
        positions.add(min);

        positions.add(max);
        positions.add(min);
        positions.add(min);

        positions.add(min);
        positions.add(min);
        positions.add(min);
        //back
        float backLight = 1f;
        //back
        for (int i = 0; i < 12; i++) {
            light.add(backLight);
        }
        //back
        indices.add(0 + indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(0 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);
        indicesCount += 4;


        //-x +x  -y +y
        // 0  1   2  3
        //back
        textureCoord.add(1f);//1
        textureCoord.add(textureMin);//2
        textureCoord.add(0f);//0
        textureCoord.add(textureMin);//2
        textureCoord.add(0f);//0
        textureCoord.add(textureMax);//3
        textureCoord.add(1f);//1
        textureCoord.add(textureMax);//3


        //todo///////////////////////////////////////////////////////

        //right
        positions.add(max);
        positions.add(max);
        positions.add(min);

        positions.add(max);
        positions.add(max);
        positions.add(max);

        positions.add(max);
        positions.add(min);
        positions.add(max);

        positions.add(max);
        positions.add(min);
        positions.add(min);
        //right
        float rightLight = 1f;

        //right
        for (int i = 0; i < 12; i++) {
            light.add(rightLight);
        }
        //right
        indices.add(0 + indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(0 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);
        indicesCount += 4;


        // 0  1   0  1
        // 0  1   2  3
        //right
        textureCoord.add(1f);//1
        textureCoord.add(textureMin);//2
        textureCoord.add(0f);//0
        textureCoord.add(textureMin);//2
        textureCoord.add(0f);//0
        textureCoord.add(textureMax);//3
        textureCoord.add(1f);//1
        textureCoord.add(textureMax);//3


        //todo///////////////////////////////////////////////////////

        //left
        positions.add(min);
        positions.add(max);
        positions.add(max);

        positions.add(min);
        positions.add(max);
        positions.add(min);

        positions.add(min);
        positions.add(min);
        positions.add(min);

        positions.add(min);
        positions.add(min);
        positions.add(max);
        //left
        float leftLight = 1f;
        //left
        for (int i = 0; i < 12; i++) {
            light.add(leftLight);
        }
        //left
        indices.add(0 + indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(0 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);
        indicesCount += 4;


        //-x +x  -y +y
        // 0  1   2  3
        //left
        textureCoord.add(1f);//1
        textureCoord.add(textureMin);//2
        textureCoord.add(0f);//0
        textureCoord.add(textureMin);//2
        textureCoord.add(0f);//0
        textureCoord.add(textureMax);//3
        textureCoord.add(1f);//1
        textureCoord.add(textureMax);//3


        //todo///////////////////////////////////////////////////////

        //top
        positions.add(min);
        positions.add(max);
        positions.add(min);

        positions.add(min);
        positions.add(max);
        positions.add(max);

        positions.add(max);
        positions.add(max);
        positions.add(max);

        positions.add(max);
        positions.add(max);
        positions.add(min);
        //top
        float topLight = 1f;
        //top
        for (int i = 0; i < 12; i++) {
            light.add(topLight);
        }
        //top
        indices.add(0 + indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(0 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);
        indicesCount += 4;


        //-x +x  -y +y
        // 0  1   2  3
        //top
        textureCoord.add(1f);//1
        textureCoord.add(textureMin);//2
        textureCoord.add(0f);//0
        textureCoord.add(textureMin);//2
        textureCoord.add(0f);//0
        textureCoord.add(textureMax);//3
        textureCoord.add(1f);//1
        textureCoord.add(textureMax);//3


        //todo///////////////////////////////////////////////////////

        //bottom
        positions.add(min);
        positions.add(min);
        positions.add(max);

        positions.add(min);
        positions.add(min);
        positions.add(min);

        positions.add(max);
        positions.add(min);
        positions.add(min);

        positions.add(max);
        positions.add(min);
        positions.add(max);
        //bottom
        float bottomLight = 1f;

        //bottom
        for (int i = 0; i < 12; i++) {
            light.add(bottomLight);
        }
        //bottom
        indices.add(0 + indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(0 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);
        indicesCount += 4;


        //-x +x  -y +y
        // 0  1   2  3
        //bottom
        textureCoord.add(1f);//1
        textureCoord.add(textureMin);//2
        textureCoord.add(0f);//0
        textureCoord.add(textureMin);//2
        textureCoord.add(0f);//0
        textureCoord.add(textureMax);//3
        textureCoord.add(1f);//1
        textureCoord.add(textureMax);//3

        //convert the position objects into usable array
        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i] = (float) positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = (float) light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = (int) indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = (float) textureCoord.get(i);
        }

        miningCrackMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, miningCrack);
    }

    private static void createWorldSelectionMesh() {

        ArrayList positions = new ArrayList();
        ArrayList textureCoord = new ArrayList();
        ArrayList indices = new ArrayList();
        ArrayList light = new ArrayList();

        float sizeOffset = 0.0025f;
        float min = -sizeOffset;
        float max = 1.f + sizeOffset;
        float width = 0.004f;
        float overshoot = sizeOffset + width;

        int indicesCount = 0;

        Vector3f[][] lines = new Vector3f[][]{
                //lower left
                {new Vector3f(min-width,min-width,0f-overshoot), new Vector3f(min+width,min+width,1f+overshoot)},
                //lower right
                {new Vector3f(max-width,min-width,0f-overshoot), new Vector3f(max+width,min+width,1f+overshoot)},

                //upper left
                {new Vector3f(min-width,max-width,0f-overshoot), new Vector3f(min+width,max+width,1f+overshoot)},
                //upper right
                {new Vector3f(max-width,max-width,0f-overshoot), new Vector3f(max+width,max+width,1f+overshoot)},

                //lower front
                {new Vector3f(0f-overshoot,min-width,min-width), new Vector3f(1f+overshoot,min+width,min+width)},
                //lower back
                {new Vector3f(0f-overshoot,min-width,max-width), new Vector3f(1f+overshoot,min+width,max+width)},

                //upper front
                {new Vector3f(0-overshoot,max-width,min-width), new Vector3f(1+overshoot,max+width,min+width)},
                //upper back
                {new Vector3f(0-overshoot,max-width,max-width), new Vector3f(1+overshoot,max+width,max+width)},

                //front left column
                {new Vector3f(min-width,0-overshoot,min-width), new Vector3f(min+width,1+overshoot, min+width)},
                //front right column
                {new Vector3f(max-width,0-overshoot,min-width), new Vector3f(max+width,1+overshoot, min+width)},

                //back left column
                {new Vector3f(min-width,0-overshoot,max-width), new Vector3f(min+width,1+overshoot, max+width)},
                //back right column
                {new Vector3f(max-width,0-overshoot,max-width), new Vector3f(max+width,1+overshoot, max+width)},

        };

        for (Vector3f[] thisArray : lines) {
            //front
            positions.add(thisArray[1].x);
            positions.add(thisArray[1].y);
            positions.add(thisArray[1].z);

            positions.add(thisArray[0].x);
            positions.add(thisArray[1].y);
            positions.add(thisArray[1].z);

            positions.add(thisArray[0].x);
            positions.add(thisArray[0].y);
            positions.add(thisArray[1].z);

            positions.add(thisArray[1].x);
            positions.add(thisArray[0].y);
            positions.add(thisArray[1].z);

            //front
            float frontLight = 1f;

            //front
            for (int i = 0; i < 12; i++) {
                light.add(frontLight);
            }
            //front
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;


            //-x +x  -y +y
            // 0  1   2  3
            //front
            textureCoord.add(1f);//1
            textureCoord.add(0f);//2
            textureCoord.add(0f);//0
            textureCoord.add(0f);//2
            textureCoord.add(0f);//0
            textureCoord.add(1f);//3
            textureCoord.add(1f);//1
            textureCoord.add(1f);//3


            //todo///////////////////////////////////////////////////////

            //back
            positions.add(thisArray[0].x);
            positions.add(thisArray[1].y);
            positions.add(thisArray[0].z);

            positions.add(thisArray[1].x);
            positions.add(thisArray[1].y);
            positions.add(thisArray[0].z);

            positions.add(thisArray[1].x);
            positions.add(thisArray[0].y);
            positions.add(thisArray[0].z);

            positions.add(thisArray[0].x);
            positions.add(thisArray[0].y);
            positions.add(thisArray[0].z);
            //back
            float backLight = 2f;
            //back
            for (int i = 0; i < 12; i++) {
                light.add(backLight);
            }
            //back
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;


            //-x +x  -y +y
            // 0  1   2  3
            //back
            textureCoord.add(1f);//1
            textureCoord.add(0f);//2
            textureCoord.add(0f);//0
            textureCoord.add(0f);//2
            textureCoord.add(0f);//0
            textureCoord.add(1f);//3
            textureCoord.add(1f);//1
            textureCoord.add(1f);//3


            //todo///////////////////////////////////////////////////////

            //right
            positions.add(thisArray[1].x);
            positions.add(thisArray[1].y);
            positions.add(thisArray[0].z);

            positions.add(thisArray[1].x);
            positions.add(thisArray[1].y);
            positions.add(thisArray[1].z);

            positions.add(thisArray[1].x);
            positions.add(thisArray[0].y);
            positions.add(thisArray[1].z);

            positions.add(thisArray[1].x);
            positions.add(thisArray[0].y);
            positions.add(thisArray[0].z);
            //right
            float rightLight = 1f;

            //right
            for (int i = 0; i < 12; i++) {
                light.add(rightLight);
            }
            //right
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;


            // 0  1   0  1
            // 0  1   2  3
            //right
            textureCoord.add(1f);//1
            textureCoord.add(0f);//2
            textureCoord.add(0f);//0
            textureCoord.add(0f);//2
            textureCoord.add(0f);//0
            textureCoord.add(1f);//3
            textureCoord.add(1f);//1
            textureCoord.add(1f);//3


            //todo///////////////////////////////////////////////////////

            //left
            positions.add(thisArray[0].x);
            positions.add(thisArray[1].y);
            positions.add(thisArray[1].z);

            positions.add(thisArray[0].x);
            positions.add(thisArray[1].y);
            positions.add(thisArray[0].z);

            positions.add(thisArray[0].x);
            positions.add(thisArray[0].y);
            positions.add(thisArray[0].z);

            positions.add(thisArray[0].x);
            positions.add(thisArray[0].y);
            positions.add(thisArray[1].z);
            //left
            float leftLight = 1f;
            //left
            for (int i = 0; i < 12; i++) {
                light.add(leftLight);
            }
            //left
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;


            //-x +x  -y +y
            // 0  1   2  3
            //left
            textureCoord.add(1f);//1
            textureCoord.add(0f);//2
            textureCoord.add(0f);//0
            textureCoord.add(0f);//2
            textureCoord.add(0f);//0
            textureCoord.add(1f);//3
            textureCoord.add(1f);//1
            textureCoord.add(1f);//3


            //todo///////////////////////////////////////////////////////

            //top
            positions.add(thisArray[0].x);
            positions.add(thisArray[1].y);
            positions.add(thisArray[0].z);

            positions.add(thisArray[0].x);
            positions.add(thisArray[1].y);
            positions.add(thisArray[1].z);

            positions.add(thisArray[1].x);
            positions.add(thisArray[1].y);
            positions.add(thisArray[1].z);

            positions.add(thisArray[1].x);
            positions.add(thisArray[1].y);
            positions.add(thisArray[0].z);
            //top
            float topLight = 1f;
            //top
            for (int i = 0; i < 12; i++) {
                light.add(topLight);
            }
            //top
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;


            //-x +x  -y +y
            // 0  1   2  3
            //top
            textureCoord.add(1f);//1
            textureCoord.add(0f);//2
            textureCoord.add(0f);//0
            textureCoord.add(0f);//2
            textureCoord.add(0f);//0
            textureCoord.add(1f);//3
            textureCoord.add(1f);//1
            textureCoord.add(1f);//3


            //todo///////////////////////////////////////////////////////

            //min = thisArray[0]
            //max = thisArray[1]

            //bottom
            positions.add(thisArray[0].x);
            positions.add(thisArray[0].y);
            positions.add(thisArray[1].z);

            positions.add(thisArray[0].x);
            positions.add(thisArray[0].y);
            positions.add(thisArray[0].z);

            positions.add(thisArray[1].x);
            positions.add(thisArray[0].y);
            positions.add(thisArray[0].z);

            positions.add(thisArray[1].x);
            positions.add(thisArray[0].y);
            positions.add(thisArray[1].z);
            //bottom
            float bottomLight = 1f;

            //bottom
            for (int i = 0; i < 12; i++) {
                light.add(bottomLight);
            }
            //bottom
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;


            //-x +x  -y +y
            // 0  1   2  3
            //bottom
            textureCoord.add(1f);//1
            textureCoord.add(0f);//2
            textureCoord.add(0f);//0
            textureCoord.add(0f);//2
            textureCoord.add(0f);//0
            textureCoord.add(1f);//3
            textureCoord.add(1f);//1
            textureCoord.add(1f);//3


        }

        //convert the position objects into usable array
        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i] = (float) positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = (float) light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = (int) indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = (float) textureCoord.get(i);
        }

        thisWorldSelectionMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, worldSelection);
    }

    private static void createCrossHair(){
        ArrayList positions = new ArrayList();
        ArrayList textureCoord = new ArrayList();
        ArrayList indices = new ArrayList();
        ArrayList light = new ArrayList();


        int indicesCount = 0;


        //front
        positions.add(0.5f);
        positions.add(0.5f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(0.5f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(-0.5f);
        positions.add(0f);

        positions.add(0.5f);
        positions.add(-0.5f);
        positions.add(0f);
        //front
        float frontLight = 1f;//getLight(x, y, z + 1, chunkX, chunkZ) / maxLight;

        //front
        for (int i = 0; i < 12; i++) {
            light.add(frontLight);
        }
        //front
        indices.add(0 + indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(0 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);

        indicesCount += 4;

        //-x +x   -y +y
        // 0  1    2  3

        //front
        textureCoord.add(1f);//1
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(1f);//3
        textureCoord.add(1f);//1
        textureCoord.add(1f);//3


        //convert the position objects into usable array
        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i] = (float) positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = (float) light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = (int) indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = (float) textureCoord.get(i);
        }

        thisCrossHairMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, crossHair);
    }

    private static void createInventorySelection(){
        ArrayList positions = new ArrayList();
        ArrayList textureCoord = new ArrayList();
        ArrayList indices = new ArrayList();
        ArrayList light = new ArrayList();


        int indicesCount = 0;
        float thisTest = -14f;

        //front
        positions.add(scale);
        positions.add(scale);
        positions.add(thisTest); //z (how close it is to screen)

        positions.add(-scale);
        positions.add(scale);
        positions.add(thisTest);

        positions.add(-scale);
        positions.add(-scale);
        positions.add(thisTest);

        positions.add(scale);
        positions.add(-scale);
        positions.add(thisTest);
        //front
        float frontLight = 1f;//getLight(x, y, z + 1, chunkX, chunkZ) / maxLight;

        //front
        for (int i = 0; i < 12; i++) {
            light.add(frontLight);
        }
        //front
        indices.add(0 + indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(0 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);

        indicesCount += 4;

        //-x +x   -y +y
        // 0  1    2  3

        //front
        textureCoord.add(1f);//1
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(1f);//3
        textureCoord.add(1f);//1
        textureCoord.add(1f);//3


        //convert the position objects into usable array
        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i] = (float) positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = (float) light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = (int) indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = (float) textureCoord.get(i);
        }

        inventorySelectionMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, selection);
    }


    private static final float PLAYER_WIDTH = 64f;
    private static final float PLAYER_HEIGHT = 32f;

    private static float[] calculateTexture(int xMin, int yMin, int xMax, int yMax){
        float[] texturePoints = new float[4];

        texturePoints[0] = (float)xMin/PLAYER_WIDTH; //min x (-)
        texturePoints[1] = (float)xMax/PLAYER_WIDTH; //max x (+)

        texturePoints[2] = (float)yMin/PLAYER_HEIGHT; //min y (-)
        texturePoints[3] = (float)yMax/PLAYER_HEIGHT; //max y (+)
        return texturePoints;
    }

    private static void createWieldHandMesh(){
        float[][] oneBlockyBoi = new float[][]{
//                //right arm
                {   -0.45f,
                        -2.25f,
                        -0.45f,

                        0.45f,
                        0.5f ,
                        0.45f,

                },
        };

        float[][] textureArrayArray = new float[][]{
                //right arm
                //front
                calculateTexture(44, 20, 48, 32), //light
                //back
                calculateTexture(48, 20, 52, 32), //dark
                //right
                calculateTexture(48, 20, 52, 32), //dark
                //left
                calculateTexture(44, 20, 48, 32), //light
                //top
                calculateTexture(44, 16, 48, 20), //shoulder
                //bottom
                calculateTexture(48, 16, 52, 20), //palm
        };


        ArrayList positions = new ArrayList();
        ArrayList textureCoord = new ArrayList();
        ArrayList indices = new ArrayList();
        ArrayList light = new ArrayList();

        int indicesCount = 0;
        int textureCounter = 0;
        for (float[] thisBlockBox : oneBlockyBoi) {
            // 0, 1, 2, 3, 4, 5
            //-x,-y,-z, x, y, z
            // 0, 0, 0, 1, 1, 1

            //front
            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[5]);
            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[5]);
            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[5]);
            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[5]);
            //front
            float frontLight = 1f;

            //front
            for (int i = 0; i < 12; i++) {
                light.add(frontLight);
            }
            //front
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);

            indicesCount += 4;

            // 0, 1,  2, 3
            //-x,+x, -y,+y

            float[] textureFront = textureArrayArray[textureCounter];

            //front
            textureCoord.add(textureFront[1]); //x positive
            textureCoord.add(textureFront[2]); //y positive
            textureCoord.add(textureFront[0]); //x negative
            textureCoord.add(textureFront[2]); //y positive

            textureCoord.add(textureFront[0]); //x negative
            textureCoord.add(textureFront[3]);   //y negative
            textureCoord.add(textureFront[1]); //x positive
            textureCoord.add(textureFront[3]);   //y negative


            textureCounter++;



            //back
            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[2]);

            //back
            float backLight = 1f;

            //back
            for (int i = 0; i < 12; i++) {
                light.add(backLight);
            }
            //back
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;

            float[] textureBack = textureArrayArray[textureCounter];

            // 0, 1, 2, 3, 4, 5
            //-x,-y,-z, x, y, z
            // 0, 0, 0, 1, 1, 1

            // 0, 1,  2, 3
            //-x,+x, -y,+y


            //back
            textureCoord.add(textureBack[1]);
            textureCoord.add(textureBack[2]);
            textureCoord.add(textureBack[0]);
            textureCoord.add(textureBack[2]);

            textureCoord.add(textureBack[0]);
            textureCoord.add(textureBack[3]);
            textureCoord.add(textureBack[1]);
            textureCoord.add(textureBack[3]);

            textureCounter++;



            //right
            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[5]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[5]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[2]);
            //right
            float rightLight = 1f;

            //right
            for (int i = 0; i < 12; i++) {
                light.add(rightLight);
            }
            //right
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;


            // 0, 1, 2, 3, 4, 5
            //-x,-y,-z, x, y, z
            // 0, 0, 0, 1, 1, 1

            // 0, 1,  2, 3
            //-x,+x, -y,+y


            float[] textureRight = textureArrayArray[textureCounter];
            //right
            textureCoord.add(textureRight[1]);
            textureCoord.add(textureRight[2]);
            textureCoord.add(textureRight[0]);
            textureCoord.add(textureRight[2]);

            textureCoord.add(textureRight[0]);
            textureCoord.add(textureRight[3]);
            textureCoord.add(textureRight[1]);
            textureCoord.add(textureRight[3]);

            textureCounter++;


            //left
            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[5]);

            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[5]);

            //left
            float leftLight = 1f;

            //left
            for (int i = 0; i < 12; i++) {
                light.add(leftLight);
            }
            //left
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;

            float[] textureLeft = textureArrayArray[textureCounter];
            //left
            textureCoord.add(textureLeft[1]);
            textureCoord.add(textureLeft[2]);
            textureCoord.add(textureLeft[0]);
            textureCoord.add(textureLeft[2]);

            textureCoord.add(textureLeft[0]);
            textureCoord.add(textureLeft[3]);
            textureCoord.add(textureLeft[1]);
            textureCoord.add(textureLeft[3]);

            textureCounter++;


            //top
            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[5]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[5]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[2]);
            //top
            float topLight = 1f;

            //top
            for (int i = 0; i < 12; i++) {
                light.add(topLight);
            }
            //top
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;

            // 0, 1, 2, 3, 4, 5
            //-x,-y,-z, x, y, z
            // 0, 0, 0, 1, 1, 1

            // 0, 1,  2, 3
            //-x,+x, -y,+y

            float[] textureTop = textureArrayArray[textureCounter];
            //top
            textureCoord.add(textureTop[1]);
            textureCoord.add(textureTop[2]);
            textureCoord.add(textureTop[0]);
            textureCoord.add(textureTop[2]);

            textureCoord.add(textureTop[0]);
            textureCoord.add(textureTop[3]);
            textureCoord.add(textureTop[1]);
            textureCoord.add(textureTop[3]);

            textureCounter++;


            //bottom
            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[5]);

            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[5]);
            //bottom
            float bottomLight = 1f;

            //bottom
            for (int i = 0; i < 12; i++) {
                light.add(bottomLight);
            }
            //bottom
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;


            // 0, 1, 2, 3, 4, 5
            //-x,-y,-z, x, y, z
            // 0, 0, 0, 1, 1, 1

            // 0, 1,  2, 3
            //-x,+x, -y,+y

            float[] textureBottom = textureArrayArray[textureCounter];
            //bottom
            textureCoord.add(textureBottom[1]);
            textureCoord.add(textureBottom[2]);
            textureCoord.add(textureBottom[0]);
            textureCoord.add(textureBottom[2]);

            textureCoord.add(textureBottom[0]);
            textureCoord.add(textureBottom[3]);
            textureCoord.add(textureBottom[1]);
            textureCoord.add(textureBottom[3]);

            textureCounter++;
        }

        //convert the position objects into usable array
        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i] = (float) positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = (float) light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = (int) indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = (float) textureCoord.get(i);
        }

        wieldHandMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, playerTexture);
    }


    private static void createPlayerMesh(){
        float[][] oneBlockyBoi = new float[][]{
//                head
                {-0.75f,-0.5f,-0.75f,0.75f,1.0f,0.75f},
////                body
                {-0.75f,-2.5f,-0.45f,0.75f,-0.5f,0.45f},
//                //right arm
                {0.75f,-2.75f,-0.45f, 1.65f,-0.5f,0.45f},
//                //left arm
                {-1.65f,-2.75f,-0.45f, -0.75f,-0.5f,0.45f},

                //right leg
                {-0.75f,-5.0f,-0.375f, 0.0f,-2.5f,0.375f},

                //left leg
                {0.0f,-5.0f,-0.375f,  0.75f,-2.5f,0.375f},
        };

        float[][] textureArrayArray = new float[][]{
                //head
                //front
                calculateTexture(8,8,16,16),
                //back
                calculateTexture(24,8,32,16),
                //right
                calculateTexture(16,8,24,16),
                //left
                calculateTexture(0,8,8,16),
                //top
                calculateTexture(8,0,16,8),
                //bottom
                calculateTexture(16,0,24,8),

                //body
                //front
                calculateTexture(20,20,28,30),
                //back
                calculateTexture(32,20,40,30),
                //right
                calculateTexture(28,20,32,30),
                //left
                calculateTexture(16,20,20,30),
                //top
                calculateTexture(20,16,28,20),
                //bottom
                calculateTexture(28,16,36,20),


                //right arm
                //front
                calculateTexture(44,20,48,32), //light
                //back
                calculateTexture(48,20,52,32), //dark
                //right
                calculateTexture(48,20,52,32), //dark
                //left
                calculateTexture(44,20,48,32), //light
                //top
                calculateTexture(44,16,48,20), //shoulder
                //bottom
                calculateTexture(48,16,52,20), //palm

                //left arm
                //front
                calculateTexture(44,20,48,32), //light
                //back
                calculateTexture(48,20,52,32), //dark
                //right
                calculateTexture(44,20,48,32), //light
                //left
                calculateTexture(48,20,52,32), //dark
                //top
                calculateTexture(44,16,48,20), //shoulder
                //bottom
                calculateTexture(48,16,52,20), //palm


                //right leg
                //front
                calculateTexture(4,20,8,32), //light
                //back
                calculateTexture(0,20,4,32), //dark
                //right
                calculateTexture(8,20,12,32), //dark
                //left
                calculateTexture(12,20,16,32), //light
                //top
                calculateTexture(4,16,8,20), //top
                //bottom
                calculateTexture(8,16,12,20), //bottom

                //left leg
                //front
                calculateTexture(4,20,8,32), //light
                //back
                calculateTexture(0,20,4,32), //dark
                //right
                calculateTexture(12,20,16,32), //light
                //left
                calculateTexture(8,20,12,32), //dark
                //top
                calculateTexture(4,16,8,20), //top
                //bottom
                calculateTexture(8,16,12,20), //bottom

        };

        ArrayList positions = new ArrayList();
        ArrayList textureCoord = new ArrayList();
        ArrayList indices = new ArrayList();
        ArrayList light = new ArrayList();

        int indicesCount = 0;
        int textureCounter = 0;
        for (float[] thisBlockBox : oneBlockyBoi) {
            // 0, 1, 2, 3, 4, 5
            //-x,-y,-z, x, y, z
            // 0, 0, 0, 1, 1, 1

            //front
            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[5]);
            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[5]);
            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[5]);
            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[5]);
            //front
            float frontLight = 1f;

            //front
            for (int i = 0; i < 12; i++) {
                light.add(frontLight);
            }
            //front
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);

            indicesCount += 4;

            // 0, 1,  2, 3
            //-x,+x, -y,+y

            float[] textureFront = textureArrayArray[textureCounter];

            //front
            textureCoord.add(textureFront[1]); //x positive
            textureCoord.add(textureFront[2]); //y positive
            textureCoord.add(textureFront[0]); //x negative
            textureCoord.add(textureFront[2]); //y positive

            textureCoord.add(textureFront[0]); //x negative
            textureCoord.add(textureFront[3]);   //y negative
            textureCoord.add(textureFront[1]); //x positive
            textureCoord.add(textureFront[3]);   //y negative


            textureCounter++;



            //back
            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[2]);

            //back
            float backLight = 1f;

            //back
            for (int i = 0; i < 12; i++) {
                light.add(backLight);
            }
            //back
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;

            float[] textureBack = textureArrayArray[textureCounter];

            // 0, 1, 2, 3, 4, 5
            //-x,-y,-z, x, y, z
            // 0, 0, 0, 1, 1, 1

            // 0, 1,  2, 3
            //-x,+x, -y,+y


            //back
            textureCoord.add(textureBack[1]);
            textureCoord.add(textureBack[2]);
            textureCoord.add(textureBack[0]);
            textureCoord.add(textureBack[2]);

            textureCoord.add(textureBack[0]);
            textureCoord.add(textureBack[3]);
            textureCoord.add(textureBack[1]);
            textureCoord.add(textureBack[3]);

            textureCounter++;



            //right
            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[5]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[5]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[2]);
            //right
            float rightLight = 1f;

            //right
            for (int i = 0; i < 12; i++) {
                light.add(rightLight);
            }
            //right
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;


            // 0, 1, 2, 3, 4, 5
            //-x,-y,-z, x, y, z
            // 0, 0, 0, 1, 1, 1

            // 0, 1,  2, 3
            //-x,+x, -y,+y


            float[] textureRight = textureArrayArray[textureCounter];
            //right
            textureCoord.add(textureRight[1]);
            textureCoord.add(textureRight[2]);
            textureCoord.add(textureRight[0]);
            textureCoord.add(textureRight[2]);

            textureCoord.add(textureRight[0]);
            textureCoord.add(textureRight[3]);
            textureCoord.add(textureRight[1]);
            textureCoord.add(textureRight[3]);

            textureCounter++;


            //left
            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[5]);

            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[5]);

            //left
            float leftLight = 1f;

            //left
            for (int i = 0; i < 12; i++) {
                light.add(leftLight);
            }
            //left
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;

            float[] textureLeft = textureArrayArray[textureCounter];
            //left
            textureCoord.add(textureLeft[1]);
            textureCoord.add(textureLeft[2]);
            textureCoord.add(textureLeft[0]);
            textureCoord.add(textureLeft[2]);

            textureCoord.add(textureLeft[0]);
            textureCoord.add(textureLeft[3]);
            textureCoord.add(textureLeft[1]);
            textureCoord.add(textureLeft[3]);

            textureCounter++;


            //top
            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[5]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[5]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[4]);
            positions.add(thisBlockBox[2]);
            //top
            float topLight = 1f;

            //top
            for (int i = 0; i < 12; i++) {
                light.add(topLight);
            }
            //top
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;

            // 0, 1, 2, 3, 4, 5
            //-x,-y,-z, x, y, z
            // 0, 0, 0, 1, 1, 1

            // 0, 1,  2, 3
            //-x,+x, -y,+y

            float[] textureTop = textureArrayArray[textureCounter];
            //top
            textureCoord.add(textureTop[1]);
            textureCoord.add(textureTop[2]);
            textureCoord.add(textureTop[0]);
            textureCoord.add(textureTop[2]);

            textureCoord.add(textureTop[0]);
            textureCoord.add(textureTop[3]);
            textureCoord.add(textureTop[1]);
            textureCoord.add(textureTop[3]);

            textureCounter++;


            //bottom
            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[5]);

            positions.add(thisBlockBox[0]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[2]);

            positions.add(thisBlockBox[3]);
            positions.add(thisBlockBox[1]);
            positions.add(thisBlockBox[5]);
            //bottom
            float bottomLight = 1f;

            //bottom
            for (int i = 0; i < 12; i++) {
                light.add(bottomLight);
            }
            //bottom
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;


            // 0, 1, 2, 3, 4, 5
            //-x,-y,-z, x, y, z
            // 0, 0, 0, 1, 1, 1

            // 0, 1,  2, 3
            //-x,+x, -y,+y

            float[] textureBottom = textureArrayArray[textureCounter];
            //bottom
            textureCoord.add(textureBottom[1]);
            textureCoord.add(textureBottom[2]);
            textureCoord.add(textureBottom[0]);
            textureCoord.add(textureBottom[2]);

            textureCoord.add(textureBottom[0]);
            textureCoord.add(textureBottom[3]);
            textureCoord.add(textureBottom[1]);
            textureCoord.add(textureBottom[3]);

            textureCounter++;
        }

        //convert the position objects into usable array
        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i] = (float) positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = (float) light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = (int) indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = (float) textureCoord.get(i);
        }

        playerMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, playerTexture);
    }

    public static float[] translateCharToArray(char thisChar){
        float[] letterArray = new float[]{0,0};
        switch (thisChar){
            case 'a':
                letterArray[1] = 1;
                break;
            case 'A':
                break;

            case 'b':
                letterArray[0] = 1;
                letterArray[1] = 1;
                break;
            case 'B':
                letterArray[0] = 1;
                letterArray[1] = 0;
                break;

            case 'c':
                letterArray[0] = 2;
                letterArray[1] = 1;
                break;
            case 'C':
                letterArray[0] = 2;
                letterArray[1] = 0;
                break;

            case 'd':
                letterArray[0] = 3;
                letterArray[1] = 1;
                break;
            case 'D':
                letterArray[0] = 3;
                letterArray[1] = 0;
                break;

            case 'e':
                letterArray[0] = 4;
                letterArray[1] = 1;
                break;
            case 'E':
                letterArray[0] = 4;
                letterArray[1] = 0;
                break;

            case 'f':
                letterArray[0] = 5;
                letterArray[1] = 1;
                break;
            case 'F':
                letterArray[0] = 5;
                letterArray[1] = 0;
                break;

            case 'g':
                letterArray[0] = 6;
                letterArray[1] = 1;
                break;
            case 'G':
                letterArray[0] = 6;
                letterArray[1] = 0;
                break;

            case 'h':
                letterArray[0] = 7;
                letterArray[1] = 1;
                break;
            case 'H':
                letterArray[0] = 7;
                letterArray[1] = 0;
                break;

            case 'i':
                letterArray[0] = 8;
                letterArray[1] = 1;
                break;
            case 'I':
                letterArray[0] = 8;
                letterArray[1] = 0;
                break;

            case 'j':
                letterArray[0] = 9;
                letterArray[1] = 1;
                break;
            case 'J':
                letterArray[0] = 9;
                letterArray[1] = 0;
                break;

            case 'k':
                letterArray[0] = 10;
                letterArray[1] = 1;
                break;
            case 'K':
                letterArray[0] = 10;
                letterArray[1] = 0;
                break;

            case 'l':
                letterArray[0] = 11;
                letterArray[1] = 1;
                break;
            case 'L':
                letterArray[0] = 11;
                letterArray[1] = 0;
                break;

            case 'm':
                letterArray[0] = 12;
                letterArray[1] = 1;
                break;
            case 'M':
                letterArray[0] = 12;
                letterArray[1] = 0;
                break;

            case 'n':
                letterArray[0] = 13;
                letterArray[1] = 1;
                break;
            case 'N':
                letterArray[0] = 13;
                letterArray[1] = 0;
                break;

            case 'o':
                letterArray[0] = 14;
                letterArray[1] = 1;
                break;
            case 'O':
                letterArray[0] = 14;
                letterArray[1] = 0;
                break;

            case 'p':
                letterArray[0] = 15;
                letterArray[1] = 1;
                break;
            case 'P':
                letterArray[0] = 15;
                letterArray[1] = 0;
                break;

            case 'q':
                letterArray[0] = 16;
                letterArray[1] = 1;
                break;
            case 'Q':
                letterArray[0] = 16;
                letterArray[1] = 0;
                break;

            case 'r':
                letterArray[0] = 17;
                letterArray[1] = 1;
                break;
            case 'R':
                letterArray[0] = 17;
                letterArray[1] = 0;
                break;

            case 's':
                letterArray[0] = 18;
                letterArray[1] = 1;
                break;
            case 'S':
                letterArray[0] = 18;
                letterArray[1] = 0;
                break;

            case 't':
                letterArray[0] = 19;
                letterArray[1] = 1;
                break;
            case 'T':
                letterArray[0] = 19;
                letterArray[1] = 0;
                break;

            case 'u':
                letterArray[0] = 20;
                letterArray[1] = 1;
                break;
            case 'U':
                letterArray[0] = 20;
                letterArray[1] = 0;
                break;

            case 'v':
                letterArray[0] = 21;
                letterArray[1] = 1;
                break;
            case 'V':
                letterArray[0] = 21;
                letterArray[1] = 0;
                break;

            case 'w':
                letterArray[0] = 22;
                letterArray[1] = 1;
                break;
            case 'W':
                letterArray[0] = 22;
                letterArray[1] = 0;
                break;

            case 'x':
                letterArray[0] = 23;
                letterArray[1] = 1;
                break;
            case 'X':
                letterArray[0] = 23;
                letterArray[1] = 0;
                break;

            case 'y':
                letterArray[0] = 24;
                letterArray[1] = 1;
                break;
            case 'Y':
                letterArray[0] = 24;
                letterArray[1] = 0;
                break;

            case 'z':
                letterArray[0] = 25;
                letterArray[1] = 1;
                break;
            case 'Z':
                letterArray[0] = 25;
                letterArray[1] = 0;
                break;
                //now I know my ABCs

            case '0':
                letterArray[0] = 26;
                break;
            case '1':
                letterArray[0] = 27;
                break;
            case '2':
                letterArray[0] = 28;
                break;
            case '3':
                letterArray[0] = 29;
                break;
            case '4':
                letterArray[0] = 30;
                break;
            case '5':
                letterArray[0] = 31;
                break;
            case '6':
                letterArray[0] = 32;
                break;
            case '7':
                letterArray[0] = 33;
                break;
            case '8':
                letterArray[0] = 34;
                break;
            case '9':
                letterArray[0] = 35;
                break;

            case '.':
                letterArray[0] = 26;
                letterArray[1] = 1;
                break;
            case '!':
                letterArray[0] = 27;
                letterArray[1] = 1;
                break;
            case '?':
                letterArray[0] = 28;
                letterArray[1] = 1;
                break;

            case ' ':
                letterArray[0] = 29;
                letterArray[1] = 1;
                break;
            case '-':
                letterArray[0] = 30;
                letterArray[1] = 1;
                break;
            case ':':
                letterArray[0] = 31;
                letterArray[1] = 1;
                break;
            default: //all unknown end up as "AAAAAAAAAA"  ¯\_(ツ)_/¯
                break;
        }

        float[] returningArray = new float[4];

        returningArray[0] = (letterArray[0] * LETTER_WIDTH) / FONT_WIDTH; //-x
        returningArray[1] = ((letterArray[0] * LETTER_WIDTH) + LETTER_WIDTH - 1) / FONT_WIDTH; //+x
        returningArray[2] = (letterArray[1] * LETTER_HEIGHT) / FONT_HEIGHT; //-y
        returningArray[3] = ((letterArray[1] * LETTER_HEIGHT) + LETTER_HEIGHT - 1) / FONT_HEIGHT; //+y

        return returningArray;
    }

    public static Mesh createCustomHudText(String text, float r, float g, float b){

        float x = 0f;
        float z = 0f;


        ArrayList positions = new ArrayList();
        ArrayList textureCoord = new ArrayList();
        ArrayList indices = new ArrayList();
        ArrayList light = new ArrayList();

        int indicesCount = 0;


        for (char letter : text.toCharArray()) {
            //front
            positions.add(x + 0.8f);
            positions.add(0f);
            positions.add(0f);

            positions.add(x);
            positions.add(0f);
            positions.add(0f);

            positions.add(x);
            positions.add(-1f);
            positions.add(0f);

            positions.add(x + 0.8f);
            positions.add(-1f);
            positions.add(0f);

            //front
            for (int i = 0; i < 4; i++) {
                light.add(r);
                light.add(g);
                light.add(b);
            }
            //front
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);

            indicesCount += 4;

            //-x +x   -y +y
            // 0  1    2  3

            float[] thisCharacterArray = translateCharToArray(letter);

            //front
            textureCoord.add(thisCharacterArray[1]);//1
            textureCoord.add(thisCharacterArray[2]);//2
            textureCoord.add(thisCharacterArray[0]);//0
            textureCoord.add(thisCharacterArray[2]);//2
            textureCoord.add(thisCharacterArray[0]);//0
            textureCoord.add(thisCharacterArray[3]);//3
            textureCoord.add(thisCharacterArray[1]);//1
            textureCoord.add(thisCharacterArray[3]);//3

            x++;
        }


        //convert the position objects into usable array
        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i] = (float) positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = (float) light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = (int) indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = (float) textureCoord.get(i);
        }

        return new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, fontTextureAtlas);
    }



    private static int[] invSelection;

    public static int[] getInvSelection(){
        return invSelection;
    }

    private static String oldSelection;

    private static boolean mouseButtonPushed = false;

    private static int pauseButtonSelection;

    public static int getPauseButtonSelection(){
        return pauseButtonSelection;
    }

    private static boolean clicking = false;

    public static boolean getIfClicking(){
        return clicking;
    }

    //todo: redo this mess
    public static void hudOnStepTest(){

        if (!getItemInInventorySlotName(getCurrentInventorySelection(), 0).equals(oldSelection)) {
            resetWieldHandSetupTrigger();
            oldSelection = getItemInInventorySlotName(getCurrentInventorySelection(), 0);
        }

        if (isPlayerInventoryOpen()) {

            playerRot.y += 0.1f;

            if (invSelection == null){
                invSelection = new int[2];
            } else {
                if (isLeftButtonPressed()) {
                    if (!mouseButtonPushed) {
                        mouseButtonPushed = true;

                        if (getMouseInventory() == null) {
                            setMouseInventory(getItemInInventorySlot(invSelection[0], invSelection[1]));

                            removeStackFromInventory(invSelection[0], invSelection[1]);
                        } else {
                            Item bufferItemMouse = getMouseInventory();
                            Item bufferItemInv  = getItemInInventorySlot(invSelection[0], invSelection[1]);
                            setItemInInventory(invSelection[0], invSelection[1], bufferItemMouse.name, bufferItemMouse.stack);
                            setMouseInventory(bufferItemInv);
                        }

                    }
                } else if (!isLeftButtonPressed()){
                    mouseButtonPushed = false;
                }
            }

            //need to create new object or the mouse position gets messed up
            Vector2d mousePos = new Vector2d(getMousePos());

            //work from the center
            mousePos.x -= (getWindowSize().x/2f);
            mousePos.y -= (getWindowSize().y/2f);
            //invert the Y position to follow rendering coordinate system
            mousePos.y *= -1f;

            //collision detect the lower inventory
            for (int x = 1; x <= 9; x++) {
                for (int y = -2; y > -5; y--) {
                    if (
                            mousePos.x > ((x - 5) * (getWindowScale() / 9.5f)) - ((getWindowScale()/10.5f) / 2f) && mousePos.x < ((x - 5) * (getWindowScale() / 9.5f)) + ((getWindowScale()/10.5f) / 2f) && //x axis
                            mousePos.y > ((y+0.3f) * (getWindowScale() / 9.5f)) - ((getWindowScale()/10.5f) / 2f) && mousePos.y < ((y+0.3f) * (getWindowScale() / 9.5f)) + ((getWindowScale()/10.5f) / 2f) //y axis
                    ){
                        invSelection[0] = (x-1);
                        invSelection[1] = ((y*-1) - 1);
                        return;
                    }
                }
            }

            //collision detect the inventory hotbar (upper part)
            for (int x = 1; x <= 9; x++) {
                if (
                        mousePos.x > ((x - 5) * (getWindowScale() / 9.5f)) - ((getWindowScale()/10.5f) / 2f) && mousePos.x < ((x - 5) * (getWindowScale() / 9.5f)) + ((getWindowScale()/10.5f) / 2f) && //x axis
                        mousePos.y > (-0.5f * (getWindowScale() / 9.5f)) - ((getWindowScale()/10.5f) / 2f) && mousePos.y < (-0.5f * (getWindowScale() / 9.5f)) + ((getWindowScale()/10.5f) / 2f) //y axis
                ){
                    invSelection[0] = (x-1);
                    invSelection[1] = 0;
                    return;
                }
            }
            invSelection = null;
        } else if (isPaused()){

            if (pauseButtonSelection != -1 && isLeftButtonPressed() && !clicking){
                clicking = true;
                playSound("button");
                if (pauseButtonSelection == 0){
                    toggleMouseLock();
                    setPaused(false);
                }else if (pauseButtonSelection == 1) {
                    setVSync(!isvSync());
                    toggleVsyncMesh();
                } else if (pauseButtonSelection == 2){
                    glfwSetWindowShouldClose(getWindowHandle(), true);
                }

            } else if (!isLeftButtonPressed() && clicking) {
                clicking = false;
            }

            //need to create new object or the mouse position gets messed up
            Vector2d mousePos = new Vector2d(getMousePos());

            //work from the center
            mousePos.x -= (getWindowSize().x/2f);
            mousePos.y -= (getWindowSize().y/2f);
            //invert the Y position to follow rendering coordinate system
            mousePos.y *= -1f;

            for (int y = 0; y > -3; y --){
//                Matrix4f modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f(0, (y+1) * getWindowScale()/3f, 0), new Vector3f(0, 0, 0), new Vector3f(windowScale/2f, windowScale/2f, windowScale/2f));

                if (mousePos.x > -(getWindowScale()/2f * 0.5f) && mousePos.x < (getWindowScale()/2f * 0.5f) && //x axis\
                    mousePos.y > ((y+1) * getWindowScale()/3f) - ((getWindowScale()/2f) * 0.125f) && mousePos.y < ((y+1) * getWindowScale()/3f) + ((getWindowScale()/2f) * 0.125f)) { //y axis
                    pauseButtonSelection = (y * -1);
                    return;
                }
            }

            pauseButtonSelection = -1;
        }
    }

    public static void togglePauseMenu(){
        setPaused(!isPaused());
    }
}
