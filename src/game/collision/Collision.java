package game.collision;

import org.joml.AABBd;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Math;

import static engine.time.Time.getDelta;
import static game.chunk.Chunk.getBlock;
import static game.blocks.BlockDefinition.*;
import static game.chunk.Chunk.getBlockRotation;
import static game.player.Player.getIfPlayerIsJumping;
import static game.player.Player.setPlayerInWater;

public class Collision {
    private static float inWater = 0;
    private static final Vector3d clonedPos = new Vector3d();
    private static final Vector3i cachedPos = new Vector3i();
    private static final Vector3d oldPos = new Vector3d();
    private static final Vector3i fPos = new Vector3i();
    private static final Vector3d oldPosSneaking = new Vector3d();
    private static final Vector3f clonedInertia = new Vector3f();

    private static final AABBd entity = new AABBd();
    private static final AABBd block = new AABBd();

    //this probably definitely absolutely should not take isPlayer as a value
    public static boolean applyInertia(Vector3d pos, Vector3f inertia, boolean onGround, float width, float height, boolean gravity, boolean sneaking, boolean applyCollision, boolean airFriction, boolean isPlayer){
        double delta = getDelta();

        double adjustedDelta;

        //the precision goal for delta is 0.01f, this adjusts it to be so
        //the side effect, is the lower your FPS, the more it has to loop - but this has been adjusted to not be so extreme
        int loops = 1;

        if (delta >  0.01f){
            loops = (int) Math.floor(delta / 0.01f);
            adjustedDelta = (delta/(double)loops);
        } else {
            adjustedDelta = delta;
        }

        boolean onGroundLock = false;

        for (int i = 0; i < loops; i++) {
            inWater = 0;//reset water detection

            //limit speed on y axis
            if (inertia.y <= -50f) {
                inertia.y = -50f;
            } else if (inertia.y > 30f) {
                inertia.y = 30f;
            }

            //limit speed on x axis
            if (inertia.x <= -30f) {
                inertia.x = -30f;
            } else if (inertia.x > 30f) {
                inertia.x = 30f;
            }

            //limit speed on z axis
            if (inertia.z <= -30f) {
                inertia.z = -30f;
            } else if (inertia.z > 30f) {
                inertia.z = 30f;
            }

            if (applyCollision) {
                oldPosSneaking.set(pos);

                onGround = collisionDetect(pos, inertia, width, height,adjustedDelta);

                //ground lock can only be shifted to true
                if (onGround){
                    onGroundLock = true;
                }

                if (sneaking && !getIfPlayerIsJumping()) {
                    int axisFallingOff = sneakCollisionDetect(pos, inertia, width, height);

                    if (axisFallingOff == 1) {
                        pos.x = oldPosSneaking.x;
                        inertia.x = 0;
                    } else if (axisFallingOff == 2) {
                        pos.z = oldPosSneaking.z;
                        inertia.z = 0;
                    } else if (axisFallingOff == 3) {
                        pos.x = oldPosSneaking.x;
                        inertia.x = 0;
                        pos.z = oldPosSneaking.z;
                        inertia.z = 0;
                    }

                }

            } else {
                pos.add(inertia.x * adjustedDelta,inertia.y * adjustedDelta,inertia.z * adjustedDelta);
            }

            //apply friction
            if (onGround || airFriction) {
                // do (10 - 9.5f) for slippery!
                inertia.add((float)(-inertia.x * adjustedDelta * 10d),0,(float)(-inertia.z * adjustedDelta * 10d));
            }

            //water resistance
            if (inWater > 0.f) {
                inertia.add((float)(-inertia.x * adjustedDelta * inWater),0,(float)(-inertia.z * adjustedDelta * inWater));
            }

            if (gravity) {
                if (inWater > 0.f) {
                    if (isPlayer) {
                        setPlayerInWater(true);
                    }

                    //water resistance
                    if (inertia.y > -50f / inWater) {
                        inertia.y -= 1000 / inWater * adjustedDelta;
                        //slow down if falling too fast
                    } else if (inertia.y <= -50f / inWater) {
                        inertia.y += 2000 / inWater * adjustedDelta;
                    }
                } else {
                    if (isPlayer) {
                        setPlayerInWater(false);
                    }
                    //regular gravity
                    inertia.y -= 30f * adjustedDelta;
                }
            }
       }

        return onGroundLock;
    }

