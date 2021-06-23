package engine.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import game.chunk.ChunkObject;
import game.crafting.InventoryObject;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.io.IOException;

import static engine.compression.Compression.decompressByteArrayToChunkObject;
import static engine.graphics.Camera.getCameraRotation;
import static game.chat.Chat.addToChatMessageBuffer;
import static game.chunk.Chunk.*;
import static game.crafting.Inventory.getMainInventory;
import static game.item.ItemEntity.*;
import static game.mainMenu.MainMenu.*;
import static game.player.OtherPlayers.updateOtherPlayer;
import static game.player.Player.*;

public class Networking {

    private static int port = 30_150;

    private static final Client client = new Client(50_000,50_000);

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
        //DO PRIMITIVE CLASS FIRST!
        kryo.register(int[].class, 90);
        kryo.register(byte[][].class,91);
        kryo.register(byte[].class,92);
        kryo.register(String.class,93);
        kryo.register(String[].class,94);
        kryo.register(String[][].class,95);
        kryo.register(Vector3d.class,96);
        kryo.register(Vector3f.class,97);
        kryo.register(Vector3i.class,98);
        kryo.register(NetworkHandshake.class,99);
        kryo.register(PlayerPosObject.class,100);
        kryo.register(ChunkRequest.class,101);
        kryo.register(BlockBreakUpdate.class,102);
        kryo.register(BlockPlaceUpdate.class,103);
        kryo.register(ItemSendingObject.class,104);
        kryo.register(ItemPickupNotification.class,105);
        kryo.register(ItemDeletionSender.class,106);
        kryo.register(NetworkMovePositionDemand.class,107);
        kryo.register(NetChunk.class,108);
        kryo.register(HotBarSlotUpdate.class,109);
        kryo.register(NetworkInventory.class,110);
        kryo.register(ThrowItemUpdate.class, 111);
        kryo.register(ChatMessage.class,112);

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
                        sendServerUpdatedInventory();
                        System.out.println("connected to server");
                    } else {
                        client.stop();
                        setServerConnected(false);
                        System.out.println("REJECTED FROM SERVER!");
                        setMenuPage((byte) 7);
                    }
                    //received chunk data
                } else if (object instanceof PlayerPosObject encodedPlayer) {
                    updateOtherPlayer(encodedPlayer);
                } else if (object instanceof  BlockBreakUpdate blockBreakUpdate){
                    digBlock(blockBreakUpdate.pos.x, blockBreakUpdate.pos.y, blockBreakUpdate.pos.z);
                } else if (object instanceof ItemSendingObject itemSendingObject){
                    addItemToQueueToBeUpdated(itemSendingObject);
                } else if (object instanceof ItemPickupNotification itemPickupNotification){
                    addItemToCollectionQueue(itemPickupNotification.name);
                } else if (object instanceof ItemDeletionSender itemDeletionSender){
                    deleteItem(itemDeletionSender.ID);
                } else if (object instanceof BlockPlaceUpdate blockPlaceUpdate){
                    Vector3i c = blockPlaceUpdate.pos;
                    placeBlock(c.x,c.y, c.z, blockPlaceUpdate.ID, blockPlaceUpdate.rot);
                } else if (object instanceof NetworkMovePositionDemand networkMovePositionDemand){
                    setPlayerPos(networkMovePositionDemand.newPos);
                } else if (object instanceof NetChunk netChunk){
                    decodeNetChunk(netChunk);
                } else if (object instanceof  ChatMessage message){
                    addToChatMessageBuffer(message.message);
                }

            }

            @Override
            public void disconnected(Connection connection) {
                //kick player back to the menu
                super.disconnected(connection);
                //killConnection();
                System.out.println("Disconnected from server!");
                client.stop();
                client.close();
                client.removeListener(this);
            }
        });
    }

    private static void decodeNetChunk(NetChunk netChunk){
        //decode compressed network packet
        ChunkObject decomp = null;

        try {
            decomp = decompressByteArrayToChunkObject(netChunk.b);
        } catch (IOException e){
            //System.out.println(e);
            //silent return
            return;
        }

        //silent return
        if (decomp == null){
            return;
        }

        setChunk(decomp);
    }

    public static void sendOutNetworkBlockBreak(int x, int y, int z){
        client.sendTCP(new BlockBreakUpdate( new Vector3i(x,y,z)));
    }

    public static void sendOutNetworkBlockPlace(int x, int y, int z, int ID, byte rotation){
        client.sendTCP(new BlockPlaceUpdate(new Vector3i(x,y,z), ID, rotation));
    }


    //send position data to server
    public static void sendPositionData() {
        PlayerPosObject myPosition = new PlayerPosObject();
        myPosition.pos = getPlayerPos();
        myPosition.name = getPlayerName();
        myPosition.cameraRot = new Vector3f(getCameraRotation());
        client.sendTCP(myPosition);
    }

    public static void sendServerUpdatedInventory(){
        InventoryObject mainInv = getMainInventory();

        assert mainInv != null;
        NetworkInventory inv = new NetworkInventory(mainInv.getSize().x, mainInv.getSize().y);

        for (int x = 0; x < mainInv.getSize().x; x++){
            for (int y = 0; y < mainInv.getSize().y; y++){
                String name = null;
                if (mainInv.get(x,y) != null){
                    name = mainInv.get(x,y).name;
                }
                inv.inventory[x][y] = name;
            }
        }
        //send compacted inventory
        client.sendTCP(inv);
    }

    public static void sendInventorySlot(int slot){
        client.sendTCP(new HotBarSlotUpdate(slot));
    }

    public static void sendOutThrowItemUpdate(){
        client.sendTCP(new ThrowItemUpdate());
    }

    //request chunk data from server
    public static void sendOutChunkRequest(ChunkRequest chunkRequest) {
        client.sendTCP(chunkRequest);
    }

    public static void sendChatMessage(String message){
        //stop spam, kind of
        if (message == null || message.equals("")){
            return;
        }
        client.sendTCP(new ChatMessage(message));
    }

    //allow main loop to send player back to multiplayer page
    public static boolean getIfConnected(){
        return client.isConnected();
    }
}
