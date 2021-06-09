package engine.network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static engine.network.NetworkThread.getGameOutputPort;
import static game.player.Player.getName;

public class NetworkOutput {

    private static String hostLock;

    public static void setHostLock(String newAddress){
        hostLock = newAddress;
    }

    public static void sendOutHandshake(String host) {
        Socket socket = null;
        {
            try {

                socket = new Socket(host, getGameOutputPort());
                socket.setSoTimeout(2);
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("YOU HAVE PUT IN A NULL ADDRESS! >:(");
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
    }

    public static void sendPositionData() {
        Socket socket = null;
        {
            try {
                socket = new Socket("localhost", getGameOutputPort());
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
                socket = new Socket(hostLock, getGameOutputPort());
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
