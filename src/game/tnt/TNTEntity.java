package game.tnt;

import engine.graphics.Mesh;
import engine.graphics.Texture;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;

import static engine.FancyMath.*;
import static engine.Time.getDelta;
import static engine.sound.SoundAPI.playSound;
import static game.blocks.BlockDefinition.*;
import static game.collision.Collision.applyInertia;
import static game.tnt.Explosion.boom;

public class TNTEntity {
    private final static float tntSize = 0.5f;
    private final static int MAX_ID_AMOUNT = 126_000;
    private static int totalTNT = 0;
    //TODO: pseudo object holder
    private static Mesh mesh;
    private static final Vector3d[] tntPos = new Vector3d[MAX_ID_AMOUNT];
    private static final Vector3d[] tntScale = new Vector3d[MAX_ID_AMOUNT];
    private static final float[] tntTimer =    new float[MAX_ID_AMOUNT];
    private static final boolean[] tntExists =    new boolean[MAX_ID_AMOUNT];
    private static final Vector3f[] tntInertia = new Vector3f[MAX_ID_AMOUNT];

    public static int getTotalTNT(){
        return totalTNT;
    }

    public static void createTNT(Vector3d pos){
        pos.x += 0.5f;
        //pos.y += 0.5f;
        pos.z += 0.5f;
        tntPos[totalTNT] = new Vector3d(pos);
        tntInertia[totalTNT] = new Vector3f(randomForceValue(3),(float)Math.random()*7f,randomForceValue(3f));
        tntExists[totalTNT] = true;
        tntTimer[totalTNT] = 0f;
        tntScale[totalTNT] = new Vector3d(1,1,1);
        totalTNT++;
        System.out.println("Created new TNT. Total TNT: " + totalTNT);
    }

    public static void createTNT(Vector3d pos, float timer, boolean punched) {
        pos.x += 0.5f;
        //pos.y += 0.5f;
        pos.z += 0.5f;
        tntPos[totalTNT] = new Vector3d(pos);
        float tntJump;
        if (punched){
            tntJump = (float)Math.random()*10f;
            playSound("tnt_ignite", pos);
        } else {
            tntJump = 0f;
        }
        tntInertia[totalTNT] = new Vector3f(randomForceValue(3f),tntJump,randomForceValue(3f));
        tntExists[totalTNT] = true;
        tntTimer[totalTNT] = timer;
        tntScale[totalTNT] = new Vector3d(1d,1d,1d);
        totalTNT++;
    }

    public static void onTNTStep() throws Exception {
        float delta = getDelta();
        for (int i = 0; i < totalTNT; i++){
            tntTimer[i] += delta;
            applyInertia(tntPos[i], tntInertia[i], true, tntSize, tntSize * 2, true, false, true, false, false);

            if(tntTimer[i]>2.23f){
                tntScale[i].x += delta;
                tntScale[i].y += delta/2f;
                tntScale[i].z += delta;
            }

            if (tntTimer[i] > 2.6f){

                boom(tntPos[i], 5);

                playSound("tnt_explode", tntPos[i]);

                deleteTNT(i);

                continue;
            }

            if (tntPos[i].y < 0){
                deleteTNT(i);
            }
        }
    }


    private static void deleteTNT(int ID){
        tntPos[ID] = null;
        tntInertia[ID] = null;
        tntExists[ID] = false;
        tntScale[ID] = null;
        tntTimer[ID] = 0;

        for (int i = ID; i < totalTNT; i ++){
            tntPos[i] = tntPos[i+1];
            tntInertia[i] = tntInertia[i+1];
            tntExists[i] = tntExists[i+1];
            tntScale[i] = tntScale[i+1];
            tntTimer[i] = tntTimer[i+1];
        }

        tntPos[totalTNT - 1] = null;
        tntInertia[totalTNT - 1] = null;
        tntExists[totalTNT - 1] = false;
        tntScale[totalTNT - 1] = null;
        tntTimer[totalTNT - 1] = 0;

        totalTNT -= 1;
//        System.out.println("A TNT was Deleted. Remaining: " + totalTNT);
    }

    public static Vector3d getTNTScale(int ID){
        return tntScale[ID];
    }

    public static Mesh getTNTMesh(){
        return mesh;
    }

    public static boolean tntExists(int ID){
        return tntExists[ID];
    }

    public static Vector3d getTNTPosition(int ID){
        return tntPos[ID];
    }

