package game.mob;

import org.joml.*;
import org.joml.Math;

import static engine.FancyMath.randomDirFloat;
import static engine.time.Time.getDelta;
import static game.chunk.Chunk.getBlock;
import static game.collision.Collision.applyInertia;
import static game.mob.MobDefinition.*;
import static game.mob.MobMeshBuilder.calculateMobTexture;
import static game.mob.MobMeshBuilder.createMobMesh;
import static game.mob.MobObject.*;
import static game.mob.MobUtilityCode.doHeadCode;
import static game.mob.MobUtilityCode.mobSmoothRotation;

public class Human {
    private static final float accelerationMultiplier  = 0.03f;
    private static final float maxWalkSpeed = 2.f;
    private static final float movementAcceleration = 900.f;
    private static final Vector2f worker2f = new Vector2f();

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

            //a debug for the animation timer
            if (thisMob == 1){
                System.out.println(thisMobAnimationTimer);
            }


            if (thisMobTimer > 1.5f) {
                boolean thisMobStand = getIfMobStanding(thisMob);
                setIfMobStanding(thisMob,!thisMobStand);
                setMobTimer(thisMob, (float)Math.random() * -2f);
                setMobRotation(thisMob, (float) (Math.toDegrees(Math.PI * Math.random() * randomDirFloat())));
            }


            //head test
            //thisObject.bodyRotations[0] = new Vector3f((float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),(float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),0);
            float animation = (float) Math.toDegrees(Math.sin(thisMobAnimationTimer * Math.PI * 2f));
            thisMobBodyRotations[2].x = animation;
            thisMobBodyRotations[3].x = -animation;

            thisMobBodyRotations[4].x = -animation;
            thisMobBodyRotations[5].x = animation;


            //thisMob.animationTimer += delta * 2f;

            float bodyYaw = Math.toRadians(thisMobRotation) + (float) Math.PI;

            thisMobInertia.x += (Math.sin(-bodyYaw) * accelerationMultiplier) * movementAcceleration * delta;
            thisMobInertia.z += (Math.cos(bodyYaw)  * accelerationMultiplier) * movementAcceleration * delta;

            worker2f.set(thisMobInertia.x, thisMobInertia.z);

            float maxSpeed = maxWalkSpeed;

            if (thisMobHealth <= 0){
                maxSpeed = 0.01f;
            }

            boolean onGround = applyInertia(thisMobPos, thisMobInertia, false, getMobDefinitionWidth(thisMobDefinitionID), getMobDefinitionHeight(thisMobDefinitionID), true, false, true, false, false);

            if (thisMobAnimationTimer >= 1f) {
                thisMobAnimationTimer = 0f;

            }

            if (worker2f.length() > maxSpeed) {
                worker2f.normalize().mul(maxSpeed);
                thisMobInertia.x = worker2f.x;
                thisMobInertia.z = worker2f.y;
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
            thisMobOldPos.set(thisMobPos);

            mobSmoothRotation(thisMob);
            doHeadCode(thisMob);
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
        registerMob("human", "hurt",true, (byte) 7, createMesh(), bodyOffsets, bodyRotations,1.9f, 0.25f, mobInterface);
    }


    private static int[] createMesh(){
        final float modelScale = 0.25f; //lazy way to fix

        final float[][][] modelPieceArray = new float[][][]{
                //head
                {{-0.75f * modelScale, 0.0f * modelScale, -0.75f * modelScale, 0.75f * modelScale, 1.5f * modelScale, 0.75f * modelScale}},
                //body
                {{-0.75f * modelScale, -2.5f * modelScale, -0.45f * modelScale, 0.75f * modelScale, 0.0f * modelScale, 0.45f * modelScale}},
                //right arm
                {{-0.375f * modelScale, -2.2f * modelScale, -0.375f * modelScale, 0.375f * modelScale, 0.3f * modelScale, 0.375f * modelScale}},
                //left arm
                {{-0.375f * modelScale, -2.2f * modelScale, -0.375f * modelScale, 0.375f * modelScale, 0.3f * modelScale, 0.375f * modelScale}},
                //right leg
                {{-0.375f * modelScale, -2.5f * modelScale, -0.375f * modelScale, 0.375f * modelScale, 0.0f * modelScale, 0.375f * modelScale}},
                //left leg
                {{-0.375f * modelScale,-2.5f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.0f * modelScale,0.375f * modelScale}},
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
                {calculateMobTexture(32, 20, 40, 30, textureWidth, textureHeight),
                //front
                calculateMobTexture(20, 20, 28, 30, textureWidth, textureHeight),
                //right
                calculateMobTexture(28, 20, 32, 30, textureWidth, textureHeight),
                //left
                calculateMobTexture(16, 20, 20, 30, textureWidth, textureHeight),
                //top
                calculateMobTexture(20, 16, 28, 20, textureWidth, textureHeight),
                //bottom
                calculateMobTexture(28, 16, 36, 20, textureWidth, textureHeight)},


                //right arm
                //back
                {calculateMobTexture(48, 20, 52, 32, textureWidth, textureHeight), //dark
                //front
                calculateMobTexture(44, 20, 48, 32, textureWidth, textureHeight), //light
                //right
                calculateMobTexture(48, 20, 52, 32, textureWidth, textureHeight), //dark
                //left
                calculateMobTexture(44, 20, 48, 32, textureWidth, textureHeight), //light
                //top
                calculateMobTexture(44, 16, 48, 20, textureWidth, textureHeight), //shoulder
                //bottom
                calculateMobTexture(48, 16, 52, 20, textureWidth, textureHeight)}, //palm

                //left arm
                //back
                {calculateMobTexture(48, 20, 52, 32, textureWidth, textureHeight), //dark
                //front
                calculateMobTexture(44, 20, 48, 32, textureWidth, textureHeight), //light
                //right
                calculateMobTexture(44, 20, 48, 32, textureWidth, textureHeight), //light
                //left
                calculateMobTexture(48, 20, 52, 32, textureWidth, textureHeight), //dark
                //top
                calculateMobTexture(44, 16, 48, 20, textureWidth, textureHeight), //shoulder
                //bottom
                calculateMobTexture(48, 16, 52, 20, textureWidth, textureHeight)}, //palm


                //right leg
                //back
                {calculateMobTexture(0, 20, 4, 32, textureWidth, textureHeight), //dark
                //front
                calculateMobTexture(4, 20, 8, 32, textureWidth, textureHeight), //light
                //right
                calculateMobTexture(8, 20, 12, 32, textureWidth, textureHeight), //dark
                //left
                calculateMobTexture(12, 20, 16, 32, textureWidth, textureHeight), //light
                //top
                calculateMobTexture(4, 16, 8, 20, textureWidth, textureHeight), //top
                //bottom
                calculateMobTexture(8, 16, 12, 20, textureWidth, textureHeight)}, //bottom

                //left leg
                //back
                {calculateMobTexture(0, 20, 4, 32, textureWidth, textureHeight), //dark
                //front
                calculateMobTexture(4, 20, 8, 32, textureWidth, textureHeight), //light
                //right
                calculateMobTexture(12, 20, 16, 32, textureWidth, textureHeight), //light
                //left
                calculateMobTexture(8, 20, 12, 32, textureWidth, textureHeight), //dark
                //top
                calculateMobTexture(4, 16, 8, 20, textureWidth, textureHeight), //top
                //bottom
                calculateMobTexture(8, 16, 12, 20, textureWidth, textureHeight)}, //bottom
        };

        return createMobMesh(modelPieceArray,modelTextureArray, "textures/player.png");
    }
}
