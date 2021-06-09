package engine.network;

import com.esotericsoftware.kryonet.Client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import static engine.network.NetworkThread.getGamePort;
import static game.player.Player.getName;

public class NetworkOutput {

    private static String hostLock;

    public static void setHostLock(String newAddress){
        hostLock = newAddress;
    }

    public static void sendOutHandshake(String host) {
        Client client = new Client();
        client.start();
        //5000 = 5000ms = 5 seconds
        try {
            client.connect(5000, "192.0.0.1", getGamePort());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String test = "succ";
        client.sendTCP(test);

        /*
        int tries = 4;
        while (tries < 5) {
            tries++;
            Socket socket;
            {
                try {
                    System.out.println("trying to connect to: " + host);
                    socket = new Socket(host, getGamePort());
                    socket.setSoTimeout(3);
                } catch (IOException e) {
                    //e.printStackTrace();
                    System.out.println("CANNOT CONNECT TO SERVER!");
                    continue;
                }
            }


            OutputStream outputStream = null;

            {
                try {
                    outputStream = socket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            try {
                dataOutputStream.writeByte(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                dataOutputStream.writeUTF(getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                dataOutputStream.flush(); // Send off the data
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                dataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            System.out.println("connected!");

            tries = 10;
        }
    }

    public static void sendPositionData() {
        Socket socket = null;
        {
            try {
                socket = new Socket("localhost", getGamePort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        OutputStream outputStream;

        {
            try {
                assert socket != null;
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Send first message
        /*
        outputStream.writeByte(1);
        dataOutPut.writeUTF("This is the first type of message.");
        dataOutPut.flush(); // Send off the data

        // Send the second message
        dataOutPut.writeByte(2);
        dataOutPut.writeUTF("This is the second type of message.");
        dataOutPut.flush(); // Send off the data

        // Send the third message
        dataOutPut.writeByte(3);
        dataOutPut.writeUTF("This is the third type of message (Part 1).");
        dataOutPut.writeUTF("This is the third type of message (Part 2).");
        dataOutPut.flush(); // Send off the data


        // Send the exit message
        dataOutPut.writeByte(-1);
        dataOutPut.flush();


        dataOutPut.close();

         */
    }

    public static void sendOutChunkRequest(String chunkID) {
        Socket socket;
        {
            try {
                socket = new Socket(hostLock, getGamePort());
                socket.setSoTimeout(2);
            } catch (IOException e) {
                //e.printStackTrace();
                return;
            }
        }

        OutputStream outputStream = null;

        {
            try {
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        try {
            dataOutputStream.writeByte(3);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.writeUTF(chunkID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.flush(); // Send off the data
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
