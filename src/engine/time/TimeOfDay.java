package engine.time;

import static engine.time.Time.getDelta;

public class TimeOfDay {

    private static final double dayCompletion = 86_400d;

    private static double timeOfDay = 0d; //6AM 0600 Hours //21_600d

    private static double timeSpeed = 20000d; //following Minetest wiki - 72 times faster (following time_speed in minetest.conf)

    public static void tickUpTimeOfDay(){
        timeOfDay += getDelta() * timeSpeed; //this calculation was ridiculous to get

        if (timeOfDay >= dayCompletion){ //mt day lasts 20 minutes - 86_400 seconds in a day - synced perfectly with real life speed
            timeOfDay -= dayCompletion; //time precision lost here
        }
    }

    public static double getTimeOfDay(){
        return timeOfDay;
    }

    public static double getTimeOfDayLinear(){
        return (timeOfDay/dayCompletion);
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
