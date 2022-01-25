package game.entity.mob;

import engine.graphics.Mesh;
import engine.time.Delta;
import game.chunk.Chunk;
import game.entity.EntityContainer;
import game.entity.collision.Collision;
import org.joml.*;
import org.joml.Math;

import static org.joml.Math.floor;

public class Chicken extends Mob {

    private final Vector2f workerVector2f = new Vector2f();


    private final Vector3f[] bodyOffsets = new Vector3f[]{
            new Vector3f(0,0.925f - 0.1f,-0.2815f),
            new Vector3f(0,0.8f - 0.1f,0),
            new Vector3f(-0.2185f,0.8f - 0.1f,0),
            new Vector3f(0.2185f,0.8f - 0.1f,0),
            new Vector3f(-0.09f,0.425f - 0.1f,-0.015f),
            new Vector3f(0.09f,0.425f - 0.1f,-0.015f),
    };

    private final Vector3f[] bodyRotations = new Vector3f[]{
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
    };

    private final Mesh[] mesh;

    private final MobInterface mobInterface = new MobInterface() {
        @Override
        public void onTick(Chunk chunk, Collision collision, Mob mob, Delta delta) {

            double dtime = delta.getDelta();

            float thisMobTimer = getTimer();
            float thisMobAnimationTimer = getAnimationTimer();
            float thisMobRotation = getRotation();
            int thisMobHealth = getHealth();

            //pointers
            Vector3d thisMobPos = getPos();
            Vector3d thisMobOldPos = getOldPos();
            Vector3f[] thisMobBodyRotations = bodyRotations;
            Vector3f thisMobInertia = getInertia();

            thisMobTimer += dtime;


            if (thisMobTimer > 1.5f) {
                boolean thisMobStand = getIfStanding();
                setStanding(!thisMobStand);
                thisMobTimer = (float)Math.random() * -2f;
                setRotation((float) (Math.toDegrees(Math.PI * Math.random() * randomDir())));
            }



            //head test
            //thisObject.bodyRotations[0] = new Vector3f((float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),(float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),0);
            float animation = (float) Math.toDegrees(Math.sin(thisMobAnimationTimer * Math.PI * 2f));
            thisMobBodyRotations[2].z = animation + 57;
            thisMobBodyRotations[3].z = -animation - 57f;

            thisMobBodyRotations[4].x = -animation;
            thisMobBodyRotations[5].x = animation;


            float bodyYaw = Math.toRadians(thisMobRotation) + (float) Math.PI;

            float accelerationMultiplier = 0.03f;
            float movementAcceleration = 900.f;
            thisMobInertia.x +=  (Math.sin(-bodyYaw) * accelerationMultiplier) * movementAcceleration * dtime;
            thisMobInertia.z +=  (Math.cos(bodyYaw)  * accelerationMultiplier) * movementAcceleration * dtime;

            workerVector2f.set(thisMobInertia.x, thisMobInertia.z);

            float maxSpeed = 2.f;

            if (thisMobHealth <= 0){
                maxSpeed = 0.01f;
            }

            boolean onGround = collision.applyInertia(thisMobPos, thisMobInertia, false, getWidth(), getHeight(), true, false, true, false, false);

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


            setOnGround(onGround);
            setRotation(thisMobRotation);


            if (thisMobHealth > 0) {
                //check for block in front
                if (onGround) {
                    double x = Math.sin(-bodyYaw);
                    double z = Math.cos(bodyYaw);

                    if (chunk.getBlock(new Vector3i((int) floor(x + thisMobPos.x), (int) floor(thisMobPos.y), (int) floor(z + thisMobPos.z))) > 0) {
                        thisMobInertia.y += 8.75f;
                    }
                }
            }

            setAnimationTimer(thisMobAnimationTimer);
            setTimer(thisMobTimer);
        }
    };

    public Chicken(MobMeshBuilder mobMeshBuilder, EntityContainer entityContainer, Vector3d pos, Vector3f inertia, float width, float height, int health) {
        super(entityContainer, "chicken", pos, inertia,  width, height, health);

        mesh = createMesh(mobMeshBuilder);
    }

    @Override
    public MobInterface getMobInterface(){
        return mobInterface;
    }

    @Override
    public Vector3f[] getBodyOffsets() {
        return bodyOffsets;
    }

    @Override
    public Vector3f[] getBodyRotations() {
        return bodyRotations;
    }

    public Mesh[] getMesh() {
        return mesh;
    }



    //public void registerChickenMob(){
        //registerMob("chicken", "hurt",false, (byte) 7, createMesh(), bodyOffsets, bodyRotations,1f, 0.35f, mobInterface);
    //}


