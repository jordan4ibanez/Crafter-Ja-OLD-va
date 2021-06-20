package game.chat;

import engine.graphics.Mesh;

import java.util.HashMap;

import static engine.gui.TextHandling.createTextWithShadow;
import static game.player.Player.getPlayerName;

public class Chat {

    private static int ID = 0; //linear ID count

    private static final HashMap<Integer,String> chatString = new HashMap<>();
    private static final HashMap<Integer, Mesh>  chatMesh   = new HashMap<>();

    private static String currentMessage;
    private static Mesh currentMessageMesh;

    public static void setCurrentChatMessage(String message){
        currentMessage = null;
        currentMessage = message;

        //rebuild current chat message
        if (currentMessageMesh != null) {
            currentMessageMesh.cleanUp(false);
        }

        String playerName = getPlayerName();
        if (playerName.equals("")){
            playerName = "SinglePlayer";
        }
        currentMessageMesh = createTextWithShadow("<" + playerName + ">:" + message, 1,1,1);
    }

    public static String getCurrentChatMessage(){
        return currentMessage;
    }

    public static Mesh getCurrentMessageMesh(){
        return currentMessageMesh;
    }

    public static void clearCurrentChatMessage(){
        currentMessage = "";
        if (currentMessageMesh != null) {
            currentMessageMesh.cleanUp(false);
        }
    }

    public static void addChatMessage(String message){
        //add message text
        chatString.put(ID, message);

        //generate message data
        chatMesh.put(ID, createTextWithShadow(message,1,1,1));

        //tick up ID
        ID++;
    }

    public static Mesh[] getRecentMessages(){
        Mesh[] arrayOfMeshes = new Mesh[5];
        int index = 0;
        for (int i = ID - 1; i > 0 && i > ID - 1 - 5; i--){
            arrayOfMeshes[index] = chatMesh.get(i);
            i++;
        }

        return arrayOfMeshes;
    }

    public static int getCurrentChatID(){
        return ID;
    }

    public static void cleanChatQueueMemory(){
        //clean chat message messages
        for (Mesh thisMesh : chatMesh.values()){
            thisMesh.cleanUp(false);
        }

        //clear data
        chatMesh.clear();
        chatString.clear();

        //reset ID
        ID = 0;
    }

}
