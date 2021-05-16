package game.mainMenu;

import static engine.MouseInput.*;

public class MainMenu {

    public static void initMainMenu(){
        //in intellij, search for 1 and you'll be able to read it
        byte[][] titleBlocks = new byte[][]{
                {0,1,0,0,1,1,0,0,0,1,0,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,0},
                {1,0,1,0,1,0,1,0,1,0,1,0,1,0,0,0,0,1,0,0,1,0,0,0,1,0,1},
                {1,0,0,0,1,1,0,0,1,1,1,0,1,1,1,0,0,1,0,0,1,1,0,0,1,1,0},
                {1,0,1,0,1,0,1,0,1,0,1,0,1,0,0,0,0,1,0,0,1,0,0,0,1,0,1},
                {0,1,0,0,1,0,1,0,1,0,1,0,1,0,0,0,0,1,0,0,1,1,1,0,1,0,1},
        };


    }

    public static void doMainMenuLogic(){
        if (isMouseLocked()){
            System.out.println("unlocking");
            toggleMouseLock();
        }
    }
}
