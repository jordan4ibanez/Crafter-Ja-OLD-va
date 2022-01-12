package game.mob;

import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.FancyMath.randomDirFloat;
import static engine.time.Time.getDelta;
import static game.blocks.BlockDefinition.getIfLiquid;
import static game.chunk.Chunk.getBlock;
import static game.collision.Collision.applyInertia;
import static game.mob.MobDefinition.*;
import static game.mob.MobMeshBuilder.calculateMobTexture;
import static game.mob.MobMeshBuilder.createMobMesh;
import static game.mob.MobObject.*;
import static game.mob.MobUtilityCode.doHeadCode;
import static game.mob.MobUtilityCode.mobSmoothRotation;

public class Pig {
    private static final float accelerationMultiplier  = 0.04f;
    final private static float maxWalkSpeed = 2.f;
    final private static float movementAcceleration = 900.f;
    private static final Vector2f workerVector2f = new Vector2f();

    private final static MobInterface mobInterface = new MobInterface() {
        @Override
        public void onTick(int thisMob) {

            double delta = getDelta();

            //primitive
            int thisMobDefinitionID = getMobID(thisMob);
            float thisMobTimer = getMobTimer(thisMob);
            float thisMobAnimationTimer = getMobAnimationTimer(thisMob);
            float thisMobRotation = getMobRotation(thisMob);
            byte thisMobHealth = getMobHealth(thisMob);

            //pointers
            Vector3d thisMobPos = getMobPos(thisMob);
            Vector3d thisMobOldPos = getMobOldPos(thisMob);
            Vector3f[] thisMobBodyRotations = getMobBodyRotations(thisMob);
            Vector3f thisMobInertia = getMobInertia(thisMob);

            thisMobTimer += delta;

            if (thisMobTimer > 1.5f) {
                boolean thisMobStand = getIfMobStanding(thisMob);
                setIfMobStanding(thisMob, !thisMobStand);
                thisMobTimer = (float) Math.random() * -2f;
                thisMobRotation = (float) (Math.toDegrees(Math.PI * Math.random() * randomDirFloat()));
            }

            //head test
            //thisObject.bodyRotations[0] = new Vector3f((float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),(float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),0);
            float animation = (float) Math.toDegrees(Math.sin(thisMobAnimationTimer * Math.PI * 2f));
            thisMobBodyRotations[2].x = animation;
            thisMobBodyRotations[3].x = -animation;

            thisMobBodyRotations[4].x = -animation;
            thisMobBodyRotations[5].x = animation;

            float yaw = Math.toRadians(thisMobRotation) + (float) Math.PI;

            thisMobInertia.x += (Math.sin(-yaw) * accelerationMultiplier) * movementAcceleration * delta;
            thisMobInertia.z += (Math.cos(yaw) * accelerationMultiplier) * movementAcceleration * delta;

            workerVector2f.set(thisMobInertia.x, thisMobInertia.z);

            float maxSpeed = maxWalkSpeed;

            if (thisMobHealth <= 0) {
                maxSpeed = 0.01f;
            }

            if (workerVector2f.length() > maxSpeed) {
                workerVector2f.normalize().mul(maxSpeed);
                thisMobInertia.x = workerVector2f.x;
                thisMobInertia.z = workerVector2f.y;
            }

            boolean onGround = applyInertia(thisMobPos, thisMobInertia, false, getMobDefinitionWidth(thisMobDefinitionID), getMobDefinitionHeight(thisMobDefinitionID), true, false, true, false, false);

            thisMobAnimationTimer += thisMobPos.distance(thisMobOldPos) / 2f;

            if (thisMobAnimationTimer >= 1f) {
                thisMobAnimationTimer = 0f;
            }


            setIfMobOnGround(thisMob, onGround);


            if (thisMobHealth > 0) {
                //check if swimming
                byte block = getBlock((int) Math.floor(thisMobPos.x), (int) Math.floor(thisMobPos.y), (int) Math.floor(thisMobPos.z));
                if (block > -1 && getIfLiquid(block)) {
                    thisMobInertia.y += 100f * delta;
                }

                //check for block in front
                if (onGround) {
                    double x = Math.sin(-yaw);
                    double z = Math.cos(yaw);

                    if (getBlock((int) Math.floor(x + thisMobPos.x), (int) Math.floor(thisMobPos.y), (int) Math.floor(z + thisMobPos.z)) > 0) {
                        thisMobInertia.y += 8.75f;
                    }
                }
            }

            setMobAnimationTimer(thisMob, thisMobAnimationTimer);
            setMobTimer(thisMob, thisMobTimer);
            thisMobOldPos.set(thisMobPos);

            mobSmoothRotation(thisMob);
            doHeadCode(thisMob);
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
        registerMob("pig", "oink",true, (byte) 6, createMesh(), bodyOffsets, bodyRotations,0.9f, 0.45f, mobInterface);
    }


    private static int[] createMesh(){

        float size = 0.25f; //lazy way to fix

        float[][][] modelPieceArray = new float[][][]{
                //head
                {{-0.8f * size,-0.8f * size,-0.8f * size,0.8f * size,0.8f * size,0.8f * size}},
                //body
                {{-1.f * size,-1.f * size,-1.75f * size, size,0.75f * size,1.75f * size}},
                //front right leg
                {{-0.375f * size,-1.2f * size,-0.375f * size,  0.375f * size,0.3f * size,0.375f * size}},
                //front left leg
                {{-0.375f * size,-1.2f * size,-0.375f * size,  0.375f * size,0.3f * size,0.375f * size}},
                //rear right leg
                {{-0.375f * size,-1.2f * size,-0.375f * size,  0.375f * size,0.3f * size,0.375f * size}},
                //rear left leg
                {{-0.375f * size,-1.2f * size,-0.375f * size,  0.375f * size,0.3f * size,0.375f * size}},
        };
        final float textureWidth = 64f;
        final float textureHeight = 32f;

        float[][][] modelTextureArray = new float[][][]{
                //head
                //back
                {calculateMobTexture(24,8,32,16,textureWidth,textureHeight),
                //front
                calculateMobTexture(8,8,16,16,textureWidth,textureHeight),
                //right
                calculateMobTexture(0,8,8,16,textureWidth,textureHeight),
                //left
                calculateMobTexture(16,8,24,16,textureWidth,textureHeight),
                //top
                calculateMobTexture(8,0,16,8,textureWidth,textureHeight),
                //bottom
                calculateMobTexture(16,0,24,8,textureWidth,textureHeight)},

                //body
                //back
                {calculateMobTexture(54,6,64,14,textureWidth,textureHeight),
                //front
                calculateMobTexture(44,6,54,14,textureWidth,textureHeight),
                //right
                calculateMobTexture(48,23,64,32,textureWidth,textureHeight),
                //left
                calculateMobTexture(48,23,64,32,textureWidth,textureHeight),
                //top
                calculateMobTexture(32,23,48,32,textureWidth,textureHeight),
                //bottom
                calculateMobTexture(48,14,64,23,textureWidth,textureHeight)},


                //right arm
                //back
                {calculateMobTexture(0,20,4,26,textureWidth,textureHeight), //dark
                //front
                calculateMobTexture(4,20,8,26,textureWidth,textureHeight), //light
                //right
                calculateMobTexture(8,20,12,26,textureWidth,textureHeight), //dark
                //left
                calculateMobTexture(12,20,16,26,textureWidth,textureHeight), //light
                //top
                calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm


                //left arm
                //back
                {calculateMobTexture(0,20,4,26,textureWidth,textureHeight), //dark
                //front
                calculateMobTexture(4,20,8,26,textureWidth,textureHeight), //light
                //right
                calculateMobTexture(8,20,12,26,textureWidth,textureHeight), //dark
                //left
                calculateMobTexture(12,20,16,26,textureWidth,textureHeight), //light
                //top
                calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm


                //right leg
                //back
                {calculateMobTexture(0,20,4,26,textureWidth,textureHeight), //dark
                //front
                calculateMobTexture(4,20,8,26,textureWidth,textureHeight), //light
                //right
                calculateMobTexture(8,20,12,26,textureWidth,textureHeight), //dark
                //left
                calculateMobTexture(12,20,16,26,textureWidth,textureHeight), //light
                //top
                calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm

                //left leg
                //back
                {calculateMobTexture(0,20,4,26,textureWidth,textureHeight), //dark
                //front
                calculateMobTexture(4,20,8,26,textureWidth,textureHeight), //light
                //right
                calculateMobTexture(8,20,12,26,textureWidth,textureHeight), //dark
                //left
                calculateMobTexture(12,20,16,26,textureWidth,textureHeight), //light
                //top
                calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm
        };

        return createMobMesh(modelPieceArray,modelTextureArray, "textures/pig.png");
    }
}

