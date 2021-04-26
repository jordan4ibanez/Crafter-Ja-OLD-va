package game.collision;

public class CustomBlockBox {

    private static float[] aabb = new float[6];

    public static void setBlockBox(int x, int y, int z, float[] blockBox){
        aabb[0] = x + blockBox[0];
        aabb[1] = y + blockBox[1];
        aabb[2] = z + blockBox[2];
        aabb[3] = x + blockBox[3];
        aabb[4] = y + blockBox[4];
        aabb[5] = z + blockBox[5];

    }

    //getters

    public static float BlockBoxGetLeft(){
        return aabb[0];
    }

    public static float BlockBoxGetBottom(){
        return aabb[1];
    }

    public static float BlockBoxGetFront(){
        return aabb[2];
    }

    public static float BlockBoxGetRight(){
        return aabb[3];
    }

    public static float BlockBoxGetTop(){
        return aabb[4];
    }

    public static float BlockBoxGetBack(){
        return aabb[5];
    }
}
