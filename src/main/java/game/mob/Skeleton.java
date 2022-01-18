package game.mob;

import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.base.FancyMath.randomDirFloat;
import static engine.time.Time.getDelta;
import static game.chunk.Chunk.getBlock;
import static game.collision.Collision.applyInertia;
import static game.mob.MobDefinition.*;
import static game.mob.MobMeshBuilder.calculateMobTexture;
import static game.mob.MobMeshBuilder.createMobMesh;
import static game.mob.MobObject.*;
import static game.mob.MobUtilityCode.doHeadCode;
import static game.mob.MobUtilityCode.mobSmoothRotation;

public class Skeleton {
    //aka mr bones
    private static final float accelerationMultiplier  = 0.03f;
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

            //debug output
            /*
            if (thisMob == 1){
                System.out.println(thisMobAnimationTimer);
            }
             */

            if (thisMobTimer > 1.5f) {
                boolean thisMobStand = getIfMobStanding(thisMob);
                setIfMobStanding(thisMob, !thisMobStand);
                thisMobTimer = (float)Math.random() * -2f;
                setMobRotation(thisMob, (float) (Math.toDegrees(Math.PI * Math.random() * randomDirFloat())));
            }


            //head test
            //thisObject.bodyRotations[0] = new Vector3f((float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),(float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),0);

            float animation = (float) Math.toDegrees(Math.sin(thisMobAnimationTimer * Math.PI * 2f));
            thisMobBodyRotations[2].x = animation;
            thisMobBodyRotations[3].x = -animation;

            thisMobBodyRotations[4].x = -animation;
            thisMobBodyRotations[5].x = animation;



            float bodyYaw = Math.toRadians(thisMobRotation) + (float) Math.PI;

            thisMobInertia.x +=  (Math.sin(-bodyYaw) * accelerationMultiplier) * movementAcceleration * delta;
            thisMobInertia.z +=  (Math.cos(bodyYaw) * accelerationMultiplier) * movementAcceleration * delta;

            workerVector2f.set(thisMobInertia.x, thisMobInertia.z);

            float maxSpeed = maxWalkSpeed;

            if (thisMobHealth <= 0){
                maxSpeed = 0.01f;
            }

            boolean onGround = applyInertia(thisMobPos, thisMobInertia, false, getMobDefinitionWidth(thisMobDefinitionID), getMobDefinitionHeight(thisMobDefinitionID), true, false, true, false, false);


            if (thisMobAnimationTimer >= 1f) {
                thisMobAnimationTimer = 0f;
            }

            if (workerVector2f.length() > maxSpeed) {
                workerVector2f.normalize().mul(maxSpeed);
                thisMobInertia.x = workerVector2f.x;
                thisMobInertia.z = workerVector2f.y;
            }

            thisMobAnimationTimer += thisMobPos.distance(thisMobOldPos) / 2f;

            if (thisMobAnimationTimer >= 1f) {
                thisMobAnimationTimer -= 1f;
            }

            setIfMobOnGround(thisMob, onGround);

            if (thisMobHealth > 0) {
                //check for block in front
                if (onGround) {
                    double x = Math.sin(-bodyYaw);
                    double z = Math.cos(bodyYaw);

                    if (getBlock((int) Math.floor(x + thisMobPos.x), (int) Math.floor(thisMobPos.y), (int) Math.floor(z + thisMobPos.z)) > 0) {
                        thisMobInertia.y += 8.75f;
                    }
                }
            }

            setMobAnimationTimer(thisMob, thisMobAnimationTimer);
            setMobTimer(thisMob, thisMobTimer);

