package game.player;

import org.joml.Vector3f;

import static game.chunk.Chunk.*;
import static game.blocks.BlockDefinition.*;
import static game.collision.Collision.wouldCollidePlacing;
import static game.collision.CustomAABB.setAABB;
import static game.collision.CustomBlockBox.setBlockBox;
import static game.item.ItemDefinition.getItemModifier;
import static game.particle.Particle.createParticle;
import static game.player.Inventory.getItemInInventorySlot;
import static game.player.Inventory.removeItemFromInventory;
import static game.player.Player.*;

public class Ray {
    public static void rayCast(Vector3f pos, Vector3f dir, float length, boolean mining, boolean placing) {
        Vector3f finalPos = new Vector3f();

        Vector3f newPos   = new Vector3f();

        Vector3f lastPos  = new Vector3f();

        Vector3f cachePos = new Vector3f();

        int foundBlock = -1;

        for(float step = 0; step <= length ; step += 0.001f) {

            cachePos.x = dir.x * step;
            cachePos.y = dir.y * step;
            cachePos.z = dir.z * step;

            newPos.x = (float)Math.floor(pos.x + cachePos.x);
            newPos.y = (float)Math.floor(pos.y + cachePos.y);
            newPos.z = (float)Math.floor(pos.z + cachePos.z);

            //stop wasting cpu resources
            if (lastPos != null) {
                if (newPos.x != lastPos.x || newPos.y != lastPos.y || newPos.z != lastPos.z) {
                    foundBlock = getBlock((int) newPos.x, (int) newPos.y, (int) newPos.z);

                    if (foundBlock > 0 && isBlockPointable(foundBlock)) {
                        finalPos = newPos;
                        break;
                    }
                }
            }

            lastPos = new Vector3f(newPos);
        }

        if(finalPos != null && foundBlock > 0) {
            if(mining) {
                destroyBlock(finalPos);
            } else if (placing && lastPos != null){

                //todo: make this call on punched
                if (!isPlayerSneaking() && blockHasOnRightClickCall(foundBlock)) {

                    getBlockDefinition(foundBlock).blockModifier.onRightClick(finalPos);

                } else {
                    setAABB(getPlayerPos().x, getPlayerPos().y, getPlayerPos().z, getPlayerWidth(), getPlayerHeight());
                    setBlockBox((int) lastPos.x, (int) lastPos.y, (int) lastPos.z, getBlockShape(1, (byte) 0)[0]); //TODO: make this check the actual block shapes

                    if (!wouldCollidePlacing() && getItemInInventorySlot(getPlayerInventorySelection(), 0) != null && !getItemInInventorySlot(getPlayerInventorySelection(), 0).definition.isTool) {
                        rayPlaceBlock(lastPos, getItemInInventorySlot(getPlayerInventorySelection(), 0).definition.blockID);
                    } else if (getItemInInventorySlot(getPlayerInventorySelection(), 0) != null && getItemInInventorySlot(getPlayerInventorySelection(), 0).definition.isTool) {
                        if (getItemModifier(getItemInInventorySlot(getPlayerInventorySelection(), 0).name) != null) {
                            getItemModifier(getItemInInventorySlot(getPlayerInventorySelection(), 0).name).onPlace(lastPos);
                        }
                    } else{
                        System.out.println("test3");
                    }
                }
            }
            else {
                setPlayerWorldSelectionPos(finalPos);
            }
        }
        else {
            setPlayerWorldSelectionPos(null);
        }
    }

    private static void destroyBlock(Vector3f flooredPos) {
        int thisBlock = getBlock((int)flooredPos.x, (int)flooredPos.y, (int)flooredPos.z);
        if (thisBlock < 0){
            return;
        }
        digBlock((int)flooredPos.x, (int)flooredPos.y, (int)flooredPos.z);
        onDigCall(thisBlock, flooredPos);

        for (int i = 0; i < 20; i++) {
            createParticle(new Vector3f(flooredPos.x + ((float)Math.random()-0.5f), flooredPos.y + ((float)Math.random()-0.5f), flooredPos.z + ((float)Math.random()-0.5f)), new Vector3f((float)(Math.random()-0.5f) * 2f, (float)Math.ceil(Math.random()*5f), (float)(Math.random()-0.5f) * 2f), thisBlock);
        }
    }
    private static void rayPlaceBlock(Vector3f flooredPos, int ID) {

        placeBlock((int) flooredPos.x, (int) flooredPos.y, (int) flooredPos.z, ID, getPlayerDir());

        onPlaceCall(ID, flooredPos);

        removeItemFromInventory(getCurrentInventorySelection(), 0);
    }
}
