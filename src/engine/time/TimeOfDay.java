package engine.time;

import static engine.time.Time.getDelta;

public class TimeOfDay {
    private static double timeOfDay = 0d;

    private static double timeSpeed = 72d;

    public static void tickUpTimeOfDay(){
        timeOfDay += getDelta() * timeSpeed;

        if (timeOfDay >= 24000){
            timeOfDay -= 24000;
        }

        System.out.println(timeOfDay);
    }

    public static double getTimeOfDay(){
        return timeOfDay;
    }

    public static double getTimeSpeed(){
        return timeSpeed;
    }

    public static void setTimeOfDay(double newTime){
        timeOfDay = newTime;
    }

    public static void setTimeSpeed(double newSpeed){
        timeSpeed = newSpeed;
    }
}
