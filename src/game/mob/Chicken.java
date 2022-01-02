package game.mob;

import org.joml.Math;
import org.joml.Vector3f;

import static engine.FancyMath.randomDirFloat;
import static engine.time.Time.getDelta;
import static game.chunk.Chunk.getBlock;
import static game.collision.Collision.applyInertia;
import static game.mob.Mob.registerMob;
import static game.mob.MobMeshBuilder.calculateMobTexture;
import static game.mob.MobMeshBuilder.createMobMesh;
import static game.mob.MobUtilityCode.doHeadCode;
import static game.mob.MobUtilityCode.mobSmoothRotation;

public class Chicken {
    private static final float accelerationMultiplier  = 0.03f;
    final private static float maxWalkSpeed = 2.f;
    final private static float movementAcceleration = 900.f;

    private final static MobInterface mobInterface = new MobInterface() {
        @Override
        public void onTick(MobObject thisMob) {

            double delta = getDelta();

            thisMob.timer += delta;

            if (thisMob.globalID == 1){
                System.out.println(thisMob.animationTimer);
            }

            if (thisMob.timer > 1.5f) {
                thisMob.stand = !thisMob.stand;
                thisMob.timer = (float) Math.random() * -2f;
                thisMob.rotation = (float) (Math.toDegrees(Math.PI * Math.random() * randomDirFloat()));
            }



            //head test
            //thisObject.bodyRotations[0] = new Vector3f((float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),(float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),0);
            float animation = (float) Math.toDegrees(Math.sin(thisMob.animationTimer * Math.PI * 2f));
            thisMob.bodyRotations[2].z = animation + 57;
            thisMob.bodyRotations[3].z = -animation - 57f;

            thisMob.bodyRotations[4].x = -animation;
            thisMob.bodyRotations[5].x = animation;


            float bodyYaw = Math.toRadians(thisMob.rotation) + (float) Math.PI;

            thisMob.inertia.x +=  (Math.sin(-bodyYaw) * accelerationMultiplier) * movementAcceleration * delta;
            thisMob.inertia.z +=  (Math.cos(bodyYaw) * accelerationMultiplier) * movementAcceleration * delta;

            Vector3f inertia2D = new Vector3f(thisMob.inertia.x, 0, thisMob.inertia.z);

            float maxSpeed = maxWalkSpeed;

            if (thisMob.health <= 0){
                maxSpeed = 0.01f;
            }

            boolean onGround = applyInertia(thisMob.pos, thisMob.inertia, false, thisMob.width, thisMob.height, true, false, true, false, false);

            if (thisMob.animationTimer >= 1f) {
                thisMob.animationTimer = 0f;
            }

            if (inertia2D.length() > maxSpeed) {
                inertia2D = inertia2D.normalize().mul(maxSpeed);
                thisMob.inertia.x = inertia2D.x;
                thisMob.inertia.z = inertia2D.z;
            }

            thisMob.animationTimer += thisMob.pos.distance(thisMob.oldPos) / 2f;

            if (thisMob.animationTimer >= 1f) {
                thisMob.animationTimer -= 1f;
            }


            thisMob.onGround = onGround;



            if (thisMob.health > 0) {
                //check for block in front
                if (onGround) {
                    double x = Math.sin(-bodyYaw);
                    double z = Math.cos(bodyYaw);

                    if (getBlock((int) Math.floor(x + thisMob.pos.x), (int) Math.floor(thisMob.pos.y), (int) Math.floor(z + thisMob.pos.z)) > 0) {
                        thisMob.inertia.y += 8.75f;
                    }
                }
            }

            mobSmoothRotation(thisMob);
            doHeadCode(thisMob);
        }
    };

    private static final float yOffsetCorrection = -0.1f;

    private static final Vector3f[] bodyOffsets = new Vector3f[]{
            new Vector3f(0,0.925f + yOffsetCorrection,-0.2815f),
            new Vector3f(0,0.8f + yOffsetCorrection,0),
            new Vector3f(-0.2185f,0.8f + yOffsetCorrection,0),
            new Vector3f(0.2185f,0.8f + yOffsetCorrection,0),
            new Vector3f(-0.09f,0.425f + yOffsetCorrection,-0.015f),
            new Vector3f(0.09f,0.425f + yOffsetCorrection,-0.015f),
    };

    private static final Vector3f[] bodyRotations = new Vector3f[]{
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
    };

    public static void registerChickenMob(){
        registerMob(new MobDefinition("chicken", "hurt",false, (byte) 7, createMesh(), bodyOffsets, bodyRotations,1f, 0.35f, mobInterface));
    }


