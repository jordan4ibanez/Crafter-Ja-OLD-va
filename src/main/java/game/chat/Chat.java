package game.chat;

import engine.graphics.Mesh;
import engine.time.Delta;
import game.player.Player;

import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;

final public class Chat {

    private final Player player;
    private final Delta delta;

    public Chat(Player player, Delta delta){
        this.player = player;
        this.delta  = delta;
    }

    private int ID = 0; //linear ID count

    //private final HashMap<Integer,String> chatString = new HashMap<>(); not needed for now
    private final ConcurrentHashMap<Integer, Mesh> chatMesh   = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Double>  chatTimer   = new ConcurrentHashMap<>();
    private final ArrayDeque<String> chatBuffer = new ArrayDeque<>();

    private double oldChatDeletionTimer = 0f;

    private String currentMessage;
    private Mesh currentMessageMesh;

    private final int chatHeightLimit = 9;

    public void cleanChatMemory(){
        for (Mesh thisMesh : chatMesh.values()){
            if (thisMesh != null) {
                thisMesh.cleanUp(false);
            }
        }

        chatMesh.clear();
        chatTimer.clear();
        ID = 0;
    }

    public void tickUpChatTimers(){
        double delta = this.delta.getDelta();
        int index = ID - chatHeightLimit;
        while (index < ID){
            Double thisTimer = chatTimer.get(index);
            if (thisTimer != null) {
                thisTimer += delta;
                float chatTimerLimit = 5f;
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

    public void deleteOldChatMeshes(){
        oldChatDeletionTimer += delta.getDelta();
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

    public void setCurrentChatMessage(String message){
        currentMessage = null;
        currentMessage = message;

        //rebuild current chat message
        currentMessageMesh.cleanUp(false);

        String playerName = player.getName();
        if (playerName.equals("")){
            playerName = "SinglePlayer";
        }
        //currentMessageMesh = createTextWithShadow("<" + playerName + ">:" + message, 1,1,1);
    }

    public String getCurrentChatMessage(){
        return currentMessage;
    }

    public Mesh getCurrentMessageMesh(){
        return currentMessageMesh;
    }

    public void clearCurrentChatMessage(){
        currentMessage = "";
        currentMessageMesh.cleanUp(false);
        currentMessageMesh = null;
    }

    public void addToChatMessageBuffer(String message){
        chatBuffer.add(message);
    }

    public void popChatMessageBuffer(){
        if (!chatBuffer.isEmpty()){
            String message = chatBuffer.pop();
            addChatMessage(message);
        }
    }

    public void addChatMessage(String message){

        int buffers = 1;

        //dynamically work through it
        int chatWordWrapCharCount = 40;
        if (message.length() > chatWordWrapCharCount){
            buffers = (int)(Math.ceil((float)message.length() / (float) chatWordWrapCharCount));
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
            //chatMesh.put(ID, createTextWithShadow(strings[i], 1, 1, 1));

            //set timer
            chatTimer.put(ID, 0d);

            //tick up ID
            ID++;
        }
    }

    //FIXME - this is programmed horribly
    public Mesh[] getViewableChatMessages(){
        /*
        int[] returningMeshes = new int[chatHeightLimit];
        int index = 0;
        for (int i = ID - 1; i > ID - chatHeightLimit - 1; i--){
            if (i >= 0) {
                returningMeshes[index] = chatMesh.get(i);
                index++;
            }
        }
        return returningMeshes;
         */
        return new Mesh[0];
    }

    private int getCurrentChatID(){
        return ID;
    }

    public void cleanChatQueueMemory(){
        //clean chat message messages
        for (Mesh thisMesh : chatMesh.values()){
            if (thisMesh != null) {
                thisMesh.cleanUp(false);
            }
        }

        //clear data
        chatMesh.clear();
        //chatString.clear();

        //reset ID
        ID = 0;
    }

}
