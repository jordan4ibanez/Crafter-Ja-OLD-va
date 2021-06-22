package engine.time;

import static engine.time.Time.getDelta;

public class TimeOfDay {
    private static double timeOfDay = 0d;

    private static double timeSpeed = 72d; //following Minetest wiki - 72 times faster (following time_speed in minetest.conf)

    public static void tickUpTimeOfDay(){
        timeOfDay += getDelta() * timeSpeed; //this calculation was ridiculous to get

        if (timeOfDay >= 86_400){ //mt day lasts 20 minutes - 86_400 seconds in a day - synced perfectly with real life speed
            timeOfDay -= 86_400; //time precision lost here
        }
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
