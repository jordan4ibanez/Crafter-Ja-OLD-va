package engine.hud;

//this is a complex mess

import engine.graph.Mesh;
import engine.graph.Texture;
import game.item.Item;
import org.joml.Vector2d;
import org.joml.Vector3f;

import java.util.ArrayList;

import static engine.MouseInput.*;
import static engine.hud.TextHandling.translateCharToArray;
import static engine.render.GameRenderer.getWindowScale;
import static engine.render.GameRenderer.getWindowSize;
import static engine.Time.getDelta;
import static engine.Window.*;
import static engine.sound.SoundAPI.playSound;
import static game.Crafter.getVersionName;
import static game.chunk.ChunkMesh.convertLight;
import static game.player.Inventory.*;
import static game.player.Player.*;
import static org.lwjgl.glfw.GLFW.*;

public class Hud {
    private final static float scale = 1f;

    private static final Vector3f playerScale = new Vector3f(0.7f,0.8f,0.7f);
    private static final Vector3f playerRot = new Vector3f(0,0,0);

    private static final Vector3f versionInfoPos = new Vector3f(-5f,8f,-14f);
    private static final Vector3f versionInfoShadowPos = new Vector3f(-4.9f,7.9f,-14f);

    private static boolean paused = false;

    //health bar elements
    //calculated per half heart
    private static final byte[] healthHudArray = new byte[10];
    private static byte healthHudFloatIndex = 9;
    private static boolean heartUp = true;
    private static final float[] healthHudFloatArray = new float[10];

    //textures
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
    private static Texture globalWaterEffect;
    private static Texture heartTexture;
    private static Texture heartShadowTexture;

    //meshes
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
    private static Mesh globalWaterEffectMesh;
    private static Mesh heartHudMesh;
    private static Mesh halfHeartHudMesh;
    private static Mesh heartShadowHudMesh;

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
        createWieldHandMesh((byte)15);
        createInventorySlot();
        createInventorySlotSelected();
        createMenuBg();
        createButtons();
        rebuildMiningMesh(0);
        createGlobalWaterEffect();
        createHeart();
        createHalfHeart();
        createHeartShadow();

