package game.collision;

import org.joml.AABBd;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static engine.time.Time.getDelta;
import static game.chunk.Chunk.getBlock;
import static game.blocks.BlockDefinition.*;
import static game.chunk.Chunk.getBlockRotation;
import static game.player.Player.getIfPlayerIsJumping;
import static game.player.Player.setPlayerInWater;

public class Collision {
    private static float inWater = 0;

    private static double adjustedDelta;

    //this probably definitely absolutely should not take isPlayer as a value
    public static boolean applyInertia(Vector3d pos, Vector3f inertia, boolean onGround, float width, float height, boolean gravity, boolean sneaking, boolean applyCollision, boolean airFriction, boolean isPlayer){
        double delta = getDelta();

        //specific debug
        if (height == 3){
            //return false;
        }

        //the precision goal for delta is 0.001f, this adjusts it to be so
        //the side effect, is the lower your FPS, the more it has to loop
        int loops = 1;

        boolean onGroundLock = false;

        if (delta >  0.001f){
            loops = (int)Math.floor(delta / 0.001f);
            adjustedDelta = (delta/(double)loops);
        } else {
            adjustedDelta = delta;
        }



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
                if (sneaking && !getIfPlayerIsJumping()) {

                    Vector3d oldPos = new Vector3d(pos);

                    onGround = collisionDetect(pos, inertia, width, height);

                    if (onGround){
                        onGroundLock = true;
                    }

                    int axisFallingOff = sneakCollisionDetect(pos, inertia, width, height);

                    if (axisFallingOff == 1) {
                        pos.x = oldPos.x;
                        inertia.x = 0;
                    } else if (axisFallingOff == 2) {
                        pos.z = oldPos.z;
                        inertia.z = 0;
                    } else if (axisFallingOff == 3) {
                        pos.x = oldPos.x;
                        inertia.x = 0;
                        pos.z = oldPos.z;
                        inertia.z = 0;
                    }

                } else {
                    onGround = collisionDetect(pos, inertia, width, height);
                    if (onGround){
                        onGroundLock = true;
                    }
                }

            } else {
                pos.x += inertia.x * adjustedDelta;
                pos.y += inertia.y * adjustedDelta;
                pos.z += inertia.z * adjustedDelta;
            }

            //apply friction
            if (onGround || airFriction) {
                inertia.x += -inertia.x * adjustedDelta * 10; // do (10 - 9.5f) for slippery!
                inertia.z += -inertia.z * adjustedDelta * 10;
            }

            //water resistance
            if (inWater > 0.f) {
                inertia.x += -inertia.x * adjustedDelta * inWater;
                inertia.z += -inertia.z * adjustedDelta * inWater;
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

        Vector3f clonedInertia = new Vector3f(inertia);
        Vector3d clonedPos = new Vector3d(pos);

        //todo: begin X collision detection
        clonedPos.y -= 0.05f;
        clonedPos.x += 0.1f * inertiaToDir(clonedInertia.x);


        final Vector3i fPos = floorPos(clonedPos);
        final Vector3i cachedPos = new Vector3i();

        for (byte x = -1; x <= 1; x++) {
            for (byte z = -1; z <= 1; z++) {

                cachedPos.x = fPos.x + x;
                cachedPos.y = fPos.y;
                cachedPos.z = fPos.z + z;

                byte cachedBlock = getBlock(cachedPos);
                byte rot = getBlockRotation(cachedPos);

                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    onGround = sneakCollideYNegative(cachedPos.x, fPos.y, cachedPos.z, rot, clonedPos, width, height, onGround, cachedBlock);
                }
            }
        }

        if (!onGround) {
            binaryReturn += 1;
        }

        //reset position vectors
        clonedPos = new Vector3d(pos);
        clonedInertia = new Vector3f(inertia);

        onGround = false;

        //todo: Begin Z collision detection
        clonedPos.y -= 0.05f;
        clonedPos.z += 0.1f * inertiaToDir(clonedInertia.z);


        fPos.set(floorPos(clonedPos));

