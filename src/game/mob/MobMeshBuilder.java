package game.mob;

import engine.graphics.Mesh;
import engine.highPerformanceContainers.MicroFloatArray;
import engine.highPerformanceContainers.MicroIntArray;

import static engine.graphics.Texture.createTexture;

public class MobMeshBuilder {

    public static Mesh[] createMobMesh(float[][][] modelPieceArray, float[][][] textureArrayArray, String texturePath){


        Mesh[] bodyMeshes = new Mesh[modelPieceArray.length];

        int bodyMeshesIndex = 0; //this is the float[THISPART] which holds the float[THISPART]{x,y,z,x,y,z}

        //allow multiple meshes to be welded together
        for (float[][] thisModelSegment : modelPieceArray) {

            MicroFloatArray positions    = new MicroFloatArray(12);
            MicroFloatArray textureCoord = new MicroFloatArray(8);
            MicroIntArray   indices      = new MicroIntArray(6);
            MicroFloatArray light        = new MicroFloatArray(12);

            int indicesCount = 0;
            int textureCounter = 0;

            for (float[] thisBlockBox : thisModelSegment) {

                //back
                positions.pack(thisBlockBox[3], thisBlockBox[4], thisBlockBox[5], thisBlockBox[0], thisBlockBox[4], thisBlockBox[5], thisBlockBox[0], thisBlockBox[1], thisBlockBox[5], thisBlockBox[3], thisBlockBox[1], thisBlockBox[5]);
                light.pack(1f);
                indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);
                indicesCount += 4;
                float[] textureFront = textureArrayArray[bodyMeshesIndex][textureCounter];
                textureCoord.pack(textureFront[1], textureFront[2], textureFront[0], textureFront[2], textureFront[0], textureFront[3], textureFront[1], textureFront[3]);
                textureCounter++;

                //front
                positions.pack(thisBlockBox[0], thisBlockBox[4], thisBlockBox[2], thisBlockBox[3], thisBlockBox[4], thisBlockBox[2], thisBlockBox[3], thisBlockBox[1], thisBlockBox[2], thisBlockBox[0], thisBlockBox[1], thisBlockBox[2]);
                light.pack(1f);
                indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);
                indicesCount += 4;
                float[] textureBack = textureArrayArray[bodyMeshesIndex][textureCounter];
                textureCoord.pack(textureBack[1], textureBack[2], textureBack[0], textureBack[2], textureBack[0], textureBack[3], textureBack[1], textureBack[3]);
                textureCounter++;

                //right
                positions.pack(thisBlockBox[3], thisBlockBox[4], thisBlockBox[2], thisBlockBox[3], thisBlockBox[4], thisBlockBox[5], thisBlockBox[3], thisBlockBox[1], thisBlockBox[5], thisBlockBox[3], thisBlockBox[1], thisBlockBox[2]);
                light.pack(1f);
                indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);
                indicesCount += 4;
                float[] textureRight = textureArrayArray[bodyMeshesIndex][textureCounter];
                textureCoord.pack(textureRight[1], textureRight[2], textureRight[0], textureRight[2], textureRight[0], textureRight[3], textureRight[1], textureRight[3]);
                textureCounter++;

                //left
                positions.pack(thisBlockBox[0], thisBlockBox[4], thisBlockBox[5], thisBlockBox[0], thisBlockBox[4], thisBlockBox[2], thisBlockBox[0], thisBlockBox[1], thisBlockBox[2], thisBlockBox[0], thisBlockBox[1], thisBlockBox[5]);
                light.pack(1f);
                indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);
                indicesCount += 4;
                float[] textureLeft = textureArrayArray[bodyMeshesIndex][textureCounter];
                textureCoord.pack(textureLeft[1], textureLeft[2], textureLeft[0], textureLeft[2], textureLeft[0], textureLeft[3], textureLeft[1], textureLeft[3]);
                textureCounter++;

                //top
                positions.pack(thisBlockBox[0], thisBlockBox[4], thisBlockBox[2], thisBlockBox[0], thisBlockBox[4], thisBlockBox[5], thisBlockBox[3], thisBlockBox[4], thisBlockBox[5], thisBlockBox[3], thisBlockBox[4], thisBlockBox[2]);
                light.pack(1f);
                indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);
                indicesCount += 4;
                float[] textureTop = textureArrayArray[bodyMeshesIndex][textureCounter];
                textureCoord.pack(textureTop[1], textureTop[2], textureTop[0], textureTop[2], textureTop[0], textureTop[3], textureTop[1], textureTop[3]);
                textureCounter++;

                //bottom
                positions.pack(thisBlockBox[0], thisBlockBox[1], thisBlockBox[5], thisBlockBox[0], thisBlockBox[1], thisBlockBox[2], thisBlockBox[3], thisBlockBox[1], thisBlockBox[2], thisBlockBox[3], thisBlockBox[1], thisBlockBox[5]);
                light.pack(1f);
                indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);
                indicesCount += 4;
                float[] textureBottom = textureArrayArray[bodyMeshesIndex][textureCounter];
                textureCoord.pack(textureBottom[1], textureBottom[2], textureBottom[0], textureBottom[2], textureBottom[0], textureBottom[3], textureBottom[1], textureBottom[3]);
                textureCounter++;
            }

            int playerTexture = createTexture(texturePath);

            bodyMeshes[bodyMeshesIndex] = new Mesh(positions.values(), light.values(), indices.values(), textureCoord.values(), playerTexture);

            positions.clear();

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
