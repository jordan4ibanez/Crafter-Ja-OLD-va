package game.collision;

import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.Time.getDelta;
import static game.chunk.Chunk.getBlock;
import static game.blocks.BlockDefinition.*;
import static game.chunk.Chunk.getBlockRotation;
import static game.collision.CollisionMath.floorPos;
import static game.collision.CustomAABB.*;
import static game.collision.CustomBlockBox.*;
import static game.player.Player.getIfPlayerIsJumping;
import static game.player.Player.setPlayerInWater;

public class Collision {
    private static float inWater = 0;

    //this probably definitely absolutely should not take isPlayer as a value
    //fix this later FIX ME FIX ME - DO OBJECT TYPE MATCHING
    public static boolean applyInertia(Vector3d pos, Vector3f inertia, boolean onGround, float width, float height, boolean gravity, boolean sneaking, boolean applyCollision, boolean airFriction, boolean isPlayer){


        float delta = getDelta();

        inWater = 0;//reset water detection

        //limit speed (falling or flying) - y axis
        if (inertia.y <= -70f){
            inertia.y = -70f;
        } else if (inertia.y > 70f){
            inertia.y = 70f;
        }

        if (applyCollision) {
            if (sneaking && !getIfPlayerIsJumping()) {

                Vector3d oldPos = new Vector3d(pos);

                onGround = collisionDetect(pos, inertia, width, height);

                int axisFallingOff = sneakCollisionDetect(pos, inertia, width, height);

                if(axisFallingOff == 1){
                    pos.x = oldPos.x;
                    inertia.x = 0;
                } else if (axisFallingOff == 2) {
                    pos.z = oldPos.z;
                    inertia.z = 0;
                } else if (axisFallingOff == 3){
                    pos.x = oldPos.x;
                    inertia.x = 0;
                    pos.z = oldPos.z;
                    inertia.z = 0;
                }

            } else {
                onGround = collisionDetect(pos, inertia, width, height);
            }

        } else {
            pos.x += inertia.x * delta;
            pos.y += inertia.y * delta;
            pos.z += inertia.z * delta;
        }

        //apply friction
        if (onGround || airFriction) {
            inertia.x += -inertia.x * delta * 10; // do (10 - 9.5f) for slippery!
            inertia.z += -inertia.z * delta * 10;
        }

        //water resistance
        if (inWater > 0.f){
            inertia.x += -inertia.x * delta * inWater;
            inertia.z += -inertia.z * delta * inWater;

            //inertia.y += -inertia.z * gameSpeed * inWater;
            //inertia.y = inertia.y / 1.2f;
        }

        if(gravity) {
            if (inWater > 0.f){
                if (isPlayer){
                    setPlayerInWater(true);
                }

                //water resistance
                if (inertia.y > -50f / inWater){
                    inertia.y -= 1000/inWater * delta;
                //slow down if falling too fast
                } else if (inertia.y <= -50f / inWater){
                    inertia.y += 2000/inWater * delta;
                }
            }else {
                if (isPlayer){
                    setPlayerInWater(false);
                }
                //regular gravity
                inertia.y -= 40f * delta;
            }
        }

        return onGround;
    }

    //these are class/method caches!! NOT FIELDS!
    private static Vector3d fPos;
    private static boolean onGround;
    private static double x,y,z;
    private static int cachedBlock;
    private static Vector3d cachedPos = new Vector3d(0d,0d,0d);



    //sneaking stuff