        for (byte x = -1; x <= 1; x++) {
            for (byte z = -1; z <= 1; z++) {

                cachedPos.x = fPos.x + x;
                cachedPos.y = fPos.y;
                cachedPos.z = fPos.z + z;

                byte cachedBlock = getBlock(cachedPos);
                byte rot = getBlockRotation(cachedPos);

                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    onGround = sneakCollideYNegative(cachedPos.x, cachedPos.y, cachedPos.z, rot, clonedPos, width, height, onGround, cachedBlock);
                }
            }
        }

        if (!onGround) {
            binaryReturn += 2;
        }

        return binaryReturn;
    }

    private static boolean sneakCollideYNegative(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3d pos, float width, float height, boolean onGround, byte blockID){
        for (float[] blockBox : getBlockShape(blockID, rot)) {
            final AABBd entity = new AABBd(pos.x - width, pos.y, pos.z - width, pos.x + width, pos.y + height, pos.z + width);
            final AABBd block  = new AABBd(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosz,blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosz);

            if (entity.intersectsAABB(block)) {
                return true;
            }
        }
        return false;
    }

    //normal collision
    private static boolean collisionDetect(Vector3d pos, Vector3f inertia, float width, float height){

        boolean onGround = false;

        final Vector3d oldPos = new Vector3d(pos);

        pos.y += inertia.y * adjustedDelta;

        final Vector3i fPos = floorPos(pos);


        //todo: begin Y collision detection -- YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY

        final Vector3i cachedPos = new Vector3i();

        switch (inertiaToDir(inertia.y)) {
            case -1:
                //y negative (falling)
                for (byte x = -1; x <= 1; x++) {
                    for (byte z = -1; z <= 1; z++) {
                        cachedPos.x = fPos.x + x;
                        cachedPos.y = fPos.y;
                        cachedPos.z = fPos.z + z;

                        byte cachedBlock = getBlock(cachedPos);

                        byte rot = getBlockRotation(cachedPos);

                        if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                            onGround = collideYNegative(cachedPos.x, cachedPos.y, cachedPos.z, rot, pos, inertia, width, height, onGround, cachedBlock);
                        }
                    }
                }
                break;
            case 1:
                //y positive (falling up)
                for (byte x = -1; x <= 1; x++) {
                    for (byte z = -1; z <= 1; z++) {
                        cachedPos.x = fPos.x + x;
                        cachedPos.y = (int)Math.floor(pos.y + height);
                        cachedPos.z = fPos.z + z;

                        byte cachedBlock = getBlock(cachedPos);

                        byte rot = getBlockRotation(cachedPos);

                        if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                            collideYPositive(cachedPos.x, cachedPos.y, cachedPos.z, rot, pos, inertia, width, height, cachedBlock);
                        }
                    }
                }
                break;
        }




        //todo: begin X collision detection -- XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

        //add inertia
        pos.x += inertia.x * adjustedDelta;

        //must clone the vector object
        fPos.set(floorPos(pos));

        //this must start at -1f (loops through every position
        for (byte y =-1; y <= height + 1; y++) {
            for (byte x = -1; x <= 1; x++) {
                for (byte z = -1; z <= 1; z++) {

                    //update to polling position
                    cachedPos.x = fPos.x + x;
                    cachedPos.y = fPos.y + y;
                    cachedPos.z = fPos.z + z;

                    //get block ID
                    byte cachedBlock = getBlock(cachedPos);

                    //get rotation
                    byte rot = getBlockRotation(cachedPos);

                    //never collide with air (block ID 0)
                    if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                        collideX(cachedPos.x, cachedPos.y, cachedPos.z, rot, pos, inertia, oldPos, width, height, cachedBlock);
                    }
                }
            }
        }


        //todo: Begin Z collision detection -- ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ

        pos.z += inertia.z * adjustedDelta;

        fPos.set(floorPos(pos));

        //this must start at -1f (loops through every position
        for (byte y =-1; y <= height + 1; y++) {
            for (byte x = -1; x <= 1; x++) {
                for (byte z = -1; z <= 1; z++) {
                    //update to polling position
                    cachedPos.x = fPos.x + x;
                    cachedPos.y = fPos.y + y;
                    cachedPos.z = fPos.z + z;

                    //get block ID
                    byte cachedBlock = getBlock(cachedPos);

                    //get rotation
                    byte rot = getBlockRotation(cachedPos);

                    //never collide with air (block ID 0)
                    if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                        collideZ(cachedPos.x, cachedPos.y, cachedPos.z, rot, pos, inertia, oldPos, width, height, cachedBlock);
                    }
                }
            }
        }

        //water check
        for (byte y = 0; y <= height; y++) {
            for (byte x = -1; x <= 1; x++) {
                for (byte z = -1; z <= 1; z++) {
                    cachedPos.x = fPos.x + x;
                    cachedPos.y = fPos.y + y;
                    cachedPos.z = fPos.z + z;

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
            final AABBd entity = new AABBd(pos.x - width, pos.y, pos.z - width, pos.x + width, pos.y + height, pos.z + width);
            final AABBd block  = new AABBd(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ,blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);
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
            final AABBd entity = new AABBd(pos.x - width, pos.y, pos.z - width, pos.x + width, pos.y + height, pos.z + width);
            final AABBd block  = new AABBd(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ,blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);
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
            final AABBd entity = new AABBd(pos.x - width, pos.y, pos.z - width, pos.x + width, pos.y + height, pos.z + width);
            final AABBd block  = new AABBd(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ,blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);
            if (entity.intersectsAABB(block)) {
                pos.y = block.minY - height - 0.0000000001d;
                inertia.y = 0;
            }
        }
    }

    //TODO ----------------------------------XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

    private static void collideX(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, Vector3d oldPos, float width, float height, byte blockID){
        //run through X collisions
        for (float[] blockBox : getBlockShape(blockID, rot)) {
            final AABBd entity = new AABBd(pos.x - width, pos.y, pos.z - width, pos.x + width, pos.y + height, pos.z + width);
            final AABBd block  = new AABBd(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ,blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);
            //collide X negative
            if (blockPosX + 0.5d <= pos.x) {
                if (entity.intersectsAABB(block)) {
                    if (isSteppable(blockID)) {
                        pos.y = block.maxY;
                    } else {
                        pos.x = block.maxX + width + 0.0000000001d;
                        inertia.x= 0f;;
                    }
                }
            }
            //collide X positive
            if (blockPosX + 0.5d >= pos.x) {
                if (entity.intersectsAABB(block)) {
                    if (isSteppable(blockID)) {
                        pos.y = block.maxY;
                    } else {
                        pos.x = block.minX - width - 0.0000000001d;
                        inertia.x= 0f;;
                    }
                }
            }
        }

        //correction for the sides of stairs
        if (isSteppable(blockID) && pos.y - oldPos.y > 0.51d) {

            pos.y = oldPos.y;

            //collide X negative
            if (blockPosX + 0.5d <= pos.x) {
                for (float[] blockBox : getBlockShape(blockID, rot)) {
                    final AABBd entity = new AABBd(pos.x - width, pos.y, pos.z - width, pos.x + width, pos.y + height, pos.z + width);
                    final AABBd block  = new AABBd(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ,blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);
                    if (entity.intersectsAABB(block)) {
                        pos.x = block.maxX + width + 0.0000000001d;
                        inertia.x= 0f;;
                    }
                }
            }

            //collide X positive
            if (blockPosX + 0.5d >= pos.x) {
                for (float[] blockBox : getBlockShape(blockID, rot)) {
                    final AABBd entity = new AABBd(pos.x - width, pos.y, pos.z - width, pos.x + width, pos.y + height, pos.z + width);
                    final AABBd block  = new AABBd(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ,blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);
                    if (entity.intersectsAABB(block)) {
                        pos.x = block.minX - width - 0.0000000001d;
                        inertia.x= 0f;;
                    }
                }
            }
        }
    }


    //TODO ----------------------------------ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ

    private static void collideZ(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, Vector3d oldPos, float width, float height, byte blockID){
        //run through Z collisions
        for (float[] blockBox : getBlockShape(blockID, rot)) {
            final AABBd entity = new AABBd(pos.x - width, pos.y, pos.z - width, pos.x + width, pos.y + height, pos.z + width);
            final AABBd block  = new AABBd(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ,blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);

            //collide Z negative
            if (blockPosZ + 0.5d <= pos.z) {
                if (entity.intersectsAABB(block)) {
                    if (isSteppable(blockID)) {
                        pos.y = block.maxY;
                    }else {
                        pos.z = block.maxZ + width + 0.0000000001d;
                        inertia.z= 0f;;
                    }
                }
            }

            //collide Z positive
            if (blockPosZ + 0.5d >= pos.z) {
                if (entity.intersectsAABB(block)) {
                    if (isSteppable(blockID)) {
                        pos.y = block.maxY;
                    } else {
                        pos.z = block.minZ - width - 0.0000000001d;
                        inertia.z= 0f;;
                    }
                }
            }
        }

        //correction for the sides of stairs
        if (pos.y - oldPos.y > 0.51d) {

            pos.y = oldPos.y;


            //collide Z negative
            if (blockPosZ + 0.5d <= pos.z) {
                for (float[] blockBox : getBlockShape(blockID, rot)) {
                    final AABBd entity = new AABBd(pos.x - width, pos.y, pos.z - width, pos.x + width, pos.y + height, pos.z + width);
                    final AABBd block  = new AABBd(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ,blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);
                    if (entity.intersectsAABB(block)) {
                        pos.z = block.maxZ + width + 0.0000000001d;
                        inertia.z= 0f;;
                    }
                }
            }

            //collide Z positive
            if (blockPosZ + 0.5d >= pos.z) {
                for (float[] blockBox : getBlockShape(blockID, rot)) {
                    final AABBd entity = new AABBd(pos.x - width, pos.y, pos.z - width, pos.x + width, pos.y + height, pos.z + width);
                    final AABBd block  = new AABBd(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ,blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);
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

    public static boolean wouldCollidePlacing(){
        return false;
    }

    //simple type cast bolt on to JOML
    //converts floored Vector3d to Vector3i
    private static Vector3i floorPos(Vector3d input){
        return new Vector3i((int)Math.floor(input.x), (int)Math.floor(input.y),(int)Math.floor(input.z));
    }
}