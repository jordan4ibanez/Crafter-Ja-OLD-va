package game.entity.mob;

import engine.graphics.Mesh;
import engine.time.Delta;
import game.entity.EntityContainer;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;

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

    private final Mesh[] mesh = createMesh();

    private final MobInterface mobInterface = new MobInterface() {
        @Override
        public void onTick(Mob mob, Delta delta) {

            double dtime = delta.getDelta();

            float thisMobTimer = MobObject.getMobTimer(thisMob);
            float thisMobAnimationTimer = MobObject.getMobAnimationTimer(thisMob);
            float thisMobRotation = MobObject.getMobRotation(thisMob);
            byte thisMobHealth = MobObject.getMobHealth(thisMob);

            //pointers
            Vector3d thisMobPos = MobObject.getMobPos(thisMob);
            Vector3d thisMobOldPos = MobObject.getMobOldPos(thisMob);
            Vector3f[] thisMobBodyRotations = MobObject.getMobBodyRotations(thisMob);
            Vector3f thisMobInertia = MobObject.getMobInertia(thisMob);

            thisMobTimer += dtime;

            //debugging for animation timer
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

            float maxWalkSpeed = 2.f;
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

            MobObject.setMobRotation(thisMob, thisMobRotation);


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


        }
    };

    public Chicken(EntityContainer entityContainer, String name, Vector3d pos, Vector3f inertia, float width, float height, int health) {
        super(entityContainer, name, pos, inertia,  width, height, health);
    }

    @Override
    public MobInterface getMobInterface(){
        return mobInterface;
    }

    public Vector3f[] getBodyOffsets() {
        return bodyOffsets;
    }

    public Mesh[] getMesh() {
        return mesh;
    }

    public Vector3f[] getBodyRotations() {
        return bodyRotations;
    }

    //public void registerChickenMob(){
        //registerMob("chicken", "hurt",false, (byte) 7, createMesh(), bodyOffsets, bodyRotations,1f, 0.35f, mobInterface);
    //}


    private Mesh[] createMesh(){

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
                        MobMeshBuilder.calculateMobTexture(7, 3, 11, 9, textureWidth, textureHeight),
                        //front
                        MobMeshBuilder.calculateMobTexture(3, 3, 7, 9, textureWidth, textureHeight),
                        //right
                        MobMeshBuilder.calculateMobTexture(0, 3, 3, 9, textureWidth, textureHeight),
                        //left
                        MobMeshBuilder.calculateMobTexture(11, 3, 14, 9, textureWidth, textureHeight),
                        //top
                        MobMeshBuilder.calculateMobTexture(22, 4, 25, 8, textureWidth, textureHeight),
                        //bottom
                        MobMeshBuilder.calculateMobTexture(25, 4, 28, 8, textureWidth, textureHeight),

                        //beak

                        //back
                        MobMeshBuilder.calculateMobTexture(22, 0, 24, 4, textureWidth, textureHeight),
                        //front
                        MobMeshBuilder.calculateMobTexture(16, 2, 20, 4, textureWidth, textureHeight),
                        //right
                        MobMeshBuilder.calculateMobTexture(14, 2, 16, 4, textureWidth, textureHeight),
                        //left
                        MobMeshBuilder.calculateMobTexture(20, 2, 22, 4, textureWidth, textureHeight),
                        //top
                        MobMeshBuilder.calculateMobTexture(24, 0, 26, 4, textureWidth, textureHeight),
                        //bottom
                        MobMeshBuilder.calculateMobTexture(22, 0, 24, 4, textureWidth, textureHeight),


                        //wattle

                        //back
                        MobMeshBuilder.calculateMobTexture(16, 4, 18, 6, textureWidth, textureHeight),
                        //front
                        MobMeshBuilder.calculateMobTexture(16, 6, 18, 8, textureWidth, textureHeight),
                        //right
                        MobMeshBuilder.calculateMobTexture(18, 4, 19, 6, textureWidth, textureHeight),
                        //left
                        MobMeshBuilder.calculateMobTexture(19, 4, 20, 6, textureWidth, textureHeight),
                        //top
                        MobMeshBuilder.calculateMobTexture(20, 6, 21, 8, textureWidth, textureHeight),
                        //bottom
                        MobMeshBuilder.calculateMobTexture(19, 6, 20, 8, textureWidth, textureHeight),

                },

                //body
                //back
                {MobMeshBuilder.calculateMobTexture(8, 19, 14, 25, textureWidth, textureHeight),
                //front
                MobMeshBuilder.calculateMobTexture(0, 19, 6, 25, textureWidth, textureHeight),
                //right
                MobMeshBuilder.calculateMobTexture(5, 26, 13, 32, textureWidth, textureHeight),
                //left
                MobMeshBuilder.calculateMobTexture(23, 26, 31, 32, textureWidth, textureHeight),
                //top
                MobMeshBuilder.calculateMobTexture(32, 26, 40, 32, textureWidth, textureHeight),
                //bottom
                MobMeshBuilder.calculateMobTexture(14, 26, 22, 32, textureWidth, textureHeight)},


                //right wing
                //back
                {MobMeshBuilder.calculateMobTexture(43, 19, 44, 23, textureWidth, textureHeight), //dark
                //front
                MobMeshBuilder.calculateMobTexture(44, 19, 45, 23, textureWidth, textureHeight), //light
                //right
                MobMeshBuilder.calculateMobTexture(37, 19, 43, 23, textureWidth, textureHeight), //dark
                //left
                MobMeshBuilder.calculateMobTexture(45, 19, 51, 23, textureWidth, textureHeight), //light
                //top
                MobMeshBuilder.calculateMobTexture(41, 15, 47, 16, textureWidth, textureHeight), //shoulder
                //bottom
                MobMeshBuilder.calculateMobTexture(41, 16, 47, 17, textureWidth, textureHeight)}, //palm


                //left wing
                //back
                {MobMeshBuilder.calculateMobTexture(43, 19, 44, 23, textureWidth, textureHeight), //dark
                //front
                MobMeshBuilder.calculateMobTexture(44, 19, 45, 23, textureWidth, textureHeight), //light
                //right
                MobMeshBuilder.calculateMobTexture(37, 19, 43, 23, textureWidth, textureHeight), //dark
                //left
                MobMeshBuilder.calculateMobTexture(45, 19, 51, 23, textureWidth, textureHeight), //light
                //top
                MobMeshBuilder.calculateMobTexture(41, 15, 47, 16, textureWidth, textureHeight), //shoulder
                //bottom
                MobMeshBuilder.calculateMobTexture(41, 16, 47, 17, textureWidth, textureHeight)}, //palm



                //right leg
                //back
                {MobMeshBuilder.calculateMobTexture(35, 3, 38, 8, textureWidth, textureHeight), //dark
                //front
                MobMeshBuilder.calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //light
                //right
                MobMeshBuilder.calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //dark
                //left
                MobMeshBuilder.calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //light
                //top
                MobMeshBuilder.calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //top
                //bottom
                MobMeshBuilder.calculateMobTexture(32, 0, 35, 3, textureWidth, textureHeight)}, //bottom


                //left leg
                //back
                {MobMeshBuilder.calculateMobTexture(35, 3, 38, 8, textureWidth, textureHeight), //dark
                //front
                MobMeshBuilder.calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //light
                //right
                MobMeshBuilder.calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //dark
                //left
                MobMeshBuilder.calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //light
                //top
                MobMeshBuilder.calculateMobTexture(0, 0, 0, 0, textureWidth, textureHeight), //top
                //bottom
                MobMeshBuilder.calculateMobTexture(32, 0, 35, 3, textureWidth, textureHeight)}, //bottom
        };

        return MobMeshBuilder.createMobMesh(modelPieceArray,modelTextureArray, "textures/chicken.png");
    }
}
