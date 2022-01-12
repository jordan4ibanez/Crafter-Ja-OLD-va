package game.mob;

import org.joml.Vector3f;



final public class MobDefinition {
    private static final int numberOfMobDefinitions = 9;
    private static int count = 0;

    private static final int[][] bodyMeshes = new int[numberOfMobDefinitions][0];
    private static final MobInterface[] mobInterface = new MobInterface[numberOfMobDefinitions];
    private static final String[] mobName = new String[numberOfMobDefinitions];
    private static final Vector3f[][] bodyOffsets = new Vector3f[numberOfMobDefinitions][0];
    private static final Vector3f[][] bodyRotations = new Vector3f[numberOfMobDefinitions][0];
    private static final float[] height = new float[numberOfMobDefinitions];
    private static final float[] width = new float[numberOfMobDefinitions];
    private static final String[] hurtSound = new String[numberOfMobDefinitions];
    private static final byte[] baseHealth = new byte[numberOfMobDefinitions];
    private static final boolean[] backFaceCulling = new boolean[numberOfMobDefinitions];


    public static void registerMob(String name, String newHurtSound, boolean newBackFaceCulling, byte newBaseHealth, int[] newBodyMeshes,Vector3f[] newBodyOffsets,Vector3f[] newBodyRotations, float newHeight, float newWidth, MobInterface newMobInterface){
        bodyMeshes[count] = newBodyMeshes;
        mobInterface[count] = newMobInterface;
        mobName[count] = name;
        bodyOffsets[count] = newBodyOffsets;
        bodyRotations[count] = newBodyRotations;
        height[count] = newHeight;
        width[count] = newWidth;
        hurtSound[count] = newHurtSound;
        baseHealth[count] = newBaseHealth;
        backFaceCulling[count] = newBackFaceCulling;

        count++;
    }

    public static int[] getMobBodyMeshes(int ID){
        return bodyMeshes[ID];
    }
    public static MobInterface getMobInterface(int ID){
        return mobInterface[ID];
    }
    public static String getMobName(int ID){
        return mobName[ID];
    }
    public static Vector3f[] getMobBodyOffsets(int ID){
        return bodyOffsets[ID];
    }
    public static Vector3f[] getMobBodyRotations(int ID){
        return bodyRotations[ID];
    }
    public static float getMobHeight(int ID){
        return height[ID];
    }
    public static float getMobWidth(int ID){
        return width[ID];
    }
    public static String getMobHurtSound(int ID){
        return hurtSound[ID];
    }
    public static byte getMobBaseHealth(int ID){
        return baseHealth[ID];
    }
    public static boolean getMobBackFaceCulling(int ID){
        return backFaceCulling[ID];
    }

}
