package game.entity.mob;

import engine.graphics.Mesh;
import engine.time.Delta;
import game.chunk.Chunk;
import game.entity.EntityContainer;
import game.entity.collision.Collision;
import org.joml.*;
import org.joml.Math;

import static org.joml.Math.floor;


public class Exploder extends Mob{

    private final Vector2f workerVector2f = new Vector2f();

    private final Vector3f[] bodyOffsets = new Vector3f[]{
            new Vector3f(0,0.985f + 0.1525f,0),
            new Vector3f(0,0.985f + 0.1525f,0),

            //rear legs
            new Vector3f(-0.121f,0.17f + 0.1525f,0.27f),
            new Vector3f(0.121f,0.17f + 0.1525f,0.27f),

            //front legs
            new Vector3f(-0.121f,0.17f + 0.1525f,-0.27f),
            new Vector3f(0.121f,0.17f + 0.1525f,-0.27f),

    };

    private final Vector3f[] bodyRotations = new Vector3f[]{
            new Vector3f(0,0,0), //head
            new Vector3f(0,0,0), //body

            new Vector3f(0,0,0), //rear right leg
            new Vector3f(0,0,0), //rear left leg

            new Vector3f(0,0,0), //front right leg
            new Vector3f(0,0,0), //front left leg
    };

    private final Mesh[] mesh;

    private final MobInterface mobInterface = new MobInterface() {
        @Override
        public void onDeath(Mob thisMob, Delta delta) {
            System.out.println("boom");
            //Vector3d thisPos = getMobPos(thisMob);
            //boom(thisPos, 4);
        }

        @Override
        public void onTick(Chunk chunk, Collision collision, Mob thisMob, Delta delta) {

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
            //these are the legs
            float animation = (float) Math.toDegrees(Math.sin(thisMobAnimationTimer * Math.PI * 2f) / 1.6f);
            thisMobBodyRotations[2].x = -animation;
            thisMobBodyRotations[3].x = animation;
            //the legs are diagonally synced, so the multiplier is flipped
            thisMobBodyRotations[4].x = animation;
            thisMobBodyRotations[5].x = -animation;


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

            thisMobAnimationTimer += thisMobPos.distance(thisMobOldPos) / 1.5f;

            if (thisMobAnimationTimer >= 1f) {
                thisMobAnimationTimer -= 1f;
            }

            setOnGround(onGround);

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

    public Exploder(MobMeshBuilder mobMeshBuilder, EntityContainer entityContainer, Vector3d pos, Vector3f inertia, float width, float height, int health) {
        super(entityContainer, "exploder", pos, inertia, width, height, health);

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

    private Mesh[] createMesh(MobMeshBuilder mobMeshBuilder){
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
                {mobMeshBuilder.calculateMobTexture(24,8,32,16, textureWidth, textureHeight),
                //front
                        mobMeshBuilder.calculateMobTexture(8,8,16,16, textureWidth, textureHeight),
                //right
                        mobMeshBuilder.calculateMobTexture(0,8,8,16, textureWidth, textureHeight),
                //left
                        mobMeshBuilder.calculateMobTexture(16,8,24,16, textureWidth, textureHeight),
                //top
                        mobMeshBuilder.calculateMobTexture(8,0,16,8, textureWidth, textureHeight),
                //bottom
                        mobMeshBuilder.calculateMobTexture(16,0,24,8, textureWidth, textureHeight)},

                //body
                //back
                {mobMeshBuilder.calculateMobTexture(20,20,28,32, textureWidth, textureHeight),
                //front
                        mobMeshBuilder.calculateMobTexture(28,20,36,32, textureWidth, textureHeight),
                //right
                        mobMeshBuilder.calculateMobTexture(16,20,20,32, textureWidth, textureHeight),
                //left
                        mobMeshBuilder.calculateMobTexture(36,20,40,32, textureWidth, textureHeight),
                //top
                        mobMeshBuilder.calculateMobTexture(20,16,28,20, textureWidth, textureHeight),
                //bottom
                        mobMeshBuilder.calculateMobTexture(28,16,36,20, textureWidth, textureHeight)},


                //rear right leg
                //back
                {mobMeshBuilder.calculateMobTexture(4,20,8,26, textureWidth, textureHeight),
                //front
                        mobMeshBuilder.calculateMobTexture(8,20,12,26, textureWidth, textureHeight),
                //right
                        mobMeshBuilder.calculateMobTexture(0,20,4,26, textureWidth, textureHeight),
                //left
                        mobMeshBuilder.calculateMobTexture(12,20,16,26, textureWidth, textureHeight),
                //top
                        mobMeshBuilder.calculateMobTexture(4,16,8,20, textureWidth, textureHeight),
                //bottom
                        mobMeshBuilder.calculateMobTexture(8,16,12,20, textureWidth, textureHeight)},

                //rear left leg
                //back
                {mobMeshBuilder.calculateMobTexture(4,20,8,26, textureWidth, textureHeight),
                //front
                        mobMeshBuilder.calculateMobTexture(8,20,12,26, textureWidth, textureHeight),
                //right
                        mobMeshBuilder.calculateMobTexture(0,20,4,26, textureWidth, textureHeight),
                //left
                        mobMeshBuilder.calculateMobTexture(12,20,16,26, textureWidth, textureHeight),
                //top
                        mobMeshBuilder.calculateMobTexture(4,16,8,20, textureWidth, textureHeight),
                //bottom
                        mobMeshBuilder.calculateMobTexture(8,16,12,20, textureWidth, textureHeight)},


                //front right leg
                //back
                {mobMeshBuilder.calculateMobTexture(4,20,8,26, textureWidth, textureHeight),
                //front
                        mobMeshBuilder.calculateMobTexture(8,20,12,26, textureWidth, textureHeight),
                //right
                        mobMeshBuilder.calculateMobTexture(0,20,4,26, textureWidth, textureHeight),
                //left
                        mobMeshBuilder.calculateMobTexture(12,20,16,26, textureWidth, textureHeight),
                //top
                        mobMeshBuilder.calculateMobTexture(4,16,8,20, textureWidth, textureHeight),
                //bottom
                        mobMeshBuilder.calculateMobTexture(8,16,12,20, textureWidth, textureHeight)},

                //front left leg
                //back
                {mobMeshBuilder.calculateMobTexture(4,20,8,26, textureWidth, textureHeight),
                //front
                        mobMeshBuilder.calculateMobTexture(8,20,12,26, textureWidth, textureHeight),
                //right
                        mobMeshBuilder.calculateMobTexture(0,20,4,26, textureWidth, textureHeight),
                //left
                        mobMeshBuilder.calculateMobTexture(12,20,16,26, textureWidth, textureHeight),
                //top
                        mobMeshBuilder.calculateMobTexture(4,16,8,20, textureWidth, textureHeight),
                //bottom
                        mobMeshBuilder.calculateMobTexture(8,16,12,20, textureWidth, textureHeight)},
        };

        return mobMeshBuilder.createMobMesh(modelPieceArray,modelTextureArray, "textures/exploder.png");
    }
}
