package game.clouds;

import engine.graphics.Mesh;
import engine.graphics.Texture;
import engine.highPerformanceContainers.HyperFloatArray;
import engine.highPerformanceContainers.HyperIntArray;

import static game.clouds.Cloud.getCloudScale;

public class CloudMesh {

    private static final Texture cloudTexture = loadCloudTexture();

    private static Texture loadCloudTexture() {
        Texture texture = null;
        try {
            texture = new Texture("textures/cloud.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return texture;
    }


    private static final byte atlasSizeX = 3;
    private static final byte atlasSizeY = 1;


    public static Mesh buildCloud3DMesh(){
        final HyperFloatArray positions    = new HyperFloatArray(12);
        final HyperFloatArray textureCoord = new HyperFloatArray(8);
        final HyperIntArray indices        = new HyperIntArray(6);
        final HyperFloatArray light        = new HyperFloatArray(12);

        float[] textureWorker;


        float height = 0.2f * getCloudScale();
        float width = getCloudScale();

        int indicesCount = 0;

        //front
        positions.pack(width, height, width, 0f, height, width, 0f, 0f, width, width, 0f, width);
        
        //front
        light.pack(1);


        //front
        indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);
                
        indicesCount += 4;

        textureWorker = calculateTexture(2);

        //front
        textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);



        //back
        positions.pack(0f, height, 0f, width, height, 0f, width, 0f, 0f, 0f, 0f, 0f);

        light.pack(1);


        //back
        indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);
        indicesCount += 4;

        textureWorker = calculateTexture(2);
        //back
        textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);


        //right
        positions.pack(width, height, 0f, width, height, width, width, 0f, width, width, 0f, 0f);

        light.pack(1);

        //right
        indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

        indicesCount += 4;

        textureWorker = calculateTexture(2);
        //right
        textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);




        //left
        positions.pack(0f, height, width, 0f, height, 0f, 0f, 0f, 0f, 0f, 0f, width);

        //left
        light.pack(1);

        //left
        indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);
        indicesCount += 4;

        textureWorker = calculateTexture(2);
        //left
        textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);


        //top
        positions.pack(0f, height, 0f, 0f, height, width, width, height, width, width, height, 0f);



        light.pack(1);


        //top
        indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);
        indicesCount += 4;

        textureWorker = calculateTexture(1);
        //top
        textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);



        //bottom
        positions.pack(0f, 0f, width, 0f, 0f, 0f, width, 0f, 0f, width, 0f, width);

        light.pack(1);


        //bottom
        indices.pack(indicesCount, 1 + indicesCount, 2 + indicesCount, indicesCount, 2 + indicesCount, 3 + indicesCount);

        textureWorker = calculateTexture(0);
        //bottom
        textureCoord.pack(textureWorker[1], textureWorker[2], textureWorker[0], textureWorker[2], textureWorker[0], textureWorker[3], textureWorker[1], textureWorker[3]);


        Mesh cloud3DMesh = new Mesh(positions.values(), light.values(), indices.values(), textureCoord.values(), cloudTexture);

        positions.clear();
        light.clear();
        indices.clear();
        textureCoord.clear();

        return cloud3DMesh;
    }

    public static Mesh buildCloud2DMesh() {
        float[] positions = new float[12];
        float[] textureCoord = new float[8];
        int[] indices = new int[6];
        float[] light = new float[12];


        float width = getCloudScale();

        positions[0] = (width);
        positions[1] = (0);
        positions[2] = (width);

        positions[3] = (0);
        positions[4] = (0);
        positions[5] = (width);

        positions[6] = (0);
        positions[7] = (0);
        positions[8] = (0);

        positions[9] = (width);
        positions[10] = (0);
        positions[11] = (0);

        for (int i = 0; i < 12; i++) {
            light[i] = 1;
        }

        indices[0] = (0);
        indices[1] = (1);
        indices[2] = (2);
        indices[3] = (0);
        indices[4] = (2);
        indices[5] = (3);

        float[] textureWorker = calculateTexture(1);
        textureCoord[0] = (textureWorker[1]);
        textureCoord[1] = (textureWorker[2]);
        textureCoord[2] = (textureWorker[0]);
        textureCoord[3] = (textureWorker[2]);
        textureCoord[4] = (textureWorker[0]);
        textureCoord[5] = (textureWorker[3]);
        textureCoord[6] = (textureWorker[1]);
        textureCoord[7] = (textureWorker[3]);

        return new Mesh(positions, light, indices, textureCoord, cloudTexture);
    }

    private static float[] calculateTexture(int x){
        float[] texturePoints = new float[4];
        texturePoints[0] = (float)x/(float)atlasSizeX;     //min x (-)
        texturePoints[1] = (float)(x+1)/(float)atlasSizeX; //max x (+)

        texturePoints[2] = (float) 0 /(float)atlasSizeY;     //min y (-)
        texturePoints[3] = (float)(1)/(float)atlasSizeY; //max y (+)
        return texturePoints;
    }
}
