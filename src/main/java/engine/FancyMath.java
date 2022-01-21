package engine;

import java.util.Random;

public class FancyMath {
    private final Random random = new Random();
    private final int[] dirArray = new int[]{-1,1};



    public byte randomByte(byte value){
        return (byte)random.nextInt(value);
    }

    //this variable does an int from min to max, but, it can also give you -max to -min
    //example: 5 to 10 can return -10 through -5
    //this is specifically designed for the mob spawning algorithm
    //this also has a horrible name
    public int randomIntFromMinToMaxNegativePositive(int min, int max){
        int x = min + random.nextInt(max - min + 1);
        return x * dirArray[random.nextInt(2)];
    }

    public float randomDirFloat(){
        return dirArray[random.nextInt(2)];
    }

    public float randomNumber(float x){
        return (float)Math.random() * x;
    }

    public float randomForceValue(float x){
        return randomNumber(x) * randomDirFloat();
    }

    public double getDistance(double x1, double y1, double z1, double x2, double y2, double z2){
        return Math.hypot((x1 - x2), Math.hypot((y1 - y2),(z1 - z2)));
    }

    public float convertLight(byte lightValue){
        return (float) Math.pow(1.25, lightValue)/28.42171f;
    }
}
