package game.mainMenu;

import engine.graphics.Mesh;
import engine.graphics.Texture;

import java.util.ArrayList;

public class MainMenuAssets {

    private static Mesh titleBlockMesh;

    private static Mesh titleBackGroundMeshTile;


    public static Mesh getTitleBlockMesh(){
        return titleBlockMesh;
    }

    public static Mesh getTitleBackGroundMeshTile(){
        return titleBackGroundMeshTile;
    }

    //this is reused garbage because I'm too busy to turn it into an internal API
    public static void createMenuMenuTitleBlock() {

        Texture titleScreenBlockTexture = new Texture("textures/title_screen_block.png");

        float sideLight = 0.6f;

        float min = 0f;
        float max = 1f;
        int indicesCount = 0;

        ArrayList<Float> positions = new ArrayList<>();
        ArrayList<Float> textureCoord = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();
        ArrayList<Float> light = new ArrayList<>();

        float textureMin = 0;
        float textureMax = 1;

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
        indices.add(    indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(    indicesCount);
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
        indices.add(    indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(    indicesCount);
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

        //right
        for (int i = 0; i < 12; i++) {
            light.add(sideLight);
        }
        //right
        indices.add(    indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(    indicesCount);
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
        //left
        for (int i = 0; i < 12; i++) {
            light.add(sideLight);
        }
        //left
        indices.add(    indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(    indicesCount);
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
        //top
        for (int i = 0; i < 12; i++) {
            light.add(sideLight);
        }
        //top
        indices.add(    indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(    indicesCount);
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
        for (int i = 0; i < 12; i++) {
            light.add(sideLight);
        }
        //bottom
        indices.add(    indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(    indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);


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

        titleBlockMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, titleScreenBlockTexture);
    }

    public static void createMainMenuBackGroundTile() {
        ArrayList<Float> positions = new ArrayList<>();
        ArrayList<Float> textureCoord = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();
        ArrayList<Float> light = new ArrayList<>();


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
        float frontLight = 0.4f;//getLight(x, y, z + 1, chunkX, chunkZ) / maxLight;

        //front
        for (int i = 0; i < 12; i++) {
            light.add(frontLight);
        }
        //front
        indices.add(    indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(    indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);

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

        titleBackGroundMeshTile = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, new Texture("textures/title_screen_background.png"));
    }
}
