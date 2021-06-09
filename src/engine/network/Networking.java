package engine.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import engine.disk.ChunkSavingObject;
import game.chunk.ChunkObject;
import org.joml.Vector3d;

import java.io.IOException;

import static engine.graphics.Camera.getCameraRotation;
import static game.chunk.Chunk.setChunk;
import static game.mainMenu.MainMenu.*;
import static game.player.OtherPlayers.updateOtherPlayer;
import static game.player.Player.*;

public class Networking {

    private static final int port = 30_150;

    private static final Client client = new Client(10_000_000,10_000_000);



    public static void disconnectClient(){
        client.stop();
        client.close();
        System.out.println("disconnected");
    }


    public static void sendOutHandshake(String host) {

        client.start();



        Kryo kryo = client.getKryo();

        kryo.register(NetworkHandshake.class);
        kryo.register(PlayerPosObject.class);
        kryo.register(ChunkRequest.class);
        kryo.register(ChunkObject.class);
        kryo.register(ChunkSavingObject.class);
        kryo.register(int[].class);
        kryo.register(byte[][].class);
        kryo.register(byte[].class);
        kryo.register(Vector3d.class);

        //5000 = 5000ms = 5 seconds
        try {
            client.connect(5000, host, port);
        } catch (IOException e) {
            e.printStackTrace();
            client.stop();
            setServerConnected(false);
            return;
        }

        client.sendTCP(new NetworkHandshake(getPlayerName()));

        //client event listener
        client.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof NetworkHandshake encodedName) {
                    if (encodedName.name != null && encodedName.name.equals(getPlayerName())){
                        setServerConnected(true);
                        System.out.println("connected to server");
                    } else {
                        client.stop();
                        setServerConnected(false);
                        System.out.println("REJECTED FROM SERVER!");
                        setMenuPage((byte) 5);
                    }
                } else if (object instanceof ChunkSavingObject encodedChunk){
                    ChunkObject abstractedChunk = new ChunkObject();
                    abstractedChunk.ID = encodedChunk.I;
                    abstractedChunk.x = encodedChunk.x;
                    abstractedChunk.z = encodedChunk.z;
                    abstractedChunk.block = encodedChunk.b;
                    abstractedChunk.rotation = encodedChunk.r;
                    abstractedChunk.light = encodedChunk.l;
                    abstractedChunk.heightMap = encodedChunk.h;
                    abstractedChunk.lightLevel = encodedChunk.e;

                    setChunk(encodedChunk.x, encodedChunk.z, abstractedChunk);

                } else if (object instanceof PlayerPosObject encodedPlayer){
                    //System.out.println("recieved the thing from the server");
                    updateOtherPlayer(encodedPlayer);
                }
            }
        });
    }

    public static void sendPositionData() {
        PlayerPosObject myPosition = new PlayerPosObject();
        myPosition.pos = getPlayerPos();
        myPosition.name = getPlayerName();
        myPosition.rotation = getCameraRotation().y;
        client.sendTCP(myPosition);
    }

    public static void sendOutChunkRequest(ChunkRequest chunkRequest) {
        client.sendTCP(chunkRequest);
    }
}