    //sneaking stuff
    private static byte sneakCollisionDetect(Vector3d pos, Vector3f inertia, float width, float height){

        byte binaryReturn = 0;

        boolean onGround = false;

        clonedInertia.set(inertia);
        clonedPos.set(pos);

        //todo: begin X collision detection
        clonedPos.y -= 0.05f;
        clonedPos.x += 0.1f * inertiaToDir(clonedInertia.x);


        //this sets fPos to the floored pos
        floorPos(clonedPos);

        for (byte x = -1; x <= 1; x++) {
            for (byte z = -1; z <= 1; z++) {

                cachedPos.set(fPos.x + x, fPos.y,fPos.z + z);

                byte cachedBlock = getBlock(cachedPos);
                byte rot = getBlockRotation(cachedPos);

                if (!onGround && cachedBlock > 0 && isBlockWalkable(cachedBlock)) {
                    onGround = sneakCollideYNegative(cachedPos.x, fPos.y, cachedPos.z, rot, width, height, cachedBlock);
                }
            }
        }

        if (!onGround) {
            binaryReturn += 1;
        }

        //reset position vectors
        clonedPos.set(pos);
        clonedInertia.set(inertia);

        onGround = false;

        //todo: Begin Z collision detection
        clonedPos.y -= 0.05f;
        clonedPos.z += 0.1f * inertiaToDir(clonedInertia.z);

        floorPos(clonedPos);

        for (byte x = -1; x <= 1; x++) {
            for (byte z = -1; z <= 1; z++) {

                cachedPos.set(fPos.x + x,fPos.y, fPos.z + z);

                byte cachedBlock = getBlock(cachedPos);
                byte rot = getBlockRotation(cachedPos);

                if (!onGround && cachedBlock > 0 && isBlockWalkable(cachedBlock)) {
                    onGround = sneakCollideYNegative(cachedPos.x, cachedPos.y, cachedPos.z, rot, width, height, cachedBlock);
                }
            }
        }

        if (!onGround) {
            binaryReturn += 2;
        }

        return binaryReturn;
    }

    private static boolean sneakCollideYNegative(int blockPosX, int blockPosY, int blockPosz, byte rot, float width, float height, byte blockID){
        for (float[] blockBox : getBlockShape(blockID, rot)) {
            entity.setMin(clonedPos.x - width, clonedPos.y, clonedPos.z - width)
                    .setMax(clonedPos.x + width, clonedPos.y + height, clonedPos.z + width);

            block.setMin(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosz)
                    .setMax(blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosz);

            if (entity.intersectsAABB(block)) {
                return true;
            }
        }
        return false;
    }

