package engine.gui;

//this is a complex mess

import engine.graph.Mesh;
import engine.graph.Texture;

import org.joml.Vector3f;

import java.util.ArrayList;

import static engine.Timer.getFpsCounted;
import static engine.gui.TextHandling.createText;
import static engine.gui.TextHandling.createTextCentered;
import static engine.Time.getDelta;
import static engine.Window.*;

import static game.Crafter.getVersionName;
import static game.chunk.ChunkMesh.convertLight;
import static game.player.Player.*;

public class GUI {
    private static final Vector3f playerScale = new Vector3f(0.7f,0.8f,0.7f);

    private static final Vector3f versionInfoPos = new Vector3f(-5f,8f,-14f);
    private static final Vector3f versionInfoShadowPos = new Vector3f(-4.9f,7.9f,-14f);

    //health bar elements
    //calculated per half heart
    private static final byte[] healthHudArray = new byte[10];
    private static byte healthHudFloatIndex = 9;
    private static boolean heartUp = true;
    private static final float[] healthHudFloatArray = new float[10];

    //textures
    private static Texture worldSelection;
    private static Texture playerTexture;
    private static Texture miningCrack;

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


    private static Mesh fpsShadowMesh;
    private static Mesh fpsMesh;

    public static void createGUI() throws Exception {

        //2D mesh creations
        thisHotBarMesh = create2DMesh(0.5f,0.06043956043f, "textures/hotbar.png");
        thisInventoryMesh = create2DMesh(0.5f,0.46335697399f,  "textures/inventory.png");
        thisSelectionMesh = create2DMesh(0.5f, 0.5f, "textures/hotbar_selected.png");
        thisCrossHairMesh = create2DMesh(0.5f, 0.5f, "textures/crosshair.png");
        inventorySelectionMesh = create2DMesh(0.5f, 0.5f, "textures/hotbar_selected.png");
        inventorySlotMesh = create2DMesh(0.5f,0.5f,"textures/inventory_slot.png");
        inventorySlotSelectedMesh = create2DMesh(0.5f,0.5f, "textures/inventory_slot_selected.png");
        menuBgMesh = create2DMesh(0.5f,0.5f, "textures/menu_bg.png");
        buttonMesh = create2DMesh(0.5f,0.5f,"textures/button.png");
        buttonSelectedMesh = create2DMesh(0.5f,0.5f,"textures/button_selected.png");
        buttonPushedMesh = create2DMesh(0.5f,0.5f,"textures/button_pushed.png");
        globalWaterEffectMesh = create2DMesh(0.5f,0.5f, "textures/water_overlay.png");
        heartHudMesh = create2DMesh(0.5f, 0.5f, "textures/heart.png");
        halfHeartHudMesh = create2DMesh(0.5f, 0.5f, 0.5f, "textures/heart.png");
        heartShadowHudMesh = create2DMesh(0.5f, 0.5f, "textures/heart_shadow.png");

        //2D text creations
        continueMesh = createTextCentered("CONTINUE", 1,1,1);
        toggleVsyncMesh = createTextCentered("VSYNC:ON", 1,1,1);
        quitGameMesh = createTextCentered("QUIT", 1,1,1);

        fpsShadowMesh = createText("FPS: " + getFpsCounted(), 0f, 0f, 0f);
        fpsMesh = createText("FPS: " + getFpsCounted(), 1f, 1f, 1f);

        versionInfoText = createText(getVersionName(), 1,1,1);
        versionInfoTextShadow = createText(getVersionName(), 0,0,0);



        //3D mesh creations
        createWieldHandMesh((byte)15);
        rebuildMiningMesh(0);
        createWorldSelectionMesh();
        createPlayerMesh(); //todo REBUILD THIS JUNK, THIS IS HORRIBLE
    }


    public static void initializeHudAtlas() throws Exception {
        worldSelection = new Texture("textures/selection.png");
        playerTexture = new Texture("textures/player.png");
        miningCrack = new Texture("textures/crack_anylength.png");
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

    public static void toggleVsyncMesh(){
        if (isvSync()) {
            toggleVsyncMesh = createTextCentered("VSYNC:ON", 1, 1, 1);
            System.out.println("vsync on");
        } else {
            toggleVsyncMesh = createTextCentered("VSYNC:OFF", 1, 1, 1);
            System.out.println("vsync off");
        }
    }

    public static void buildFPSMesh() {
        fpsShadowMesh = createText("FPS: " + getFpsCounted(), 0f, 0f, 0f);
        fpsMesh = createText("FPS: " + getFpsCounted(), 1f, 1f, 1f);
    }

    public static Mesh getFPSMesh(){
        return fpsMesh;
    }

    public static Mesh getFPSShadowMesh(){
        return fpsShadowMesh;
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


    private static Mesh create2DMesh(float width, float height, String texture) throws Exception {
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

    //overloaded for texture width (used for half hearts)
    private static Mesh create2DMesh(float width, float height, float textureWidth, String texture) throws Exception {
        float[] positions = new float[12];
        float[] textureCoord = new float[8];
        int[] indices = new int[6];
        float[] light = new float[12];

        positions[0] = (width - textureWidth);
        positions[1] = (height);
        positions[2] = (0f);
        positions[3] = (-width);
        positions[4] = (height);
        positions[5] = (0f);
        positions[6] = (-width);
        positions[7] = (-height);
        positions[8] = (0f);
        positions[9] = (width - textureWidth);
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

        textureCoord[0] = (textureWidth);
        textureCoord[1] = (0f);
        textureCoord[2] = (0f);
        textureCoord[3] = (0f);
        textureCoord[4] = (0f);
        textureCoord[5] = (1f);
        textureCoord[6] = (textureWidth);
        textureCoord[7] = (1f);

        return new Mesh(positions, light, indices, textureCoord, new Texture(texture));
    }
}