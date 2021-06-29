package game.mob;

import engine.graphics.Mesh;
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

public class Exploder {

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
            //these are the legs
            thisMob.bodyRotations[2].x = (float) Math.toDegrees(Math.sin(thisMob.animationTimer * Math.PI * -2f) / 1.6f);
            thisMob.bodyRotations[3].x = (float) Math.toDegrees(Math.sin(thisMob.animationTimer * Math.PI * 2f) / 1.6f);
            //the legs are diagonally synced, so the multiplier is flipped
            thisMob.bodyRotations[4].x = (float) Math.toDegrees(Math.sin(thisMob.animationTimer * Math.PI * 2f) / 1.6f);
            thisMob.bodyRotations[5].x = (float) Math.toDegrees(Math.sin(thisMob.animationTimer * Math.PI * -2f) / 1.6f);


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

            thisMob.animationTimer += thisMob.pos.distance(thisMob.oldPos) / 1.5f;

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

    private static final float yOffsetCorrection = 0.1525f;

    private static final Vector3f[] bodyOffsets = new Vector3f[]{
            new Vector3f(0,0.985f + yOffsetCorrection,0),
            new Vector3f(0,0.985f + yOffsetCorrection,0),

            //rear legs
            new Vector3f(-0.121f,0.17f + yOffsetCorrection,0.27f),
            new Vector3f(0.121f,0.17f + yOffsetCorrection,0.27f),

            //front legs
            new Vector3f(-0.121f,0.17f + yOffsetCorrection,-0.27f),
            new Vector3f(0.121f,0.17f + yOffsetCorrection,-0.27f),

    };

    private static final Vector3f[] bodyRotations = new Vector3f[]{
            new Vector3f(0,0,0), //head
            new Vector3f(0,0,0), //body

            new Vector3f(0,0,0), //rear right leg
            new Vector3f(0,0,0), //rear left leg

            new Vector3f(0,0,0), //front right leg
            new Vector3f(0,0,0), //front left leg
    };

    public static void registerExploderMob(){
        registerMob(new MobDefinition("exploder", "hurt", (byte) 7, createMesh(), bodyOffsets, bodyRotations,1.9f, 0.25f, mobInterface));
    }


    private static Mesh[] createMesh(){
        final float modelScale = 0.325f; //lazy way to fix

        final float[][] modelPieceArray = new float[][]{
//                head
                {-0.75f * modelScale,0.0f * modelScale,-0.75f * modelScale,0.75f * modelScale,1.5f * modelScale,0.75f * modelScale},
//                body
                {-0.75f * modelScale,-2.5f * modelScale,-0.45f * modelScale,0.75f * modelScale,0.0f * modelScale,0.45f * modelScale},
                //rear right leg
                {-0.375f * modelScale,-1.f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.0f * modelScale,0.375f * modelScale},
                //rear left leg
                {-0.375f * modelScale,-1f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.0f * modelScale,0.375f * modelScale},
                //front right leg
                {-0.375f * modelScale,-1.f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.0f * modelScale,0.375f * modelScale},
                //front left leg
                {-0.375f * modelScale,-1f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.0f * modelScale,0.375f * modelScale},
        };


        float textureWidth = 64f;
        final float textureHeight = 32f;

        float[][] modelTextureArray = new float[][]{
                //head
                //front
                calculateMobTexture(24,8,32,16, textureWidth, textureHeight),
                //back
                calculateMobTexture(8,8,16,16, textureWidth, textureHeight),
                //right
                calculateMobTexture(0,8,8,16, textureWidth, textureHeight),
                //left
                calculateMobTexture(16,8,24,16, textureWidth, textureHeight),
                //top
                calculateMobTexture(8,0,16,8, textureWidth, textureHeight),
                //bottom
                calculateMobTexture(16,0,24,8, textureWidth, textureHeight),

                //body
                //front
                calculateMobTexture(20,20,28,32, textureWidth, textureHeight),
                //back
                calculateMobTexture(28,20,36,32, textureWidth, textureHeight),
                //right
                calculateMobTexture(16,20,20,32, textureWidth, textureHeight),
                //left
                calculateMobTexture(36,20,40,32, textureWidth, textureHeight),
                //top
                calculateMobTexture(20,16,28,20, textureWidth, textureHeight),
                //bottom
                calculateMobTexture(28,16,36,20, textureWidth, textureHeight),


                //rear right leg
                //front
                calculateMobTexture(4,20,8,26, textureWidth, textureHeight),
                //back
                calculateMobTexture(8,20,12,26, textureWidth, textureHeight),
                //right
                calculateMobTexture(0,20,4,26, textureWidth, textureHeight),
                //left
                calculateMobTexture(12,20,16,26, textureWidth, textureHeight),
                //top
                calculateMobTexture(4,16,8,20, textureWidth, textureHeight),
                //bottom
                calculateMobTexture(8,16,12,20, textureWidth, textureHeight),

                //rear left leg
                //front
                calculateMobTexture(4,20,8,26, textureWidth, textureHeight),
                //back
                calculateMobTexture(8,20,12,26, textureWidth, textureHeight),
                //right
                calculateMobTexture(0,20,4,26, textureWidth, textureHeight),
                //left
                calculateMobTexture(12,20,16,26, textureWidth, textureHeight),
                //top
                calculateMobTexture(4,16,8,20, textureWidth, textureHeight),
                //bottom
                calculateMobTexture(8,16,12,20, textureWidth, textureHeight),


                //front right leg
                //front
                calculateMobTexture(4,20,8,26, textureWidth, textureHeight),
                //back
                calculateMobTexture(8,20,12,26, textureWidth, textureHeight),
                //right
                calculateMobTexture(0,20,4,26, textureWidth, textureHeight),
                //left
                calculateMobTexture(12,20,16,26, textureWidth, textureHeight),
                //top
                calculateMobTexture(4,16,8,20, textureWidth, textureHeight),
                //bottom
                calculateMobTexture(8,16,12,20, textureWidth, textureHeight),

                //front left leg
                //front
                calculateMobTexture(4,20,8,26, textureWidth, textureHeight),
                //back
                calculateMobTexture(8,20,12,26, textureWidth, textureHeight),
                //right
                calculateMobTexture(0,20,4,26, textureWidth, textureHeight),
                //left
                calculateMobTexture(12,20,16,26, textureWidth, textureHeight),
                //top
                calculateMobTexture(4,16,8,20, textureWidth, textureHeight),
                //bottom
                calculateMobTexture(8,16,12,20, textureWidth, textureHeight),
        };

        return createMobMesh(modelPieceArray,modelTextureArray, "textures/exploder.png");
    }
}