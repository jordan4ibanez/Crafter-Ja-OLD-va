package game.item;

import engine.graph.Mesh;
import engine.graph.Texture;
import game.blocks.BlockShape;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static game.blocks.BlockDefinition.*;
import static game.chunk.ChunkMesh.getTextureAtlas;

public class ItemDefinition {
    private final static float itemSize   = 0.4f;
    private final static Map<String, ItemDefinition> definitions = new HashMap<>();

    public final String name;
    public final int blockID;
    public final Mesh mesh;
    public final boolean isTool;
    public final ItemModifier itemModifier;

    public ItemDefinition(String name, int blockID){
        this.name = name;
        this.blockID = blockID;
        this.mesh = createBlockObjectMesh(blockID);
        this.isTool = false;
        this.itemModifier = null;
    }

    public ItemDefinition(String name, String texturePath, ItemModifier itemModifier){
        this.name = name;
        this.blockID = -1;
        this.mesh = createItemObjectMesh(texturePath);
        this.isTool = true;
        this.itemModifier = itemModifier;
    }

    public static ItemModifier getItemModifier(String name){
        return definitions.get(name).itemModifier;
    }

    public static void registerItem(String name, int blockID){
        definitions.put(name, new ItemDefinition(name, blockID));
    }

    public static void registerItem(String name, String texturePath, ItemModifier itemModifier){
        definitions.put(name, new ItemDefinition(name, texturePath, itemModifier));
    }

    public static ItemDefinition getItemDefinition(String name){
        return definitions.get(name);
    }