    //normal collision
    private static boolean collisionDetect(Vector3d pos, Vector3f inertia, float width, float height, double delta){

        boolean onGround = false;

        oldPos.set(pos);

        pos.y += inertia.y * delta;

        floorPos(pos);

        //todo: begin Y collision detection -- YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY

        switch (inertiaToDir(inertia.y)) {
            case -1:
                //y negative (falling)
                for (byte x = -1; x <= 1; x++) {
                    for (byte z = -1; z <= 1; z++) {
                        cachedPos.set(fPos.x + x,fPos.y,fPos.z + z);

                        byte cachedBlock = getBlock(cachedPos);

                        byte rot = getBlockRotation(cachedPos);

                        if (cachedBlock > 0 && isBlockWalkable(cachedBlock)) {
                            onGround = collideYNegative(cachedPos.x, cachedPos.y, cachedPos.z, rot, pos, inertia, width, height, onGround, cachedBlock);
                        }
                    }
                }
                break;
            case 1:
                //y positive (falling up)
                for (byte x = -1; x <= 1; x++) {
                    for (byte z = -1; z <= 1; z++) {
                        cachedPos.set(fPos.x + x,(int)Math.floor(pos.y + height),fPos.z + z);

                        byte cachedBlock = getBlock(cachedPos);

                        byte rot = getBlockRotation(cachedPos);

                        if (cachedBlock > 0 && isBlockWalkable(cachedBlock)) {
                            collideYPositive(cachedPos.x, cachedPos.y, cachedPos.z, rot, pos, inertia, width, height, cachedBlock);
                        }
                    }
                }
                break;
        }




        //todo: begin X collision detection -- XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

        //add inertia
        pos.x += inertia.x * delta;

        floorPos(pos);

        //this must start at -1f (loops through every position
        for (byte y =-1; y <= height + 1; y++) {
            for (byte x = -1; x <= 1; x++) {
                for (byte z = -1; z <= 1; z++) {

                    //update to polling position
                    cachedPos.set(fPos.x + x,fPos.y + y,fPos.z + z);

                    //get block ID
                    byte cachedBlock = getBlock(cachedPos);

                    //get rotation
                    byte rot = getBlockRotation(cachedPos);

                    //never collide with air (block ID 0)
                    if (cachedBlock > 0 && isBlockWalkable(cachedBlock)) {
                        collideX(cachedPos.x, cachedPos.y, cachedPos.z, rot, pos, inertia, width, height, cachedBlock);
                    }
                }
            }
        }


        //todo: Begin Z collision detection -- ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ

        pos.z += inertia.z * delta;

        floorPos(pos);

        //this must start at -1f (loops through every position
        for (byte y =-1; y <= height + 1; y++) {
            for (byte x = -1; x <= 1; x++) {
                for (byte z = -1; z <= 1; z++) {
                    //update to polling position
                    cachedPos.set(fPos.x + x,fPos.y + y,fPos.z + z);

                    //get block ID
                    byte cachedBlock = getBlock(cachedPos);

                    //get rotation
                    byte rot = getBlockRotation(cachedPos);

                    //never collide with air (block ID 0)
                    if (cachedBlock > 0 && isBlockWalkable(cachedBlock)) {
                        collideZ(cachedPos.x, cachedPos.y, cachedPos.z, rot, pos, inertia, width, height, cachedBlock);
                    }
                }
            }
        }

        //water check
        for (byte y = 0; y <= height; y++) {
            for (byte x = -1; x <= 1; x++) {
                for (byte z = -1; z <= 1; z++) {
                    cachedPos.set(fPos.x + x,fPos.y + y,fPos.z + z);

                    //get block ID
                    byte cachedBlock = getBlock(cachedPos);

                    //get rotation
                    byte rot = getBlockRotation(cachedPos);

                    //never collide with air (block ID 0)
                    if (cachedBlock > 0 && isBlockLiquid(cachedBlock)){
                        detectIfInWater(cachedPos.x, cachedPos.y, cachedPos.z, rot, pos, width, height, cachedBlock);
                    }
                }
            }
        }

        return onGround;
    }

    //a simple way to check if an object is in the water, only done on x and z passes so you can't stand
    //next to water and get slowed down
    private static void detectIfInWater(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, float width, float height, byte blockID){
        for (float[] blockBox : getBlockShape(blockID, rot)) {
            entity.setMin(pos.x - width, pos.y, pos.z - width)
                    .setMax(pos.x + width, pos.y + height, pos.z + width);

            block.setMin(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ)
                    .setMax(blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);
            if (entity.intersectsAABB(block)) {
                float localViscosity = getBlockViscosity(blockID);
                if (localViscosity > inWater){
                    inWater = localViscosity;
                }
            }
        }
    }