    private static int[] createMesh(){

        final float modelScale = 0.25f; //lazy way to fix

        final float headYAdjustment = -0.25f;

        final float beakOffset = -0.625f * modelScale;
        final float beakHeight = 0.75f * modelScale;

        final float wattleOffset = -0.5f * modelScale;
        final float wattleHeight = 0.25f * modelScale;

        final float[][][] modelPieceArray = new float[][][]{
                //head
                {
                    //base
                    {-0.525f * modelScale, (0.0f * modelScale) + headYAdjustment, -0.375f * modelScale, 0.525f * modelScale, (1.5f * modelScale) + headYAdjustment, 0.375f * modelScale},
                    //beak
                    {-0.525f * modelScale, (-0.25f * modelScale) + beakHeight + headYAdjustment, (-0.25f * modelScale) + beakOffset, 0.525f * modelScale, (0.25f * modelScale) + beakHeight + headYAdjustment, (0.25f * modelScale) + beakOffset},
                    //wattle (the red thing)
                    {-0.2625f * modelScale, (-0.25f * modelScale) + wattleHeight + headYAdjustment, (-0.125f * modelScale) + wattleOffset, 0.2625f * modelScale, (0.25f * modelScale) + wattleHeight + headYAdjustment, (0.125f * modelScale) + wattleOffset}
                },

                //body
                {{-0.75f * modelScale,-1.5f * modelScale,-1f * modelScale,0.75f * modelScale,0.0f * modelScale, modelScale}},
                //right wing
                {{-0.125f * modelScale,-1f * modelScale,-0.75f * modelScale,  0.125f * modelScale,0f * modelScale,0.75f * modelScale}},
                //left wing
                {{-0.125f * modelScale,-1f * modelScale,-0.75f * modelScale,  0.125f * modelScale,0f * modelScale,0.75f * modelScale}},

                //right leg
                {{-0.375f * modelScale,-1.25f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.0f * modelScale,0.375f * modelScale}},
                //left leg
                {{-0.375f * modelScale,-1.25f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.0f * modelScale,0.375f * modelScale}},

        };


        float textureWidth = 64f;
        final float textureHeight = 32f;

        float[][][] modelTextureArray = new float[][][]{
                //head
                {
                        //base

                        //back
                        calculateMobTexture(7, 3, 11, 9, textureWidth, textureHeight),
                        //front
                        calculateMobTexture(3, 3, 7, 9, textureWidth, textureHeight),
                        //right
                        calculateMobTexture(0, 3, 3, 9, textureWidth, textureHeight),
                        //left
                        calculateMobTexture(11, 3, 14, 9, textureWidth, textureHeight),
                        //top
                        calculateMobTexture(22, 4, 25, 8, textureWidth, textureHeight),
                        //bottom
                        calculateMobTexture(25, 4, 28, 8, textureWidth, textureHeight),

                        //beak

                        //back
                        calculateMobTexture(22, 0, 24, 4, textureWidth, textureHeight),
                        //front
                        calculateMobTexture(16, 2, 20, 4, textureWidth, textureHeight),
                        //right
                        calculateMobTexture(14, 2, 16, 4, textureWidth, textureHeight),
                        //left
                        calculateMobTexture(20, 2, 22, 4, textureWidth, textureHeight),
                        //top
                        calculateMobTexture(24, 0, 26, 4, textureWidth, textureHeight),
                        //bottom
                        calculateMobTexture(22, 0, 24, 4, textureWidth, textureHeight),


                        //wattle

                        //back
                        calculateMobTexture(16, 4, 18, 6, textureWidth, textureHeight),
                        //front
                        calculateMobTexture(16, 6, 18, 8, textureWidth, textureHeight),
                        //right
                        calculateMobTexture(18, 4, 19, 6, textureWidth, textureHeight),
                        //left
                        calculateMobTexture(19, 4, 20, 6, textureWidth, textureHeight),
                        //top
                        calculateMobTexture(20, 6, 21, 8, textureWidth, textureHeight),
                        //bottom
                        calculateMobTexture(19, 6, 20, 8, textureWidth, textureHeight),

                },

                //body
                //back
                {calculateMobTexture(8, 19, 14, 25, textureWidth, textureHeight),
                //front
                calculateMobTexture(0, 19, 6, 25, textureWidth, textureHeight),
                //right
                calculateMobTexture(5, 26, 13, 32, textureWidth, textureHeight),
                //left
                calculateMobTexture(23, 26, 31, 32, textureWidth, textureHeight),
                //top
                calculateMobTexture(32, 26, 40, 32, textureWidth, textureHeight),
                //bottom
                calculateMobTexture(14, 26, 22, 32, textureWidth, textureHeight)},


                //right wing
                //back
                {calculateMobTexture(43, 19, 44, 23, textureWidth, textureHeight), //dark
                //front
                calculateMobTexture(44, 19, 45, 23, textureWidth, textureHeight), //light
                //right
                calculateMobTexture(37, 19, 43, 23, textureWidth, textureHeight), //dark
                //left
                calculateMobTexture(45, 19, 51, 23, textureWidth, textureHeight), //light
                //top
                calculateMobTexture(41, 15, 47, 16, textureWidth, textureHeight), //shoulder
                //bottom
                calculateMobTexture(41, 16, 47, 17, textureWidth, textureHeight)}, //palm


                //left wing
                //back
                {calculateMobTexture(43, 19, 44, 23, textureWidth, textureHeight), //dark
                //front
                calculateMobTexture(44, 19, 45, 23, textureWidth, textureHeight), //light
                //right
                calculateMobTexture(37, 19, 43, 23, textureWidth, textureHeight), //dark
                //left
                calculateMobTexture(45, 19, 51, 23, textureWidth, textureHeight), //light
                //top
                calculateMobTexture(41, 15, 47, 16, textureWidth, textureHeight), //shoulder
                //bottom
                calculateMobTexture(41, 16, 47, 17, textureWidth, textureHeight)}, //palm



                //right leg
                //back
                {calculateMobTexture(35, 3, 38, 8, textureWidth, textureHeight), //dark
                //front
                calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //light
                //right
                calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //dark
                //left
                calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //light
                //top
                calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //top
                //bottom
                calculateMobTexture(32, 0, 35, 3, textureWidth, textureHeight)}, //bottom


                //left leg
                //back
                {calculateMobTexture(35, 3, 38, 8, textureWidth, textureHeight), //dark
                //front
                calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //light
                //right
                calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //dark
                //left
                calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //light
                //top
                calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //top
                //bottom
                calculateMobTexture(32, 0, 35, 3, textureWidth, textureHeight)}, //bottom
        };

        return createMobMesh(modelPieceArray,modelTextureArray, "textures/chicken.png");
    }
}
