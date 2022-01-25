package game.entity.mob;

import engine.graphics.Mesh;
import engine.time.Delta;
import game.chunk.Chunk;
import game.entity.EntityContainer;
import game.entity.collision.Collision;
import org.joml.*;
import org.joml.Math;

public class Skeleton extends Mob {
    private final Vector2f workerVector2f = new Vector2f();

    private final Vector3f[] bodyOffsets = new Vector3f[]{
            new Vector3f(0,0.8f + 0.5f,0),
            new Vector3f(0,0.8f + 0.5f,0),
            new Vector3f(-0.24f,0.725f + 0.5f,0),
            new Vector3f(0.24f,0.725f + 0.5f,0),
            new Vector3f(-0.09f,0.17f + 0.5f,0),
            new Vector3f(0.09f,0.17f + 0.5f,0),

    };

    private final Vector3f[] bodyRotations = new Vector3f[]{
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,10f),
            new Vector3f(0,0,-10f),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),

    };

    private final Mesh[] mesh;

    private final MobInterface mobInterface = new MobInterface() {
        @Override
        public void onTick(Chunk chunk, Collision collision, Mob thisMob, Delta delta) {

            double dtime = delta.getDelta();

            float thisMobTimer = getTimer();
            float thisMobAnimationTimer = getAnimationTimer();
            float thisMobRotation = getRotation();
            int thisMobHealth = getHealth();

            Vector3d thisMobPos = getPos();
            Vector3d thisMobOldPos = getOldPos();
            Vector3f[] thisMobBodyRotations = getBodyRotations();
            Vector3f thisMobInertia = getInertia();

            thisMobTimer += dtime;

            //debug output
            /*
            if (thisMob == 1){
                System.out.println(thisMobAnimationTimer);
            }
             */

            if (thisMobTimer > 1.5f) {
                boolean thisMobStand = getIfStanding();
                setStanding(!thisMobStand);
                thisMobTimer = (float)Math.random() * -2f;
                setRotation((float) (Math.toDegrees(Math.PI * Math.random() * randomDir())));
            }


            //head test
            //thisObject.bodyRotations[0] = new Vector3f((float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),(float)Math.toDegrees(Math.sin(thisObject.animationTimer * Math.PI * 2f) * 1.65f),0);

            float animation = (float) Math.toDegrees(Math.sin(thisMobAnimationTimer * Math.PI * 2f));
            thisMobBodyRotations[2].x = animation;
            thisMobBodyRotations[3].x = -animation;

            thisMobBodyRotations[4].x = -animation;
            thisMobBodyRotations[5].x = animation;



            float bodyYaw = Math.toRadians(thisMobRotation) + (float) Math.PI;

            //aka mr bones
            float accelerationMultiplier = 0.03f;
            float movementAcceleration = 900.f;
            thisMobInertia.x +=  (Math.sin(-bodyYaw) * accelerationMultiplier) * movementAcceleration * dtime;
            thisMobInertia.z +=  (Math.cos(bodyYaw) * accelerationMultiplier) * movementAcceleration * dtime;

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

            if (thisMobHealth > 0) {
                //check for block in front
                if (onGround) {
                    double x = Math.sin(-bodyYaw);
                    double z = Math.cos(bodyYaw);

                    if (chunk.getBlock(new Vector3i((int) Math.floor(x + thisMobPos.x), (int) Math.floor(thisMobPos.y), (int) Math.floor(z + thisMobPos.z))) > 0) {
                        thisMobInertia.y += 8.75f;
                    }
                }
            }

            setAnimationTimer(thisMobAnimationTimer);
            setTimer(thisMobTimer);
        }
    };


    public Skeleton(MobMeshBuilder mobMeshBuilder, EntityContainer entityContainer, String name, Vector3d pos, Vector3f inertia) {
        super(entityContainer, "skeleton", pos, inertia, 1.9f, 0.25f, 7);

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


    //public void registerSkeletonMob(){
        //registerMob("skeleton", "hurt",false, (byte) 7, createMesh(), bodyOffsets, bodyRotations,1.9f, 0.25f, mobInterface);
    //}

    private Mesh[] createMesh(MobMeshBuilder mobMeshBuilder){
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
                {mobMeshBuilder.calculateMobTexture(24, 8, 32, 16, textureWidth, textureHeight),
                //front
                        mobMeshBuilder.calculateMobTexture(8, 8, 16, 16, textureWidth, textureHeight),
                //right
                        mobMeshBuilder.calculateMobTexture(0, 8, 8, 16, textureWidth, textureHeight),
                //left
                        mobMeshBuilder.calculateMobTexture(16, 8, 24, 16, textureWidth, textureHeight),
                //top
                        mobMeshBuilder.calculateMobTexture(8, 0, 16, 8, textureWidth, textureHeight),
                //bottom
                        mobMeshBuilder.calculateMobTexture(16, 0, 24, 8, textureWidth, textureHeight)},

                //body
                //back
                {mobMeshBuilder.calculateMobTexture(34, 20, 42, 32, textureWidth, textureHeight),
                //front
                        mobMeshBuilder.calculateMobTexture(21, 20, 29, 32, textureWidth, textureHeight),
                //right
                        mobMeshBuilder.calculateMobTexture(17, 20, 21, 32, textureWidth, textureHeight),
                //left
                        mobMeshBuilder.calculateMobTexture(30, 20, 34, 32, textureWidth, textureHeight),
                //top
                        mobMeshBuilder.calculateMobTexture(20, 16, 28, 20, textureWidth, textureHeight),
                //bottom
                        mobMeshBuilder.calculateMobTexture(28, 16, 36, 20, textureWidth, textureHeight)},



                //right arm
                //back
                {mobMeshBuilder.calculateMobTexture(0, 18, 2, 32, textureWidth, textureHeight), //dark
                //front
                        mobMeshBuilder.calculateMobTexture(0, 18, 2, 32, textureWidth, textureHeight), //dark
                //right
                        mobMeshBuilder.calculateMobTexture(2, 18, 4, 32, textureWidth, textureHeight), //dark
                //left
                        mobMeshBuilder.calculateMobTexture(2, 18, 4, 32, textureWidth, textureHeight), //dark
                //top
                        mobMeshBuilder.calculateMobTexture(4, 16, 6, 18, textureWidth, textureHeight), //shoulder
                //bottom
                        mobMeshBuilder.calculateMobTexture(6, 16, 8, 18, textureWidth, textureHeight)}, //palm



                //left arm
                //back
                {mobMeshBuilder.calculateMobTexture(0, 18, 2, 32, textureWidth, textureHeight), //dark
                //front
                        mobMeshBuilder.calculateMobTexture(0, 18, 2, 32, textureWidth, textureHeight), //dark
                //right
                        mobMeshBuilder.calculateMobTexture(2, 18, 4, 32, textureWidth, textureHeight), //dark
                //left
                        mobMeshBuilder.calculateMobTexture(2, 18, 4, 32, textureWidth, textureHeight), //dark
                //top
                        mobMeshBuilder.calculateMobTexture(4, 16, 6, 18, textureWidth, textureHeight), //shoulder
                //bottom
                        mobMeshBuilder.calculateMobTexture(6, 16, 8, 18, textureWidth, textureHeight)}, //palm



                //right leg
                //back
                {mobMeshBuilder.calculateMobTexture(43, 18, 45, 32, textureWidth, textureHeight), //dark
                //front
                        mobMeshBuilder.calculateMobTexture(43, 18, 45, 32, textureWidth, textureHeight), //dark
                //right
                        mobMeshBuilder.calculateMobTexture(45, 18, 47, 32, textureWidth, textureHeight), //dark
                //left
                        mobMeshBuilder.calculateMobTexture(45, 18, 47, 32, textureWidth, textureHeight), //dark
                //top
                        mobMeshBuilder.calculateMobTexture(47, 16, 49, 18, textureWidth, textureHeight), //top
                //bottom
                        mobMeshBuilder.calculateMobTexture(49, 16, 51, 18, textureWidth, textureHeight)}, //bottom


                //left leg
                //back
                {mobMeshBuilder.calculateMobTexture(43, 18, 45, 32, textureWidth, textureHeight), //dark
                //front
                        mobMeshBuilder.calculateMobTexture(43, 18, 45, 32, textureWidth, textureHeight), //dark
                //right
                        mobMeshBuilder.calculateMobTexture(45, 18, 47, 32, textureWidth, textureHeight), //dark
                //left
                        mobMeshBuilder.calculateMobTexture(45, 18, 47, 32, textureWidth, textureHeight), //dark
                //top
                        mobMeshBuilder.calculateMobTexture(47, 16, 49, 18, textureWidth, textureHeight), //top
                //bottom
                        mobMeshBuilder.calculateMobTexture(49, 16, 51, 18, textureWidth, textureHeight)}, //bottom

        };

        return mobMeshBuilder.createMobMesh(modelPieceArray,modelTextureArray, "textures/skeleton.png");
    }
}
