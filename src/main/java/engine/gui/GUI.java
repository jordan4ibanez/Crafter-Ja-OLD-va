package engine.gui;

import engine.graphics.Mesh;
import engine.graphics.Texture;
import engine.highPerformanceContainers.HyperFloatArray;
import engine.highPerformanceContainers.HyperIntArray;
import org.joml.Vector3f;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class GUI {

    private final MeshCreator2D meshCreator2D = new MeshCreator2D();
    private final TextHandling textHandling = new TextHandling();

    //textures
    private final Texture worldSelectionTexture;
    private final Texture playerTexture;
    private final Texture miningCrack;

    //meshes
    private final Mesh[] miningCrackMesh;

    private final Mesh hotBarMesh;
    private final Mesh hotBarSelectionMesh;

    private final Mesh inventoryMesh;
    private final Mesh inventoryBackdropMesh;
    private final Mesh worldSelectionMesh;
    private final Mesh crossHairMesh;
    private final Mesh playerMesh;
    private final Mesh versionInfoText;
    private final Mesh wieldHandMesh;
    private final Mesh inventorySlotMesh;
    private final Mesh inventorySlotSelectedMesh;
    private final Mesh buttonMesh;
    private final Mesh buttonSelectedMesh;
    private final Mesh buttonPushedMesh;
    private final Mesh textInputMesh;
    private final Mesh textInputSelectedMesh;
    private final Mesh menuBgMesh;
    private final Mesh globalWaterEffectMesh;
    private final Mesh heartHudMesh;
    private final Mesh halfHeartHudMesh;
    private final Mesh heartShadowHudMesh;
    private final Mesh chatBox;

    private final Mesh sun;
    private final Mesh moon;


    public GUI(String versionName){
        //2D mesh creations
        hotBarMesh = meshCreator2D.create2DMesh(0.5f,0.06043956043f, "textures/hotbar.png");
        inventoryMesh = meshCreator2D.create2DMesh(0.5f,0.46335697399f,  "textures/inventory.png");
        inventoryBackdropMesh = meshCreator2D.create2DMesh(0.5f,0.46335697399f,  "textures/inventory_backdrop.png");
        hotBarSelectionMesh = meshCreator2D.create2DMesh(0.5f, 0.5f, "textures/hotbar_selected.png");
        crossHairMesh = meshCreator2D.create2DMesh(0.5f, 0.5f, "textures/crosshair.png");
        inventorySlotMesh = meshCreator2D.create2DMesh(0.5f,0.5f,"textures/inventory_slot.png");
        inventorySlotSelectedMesh = meshCreator2D.create2DMesh(0.5f,0.5f, "textures/inventory_slot_selected.png");
        menuBgMesh = meshCreator2D.create2DMesh(0.5f,0.5f, "textures/menu_bg.png");
        buttonMesh = meshCreator2D.create2DMesh(0.5f,0.5f,"textures/button.png");
        buttonSelectedMesh = meshCreator2D.create2DMesh(0.5f,0.5f,"textures/button_selected.png");
        buttonPushedMesh = meshCreator2D.create2DMesh(0.5f,0.5f,"textures/button_pushed.png");
        textInputMesh = meshCreator2D.create2DMesh(0.5f,0.5f,"textures/text_box.png");
        textInputSelectedMesh = meshCreator2D.create2DMesh(0.5f,0.5f,"textures/text_box_selected.png");
        globalWaterEffectMesh = meshCreator2D.create2DMesh(0.5f,0.5f, "textures/water_overlay.png");
        heartHudMesh = meshCreator2D.create2DMesh(0.5f, 0.5f, "textures/heart.png");
        halfHeartHudMesh = meshCreator2D.createHalf2DMesh(0.5f, 0.5f, 0.5f, "textures/heart.png");
        heartShadowHudMesh = meshCreator2D.create2DMesh(0.5f, 0.5f, "textures/heart_shadow.png");
        chatBox = meshCreator2D.create2DMeshOffsetRight();
        sun = meshCreator2D.create2DMesh(0.5f, 0.5f, "textures/sun.png");
        moon = meshCreator2D.create2DMesh(0.5f, 0.5f, "textures/moon.png");

        //2D text creation
        versionInfoText = textHandling.createTextWithShadow(versionName, 1,1,1);


        worldSelectionTexture = new Texture("textures/selection.png");
        playerTexture = new Texture("textures/player.png");
        miningCrack = new Texture("textures/crack_anylength.png");

        //3D mesh creations
        wieldHandMesh = buildHandMesh();

        miningCrackMesh = new Mesh[9];
        for (int i = 0; i < 9; i++){
            miningCrackMesh[i] = buildMiningMesh(i);
        }

        worldSelectionMesh = createWorldSelectionMesh();
        playerMesh = createPlayerMesh();
    }

    public Mesh getSunMesh(){
        return sun;
    }

    public Mesh getMoonMesh(){
        return moon;
    }

    public Mesh getChatBoxMesh(){
        return chatBox;
    }

    public Mesh getHeartHudMesh(){
        return heartHudMesh;
    }

    public Mesh getHalfHeartHudMesh(){
        return halfHeartHudMesh;
    }

    public Mesh getHeartShadowHudMesh(){
        return heartShadowHudMesh;
    }

    public Mesh getHotBarMesh(){
        return hotBarMesh;
    }

    public Mesh getHotBarSelectionMesh(){
        return hotBarSelectionMesh;
    }

    public Mesh getInventoryMesh(){
        return inventoryMesh;
    }
    public Mesh getInventoryBackdropMesh(){
        return inventoryBackdropMesh;
    }

    public Mesh getWorldSelectionMesh(){
        return worldSelectionMesh;
    }

    public Mesh getCrossHairMesh(){
        return crossHairMesh;
    }

    public Mesh getPlayerMesh(){
        return playerMesh;
    }

    public Mesh getVersionInfoText(){
        return versionInfoText;
    }

    public Mesh getWieldHandMesh(){
        return wieldHandMesh;
    }

    public Mesh getButtonMesh(){
        return buttonMesh;
    }
    public Mesh getButtonSelectedMesh(){
        return buttonSelectedMesh;
    }

    public Mesh getTextInputMesh(){
        return textInputMesh;
    }
    public Mesh getTextInputSelectedMesh(){
        return textInputSelectedMesh;
    }

    public Mesh getButtonPushedMesh(){
        return buttonPushedMesh;
    }
    public Mesh getMenuBgMesh(){
        return menuBgMesh;
    }

    public Mesh getInventorySlotMesh(){
        return inventorySlotMesh;
    }
    public Mesh getInventorySlotSelectedMesh(){
        return inventorySlotSelectedMesh;
    }

    public Mesh getMiningCrackMesh(byte diggingFrame){
        return miningCrackMesh[diggingFrame];
    }

    public Mesh getGlobalWaterEffectMesh(){
        return globalWaterEffectMesh;
    }

    private Mesh createPlayerMesh(){
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

        positions.clear();
        light.clear();
        indices.clear();
        textureCoord.clear();

        return new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, playerTexture);
    }

    public Mesh buildMiningMesh(int level) {
        HyperFloatArray positions = new HyperFloatArray(12);
        HyperFloatArray textureCoord = new HyperFloatArray(6);
        HyperIntArray indices = new HyperIntArray(4);
        HyperFloatArray light = new HyperFloatArray(12);

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

        positions.clear();
        light.clear();
        indices.clear();
        textureCoord.clear();

        return new Mesh(positions.values(), light.values(), indices.values(), textureCoord.values(), miningCrack);
    }

    private Mesh createWorldSelectionMesh() {

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

        return new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, worldSelectionTexture);
    }

    private Mesh buildHandMesh(){

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

        positions.clear();
        light.clear();
        indices.clear();
        textureCoord.clear();

        return new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, playerTexture);
    }

    private float[] calculateTexture(int xMin, int yMin, int xMax, int yMax){
        float[] texturePoints = new float[4];

        float PLAYER_WIDTH = 64f;
        texturePoints[0] = (float)xMin/ PLAYER_WIDTH; //min x (-)
        texturePoints[1] = (float)xMax/ PLAYER_WIDTH; //max x (+)

        float PLAYER_HEIGHT = 32f;
        texturePoints[2] = (float)yMin/ PLAYER_HEIGHT; //min y (-)
        texturePoints[3] = (float)yMax/ PLAYER_HEIGHT; //max y (+)
        return texturePoints;
    }
}
