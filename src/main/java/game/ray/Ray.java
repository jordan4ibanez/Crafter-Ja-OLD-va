package game.ray;

import it.unimi.dsi.fastutil.ints.IntSet;
import org.joml.Math;
import org.joml.Vector3d;

import static engine.network.Networking.*;
import static game.blocks.BlockDefinition.*;
import static game.chunk.Chunk.*;
import static game.collision.Collision.wouldCollidePlacing;
import static game.collision.CollisionObject.pointIsWithin;
import static game.collision.CollisionObject.setPointAABB;
import static game.crafting.InventoryObject.getItemInInventory;
import static game.crafting.InventoryObject.removeItemFromInventory;
import static game.item.ItemDefinition.*;
import static game.mob.Mob.punchMob;
import static game.mob.MobDefinition.getMobDefinitionHeight;
import static game.mob.MobDefinition.getMobDefinitionWidth;
import static game.mob.MobObject.*;
import static game.particle.Particle.createParticle;
import static game.player.Player.*;

final public class Ray {

    //this is now stack/cache happy as can be
    public static void playerRayCast(double posX, double posY, double posZ, float dirX, float dirY, float dirZ, float length, boolean mining, boolean placing, boolean hasMined) {

        int finalPosX = 0;
        int finalPosY = 0;
        int finalPosZ = 0;


        int pointedThingAboveX = 0;
        int pointedThingAboveY = 0;
        int pointedThingAboveZ = 0;

        byte foundBlock = -1;

        int foundMob = -1;

        //get all mobs once
        IntSet mobs = getMobKeys();

        int oldFlooredPosX = 0;
        int oldFlooredPosY = 0;
        int oldFlooredPosZ = 0;

        double realNewPosX = 0;
        double realNewPosY = 0;
        double realNewPosZ = 0;

        for(double step = 0d; step <= length ; step += 0.001d) {

            double cachedPosX = dirX * step;
            double cachedPosY = dirY * step;
            double cachedPosZ = dirZ * step;

            int newFlooredPosX = (int) Math.floor(posX + cachedPosX);
            int newFlooredPosY = (int) Math.floor(posY + cachedPosY);
            int newFlooredPosZ = (int) Math.floor(posZ + cachedPosZ);

            realNewPosX = posX + cachedPosX;
            realNewPosY = posY + cachedPosY;
            realNewPosZ = posZ + cachedPosZ;

            for (int thisMob : mobs){
                //reference - not new variable
                Vector3d mobPos = getMobPos(thisMob);

                byte thisMobHealth = getMobHealth(thisMob);
                int thisMobDefinitionID = getMobID(thisMob);

                if (mobPos.distance(realNewPosX, realNewPosY, realNewPosZ) <= 4.5 && thisMobHealth > 0){
                    setPointAABB(mobPos.x, mobPos.y, mobPos.z, getMobDefinitionWidth(thisMobDefinitionID),getMobDefinitionHeight(thisMobDefinitionID));
                    if(pointIsWithin(realNewPosX, realNewPosY, realNewPosZ)){
                        foundMob = thisMob;
                        break;
                    }
                }
            }

            //pointing to a mob, break mob detection loop
            if (foundMob != -1){
                break;
            }

            //do not get block when the floored (integer) position is the same as the last floored position
            //stop wasting cpu resources
            if (newFlooredPosX == oldFlooredPosX || newFlooredPosY == oldFlooredPosY || newFlooredPosZ == oldFlooredPosZ) {
                foundBlock = getBlock( newFlooredPosX, newFlooredPosY, newFlooredPosZ);

                if (foundBlock > 0 && isBlockPointable(foundBlock)) {

                    finalPosX = newFlooredPosX;
                    finalPosY = newFlooredPosY;
                    finalPosZ = newFlooredPosZ;

                    pointedThingAboveX = oldFlooredPosX;
                    pointedThingAboveY = oldFlooredPosY;
                    pointedThingAboveZ = oldFlooredPosZ;
                    break;
                }
            }

            oldFlooredPosX = newFlooredPosX;
            oldFlooredPosY = newFlooredPosY;
            oldFlooredPosZ = newFlooredPosZ;
        }



        //if a mob is found, the pointer is pointing to that thing
        //this needs a rework
        /*

        rework:
        only get if pointing to mob or a block

        secondary functions externally call on right click or on leftclick (punch) or on mine (dug)
         */

        if (foundMob != -1){
            //punch mob if pointing to it
            if (mining) {
                punchMob(foundMob);
            }
            setPlayerWorldSelectionPos(null);
        } else {
            if (foundBlock > 0 && isBlockPointable(foundBlock)) {
                if (mining && hasMined) {
                    destroyBlock(finalPosX, finalPosY, finalPosZ);
                } else if (placing) {

                    //todo: make this call on punched
                    if (!isPlayerSneaking() && blockHasOnRightClickCall(foundBlock)) {
                        getBlockModifier(foundBlock).onRightClick(finalPosX, finalPosY, finalPosZ);
                    } else {
                        String wielding = getItemInInventory("main", getPlayerInventorySelection(), 0);
                        if (wielding != null && itemIsBlock(wielding) && !wouldCollidePlacing(getPlayerPos(),getPlayerWidth(), getPlayerHeight(), pointedThingAboveX, pointedThingAboveY, pointedThingAboveZ, getBlockID(wielding), getPlayerDir())) {
                            rayPlaceBlock(getBlockID(wielding), pointedThingAboveX, pointedThingAboveY, pointedThingAboveZ);
                        } else if (wielding != null && getIfItem(wielding)) {
                            if (getItemModifier(wielding) != null) {
                                getItemModifier(wielding).onPlace(finalPosX, finalPosY, finalPosZ, pointedThingAboveX, pointedThingAboveY, pointedThingAboveZ);
                            }
                        } else {
                            System.out.println("test3: This is a test of last branch of on place call");
                        }
                    }
                } else {
                    setPlayerWorldSelectionPos(finalPosX, finalPosY, finalPosZ);
                }
            } else {
                setPlayerWorldSelectionPos(null);
            }
        }
    }

