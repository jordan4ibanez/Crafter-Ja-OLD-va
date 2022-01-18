package game.collision;

//static collision object
final public class CollisionObject {
    final private static double[] aabb = new double[6];

    //sets the REUSED memory object which is a simple double array
    public static void setAABB(double x, double y, double z, float width, float height){
        aabb[0] = x-width; //left
        aabb[1] = y; //bottom
        aabb[2] = z-width; //back
        aabb[3] = x+width; //right
        aabb[4] = y+height; //top
        aabb[5] = z+width; //front
    }

    //exclusion AABB for single point in space
    public static boolean pointIsWithin(double x, double y, double z){
        return !(aabb[0] > x ||
                aabb[3] < x ||
                aabb[1] > y ||
                aabb[4] < y ||
                aabb[2] > z ||
                aabb[5] < z);
    }
}
