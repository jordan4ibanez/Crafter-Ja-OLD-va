package game.mob;

import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;

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

public class Exploder {

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
            if (thisMob == 1){
                System.out.println(thisMobAnimationTimer);
            }

            if (thisMobTimer > 1.5f) {
                boolean thisMobStand = getIfMobStanding(thisMob);
                setIfMobStanding(thisMob, !thisMobStand);
                thisMobTimer = (float)Math.random() * -2f;
                setMobRotation(thisMob, (float) (Math.toDegrees(Math.PI * Math.random() * randomDirFloat())));
            }

            //head test
            //thisObject.bodyRotations[0] = new Vector3f((float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),(float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),0);
            //these are the legs
            float animation = (float) Math.toDegrees(Math.sin(thisMobAnimationTimer * Math.PI * 2f) / 1.6f);
            thisMobBodyRotations[2].x = -animation;
            thisMobBodyRotations[3].x = animation;
            //the legs are diagonally synced, so the multiplier is flipped
            thisMobBodyRotations[4].x = animation;
            thisMobBodyRotations[5].x = -animation;


            float bodyYaw = Math.toRadians(thisMobRotation) + (float) Math.PI;


            thisMobInertia.x +=  (Math.sin(-bodyYaw) * accelerationMultiplier) * movementAcceleration * delta;
            thisMobInertia.z +=  (Math.cos(bodyYaw)  * accelerationMultiplier) * movementAcceleration * delta;


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

            thisMobAnimationTimer += thisMobPos.distance(thisMobOldPos) / 1.5f;

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

    private static final float yOffsetCorrection = 0.1525f;

    private static final Vector3f[] bodyOffsets = new Vector3f[]{
            new Vector3f(0,0.985f + yOffsetCorrection,0),
            new Vector3f(0,0.985f + yOffsetCorrection,0),

            //rear legs
            new Vector3f(-0.121f,0.17f + yOffsetCorrection,0.27f),
            new Vector3f(0.121f,0.17f + yOffsetCorrection,0.27f),

            //front legs
            new Vector3f(-0.121f,0.17f + yOffsetCorrection,-0.27f),
            new Vector3f(0.121f,0.17f + yOffsetCorrection,-0.27f),

    };

    private static final Vector3f[] bodyRotations = new Vector3f[]{
            new Vector3f(0,0,0), //head
            new Vector3f(0,0,0), //body

            new Vector3f(0,0,0), //rear right leg
            new Vector3f(0,0,0), //rear left leg

            new Vector3f(0,0,0), //front right leg
            new Vector3f(0,0,0), //front left leg
    };

    public static void registerExploderMob(){
        registerMob("exploder", "hurt",true, (byte) 7, createMesh(), bodyOffsets, bodyRotations,1.9f, 0.25f, mobInterface);
    }


    private static int[] createMesh(){
        final float modelScale = 0.325f; //lazy way to fix

        final float[][][] modelPieceArray = new float[][][]{
                //head
                {{-0.75f * modelScale, 0.0f * modelScale, -0.75f * modelScale, 0.75f * modelScale, 1.5f * modelScale, 0.75f * modelScale}},
                //body
                {{-0.75f * modelScale, -2.5f * modelScale, -0.45f * modelScale, 0.75f * modelScale, 0.0f * modelScale, 0.45f * modelScale}},
                //rear right leg
                {{-0.375f * modelScale,-1.f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.0f * modelScale,0.375f * modelScale}},
                //rear left leg
                {{-0.375f * modelScale,-1f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.0f * modelScale,0.375f * modelScale}},
                //front right leg
                {{-0.375f * modelScale,-1.f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.0f * modelScale,0.375f * modelScale}},
                //front left leg
                {{-0.375f * modelScale,-1f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.0f * modelScale,0.375f * modelScale}},
        };


        float textureWidth = 64f;
        final float textureHeight = 32f;

        float[][][] modelTextureArray = new float[][][]{
                //head
                //back
                {calculateMobTexture(24,8,32,16, textureWidth, textureHeight),
                //front
                calculateMobTexture(8,8,16,16, textureWidth, textureHeight),
                //right
                calculateMobTexture(0,8,8,16, textureWidth, textureHeight),
                //left
                calculateMobTexture(16,8,24,16, textureWidth, textureHeight),
                //top
                calculateMobTexture(8,0,16,8, textureWidth, textureHeight),
                //bottom
                calculateMobTexture(16,0,24,8, textureWidth, textureHeight)},

                //body
                //back
                {calculateMobTexture(20,20,28,32, textureWidth, textureHeight),
                //front
                calculateMobTexture(28,20,36,32, textureWidth, textureHeight),
                //right
                calculateMobTexture(16,20,20,32, textureWidth, textureHeight),
                //left
                calculateMobTexture(36,20,40,32, textureWidth, textureHeight),
                //top
                calculateMobTexture(20,16,28,20, textureWidth, textureHeight),
                //bottom
                calculateMobTexture(28,16,36,20, textureWidth, textureHeight)},


                //rear right leg
                //back
                {calculateMobTexture(4,20,8,26, textureWidth, textureHeight),
                //front
                calculateMobTexture(8,20,12,26, textureWidth, textureHeight),
                //right
                calculateMobTexture(0,20,4,26, textureWidth, textureHeight),
                //left
                calculateMobTexture(12,20,16,26, textureWidth, textureHeight),
                //top
                calculateMobTexture(4,16,8,20, textureWidth, textureHeight),
                //bottom
                calculateMobTexture(8,16,12,20, textureWidth, textureHeight)},

                //rear left leg
                //back
                {calculateMobTexture(4,20,8,26, textureWidth, textureHeight),
                //front
                calculateMobTexture(8,20,12,26, textureWidth, textureHeight),
                //right
                calculateMobTexture(0,20,4,26, textureWidth, textureHeight),
                //left
                calculateMobTexture(12,20,16,26, textureWidth, textureHeight),
                //top
                calculateMobTexture(4,16,8,20, textureWidth, textureHeight),
                //bottom
                calculateMobTexture(8,16,12,20, textureWidth, textureHeight)},


                //front right leg
                //back
                {calculateMobTexture(4,20,8,26, textureWidth, textureHeight),
                //front
                calculateMobTexture(8,20,12,26, textureWidth, textureHeight),
                //right
                calculateMobTexture(0,20,4,26, textureWidth, textureHeight),
                //left
                calculateMobTexture(12,20,16,26, textureWidth, textureHeight),
                //top
                calculateMobTexture(4,16,8,20, textureWidth, textureHeight),
                //bottom
                calculateMobTexture(8,16,12,20, textureWidth, textureHeight)},

                //front left leg
                //back
                {calculateMobTexture(4,20,8,26, textureWidth, textureHeight),
                //front
                calculateMobTexture(8,20,12,26, textureWidth, textureHeight),
                //right
                calculateMobTexture(0,20,4,26, textureWidth, textureHeight),
                //left
                calculateMobTexture(12,20,16,26, textureWidth, textureHeight),
                //top
                calculateMobTexture(4,16,8,20, textureWidth, textureHeight),
                //bottom
                calculateMobTexture(8,16,12,20, textureWidth, textureHeight)},
        };

        return createMobMesh(modelPieceArray,modelTextureArray, "textures/exploder.png");
    }
}