    private static void destroyBlock(int posX, int posY, int posZ) {

        byte thisBlock = getBlock(posX, posY, posZ);

        if (thisBlock < 0) {
            return;
        }

        if (getIfMultiplayer()){
            sendOutNetworkBlockBreak(posX, posY, posZ);
        } else {
            digBlock(posX, posY, posZ);
        }
        for (int i = 0; i < 40 + (int)(Math.random() * 15); i++) {
            createParticle((double)posX + (Math.random()), (double)posY + (Math.random()), (double)posZ + (Math.random()), (float)(Math.random()-0.5f) * 2f, 0f, (float)(Math.random()-0.5f) * 2f, thisBlock);
        }
    }
    private static void rayPlaceBlock(byte ID, int pointedThingAboveX, int pointedThingAboveY, int pointedThingAboveZ) {
        if (getIfMultiplayer()){
            sendOutNetworkBlockPlace( pointedThingAboveX, pointedThingAboveY, pointedThingAboveZ, ID, getPlayerDir());
        } else {
            placeBlock( pointedThingAboveX, pointedThingAboveY, pointedThingAboveZ, ID, getPlayerDir());
        }

        removeItemFromInventory("main", getCurrentInventorySelection(), 0);
    }

    private static final Vector3d cameraWorker = new Vector3d();

    public static Vector3d cameraRayCast(double posX, double posY, double posZ, float dirX, float dirY, float dirZ, float length){
        double realNewPosX = 0;
        double realNewPosY = 0;
        double realNewPosZ = 0;

        int oldFlooredPosX = 0;
        int oldFlooredPosY = 0;
        int oldFlooredPosZ = 0;

        for(double step = 0d; step <= length ; step += 0.001d) {

            double cachePosX = dirX * step;
            double cachePosY = dirY * step;
            double cachePosZ = dirZ * step;

            int newFlooredPosX = (int) Math.floor(posX + cachePosX);
            int newFlooredPosY = (int) Math.floor(posY + cachePosY);
            int newFlooredPosZ = (int) Math.floor(posZ + cachePosZ);

            realNewPosX = posX + cachePosX;
            realNewPosY = posY + cachePosY;
            realNewPosZ = posZ + cachePosZ;

            //stop wasting cpu resources
            if (newFlooredPosX != oldFlooredPosX || newFlooredPosY != oldFlooredPosY || newFlooredPosZ != oldFlooredPosZ) {
                byte foundBlock = getBlock(newFlooredPosX, newFlooredPosY, newFlooredPosZ);
                if (foundBlock > 0 && isBlockPointable(foundBlock)) {
                    break;
                }
            }
            oldFlooredPosX = newFlooredPosX;
            oldFlooredPosY = newFlooredPosY;
            oldFlooredPosZ = newFlooredPosZ;
        }

        return cameraWorker.set(realNewPosX, realNewPosY, realNewPosZ);
    }
}
