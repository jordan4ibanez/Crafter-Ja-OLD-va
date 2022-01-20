package game.player;

import engine.graphics.Mesh;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static engine.time.Time.getDelta;
import static game.entity.mob.MobMeshBuilder.calculateMobTexture;
import static game.entity.mob.MobMeshBuilder.createMobMesh;
import static game.player.Player.getPlayerInertiaX;
import static game.player.Player.getPlayerInertiaZ;

public class PlayerMesh {

    //this is auto constructed
    private final static Mesh[] bodyMeshes = createMesh();

    private static float animationTimer = 0f;

    final private static float maxWalkSpeed = 2.f;

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
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
    };

    //body animation scope
    private static final Vector2f inertiaWorker = new Vector2f();
    public static void applyPlayerBodyAnimation(){

        double delta = getDelta();

        inertiaWorker.set(getPlayerInertiaX(), getPlayerInertiaZ());

        animationTimer += delta * (inertiaWorker.length() / maxWalkSpeed);

        if (animationTimer >= 1f) {
            animationTimer -= 1f;
        }

        bodyRotations[2].set((float) java.lang.Math.toDegrees(java.lang.Math.sin(animationTimer * java.lang.Math.PI * 2f)), 0, 0);
        bodyRotations[3].set((float) java.lang.Math.toDegrees(java.lang.Math.sin(animationTimer * java.lang.Math.PI * -2f)), 0, 0);
        bodyRotations[4].set((float) java.lang.Math.toDegrees(java.lang.Math.sin(animationTimer * java.lang.Math.PI * -2f)), 0, 0);
        bodyRotations[5].set((float) java.lang.Math.toDegrees(java.lang.Math.sin(animationTimer * Math.PI * 2f)), 0, 0);
    };

    public static Vector3f[] getPlayerBodyRotations(){
        return bodyRotations;
    }

    public static Mesh[] getPlayerMeshes(){
        return bodyMeshes;
    }

    public static Vector3f[]getPlayerBodyOffsets(){
        return bodyOffsets;
    }

    private static Mesh[] createMesh(){
        final float modelScale = 0.25f; //lazy way to fix

        final float[][][] modelPieceArray = new float[][][]{
                //head
                {{-0.75f * modelScale, 0.0f * modelScale, -0.75f * modelScale, 0.75f * modelScale, 1.5f * modelScale, 0.75f * modelScale}},
                //body
                {{-0.75f * modelScale, -2.5f * modelScale, -0.45f * modelScale, 0.75f * modelScale, 0.0f * modelScale, 0.45f * modelScale}},
                //right arm
                {{-0.375f * modelScale, -2.2f * modelScale, -0.375f * modelScale, 0.375f * modelScale, 0.3f * modelScale, 0.375f * modelScale}},
                //left arm
                {{-0.375f * modelScale, -2.2f * modelScale, -0.375f * modelScale, 0.375f * modelScale, 0.3f * modelScale, 0.375f * modelScale}},
                //right leg
                {{-0.375f * modelScale, -2.5f * modelScale, -0.375f * modelScale, 0.375f * modelScale, 0.0f * modelScale, 0.375f * modelScale}},
                //left leg
                {{-0.375f * modelScale,-2.5f * modelScale,-0.375f * modelScale,  0.375f * modelScale,0.0f * modelScale,0.375f * modelScale}},
        };


        float textureWidth = 64f;
        final float textureHeight = 32f;

        float[][][] modelTextureArray = new float[][][]{
                //head
                //back
                {calculateMobTexture(24, 8, 32, 16, textureWidth, textureHeight),
                        //front
                        calculateMobTexture(8, 8, 16, 16, textureWidth, textureHeight),
                        //right
                        calculateMobTexture(0, 8, 8, 16, textureWidth, textureHeight),
                        //left
                        calculateMobTexture(16, 8, 24, 16, textureWidth, textureHeight),
                        //top
                        calculateMobTexture(8, 0, 16, 8, textureWidth, textureHeight),
                        //bottom
                        calculateMobTexture(16, 0, 24, 8, textureWidth, textureHeight)},

                //body
                //back
                {calculateMobTexture(32, 20, 40, 30, textureWidth, textureHeight),
                        //front
                        calculateMobTexture(20, 20, 28, 30, textureWidth, textureHeight),
                        //right
                        calculateMobTexture(28, 20, 32, 30, textureWidth, textureHeight),
                        //left
                        calculateMobTexture(16, 20, 20, 30, textureWidth, textureHeight),
                        //top
                        calculateMobTexture(20, 16, 28, 20, textureWidth, textureHeight),
                        //bottom
                        calculateMobTexture(28, 16, 36, 20, textureWidth, textureHeight)},


                //right arm
                //back
                {calculateMobTexture(48, 20, 52, 32, textureWidth, textureHeight), //dark
                        //front
                        calculateMobTexture(44, 20, 48, 32, textureWidth, textureHeight), //light
                        //right
                        calculateMobTexture(48, 20, 52, 32, textureWidth, textureHeight), //dark
                        //left
                        calculateMobTexture(44, 20, 48, 32, textureWidth, textureHeight), //light
                        //top
                        calculateMobTexture(44, 16, 48, 20, textureWidth, textureHeight), //shoulder
                        //bottom
                        calculateMobTexture(48, 16, 52, 20, textureWidth, textureHeight)}, //palm

                //left arm
                //back
                {calculateMobTexture(48, 20, 52, 32, textureWidth, textureHeight), //dark
                        //front
                        calculateMobTexture(44, 20, 48, 32, textureWidth, textureHeight), //light
                        //right
                        calculateMobTexture(44, 20, 48, 32, textureWidth, textureHeight), //light
                        //left
                        calculateMobTexture(48, 20, 52, 32, textureWidth, textureHeight), //dark
                        //top
                        calculateMobTexture(44, 16, 48, 20, textureWidth, textureHeight), //shoulder
                        //bottom
                        calculateMobTexture(48, 16, 52, 20, textureWidth, textureHeight)}, //palm


                //right leg
                //back
                {calculateMobTexture(0, 20, 4, 32, textureWidth, textureHeight), //dark
                        //front
                        calculateMobTexture(4, 20, 8, 32, textureWidth, textureHeight), //light
                        //right
                        calculateMobTexture(8, 20, 12, 32, textureWidth, textureHeight), //dark
                        //left
                        calculateMobTexture(12, 20, 16, 32, textureWidth, textureHeight), //light
                        //top
                        calculateMobTexture(4, 16, 8, 20, textureWidth, textureHeight), //top
                        //bottom
                        calculateMobTexture(8, 16, 12, 20, textureWidth, textureHeight)}, //bottom

                //left leg
                //back
                {calculateMobTexture(0, 20, 4, 32, textureWidth, textureHeight), //dark
                        //front
                        calculateMobTexture(4, 20, 8, 32, textureWidth, textureHeight), //light
                        //right
                        calculateMobTexture(12, 20, 16, 32, textureWidth, textureHeight), //light
                        //left
                        calculateMobTexture(8, 20, 12, 32, textureWidth, textureHeight), //dark
                        //top
                        calculateMobTexture(4, 16, 8, 20, textureWidth, textureHeight), //top
                        //bottom
                        calculateMobTexture(8, 16, 12, 20, textureWidth, textureHeight)}, //bottom
        };

        return createMobMesh(modelPieceArray,modelTextureArray, "textures/player.png");
    }
}
