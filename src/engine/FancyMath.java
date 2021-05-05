package engine;

import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.FancyMath.randomForceValue;

public class FancyMath {
    public static int randomDirInt(){
        return  -1 + ((int)(Math.random()*2f) * 2);
    }

    public static float randomDirFloat(){
        return  -1f + ((int)(Math.random()*2f) * 2);
    }

    public static float randomNumber(float x){
        return (float)Math.random() * x;
    }

    public static float randomForceValue(float x){
        return randomNumber(x) * randomDirFloat();
    }

    public static double getDistance(Vector3d pos1, Vector3d pos2){
        return Math.hypot((pos1.x - pos2.x), Math.hypot((pos1.y - pos2.y), (pos1.z - pos2.z)));
    }

    public static double getDistance(double x1, double y1, double z1, double x2, double y2, double z2){
        double x = x1 - x2;
        double y = y1 - y2;
        double z = z1 - z2;
        return Math.hypot(x, Math.hypot(y,z));
    }

}