        continueMesh = createCustomHudText("CONTINUE", 1,1,1);
        toggleVsyncMesh = createCustomHudText("VSYNC:ON", 1,1,1);
        quitGameMesh = createCustomHudText("QUIT", 1,1,1);
    }

    public static Vector3f getPlayerHudRotation(){
        return playerRot;
    }

    public static boolean isPaused(){
        return paused;
    }

    public static void setPaused(boolean truth){
        paused = truth;
    }


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
        globalWaterEffect = new Texture("textures/water_overlay.png");
        heartTexture = new Texture("textures/heart.png");
        heartShadowTexture = new Texture("textures/heart_shadow.png");
    }

    public static Mesh getHeartHudMesh(){
        return heartHudMesh;
    }

    public static Mesh getHalfHeartHudMesh(){
        return halfHeartHudMesh;
    }

    public static Mesh getHeartShadowHudMesh(){
        return heartShadowHudMesh;
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

    public static Mesh getGlobalWaterEffectMesh(){
        return globalWaterEffectMesh;
    }

    public static void calculateHealthBarElements(){
        int health = getPlayerHealth();

        byte z = 1; //this needs to start from 1 like lua

        //compare health elements (base 2), generate new health bar
        for (byte i = 0; i < 10; i++){
            int compare = health - (z * 2);

            if (compare >= 0){
                healthHudArray[i] = 2;
            } else if (compare == -1){
                healthHudArray[i] = 1;
            } else {
                healthHudArray[i] = 0;
            }
            z++;
        }
        //System.out.println(Arrays.toString(healthHudArray));
    }

    public static void makeHeartsJiggle(){
        float delta = getDelta();
        if (heartUp) {
            healthHudFloatArray[healthHudFloatIndex] += delta * 200f;
            if (healthHudFloatArray[healthHudFloatIndex] > 10f){
                healthHudFloatArray[healthHudFloatIndex] = 10f;
                heartUp = false;
            }
        } else {
            healthHudFloatArray[healthHudFloatIndex] -= delta * 200f;
            if (healthHudFloatArray[healthHudFloatIndex] < 0f){
                healthHudFloatArray[healthHudFloatIndex] = 0f;

                heartUp = true;

                //cycle through hearts
                healthHudFloatIndex -= 1;
                if (healthHudFloatIndex < 0){
                    healthHudFloatIndex = 9;
                }

            }
        }


    }

    public static byte[] getHealthHudArray(){
        return healthHudArray;
    }

    public static float[] getHealthHudFloatArray(){
        return healthHudFloatArray;
    }

    private static void createGlobalWaterEffect(){
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

        globalWaterEffectMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, globalWaterEffect);
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

    private static void createHalfHeart(){
        ArrayList positions = new ArrayList();
        ArrayList textureCoord = new ArrayList();
        ArrayList indices = new ArrayList();
        ArrayList light = new ArrayList();

        int indicesCount = 0;

        //front
        positions.add(0f);
        positions.add(0.5f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(0.5f);
        positions.add(0f);

        positions.add(-0.5f);
        positions.add(-0.5f);
        positions.add(0f);

        positions.add(0f);
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
        textureCoord.add(0.5f);//1
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(0f);//2
        textureCoord.add(0f);//0
        textureCoord.add(1f);//3
        textureCoord.add(0.5f);//1
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

        halfHeartHudMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, heartTexture);
    }

    private static void createHeart(){
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

        heartHudMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, heartTexture);
    }

    private static void createHeartShadow(){
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

        heartShadowHudMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, heartShadowTexture);
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

    //public to interface with rest of it
    public static void rebuildWieldHandMesh(byte lightLevel){
        createWieldHandMesh(lightLevel);
    }

    private static void createWieldHandMesh(byte lightLevel){

        float floatedLight = convertLight((float)lightLevel/15f);

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
            for (int i = 0; i < 12; i++) {
                light.add(floatedLight);
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
            for (int i = 0; i < 12; i++) {
                light.add(floatedLight);
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
            for (int i = 0; i < 12; i++) {
                light.add(floatedLight);
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
            for (int i = 0; i < 12; i++) {
                light.add(floatedLight);
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
            for (int i = 0; i < 12; i++) {
                light.add(floatedLight);
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
            for (int i = 0; i < 12; i++) {
                light.add(floatedLight);
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


    public static Mesh createCustomHudTextCentered(String text, float r, float g, float b){
        //x is the actual position in the mesh creation of the letter
        float x = ((-text.length()/2f) * 0.8f);
        //get the amount of letters in the string
        int stringLength = text.length();

        float[] positions = new float[stringLength * 12];
        float[] textureCoord = new float[stringLength * 8];
        int[] indices = new int[stringLength * 6];

        float[] light = new float[stringLength * 12];

        int indicesCount = 0;


        int i = 0; //positions count
        int a = 0; //light count
        int w = 0; //textureCoord count
        int t = 0; //indices count

        for (char letter : text.toCharArray()) {
            positions[i     ] = (x + 0.8f);
            positions[i + 1 ] = (0.5f);
            positions[i + 2 ] = (0f);
            positions[i + 3 ] = (x);
            positions[i + 4 ] = (0.5f);
            positions[i + 5 ] = (0f);
            positions[i + 6 ] = (x);
            positions[i + 7 ] = (-0.5f);
            positions[i + 8 ] = (0f);
            positions[i + 9 ] = (x + 0.8f);
            positions[i + 10] = (-0.5f);
            positions[i + 11] = (0f);
            i += 12;

            for (int q = 0; q < 4; q++) {
                light[a    ] = (r);
                light[a + 1] = (g);
                light[a + 2] = (b);
                a += 3;
            }

            indices[t    ] = (0 + indicesCount);
            indices[t + 1] = (1 + indicesCount);
            indices[t + 2] = (2 + indicesCount);
            indices[t + 3] = (0 + indicesCount);
            indices[t + 4] = (2 + indicesCount);
            indices[t + 5] = (3 + indicesCount);

            t += 6;
            indicesCount += 4;


            //translate the character (char primitive) into a usable float array
            float[] thisCharacterArray = translateCharToArray(letter);

            textureCoord[w    ] = (thisCharacterArray[1]);
            textureCoord[w + 1] = (thisCharacterArray[2]);
            textureCoord[w + 2] = (thisCharacterArray[0]);
            textureCoord[w + 3] = (thisCharacterArray[2]);
            textureCoord[w + 4] = (thisCharacterArray[0]);
            textureCoord[w + 5] = (thisCharacterArray[3]);
            textureCoord[w + 6] = (thisCharacterArray[1]);
            textureCoord[w + 7] = (thisCharacterArray[3]);
            w += 8;

            //shift the left of the letter to the right
            //kind of like a type writer
            x++;
        }

        return new Mesh(positions, light, indices, textureCoord, fontTextureAtlas);
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

        float delta = getDelta();

        if (!getItemInInventorySlotName(getCurrentInventorySelection(), 0).equals(oldSelection)) {
            resetWieldHandSetupTrigger();
            oldSelection = getItemInInventorySlotName(getCurrentInventorySelection(), 0);
        }

        if (isPlayerInventoryOpen()) {

            //begin player in inventory thing
            //new scope because lazy
            {

                float windowScale = getWindowScale();
                Vector2d basePlayerPos = new Vector2d(-(windowScale / 3.75d), (windowScale / 2.8d));
                Vector2d mousePos = getMousePos();

                float rotationY = (float)((mousePos.x - (getWindowWidth()/2f)) - basePlayerPos.x) / (windowScale * 1.2f);
                rotationY *= 40f;
                playerRot.y = rotationY;


                float rotationX = (float)((mousePos.y - (getWindowHeight()/2f)) + (basePlayerPos.y /2f)) / (windowScale * 1.2f);
                rotationX *= 40f;
                playerRot.x = rotationX;
            }


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


    private static Mesh create2DMesh(float width, float height, String texture) throws Exception {

        //ArrayList positions = new ArrayList();
        float[] positions = new float[12];
        float[] textureCoord = new float[8];

        int[] indices = new int[6];

        float[] light = new float[12];


        positions[0] = (width);
        positions[1] = (height);
        positions[2] = (0f);
        positions[3] = (-width);
        positions[4] = (height);
        positions[5] = (0f);
        positions[6] = (-width);
        positions[7] = (-height);
        positions[8] = (0f);
        positions[9] = (width);
        positions[10] = (-height);
        positions[11] = (0f);

        for (int i = 0; i < 12; i++) {
            light[i] = 1f;
        }

        indices[0] = (0);
        indices[1] = (1);
        indices[2] = (2);
        indices[3] = (0);
        indices[4] = (2);
        indices[5] = (3);

        textureCoord[0] = (1f);
        textureCoord[1] = (0f);
        textureCoord[2] = (0f);
        textureCoord[3] = (0f);
        textureCoord[4] = (0f);
        textureCoord[5] = (1f);
        textureCoord[6] = (1f);
        textureCoord[7] = (1f);


        return new Mesh(positions, light, indices, textureCoord, new Texture(texture));
    }
}
