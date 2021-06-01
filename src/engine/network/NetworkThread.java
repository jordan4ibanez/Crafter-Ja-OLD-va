package engine.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import engine.Vector3dn;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static engine.Window.windowShouldClose;
import static game.mainMenu.MainMenu.quickToggleServerConnectedBoolean;
import static game.mainMenu.MainMenu.setMenuPage;
import static game.player.Player.getPlayerName;

public class NetworkThread {

    private static final int inputPort = 30_151; //minetest, why not
    private static final int outputPort = 30_150; //minetest, why not

    public static int getGameInputPort(){
        return inputPort;
    }

    public static int getGameOutputPort(){
        return outputPort;
    }

    //if players send garbage data, break connection, destroy player object

    /*
    data chart: (base 1 like LUA - 0 reserved for null data)
    1 - confirm handshake
    2 - get other player's data
    3 - receive chunk data (JACKSON CONVERSION)
    4 -
     */
    private static Thread networkingThread;

    public static void startNetworkThread() {
        networkingThread = new Thread(() -> {

            //used for raw data conversion
            final ObjectMapper objectMapper = new ObjectMapper();

            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(inputPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Socket socket = null;

            try {
                while (!windowShouldClose()) {
                    try {
                        assert serverSocket != null;
                        serverSocket.setSoTimeout(10);
                        socket = serverSocket.accept();
                    } catch (IOException e) {
                        //e.printStackTrace(); <-THIS WILL SPAM YOUR TERMINAL LIKE CRAZY
                        //System.out.println("SKIPPIN");
                        continue;
                    }

                    DataInputStream dataInputStream = null;

                    try {
                        assert socket != null;
                        dataInputStream = new DataInputStream(socket.getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    boolean readingData = true;
                    while (readingData) {
                        byte messageType;
                        try {
                            assert dataInputStream != null;
                            messageType = dataInputStream.readByte();
                        } catch (IOException e) {
                            break;
                        }

                        switch (messageType) {

                            //confirm server handshake
                            case 1 -> {
                                try {
                                    String confirmation = dataInputStream.readUTF(); //name or kill

                                    if (confirmation.equals(getPlayerName())) {
                                        quickToggleServerConnectedBoolean();
                                    } else if (confirmation.equals("KILL")) {
                                        System.out.println("REJECTED FROM SERVER!");
                                        setMenuPage((byte) 5);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            //get other player's position
                            case 2 -> {
                                try {
                                    String position = dataInputStream.readUTF(); //vector 3dn

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            //receive chunk objects
                            case 3 -> {
                                try {
                                    String position = dataInputStream.readUTF(); //chunk object
                                    Vector3dn newPosition = objectMapper.readValue(position, Vector3dn.class);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            default -> readingData = false;
                        }
                    }

                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                try {
                    if (socket != null){
                        socket.close();
                    }
                } catch (IOException e) {
                    //e.printStackTrace();
                }

                try {
                    if (serverSocket != null) {
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        });
        networkingThread.start();
    }

    public static void killNetworkingThread(){
        System.out.println("KILLING NETWORK THREAD");
        networkingThread.interrupt();
        networkingThread = null;
    }
}
