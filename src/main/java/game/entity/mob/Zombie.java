package game.entity.mob;

import engine.graphics.Mesh;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.FancyMath.randomDirFloat;
import static engine.time.Delta.getDelta;
import static game.chunk.Chunk.getBlock;
import static game.entity.collision.Collision.applyInertia;
import static game.entity.mob.MobDefinition.*;

public class Zombie {

    private static final float accelerationMultiplier  = 0.03f;
    final private static float maxWalkSpeed = 2.f;
    final private static float movementAcceleration = 900.f;
    private static final Vector2f workerVector2f = new Vector2f();

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

            //debug
            /*
            if (thisMob == 1){
                System.out.println(thisMobAnimationTimer);
            }
             */

            if (thisMobTimer > 1.5f) {
                boolean thisMobStand = MobObject.getIfMobStanding(thisMob);
                MobObject.setIfMobStanding(thisMob, !thisMobStand);
                thisMobTimer = (float)Math.random() * -2f;
                MobObject.setMobRotation(thisMob, (float) (Math.toDegrees(Math.PI * Math.random() * randomDirFloat())));
            }



            //head test
            //thisObject.bodyRotations[0] = new Vector3f((float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),(float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),0);
            //these are the arms
            //thisMob.bodyRotations[2].x = (float) Math.toDegrees(Math.sin(thisMob.animationTimer * Math.PI * 2f));
            //thisMob.bodyRotations[3].x = (float) Math.toDegrees(Math.sin(thisMob.animationTimer * Math.PI * -2f));
            //these are the legs
            float animation = (float) Math.toDegrees(Math.sin(thisMobAnimationTimer * Math.PI * 2f));
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

            MobObject.setIfMobOnGround(thisMob, onGround);



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

            MobObject.setMobAnimationTimer(thisMob, thisMobAnimationTimer);
            MobObject.setMobTimer(thisMob, thisMobTimer);

            MobUtilityCode.mobSmoothRotation(thisMob);
            MobUtilityCode.doHeadCode(thisMob);
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
            new Vector3f(0,0,0), //head
            new Vector3f(0,0,0), //body

            new Vector3f(-90,0,0), //arms
            new Vector3f(-90,0,0),

