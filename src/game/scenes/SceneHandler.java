package game.scenes;

public class SceneHandler {
    private static boolean inMainMenu = true;
    private static boolean inGame = false;

    public static void setIfInMainMenu(boolean truth){
        inMainMenu = truth;
    }

    public static boolean getIfInMainMenu(){
        return inMainMenu;
    }

    public static void setIfInGame(boolean truth){
        inGame = truth;
    }

    public static boolean getIfInGame(){
        return inGame;
    }
}
