package game.chat;

import engine.graphics.Mesh;

import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;

import static engine.gui.TextHandling.createTextWithShadow;
import static engine.time.Time.getDelta;
import static game.player.Player.getPlayerName;

public class Chat {

    private static int ID = 0; //linear ID count

    private static final int chatWordWrapCharCount = 40;

    //private static final HashMap<Integer,String> chatString = new HashMap<>(); not needed for now
    private static final ConcurrentHashMap<Integer, Mesh> chatMesh   = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, Double>  chatTimer   = new ConcurrentHashMap<>();
    private static final ArrayDeque<String> chatBuffer = new ArrayDeque<>();

    private static double oldChatDeletionTimer = 0f;

    private static String currentMessage;
    private static Mesh currentMessageMesh;

    private static final int chatHeightLimit = 9;
    private static final float chatTimerLimit = 5f;

    public static void cleanChatMemory(){
        for (Mesh thisMesh : chatMesh.values()){
            thisMesh.cleanUp(false);
        }

        chatMesh.clear();
        chatTimer.clear();
        ID = 0;
    }

    public static void tickUpChatTimers(){
        double delta = getDelta();
        int index = ID - chatHeightLimit;
        while (index < ID){
            Double thisTimer = chatTimer.get(index);
            if (thisTimer != null) {
                thisTimer += delta;
                if (thisTimer > chatTimerLimit) {
                    if (chatMesh.get(index) != null){
                        chatMesh.get(index).cleanUp(false);
                        chatMesh.remove(index);
                    }
                    chatTimer.remove(index);
                } else {
                    chatTimer.put(index, thisTimer);
                }
            }
            index++;
        }
    }

    public static void deleteOldChatMeshes(){
        oldChatDeletionTimer += getDelta();
        if (oldChatDeletionTimer >= 3f){
            for (int key : chatMesh.keySet()){
                if (ID - key > chatHeightLimit){
                    if (chatMesh.get(key) != null){
                        chatMesh.get(key).cleanUp(false);
                        chatMesh.remove(key);
                        if (chatTimer.get(key) != null){
                            chatTimer.remove(key);
                        }
                    }
                }
            }
        }
    }

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

    public static void addToChatMessageBuffer(String message){
        chatBuffer.add(message);
    }

    public static void popChatMessageBuffer(){
        if (!chatBuffer.isEmpty()){
            String message = chatBuffer.pop();
            addChatMessage(message);
        }
    }

    public static void addChatMessage(String message){

        int buffers = 1;

        //dynamically work through it
        if (message.length() > chatWordWrapCharCount){
            buffers = (int)(Math.ceil((float)message.length() / (float)chatWordWrapCharCount));
        }


        char[][] letterData = new char[buffers][chatWordWrapCharCount];

        int index = 0;
        int count = 0;
        //dynamically address individual bytes
        for (char c : message.toCharArray()){
            letterData[index][count] = c;
            count++;
            if (count >= chatWordWrapCharCount){
                count = 0;
                index++;
            }
        }

        String[] strings = new String[buffers];

        index = 0;
        for (char[] chars : letterData){
            //System.out.println(Arrays.toString(chars));
            //System.out.println(chars.length);

            strings[index] = "";

            for (char c : chars){
                if ((int)c != 0) {
                    strings[index] += c;
                }
            }

            //System.out.println(strings[index]);

            index++;
        }

        //add the processed text data in
        for (int i = 0; i < buffers; i++ ) {
            //add message text
            //chatString.put(ID, strings[i]);

            //generate message data
            chatMesh.put(ID, createTextWithShadow(strings[i], 1, 1, 1));

            //set timer
            chatTimer.put(ID, 0d);

            //tick up ID
            ID++;
        }
    }

    public static Mesh[] getViewableChatMessages(){
        Mesh[] returningMeshes = new Mesh[chatHeightLimit];
        int index = 0;
        for (int i = ID - 1; i > ID - chatHeightLimit - 1; i--){
            if (i >= 0) {
                returningMeshes[index] = chatMesh.get(i);
                index++;
            }
        }
        return returningMeshes;
    }

    private static int getCurrentChatID(){
        return ID;
    }

    public static void cleanChatQueueMemory(){
        //clean chat message messages
        for (Mesh thisMesh : chatMesh.values()){
            thisMesh.cleanUp(false);
        }

        //clear data
        chatMesh.clear();
        //chatString.clear();

        //reset ID
        ID = 0;
    }

}
