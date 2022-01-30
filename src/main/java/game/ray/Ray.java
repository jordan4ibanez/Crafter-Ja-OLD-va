package game.ray;

import game.blocks.BlockDefinitionContainer;
import game.chunk.Chunk;
import game.player.Player;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class Ray {

    private Chunk chunk;
    private final BlockDefinitionContainer blockDefinitionContainer = new BlockDefinitionContainer();

    public void setChunk(Chunk chunk){
        if (this.chunk == null){
            this.chunk = chunk;
        }
    }

    public Ray(){
    }

    //this is now stack/cache happy as can be
    public void playerRayCast(Player player, Vector3d pos, Vector3f dir, float length, boolean mining, boolean placing, boolean hasMined) {

        int finalPosX = 0;
        int finalPosY = 0;
        int finalPosZ = 0;

        int pointedThingAboveX = 0;
        int pointedThingAboveY = 0;
        int pointedThingAboveZ = 0;

        byte foundBlock = -1;

        int foundMob = -1;

        //get all mobs once
        //IntSet mobs = getMobKeys();

        int oldFlooredPosX = 0;
        int oldFlooredPosY = 0;
        int oldFlooredPosZ = 0;

        for(double step = 0d; step <= length ; step += 0.01d) {

            double cachedPosX = dir.x * step;
            double cachedPosY = dir.y * step;
            double cachedPosZ = dir.z * step;

            int newFlooredPosX = (int) Math.floor(pos.x + cachedPosX);
            int newFlooredPosY = (int) Math.floor(pos.y + cachedPosY);
            int newFlooredPosZ = (int) Math.floor(pos.z + cachedPosZ);

            double realNewPosX = pos.x + cachedPosX;
            double realNewPosY = pos.y + cachedPosY;
            double realNewPosZ = pos.z + cachedPosZ;

            /*
            for (int thisMob : mobs){
                //reference - not new variable
                Vector3d mobPos = getMobPos(thisMob);

                byte thisMobHealth = getMobHealth(thisMob);
                int thisMobDefinitionID = getMobID(thisMob);

                if (mobPos.distance(realNewPosX, realNewPosY, realNewPosZ) <= 4.5 && thisMobHealth > 0){
                    
                    setAABBEntity(mobPos.x, mobPos.y, mobPos.z, getMobDefinitionWidth(thisMobDefinitionID),getMobDefinitionHeight(thisMobDefinitionID));
                    
                    if(pointIsWithinEntity(realNewPosX, realNewPosY, realNewPosZ)){
                        foundMob = thisMob;
                        break;
                    }
                }
            }


            //pointing to a mob, break mob detection loop
            if (foundMob != -1){
                break;
            }
             */

            //do not get block when the floored (integer) position is the same as the last floored position
            //stop wasting cpu resources
            if (newFlooredPosX == oldFlooredPosX || newFlooredPosY == oldFlooredPosY || newFlooredPosZ == oldFlooredPosZ) {
                foundBlock = chunk.getBlock( new Vector3i(newFlooredPosX, newFlooredPosY, newFlooredPosZ));

                if (foundBlock > 0 && blockDefinitionContainer.isPointable(foundBlock)) {

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
                //punchMob(foundMob);
            }
            //setPlayerWorldSelectionPos(null);
        } else if (foundBlock > 0) {
            if (mining && hasMined) {
                destroyBlock(new Vector3i(finalPosX, finalPosY, finalPosZ));
            } else if (placing) {

                /*
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
                */
            } else {
                player.setPlayerWorldSelectionPos(new Vector3i(finalPosX, finalPosY, finalPosZ));
            }
        } else {
            player.setPlayerWorldSelectionPos(null);
        }
    }

    private void destroyBlock(Vector3i pos) {

        byte thisBlock = chunk.getBlock(pos);

        if (thisBlock < 0) {
            return;
        }

        /*
        if (getIfMultiplayer()){
            sendOutNetworkBlockBreak(posX, posY, posZ);
        } else {*/
            chunk.digBlock(pos);
        //}
        for (int i = 0; i < 40 + (int)(Math.random() * 15); i++) {
            //createParticle((double)posX + (Math.random()), (double)posY + (Math.random()), (double)posZ + (Math.random()), (float)(Math.random()-0.5f) * 2f, 0f, (float)(Math.random()-0.5f) * 2f, thisBlock);
        }
    }
    private void rayPlaceBlock(byte ID, int pointedThingAboveX, int pointedThingAboveY, int pointedThingAboveZ) {
        /*if (getIfMultiplayer()){
            sendOutNetworkBlockPlace( pointedThingAboveX, pointedThingAboveY, pointedThingAboveZ, ID, getPlayerDir());
        } else {
         */
            //chunk.placeBlock( pointedThingAboveX, pointedThingAboveY, pointedThingAboveZ, ID, getPlayerDir());
        //}

        //removeItemFromInventory("main", getCurrentInventorySelection(), 0);
    }

    private final Vector3d cameraWorker = new Vector3d();

    public Vector3d cameraRayCast(Vector3d pos, Vector3f dir, float length){
        double realNewPosX = 0;
        double realNewPosY = 0;
        double realNewPosZ = 0;

        int oldFlooredPosX = 0;
        int oldFlooredPosY = 0;
        int oldFlooredPosZ = 0;

        for(double step = 0d; step <= length ; step += 0.01d) {

            double cachePosX = dir.x * step;
            double cachePosY = dir.y * step;
            double cachePosZ = dir.z * step;

            int newFlooredPosX = (int) Math.floor(pos.x + cachePosX);
            int newFlooredPosY = (int) Math.floor(pos.y + cachePosY);
            int newFlooredPosZ = (int) Math.floor(pos.z + cachePosZ);

            realNewPosX = pos.x + cachePosX;
            realNewPosY = pos.y + cachePosY;
            realNewPosZ = pos.z + cachePosZ;

            //stop wasting cpu resources
            if (newFlooredPosX != oldFlooredPosX || newFlooredPosY != oldFlooredPosY || newFlooredPosZ != oldFlooredPosZ) {
                byte foundBlock = chunk.getBlock(new Vector3i(newFlooredPosX, newFlooredPosY, newFlooredPosZ));
                if (foundBlock > 0 && blockDefinitionContainer.isPointable(foundBlock)) {
                    break;
                }
            }
            oldFlooredPosX = newFlooredPosX;
            oldFlooredPosY = newFlooredPosY;
            oldFlooredPosZ = newFlooredPosZ;
        }

        return cameraWorker.set(realNewPosX, realNewPosY, realNewPosZ);
    }

    public boolean lineOfSight(Vector3d pos1, Vector3d pos2){
        final Vector3d newPos   = new Vector3d();
        final Vector3d lastPos  = new Vector3d();
        final Vector3d cachePos = new Vector3d();
        final Vector3f dir = new Vector3f();
        dir.set(pos2.x - pos1.x, pos2.y-pos1.y, pos2.z - pos1.z).normalize();

        //this does not have to be perfect, can afford float imprecision
        for(float step = 0f; step <= pos1.distance(pos2) ; step += 0.01f) {

            cachePos.x = dir.x * step;
            cachePos.y = dir.y * step;
            cachePos.z = dir.z * step;

            newPos.x = Math.floor(pos1.x + cachePos.x);
            newPos.y = Math.floor(pos1.y + cachePos.y);
            newPos.z = Math.floor(pos1.z + cachePos.z);

            //stop wasting cpu resources
            if (!newPos.equals(lastPos)) {
                byte foundBlock = chunk.getBlock(new Vector3i((int) newPos.x, (int) newPos.y, (int) newPos.z));
                if (foundBlock > 0 && blockDefinitionContainer.getWalkable(foundBlock)) {
                    return false;
                }
            }
            lastPos.set(newPos);
        }

        return true;
    }
}