    private static int sneakCollisionDetect(Vector3d pos, Vector3f inertia, float width, float height){

        int binaryReturn = 0;

        boolean onGround = false;

        Vector3f clonedInertia = new Vector3f(inertia);
        Vector3d clonedPos = new Vector3d(pos);
        //todo: begin X collision detection
        clonedPos.y -= 0.05f;
        clonedPos.x += 0.1f * inertiaToDir(clonedInertia.x);

        fPos = floorPos(new Vector3d(clonedPos));

        for (x = -1; x <= 1; x++) {
            for (z = -1; z <= 1; z++) {

                Vector3d cachedPos = new Vector3d();
                cachedPos.x = fPos.x + x;
                cachedPos.y = fPos.y;
                cachedPos.z = fPos.z + z;

                cachedBlock = detectBlock(cachedPos);

                byte rot =  detectRot(cachedPos);

                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    onGround = sneakCollideYNegative((int)(fPos.x + x), (int)fPos.y, (int)(fPos.z + z), rot, clonedPos, clonedInertia, width, height, onGround, cachedBlock);
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

        fPos = floorPos(new Vector3d(clonedPos));

        for (x = -1; x <= 1; x++) {
            for (z = -1; z <= 1; z++) {

                Vector3d cachedPos = new Vector3d();
                cachedPos.x = fPos.x + x;
                cachedPos.y = fPos.y;
                cachedPos.z = fPos.z + z;

                cachedBlock = detectBlock(cachedPos);

                byte rot =  detectRot(cachedPos);

                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    onGround = sneakCollideYNegative((int)(fPos.x + x), (int)fPos.y, (int)(fPos.z + z), rot, clonedPos, clonedInertia, width, height, onGround, cachedBlock);
                }
            }
        }


        if (!onGround) {
            binaryReturn += 2;
        }


        return binaryReturn;
    }

    private static boolean sneakCollideYNegative(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3d pos, Vector3f inertia, float width, float height, boolean onGround, int blockID){
        for (double[] thisBlockBox : getBlockShape(blockID, rot)) {
            setAABB(pos.x, pos.y, pos.z, width, height);
            setBlockBox(blockPosX,blockPosY,blockPosz,thisBlockBox);

            if (isWithin()) {
                return true;
            }
        }
        return onGround;
    }





    //normal collision

    private static boolean collisionDetect(Vector3d pos, Vector3f inertia, float width, float height){

        float delta = getDelta();

        onGround = false;

        Vector3d oldPos = new Vector3d();

        oldPos.x = pos.x;
        oldPos.y = pos.y;
        oldPos.z = pos.z;

        pos.y += inertia.y * delta;

        fPos = floorPos(new Vector3d(pos));

        int up = 0;

        //todo: begin Y collision detection -- YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY
        switch (inertiaToDir(inertia.y)){
            case -1:
                y = (int)fPos.y;
                break;
            case 1:
                y = (int)Math.floor(pos.y + height);
                up = 1;
                break;
            default:
                y = 777;
                break;
        }

        if (y != 777) {
            switch (up){
                case 0:
                    for (x = -1; x <= 1; x++) {
                        for (z = -1; z <= 1; z++) {
                            cachedPos.x = fPos.x + x;
                            cachedPos.y = y;
                            cachedPos.z = fPos.z + z;

                            cachedBlock = detectBlock(cachedPos);

                            byte rot =  detectRot(cachedPos);

                            if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                                onGround = collideYNegative((int)(fPos.x + x), (int)y, (int)(fPos.z + z), rot, pos, inertia, width, height, onGround, cachedBlock);
                            }
                        }
                    }
                    break;
                case 1:
                    for (x = -1; x <= 1; x++) {
                        for (z = -1; z <= 1; z++) {
                            cachedPos.x = fPos.x + x;
                            cachedPos.y = y;
                            cachedPos.z = fPos.z + z;

                            cachedBlock = detectBlock(cachedPos);

                            byte rot =  detectRot(cachedPos);

                            if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                                collideYPositive((int)(fPos.x + x), (int)y, (int)(fPos.z + z), rot, pos, inertia, width, height, cachedBlock);
                            }
                        }
                    }
                    break;
            }
        }


        //todo: begin X collision detection -- XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

        //add inertia
        pos.x += inertia.x * delta;

        //must clone the vector object
        fPos = floorPos(new Vector3d(pos));

