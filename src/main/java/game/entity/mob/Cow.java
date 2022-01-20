package game.entity.mob;

import engine.graphics.Mesh;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.FancyMath.randomDirFloat;
import static engine.time.Time.getDelta;
import static game.blocks.BlockDefinition.getIfLiquid;
import static game.chunk.Chunk.getBlock;
import static game.entity.collision.Collision.applyInertia;
import static game.entity.mob.MobDefinition.*;

public class Cow {
    private static final float accelerationMultiplier  = 0.04f;
    final private static float maxWalkSpeed = 2.f;
    final private static float movementAcceleration = 900.f;
    private static final Vector2f worker2f = new Vector2f();

    private final static MobInterface mobInterface = new MobInterface() {
        @Override
        public void onTick(int thisMob) {

            double delta = getDelta();

            //primitive
            int thisMobDefinitionID = MobObject.getMobID(thisMob);
            float thisMobTimer = MobObject.getMobTimer(thisMob);
            float thisMobAnimationTimer = MobObject.getMobAnimationTimer(thisMob);
            float thisMobRotation = MobObject.getMobRotation(thisMob);
            byte thisMobHealth = MobObject.getMobHealth(thisMob);

            //pointers
            Vector3d thisMobPos = MobObject.getMobPos(thisMob);
            Vector3d thisMobOldPos = MobObject.getMobOldPos(thisMob);
            Vector3f[] thisMobBodyRotations = MobObject.getMobBodyRotations(thisMob);
            Vector3f thisMobInertia = MobObject.getMobInertia(thisMob);

            thisMobTimer += delta;

            if (thisMobTimer > 1.5f) {
                boolean thisMobStand = MobObject.getIfMobStanding(thisMob);
                MobObject.setIfMobStanding(thisMob, !thisMobStand);
                thisMobTimer = (float)Math.random() * -2f;
                MobObject.setMobRotation(thisMob, (float) (Math.toDegrees(Math.PI * Math.random() * randomDirFloat())));
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

            worker2f.set(thisMobInertia.x, thisMobInertia.z);

            float maxSpeed = maxWalkSpeed;

            if (thisMobHealth <= 0) {
                maxSpeed = 0.01f;
            }

            if (worker2f.length() > maxSpeed) {
                worker2f.normalize().mul(maxSpeed);
                thisMobInertia.x = worker2f.x;
                thisMobInertia.z = worker2f.y;
            }

            boolean onGround = applyInertia(thisMobPos, thisMobInertia, false, getMobDefinitionWidth(thisMobDefinitionID),getMobDefinitionHeight(thisMobDefinitionID), true, false, true, false, false);

            thisMobAnimationTimer += thisMobPos.distance(thisMobOldPos) / 2f;

            if (thisMobAnimationTimer >= 1f) {
                thisMobAnimationTimer = 0f;
            }

            MobObject.setIfMobOnGround(thisMob, onGround);


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

            MobObject.setMobAnimationTimer(thisMob, thisMobAnimationTimer);
            MobObject.setMobTimer(thisMob, thisMobTimer);


            MobUtilityCode.mobSmoothRotation(thisMob);
            MobUtilityCode.doHeadCode(thisMob);
        }
    };


    private static final float yHeightCorrection = 0.5f;

    private static final Vector3f[] bodyOffsets = new Vector3f[]{
            //head
            new Vector3f(0,0.885f + yHeightCorrection,-0.945f),
            //body
            new Vector3f(0,0.7f + yHeightCorrection,0),

            //front right leg
            new Vector3f(-0.235f,0.3f + yHeightCorrection,-0.5f),

            //front left leg
            new Vector3f(0.235f,0.3f + yHeightCorrection,-0.5f),

            //rear right leg
            new Vector3f(-0.235f,0.3f + yHeightCorrection,0.5f),

            //rear left leg
            new Vector3f(0.235f,0.3f + yHeightCorrection,0.5f),
    };

    private static final Vector3f[] bodyRotations = new Vector3f[]{
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
    };

    public static void registerCowMob(){
        registerMob("cow", "cow_hurt_1",true, (byte) 6, createMesh(), bodyOffsets, bodyRotations,1.5f, 0.45f, mobInterface);
    }


    private static Mesh[] createMesh(){

        float size = 0.395f; //lazy way to fix

        float[][][] modelPieceArray = new float[][][]{
                //head
                {{-0.65f * size,-0.65f * size,-0.275f * size,0.65f * size,0.65f * size,0.65f * size}},
                //body
                {{-1.f * size,-1.f * size,-1.75f * size, size,0.75f * size,1.75f * size}},
                //front right leg
                {{-0.375f * size,-2f * size,-0.375f * size,  0.375f * size,0 * size,0.375f * size}},
                //front left leg
                {{-0.375f * size,-2f * size,-0.375f * size,  0.375f * size,0 * size,0.375f * size}},
                //rear right leg
                {{-0.375f * size,-2f * size,-0.375f * size,  0.375f * size,0 * size,0.375f * size}},
                //rear left leg
                {{-0.375f * size,-2f * size,-0.375f * size,  0.375f * size,0 * size,0.375f * size}},
        };
        final float textureWidth = 64f;
        final float textureHeight = 32f;

        float[][][] modelTextureArray = new float[][][]{
                //head
                //back
                {MobMeshBuilder.calculateMobTexture(5,0,13,8,textureWidth,textureHeight),
                //front
                MobMeshBuilder.calculateMobTexture(6,8,14,16,textureWidth,textureHeight),
                //right
                MobMeshBuilder.calculateMobTexture(0,8,6,16,textureWidth,textureHeight),
                //left
                MobMeshBuilder.calculateMobTexture(14,8,20,16,textureWidth,textureHeight),
                //top
                MobMeshBuilder.calculateMobTexture(38,0,46,6,textureWidth,textureHeight),
                //bottom
                MobMeshBuilder.calculateMobTexture(38,7,46,13,textureWidth,textureHeight)},

                //body
                //back
                {MobMeshBuilder.calculateMobTexture(52,10,64,20,textureWidth,textureHeight),
                //front
                MobMeshBuilder.calculateMobTexture(52,21,64,31,textureWidth,textureHeight),
                //right
                MobMeshBuilder.calculateMobTexture(16,22,34,32,textureWidth,textureHeight),
                //left
                MobMeshBuilder.calculateMobTexture(46,0,64,10,textureWidth,textureHeight),
                //top
                MobMeshBuilder.calculateMobTexture(34,20,52,32,textureWidth,textureHeight),
                //bottom
                MobMeshBuilder.calculateMobTexture(20,1,38,13,textureWidth,textureHeight)},


                //right arm
                //back
                {MobMeshBuilder.calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //dark
                //front
                MobMeshBuilder.calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //right
                MobMeshBuilder.calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                MobMeshBuilder.calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //light
                //top
                MobMeshBuilder.calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                MobMeshBuilder.calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm


                //left arm
                //back
                {MobMeshBuilder.calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //dark
                //front
                MobMeshBuilder.calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //right
                MobMeshBuilder.calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                MobMeshBuilder.calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //light
                //top
                MobMeshBuilder.calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                MobMeshBuilder.calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm


                //right leg
                //back
                {MobMeshBuilder.calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //dark
                //front
                MobMeshBuilder.calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //right
                MobMeshBuilder.calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                MobMeshBuilder.calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //light
                //top
                MobMeshBuilder.calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                MobMeshBuilder.calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm

                //left leg
                //back
                {MobMeshBuilder.calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //dark
                //front
                MobMeshBuilder.calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //right
                MobMeshBuilder.calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                MobMeshBuilder.calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //light
                //top
                MobMeshBuilder.calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                MobMeshBuilder.calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm
        };

        return MobMeshBuilder.createMobMesh(modelPieceArray,modelTextureArray, "textures/cow.png");
    }
}