    private Mesh[] createMesh(MobMeshBuilder mobMeshBuilder){

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
                        mobMeshBuilder.calculateMobTexture(7, 3, 11, 9, textureWidth, textureHeight),
                        //front
                        mobMeshBuilder.calculateMobTexture(3, 3, 7, 9, textureWidth, textureHeight),
                        //right
                        mobMeshBuilder.calculateMobTexture(0, 3, 3, 9, textureWidth, textureHeight),
                        //left
                        mobMeshBuilder.calculateMobTexture(11, 3, 14, 9, textureWidth, textureHeight),
                        //top
                        mobMeshBuilder.calculateMobTexture(22, 4, 25, 8, textureWidth, textureHeight),
                        //bottom
                        mobMeshBuilder.calculateMobTexture(25, 4, 28, 8, textureWidth, textureHeight),

                        //beak

                        //back
                        mobMeshBuilder.calculateMobTexture(22, 0, 24, 4, textureWidth, textureHeight),
                        //front
                        mobMeshBuilder.calculateMobTexture(16, 2, 20, 4, textureWidth, textureHeight),
                        //right
                        mobMeshBuilder.calculateMobTexture(14, 2, 16, 4, textureWidth, textureHeight),
                        //left
                        mobMeshBuilder.calculateMobTexture(20, 2, 22, 4, textureWidth, textureHeight),
                        //top
                        mobMeshBuilder.calculateMobTexture(24, 0, 26, 4, textureWidth, textureHeight),
                        //bottom
                        mobMeshBuilder.calculateMobTexture(22, 0, 24, 4, textureWidth, textureHeight),


                        //wattle

                        //back
                        mobMeshBuilder.calculateMobTexture(16, 4, 18, 6, textureWidth, textureHeight),
                        //front
                        mobMeshBuilder.calculateMobTexture(16, 6, 18, 8, textureWidth, textureHeight),
                        //right
                        mobMeshBuilder.calculateMobTexture(18, 4, 19, 6, textureWidth, textureHeight),
                        //left
                        mobMeshBuilder.calculateMobTexture(19, 4, 20, 6, textureWidth, textureHeight),
                        //top
                        mobMeshBuilder.calculateMobTexture(20, 6, 21, 8, textureWidth, textureHeight),
                        //bottom
                        mobMeshBuilder.calculateMobTexture(19, 6, 20, 8, textureWidth, textureHeight),

                },

                //body
                //back
                {mobMeshBuilder.calculateMobTexture(8, 19, 14, 25, textureWidth, textureHeight),
                //front
                        mobMeshBuilder.calculateMobTexture(0, 19, 6, 25, textureWidth, textureHeight),
                //right
                        mobMeshBuilder.calculateMobTexture(5, 26, 13, 32, textureWidth, textureHeight),
                //left
                        mobMeshBuilder.calculateMobTexture(23, 26, 31, 32, textureWidth, textureHeight),
                //top
                        mobMeshBuilder.calculateMobTexture(32, 26, 40, 32, textureWidth, textureHeight),
                //bottom
                        mobMeshBuilder.calculateMobTexture(14, 26, 22, 32, textureWidth, textureHeight)},


                //right wing
                //back
                {mobMeshBuilder.calculateMobTexture(43, 19, 44, 23, textureWidth, textureHeight), //dark
                //front
                        mobMeshBuilder.calculateMobTexture(44, 19, 45, 23, textureWidth, textureHeight), //light
                //right
                        mobMeshBuilder.calculateMobTexture(37, 19, 43, 23, textureWidth, textureHeight), //dark
                //left
                        mobMeshBuilder.calculateMobTexture(45, 19, 51, 23, textureWidth, textureHeight), //light
                //top
                        mobMeshBuilder.calculateMobTexture(41, 15, 47, 16, textureWidth, textureHeight), //shoulder
                //bottom
                        mobMeshBuilder.calculateMobTexture(41, 16, 47, 17, textureWidth, textureHeight)}, //palm


                //left wing
                //back
                {mobMeshBuilder.calculateMobTexture(43, 19, 44, 23, textureWidth, textureHeight), //dark
                //front
                        mobMeshBuilder.calculateMobTexture(44, 19, 45, 23, textureWidth, textureHeight), //light
                //right
                        mobMeshBuilder.calculateMobTexture(37, 19, 43, 23, textureWidth, textureHeight), //dark
                //left
                        mobMeshBuilder.calculateMobTexture(45, 19, 51, 23, textureWidth, textureHeight), //light
                //top
                        mobMeshBuilder.calculateMobTexture(41, 15, 47, 16, textureWidth, textureHeight), //shoulder
                //bottom
                        mobMeshBuilder.calculateMobTexture(41, 16, 47, 17, textureWidth, textureHeight)}, //palm



                //right leg
                //back
                {mobMeshBuilder.calculateMobTexture(35, 3, 38, 8, textureWidth, textureHeight), //dark
                //front
                        mobMeshBuilder.calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //light
                //right
                        mobMeshBuilder.calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //dark
                //left
                        mobMeshBuilder.calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //light
                //top
                        mobMeshBuilder.calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //top
                //bottom
                        mobMeshBuilder.calculateMobTexture(32, 0, 35, 3, textureWidth, textureHeight)}, //bottom


                //left leg
                //back
                {mobMeshBuilder.calculateMobTexture(35, 3, 38, 8, textureWidth, textureHeight), //dark
                //front
                        mobMeshBuilder.calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //light
                //right
                        mobMeshBuilder.calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //dark
                //left
                        mobMeshBuilder.calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //light
                //top
                        mobMeshBuilder.calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //top
                //bottom
                        mobMeshBuilder.calculateMobTexture(32, 0, 35, 3, textureWidth, textureHeight)}, //bottom
        };

        return mobMeshBuilder.createMobMesh(modelPieceArray,modelTextureArray, "textures/chicken.png");
    }
}
