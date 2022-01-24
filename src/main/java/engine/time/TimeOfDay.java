package engine.time;

import engine.Window;
import game.light.Light;

public class TimeOfDay {

    private Window window;
    private Light light;

    private final double dayCompletion = 86_400d;
    //3600 per hour, this is messed up maybe?
    private double timeOfDay = 21_600; //6am 21_600

    private double timeSpeed = 72d; //following Minetest wiki - 72 times faster (following time_speed in minetest.conf)

    public void setWindow(Window window){
        if (this.window == null){
            this.window = window;
        }
    }

    public void setLight(Light light){
        if (this.light == null){
            this.light = light;
        }
    }

    public TimeOfDay(){
    }

    public void tickUpTimeOfDay(Delta delta){
        timeOfDay += delta.getDelta() * timeSpeed; //this calculation was ridiculous to get

        triggerNextStage();

        if (timeOfDay >= dayCompletion){ //mt day lasts 20 minutes - 86_400 seconds in a day - synced perfectly with real life speed
            timeOfDay -= dayCompletion; //time precision lost here
        }
    }

    public double getTimeOfDay(){
        return timeOfDay;
    }

    public double getTimeOfDayLinear(){
        return (timeOfDay/dayCompletion);
    }

    public int getTimeOfDay24H(){
        //add in a 00:00 thing eventually
        return (int) ((timeOfDay/dayCompletion) * 2400);
    }

    public double getTimeSpeed(){
        return timeSpeed;
    }

    public void setTimeOfDay(double newTime){
        timeOfDay = newTime;
    }

    public void pollTimeOfDay(){
        triggerNextStage();
    }

    public void setTimeSpeed(double newSpeed){
        timeSpeed = newSpeed;
    }

    /*
    you can find these values in 24 hour time by multiplying them by 2400
    or you can chuck them into getTimeOfDay24H()

    moon comes up at 0.7  or 7:20pm
    sun goes down at 0.3 or 7:20am

    sun comes up at 0.3 or 7:20am
    sun goes down at 0.7 or 7:20pm

    custom light levels for every stage - 21 stages in total - loops

    stage 0 : light level 5  - night ends - morning begins
    stage 1 : light level 6
    stage 2 : light level 7
    stage 3 : light level 8
    stage 4 : light level 9
    stage 5 : light level 10
    stage 6 : light level 11
    stage 7 : light level 12
    stage 8 : light level 13
    stage 9 : light level 14 - mid day - sun is at highest
    stage 10: light level 15
    stage 11: light level 14 - evening begins
    stage 12: light level 13
    stage 13: light level 12
    stage 14: light level 11
    stage 15: light level 10
    stage 16: light level 9
    stage 17: light level 8
    stage 18: light level 7
    stage 19: light level 6
    stage 20: light level 5 - night begins
    loops to stage 0
     */


    private final byte[] dayStageLight = new byte[]{
            5,  // stage 0
            6,  // stage 1
            7,  // stage 2
            8,  // stage 3
            9,  // stage 4
            10, // stage 5
            11, // stage 6
            12, // stage 7
            13, // stage 8
            14, // stage 9
            15, // stage 10
            14, // stage 11
            13, // stage 12
            12, // stage 13
            11, // stage 14
            10, // stage 15
            9,  // stage 16
            8,  // stage 17
            7,  // stage 18
            6,  // stage 19
            5,  // stage 20
    };




    //can afford float imprecision
    //get locked until the limit - used as a key - currentLinearTime >= dayStageGoal[x] then flips to next
    private final float[] dayStageGoal = new float[]{
            0.25f, // stage 0
            0.26f, // stage 1
            0.27f, // stage 2
            0.28f, // stage 3
            0.29f, // stage 4
            0.30f, // stage 5
            0.31f, // stage 6
            0.32f, // stage 7
            0.33f, // stage 8
            0.34f, // stage 9
            0.66f, // stage 10
            0.67f, // stage 11
            0.68f, // stage 12
            0.69f, // stage 13
            0.70f, // stage 14
            0.71f, // stage 15
            0.72f, // stage 16
            0.73f, // stage 17
            0.74f, // stage 18
            0.75f, // stage 19
            1.00f, // stage 20
    };

    //I completely winged these colors :T
    private final float[][] skyColors = new float[][]{
            {0,   0,    0   },
            { 20, 20,   20  }, // night ends - morning begins
            { 45, 55,   50  },
            { 70, 90,   95  },
            { 90, 130,  155 },
            { 100, 155, 175 },
            { 123, 185, 205 },
            { 132, 195, 220 },
            { 132, 200, 225 },
            { 135, 206, 235 }, // mid day - sun is at highest
            { 132, 200, 225 },// evening begins
            { 132, 190, 210 },
            { 130, 175, 195 },
            { 125, 160, 170 },
            { 115, 150, 150 },
            { 105, 135, 145 },
            { 95,  115, 125 },
            { 70,  90,  95  },
            { 55,  70,  75  },
            { 54,  55,  45  },
            { 0,   0,   0   } // night begins
    };

    private byte currentDayStage = calculateCurrentDayStage();
    private float currentDayTimeGoal = dayStageGoal[currentDayStage];
    private final byte maxStage = 20;
    private byte oldStageLight = 0;

    private byte calculateCurrentDayStage(){
        double linearTime = getTimeOfDayLinear();
        for (byte i = 0; i < dayStageGoal.length; i++){
            if (linearTime >= dayStageGoal[i]){
                currentDayStage = i;
                currentDayTimeGoal = dayStageGoal[currentDayStage];

                if (dayStageLight[currentDayStage] != oldStageLight){
                    light.setCurrentLightLevel(dayStageLight[currentDayStage]);
                    window.setWindowClearColorGoal(skyColors[currentDayStage][0]/255f,skyColors[currentDayStage][1]/255f, skyColors[currentDayStage][2]/255f, 1f);
                }
                oldStageLight = dayStageLight[currentDayStage];

                return (i);
            }
        }

        return (0);
    }

    private void triggerNextStage(){
        double linearTime = getTimeOfDayLinear();

        if (currentDayStage != maxStage && linearTime >= currentDayTimeGoal){
            currentDayStage++;
            currentDayTimeGoal = dayStageGoal[currentDayStage];

            if (dayStageLight[currentDayStage] != oldStageLight){
                light.setCurrentLightLevel(dayStageLight[currentDayStage]);
                window.setWindowClearColorGoal(skyColors[currentDayStage][0]/255f,skyColors[currentDayStage][1]/255f, skyColors[currentDayStage][2]/255f, 1f);
            }
            oldStageLight = dayStageLight[currentDayStage];

            System.out.println("current stage: " + currentDayStage);
        //loop around to next day
        } else if (currentDayStage == maxStage && linearTime >= currentDayTimeGoal){
            currentDayStage = 0;
            currentDayTimeGoal = dayStageGoal[currentDayStage];

            System.out.println("new day, stage: 0");

            if (dayStageLight[currentDayStage] != oldStageLight){
                light.setCurrentLightLevel(dayStageLight[currentDayStage]);
            }
            window.setWindowClearColorGoal(skyColors[currentDayStage][0]/255f,skyColors[currentDayStage][1]/255f, skyColors[currentDayStage][2]/255f, 1f);

            oldStageLight = dayStageLight[currentDayStage];
        }
    }
}
