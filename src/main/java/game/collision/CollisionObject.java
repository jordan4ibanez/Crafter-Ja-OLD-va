package game.collision;

//static collision object
final public class CollisionObject {
    final private static double[] blockAABB = new double[6];
    final private static double[] entityAABB = new double[6];

    //sets the REUSED memory object which is a simple double array
    //the Y point is the BASE of the object, the height is added to this
    //the X point is the CENTER of the object, with the width adding NEGATIVE and POSITIVE to the object <- might need to be refactored to half
    //the Z point is the CENTER of the object, with the width adding NEGATIVE and POSITIVE to the object <- might need to be refactored to half
    //
    //with this implementation, technically entities can utilize the AABB physics system that is used to
    //collide entities with blocks
    
    /* reuse this for entity solid collision if ever needed
    public static void setAABBBlock(double x, double y, double z, float width, float height){
        blockAABB[0] = x-width; //left
        blockAABB[1] = y; //bottom
        blockAABB[2] = z-width; //back
        blockAABB[3] = x+width; //right
        blockAABB[4] = y+height; //top
        blockAABB[5] = z+width; //front
    }
    */
    
    //autosetter for blockshapes - uses literal position and block shape
    public static void setAABBBlock(float[] blockBox, int x, int y, int z){
        blockAABB[0] = blockBox[0] + x; //left
        blockAABB[1] = blockBox[1] + y; //bottom
        blockAABB[2] = blockBox[2] + z; //back
        blockAABB[3] = blockBox[3] + x; //right
        blockAABB[4] = blockBox[4] + y; //top
        blockAABB[5] = blockBox[5] + z; //front
    }
    
    
    public static void setAABBEntity(double x, double y, double z, float width, float height){
        entityAABB[0] = x-width; //left
        entityAABB[1] = y; //bottom
        entityAABB[2] = z-width; //back
        entityAABB[3] = x+width; //right
        entityAABB[4] = y+height; //top
        entityAABB[5] = z+width; //front
    }
    
    //exclusive AABB for block to entity collision detection
    public static boolean intersectsAABB() {
        return !(entityAABB[0] > blockAABB[3] ||
                   entityAABB[3] < blockAABB[0] ||
                   entityAABB[1] > blockAABB[4] ||
                   entityAABB[4] < blockAABB[1] ||
                   entityAABB[2] > blockAABB[5] ||
                   entityAABB[5] < blockAABB[2]); 
    }
    
    

    //exclusion AABB for single point in space
    public static boolean pointIsWithin(double x, double y, double z){
        return !(blockAABB[0] > x ||
                   blockAABB[3] < x ||
                   blockAABB[1] > y ||
                   blockAABB[4] < y ||
                   blockAABB[2] > z ||
                   blockAABB[5] < z);
    }
}
