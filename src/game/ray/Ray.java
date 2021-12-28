package game.ray;

import game.item.Item;
import game.mob.MobObject;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static engine.network.Networking.*;
import static game.blocks.BlockDefinition.*;
import static game.chunk.Chunk.*;
import static game.collision.Collision.wouldCollidePlacing;
import static game.collision.PointCollision.pointIsWithin;
import static game.collision.PointCollision.setPointAABB;
import static game.crafting.Inventory.getItemInInventorySlot;
import static game.crafting.Inventory.removeItemFromInventory;
import static game.item.ItemDefinition.getItemModifier;
import static game.mob.Mob.getAllMobs;
import static game.mob.Mob.punchMob;
import static game.particle.Particle.createParticle;
import static game.player.Player.*;

public class Ray {

    private static final Vector3i finalPos   = new Vector3i();
    private static final Vector3d newPos     = new Vector3d();
    private static final Vector3d realNewPos = new Vector3d();
    private static final Vector3d lastPos    = new Vector3d();
    private static final Vector3d cachePos   = new Vector3d();
    private static final Vector3i pointedThingAbove = new Vector3i();

    public static void playerRayCast(Vector3d pos, Vector3f dir, float length, boolean mining, boolean placing, boolean hasMined) {

        pointedThingAbove.set(0,0,0);

        byte foundBlock = -1;

        MobObject foundMob = null;

        //get all mobs once
        MobObject[] mobs = getAllMobs();

        for(double step = 0d; step <= length ; step += 0.001d) {

            cachePos.x = dir.x * step;
            cachePos.y = dir.y * step;
            cachePos.z = dir.z * step;

            newPos.x = Math.floor(pos.x + cachePos.x);
            newPos.y = Math.floor(pos.y + cachePos.y);
            newPos.z = Math.floor(pos.z + cachePos.z);

            realNewPos.x = pos.x + cachePos.x;
            realNewPos.y = pos.y + cachePos.y;
            realNewPos.z = pos.z + cachePos.z;


            for (MobObject thisMob : mobs){

                //null pointer catch
                if (thisMob == null){
                    continue;
                }

                if (thisMob.pos.distance(realNewPos) <= 4.5 && thisMob.health > 0){
                    setPointAABB(thisMob.pos.x, thisMob.pos.y, thisMob.pos.z, thisMob.width,thisMob.height);
                    if(pointIsWithin(realNewPos.x, realNewPos.y, realNewPos.z)){
                        foundMob = thisMob;
                        break;
                    }
                }
            }

            //pointing to a mob, break mob detection loop
            if (foundMob != null){
                break;
            }

            //do not get block when the floored (integer) position is the same as the last floored position
            //stop wasting cpu resources
            if (!newPos.equals(lastPos)) {
                foundBlock = getBlock((int) newPos.x, (int) newPos.y, (int) newPos.z);

                if (foundBlock > 0 && isBlockPointable(foundBlock)) {

                    finalPos.set((int)Math.floor(newPos.x), (int)Math.floor(newPos.y), (int)Math.floor(newPos.z));

                    pointedThingAbove.set((int)Math.floor(lastPos.x), (int)Math.floor(lastPos.y), (int)Math.floor(lastPos.z));
                    break;
                }
            }

            lastPos.set(newPos);
        }



        //if a mob is found, the pointer is pointing to that thing
        //this needs a rework
        /*

        rework:
        only get if pointing to mob or a block

        secondary functions externally call on right click or on leftclick (punch) or on mine (dug)


         */
        if (foundMob != null){
            //punch mob if pointing to it
            if (mining) {
                punchMob(foundMob);
            }
            setPlayerWorldSelectionPos(null);
        } else {
            if (foundBlock > 0 && isBlockPointable(foundBlock)) {
                if (mining && hasMined) {
                    destroyBlock();
                } else if (placing) {

                    //todo: make this call on punched
                    if (!isPlayerSneaking() && blockHasOnRightClickCall(foundBlock)) {
                        getBlockModifier(foundBlock).onRightClick(finalPos.x, finalPos.y, finalPos.z);
                    } else {
                        Item wielding = getItemInInventorySlot(getPlayerInventorySelection(), 0);
                        if (wielding != null && !wouldCollidePlacing(getPlayerPos(),getPlayerWidth(), getPlayerHeight(), pointedThingAbove, wielding.definition.blockID, getPlayerDir()) && getItemInInventorySlot(getPlayerInventorySelection(), 0) != null && !getItemInInventorySlot(getPlayerInventorySelection(), 0).definition.isItem) {
                            rayPlaceBlock(getItemInInventorySlot(getPlayerInventorySelection(), 0).definition.blockID);
                        } else if (wielding != null && wielding.definition.isItem) {
                            if (getItemModifier(getItemInInventorySlot(getPlayerInventorySelection(), 0).name) != null) {
                                getItemModifier(getItemInInventorySlot(getPlayerInventorySelection(), 0).name).onPlace(finalPos, pointedThingAbove);
                            }
                        } else {
                            System.out.println("test3: This is a test of last branch of on place call");
                        }
                    }
                } else {
                    setPlayerWorldSelectionPos(finalPos);
                }
            } else {
                setPlayerWorldSelectionPos(null);
            }
        }
    }

    private static void destroyBlock() {

        byte thisBlock = getBlock(finalPos.x, finalPos.y, finalPos.z);

        if (thisBlock < 0) {
            return;
        }

        if (getIfMultiplayer()){
            sendOutNetworkBlockBreak(finalPos.x, finalPos.y, finalPos.z);
        } else {
            digBlock(finalPos.x, finalPos.y, finalPos.z);
        }
        for (int i = 0; i < 40 + (int)(Math.random() * 15); i++) {
            createParticle(finalPos.x + (Math.random()-0.5d), finalPos.y + (Math.random()-0.5d), finalPos.z + (Math.random()-0.5d), (float)(Math.random()-0.5f) * 2f, 0f, (float)(Math.random()-0.5f) * 2f, thisBlock);
        }
    }
    private static void rayPlaceBlock(byte ID) {
        if (getIfMultiplayer()){
            sendOutNetworkBlockPlace((int) lastPos.x, (int) lastPos.y, (int) lastPos.z, ID, getPlayerDir());
        } else {
            placeBlock((int) lastPos.x, (int) lastPos.y, (int) lastPos.z, ID, getPlayerDir());
        }

        removeItemFromInventory(getCurrentInventorySelection(), 0);
    }

    public static Vector3d genericWorldRaycast(Vector3d pos, Vector3f dir, float length){
        for(double step = 0d; step <= length ; step += 0.001d) {

            cachePos.x = dir.x * step;
            cachePos.y = dir.y * step;
            cachePos.z = dir.z * step;

            newPos.x = Math.floor(pos.x + cachePos.x);
            newPos.y = Math.floor(pos.y + cachePos.y);
            newPos.z = Math.floor(pos.z + cachePos.z);

            realNewPos.x = pos.x + cachePos.x;
            realNewPos.y = pos.y + cachePos.y;
            realNewPos.z = pos.z + cachePos.z;

            //stop wasting cpu resources
            if (!newPos.equals(lastPos)) {
                byte foundBlock = getBlock((int) newPos.x, (int) newPos.y, (int) newPos.z);
                if (foundBlock > 0 && isBlockPointable(foundBlock)) {
                    break;
                }
            }
            lastPos.set(newPos);
        }

        return realNewPos;
    }
}
