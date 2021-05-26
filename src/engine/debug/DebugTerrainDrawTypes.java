package engine.debug;

import engine.graphics.Mesh;

import static game.blocks.BlockDefinition.*;
import static game.chunk.ChunkMesh.getTextureAtlas;

public class DebugTerrainDrawTypes {

    private static final int[] debugChunkDataArray = new int[16^3];

    private static Mesh debugMesh;

    public static Mesh getDebugMesh(){
        return debugMesh;
    }

    //horrible way to debug draw types
    public static void generateDebugChunkMesh() {
        //long startTime = System.nanoTime();

        //allFaces block stuff

        final float[] allFacesPositions = new float[550_824];
        int allFacesPositionsCount = 0;

        final float[] allFacesTextureCoord = new float[550_824];
        int allFacesTextureCoordCount = 0;

        final int[] allFacesIndices = new int[550_824];
        int allFacesIndicesTableCount = 0;
        int allFacesIndicesCount = 0;

        final float[] allFacesLight = new float[550_824];
        int allFacesLightCount = 0;

        //currently debugging leaves
        int thisBlock = 26;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 16; y++) {
                    {
                        //front
                        allFacesPositions[allFacesPositionsCount + 0] = (1f + x);
                        allFacesPositions[allFacesPositionsCount + 1] = (1f + y);
                        allFacesPositions[allFacesPositionsCount + 2] = (1f + z);

                        allFacesPositions[allFacesPositionsCount + 3] = (0f + x);
                        allFacesPositions[allFacesPositionsCount + 4] = (1f + y);
                        allFacesPositions[allFacesPositionsCount + 5] = (1f + z);

                        allFacesPositions[allFacesPositionsCount + 6] = (0f + x);
                        allFacesPositions[allFacesPositionsCount + 7] = (0f + y);
                        allFacesPositions[allFacesPositionsCount + 8] = (1f + z);

                        allFacesPositions[allFacesPositionsCount + 9] = (1f + x);
                        allFacesPositions[allFacesPositionsCount + 10] = (0f + y);
                        allFacesPositions[allFacesPositionsCount + 11] = (1f + z);

                        allFacesPositionsCount += 12;

                        //front
                        float frontLight = 1f;

                        //front
                        for (int i = 0; i < 12; i++) {
                            allFacesLight[i + allFacesLightCount] = (frontLight);
                        }

                        allFacesLightCount += 12;


                        //front
                        allFacesIndices[allFacesIndicesTableCount + 0] = (0 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 1] = (1 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 2] = (2 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 3] = (0 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 4] = (2 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 5] = (3 + allFacesIndicesCount);

                        allFacesIndicesCount += 4;
                        allFacesIndicesTableCount += 6;

                        float[] textureFront = getFrontTexturePoints(thisBlock, (byte)0);
                        //front
                        allFacesTextureCoord[allFacesTextureCoordCount + 0] = (textureFront[1]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 1] = (textureFront[2]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 2] = (textureFront[0]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 3] = (textureFront[2]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 4] = (textureFront[0]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 5] = (textureFront[3]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 6] = (textureFront[1]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 7] = (textureFront[3]);
                        allFacesTextureCoordCount += 8;
                    }

                    {
                        //back
                        allFacesPositions[allFacesPositionsCount + 0] = (0f + x);
                        allFacesPositions[allFacesPositionsCount + 1] = (1f + y);
                        allFacesPositions[allFacesPositionsCount + 2] = (0f + z);

                        allFacesPositions[allFacesPositionsCount + 3] = (1f + x);
                        allFacesPositions[allFacesPositionsCount + 4] = (1f + y);
                        allFacesPositions[allFacesPositionsCount + 5] = (0f + z);

                        allFacesPositions[allFacesPositionsCount + 6] = (1f + x);
                        allFacesPositions[allFacesPositionsCount + 7] = (0f + y);
                        allFacesPositions[allFacesPositionsCount + 8] = (0f + z);

                        allFacesPositions[allFacesPositionsCount + 9] = (0f + x);
                        allFacesPositions[allFacesPositionsCount + 10] = (0f + y);
                        allFacesPositions[allFacesPositionsCount + 11] = (0f + z);

                        allFacesPositionsCount += 12;

                        //back
                        float backLight = 1f;

                        //back
                        for (int i = 0; i < 12; i++) {
                            allFacesLight[i + allFacesLightCount] = (backLight);
                        }

                        allFacesLightCount += 12;

                        //back
                        allFacesIndices[allFacesIndicesTableCount + 0] = (0 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 1] = (1 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 2] = (2 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 3] = (0 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 4] = (2 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 5] = (3 + allFacesIndicesCount);
                        allFacesIndicesCount += 4;
                        allFacesIndicesTableCount += 6;

                        float[] textureBack = getBackTexturePoints(thisBlock, (byte)0);
                        //back
                        allFacesTextureCoord[allFacesTextureCoordCount + 0] = (textureBack[1]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 1] = (textureBack[2]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 2] = (textureBack[0]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 3] = (textureBack[2]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 4] = (textureBack[0]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 5] = (textureBack[3]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 6] = (textureBack[1]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 7] = (textureBack[3]);
                        allFacesTextureCoordCount += 8;
                    }

                    {
                        //right
                        allFacesPositions[allFacesPositionsCount + 0] = (1f + x);
                        allFacesPositions[allFacesPositionsCount + 1] = (1f + y);
                        allFacesPositions[allFacesPositionsCount + 2] = (0f + z);

                        allFacesPositions[allFacesPositionsCount + 3] = (1f + x);
                        allFacesPositions[allFacesPositionsCount + 4] = (1f + y);
                        allFacesPositions[allFacesPositionsCount + 5] = (1f + z);

                        allFacesPositions[allFacesPositionsCount + 6] = (1f + x);
                        allFacesPositions[allFacesPositionsCount + 7] = (0f + y);
                        allFacesPositions[allFacesPositionsCount + 8] = (1f + z);

                        allFacesPositions[allFacesPositionsCount + 9] = (1f + x);
                        allFacesPositions[allFacesPositionsCount + 10] = (0f + y);
                        allFacesPositions[allFacesPositionsCount + 11] = (0f + z);

                        allFacesPositionsCount += 12;

                        //right
                        float rightLight = 1f;
                        //right
                        for (int i = 0; i < 12; i++) {
                            allFacesLight[i + allFacesLightCount] = (rightLight);
                        }

                        allFacesLightCount += 12;

                        //right
                        allFacesIndices[allFacesIndicesTableCount + 0] = (0 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 1] = (1 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 2] = (2 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 3] = (0 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 4] = (2 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 5] = (3 + allFacesIndicesCount);
                        allFacesIndicesCount += 4;
                        allFacesIndicesTableCount += 6;

                        float[] textureRight = getRightTexturePoints(thisBlock, (byte)0);
                        //right
                        allFacesTextureCoord[allFacesTextureCoordCount + 0] = (textureRight[1]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 1] = (textureRight[2]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 2] = (textureRight[0]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 3] = (textureRight[2]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 4] = (textureRight[0]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 5] = (textureRight[3]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 6] = (textureRight[1]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 7] = (textureRight[3]);
                        allFacesTextureCoordCount += 8;
                    }

                    {
                        //left
                        allFacesPositions[allFacesPositionsCount + 0] = (0f + x);
                        allFacesPositions[allFacesPositionsCount + 1] = (1f + y);
                        allFacesPositions[allFacesPositionsCount + 2] = (1f + z);

                        allFacesPositions[allFacesPositionsCount + 3] = (0f + x);
                        allFacesPositions[allFacesPositionsCount + 4] = (1f + y);
                        allFacesPositions[allFacesPositionsCount + 5] = (0f + z);

                        allFacesPositions[allFacesPositionsCount + 6] = (0f + x);
                        allFacesPositions[allFacesPositionsCount + 7] = (0f + y);
                        allFacesPositions[allFacesPositionsCount + 8] = (0f + z);

                        allFacesPositions[allFacesPositionsCount + 9] = (0f + x);
                        allFacesPositions[allFacesPositionsCount + 10] = (0f + y);
                        allFacesPositions[allFacesPositionsCount + 11] = (1f + z);

                        allFacesPositionsCount += 12;

                        //left
                        float leftLight = 1f;

                        //left
                        for (int i = 0; i < 12; i++) {
                            allFacesLight[i + allFacesLightCount] = (leftLight);
                        }

                        allFacesLightCount += 12;

                        //left
                        allFacesIndices[allFacesIndicesTableCount + 0] = (0 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 1] = (1 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 2] = (2 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 3] = (0 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 4] = (2 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 5] = (3 + allFacesIndicesCount);
                        allFacesIndicesCount += 4;
                        allFacesIndicesTableCount += 6;

                        float[] textureLeft = getLeftTexturePoints(thisBlock, (byte)0);
                        //left
                        allFacesTextureCoord[allFacesTextureCoordCount + 0] = (textureLeft[1]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 1] = (textureLeft[2]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 2] = (textureLeft[0]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 3] = (textureLeft[2]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 4] = (textureLeft[0]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 5] = (textureLeft[3]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 6] = (textureLeft[1]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 7] = (textureLeft[3]);
                        allFacesTextureCoordCount += 8;
                    }

                    {
                        //top
                        allFacesPositions[allFacesPositionsCount + 0] = (0f + x);
                        allFacesPositions[allFacesPositionsCount + 1] = (1f + y);
                        allFacesPositions[allFacesPositionsCount + 2] = (0f + z);

                        allFacesPositions[allFacesPositionsCount + 3] = (0f + x);
                        allFacesPositions[allFacesPositionsCount + 4] = (1f + y);
                        allFacesPositions[allFacesPositionsCount + 5] = (1f + z);

                        allFacesPositions[allFacesPositionsCount + 6] = (1f + x);
                        allFacesPositions[allFacesPositionsCount + 7] = (1f + y);
                        allFacesPositions[allFacesPositionsCount + 8] = (1f + z);

                        allFacesPositions[allFacesPositionsCount + 9] = (1f + x);
                        allFacesPositions[allFacesPositionsCount + 10] = (1f + y);
                        allFacesPositions[allFacesPositionsCount + 11] = (0f + z);

                        allFacesPositionsCount += 12;

                        //top
                        float topLight = 1f;

                        //top
                        for (int i = 0; i < 12; i++) {
                            allFacesLight[i + allFacesLightCount] = (topLight);
                        }

                        allFacesLightCount += 12;

                        //top
                        allFacesIndices[allFacesIndicesTableCount + 0] = (0 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 1] = (1 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 2] = (2 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 3] = (0 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 4] = (2 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 5] = (3 + allFacesIndicesCount);
                        allFacesIndicesCount += 4;
                        allFacesIndicesTableCount += 6;

                        float[] textureTop = getTopTexturePoints(thisBlock);
                        //top
                        allFacesTextureCoord[allFacesTextureCoordCount + 0] = (textureTop[1]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 1] = (textureTop[2]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 2] = (textureTop[0]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 3] = (textureTop[2]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 4] = (textureTop[0]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 5] = (textureTop[3]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 6] = (textureTop[1]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 7] = (textureTop[3]);
                        allFacesTextureCoordCount += 8;

                    }


                    {
                        //bottom
                        allFacesPositions[allFacesPositionsCount + 0] = (0f + x);
                        allFacesPositions[allFacesPositionsCount + 1] = (0f + y);
                        allFacesPositions[allFacesPositionsCount + 2] = (1f + z);

                        allFacesPositions[allFacesPositionsCount + 3] = (0f + x);
                        allFacesPositions[allFacesPositionsCount + 4] = (0f + y);
                        allFacesPositions[allFacesPositionsCount + 5] = (0f + z);

                        allFacesPositions[allFacesPositionsCount + 6] = (1f + x);
                        allFacesPositions[allFacesPositionsCount + 7] = (0f + y);
                        allFacesPositions[allFacesPositionsCount + 8] = (0f + z);

                        allFacesPositions[allFacesPositionsCount + 9] = (1f + x);
                        allFacesPositions[allFacesPositionsCount + 10] = (0f + y);
                        allFacesPositions[allFacesPositionsCount + 11] = (1f + z);

                        allFacesPositionsCount += 12;

                        //bottom
                        float bottomLight = 1f;

                        //bottom
                        for (int i = 0; i < 12; i++) {
                            allFacesLight[i + allFacesLightCount] = (bottomLight);
                        }

                        allFacesLightCount += 12;

                        //bottom
                        allFacesIndices[allFacesIndicesTableCount + 0] = (0 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 1] = (1 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 2] = (2 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 3] = (0 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 4] = (2 + allFacesIndicesCount);
                        allFacesIndices[allFacesIndicesTableCount + 5] = (3 + allFacesIndicesCount);
                        allFacesIndicesCount += 4;
                        allFacesIndicesTableCount += 6;

                        float[] textureBottom = getBottomTexturePoints(thisBlock);
                        //bottom
                        allFacesTextureCoord[allFacesTextureCoordCount + 0] = (textureBottom[1]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 1] = (textureBottom[2]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 2] = (textureBottom[0]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 3] = (textureBottom[2]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 4] = (textureBottom[0]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 5] = (textureBottom[3]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 6] = (textureBottom[1]);
                        allFacesTextureCoord[allFacesTextureCoordCount + 7] = (textureBottom[3]);
                        allFacesTextureCoordCount += 8;
                    }
                }
            }
        }


        if (allFacesPositionsCount > 0) {
//        convert the position objects into usable array
            float[] allFacesPositionsArray = new float[allFacesPositionsCount];
            System.arraycopy(allFacesPositions, 0, allFacesPositionsArray, 0, allFacesPositionsCount);

            //convert the light objects into usable array
            float[] allFacesLightArray = new float[allFacesLightCount];
            System.arraycopy(allFacesLight, 0, allFacesLightArray, 0, allFacesLightCount);

            //convert the indices objects into usable array
            int[] allFacesIndicesArray = new int[allFacesIndicesTableCount];
            System.arraycopy(allFacesIndices, 0, allFacesIndicesArray, 0, allFacesIndicesTableCount);

            //convert the textureCoord objects into usable array
            float[] allFacesTextureCoordArray = new float[allFacesTextureCoordCount];
            System.arraycopy(allFacesTextureCoord, 0, allFacesTextureCoordArray, 0, allFacesTextureCoordCount);

            //pass data to container object

            debugMesh = new Mesh(allFacesPositionsArray, allFacesLightArray, allFacesIndicesArray, allFacesTextureCoordArray, getTextureAtlas());
        }

        //long endTime = System.nanoTime();
        //double duration = (double)(endTime - startTime) /  1_000_000_000d;  //divide by 1000000 to get milliseconds.
        //System.out.println("This took: " + duration + " seconds");
    }
}
