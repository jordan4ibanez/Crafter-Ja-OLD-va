package engine.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import engine.Vector3dn;
import game.player.Player;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketOption;

import static engine.Window.windowShouldClose;
import static game.mainMenu.MainMenu.setMenuPage;
import static game.player.Player.getPlayerName;
import static java.net.SocketOptions.SO_TIMEOUT;

public class NetworkThread {

    private static final int port = 30_151; //minetest, why not

    public static int getGamePort(){
        return port;
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
                serverSocket = new ServerSocket(getGamePort());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Socket socket;


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

                System.out.println("5e");


                boolean readingData = true;
                while (readingData) {
                    byte messageType = 0;
                    try {
                        assert dataInputStream != null;
                        messageType = dataInputStream.readByte();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    switch (messageType) {

                        //confirm server handshake
                        case 1 -> {
                            try {
                                String confirmation =  dataInputStream.readUTF(); //name or kill

                                if (confirmation.equals(getPlayerName())){
                                    System.out.println("SWITCH TO MULTIPLAYER LOOP AND LOCK!");
                                } else if (confirmation.equals("KILL")){
                                    System.out.println("REJECTED FROM SERVER!");
                                    setMenuPage((byte)5);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        //get other player's position
                        case 2 ->
                                {
                                    try {
                                        String position =  dataInputStream.readUTF(); //vector 3dn

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                        //receive chunk objects
                        case 3 ->
                                {
                                    try {
                                        String position =  dataInputStream.readUTF(); //chunk object
                                        Vector3dn newPosition = objectMapper.readValue(position, Vector3dn.class);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                        default -> readingData = false;
                    }
                }

                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
