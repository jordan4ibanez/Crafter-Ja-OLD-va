package game.entity.mob;

import engine.graphics.Mesh;
import engine.time.Delta;
import game.blocks.BlockDefinitionContainer;
import game.chunk.Chunk;
import game.entity.EntityContainer;
import game.entity.collision.Collision;
import org.joml.*;
import org.joml.Math;

public class Cow extends Mob {

    BlockDefinitionContainer blockDefinitionContainer = new BlockDefinitionContainer();

    private final Vector2f worker2f = new Vector2f();

    private final Vector3f[] bodyRotations = new Vector3f[]{
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
    };

    private final Vector3f[] bodyOffsets = new Vector3f[]{
            //head
            new Vector3f(0,0.885f + 0.5f,-0.945f),
            //body
            new Vector3f(0,0.7f + 0.5f,0),

            //front right leg
            new Vector3f(-0.235f,0.3f + 0.5f,-0.5f),

            //front left leg
            new Vector3f(0.235f,0.3f + 0.5f,-0.5f),

            //rear right leg
            new Vector3f(-0.235f,0.3f + 0.5f,0.5f),

            //rear left leg
            new Vector3f(0.235f,0.3f + 0.5f,0.5f),
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
            Vector3f[] thisMobBodyRotations = getBodyRotations();
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
            thisMobBodyRotations[2].x = animation;
            thisMobBodyRotations[3].x = -animation;

            thisMobBodyRotations[4].x = -animation;
            thisMobBodyRotations[5].x = animation;

            float yaw = Math.toRadians(thisMobRotation) + (float) Math.PI;

            float accelerationMultiplier = 0.04f;
            float movementAcceleration = 900.f;
            thisMobInertia.x += (Math.sin(-yaw) * accelerationMultiplier) * movementAcceleration * dtime;
            thisMobInertia.z += (Math.cos(yaw) * accelerationMultiplier) * movementAcceleration * dtime;

            worker2f.set(thisMobInertia.x, thisMobInertia.z);

            float maxSpeed = 2.f;

            if (thisMobHealth <= 0) {
                maxSpeed = 0.01f;
            }

            if (worker2f.length() > maxSpeed) {
                worker2f.normalize().mul(maxSpeed);
                thisMobInertia.x = worker2f.x;
                thisMobInertia.z = worker2f.y;
            }

            boolean onGround = collision.applyInertia(thisMobPos, thisMobInertia, false, getWidth(),getHeight(), true, false, true, false, false);

            thisMobAnimationTimer += thisMobPos.distance(thisMobOldPos) / 2f;

            if (thisMobAnimationTimer >= 1f) {
                thisMobAnimationTimer = 0f;
            }

            setOnGround(onGround);


            if (thisMobHealth > 0) {
                //check if swimming
                byte block = chunk.getBlock(new Vector3i((int) Math.floor(thisMobPos.x), (int) Math.floor(thisMobPos.y), (int) Math.floor(thisMobPos.z)));
                if (block > -1 && blockDefinitionContainer.getIfLiquid(block)) {
                    thisMobInertia.y += 100f * dtime;
                }

                //check for block in front
                if (onGround) {
                    double x = Math.sin(-yaw);
                    double z = Math.cos(yaw);

                    if (chunk.getBlock(new Vector3i((int) Math.floor(x + thisMobPos.x), (int) Math.floor(thisMobPos.y), (int) Math.floor(z + thisMobPos.z))) > 0) {
                        thisMobInertia.y += 8.75f;
                    }
                }
            }

            setAnimationTimer(thisMobAnimationTimer);
            setTimer(thisMobTimer);
        }
    };

    public Cow(MobMeshBuilder mobMeshBuilder, EntityContainer entityContainer, Vector3d pos, Vector3f inertia, float width, float height, int health) {
        super(entityContainer, "cow", pos, inertia, width, height, health);
        this.mesh = createMesh(mobMeshBuilder);
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

    //public void registerCowMob(){
        //registerMob("cow", "cow_hurt_1",true, (byte) 6, createMesh(), bodyOffsets, bodyRotations,1.5f, 0.45f, mobInterface);
    //}


    private Mesh[] createMesh(MobMeshBuilder mobMeshBuilder){

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
                {mobMeshBuilder.calculateMobTexture(5,0,13,8,textureWidth,textureHeight),
                //front
                        mobMeshBuilder.calculateMobTexture(6,8,14,16,textureWidth,textureHeight),
                //right
                        mobMeshBuilder.calculateMobTexture(0,8,6,16,textureWidth,textureHeight),
                //left
                        mobMeshBuilder.calculateMobTexture(14,8,20,16,textureWidth,textureHeight),
                //top
                        mobMeshBuilder.calculateMobTexture(38,0,46,6,textureWidth,textureHeight),
                //bottom
                        mobMeshBuilder.calculateMobTexture(38,7,46,13,textureWidth,textureHeight)},

                //body
                //back
                {mobMeshBuilder.calculateMobTexture(52,10,64,20,textureWidth,textureHeight),
                //front
                        mobMeshBuilder.calculateMobTexture(52,21,64,31,textureWidth,textureHeight),
                //right
                        mobMeshBuilder.calculateMobTexture(16,22,34,32,textureWidth,textureHeight),
                //left
                        mobMeshBuilder.calculateMobTexture(46,0,64,10,textureWidth,textureHeight),
                //top
                        mobMeshBuilder.calculateMobTexture(34,20,52,32,textureWidth,textureHeight),
                //bottom
                        mobMeshBuilder.calculateMobTexture(20,1,38,13,textureWidth,textureHeight)},


                //right arm
                //back
                {mobMeshBuilder.calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //dark
                //front
                        mobMeshBuilder.calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //right
                        mobMeshBuilder.calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                        mobMeshBuilder.calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //light
                //top
                        mobMeshBuilder.calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                        mobMeshBuilder.calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm


                //left arm
                //back
                {mobMeshBuilder.calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //dark
                //front
                        mobMeshBuilder.calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //right
                        mobMeshBuilder.calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                        mobMeshBuilder.calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //light
                //top
                        mobMeshBuilder.calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                        mobMeshBuilder.calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm


                //right leg
                //back
                {mobMeshBuilder.calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //dark
                //front
                        mobMeshBuilder.calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //right
                        mobMeshBuilder.calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                        mobMeshBuilder.calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //light
                //top
                        mobMeshBuilder.calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                        mobMeshBuilder.calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm

                //left leg
                //back
                {mobMeshBuilder.calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //dark
                //front
                        mobMeshBuilder.calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //right
                        mobMeshBuilder.calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                        mobMeshBuilder.calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //light
                //top
                        mobMeshBuilder.calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                        mobMeshBuilder.calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm
        };

        return mobMeshBuilder.createMobMesh(modelPieceArray,modelTextureArray, "textures/cow.png");
    }
}
