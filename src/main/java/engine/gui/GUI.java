package engine.gui;

import engine.highPerformanceContainers.HyperFloatArray;
import engine.highPerformanceContainers.HyperIntArray;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static engine.graphics.Mesh.cleanUpMesh;
import static engine.graphics.Mesh.createMesh;
import static engine.graphics.Texture.createTexture;
import static engine.gui.TextHandling.createTextWithShadow;
import static engine.time.Timer.getFpsCounted;
import static game.Crafter.getVersionName;

public class GUI {

    //textures
    private static int worldSelectionTexture;
    private static int playerTexture;
    private static int miningCrack;

    //meshes
    private static final int[] miningCrackMesh = new int[9];

    private static int hotBarMesh;
    private static int hotBarSelectionMesh;

    private static int inventoryMesh;
    private static int inventoryBackdropMesh;
    private static int thisWorldSelectionMesh;
    private static int thisCrossHairMesh;
    private static int playerMesh;
    private static int versionInfoText;
    private static int wieldHandMesh;
    private static int inventorySlotMesh;
    private static int inventorySlotSelectedMesh;
    private static int buttonMesh;
    private static int buttonSelectedMesh;
    private static int buttonPushedMesh;
    private static int textInputMesh;
    private static int textInputSelectedMesh;
    private static int menuBgMesh;
    private static int globalWaterEffectMesh;
    private static int heartHudMesh;
    private static int halfHeartHudMesh;
    private static int heartShadowHudMesh;
    private static int chatBox;

    private static int sun;
    private static int moon;

    private static int fpsMesh;

    public static void createGUI() {

        //2D mesh creations
        hotBarMesh = create2DMesh(0.5f,0.06043956043f, "textures/hotbar.png");
        inventoryMesh = create2DMesh(0.5f,0.46335697399f,  "textures/inventory.png");
        inventoryBackdropMesh = create2DMesh(0.5f,0.46335697399f,  "textures/inventory_backdrop.png");
        hotBarSelectionMesh = create2DMesh(0.5f, 0.5f, "textures/hotbar_selected.png");
        thisCrossHairMesh = create2DMesh(0.5f, 0.5f, "textures/crosshair.png");
        inventorySlotMesh = create2DMesh(0.5f,0.5f,"textures/inventory_slot.png");
        inventorySlotSelectedMesh = create2DMesh(0.5f,0.5f, "textures/inventory_slot_selected.png");
        menuBgMesh = create2DMesh(0.5f,0.5f, "textures/menu_bg.png");
        buttonMesh = create2DMesh(0.5f,0.5f,"textures/button.png");
        buttonSelectedMesh = create2DMesh(0.5f,0.5f,"textures/button_selected.png");
        buttonPushedMesh = create2DMesh(0.5f,0.5f,"textures/button_pushed.png");
        textInputMesh = create2DMesh(0.5f,0.5f,"textures/text_box.png");
        textInputSelectedMesh = create2DMesh(0.5f,0.5f,"textures/text_box_selected.png");
        globalWaterEffectMesh = create2DMesh(0.5f,0.5f, "textures/water_overlay.png");
        heartHudMesh = create2DMesh(0.5f, 0.5f, "textures/heart.png");
        halfHeartHudMesh = create2DMesh(0.5f, 0.5f, 0.5f, "textures/heart.png");
        heartShadowHudMesh = create2DMesh(0.5f, 0.5f, "textures/heart_shadow.png");
        chatBox = create2DMeshOffsetRight(0.5f, 0.5f, "textures/chat_box.png");
        sun = create2DMesh(0.5f, 0.5f, "textures/sun.png");
        moon = create2DMesh(0.5f, 0.5f, "textures/moon.png");

        //2D text creations
        fpsMesh = createTextWithShadow("FPS: " + getFpsCounted(), 1f, 1f, 1f);
        versionInfoText = createTextWithShadow(getVersionName(), 1,1,1);

        //3D mesh creations
        buildHandMesh();
        buildMiningMesh();
        createWorldSelectionMesh();
        createPlayerMesh(); //todo REBUILD THIS JUNK, THIS IS HORRIBLE
    }


    public static void initializeHudAtlas() {
        worldSelectionTexture = createTexture("textures/selection.png");
        playerTexture = createTexture("textures/player.png");
        miningCrack = createTexture("textures/crack_anylength.png");
    }

    public static int getSunMesh(){
        return sun;
    }

    public static int getMoonMesh(){
        return moon;
    }

    public static int getChatBoxMesh(){
        return chatBox;
    }

