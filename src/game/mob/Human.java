package game.mob;

import engine.graphics.Mesh;
import engine.graphics.Texture;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;

import static engine.FancyMath.randomDirFloat;
import static engine.Time.getDelta;
import static game.chunk.Chunk.getBlock;
import static game.collision.Collision.applyInertia;
import static game.mob.MobUtilityCode.doHeadCode;
import static game.mob.Mob.registerMob;
import static game.mob.MobUtilityCode.mobSmoothRotation;

public class Human {

    private final static Mesh[] bodyMeshes = createPlayerMesh();

    public static Mesh[] getHumanMeshes(){
        return bodyMeshes;
    }

    public static Vector3f[]getHumanBodyOffsets(){
        return bodyOffsets;
    }

    private static final float accelerationMultiplier  = 0.03f;
    final private static float maxWalkSpeed = 2.f;
    final private static float movementAcceleration = 900.f;

    private final static MobInterface mobInterface = new MobInterface() {
        @Override
        public void onTick(MobObject thisObject) {

            float delta = getDelta();

            thisObject.timer += delta;

            if (thisObject.timer > 1.5f) {
                thisObject.stand = !thisObject.stand;
                thisObject.timer = (float)Math.random() * -2f;
                thisObject.rotation = (float) (Math.toDegrees(Math.PI * Math.random() * randomDirFloat()));
            }



            //head test
            //thisObject.bodyRotations[0] = new Vector3f((float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),(float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),0);
            thisObject.bodyRotations[2] = new Vector3f((float) Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f)), 0, 0);
            thisObject.bodyRotations[3] = new Vector3f((float) Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * -2f)), 0, 0);

            thisObject.bodyRotations[4] = new Vector3f((float) Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * -2f)), 0, 0);
            thisObject.bodyRotations[5] = new Vector3f((float) Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f)), 0, 0);


            float bodyYaw = (float) Math.toRadians(thisObject.rotation) + (float) Math.PI;

            thisObject.inertia.x += (float) (Math.sin(-bodyYaw) * accelerationMultiplier) * movementAcceleration * delta;
            thisObject.inertia.z += (float) (Math.cos(bodyYaw) * accelerationMultiplier) * movementAcceleration * delta;

            Vector3f inertia2D = new Vector3f(thisObject.inertia.x, 0, thisObject.inertia.z);

            float maxSpeed = maxWalkSpeed;

            if (thisObject.health <= 0){
                maxSpeed = 0.01f;
            }

            if (thisObject.animationTimer >= 1f) {
                thisObject.animationTimer = 0f;
            }

            if (inertia2D.length() > maxSpeed) {
                inertia2D = inertia2D.normalize().mul(maxSpeed);
                thisObject.inertia.x = inertia2D.x;
                thisObject.inertia.z = inertia2D.z;
            }

            thisObject.animationTimer += delta * (inertia2D.length() / maxSpeed);

            if (thisObject.animationTimer >= 1f) {
                thisObject.animationTimer = 0f;
            }

            boolean onGround = applyInertia(thisObject.pos, thisObject.inertia, false, thisObject.width, thisObject.height, true, false, true, false, false);

            thisObject.onGround = onGround;


            if (thisObject.health > 0) {
                //check for block in front
                if (onGround) {
                    double x = Math.sin(-bodyYaw);
                    double z = Math.cos(bodyYaw);

                    if (getBlock((int) Math.floor(x + thisObject.pos.x), (int) Math.floor(thisObject.pos.y), (int) Math.floor(z + thisObject.pos.z)) > 0) {
                        thisObject.inertia.y += 8.75f;
                    }
                }
            }

            mobSmoothRotation(thisObject);
            doHeadCode(thisObject);

            thisObject.lastPos = new Vector3d(thisObject.pos);

        }
    };

    private static final float yOffsetCorrection = 0.5f;

    private static final Vector3f[] bodyOffsets = new Vector3f[]{
            new Vector3f(0,0.8f + yOffsetCorrection,0),
            new Vector3f(0,0.8f + yOffsetCorrection,0),
            new Vector3f(-0.28f,0.725f + yOffsetCorrection,0),
            new Vector3f(0.28f,0.725f + yOffsetCorrection,0),
            new Vector3f(-0.09f,0.17f + yOffsetCorrection,0),
            new Vector3f(0.09f,0.17f + yOffsetCorrection,0),
    };

    private static final Vector3f[] bodyRotations = new Vector3f[]{
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
    };

    public static void registerHumanMob(){
        registerMob(new MobDefinition("human", "hurt", 7, bodyMeshes, bodyOffsets, bodyRotations,1.9f, 0.25f, mobInterface));
    }



    private static final float PLAYER_WIDTH = 64f;
    private static final float PLAYER_HEIGHT = 32f;

    private static float[] calculateTexture(int xMin, int yMin, int xMax, int yMax){
        float[] texturePoints = new float[4];

        texturePoints[0] = (float)xMin/PLAYER_WIDTH; //min x (-)
        texturePoints[1] = (float)xMax/PLAYER_WIDTH; //max x (+)

        texturePoints[2] = (float)yMin/PLAYER_HEIGHT; //min y (-)
        texturePoints[3] = (float)yMax/PLAYER_HEIGHT; //max y (+)
        return texturePoints;
    }

    private static Mesh[] createPlayerMesh(){

        float size = 0.25f; //lazy way to fix

        float[][] oneBlockyBoi = new float[][]{
//                head
                {-0.75f * size,0.0f * size,-0.75f * size,0.75f * size,1.5f * size,0.75f * size},
//                body
                {-0.75f * size,-2.5f * size,-0.45f * size,0.75f * size,0.0f * size,0.45f * size},
//                //right arm
                {-0.375f * size,-2.2f * size,-0.375f * size,  0.375f * size,0.3f * size,0.375f * size},
//                //left arm
                {-0.375f * size,-2.2f * size,-0.375f * size,  0.375f * size,0.3f * size,0.375f * size},
                //right leg
                {-0.375f * size,-2.5f * size,-0.375f * size,  0.375f * size,0.0f * size,0.375f * size},
                //left leg
                {-0.375f * size,-2.5f * size,-0.375f * size,  0.375f * size,0.0f * size,0.375f * size},
        };


        float[][] textureArrayArray = new float[][]{
                //head
                //right
                calculateTexture(24,8,32,16),
                //left
                calculateTexture(8,8,16,16),
                //front
                calculateTexture(0,8,8,16),
                //back
                calculateTexture(16,8,24,16),
                //top
                calculateTexture(8,0,16,8),
                //bottom
                calculateTexture(16,0,24,8),

                //body
                //front
                calculateTexture(32,20,40,30),
                //back
                calculateTexture(20,20,28,30),
                //right
                calculateTexture(28,20,32,30),
                //left
                calculateTexture(16,20,20,30),
                //top
                calculateTexture(20,16,28,20),
                //bottom
                calculateTexture(28,16,36,20),


                //right arm
                //front
                calculateTexture(48,20,52,32), //dark
                //back
                calculateTexture(44,20,48,32), //light
                //right
                calculateTexture(48,20,52,32), //dark
                //left
                calculateTexture(44,20,48,32), //light
                //top
                calculateTexture(44,16,48,20), //shoulder
                //bottom
                calculateTexture(48,16,52,20), //palm

                //left arm
                //front
                calculateTexture(48,20,52,32), //dark
                //back
                calculateTexture(44,20,48,32), //light
                //right
                calculateTexture(44,20,48,32), //light
                //left
                calculateTexture(48,20,52,32), //dark
                //top
                calculateTexture(44,16,48,20), //shoulder
                //bottom
                calculateTexture(48,16,52,20), //palm


                //right leg
                //front
                calculateTexture(0,20,4,32), //dark
                //back
                calculateTexture(4,20,8,32), //light
                //right
                calculateTexture(8,20,12,32), //dark
                //left
                calculateTexture(12,20,16,32), //light
                //top
                calculateTexture(4,16,8,20), //top
                //bottom
                calculateTexture(8,16,12,20), //bottom

                //left leg
                //front
                calculateTexture(0,20,4,32), //dark
                //back
                calculateTexture(4,20,8,32), //light
                //right
                calculateTexture(12,20,16,32), //light
                //left
                calculateTexture(8,20,12,32), //dark
                //top
                calculateTexture(4,16,8,20), //top
                //bottom
                calculateTexture(8,16,12,20), //bottom

        };

        Mesh[] bodyMeshes = new Mesh[6];
        int bodyMeshesIndex = 0;

        int textureCounter = 0;

        for (float[] thisBlockBox : oneBlockyBoi) {
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
                playerTexture = new Texture("textures/player.png");
            } catch (Exception e) {
                e.printStackTrace();
            }


            bodyMeshes[bodyMeshesIndex] = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, playerTexture);
            bodyMeshesIndex++;

        }



        return bodyMeshes;
    }
}
