package game.collision;

import org.joml.Vector3f;

public class CustomAABB {

    final private static double[] aabb = new double[6];
    final private static float width = 0f;
    final private static float height = 0f;

    public static void setAABB(double x, double y, double z, float width, float height){
        aabb[0] = x-width;
        aabb[1] = y;
        aabb[2] = z-width;
        aabb[3] = x+width;
        aabb[4] = y+height;
        aabb[5] = z+width;
    }

    public static void updateAABBPos(Vector3f pos){
        aabb[0] = pos.x-width;
        aabb[1] = pos.y;
        aabb[2] = pos.z-width;
        aabb[3] = pos.x+width;
        aabb[4] = pos.y+height;
        aabb[5] = pos.z+width;
    }

    /*
    public static void updateWidth(float newWidth){
        width = newWidth;
    }

     */

    public static double AABBGetLeft(){
        return aabb[0];
    }

    public static double AABBGetBottom(){
        return aabb[1];
    }

    public static double AABBGetFront(){
        return aabb[2];
    }

    public static double AABBGetRight(){
        return aabb[3];
    }

    public static double AABBGetTop(){
        return aabb[4];
    }

    public static double AABBGetBack(){
        return aabb[5];
    }
}