    public static void createTNTEntityMesh() throws Exception {

        int indicesCount = 0;

        ArrayList<Float> positions     = new ArrayList<>();
        ArrayList<Float> textureCoord  = new ArrayList<>();
        ArrayList<Integer> indices       = new ArrayList<>();
        ArrayList<Float> light         = new ArrayList<>();

        //create the mesh

        float thisLight = 1f;//(float)Math.pow(Math.pow(15,1.5),1.5);

        //front
        positions.add(tntSize); positions.add( tntSize *2f); positions.add(tntSize);
        positions.add(-tntSize); positions.add( tntSize *2f); positions.add(tntSize);
        positions.add(-tntSize); positions.add(0f); positions.add(tntSize);
        positions.add(tntSize); positions.add(0f); positions.add(tntSize);
        //front
        for (int i = 0; i < 12; i++){
            light.add(thisLight);
        }
        //front
        indices.add(0); indices.add(1+indicesCount); indices.add(2+indicesCount); indices.add(0); indices.add(2+indicesCount); indices.add(3+indicesCount);
        indicesCount += 4;

        float[] textureFront = getFrontTexturePoints(6,(byte) 0);
        //front
        textureCoord.add(textureFront[1]);textureCoord.add(textureFront[2]);
        textureCoord.add(textureFront[0]);textureCoord.add(textureFront[2]);
        textureCoord.add(textureFront[0]);textureCoord.add(textureFront[3]);
        textureCoord.add(textureFront[1]);textureCoord.add(textureFront[3]);

        //back
        positions.add(-tntSize); positions.add( tntSize *2f); positions.add(-tntSize);
        positions.add(tntSize); positions.add( tntSize *2f); positions.add(-tntSize);
        positions.add(tntSize); positions.add(0f); positions.add(-tntSize);
        positions.add(-tntSize); positions.add(0f); positions.add(-tntSize);
        //back

        //back
        for (int i = 0; i < 12; i++){
            light.add(thisLight);
        }
        //back
        indices.add(indicesCount); indices.add(1+indicesCount); indices.add(2+indicesCount); indices.add(indicesCount); indices.add(2+indicesCount); indices.add(3+indicesCount);
        indicesCount += 4;

        float[] textureBack = getBackTexturePoints(6,(byte) 0);
        //back
        textureCoord.add(textureBack[1]);textureCoord.add(textureBack[2]);
        textureCoord.add(textureBack[0]);textureCoord.add(textureBack[2]);
        textureCoord.add(textureBack[0]);textureCoord.add(textureBack[3]);
        textureCoord.add(textureBack[1]);textureCoord.add(textureBack[3]);

        //right
        positions.add(tntSize); positions.add(tntSize *2f); positions.add(-tntSize);
        positions.add(tntSize); positions.add(tntSize *2f); positions.add(tntSize);
        positions.add(tntSize); positions.add(0f); positions.add(tntSize);
        positions.add(tntSize); positions.add(0f); positions.add(-tntSize);

        //right
        for (int i = 0; i < 12; i++){
            light.add(thisLight);
        }
        //right
        indices.add(indicesCount); indices.add(1+indicesCount); indices.add(2+indicesCount); indices.add(indicesCount); indices.add(2+indicesCount); indices.add(3+indicesCount);
        indicesCount += 4;

        float[] textureRight = getRightTexturePoints(6,(byte) 0);
        //right
        textureCoord.add(textureRight[1]);textureCoord.add(textureRight[2]);
        textureCoord.add(textureRight[0]);textureCoord.add(textureRight[2]);
        textureCoord.add(textureRight[0]);textureCoord.add(textureRight[3]);
        textureCoord.add(textureRight[1]);textureCoord.add(textureRight[3]);

        //left
        positions.add(-tntSize); positions.add(tntSize *2f); positions.add(tntSize);
        positions.add(-tntSize); positions.add(tntSize *2f); positions.add(-tntSize);
        positions.add(-tntSize); positions.add(0f); positions.add(-tntSize);
        positions.add(-tntSize); positions.add(0f); positions.add(tntSize);

        //left
        for (int i = 0; i < 12; i++){
            light.add(thisLight);
        }
        //left
        indices.add(indicesCount); indices.add(1+indicesCount); indices.add(2+indicesCount); indices.add(indicesCount); indices.add(2+indicesCount); indices.add(3+indicesCount);
        indicesCount += 4;

        float[] textureLeft = getLeftTexturePoints(6,(byte) 0);
        //left
        textureCoord.add(textureLeft[1]);textureCoord.add(textureLeft[2]);
        textureCoord.add(textureLeft[0]);textureCoord.add(textureLeft[2]);
        textureCoord.add(textureLeft[0]);textureCoord.add(textureLeft[3]);
        textureCoord.add(textureLeft[1]);textureCoord.add(textureLeft[3]);

        //top
        positions.add(-tntSize); positions.add(tntSize *2f ); positions.add(-tntSize);
        positions.add(-tntSize); positions.add(tntSize *2f ); positions.add(tntSize);
        positions.add(tntSize); positions.add(tntSize *2f); positions.add(tntSize);
        positions.add(tntSize); positions.add(tntSize *2f); positions.add(-tntSize);

        //top
        for (int i = 0; i < 12; i++){
            light.add(thisLight);
        }
        //top
        indices.add(indicesCount); indices.add(1+indicesCount); indices.add(2+indicesCount); indices.add(indicesCount); indices.add(2+indicesCount); indices.add(3+indicesCount);
        indicesCount += 4;

        float[] textureTop = getTopTexturePoints(6);
        //top
        textureCoord.add(textureTop[1]);textureCoord.add(textureTop[2]);
        textureCoord.add(textureTop[0]);textureCoord.add(textureTop[2]);
        textureCoord.add(textureTop[0]);textureCoord.add(textureTop[3]);
        textureCoord.add(textureTop[1]);textureCoord.add(textureTop[3]);


        //bottom
        positions.add(-tntSize); positions.add(0f);positions.add(tntSize);
        positions.add(-tntSize); positions.add(0f);positions.add(-tntSize);
        positions.add(tntSize); positions.add(0f);positions.add(-tntSize);
        positions.add(tntSize); positions.add(0f);positions.add(tntSize);

        //bottom
        for (int i = 0; i < 12; i++){
            light.add(thisLight);
        }
        //bottom
        indices.add(indicesCount); indices.add(1+indicesCount); indices.add(2+indicesCount); indices.add(indicesCount); indices.add(2+indicesCount); indices.add(3+indicesCount);

        float[] textureBottom = getBottomTexturePoints(6);
        //bottom
        textureCoord.add(textureBottom[1]);textureCoord.add(textureBottom[2]);
        textureCoord.add(textureBottom[0]);textureCoord.add(textureBottom[2]);
        textureCoord.add(textureBottom[0]);textureCoord.add(textureBottom[3]);
        textureCoord.add(textureBottom[1]);textureCoord.add(textureBottom[3]);


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

        Texture texture = new Texture("textures/textureAtlas.png");

        mesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, texture);
    }

    public static void cleanTNTUp(){
        mesh.cleanUp(false);
    }
}