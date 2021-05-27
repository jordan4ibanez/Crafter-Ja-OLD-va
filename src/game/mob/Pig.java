package game.mob;

import engine.graphics.Mesh;
import engine.graphics.Texture;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;

import static engine.FancyMath.randomDirFloat;
import static engine.Time.getDelta;
import static game.blocks.BlockDefinition.getIfLiquid;
import static game.chunk.Chunk.getBlock;
import static game.collision.Collision.applyInertia;
import static game.mob.MobUtilityCode.doHeadCode;
import static game.mob.Mob.registerMob;
import static game.mob.MobUtilityCode.mobSmoothRotation;

public class Pig {

    private final static Mesh[] bodyMeshes = createPigMesh();

    private static final float accelerationMultiplier  = 0.04f;
    final private static float maxWalkSpeed = 2.f;
    final private static float movementAcceleration = 900.f;

    private final static MobInterface mobInterface = new MobInterface() {
        @Override
        public void onTick(MobObject thisObject) {
            float delta = getDelta();

            thisObject.timer += delta;


            if (thisObject.timer > 1.5f) {
                thisObject.stand = !thisObject.stand;
                thisObject.timer = (float) Math.random() * -2f;
                thisObject.rotation = (float) (Math.toDegrees(Math.PI * Math.random() * randomDirFloat()));
            }

            //head test
            //thisObject.bodyRotations[0] = new Vector3f((float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),(float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),0);
            thisObject.bodyRotations[2] = new Vector3f((float) Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f)), 0, 0);
            thisObject.bodyRotations[3] = new Vector3f((float) Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * -2f)), 0, 0);

            thisObject.bodyRotations[4] = new Vector3f((float) Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * -2f)), 0, 0);
            thisObject.bodyRotations[5] = new Vector3f((float) Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f)), 0, 0);

            float yaw = (float) Math.toRadians(thisObject.rotation) + (float) Math.PI;

            thisObject.inertia.x += (float) (Math.sin(-yaw) * accelerationMultiplier) * movementAcceleration * delta;
            thisObject.inertia.z += (float) (Math.cos(yaw) * accelerationMultiplier) * movementAcceleration * delta;

            Vector3f inertia2D = new Vector3f(thisObject.inertia.x, 0, thisObject.inertia.z);

            float maxSpeed = maxWalkSpeed;

            if (thisObject.health <= 0) {
                maxSpeed = 0.01f;
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
                //check if swimming
                int block = getBlock((int) Math.floor(thisObject.pos.x), (int) Math.floor(thisObject.pos.y), (int) Math.floor(thisObject.pos.z));
                if (block > -1 && getIfLiquid(block)) {
                    thisObject.inertia.y += 100f * delta;
                }

                //check for block in front
                if (onGround) {
                    double x = Math.sin(-yaw);
                    double z = Math.cos(yaw);

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

    private static final Vector3f[] bodyOffsets = new Vector3f[]{
            //head
            new Vector3f(0,0.7f,-0.635f),
            //body
            new Vector3f(0,0.6f,0),

            //front right leg
            new Vector3f(-0.15f,0.3f,-0.32f),

            //front left leg
            new Vector3f(0.15f,0.3f,-0.32f),

            //rear right leg
            new Vector3f(-0.15f,0.3f,0.32f),

            //rear left leg
            new Vector3f(0.15f,0.3f,0.32f),
    };

    private static final Vector3f[] bodyRotations = new Vector3f[]{
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
    };

    public static void registerPigMob(){
        registerMob(new MobDefinition("pig", "oink", 6, bodyMeshes, bodyOffsets, bodyRotations,0.9f, 0.45f, mobInterface));
    }



    private static final float PIG_WIDTH = 64f;
    private static final float PIG_HEIGHT = 32f;

    private static float[] calculateTexture(int xMin, int yMin, int xMax, int yMax){
        float[] texturePoints = new float[4];

        texturePoints[0] = (float)xMin/ PIG_WIDTH; //min x (-)
        texturePoints[1] = (float)xMax/ PIG_WIDTH; //max x (+)

        texturePoints[2] = (float)yMin/ PIG_HEIGHT; //min y (-)
        texturePoints[3] = (float)yMax/ PIG_HEIGHT; //max y (+)
        return texturePoints;
    }

    private static Mesh[] createPigMesh(){

        float size = 0.25f; //lazy way to fix

        float[][] oneBlockyBoi = new float[][]{
//                head
                {-0.8f * size,-0.8f * size,-0.8f * size,0.8f * size,0.8f * size,0.8f * size},
//                body
                {-1.f * size,-1.f * size,-1.75f * size, size,0.75f * size,1.75f * size},
//                //front right leg

                {-0.375f * size,-1.2f * size,-0.375f * size,  0.375f * size,0.3f * size,0.375f * size},
//                //front left leg
                {-0.375f * size,-1.2f * size,-0.375f * size,  0.375f * size,0.3f * size,0.375f * size},
                //rear right leg
                {-0.375f * size,-1.2f * size,-0.375f * size,  0.375f * size,0.3f * size,0.375f * size},
                //rear left leg
                {-0.375f * size,-1.2f * size,-0.375f * size,  0.375f * size,0.3f * size,0.375f * size},
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
                calculateTexture(54,6,64,14),
                //back
                calculateTexture(44,6,54,14),
                //right
                calculateTexture(48,23,64,32),
                //left
                calculateTexture(48,23,64,32),
                //top
                calculateTexture(32,23,48,32),
                //bottom
                calculateTexture(48,14,64,23),


                //right arm

                //front
                calculateTexture(0,20,4,26), //dark
                //back
                calculateTexture(4,20,8,26), //light

                //right
                calculateTexture(8,20,12,26), //dark
                //left
                calculateTexture(12,20,16,26), //light

                //top
                calculateTexture(4,16,8,20), //shoulder
                //bottom
                calculateTexture(8,16,12,20), //palm

                //left arm

                //front
                calculateTexture(0,20,4,26), //dark
                //back
                calculateTexture(4,20,8,26), //light

                //right
                calculateTexture(8,20,12,26), //dark
                //left
                calculateTexture(12,20,16,26), //light

                //top
                calculateTexture(4,16,8,20), //shoulder
                //bottom
                calculateTexture(8,16,12,20), //palm


                //right leg

                //front
                calculateTexture(0,20,4,26), //dark
                //back
                calculateTexture(4,20,8,26), //light

                //right
                calculateTexture(8,20,12,26), //dark
                //left
                calculateTexture(12,20,16,26), //light

                //top
                calculateTexture(4,16,8,20), //shoulder
                //bottom
                calculateTexture(8,16,12,20), //palm

                //left leg

                //front
                calculateTexture(0,20,4,26), //dark
                //back
                calculateTexture(4,20,8,26), //light

                //right
                calculateTexture(8,20,12,26), //dark
                //left
                calculateTexture(12,20,16,26), //light

                //top
                calculateTexture(4,16,8,20), //shoulder
                //bottom
                calculateTexture(8,16,12,20), //palm

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

            Texture pigTexture = null;
            try {
                pigTexture = new Texture("textures/pig.png");
            } catch (Exception e) {
                e.printStackTrace();
            }


            bodyMeshes[bodyMeshesIndex] = new Mesh(positionsArray, lightArray, indicesArray, textureCoordArray, pigTexture);
            bodyMeshesIndex++;

        }



        return bodyMeshes;
    }
}

