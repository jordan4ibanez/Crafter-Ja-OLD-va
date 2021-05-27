package game.ray;

import game.mob.MobObject;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static engine.FancyMath.getDistance;
import static game.chunk.Chunk.*;
import static game.blocks.BlockDefinition.*;
import static game.collision.Collision.wouldCollidePlacing;
import static game.collision.CustomAABB.setAABB;
import static game.collision.CustomBlockBox.setBlockBox;
import static game.collision.PointCollision.pointIsWithin;
import static game.collision.PointCollision.setPointAABB;
import static game.item.ItemDefinition.getItemModifier;
import static game.mob.Mob.getAllMobs;
import static game.mob.Mob.punchMob;
import static game.particle.Particle.createParticle;
import static game.crafting.Inventory.getItemInInventorySlot;
import static game.crafting.Inventory.removeItemFromInventory;
import static game.player.Player.*;

public class Ray {
    public static void playerRayCast(Vector3d pos, Vector3f dir, float length, boolean mining, boolean placing, boolean hasMined) {
        Vector3d finalPos = new Vector3d();
        Vector3d newPos   = new Vector3d();
        Vector3d realNewPos = new Vector3d();
        Vector3d lastPos  = new Vector3d();
        Vector3d cachePos = new Vector3d();
        int foundBlock = -1;
        MobObject[] mobs = null;

        if (mining){
            mobs = getAllMobs();
        }
        MobObject foundMob = null;

        for(float step = 0f; step <= length ; step += 0.001d) {

            cachePos.x = dir.x * step;
            cachePos.y = dir.y * step;
            cachePos.z = dir.z * step;

            newPos.x = Math.floor(pos.x + cachePos.x);
            newPos.y = Math.floor(pos.y + cachePos.y);
            newPos.z = Math.floor(pos.z + cachePos.z);

            realNewPos.x = pos.x + cachePos.x;
            realNewPos.y = pos.y + cachePos.y;
            realNewPos.z = pos.z + cachePos.z;

            if (mining){
                for (MobObject thisMob : mobs){
                    if (thisMob == null){
                        continue;
                    }
                    if (getDistance(thisMob.pos, realNewPos) <= 4.5){
                        setPointAABB(thisMob.pos.x, thisMob.pos.y, thisMob.pos.z, thisMob.width,thisMob.height);
                        if(pointIsWithin(realNewPos.x, realNewPos.y, realNewPos.z)){
                            foundMob = thisMob;
                            break;
                        }
                    }
                }
            }

            if (foundMob != null){
                break;
            }

            //stop wasting cpu resources
            if (newPos.x != lastPos.x || newPos.y != lastPos.y || newPos.z != lastPos.z) {
                foundBlock = getBlock((int) newPos.x, (int) newPos.y, (int) newPos.z);

                if (foundBlock > 0 && isBlockPointable(foundBlock)) {
                    finalPos = newPos;
                    break;
                }
            }

            lastPos = new Vector3d(newPos);
        }

        if (foundMob != null){
            punchMob(foundMob);
        } else {
            if (foundBlock > 0 && getBlockDefinition(foundBlock).pointable) {
                if (mining && hasMined) {
                    destroyBlock(finalPos);
                } else if (placing) {

                    //todo: make this call on punched
                    if (!isPlayerSneaking() && blockHasOnRightClickCall(foundBlock)) {

                        getBlockDefinition(foundBlock).blockModifier.onRightClick(finalPos);

                    } else {
                        setAABB(getPlayerPos().x, getPlayerPos().y, getPlayerPos().z, getPlayerWidth(), getPlayerHeight());
                        setBlockBox((int) lastPos.x, (int) lastPos.y, (int) lastPos.z, getBlockShape(1, (byte) 0)[0]); //TODO: make this check the actual block shapes

                        if (!wouldCollidePlacing() && getItemInInventorySlot(getPlayerInventorySelection(), 0) != null && !getItemInInventorySlot(getPlayerInventorySelection(), 0).definition.isItem) {
                            rayPlaceBlock(lastPos, getItemInInventorySlot(getPlayerInventorySelection(), 0).definition.blockID);
                        } else if (getItemInInventorySlot(getPlayerInventorySelection(), 0) != null && getItemInInventorySlot(getPlayerInventorySelection(), 0).definition.isItem) {
                            if (getItemModifier(getItemInInventorySlot(getPlayerInventorySelection(), 0).name) != null) {
                                getItemModifier(getItemInInventorySlot(getPlayerInventorySelection(), 0).name).onPlace(lastPos);
                            }
                        } else {
                            System.out.println("test3");
                        }
                    }
                } else {
                    setPlayerWorldSelectionPos(new Vector3i((int) finalPos.x, (int) finalPos.y, (int) finalPos.z));
                }
            } else {
                setPlayerWorldSelectionPos(null);
            }
        }
    }

    private static void destroyBlock(Vector3d flooredPos) {
        int thisBlock = getBlock((int)flooredPos.x, (int)flooredPos.y, (int)flooredPos.z);
        if (thisBlock < 0){
            return;
        }
        digBlock((int)flooredPos.x, (int)flooredPos.y, (int)flooredPos.z);
        onDigCall(thisBlock, flooredPos);



        for (int i = 0; i < 40 + (int)(Math.random() * 15); i++) {
            createParticle(new Vector3d(flooredPos.x + (Math.random()-0.5d), flooredPos.y + (Math.random()-0.5d), flooredPos.z + (Math.random()-0.5d)), new Vector3f((float)(Math.random()-0.5f) * 2f, 0f, (float)(Math.random()-0.5f) * 2f), thisBlock);
        }
    }
    private static void rayPlaceBlock(Vector3d flooredPos, int ID) {

        placeBlock((int) flooredPos.x, (int) flooredPos.y, (int) flooredPos.z, ID, getPlayerDir());

        onPlaceCall(ID, flooredPos);

        removeItemFromInventory(getCurrentInventorySelection(), 0);
    }


    public static Vector3d genericWorldRaycast(Vector3d pos, Vector3f dir, float length){
        Vector3d newPos   = new Vector3d();
        Vector3d realNewPos = new Vector3d();
        Vector3d lastPos  = new Vector3d();
        Vector3d cachePos = new Vector3d();
        int foundBlock;

        for(float step = 0f; step <= length ; step += 0.001d) {
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
            if (newPos.x != lastPos.x || newPos.y != lastPos.y || newPos.z != lastPos.z) {
                foundBlock = getBlock((int) newPos.x, (int) newPos.y, (int) newPos.z);
                if (foundBlock > 0 && isBlockPointable(foundBlock)) {
                    break;
                }
            }
            lastPos = new Vector3d(newPos);
        }

        return realNewPos;
    }
}
