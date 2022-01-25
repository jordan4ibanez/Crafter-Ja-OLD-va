package game.entity.mob;

import engine.graphics.Mesh;
import engine.time.Delta;
import game.blocks.BlockDefinitionContainer;
import game.chunk.Chunk;
import game.entity.EntityContainer;
import game.entity.collision.Collision;
import org.joml.*;
import org.joml.Math;


public class Sheep extends Mob{

    private final Vector2f workerVector2f = new Vector2f();

    private final BlockDefinitionContainer blockDefinitionContainer = new BlockDefinitionContainer();

    private final Vector3f[] bodyOffsets = new Vector3f[]{
            //head
            new Vector3f(0,0.75f +  0.25f,-0.5f),
            //body
            new Vector3f(0,0.6f +  0.25f,0),

            //front right leg
            new Vector3f(-0.15f,0.45f +  0.25f,-0.32f),

            //front left leg
            new Vector3f(0.15f,0.45f +  0.25f,-0.32f),

            //rear right leg
            new Vector3f(-0.15f,0.45f +  0.25f,0.32f),

            //rear left leg
            new Vector3f(0.15f,0.45f +  0.25f,0.32f),
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

    private final MobInterface woolInterface = new MobInterface() {

        @Override
        public void onPunch(Mob thisMob, Delta delta){
            //Vector3d thisMobPos = getPos();
            //adding dirt for a placeholder
            //for (byte i = 0; i < 3; i++) {
                //throwItem("dirt", thisMobPos.x, thisMobPos.y + 1d, thisMobPos.z, 1, 0);
            //}
            //shaved sheep always comes after wool sheep
            //MobObject.setMobID(thisMob, MobObject.getMobID(thisMob) + 1);
        }

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
            thisMobInertia.z += (Math.cos(yaw)  * accelerationMultiplier) * movementAcceleration * dtime;

            workerVector2f.set(thisMobInertia.x, thisMobInertia.z);

            float maxSpeed = 2.f;

            if (thisMobHealth <= 0) {
                maxSpeed = 0.01f;
            }

            if (workerVector2f.length() > maxSpeed) {
                workerVector2f.normalize().mul(maxSpeed);
                thisMobInertia.x = workerVector2f.x;
                thisMobInertia.z = workerVector2f.y;
            }

            boolean onGround = collision.applyInertia(thisMobPos, thisMobInertia, false, getWidth(), getHeight(), true, false, true, false, false);

            thisMobAnimationTimer += dtime * (workerVector2f.length() / maxSpeed);

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


    //this is a method pointer
    private final MobInterface shavedInterface = new MobInterface() {
        @Override
        public void onTick(Chunk chunk, Collision collision, Mob thisMob, Delta delta) {
            //link them together to prevent boilerplate
            woolInterface.onTick(chunk, collision, thisMob, delta);
        }
    };

    public Sheep(MobMeshBuilder mobMeshBuilder, EntityContainer entityContainer, String name, Vector3d pos, Vector3f inertia) {
        super(entityContainer, name, pos, inertia, 0.9f, 0.45f, 6);

        mesh = createMesh(mobMeshBuilder);
    }

    @Override
    public MobInterface getMobInterface(){
        return woolInterface;
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


    //public void registerSheepMob(){
        //registerMob("sheep_wool", "sheep_1",true, (byte) 6, createWoolMesh(), bodyOffsets, bodyRotations,0.9f, 0.45f, woolInterface);
        //registerMob("sheep_shaved", "sheep_2",true, (byte) 6, createShavedMesh(), bodyOffsets, bodyRotations,0.9f, 0.45f, shavedInterface);
    //}


    private Mesh[] createMesh(MobMeshBuilder mobMeshBuilder){

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
                {mobMeshBuilder.calculateMobTexture(8,0,16,6,textureWidth,textureHeight),
                //front
                        mobMeshBuilder.calculateMobTexture(8,6,16,12,textureWidth,textureHeight),
                //right
                        mobMeshBuilder.calculateMobTexture(0,6,8,12,textureWidth,textureHeight),
                //left
                        mobMeshBuilder.calculateMobTexture(16,6,24,12,textureWidth,textureHeight),
                //top
                        mobMeshBuilder.calculateMobTexture(8,0,16,6,textureWidth,textureHeight),
                //bottom
                        mobMeshBuilder.calculateMobTexture(8,0,16,6,textureWidth,textureHeight)},

                //body
                //back
                {mobMeshBuilder.calculateMobTexture(40,20,48,26,textureWidth,textureHeight),
                //front
                        mobMeshBuilder.calculateMobTexture(40,26,48,32,textureWidth,textureHeight),
                //right
                        mobMeshBuilder.calculateMobTexture(24,20,40,26,textureWidth,textureHeight),
                //left
                        mobMeshBuilder.calculateMobTexture(24,26,40,32,textureWidth,textureHeight),
                //top
                        mobMeshBuilder.calculateMobTexture(48,16,64,24,textureWidth,textureHeight),
                //bottom
                        mobMeshBuilder.calculateMobTexture(48,24,64,32,textureWidth,textureHeight)},


                //right arm
                //back
                {mobMeshBuilder.calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //dark
                //front
                        mobMeshBuilder.calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //light
                //right
                        mobMeshBuilder.calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                        mobMeshBuilder.calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //top
                        mobMeshBuilder.calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                        mobMeshBuilder.calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm

                //left arm
                //back
                {mobMeshBuilder.calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //dark
                //front
                        mobMeshBuilder.calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //light
                //right
                        mobMeshBuilder.calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                        mobMeshBuilder.calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //top
                        mobMeshBuilder.calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                        mobMeshBuilder.calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm


                //right leg
                //back
                {mobMeshBuilder.calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //dark
                //front
                        mobMeshBuilder.calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //light
                //right
                        mobMeshBuilder.calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                        mobMeshBuilder.calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //top
                        mobMeshBuilder.calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                        mobMeshBuilder.calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm

                //left leg
                //back
                {mobMeshBuilder.calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //dark
                //front
                        mobMeshBuilder.calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //light
                //right
                        mobMeshBuilder.calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                        mobMeshBuilder.calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //top
                        mobMeshBuilder.calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                        mobMeshBuilder.calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm
        };


        return mobMeshBuilder.createMobMesh(modelPieceArray,modelTextureArray, "textures/sheep_wool.png");
    }

    /*
    private Mesh[] createShavedMesh(){

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
                {MobMeshBuilder.calculateMobTexture(8,0,16,6,textureWidth,textureHeight),
                //front
                MobMeshBuilder.calculateMobTexture(8,6,16,12,textureWidth,textureHeight),
                //right
                MobMeshBuilder.calculateMobTexture(0,6,8,12,textureWidth,textureHeight),
                //left
                MobMeshBuilder.calculateMobTexture(16,6,24,12,textureWidth,textureHeight),
                //top
                MobMeshBuilder.calculateMobTexture(8,0,16,6,textureWidth,textureHeight),
                //bottom
                MobMeshBuilder.calculateMobTexture(8,0,16,6,textureWidth,textureHeight)},

                //body
                //back
                {MobMeshBuilder.calculateMobTexture(40,20,48,26,textureWidth,textureHeight),
                //front
                MobMeshBuilder.calculateMobTexture(40,26,48,32,textureWidth,textureHeight),
                //right
                MobMeshBuilder.calculateMobTexture(24,20,40,26,textureWidth,textureHeight),
                //left
                MobMeshBuilder.calculateMobTexture(24,26,40,32,textureWidth,textureHeight),
                //top
                MobMeshBuilder.calculateMobTexture(48,16,64,24,textureWidth,textureHeight),
                //bottom
                MobMeshBuilder.calculateMobTexture(48,24,64,32,textureWidth,textureHeight)},


                //right arm
                //back
                {MobMeshBuilder.calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //dark
                //front
                MobMeshBuilder.calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //light
                //right
                MobMeshBuilder.calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                MobMeshBuilder.calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //top
                MobMeshBuilder.calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                MobMeshBuilder.calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm

                //left arm
                //back
                {MobMeshBuilder.calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //dark
                //front
                MobMeshBuilder.calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //light
                //right
                MobMeshBuilder.calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                MobMeshBuilder.calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //top
                MobMeshBuilder.calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                MobMeshBuilder.calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm


                //right leg
                //back
                {MobMeshBuilder.calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //dark
                //front
                MobMeshBuilder.calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //light
                //right
                MobMeshBuilder.calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                MobMeshBuilder.calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //top
                MobMeshBuilder.calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                MobMeshBuilder.calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm

                //left leg
                //back
                {MobMeshBuilder.calculateMobTexture(0,20,4,32,textureWidth,textureHeight), //dark
                //front
                MobMeshBuilder.calculateMobTexture(4,20,8,32,textureWidth,textureHeight), //light
                //right
                MobMeshBuilder.calculateMobTexture(8,20,12,32,textureWidth,textureHeight), //dark
                //left
                MobMeshBuilder.calculateMobTexture(12,20,16,32,textureWidth,textureHeight), //light
                //top
                MobMeshBuilder.calculateMobTexture(4,16,8,20,textureWidth,textureHeight), //shoulder
                //bottom
                MobMeshBuilder.calculateMobTexture(8,16,12,20,textureWidth,textureHeight)}, //palm
        };

        return MobMeshBuilder.createMobMesh(modelPieceArray,modelTextureArray, "textures/sheep_shaved.png");
    }
     */
}
