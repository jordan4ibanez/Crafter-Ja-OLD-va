package game.mob;

import engine.graphics.Mesh;
import engine.graphics.Texture;

import java.util.ArrayList;

public class MobMeshBuilder {

    public static Mesh[] createMobMesh(float[][] modelPieceArray, float[][] textureArrayArray, String texturePath){


        Mesh[] bodyMeshes = new Mesh[modelPieceArray.length];
        int bodyMeshesIndex = 0;
        int textureCounter = 0;

        for (float[] thisBlockBox : modelPieceArray) {
            ArrayList<Float> positions = new ArrayList<>();
            ArrayList<Float> textureCoord = new ArrayList<>();
            ArrayList<Integer> indices = new ArrayList<>();
            ArrayList<Float> light = new ArrayList<>();

            int indicesCount = 0;
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
            indices.add(0);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(0);
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
            indices.add(indicesCount);
            indices.add(1 + indicesCount);
            indices.add(2 + indicesCount);
            indices.add(indicesCount);
            indices.add(2 + indicesCount);
            indices.add(3 + indicesCount);


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

            Texture playerTexture = null;
            try {
                playerTexture = new Texture(texturePath);
            } catch (Exception e) {
                e.printStackTrace();
            }


            bodyMeshes[bodyMeshesIndex] = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, playerTexture);
            bodyMeshesIndex++;

        }

        return bodyMeshes;
    }

    public static float[] calculateMobTexture(int xMin, int yMin, int xMax, int yMax, float textureWidth, float textureHeight){
        float[] texturePoints = new float[4];

        texturePoints[0] = (float)xMin/textureWidth; //min x (-)
        texturePoints[1] = (float)xMax/textureWidth; //max x (+)

        texturePoints[2] = (float)yMin/textureHeight; //min y (-)
        texturePoints[3] = (float)yMax/textureHeight; //max y (+)
        return texturePoints;
    }
}
