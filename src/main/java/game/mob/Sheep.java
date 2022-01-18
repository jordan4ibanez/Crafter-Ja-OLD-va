package game.mob;

import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.base.FancyMath.randomDirFloat;
import static engine.time.Time.getDelta;
import static game.blocks.BlockDefinition.getIfLiquid;
import static game.chunk.Chunk.getBlock;
import static game.collision.Collision.applyInertia;
import static game.item.ItemEntity.throwItem;
import static game.mob.MobDefinition.*;
import static game.mob.MobMeshBuilder.calculateMobTexture;
import static game.mob.MobMeshBuilder.createMobMesh;
import static game.mob.MobObject.*;
import static game.mob.MobUtilityCode.doHeadCode;
import static game.mob.MobUtilityCode.mobSmoothRotation;

public class Sheep {
    private static final float accelerationMultiplier  = 0.04f;
    final private static float maxWalkSpeed = 2.f;
    final private static float movementAcceleration = 900.f;
    private static final Vector2f workerVector2f = new Vector2f();

    private final static MobInterface woolInterface = new MobInterface() {

        @Override
        public void onPunch(int thisMob){
            Vector3d thisMobPos = getMobPos(thisMob);
            //adding dirt for a placeholder
            for (byte i = 0; i < 3; i++) {

                throwItem("dirt", thisMobPos.x, thisMobPos.y + 1d, thisMobPos.z, 1, 0);
            }
            //shaved sheep always comes after wool sheep
            setMobID(thisMob, getMobID(thisMob) + 1);
        }

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

            float yaw = Math.toRadians(thisMobRotation) + (float) Math.PI;

            thisMobInertia.x += (Math.sin(-yaw) * accelerationMultiplier) * movementAcceleration * delta;
            thisMobInertia.z += (Math.cos(yaw)  * accelerationMultiplier) * movementAcceleration * delta;

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

            thisMobAnimationTimer += delta * (workerVector2f.length() / maxSpeed);

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

            mobSmoothRotation(thisMob);
            doHeadCode(thisMob);
        }
    };


    //this is a method pointer
    private final static MobInterface shavedInterface = new MobInterface() {
        @Override
        public void onTick(int thisMob) {
            //link them together to prevent boilerplate
            woolInterface.onTick(thisMob);
        }
    };


    private static final float yPosCorrection = 0.25f;
    private static final Vector3f[] bodyOffsets = new Vector3f[]{
            //head
            new Vector3f(0,0.75f + yPosCorrection,-0.5f),
            //body
            new Vector3f(0,0.6f + yPosCorrection,0),

            //front right leg
            new Vector3f(-0.15f,0.45f + yPosCorrection,-0.32f),

            //front left leg
            new Vector3f(0.15f,0.45f + yPosCorrection,-0.32f),

            //rear right leg
            new Vector3f(-0.15f,0.45f + yPosCorrection,0.32f),

            //rear left leg
            new Vector3f(0.15f,0.45f + yPosCorrection,0.32f),
    };

    private static final Vector3f[] bodyRotations = new Vector3f[]{
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
    };

    public static void registerSheepMob(){
        registerMob("sheep_wool", "sheep_1",true, (byte) 6, createWoolMesh(), bodyOffsets, bodyRotations,0.9f, 0.45f, woolInterface);
        registerMob("sheep_shaved", "sheep_2",true, (byte) 6, createShavedMesh(), bodyOffsets, bodyRotations,0.9f, 0.45f, shavedInterface);
    }


    private static int[] createWoolMesh(){

        float size = 0.25f; //lazy way to fix
        float woolSize = 1.25f; //ultra lazy way to fix

        float[][][] modelPieceArray = new float[][][]{
//                head
                {{-0.7f * size,-0.6f * size,-0.8f * size,0.7f * size,0.6f * size,0.8f * size}},
//                body
                {{-1 * size * woolSize, -0.7f * size * woolSize ,-1.75f * size * woolSize, 1f * size  * woolSize,0.7f * size * woolSize,1.75f * size * woolSize}},
//                //front right leg
                {{-0.375f * size,-2.5f * size,-0.375f * size,  0.375f * size,0,0.375f * size}},
//                //front left leg
                {{-0.375f * size,-2.5f * size,-0.375f * size,  0.375f * size,0,0.375f * size}},
                //rear right leg
                {{-0.375f * size,-2.5f * size,-0.375f * size,  0.375f * size,0,0.375f * size}},
                //rear left leg
                {{-0.375f * size,-2.5f * size,-0.375f * size,  0.375f * size,0,0.375f * size}},
        };
        final float textureWidth = 64f;
        final float textureHeight = 32f;

        float[][][] modelTextureArray = new float[][][]{
                //head
                //back
                {calculateMobTexture(8,0,16,6,textureWidth,textureHeight),
                //front
                calculateMobTexture(8,6,16,12,textureWidth,textureHeight),
                //right
                calculateMobTexture(0,6,8,12,textureWidth,textureHeight),
                //left
                calculateMobTexture(16,6,24,12,textureWidth,textureHeight),
                //top
                calculateMobTexture(8,0,16,6,textureWidth,textureHeight),
                //bottom
                calculateMobTexture(8,0,16,6,textureWidth,textureHeight)},

                //body
                //back
                {calculateMobTexture(40,20,48,26,textureWidth,textureHeight),
                //front
                calculateMobTexture(40,26,48,32,textureWidth,textureHeight),
                //right
                calculateMobTexture(24,20,40,26,textureWidth,textureHeight),
                //left
                calculateMobTexture(24,26,40,32,textureWidth,textureHeight),
                //top
                calculateMobTexture(48,16,64,24,textureWidth,textureHeight),
                //bottom
                calculateMobTexture(48,24,64,32,textureWidth,textureHeight)},


                //right arm
                //back
                {calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //dark
                //front
                calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //light
                //right
                calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //top
                calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm

                //left arm
                //back
                {calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //dark
                //front
                calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //light
                //right
                calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //top
                calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm


                //right leg
                //back
                {calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //dark
                //front
                calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //light
                //right
                calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //top
                calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm

                //left leg
                //back
                {calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //dark
                //front
                calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //light
                //right
                calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //top
                calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm
        };


        return createMobMesh(modelPieceArray,modelTextureArray, "textures/sheep_wool.png");
    }

    private static int[] createShavedMesh(){

        float size = 0.25f; //lazy way to fix
        float bodySize = 0.9f;

        float[][][] modelPieceArray = new float[][][]{
                //head
                {{-0.7f * size,-0.6f * size,-0.8f * size,0.7f * size,0.6f * size,0.8f * size}},
                //body
                {{-1.f * size * bodySize, -0.7f * size * bodySize,-1.75f * size * bodySize, 1f * size * bodySize,0.7f * size * bodySize,1.75f * size * bodySize}},
                //front right leg
                {{-0.375f * size,-2.5f * size,-0.375f * size,  0.375f * size,0,0.375f * size}},
                //front left leg
                {{-0.375f * size,-2.5f * size,-0.375f * size,  0.375f * size,0,0.375f * size}},
                //rear right leg
                {{-0.375f * size,-2.5f * size,-0.375f * size,  0.375f * size,0,0.375f * size}},
                //rear left leg
                {{-0.375f * size,-2.5f * size,-0.375f * size,  0.375f * size,0,0.375f * size}},
        };
        final float textureWidth = 64f;
        final float textureHeight = 32f;

        float[][][] modelTextureArray = new float[][][]{
                //head
                //back
                {calculateMobTexture(8,0,16,6,textureWidth,textureHeight),
                //front
                calculateMobTexture(8,6,16,12,textureWidth,textureHeight),
                //right
                calculateMobTexture(0,6,8,12,textureWidth,textureHeight),
                //left
                calculateMobTexture(16,6,24,12,textureWidth,textureHeight),
                //top
                calculateMobTexture(8,0,16,6,textureWidth,textureHeight),
                //bottom
                calculateMobTexture(8,0,16,6,textureWidth,textureHeight)},

                //body
                //back
                {calculateMobTexture(40,20,48,26,textureWidth,textureHeight),
                //front
                calculateMobTexture(40,26,48,32,textureWidth,textureHeight),
                //right
                calculateMobTexture(24,20,40,26,textureWidth,textureHeight),
                //left
                calculateMobTexture(24,26,40,32,textureWidth,textureHeight),
                //top
                calculateMobTexture(48,16,64,24,textureWidth,textureHeight),
                //bottom
                calculateMobTexture(48,24,64,32,textureWidth,textureHeight)},


                //right arm
                //back
                {calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //dark
                //front
                calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //light
                //right
                calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //top
                calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm

                //left arm
                //back
                {calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //dark
                //front
                calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //light
                //right
                calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //top
                calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm


                //right leg
                //back
                {calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //dark
                //front
                calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //light
                //right
                calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //top
                calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm

                //left leg
                //back
                {calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //dark
                //front
                calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //light
                //right
                calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //top
                calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm
        };

        return createMobMesh(modelPieceArray,modelTextureArray, "textures/sheep_shaved.png");
    }
}