            mobSmoothRotation(thisMob);
            doHeadCode(thisMob);
        }
    };

    private static final float yOffsetCorrection = 0.5f;

    private static final Vector3f[] bodyOffsets = new Vector3f[]{
            new Vector3f(0,0.8f + yOffsetCorrection,0),
            new Vector3f(0,0.8f + yOffsetCorrection,0),
            new Vector3f(-0.24f,0.725f + yOffsetCorrection,0),
            new Vector3f(0.24f,0.725f + yOffsetCorrection,0),
            new Vector3f(-0.09f,0.17f + yOffsetCorrection,0),
            new Vector3f(0.09f,0.17f + yOffsetCorrection,0),

    };

    private static final Vector3f[] bodyRotations = new Vector3f[]{
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,10f),
            new Vector3f(0,0,-10f),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),

    };

    public static void registerSkeletonMob(){
        registerMob("skeleton", "hurt",false, (byte) 7, createMesh(), bodyOffsets, bodyRotations,1.9f, 0.25f, mobInterface);
    }

    private static int[] createMesh(){
        final float modelScale = 0.25f; //lazy way to fix

        final float[][][] modelPieceArray = new float[][][]{
                //head
                {{-0.75f * modelScale,0.0f * modelScale,-0.75f * modelScale,0.75f * modelScale,1.5f * modelScale,0.75f * modelScale}},
                //body
                {{-0.75f * modelScale,-2.5f * modelScale,-0.45f * modelScale,0.75f * modelScale,0.0f * modelScale,0.45f * modelScale}},
                //right arm
                {{-0.15f * modelScale,-2.2f * modelScale,-0.15f * modelScale,  0.15f * modelScale,0.3f * modelScale,0.15f * modelScale}},
                //left arm
                {{-0.15f * modelScale,-2.2f * modelScale,-0.15f * modelScale,  0.15f * modelScale,0.3f * modelScale,0.15f * modelScale}},
                //right leg
                {{-0.15f * modelScale,-2.5f * modelScale,-0.15f * modelScale,  0.15f * modelScale,0.0f * modelScale,0.15f * modelScale}},
                //left leg
                {{-0.15f * modelScale,-2.5f * modelScale,-0.15f * modelScale,  0.15f * modelScale,0.0f * modelScale,0.15f * modelScale}},
        };


        float textureWidth = 64f;
        final float textureHeight = 32f;

        float[][][] modelTextureArray = new float[][][]{
                //head
                //back
                {calculateMobTexture(24, 8, 32, 16, textureWidth, textureHeight),
                //front
                calculateMobTexture(8, 8, 16, 16, textureWidth, textureHeight),
                //right
                calculateMobTexture(0, 8, 8, 16, textureWidth, textureHeight),
                //left
                calculateMobTexture(16, 8, 24, 16, textureWidth, textureHeight),
                //top
                calculateMobTexture(8, 0, 16, 8, textureWidth, textureHeight),
                //bottom
                calculateMobTexture(16, 0, 24, 8, textureWidth, textureHeight)},

                //body
                //back
                {calculateMobTexture(34, 20, 42, 32, textureWidth, textureHeight),
                //front
                calculateMobTexture(21, 20, 29, 32, textureWidth, textureHeight),
                //right
                calculateMobTexture(17, 20, 21, 32, textureWidth, textureHeight),
                //left
                calculateMobTexture(30, 20, 34, 32, textureWidth, textureHeight),
                //top
                calculateMobTexture(20, 16, 28, 20, textureWidth, textureHeight),
                //bottom
                calculateMobTexture(28, 16, 36, 20, textureWidth, textureHeight)},



                //right arm
                //back
                {calculateMobTexture(0, 18, 2, 32, textureWidth, textureHeight), //dark
                //front
                calculateMobTexture(0, 18, 2, 32, textureWidth, textureHeight), //dark
                //right
                calculateMobTexture(2, 18, 4, 32, textureWidth, textureHeight), //dark
                //left
                calculateMobTexture(2, 18, 4, 32, textureWidth, textureHeight), //dark
                //top
                calculateMobTexture(4, 16, 6, 18, textureWidth, textureHeight), //shoulder
                //bottom
                calculateMobTexture(6, 16, 8, 18, textureWidth, textureHeight)}, //palm



                //left arm
                //back
                {calculateMobTexture(0, 18, 2, 32, textureWidth, textureHeight), //dark
                //front
                calculateMobTexture(0, 18, 2, 32, textureWidth, textureHeight), //dark
                //right
                calculateMobTexture(2, 18, 4, 32, textureWidth, textureHeight), //dark
                //left
                calculateMobTexture(2, 18, 4, 32, textureWidth, textureHeight), //dark
                //top
                calculateMobTexture(4, 16, 6, 18, textureWidth, textureHeight), //shoulder
                //bottom
                calculateMobTexture(6, 16, 8, 18, textureWidth, textureHeight)}, //palm



                //right leg
                //back
                {calculateMobTexture(43, 18, 45, 32, textureWidth, textureHeight), //dark
                //front
                calculateMobTexture(43, 18, 45, 32, textureWidth, textureHeight), //dark
                //right
                calculateMobTexture(45, 18, 47, 32, textureWidth, textureHeight), //dark
                //left
                calculateMobTexture(45, 18, 47, 32, textureWidth, textureHeight), //dark
                //top
                calculateMobTexture(47, 16, 49, 18, textureWidth, textureHeight), //top
                //bottom
                calculateMobTexture(49, 16, 51, 18, textureWidth, textureHeight)}, //bottom


                //left leg
                //back
                {calculateMobTexture(43, 18, 45, 32, textureWidth, textureHeight), //dark
                //front
                calculateMobTexture(43, 18, 45, 32, textureWidth, textureHeight), //dark
                //right
                calculateMobTexture(45, 18, 47, 32, textureWidth, textureHeight), //dark
                //left
                calculateMobTexture(45, 18, 47, 32, textureWidth, textureHeight), //dark
                //top
                calculateMobTexture(47, 16, 49, 18, textureWidth, textureHeight), //top
                //bottom
                calculateMobTexture(49, 16, 51, 18, textureWidth, textureHeight)}, //bottom

        };

        return createMobMesh(modelPieceArray,modelTextureArray, "textures/skeleton.png");
    }
}