            new Vector3f(0,0,0), //legs
            new Vector3f(0,0,0),
    };

    public static void registerZombieMob(){
        registerMob("zombie", "hurt",true, (byte) 7, createMesh(), bodyOffsets, bodyRotations,1.9f, 0.25f, mobInterface);
    }


    private static Mesh[] createMesh(){
        final float modelScale = 0.25f; //lazy way to fix

        final float[][][] modelPieceArray = new float[][][]{
                //head
                {{-0.75f * modelScale,0.0f * modelScale,-0.75f * modelScale,0.75f * modelScale,1.5f * modelScale,0.75f * modelScale}},
                //body
                {{-0.75f * modelScale,-2.5f * modelScale,-0.45f * modelScale,0.75f * modelScale,0.0f * modelScale,0.45f * modelScale}},
                //right arm
                {{-0.375f * modelScale,-2.2f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.3f * modelScale,0.375f * modelScale}},
                //left arm
                {{-0.375f * modelScale,-2.2f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.3f * modelScale,0.375f * modelScale}},
                //right leg
                {{-0.375f * modelScale,-2.5f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.0f * modelScale,0.375f * modelScale}},
                //left leg
                {{-0.375f * modelScale,-2.5f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.0f * modelScale,0.375f * modelScale}},
        };


        float textureWidth = 64f;
        final float textureHeight = 32f;

        float[][][] modelTextureArray = new float[][][]{
                //head
                //back
                {MobMeshBuilder.calculateMobTexture(24,8,32,16, textureWidth, textureHeight),
                //front
                MobMeshBuilder.calculateMobTexture(8,8,16,16, textureWidth, textureHeight),
                //right
                MobMeshBuilder.calculateMobTexture(0,8,8,16, textureWidth, textureHeight),
                //left
                MobMeshBuilder.calculateMobTexture(16,8,24,16, textureWidth, textureHeight),
                //top
                MobMeshBuilder.calculateMobTexture(8,0,16,8, textureWidth, textureHeight),
                //bottom
                MobMeshBuilder.calculateMobTexture(16,0,24,8, textureWidth, textureHeight)},

                //body
                //back
                {MobMeshBuilder.calculateMobTexture(32,20,40,30, textureWidth, textureHeight),
                //front
                MobMeshBuilder.calculateMobTexture(20,20,28,30, textureWidth, textureHeight),
                //right
                MobMeshBuilder.calculateMobTexture(28,20,32,30, textureWidth, textureHeight),
                //left
                MobMeshBuilder.calculateMobTexture(16,20,20,30, textureWidth, textureHeight),
                //top
                MobMeshBuilder.calculateMobTexture(20,16,28,20, textureWidth, textureHeight),
                //bottom
                MobMeshBuilder.calculateMobTexture(28,16,36,20, textureWidth, textureHeight)},


                //right arm
                //back
                {MobMeshBuilder.calculateMobTexture(48,20,52,32, textureWidth, textureHeight), //dark
                //front
                MobMeshBuilder.calculateMobTexture(44,20,48,32, textureWidth, textureHeight), //light
                //right
                MobMeshBuilder.calculateMobTexture(48,20,52,32, textureWidth, textureHeight), //dark
                //left
                MobMeshBuilder.calculateMobTexture(44,20,48,32, textureWidth, textureHeight), //light
                //top
                MobMeshBuilder.calculateMobTexture(44,16,48,20, textureWidth, textureHeight), //shoulder
                //bottom
                MobMeshBuilder.calculateMobTexture(48,16,52,20, textureWidth, textureHeight)}, //palm

                //left arm
                //back
                {MobMeshBuilder.calculateMobTexture(48,20,52,32, textureWidth, textureHeight), //dark
                //front
                MobMeshBuilder.calculateMobTexture(44,20,48,32, textureWidth, textureHeight), //light
                //right
                MobMeshBuilder.calculateMobTexture(44,20,48,32, textureWidth, textureHeight), //light
                //left
                MobMeshBuilder.calculateMobTexture(48,20,52,32, textureWidth, textureHeight), //dark
                //top
                MobMeshBuilder.calculateMobTexture(44,16,48,20, textureWidth, textureHeight), //shoulder
                //bottom
                MobMeshBuilder.calculateMobTexture(48,16,52,20, textureWidth, textureHeight)}, //palm


                //right leg
                //back
                {MobMeshBuilder.calculateMobTexture(0,20,4,32, textureWidth, textureHeight), //dark
                //front
                MobMeshBuilder.calculateMobTexture(4,20,8,32, textureWidth, textureHeight), //light
                //right
                MobMeshBuilder.calculateMobTexture(8,20,12,32, textureWidth, textureHeight), //dark
                //left
                MobMeshBuilder.calculateMobTexture(12,20,16,32, textureWidth, textureHeight), //light
                //top
                MobMeshBuilder.calculateMobTexture(4,16,8,20, textureWidth, textureHeight), //top
                //bottom
                MobMeshBuilder.calculateMobTexture(8,16,12,20, textureWidth, textureHeight)}, //bottom

                //left leg
                //back
                {MobMeshBuilder.calculateMobTexture(0,20,4,32, textureWidth, textureHeight), //dark
                //front
                MobMeshBuilder.calculateMobTexture(4,20,8,32, textureWidth, textureHeight), //light
                //right
                MobMeshBuilder.calculateMobTexture(12,20,16,32, textureWidth, textureHeight), //light
                //left
                MobMeshBuilder.calculateMobTexture(8,20,12,32, textureWidth, textureHeight), //dark
                //top
                MobMeshBuilder.calculateMobTexture(4,16,8,20, textureWidth, textureHeight), //top
                //bottom
                MobMeshBuilder.calculateMobTexture(8,16,12,20, textureWidth, textureHeight)}, //bottom
        };

        return MobMeshBuilder.createMobMesh(modelPieceArray,modelTextureArray, "textures/zombie.png");
    }
}