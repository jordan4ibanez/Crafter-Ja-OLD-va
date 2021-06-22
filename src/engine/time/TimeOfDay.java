package engine.time;

import static engine.time.Time.getDelta;

public class TimeOfDay {

    private static final double dayCompletion = 86_400d;

    private static double timeOfDay = 0d; //6AM 0600 Hours //21_600d

    private static double timeSpeed = 10000d; //following Minetest wiki - 72 times faster (following time_speed in minetest.conf)

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

    public static int getTimeOfDay24H(){
        //add in a 00:00 thing eventually
        return (int) ((timeOfDay/dayCompletion) * 2400);
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

    /*
    you can find these values in 24 hour time by multiplying them by 2400
    or you can chuck them into getTimeOfDay24H()

    the sun/moon time scale is a bit messed up

    sky colors need to be interpolated soon - vector 3f? vector 3d?

    moon comes up at 0.7  or 7:20pm
    sun goes down at 0.3 or 7:20am

    sun comes up at 0.3 or 7:20am
    sun goes down at 0.7 or 7:20pm

    custom light levels for every stage - 21 stages in total - loops

    stage 0 : light level 5  - 0.00 to 0.30 - night ends
    stage 1 : light level 6  - 0.30 to 0.31 - morning begins
    stage 2 : light level 7  - 0.31 to 0.32
    stage 3 : light level 8  - 0.32 to 0.33
    stage 4 : light level 9  - 0.33 to 0.34
    stage 5 : light level 10 - 0.34 to 0.35
    stage 6 : light level 11 - 0.35 to 0.36
    stage 7 : light level 12 - 0.36 to 0.37
    stage 8 : light level 13 - 0.37 to 0.38
    stage 9 : light level 14 - 0.38 to 0.39
    stage 10: light level 15 - 0.39 to 0.60 - mid day - sun is at highest
    stage 11: light level 14 - 0.60 to 0.61 - evening begins
    stage 12: light level 13 - 0.61 to 0.62
    stage 13: light level 12 - 0.62 to 0.63
    stage 14: light level 11 - 0.63 to 0.64
    stage 15: light level 10 - 0.64 to 0.65
    stage 16: light level 9  - 0.65 to 0.66
    stage 17: light level 8  - 0.66 to 0.67
    stage 18: light level 7  - 0.67 to 0.68
    stage 19: light level 6  - 0.68 to 0.69
    stage 20: light level 5  - 0.69 to 0.70 - night begins
    loops to stage 0
     */

    //private static void
}