    public static int getHeartHudMesh(){
        return heartHudMesh;
    }

    public static int getHalfHeartHudMesh(){
        return halfHeartHudMesh;
    }

    public static int getHeartShadowHudMesh(){
        return heartShadowHudMesh;
    }

    public static int getHotBarMesh(){
        return hotBarMesh;
    }

    public static int getHotBarSelectionMesh(){
        return hotBarSelectionMesh;
    }

    public static int getInventoryMesh(){
        return inventoryMesh;
    }
    public static int getInventoryBackdropMesh(){
        return inventoryBackdropMesh;
    }

    public static int getWorldSelectionMesh(){
        return thisWorldSelectionMesh;
    }

    public static int getCrossHairMesh(){
        return thisCrossHairMesh;
    }

    public static int getPlayerMesh(){
        return playerMesh;
    }

    public static int getVersionInfoText(){
        return versionInfoText;
    }

    public static int getWieldHandMesh(){
        return wieldHandMesh;
    }

    public static int getButtonMesh(){
        return buttonMesh;
    }
    public static int getButtonSelectedMesh(){
        return buttonSelectedMesh;
    }

    public static int getTextInputMesh(){
        return textInputMesh;
    }
    public static int getTextInputSelectedMesh(){
        return textInputSelectedMesh;
    }

    public static int getButtonPushedMesh(){
        return buttonPushedMesh;
    }
    public static int getMenuBgMesh(){
        return menuBgMesh;
    }

    public static int getInventorySlotMesh(){
        return inventorySlotMesh;
    }
    public static int getInventorySlotSelectedMesh(){
        return inventorySlotSelectedMesh;
    }

    public static int getMiningCrackMesh(byte diggingFrame){
        return miningCrackMesh[diggingFrame];
    }

    public static int getGlobalWaterEffectMesh(){
        return globalWaterEffectMesh;
    }

    public static void buildFPSMesh() {
        cleanUpMesh(fpsMesh, false);
        fpsMesh = createTextWithShadow("FPS: " + getFpsCounted(), 1f, 1f, 1f);
    }

    public static int getFPSMesh(){
        return fpsMesh;
    }

    public static void buildMiningMesh() {
        HyperFloatArray positions = new HyperFloatArray(12);
        HyperFloatArray textureCoord = new HyperFloatArray(6);
        HyperIntArray indices = new HyperIntArray(4);
        HyperFloatArray light = new HyperFloatArray(12);

        for (byte level = 0; level < 9; level++) {
            final float min = -0.0001f;
            final float max = 1.0001f;

            int indicesCount = 0;

            final float maxLevels = 9;

            float textureMin = (float) level / maxLevels;
            float textureMax = (float) (level + 1) / maxLevels;

            positions.pack(max, max, max, min, max, max, min, min, max, max, min, max);
            light.pack(1);
            indices.pack(0, 1 + indicesCount, 2 + indicesCount, 0, 2 + indicesCount, 3 + indicesCount);
            indicesCount += 4;
            textureCoord.pack(1f, textureMin, 0f, textureMin, 0f, textureMax, 1f, textureMax);

            positions.pack(min, max, min, max, max, min, max, min, min, min, min, min);
            light.pack(1);
            indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);
            indicesCount += 4;
            textureCoord.pack(1f, textureMin, 0f, textureMin, 0f, textureMax, 1f, textureMax);

            positions.pack(max, max, min, max, max, max, max, min, max, max, min, min);
            light.pack(1);
            indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);
            indicesCount += 4;
            textureCoord.pack(1f, textureMin, 0f, textureMin, 0f, textureMax, 1f, textureMax);

