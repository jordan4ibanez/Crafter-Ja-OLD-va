package engine.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import engine.Vector3dn;
import game.player.Player;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static engine.Window.windowShouldClose;

public class NetworkThread {

    private static final int port = 30_150; //minetest, why not

    public static int getGamePort(){
        return port;
    }

    //if players send garbage data, break connection, destroy player object

    /*
    data chart: (base 1 like LUA - 0 reserved for null data)
    1 - get other player's data
    2 - receive chunk data (JACKSON CONVERSION)
    3 -
     */

    public static void startNetworkThread() {
        new Thread(() -> {

            //used for raw data conversion
            final ObjectMapper objectMapper = new ObjectMapper();

            while (!windowShouldClose()) {

                ServerSocket server = null;
                Socket socket = null;

                try {
                    server = new ServerSocket(port);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    assert server != null;
                    socket = server.accept();
                } catch (IOException e) {
                    e.printStackTrace();
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
                    byte messageType = 0;
                    try {
                        assert dataInputStream != null;
                        messageType = dataInputStream.readByte();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    switch (messageType) {

                        //get other player's position
                        case 1 ->
                                {
                                    try {
                                        String position =  dataInputStream.readUTF(); //vector 3dn

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                        //receive chunk objects
                        case 2 ->
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
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
