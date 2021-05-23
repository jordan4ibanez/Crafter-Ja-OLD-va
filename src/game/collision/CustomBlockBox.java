package game.collision;

public class CustomBlockBox {

    private static final double[] aabb = new double[6];

    public static void setBlockBox(int x, int y, int z, double[] blockBox){
        aabb[0] = (double)x + blockBox[0];
        aabb[1] = (double)y + blockBox[1];
        aabb[2] = (double)z + blockBox[2];
        aabb[3] = (double)x + blockBox[3];
        aabb[4] = (double)y + blockBox[4];
        aabb[5] = (double)z + blockBox[5];
    }

    //getters

    public static double BlockBoxGetLeft(){
        return aabb[0];
    }

    public static double BlockBoxGetBottom(){
        return aabb[1];
    }

    public static double BlockBoxGetFront(){
        return aabb[2];
    }

    public static double BlockBoxGetRight(){
        return aabb[3];
    }

    public static double BlockBoxGetTop(){
        return aabb[4];
    }

    public static double BlockBoxGetBack(){
        return aabb[5];
    }
}