            positions.pack(min, max, max, min, max, min, min, min, min, min, min, max);
            light.pack(1);
            indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);
            indicesCount += 4;
            textureCoord.pack(1f, textureMin, 0f, textureMin, 0f, textureMax, 1f, textureMax);

            positions.pack(min, max, min, min, max, max, max, max, max, max, max, min);
            light.pack(1);
            indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);
            indicesCount += 4;
            textureCoord.pack(1f, textureMin, 0f, textureMin, 0f, textureMax, 1f, textureMax);


            positions.pack(min, min, max, min, min, min, max, min, min, max, min, max);
            light.pack(1);
            indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);
            textureCoord.pack(1f, textureMin, 0f, textureMin, 0f, textureMax, 1f, textureMax);

            miningCrackMesh[level] = createMesh(positions.values(), light.values(), indices.values(), textureCoord.values(), miningCrack);

            positions.reset();
            light.reset();
            indices.reset();
            textureCoord.reset();
        }

        positions.clear();
        light.clear();
        indices.clear();
        textureCoord.clear();
    }

    private static void createWorldSelectionMesh() {

        List<Float> positions = new ArrayList<>();
        List<Float> textureCoord = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Float> light = new ArrayList<>();

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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
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
            float backLight = 1f;
            //back
            for (int i = 0; i < 12; i++) {
                light.add(backLight);
            }
            //back
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;

            //top
            textureCoord.add(1f);//1
            textureCoord.add(0f);//2
            textureCoord.add(0f);//0
            textureCoord.add(0f);//2
            textureCoord.add(0f);//0
            textureCoord.add(1f);//3
            textureCoord.add(1f);//1
            textureCoord.add(1f);//3


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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;


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
            positionsArray[i] = positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = textureCoord.get(i);
        }

        thisWorldSelectionMesh = createMesh(positionsArray, lightArray, indicesArray, textureCoordArray, worldSelectionTexture);
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

    private static void buildHandMesh(){

        float floatedLight = 1f;

        float[][] handArrayArray = new float[][]{
                //right arm
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


        List<Float> positions = new ArrayList<>();
        List<Float> textureCoord = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Float> light = new ArrayList<>();

        int indicesCount = 0;
        int textureCounter = 0;
        for (float[] thisBlockBox : handArrayArray) {
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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;

            float[] textureBack = textureArrayArray[textureCounter];

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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;


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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;

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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;

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
            positionsArray[i] = positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = textureCoord.get(i);
        }

        wieldHandMesh = createMesh(positionsArray, lightArray, indicesArray, textureCoordArray, playerTexture);

        positions.clear();
        light.clear();
        indices.clear();
        textureCoord.clear();
    }


    private static void createPlayerMesh(){
        float[][] playerModelBoxes = new float[][]{
                //head
                {-0.75f,-0.5f,-0.75f,0.75f,1.0f,0.75f},
                //body
                {-0.75f,-2.5f,-0.45f,0.75f,-0.5f,0.45f},
                //right arm
                {0.75f,-2.75f,-0.45f, 1.65f,-0.5f,0.45f},
                //left arm
                {-1.65f,-2.75f,-0.45f, -0.75f,-0.5f,0.45f},
                //right leg
                {-0.75f,-5.0f,-0.375f, 0.0f,-2.5f,0.375f},
                //left leg
                {0.0f,-5.0f,-0.375f,  0.75f,-2.5f,0.375f},
        };

        float[][] textureArrayArray = new float[][]{
                //head
                //back
                calculateTexture(8,8,16,16),
                //front
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
                //back
                calculateTexture(20,20,28,30),
                //front
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
                //back
                calculateTexture(44,20,48,32), //light
                //front
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
                //back
                calculateTexture(44,20,48,32), //light
                //front
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
                //back
                calculateTexture(4,20,8,32), //light
                //front
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
                //back
                calculateTexture(4,20,8,32), //light
                //front
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

        List<Float> positions = new ArrayList<>();
        List<Float> textureCoord = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Float> light = new ArrayList<>();

        int indicesCount = 0;
        int textureCounter = 0;
        for (float[] thisBlockBox : playerModelBoxes) {

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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;

            float[] textureBack = textureArrayArray[textureCounter];

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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;


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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;


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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
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
            positionsArray[i] = positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = textureCoord.get(i);
        }

        playerMesh = createMesh(positionsArray, lightArray, indicesArray, textureCoordArray, playerTexture);

        positions.clear();
        light.clear();
        indices.clear();
        textureCoord.clear();
    }


    public static int create2DMesh(float width, float height, String texture) {
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

        return createMesh(positions, light, indices, textureCoord, createTexture(texture));
    }

    public static int create2DMeshOffsetRight(float width, float height, String texture) {
        float[] positions = new float[12];
        float[] textureCoord = new float[8];
        int[] indices = new int[6];
        float[] light = new float[12];

        positions[0] = (width*2f);
        positions[1] = (height);
        positions[2] = (0f);

        positions[3] = (0);
        positions[4] = (height);
        positions[5] = (0f);

        positions[6] = (0);
        positions[7] = (-height);
        positions[8] = (0f);

        positions[9] = (width*2f);
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

        return createMesh(positions, light, indices, textureCoord, createTexture(texture));
    }

    //overloaded for texture width (used for half hearts)
    public static int create2DMesh(float width, float height, float textureWidth, String texture) {
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

        return createMesh(positions, light, indices, textureCoord, createTexture(texture));
    }
}