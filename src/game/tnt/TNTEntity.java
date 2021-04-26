package game.tnt;

import engine.graph.Mesh;
import engine.graph.Texture;
import org.joml.Vector3f;

import java.util.ArrayList;

import static engine.FancyMath.*;
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
    private static Vector3f[] tntPos = new Vector3f[MAX_ID_AMOUNT];
    private static Vector3f tntScale[] = new Vector3f[MAX_ID_AMOUNT];
    private static float[] tntTimer =    new float[MAX_ID_AMOUNT];
    private static boolean[] tntExists =    new boolean[MAX_ID_AMOUNT];
    private static Vector3f[] tntInertia = new Vector3f[MAX_ID_AMOUNT];

    public static int getTotalTNT(){
        return totalTNT;
    }

    public static void createTNT(Vector3f pos){
        pos.x += 0.5f;
        pos.y += 0.5f;
        pos.z += 0.5f;
        tntPos[totalTNT] = new Vector3f(pos);
        tntInertia[totalTNT] = new Vector3f(randomForceValue(15f),(float)Math.random()*7f,randomForceValue(15f));
        tntExists[totalTNT] = true;
        tntTimer[totalTNT] = 0f;
        tntScale[totalTNT] = new Vector3f(1,1,1);
        totalTNT++;
        System.out.println("Created new TNT. Total TNT: " + totalTNT);
    }

    public static void createTNT(Vector3f pos, float timer, boolean punched) throws Exception {
        pos.x += 0.5f;
        pos.y += 0.5f;
        pos.z += 0.5f;
        tntPos[totalTNT] = new Vector3f(pos);
        float tntJump;
        if (punched){
            tntJump = (float)Math.random()*10f;
//            soundMgr.playSoundSource(Crafter.Sounds.TNTHISS.toString());
            playSound("tnt_ignite", pos);
        } else {
            tntJump = 0f;
        }
        tntInertia[totalTNT] = new Vector3f(randomForceValue(15f),tntJump,randomForceValue(15f));
        tntExists[totalTNT] = true;
        tntTimer[totalTNT] = timer;
        tntScale[totalTNT] = new Vector3f(1,1,1);
        totalTNT++;
//        System.out.println("Created new TNT. Total TNT: " + totalTNT);
    }

    public static void onTNTStep() throws Exception {
        for (int i = 0; i < totalTNT; i++){
            tntTimer[i] += 0.001f;
            applyInertia(tntPos[i], tntInertia[i], true, tntSize, tntSize * 2, true, false, true, false);

            if(tntTimer[i]>2.23f){
                tntScale[i].x += 0.002f;
                tntScale[i].y += 0.00075f;
                tntScale[i].z += 0.002f;
            }

            if (tntTimer[i] > 2.6f){

                boom(tntPos[i], 5);

//                soundMgr.playSoundSource(Crafter.Sounds.TNT.toString());
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

    public static Vector3f getTNTScale(int ID){
        return tntScale[ID];
    }

    public static void clearTNT(){
        for (int i = 0; i < totalTNT; i++){
            if(tntExists[i]){
                tntPos[i] = null;
                tntInertia[i] = null;
                tntExists[i] = false;
                tntScale[i] = null;
                tntTimer[i] = 0;
            }
        }
        totalTNT = 0;
    }

    public static Mesh getTNTMesh(){
        return mesh;
    }

    public static boolean tntExists(int ID){
        return tntExists[ID];
    }

    public static Vector3f getTNTPosition(int ID){
        return tntPos[ID];
    }

    public static void createTNTEntityMesh() throws Exception {

        int indicesCount = 0;

        ArrayList positions     = new ArrayList();
        ArrayList textureCoord  = new ArrayList();
        ArrayList indices       = new ArrayList();
        ArrayList light         = new ArrayList();

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
        indices.add(0+indicesCount); indices.add(1+indicesCount); indices.add(2+indicesCount); indices.add(0+indicesCount); indices.add(2+indicesCount); indices.add(3+indicesCount);
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
        indices.add(0+indicesCount); indices.add(1+indicesCount); indices.add(2+indicesCount); indices.add(0+indicesCount); indices.add(2+indicesCount); indices.add(3+indicesCount);
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
        indices.add(0+indicesCount); indices.add(1+indicesCount); indices.add(2+indicesCount); indices.add(0+indicesCount); indices.add(2+indicesCount); indices.add(3+indicesCount);
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
        indices.add(0+indicesCount); indices.add(1+indicesCount); indices.add(2+indicesCount); indices.add(0+indicesCount); indices.add(2+indicesCount); indices.add(3+indicesCount);
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
        indices.add(0+indicesCount); indices.add(1+indicesCount); indices.add(2+indicesCount); indices.add(0+indicesCount); indices.add(2+indicesCount); indices.add(3+indicesCount);
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
        indices.add(0+indicesCount); indices.add(1+indicesCount); indices.add(2+indicesCount); indices.add(0+indicesCount); indices.add(2+indicesCount); indices.add(3+indicesCount);

        float[] textureBottom = getBottomTexturePoints(6);
        //bottom
        textureCoord.add(textureBottom[1]);textureCoord.add(textureBottom[2]);
        textureCoord.add(textureBottom[0]);textureCoord.add(textureBottom[2]);
        textureCoord.add(textureBottom[0]);textureCoord.add(textureBottom[3]);
        textureCoord.add(textureBottom[1]);textureCoord.add(textureBottom[3]);


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

        Texture texture = new Texture("textures/textureAtlas.png");

        Mesh thisMesh = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, texture);

        mesh = thisMesh;
    }

    public static void cleanTNTUp(){
        mesh.cleanUp(false);
    }
}