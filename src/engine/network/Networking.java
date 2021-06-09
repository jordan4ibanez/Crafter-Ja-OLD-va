package engine.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import engine.disk.ChunkSavingObject;
import game.chunk.ChunkObject;

import java.io.IOException;

import static game.chunk.Chunk.setChunk;
import static game.mainMenu.MainMenu.quickToggleServerConnectedBoolean;
import static game.mainMenu.MainMenu.setMenuPage;
import static game.player.OtherPlayers.updateOtherPlayer;
import static game.player.Player.*;

public class Networking {

    private static final int port = 30_150;

    private static final Client client = new Client();

    public static void sendOutHandshake(String host) {

        client.start();

        //5000 = 5000ms = 5 seconds
        try {
            client.connect(5000, host, port);
        } catch (IOException e) {
            e.printStackTrace();
            client.stop();
            return;
        }

        client.sendTCP(getPlayerName());

        //client event listener
        client.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof NetworkHandshake encodedName) {
                    if (encodedName.name != null && encodedName.name.equals(getPlayerName())){
                        quickToggleServerConnectedBoolean();
                        System.out.println("connected to server");
                    } else {
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
                    updateOtherPlayer(encodedPlayer);
                }
            }
        });
    }

    public static void sendPositionData() {
        client.sendTCP(getPlayerPos());
    }

    public static void sendOutChunkRequest(ChunkRequest chunkRequest) {
        client.sendTCP(chunkRequest);
    }
}
