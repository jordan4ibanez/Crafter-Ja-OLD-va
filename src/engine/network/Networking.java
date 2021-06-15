package engine.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import engine.disk.ChunkSavingObject;
import game.chunk.ChunkObject;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.io.IOException;

import static engine.graphics.Camera.getCameraRotation;
import static engine.sound.SoundAPI.playSound;
import static game.chunk.Chunk.*;
import static game.crafting.Inventory.addItemToInventory;
import static game.item.ItemEntity.*;
import static game.mainMenu.MainMenu.*;
import static game.player.OtherPlayers.updateOtherPlayer;
import static game.player.Player.*;

public class Networking {

    private static int port = 30_150;

    private static final Client client = new Client(10_000_000,10_000_000);

    public static void setPort(int newPort){
        port = newPort;
    }

    public static int getPort(){
        return port;
    }

    public static void disconnectClient(){
        client.stop();
        client.close();
        System.out.println("disconnected");
    }

    public static boolean getIfMultiplayer(){
        return client.isConnected();
    }


    public static void sendOutHandshake(String host) {

        client.start();

        Kryo kryo = client.getKryo();

        //register classes to be serialized
        kryo.register(NetworkHandshake.class);
        kryo.register(PlayerPosObject.class);
        kryo.register(ChunkRequest.class);
        kryo.register(ChunkObject.class);
        kryo.register(ChunkSavingObject.class);
        kryo.register(int[].class);
        kryo.register(byte[][].class);
        kryo.register(byte[].class);
        kryo.register(Vector3d.class);
        kryo.register(Vector3f.class);
        kryo.register(BreakBlockClassThing.class);
        kryo.register(Vector3i.class);
        kryo.register(BlockBreakingReceiver.class);
        kryo.register(ItemSendingObject.class);
        kryo.register(ItemPickupNotification.class);
        kryo.register(ItemDeletionSender.class);
        kryo.register(BlockPlacingReceiver.class);

        //5000 = 5000ms = 5 seconds
        try {
            client.connect(5000, host, port);
        } catch (IOException e) {
            //e.printStackTrace(); <-spam
            client.stop();
            setServerConnected(false);
            setConnectionFailure();
            return;
        }

        client.sendTCP(new NetworkHandshake(getPlayerName()));

        //client event listener
        client.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                //handshake receival
                if (object instanceof NetworkHandshake encodedName) {
                    if (encodedName.name != null && encodedName.name.equals(getPlayerName())){
                        setServerConnected(true);
                        System.out.println("connected to server");
                    } else {
                        client.stop();
                        setServerConnected(false);
                        System.out.println("REJECTED FROM SERVER!");
                        setMenuPage((byte) 7);
                    }
                    //received chunk data
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

                    //received other player position data
                } else if (object instanceof PlayerPosObject encodedPlayer) {
                    updateOtherPlayer(encodedPlayer);
                } else if (object instanceof  BlockBreakingReceiver blockBreakingReceiver){
                    Vector3i c = blockBreakingReceiver.receivedPos;
                    digBlock(c.x, c.y, c.z);
                } else if (object instanceof ItemSendingObject itemSendingObject){
                    //System.out.println("we have received an item entity");
                    addItemToQueueToBeUpdated(itemSendingObject);
                } else if (object instanceof ItemPickupNotification itemPickupNotification){
                    addItemToCollectionQueue(itemPickupNotification.name);
                } else if (object instanceof ItemDeletionSender itemDeletionSender){
                    deleteItem(itemDeletionSender.ID);
                } else if (object instanceof BlockPlacingReceiver blockPlacingReceiver){
                    Vector3i c = blockPlacingReceiver.receivedPos;
                    placeBlock(c.x,c.y, c.z, blockPlacingReceiver.ID, blockPlacingReceiver.ID);
                }
            }

            @Override
            public void disconnected(Connection connection) {
                //kick player back to the menu
                super.disconnected(connection);
                //killConnection();
                System.out.println("Disconnected from server!");
            }
        });
    }

    public static void sendOutNetworkBlockBreak(int x, int y, int z){
        BreakBlockClassThing thisBlockThing = new BreakBlockClassThing();
        thisBlockThing.breakingPos = new Vector3i(x,y,z);
        client.sendTCP(thisBlockThing);
    }

    public static void sendOutNetworkBlockPlace(int x, int y, int z, int ID, byte rotation){
        client.sendTCP(new BlockPlacingReceiver(new Vector3i(x,y,z), ID, rotation));
    }


    //send position data to server
    public static void sendPositionData() {
        PlayerPosObject myPosition = new PlayerPosObject();
        myPosition.pos = getPlayerPos();
        myPosition.name = getPlayerName();
        myPosition.cameraRot = new Vector3f(getCameraRotation());
        client.sendTCP(myPosition);
    }

    //request chunk data from server
    public static void sendOutChunkRequest(ChunkRequest chunkRequest) {
        client.sendTCP(chunkRequest);
    }

    //allow main loop to send player back to multiplayer page
    public static boolean getIfConnected(){
        return client.isConnected();
    }
}