    private static boolean collideYNegative(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, float width, float height, boolean onGround, byte blockID){
        for (float[] blockBox : getBlockShape(blockID, rot)) {
            entity.setMin(pos.x - width, pos.y, pos.z - width)
                    .setMax( pos.x + width, pos.y + height, pos.z + width);

            block.setMin(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ)
                    .setMax(blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);
            if (entity.intersectsAABB(block)) {
                pos.y = block.maxY + 0.0000000001d;
                inertia.y = 0;
                onGround = true;
            }

        }
        return onGround;
    }

    private static void collideYPositive(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, float width, float height, byte blockID){
        for (float[] blockBox : getBlockShape(blockID, rot)) {
            entity.setMin(pos.x - width, pos.y, pos.z - width)
                    .setMax(pos.x + width, pos.y + height, pos.z + width);

            block.setMin(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ)
                    .setMax(blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);

            if (entity.intersectsAABB(block)) {
                pos.y = block.minY - height - 0.0000000001d;
                inertia.y = 0;
            }
        }
    }

    //TODO ----------------------------------XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

    private static void collideX(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, float width, float height, byte blockID){
        double blockXCenter;
        //run through X collisions
        for (float[] blockBox : getBlockShape(blockID, rot)) {
            block.setMin(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ)
                    .setMax(blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);
            blockXCenter = ((blockBox[0] + blockBox[3])/2d);
            //collide X negative
            if (blockPosX + blockXCenter <= pos.x) {
                entity.setMin(pos.x - width, pos.y, pos.z - width)
                        .setMax(pos.x + width, pos.y + height, pos.z + width);
                if (entity.intersectsAABB(block)) {
                    if (isSteppable(blockID) && inertia.y == 0) {
                        pos.y = block.maxY;
                    } else {
                        pos.x = block.maxX + width + 0.0000000001d;
                        inertia.x= 0f;
                    }
                }
            }
            //collide X positive
            if (blockPosX + blockXCenter >= pos.x) {
                entity.setMin(pos.x - width, pos.y, pos.z - width)
                        .setMax(pos.x + width, pos.y + height, pos.z + width);
                if (entity.intersectsAABB(block)) {
                    if (isSteppable(blockID) && inertia.y == 0) {
                        pos.y = block.maxY;
                    } else {
                        pos.x = block.minX - width - 0.0000000001d;
                        inertia.x= 0f;
                    }
                }
            }
        }

        //correction for the sides of stairs
        if (isSteppable(blockID) && pos.y - Collision.oldPos.y > 0.51d) {

            pos.y = Collision.oldPos.y;

            for (float[] blockBox : getBlockShape(blockID, rot)) {

                block.setMin(blockBox[0] + blockPosX, blockBox[1] + blockPosY, blockBox[2] + blockPosZ)
                        .setMax(blockBox[3] + blockPosX, blockBox[4] + blockPosY, blockBox[5] + blockPosZ);
                blockXCenter = ((blockBox[0] + blockBox[3])/2d);

                //collide X negative
                if (blockPosX + blockXCenter <= pos.x) {
                    entity.setMin(pos.x - width, pos.y, pos.z - width)
                            .setMax(pos.x + width, pos.y + height, pos.z + width);
                    if (entity.intersectsAABB(block)) {
                        pos.x = block.maxX + width + 0.0000000001d;
                        inertia.x = 0f;
                    }
                }

                //collide X positive
                if (blockPosX + blockXCenter >= pos.x) {
                    entity.setMin(pos.x - width, pos.y, pos.z - width)
                            .setMax(pos.x + width, pos.y + height, pos.z + width);
                    if (entity.intersectsAABB(block)) {
                        pos.x = block.minX - width - 0.0000000001d;
                        inertia.x = 0f;
                    }
                }
            }
        }
    }


