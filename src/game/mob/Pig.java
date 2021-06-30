package game.mob;

import engine.graphics.Mesh;
import engine.graphics.Texture;
import org.joml.Math;
import org.joml.Vector3f;

import java.util.ArrayList;

import static engine.FancyMath.randomDirFloat;
import static engine.time.Time.getDelta;
import static game.blocks.BlockDefinition.getIfLiquid;
import static game.chunk.Chunk.getBlock;
import static game.collision.Collision.applyInertia;
import static game.mob.Mob.registerMob;
import static game.mob.MobMeshBuilder.calculateMobTexture;
import static game.mob.MobMeshBuilder.createMobMesh;
import static game.mob.MobUtilityCode.doHeadCode;
import static game.mob.MobUtilityCode.mobSmoothRotation;

public class Pig {
    private static final float accelerationMultiplier  = 0.04f;
    final private static float maxWalkSpeed = 2.f;
    final private static float movementAcceleration = 900.f;

    private final static MobInterface mobInterface = new MobInterface() {
        @Override
        public void onTick(MobObject thisMob) {

            double delta = getDelta();

            thisMob.timer += delta;

            if (thisMob.timer > 1.5f) {
                thisMob.stand = !thisMob.stand;
                thisMob.timer = (float) Math.random() * -2f;
                thisMob.rotation = (float) (Math.toDegrees(Math.PI * Math.random() * randomDirFloat()));
            }

            //head test
            //thisObject.bodyRotations[0] = new Vector3f((float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),(float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),0);
            float animation = (float) Math.toDegrees(Math.sin(thisMob.animationTimer * Math.PI * 2f));
            thisMob.bodyRotations[2].x = animation;
            thisMob.bodyRotations[3].x = -animation;

            thisMob.bodyRotations[4].x = -animation;
            thisMob.bodyRotations[5].x = animation;

            float yaw = Math.toRadians(thisMob.rotation) + (float) Math.PI;

            thisMob.inertia.x += (Math.sin(-yaw) * accelerationMultiplier) * movementAcceleration * delta;
            thisMob.inertia.z += (Math.cos(yaw) * accelerationMultiplier) * movementAcceleration * delta;

            Vector3f inertia2D = new Vector3f(thisMob.inertia.x, 0, thisMob.inertia.z);

            float maxSpeed = maxWalkSpeed;

            if (thisMob.health <= 0) {
                maxSpeed = 0.01f;
            }

            if (inertia2D.length() > maxSpeed) {
                inertia2D = inertia2D.normalize().mul(maxSpeed);
                thisMob.inertia.x = inertia2D.x;
                thisMob.inertia.z = inertia2D.z;
            }

            boolean onGround = applyInertia(thisMob.pos, thisMob.inertia, false, thisMob.width, thisMob.height, true, false, true, false, false);

            thisMob.animationTimer += delta * (inertia2D.length() / maxSpeed);

            if (thisMob.animationTimer >= 1f) {
                thisMob.animationTimer = 0f;
            }


            thisMob.onGround = onGround;


            if (thisMob.health > 0) {
                //check if swimming
                int block = getBlock((int) Math.floor(thisMob.pos.x), (int) Math.floor(thisMob.pos.y), (int) Math.floor(thisMob.pos.z));
                if (block > -1 && getIfLiquid(block)) {
                    thisMob.inertia.y += 100f * delta;
                }

                //check for block in front
                if (onGround) {
                    double x = Math.sin(-yaw);
                    double z = Math.cos(yaw);

                    if (getBlock((int) Math.floor(x + thisMob.pos.x), (int) Math.floor(thisMob.pos.y), (int) Math.floor(z + thisMob.pos.z)) > 0) {
                        thisMob.inertia.y += 8.75f;
                    }
                }
            }


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
        registerMob(new MobDefinition("pig", "oink",true, (byte) 6, createMesh(), bodyOffsets, bodyRotations,0.9f, 0.45f, mobInterface));
    }


    private static Mesh[] createMesh(){

        float size = 0.25f; //lazy way to fix

        float[][] modelPieceArray = new float[][]{
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
        final float textureWidth = 64f;
        final float textureHeight = 32f;

        float[][] modelTextureArray = new float[][]{
                //head

                //front
                calculateMobTexture(24,8,32,16,textureWidth,textureHeight),
                //back
                calculateMobTexture(8,8,16,16,textureWidth,textureHeight),
                //right
                calculateMobTexture(0,8,8,16,textureWidth,textureHeight),
                //left
                calculateMobTexture(16,8,24,16,textureWidth,textureHeight),
                //top
                calculateMobTexture(8,0,16,8,textureWidth,textureHeight),
                //bottom
                calculateMobTexture(16,0,24,8,textureWidth,textureHeight),

                //body

                //front
                calculateMobTexture(54,6,64,14,textureWidth,textureHeight),
                //back
                calculateMobTexture(44,6,54,14,textureWidth,textureHeight),
                //right
                calculateMobTexture(48,23,64,32,textureWidth,textureHeight),
                //left
                calculateMobTexture(48,23,64,32,textureWidth,textureHeight),
                //top
                calculateMobTexture(32,23,48,32,textureWidth,textureHeight),
                //bottom
                calculateMobTexture(48,14,64,23,textureWidth,textureHeight),


                //right arm

                //front
                calculateMobTexture(0,20,4,26,textureWidth,textureHeight), //dark
                //back
                calculateMobTexture(4,20,8,26,textureWidth,textureHeight), //light

                //right
                calculateMobTexture(8,20,12,26,textureWidth,textureHeight), //dark
                //left
                calculateMobTexture(12,20,16,26,textureWidth,textureHeight), //light

                //top
                calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                calculateMobTexture(8,16,12,20,textureWidth,textureHeight), //palm

                //left arm

                //front
                calculateMobTexture(0,20,4,26,textureWidth,textureHeight), //dark
                //back
                calculateMobTexture(4,20,8,26,textureWidth,textureHeight), //light

                //right
                calculateMobTexture(8,20,12,26,textureWidth,textureHeight), //dark
                //left
                calculateMobTexture(12,20,16,26,textureWidth,textureHeight), //light

                //top
                calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                calculateMobTexture(8,16,12,20,textureWidth,textureHeight), //palm


                //right leg

                //front
                calculateMobTexture(0,20,4,26,textureWidth,textureHeight), //dark
                //back
                calculateMobTexture(4,20,8,26,textureWidth,textureHeight), //light

                //right
                calculateMobTexture(8,20,12,26,textureWidth,textureHeight), //dark
                //left
                calculateMobTexture(12,20,16,26,textureWidth,textureHeight), //light

                //top
                calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                calculateMobTexture(8,16,12,20,textureWidth,textureHeight), //palm

                //left leg

                //front
                calculateMobTexture(0,20,4,26,textureWidth,textureHeight), //dark
                //back
                calculateMobTexture(4,20,8,26,textureWidth,textureHeight), //light

                //right
                calculateMobTexture(8,20,12,26,textureWidth,textureHeight), //dark
                //left
                calculateMobTexture(12,20,16,26,textureWidth,textureHeight), //light

                //top
                calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                calculateMobTexture(8,16,12,20,textureWidth,textureHeight), //palm
        };

        return createMobMesh(modelPieceArray,modelTextureArray, "textures/pig.png");
    }
}