    public static Mesh createBlockObjectMesh(int blockID) {
        int indicesCount = 0;

        ArrayList positions     = new ArrayList();
        ArrayList textureCoord  = new ArrayList();
        ArrayList indices       = new ArrayList();
        ArrayList light         = new ArrayList();

        //create the mesh

        float thisLight = 1f;//(float)Math.pow(Math.pow(15,1.5),1.5);



        for (float[] thisBlockBox : getBlockShape(blockID, (byte) 0)) {
            //front
            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add((thisBlockBox[4] - 0.5f) * itemSize + (itemSize/2));
            positions.add((thisBlockBox[5] - 0.5f) * itemSize);

            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[5] - 0.5f) * itemSize);

            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[5] - 0.5f) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[5] - 0.5f) * itemSize);

            //front
            for (int i = 0; i < 12; i++) {
                light.add(thisLight);
            }
            //front
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;

            float[] textureFront = getFrontTexturePoints(blockID, (byte) 0);

            //front
            textureCoord.add(textureFront[1] - ((1-thisBlockBox[3])/32f)); //x positive
            textureCoord.add(textureFront[2] + ((1-thisBlockBox[4])/32f)); //y positive
            textureCoord.add(textureFront[0] - ((0-thisBlockBox[0])/32f)); //x negative
            textureCoord.add(textureFront[2] + ((1-thisBlockBox[4])/32f)); //y positive

            textureCoord.add(textureFront[0] - ((0-thisBlockBox[0])/32f)); //x negative
            textureCoord.add(textureFront[3] - ((thisBlockBox[1])/32f));   //y negative
            textureCoord.add(textureFront[1] - ((1-thisBlockBox[3])/32f)); //x positive
            textureCoord.add(textureFront[3] - ((thisBlockBox[1])/32f));   //y negative



            //back
            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[2] - 0.5f) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[2] - 0.5f) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[2] - 0.5f) * itemSize);

            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[2] - 0.5f) * itemSize);


            //back
            for (int i = 0; i < 12; i++) {
                light.add(thisLight);
            }
            //back
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;

            float[] textureBack = getBackTexturePoints(blockID,(byte) 0);

            //back
            textureCoord.add(textureBack[1] - ((1-thisBlockBox[0])/32f));
            textureCoord.add(textureBack[2] + ((1-thisBlockBox[4])/32f));
            textureCoord.add(textureBack[0] - ((0-thisBlockBox[3])/32f));
            textureCoord.add(textureBack[2] + ((1-thisBlockBox[4])/32f));

            textureCoord.add(textureBack[0] - ((0-thisBlockBox[3])/32f));
            textureCoord.add(textureBack[3] - ((  thisBlockBox[1])/32f));
            textureCoord.add(textureBack[1] - ((1-thisBlockBox[0])/32f));
            textureCoord.add(textureBack[3] - ((  thisBlockBox[1])/32f));





            //right
            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[2] - 0.5f) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[5] - 0.5f) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[5] - 0.5f) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[2] - 0.5f) * itemSize);

            //right
            for (int i = 0; i < 12; i++) {
                light.add(thisLight);
            }
            //right
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;



            float[] textureRight = getRightTexturePoints(blockID,(byte) 0);
            //right
            textureCoord.add(textureRight[1] - ((1-thisBlockBox[2])/32f));
            textureCoord.add(textureRight[2] + ((1-thisBlockBox[4])/32f));
            textureCoord.add(textureRight[0] - ((0-thisBlockBox[5])/32f));
            textureCoord.add(textureRight[2] + ((1-thisBlockBox[4])/32f));

            textureCoord.add(textureRight[0] - ((0-thisBlockBox[5])/32f));
            textureCoord.add(textureRight[3] - ((  thisBlockBox[1])/32f));
            textureCoord.add(textureRight[1] - ((1-thisBlockBox[2])/32f));
            textureCoord.add(textureRight[3] - ((  thisBlockBox[1])/32f));




            //left
            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[5] - 0.5f) * itemSize);

            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[2] - 0.5f) * itemSize);

            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[2] - 0.5f) * itemSize);

            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[5] - 0.5f) * itemSize);

            //left
            for (int i = 0; i < 12; i++) {
                light.add(thisLight);
            }
            //left
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;

            float[] textureLeft = getLeftTexturePoints(blockID,(byte) 0);
            //left
            textureCoord.add(textureLeft[1] - ((1-thisBlockBox[5])/32f));
            textureCoord.add(textureLeft[2] + ((1-thisBlockBox[4])/32f));
            textureCoord.add(textureLeft[0] - ((0-thisBlockBox[2])/32f));
            textureCoord.add(textureLeft[2] + ((1-thisBlockBox[4])/32f));

            textureCoord.add(textureLeft[0] - ((0-thisBlockBox[2])/32f));
            textureCoord.add(textureLeft[3] - ((thisBlockBox[1])/32f));
            textureCoord.add(textureLeft[1] - ((1-thisBlockBox[5])/32f));
            textureCoord.add(textureLeft[3] - ((thisBlockBox[1])/32f));




            //top
            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[2] - 0.5f) * itemSize);

            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[5] - 0.5f) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[5] - 0.5f) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[2] - 0.5f) * itemSize);

            //top
            for (int i = 0; i < 12; i++) {
                light.add(thisLight);
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

            float[] textureTop = getTopTexturePoints(blockID);
            //top
            textureCoord.add(textureTop[1] - ((1-thisBlockBox[5])/32f));
            textureCoord.add(textureTop[2] + ((1-thisBlockBox[0])/32f));
            textureCoord.add(textureTop[0] - ((0-thisBlockBox[2])/32f));
            textureCoord.add(textureTop[2] + ((1-thisBlockBox[0])/32f));

            textureCoord.add(textureTop[0] - ((0-thisBlockBox[2])/32f));
            textureCoord.add(textureTop[3] - ((  thisBlockBox[3])/32f));
            textureCoord.add(textureTop[1] - ((1-thisBlockBox[5])/32f));
            textureCoord.add(textureTop[3] - ((  thisBlockBox[3])/32f));



            //bottom
            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[5] - 0.5f) * itemSize);

            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[2] - 0.5f) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[2] - 0.5f) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2));
            positions.add((thisBlockBox[5] - 0.5f) * itemSize);

            //bottom
            for (int i = 0; i < 12; i++) {
                light.add(thisLight);
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

            float[] textureBottom = getBottomTexturePoints(blockID);
            //bottom
            textureCoord.add(textureBottom[1] - ((1-thisBlockBox[5])/32f));
            textureCoord.add(textureBottom[2] + ((1-thisBlockBox[0])/32f));
            textureCoord.add(textureBottom[0] - ((0-thisBlockBox[2])/32f));
            textureCoord.add(textureBottom[2] + ((1-thisBlockBox[0])/32f));

            textureCoord.add(textureBottom[0] - ((0-thisBlockBox[2])/32f));
            textureCoord.add(textureBottom[3] - ((  thisBlockBox[3])/32f));
            textureCoord.add(textureBottom[1] - ((1-thisBlockBox[5])/32f));
            textureCoord.add(textureBottom[3] - ((  thisBlockBox[3])/32f));

        }
        //todo: ------------------------------------------------------------------------------------------------=-=-=-=


        //convert the position objects into usable array
        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i] = (float)positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = (float)light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = (int)indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = (float)textureCoord.get(i);
        }

       return new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, getTextureAtlas());
    }


    public static Mesh createItemObjectMesh(String texturePath){
        int indicesCount = 0;

        ArrayList positions     = new ArrayList();
        ArrayList textureCoord  = new ArrayList();
        ArrayList indices       = new ArrayList();
        ArrayList light         = new ArrayList();

        //create the mesh

        float thisLight = 1f;//(float)Math.pow(Math.pow(15,1.5),1.5);


        float[][] pixels = new float[16*16][6];
        boolean[] render = new boolean[16*16];

        BufferedImage bufferboi = null;
        try {
            bufferboi = ImageIO.read(new File(texturePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

         for (int x = 0; x < 16; x++){
             for (int y = 0; y < 16; y++){
                 pixels[(x*16)+y] = new float[]{(float)x/16f, (float)y/16f, -0.05f, ((float)x+1f)/16f, ((float)y+1f)/16f, 0.05f};

                 int pixel = bufferboi.getRGB(x,y);

                 if( (pixel>>24) == 0x00 ) {
                     render[(x*16)+y] = false;
                 } else {
                     render[(x*16)+y] = true;
                 }
             }
         }

         int pixelCount = 0;

        for (float[] thisBlockBox : pixels) {
            if (!render[pixelCount]){
                pixelCount++;
                continue;
            }
            pixelCount++;
            //front
            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add(Math.abs(1f - ((thisBlockBox[1] - 0.5f) * itemSize + (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[5]) * itemSize);

            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add(Math.abs(1f - ((thisBlockBox[1] - 0.5f) * itemSize + (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[5]) * itemSize);

            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add(Math.abs(1f - ((thisBlockBox[4] - 0.5f) * itemSize + (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[5]) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add(Math.abs(1f - ((thisBlockBox[4] - 0.5f) * itemSize + (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[5] ) * itemSize);

            //front
            for (int i = 0; i < 12; i++) {
                light.add(thisLight);
            }
            //front
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
            //front
            textureCoord.add(thisBlockBox[0]); //x positive
            textureCoord.add(thisBlockBox[1]); //y positive
            textureCoord.add(thisBlockBox[3]); //x negative
            textureCoord.add(thisBlockBox[1]); //y positive
            textureCoord.add(thisBlockBox[3]); //x negative
            textureCoord.add(thisBlockBox[4]); //y negative
            textureCoord.add(thisBlockBox[0]); //x positive
            textureCoord.add(thisBlockBox[4]); //y negative




            //back
            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add(Math.abs( 1f - ((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[2]) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add(Math.abs( 1f - ((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[2]) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add(Math.abs( 1f - ((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[2]) * itemSize);

            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add(Math.abs( 1f - ((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[2]) * itemSize);


            //back
            for (int i = 0; i < 12; i++) {
                light.add(thisLight);
            }
            //back
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;

            //back
            textureCoord.add(thisBlockBox[0]); //x positive
            textureCoord.add(thisBlockBox[1]); //y positive
            textureCoord.add(thisBlockBox[3]); //x negative
            textureCoord.add(thisBlockBox[1]); //y positive
            textureCoord.add(thisBlockBox[3]); //x negative
            textureCoord.add(thisBlockBox[4]); //y negative
            textureCoord.add(thisBlockBox[0]); //x positive
            textureCoord.add(thisBlockBox[4]); //y negative





            //right
            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add(Math.abs(1f - ((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[2] ) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add(Math.abs(1f - ((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[5]) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add(Math.abs(1f - ((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[5] ) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add(Math.abs(1f - ((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[2] ) * itemSize);

            //right
            for (int i = 0; i < 12; i++) {
                light.add(thisLight);
            }
            //right
            indices.add(0 + indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);
            indicesCount += 4;


            //right
            textureCoord.add(thisBlockBox[0]); //x positive
            textureCoord.add(thisBlockBox[1]); //y positive
            textureCoord.add(thisBlockBox[3]); //x negative
            textureCoord.add(thisBlockBox[1]); //y positive
            textureCoord.add(thisBlockBox[3]); //x negative
            textureCoord.add(thisBlockBox[4]); //y negative
            textureCoord.add(thisBlockBox[0]); //x positive
            textureCoord.add(thisBlockBox[4]); //y negative





            //left
            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add(Math.abs( 1f - ((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[5]) * itemSize);

            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add(Math.abs( 1f - ((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[2] ) * itemSize);

            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add(Math.abs( 1f - ((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[2]) * itemSize);

            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add(Math.abs( 1f - ((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[5] ) * itemSize);




            //left
            for (int i = 0; i < 12; i++) {
                light.add(thisLight);
            }
            //left
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

            //left
            textureCoord.add(thisBlockBox[3]); //x+
            textureCoord.add(thisBlockBox[1]); //y-
            textureCoord.add(thisBlockBox[0]); //x-
            textureCoord.add(thisBlockBox[1]); //y-

            textureCoord.add(thisBlockBox[0]); //x-
            textureCoord.add(thisBlockBox[4]); //y+
            textureCoord.add(thisBlockBox[3]); //x+
            textureCoord.add(thisBlockBox[4]); //y+



            //top
            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add(Math.abs( 1f - ((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[2] ) * itemSize);

            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add(Math.abs( 1f - ((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[5]) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add(Math.abs( 1f - ((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[5]) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add(Math.abs( 1f - ((thisBlockBox[1] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[2]) * itemSize);

            //top
            for (int i = 0; i < 12; i++) {
                light.add(thisLight);
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

            //top
            textureCoord.add(thisBlockBox[3]); //x positive
            textureCoord.add(thisBlockBox[4]); //y positive
            textureCoord.add(thisBlockBox[0]); //x negative
            textureCoord.add(thisBlockBox[4]); //y positive
            textureCoord.add(thisBlockBox[0]); //x negative
            textureCoord.add(thisBlockBox[1]);   //y negative
            textureCoord.add(thisBlockBox[3]); //x positive
            textureCoord.add(thisBlockBox[1]);   //y negative



            //bottom
            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add(Math.abs( 1f - ((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[5] ) * itemSize);

            positions.add((thisBlockBox[0] - 0.5f) * itemSize);
            positions.add(Math.abs( 1f - ((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[2] ) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add(Math.abs( 1f - ((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[2] ) * itemSize);

            positions.add((thisBlockBox[3] - 0.5f) * itemSize);
            positions.add(Math.abs( 1f - ((thisBlockBox[4] - 0.5f) * itemSize+ (itemSize/2))) - (itemSize*1.5f));
            positions.add((thisBlockBox[5]) * itemSize);

            //bottom
            for (int i = 0; i < 12; i++) {
                light.add(thisLight);
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

            //bottom
            textureCoord.add(thisBlockBox[3]); //x positive
            textureCoord.add(thisBlockBox[4]); //y positive
            textureCoord.add(thisBlockBox[0]); //x negative
            textureCoord.add(thisBlockBox[4]); //y positive
            textureCoord.add(thisBlockBox[0]); //x negative
            textureCoord.add(thisBlockBox[1]);   //y negative
            textureCoord.add(thisBlockBox[3]); //x positive
            textureCoord.add(thisBlockBox[1]);   //y negative


        }
        //todo: ------------------------------------------------------------------------------------------------=-=-=-=


        //convert the position objects into usable array
        float[] positionsArray = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i] = (float)positions.get(i);
        }

        //convert the light objects into usable array
        float[] lightArray = new float[light.size()];
        for (int i = 0; i < light.size(); i++) {
            lightArray[i] = (float)light.get(i);
        }

        //convert the indices objects into usable array
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = (int)indices.get(i);
        }

        //convert the textureCoord objects into usable array
        float[] textureCoordArray = new float[textureCoord.size()];
        for (int i = 0; i < textureCoord.size(); i++) {
            textureCoordArray[i] = (float)textureCoord.get(i);
        }


        Texture thisTexture = null;
        try {
            thisTexture = new Texture(texturePath);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, thisTexture);
    }

    public static ItemDefinition getRandomItemDefinition(){
        Object[] definitionsArray = definitions.values().toArray();
        int thisItem = (int)Math.floor(Math.random() * definitionsArray.length);
        return (ItemDefinition)definitionsArray[thisItem];
    }

    public static void cleanUp(){
        for (ItemDefinition thisDefinition : definitions.values()){
            if (thisDefinition.mesh != null){
                thisDefinition.mesh.cleanUp(true);
            }
        }
    }
}