    //TODO ----------------------------------ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ
    private static void collideZ(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, float width, float height, byte blockID){

        double blockZCenter;

        //run through Z collisions
        for (float[] blockBox : getBlockShape(blockID, rot)) {
            block.setMin(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ)
                    .setMax(blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);

            blockZCenter = ((blockBox[2] + blockBox[5])/2d);

            //collide Z negative
            if (blockPosZ + blockZCenter <= pos.z) {
                entity.setMin(pos.x - width, pos.y, pos.z - width)
                        .setMax(pos.x + width, pos.y + height, pos.z + width);
                if (entity.intersectsAABB(block)) {
                    if (isSteppable(blockID) && inertia.y == 0) {
                        pos.y = block.maxY;
                    }else {
                        pos.z = block.maxZ + width + 0.0000000001d;
                        inertia.z= 0f;
                    }
                }
            }

            //collide Z positive
            if (blockPosZ + blockZCenter >= pos.z) {
                entity.setMin(pos.x - width, pos.y, pos.z - width)
                        .setMax(pos.x + width, pos.y + height, pos.z + width);
                if (entity.intersectsAABB(block)) {
                    if (isSteppable(blockID) && inertia.y == 0) {
                        pos.y = block.maxY;
                    } else {
                        pos.z = block.minZ - width - 0.0000000001d;
                        inertia.z= 0f;
                    }
                }
            }
        }

        //correction for the sides of stairs
        if (pos.y - Collision.oldPos.y > 0.51d) {

            pos.y = Collision.oldPos.y;


            for (float[] blockBox : getBlockShape(blockID, rot)) {
                block.setMin(blockBox[0] + blockPosX, blockBox[1] + blockPosY, blockBox[2] + blockPosZ)
                        .setMax(blockBox[3] + blockPosX, blockBox[4] + blockPosY, blockBox[5] + blockPosZ);
                blockZCenter = ((blockBox[2] + blockBox[5])/2d);

                //collide Z negative
                if (blockPosZ + blockZCenter <= pos.z) {
                    entity.setMin(pos.x - width, pos.y, pos.z - width)
                            .setMax(pos.x + width, pos.y + height, pos.z + width);
                    if (entity.intersectsAABB(block)) {
                        pos.z = block.maxZ + width + 0.0000000001d;
                        inertia.z = 0f;
                    }
                }

                //collide Z positive
                if (blockPosZ + blockZCenter >= pos.z) {
                    entity.setMin(pos.x - width, pos.y, pos.z - width)
                            .setMax( pos.x + width, pos.y + height, pos.z + width);
                    if (entity.intersectsAABB(block)) {
                        pos.z = block.minZ - width - 0.0000000001d;
                        inertia.z = 0f;
                    }
                }
            }
        }
    }



    private static int inertiaToDir(float thisInertia){
        if (thisInertia > 0.0001f){
            return 1;
        } else if (thisInertia < -0.0001f){
            return -1;
        }

        return 0;
    }

    //precise collision prediction when placing
    public static boolean wouldCollidePlacing(Vector3d pos, float width, float height, Vector3i blockPos, byte blockID, byte rotation){
        entity.setMin(pos.x - width, pos.y, pos.z - width)
                .setMax(pos.x + width, pos.y + height, pos.z + width);
        for (float[] blockBox : getBlockShape(blockID, rotation)) {
            block.setMin(blockBox[0] + blockPos.x, blockBox[1] + blockPos.y, blockBox[2] + blockPos.z)
                    .setMax(blockBox[3] + blockPos.x, blockBox[4] + blockPos.y, blockBox[5] + blockPos.z);
            if (entity.intersectsAABB(block)){
                return true;
            }
        }

        return false;
    }

    //simple type cast bolt on to JOML
    //converts floored Vector3d to Vector3i
    private static void floorPos(Vector3d input){
        fPos.set((int) Math.floor(input.x), (int)Math.floor(input.y),(int)Math.floor(input.z));
    }
}