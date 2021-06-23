package game.clouds;

import engine.graphics.Mesh;
import engine.graphics.Texture;

import java.util.LinkedList;

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
        final LinkedList<Float> positions = new LinkedList<>();
        final LinkedList<Float> textureCoord = new LinkedList<>();
        final LinkedList<Integer> indices = new LinkedList<>();
        final LinkedList<Float> light = new LinkedList<>();

        final byte maxLight = 15;
        final float lightValue = 15;
        float digestedLight;

        float[] textureWorker;


        float height = 0.2f * getCloudScale();
        float width = getCloudScale();

        int indicesCount = 0;

        //front
        positions.add(width);
        positions.add(height);
        positions.add(width);

        positions.add(0f);
        positions.add(height);
        positions.add(width);

        positions.add(0f);
        positions.add(0f);
        positions.add(width);

        positions.add(width);
        positions.add(0f);
        positions.add(width);

        //front
        digestedLight =  convertLight(lightValue / maxLight);

        //front
        for (int i = 0; i < 12; i++) {
            light.add(digestedLight);
        }

        //front
        indices.add(indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);
        indicesCount += 4;

        textureWorker = calculateTexture(2, 0);

        //front
        textureCoord.add(textureWorker[1]);
        textureCoord.add(textureWorker[2]);
        textureCoord.add(textureWorker[0]);
        textureCoord.add(textureWorker[2]);
        textureCoord.add(textureWorker[0]);
        textureCoord.add(textureWorker[3]);
        textureCoord.add(textureWorker[1]);
        textureCoord.add(textureWorker[3]);



        //back
        positions.add(0f);
        positions.add(height);
        positions.add(0f);
        positions.add(width);
        positions.add(height);
        positions.add(0f);
        positions.add(width);
        positions.add(0f);
        positions.add(0f);
        positions.add(0f);
        positions.add(0f);
        positions.add(0f);

        digestedLight = convertLight(lightValue / maxLight);

        //back
        for (int i = 0; i < 12; i++) {
            light.add(digestedLight);
        }

        //back
        indices.add(indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);
        indicesCount += 4;

        textureWorker = calculateTexture(2, 0);
        //back
        textureCoord.add(textureWorker[1]);
        textureCoord.add(textureWorker[2]);
        textureCoord.add(textureWorker[0]);
        textureCoord.add(textureWorker[2]);
        textureCoord.add(textureWorker[0]);
        textureCoord.add(textureWorker[3]);
        textureCoord.add(textureWorker[1]);
        textureCoord.add(textureWorker[3]);


        //right
        positions.add(width);
        positions.add(height);
        positions.add(0f);
        positions.add(width);
        positions.add(height);
        positions.add(width);
        positions.add(width);
        positions.add(0f);
        positions.add(width);
        positions.add(width);
        positions.add(0f);
        positions.add(0f);


        digestedLight = convertLight(lightValue / maxLight);
        //right
        for (int i = 0; i < 12; i++) {
            light.add(digestedLight);
        }

        //right
        indices.add(indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);
        indicesCount += 4;

        textureWorker = calculateTexture(2, 0);
        //right
        textureCoord.add(textureWorker[1]);
        textureCoord.add(textureWorker[2]);
        textureCoord.add(textureWorker[0]);
        textureCoord.add(textureWorker[2]);
        textureCoord.add(textureWorker[0]);
        textureCoord.add(textureWorker[3]);
        textureCoord.add(textureWorker[1]);
        textureCoord.add(textureWorker[3]);




        //left
        positions.add(0f);
        positions.add(height);
        positions.add(width);
        positions.add(0f);
        positions.add(height);
        positions.add(0f);
        positions.add(0f);
        positions.add(0f);
        positions.add(0f);
        positions.add(0f);
        positions.add(0f);
        positions.add(width);



        digestedLight = convertLight(lightValue / maxLight);
        //left
        for (int i = 0; i < 12; i++) {
            light.add(digestedLight);
        }

        //left
        indices.add(indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);
        indicesCount += 4;

        textureWorker = calculateTexture(2, 0);
        //left
        textureCoord.add(textureWorker[1]);
        textureCoord.add(textureWorker[2]);
        textureCoord.add(textureWorker[0]);
        textureCoord.add(textureWorker[2]);
        textureCoord.add(textureWorker[0]);
        textureCoord.add(textureWorker[3]);
        textureCoord.add(textureWorker[1]);
        textureCoord.add(textureWorker[3]);


        //top
        positions.add(0f);
        positions.add(height);
        positions.add(0f);
        positions.add(0f);
        positions.add(height);
        positions.add(width);
        positions.add(width);
        positions.add(height);
        positions.add(width);
        positions.add(width);
        positions.add(height);
        positions.add(0f);


        digestedLight = convertLight(lightValue / maxLight);

        //top
        for (int i = 0; i < 12; i++) {
            light.add(digestedLight);
        }

        //top
        indices.add(indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);
        indicesCount += 4;

        textureWorker = calculateTexture(1, 0);
        //top
        textureCoord.add(textureWorker[1]);
        textureCoord.add(textureWorker[2]);
        textureCoord.add(textureWorker[0]);
        textureCoord.add(textureWorker[2]);
        textureCoord.add(textureWorker[0]);
        textureCoord.add(textureWorker[3]);
        textureCoord.add(textureWorker[1]);
        textureCoord.add(textureWorker[3]);



        //bottom
        positions.add(0f);
        positions.add(0f);
        positions.add(width);
        positions.add(0f);
        positions.add(0f);
        positions.add(0f);
        positions.add(width);
        positions.add(0f);
        positions.add(0f);
        positions.add(width);
        positions.add(0f);
        positions.add(width);


        digestedLight = convertLight(lightValue / maxLight);
        //bottom
        for (int i = 0; i < 12; i++) {
            light.add(digestedLight);
        }

        //bottom
        indices.add(indicesCount);
        indices.add(1 + indicesCount);
        indices.add(2 + indicesCount);
        indices.add(indicesCount);
        indices.add(2 + indicesCount);
        indices.add(3 + indicesCount);

        textureWorker = calculateTexture(0, 0);
        //bottom
        textureCoord.add(textureWorker[1]);
        textureCoord.add(textureWorker[2]);
        textureCoord.add(textureWorker[0]);
        textureCoord.add(textureWorker[2]);
        textureCoord.add(textureWorker[0]);
        textureCoord.add(textureWorker[3]);
        textureCoord.add(textureWorker[1]);
        textureCoord.add(textureWorker[3]);


        //this is very wasteful
        int workerCounter = 0;

        //convert all ArrayLists<>() into primitive[]
        float[] positionsArray = new float[positions.size()];
        for (Float data : positions) {
            //auto casted from Float to float
            positionsArray[workerCounter] = data;
            workerCounter++;
        }

        workerCounter = 0;

        float[] lightArray = new float[light.size()];
        for (Float data : light) {
            //auto casted from Float to float
            lightArray[workerCounter] = data;
            workerCounter++;
        }

        workerCounter = 0;

        int[] indicesArray = new int[indices.size()];
        for (Integer data : indices) {
            //auto casted from Integer to int
            indicesArray[workerCounter] = data;
            workerCounter++;
        }

        workerCounter = 0;

        float[] textureCoordArray = new float[textureCoord.size()];
        for (Float data : textureCoord) {
            //auto casted from Float to float
            textureCoordArray[workerCounter] = data;
            workerCounter++;
        }

        return new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, cloudTexture);
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

        positions[3] = (-width);
        positions[4] = (0);
        positions[5] = (width);

        positions[6] = (-width);
        positions[7] = (0);
        positions[8] = (-width);

        positions[9] = (width);
        positions[10] = (0);
        positions[11] = (-width);

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

        return new Mesh(positions, light, indices, textureCoord, cloudTexture);
    }

    private static float[] calculateTexture(int x, int y){
        float[] texturePoints = new float[4];
        texturePoints[0] = (float)x/(float)atlasSizeX;     //min x (-)
        texturePoints[1] = (float)(x+1)/(float)atlasSizeX; //max x (+)

        texturePoints[2] = (float)y/(float)atlasSizeY;     //min y (-)
        texturePoints[3] = (float)(y+1)/(float)atlasSizeY; //max y (+)
        return texturePoints;
    }

    private static float convertLight(float lightByte){
        return (float) Math.pow(Math.pow(lightByte, 1.5), 1.5);
    }
}
