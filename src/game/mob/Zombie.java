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

public class Zombie {

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
            //these are the arms
            //thisMob.bodyRotations[2].x = (float) Math.toDegrees(Math.sin(thisMob.animationTimer * Math.PI * 2f));
            //thisMob.bodyRotations[3].x = (float) Math.toDegrees(Math.sin(thisMob.animationTimer * Math.PI * -2f));
            //these are the legs
            thisMob.bodyRotations[4].x = (float) Math.toDegrees(Math.sin(thisMob.animationTimer * Math.PI * -2f));
            thisMob.bodyRotations[5].x = (float) Math.toDegrees(Math.sin(thisMob.animationTimer * Math.PI * 2f));


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

            thisMob.animationTimer += thisMob.pos.distance(thisMob.oldPos) / 2f;

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
        registerMob(new MobDefinition("zombie", "hurt", (byte) 7, createMesh(), bodyOffsets, bodyRotations,1.9f, 0.25f, mobInterface));
    }


    private static Mesh[] createMesh(){
        final float modelScale = 0.25f; //lazy way to fix

        final float[][] modelPieceArray = new float[][]{
//                head
                {-0.75f * modelScale,0.0f * modelScale,-0.75f * modelScale,0.75f * modelScale,1.5f * modelScale,0.75f * modelScale},
//                body
                {-0.75f * modelScale,-2.5f * modelScale,-0.45f * modelScale,0.75f * modelScale,0.0f * modelScale,0.45f * modelScale},
//                //right arm
                {-0.375f * modelScale,-2.2f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.3f * modelScale,0.375f * modelScale},
//                //left arm
                {-0.375f * modelScale,-2.2f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.3f * modelScale,0.375f * modelScale},
                //right leg
                {-0.375f * modelScale,-2.5f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.0f * modelScale,0.375f * modelScale},
                //left leg
                {-0.375f * modelScale,-2.5f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.0f * modelScale,0.375f * modelScale},
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
                calculateMobTexture(32,20,40,30, textureWidth, textureHeight),
                //back
                calculateMobTexture(20,20,28,30, textureWidth, textureHeight),
                //right
                calculateMobTexture(28,20,32,30, textureWidth, textureHeight),
                //left
                calculateMobTexture(16,20,20,30, textureWidth, textureHeight),
                //top
                calculateMobTexture(20,16,28,20, textureWidth, textureHeight),
                //bottom
                calculateMobTexture(28,16,36,20, textureWidth, textureHeight),


                //right arm
                //front
                calculateMobTexture(48,20,52,32, textureWidth, textureHeight), //dark
                //back
                calculateMobTexture(44,20,48,32, textureWidth, textureHeight), //light
                //right
                calculateMobTexture(48,20,52,32, textureWidth, textureHeight), //dark
                //left
                calculateMobTexture(44,20,48,32, textureWidth, textureHeight), //light
                //top
                calculateMobTexture(44,16,48,20, textureWidth, textureHeight), //shoulder
                //bottom
                calculateMobTexture(48,16,52,20, textureWidth, textureHeight), //palm

                //left arm
                //front
                calculateMobTexture(48,20,52,32, textureWidth, textureHeight), //dark
                //back
                calculateMobTexture(44,20,48,32, textureWidth, textureHeight), //light
                //right
                calculateMobTexture(44,20,48,32, textureWidth, textureHeight), //light
                //left
                calculateMobTexture(48,20,52,32, textureWidth, textureHeight), //dark
                //top
                calculateMobTexture(44,16,48,20, textureWidth, textureHeight), //shoulder
                //bottom
                calculateMobTexture(48,16,52,20, textureWidth, textureHeight), //palm


                //right leg
                //front
                calculateMobTexture(0,20,4,32, textureWidth, textureHeight), //dark
                //back
                calculateMobTexture(4,20,8,32, textureWidth, textureHeight), //light
                //right
                calculateMobTexture(8,20,12,32, textureWidth, textureHeight), //dark
                //left
                calculateMobTexture(12,20,16,32, textureWidth, textureHeight), //light
                //top
                calculateMobTexture(4,16,8,20, textureWidth, textureHeight), //top
                //bottom
                calculateMobTexture(8,16,12,20, textureWidth, textureHeight), //bottom

                //left leg
                //front
                calculateMobTexture(0,20,4,32, textureWidth, textureHeight), //dark
                //back
                calculateMobTexture(4,20,8,32, textureWidth, textureHeight), //light
                //right
                calculateMobTexture(12,20,16,32, textureWidth, textureHeight), //light
                //left
                calculateMobTexture(8,20,12,32, textureWidth, textureHeight), //dark
                //top
                calculateMobTexture(4,16,8,20, textureWidth, textureHeight), //top
                //bottom
                calculateMobTexture(8,16,12,20, textureWidth, textureHeight), //bottom
        };

        return createMobMesh(modelPieceArray,modelTextureArray, "textures/zombie.png");
    }
}