        //this must start at -1f (loops through every position
        for (float yy =-1; yy <= height + 1; yy++) {
            for (x = -1; x <= 1; x++) {
                for (z = -1; z <= 1; z++) {

                    //update to polling position
                    cachedPos.x = Math.floor(fPos.x + x);
                    cachedPos.y = Math.floor(pos.y + yy);
                    cachedPos.z = Math.floor(fPos.z + z);

                    //get block ID
                    cachedBlock = detectBlock(floorPos(cachedPos));

                    //get rotation
                    byte rot = detectRot(cachedPos);

                    //never collide with air (block ID 0)
                    if (cachedBlock > 0 && isWalkable(cachedBlock)) {

                        collideX((int)cachedPos.x, (int) (yy + pos.y), (int) cachedPos.z, rot, pos, inertia, oldPos, width, height, cachedBlock);
                    }
                }
            }
        }


        //todo: Begin Z collision detection -- ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ

        pos.z += inertia.z * delta;

        fPos = floorPos(new Vector3d(pos));

        //this must start at -1f (loops through every position
        for (float yy =-1; yy <= height + 1; yy++) {
            for (x = -1; x <= 1; x++) {
                for (z = -1; z <= 1; z++) {
                    //update to polling position
                    cachedPos.x = Math.floor(fPos.x + x);
                    cachedPos.y = Math.floor(pos.y + yy);
                    cachedPos.z = Math.floor(fPos.z + z);

                    //get block ID
                    cachedBlock = detectBlock(floorPos(cachedPos));

                    //get rotation
                    byte rot = detectRot(cachedPos);

                    //never collide with air (block ID 0)
                    if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                        collideZ((int)cachedPos.x, (int) (yy + pos.y), (int) cachedPos.z, rot, pos, inertia, oldPos, width, height, cachedBlock);
                    }
                }
            }
        }



        //water check
        for (float yy = 0; yy <= height; yy = yy + 0.5f) {
            for (x = -1; x <= 1; x++) {
                for (z = -1; z <= 1; z++) {
                    cachedPos.x = fPos.x + x;
                    cachedPos.y = yy + pos.y;
                    cachedPos.z = fPos.z + z;
                    cachedBlock = detectBlock(floorPos(cachedPos));

                    byte rot =  detectRot(cachedPos);

                    if (cachedBlock > 0 && isBlockLiquid(cachedBlock)){
                        detectIfInWater((int)(fPos.x + x), (int)(yy + pos.y), (int)(fPos.z + z), rot, pos, inertia, width, height, cachedBlock);
                    }
                }
            }
        }

        return onGround;
    }

    //a simple way to check if an object is in the water, only done on x and z passes so you can't stand
    //next to water and get slowed down
    private static void detectIfInWater(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3d pos, Vector3f inertia, float width, float height, int blockID){
        for (double[] thisBlockBox : getBlockShape(blockID, rot)) {
            setAABB(pos.x, pos.y, pos.z, width, height);
            setBlockBox(blockPosX,blockPosY,blockPosz,thisBlockBox);

            if (isWithin()) {
                float localViscosity = getBlockViscosity(blockID);

                if (localViscosity > inWater){
                    inWater = localViscosity;
                }
            }
        }
    }

    private static boolean collideYNegative(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3d pos, Vector3f inertia, float width, float height, boolean onGround, int blockID){
        for (double[] thisBlockBox : getBlockShape(blockID, rot)) {
            setAABB(pos.x, pos.y, pos.z, width, height);
            setBlockBox(blockPosX,blockPosY,blockPosz,thisBlockBox);
            //this coordinate is not within enough distance to get affected by floating point precision
            if (isWithin() && BlockBoxGetTop() > AABBGetBottom() && AABBGetBottom() - BlockBoxGetTop() > -0.1d) {
                pos.y = BlockBoxGetTop() + 0.0001d; //players position needs to constantly change or else this breaks stairs/slabs
                inertia.y = 0;
                onGround = true;
            }
        }
        return onGround;
    }

    private static void collideYPositive(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3d pos, Vector3f inertia, float width, float height, int blockID){
        for (double[] thisBlockBox : getBlockShape(blockID, rot)) {
            setAABB(pos.x, pos.y, pos.z, width, height);
            setBlockBox(blockPosX, blockPosY, blockPosz,thisBlockBox);
            //this coordinate is not within enough distance to get affected by floating point precision
            //head detection
            if (isWithin() && BlockBoxGetBottom() < AABBGetTop() && AABBGetTop() - BlockBoxGetBottom() < 0.1d) {
                pos.y = BlockBoxGetBottom() - height - 0.001d;
                inertia.y = 0;
            }
        }
    }


    //TODO ----------------------------------XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

    private static void collideX(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, Vector3d oldPos, float width, float height, int blockID){
        //run through X collisions
        for (double[] thisBlockBox : getBlockShape(blockID, rot)) {
            setAABB(pos.x, pos.y, pos.z, width, height);
            setBlockBox(blockPosX, blockPosY, blockPosZ, thisBlockBox);

            //collide X negative
            if (blockPosX + 0.5d <= pos.x) {
                if (isWithin() && BlockBoxGetRight() > AABBGetLeft() && BlockBoxGetRight() - AABBGetLeft() < 0.01d) {
                    if (isSteppable(blockID) && AABBGetBottom() - BlockBoxGetTop() < -0.48d) {
                        //steppable
                        pos.y = BlockBoxGetTop() + 0.0001d;
                    } else {
                        //not steppable
                        pos.x = BlockBoxGetRight() + width + 0.001d;
                        inertia.x /= 2f;
                    }
                }
            }
            //collide X positive
            if (blockPosX + 0.5d >= pos.x) {
                if (isWithin() && BlockBoxGetLeft() < AABBGetRight() && BlockBoxGetLeft() - AABBGetRight() > -0.01d) {
                    if (isSteppable(blockID) && AABBGetBottom() - BlockBoxGetTop() < -0.48d) {
                        //steppable
                        pos.y = BlockBoxGetTop() + 0.0001d;
                    } else {
                        //not steppable
                        pos.x = BlockBoxGetLeft() - width - 0.001d;
                        inertia.x /= 2f;
                    }
                }
            }
        }


        //correction for the sides of stairs
        if (isSteppable(blockID) && pos.y - oldPos.y > 0.51d) {

            pos.y = oldPos.y;

            //collide X negative
            if (blockPosX + 0.5d <= pos.x) {
                for (double[] thisBlockBox : getBlockShape(blockID, rot)) {
                    setAABB(pos.x, pos.y, pos.z, width, height);
                    setBlockBox(blockPosX, blockPosY, blockPosZ, thisBlockBox);
                    if (isWithin() && BlockBoxGetRight() > AABBGetLeft() && BlockBoxGetRight() - AABBGetLeft() < 0.01d) {
                        pos.x = BlockBoxGetRight() + width + 0.001d;
                        inertia.x /= 2f;
                    }
                }
            }

            //collide X positive
            if (blockPosX + 0.5d >= pos.x) {
                for (double[] thisBlockBox : getBlockShape(blockID, rot)) {
                    setAABB(pos.x, pos.y, pos.z, width, height);
                    setBlockBox(blockPosX, blockPosY, blockPosZ, thisBlockBox);
                    if (isWithin() && BlockBoxGetLeft() < AABBGetRight() && BlockBoxGetLeft() - AABBGetRight() > -0.01d) {
                        pos.x = BlockBoxGetLeft() - width - 0.001d;
                        inertia.x /= 2f;
                    }
                }
            }
        }
    }


    //TODO ----------------------------------ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ

    private static void collideZ(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, Vector3d oldPos, float width, float height, int blockID){
        //run through Z collisions
        for (double[] thisBlockBox : getBlockShape(blockID, rot)) {
            setAABB(pos.x, pos.y, pos.z, width, height);
            setBlockBox(blockPosX, blockPosY, blockPosZ, thisBlockBox);

            //collide Z negative
            if (blockPosZ + 0.5d <= pos.z) {
                if (isWithin() && BlockBoxGetBack() > AABBGetFront() && BlockBoxGetBack() - AABBGetFront() < 0.01d) {
                    if (isSteppable(blockID) && AABBGetBottom() - BlockBoxGetTop() < -0.48d) {
                        //steppable
                        pos.y = BlockBoxGetTop() + 0.0001d;
                    }else {
                        //not steppable
                        pos.z = BlockBoxGetBack() + width + 0.001d;
                        inertia.z /= 2f;
                    }
                }
            }
            //collide Z positive
            if (blockPosZ + 0.5d >= pos.z) {
                if (isWithin() && BlockBoxGetFront() < AABBGetBack() && BlockBoxGetFront() - AABBGetBack() > -0.01d) {
                    if (isSteppable(blockID) && AABBGetBottom() - BlockBoxGetTop() < -0.48d) {
                        //steppable
                        pos.y = BlockBoxGetTop() + 0.0001d;
                    } else {
                        //not steppable
                        pos.z = BlockBoxGetFront() - width - 0.001d;
                        inertia.z /= 2f;
                    }
                }
            }
        }

        //correction for the sides of stairs
        if (pos.y - oldPos.y > 0.51d) {

            pos.y = oldPos.y;

            //collide Z negative
            if (blockPosZ + 0.5d <= pos.z) {
                for (double[] thisBlockBox : getBlockShape(blockID, rot)) {
                    setAABB(pos.x, pos.y, pos.z, width, height);
                    setBlockBox(blockPosX, blockPosY, blockPosZ, thisBlockBox);
                    if (isWithin() && BlockBoxGetBack() > AABBGetFront() && BlockBoxGetBack() - AABBGetFront() < 0.01d) {
                        pos.z = BlockBoxGetBack() + width + 0.001d;
                        inertia.z /= 2f;
                    }
                }
            }

            //collide Z positive
            if (blockPosZ + 0.5d >= pos.z) {
                for (double[] thisBlockBox : getBlockShape(blockID, rot)) {
                    setAABB(pos.x, pos.y, pos.z, width, height);
                    setBlockBox(blockPosX, blockPosY, blockPosZ, thisBlockBox);
                    if (isWithin() && BlockBoxGetFront() < AABBGetBack() && BlockBoxGetFront() - AABBGetBack() > -0.01d) {
                        pos.z = BlockBoxGetFront() - width - 0.001d;
                        inertia.z /= 2f;
                    }
                }
            }
        }
    }


    private static boolean isWithin(){
        return !(AABBGetLeft() > BlockBoxGetRight() ||
                 AABBGetRight() < BlockBoxGetLeft() ||
                 AABBGetBottom() > BlockBoxGetTop() ||
                 AABBGetTop() < BlockBoxGetBottom() ||
                 AABBGetFront() > BlockBoxGetBack() ||
                 AABBGetBack() < BlockBoxGetFront());
    }

    private static int detectBlock(Vector3d flooredPos){
        return getBlock((int)flooredPos.x, (int)flooredPos.y, (int)flooredPos.z);
    }

    private static byte detectRot(Vector3d flooredPos){
        return getBlockRotation((int)flooredPos.x, (int)flooredPos.y, (int)flooredPos.z);
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
        return !(AABBGetLeft() > BlockBoxGetRight() ||
                AABBGetRight() < BlockBoxGetLeft() ||
                AABBGetBottom() > BlockBoxGetTop() ||
                AABBGetTop() < BlockBoxGetBottom() ||
                AABBGetFront() > BlockBoxGetBack() ||
                AABBGetBack() < BlockBoxGetFront());
    }
}