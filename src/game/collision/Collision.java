package game.collision;

import org.joml.Vector3f;

import static game.chunk.Chunk.getBlock;
import static game.blocks.BlockDefinition.*;
import static game.chunk.Chunk.getBlockRotation;
import static game.collision.CollisionMath.floorPos;
import static game.collision.CustomAABB.*;
import static game.collision.CustomBlockBox.*;

public class Collision {
    final private static float gameSpeed = 0.001f;

    public static boolean applyInertia(Vector3f pos, Vector3f inertia, boolean onGround, float width, float height, boolean gravity, boolean sneaking, boolean applyCollision, boolean airFriction){

        if(gravity) {
            inertia.y -= 40f * gameSpeed; //gravity
        }

        //limit speed
        if (inertia.y <= -70f){
            inertia.y = -70f;
        } else if (inertia.y > 70f){
            inertia.y = 70f;
        }

        if (applyCollision) {
            if (sneaking) {
                Vector3f oldPos = new Vector3f(pos);

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
            pos.x += inertia.x * gameSpeed;
            pos.y += inertia.y * gameSpeed;
            pos.z += inertia.z * gameSpeed;
        }

        //apply friction
        if (onGround || airFriction) {
            inertia.x += -inertia.x * gameSpeed * 10; // do (10 - 9.5f) for slippery!
            inertia.z += -inertia.z * gameSpeed * 10;
        }

        return onGround;
    }

    //these are class/method caches!! NOT FIELDS!
    private static Vector3f fPos;
    private static boolean onGround;
    private static int x,y,z;
    private static int cachedBlock;
    private static Vector3f cachedPos = new Vector3f(0,0,0);



    //sneaking stuff

    private static int sneakCollisionDetect(Vector3f pos, Vector3f inertia, float width, float height){

        int binaryReturn = 0;

        boolean onGround = false;

        Vector3f clonedInertia = new Vector3f(inertia);
        Vector3f clonedPos = new Vector3f(pos);
        //todo: begin X collision detection
        clonedPos.y -= 0.05f;
        clonedPos.x += 0.1f * inertiaToDir(clonedInertia.x);

        fPos = floorPos(new Vector3f(clonedPos));

        for (x = -1; x <= 1; x++) {
            for (z = -1; z <= 1; z++) {

                Vector3f cachedPos = new Vector3f();
                cachedPos.x = fPos.x + x;
                cachedPos.y = fPos.y;
                cachedPos.z = fPos.z + z;

                cachedBlock = detectBlock(cachedPos);

                byte rot =  detectRot(cachedPos);

                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    onGround = sneakCollideYNegative((int) fPos.x + x, (int)fPos.y, (int) fPos.z + z, rot, clonedPos, clonedInertia, width, height, onGround, cachedBlock);
                }
            }
        }

        if (!onGround) {
            binaryReturn += 1;
        }

        //reset position vectors
        clonedPos = new Vector3f(pos);
        clonedInertia = new Vector3f(inertia);

        onGround = false;

        //todo: Begin Z collision detection
        clonedPos.y -= 0.05f;
        clonedPos.z += 0.1f * inertiaToDir(clonedInertia.z);

        fPos = floorPos(new Vector3f(clonedPos));

        for (x = -1; x <= 1; x++) {
            for (z = -1; z <= 1; z++) {

                Vector3f cachedPos = new Vector3f();
                cachedPos.x = fPos.x + x;
                cachedPos.y = fPos.y;
                cachedPos.z = fPos.z + z;

                cachedBlock = detectBlock(cachedPos);

                byte rot =  detectRot(cachedPos);

                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    onGround = sneakCollideYNegative((int) fPos.x + x, (int)fPos.y, (int) fPos.z + z, rot, clonedPos, clonedInertia, width, height, onGround, cachedBlock);
                }
            }
        }


        if (!onGround) {
            binaryReturn += 2;
        }


        return binaryReturn;
    }

    private static boolean sneakCollideYNegative(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3f pos, Vector3f inertia, float width, float height, boolean onGround, int blockID){
        for (float[] thisBlockBox : getBlockShape(blockID, rot)) {
            setAABB(pos.x, pos.y, pos.z, width, height);
            setBlockBox(blockPosX,blockPosY,blockPosz,thisBlockBox);

            if (isWithin()) {
                return true;
            }
        }
        return onGround;
    }





    //normal collision

    private static boolean collisionDetect(Vector3f pos, Vector3f inertia, float width, float height){
        onGround = false;

        Vector3f oldPos = new Vector3f();

        oldPos.x = pos.x;
        oldPos.y = pos.y;
        oldPos.z = pos.z;

        pos.y += inertia.y * gameSpeed;

        fPos = floorPos(new Vector3f(pos));

        int up = 0;

        //todo: begin Y collision detection
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
                                onGround = collideYNegative((int) fPos.x + x, y, (int) fPos.z + z, rot, pos, inertia, width, height, onGround, cachedBlock);
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
                                collideYPositive((int) fPos.x + x, y, (int) fPos.z + z, rot, pos, inertia, width, height, cachedBlock);
                            }
                        }
                    }
                    break;
            }
        }


        //todo: begin X collision detection
        pos.x += inertia.x * gameSpeed;

        fPos = floorPos(new Vector3f(pos));

        boolean doIt = true;

        int positive = 0;

        switch (inertiaToDir(inertia.x)){
            case -1:
                x = (int)Math.floor(pos.x - width);
                break;
            case 1:
                x = (int)Math.floor(pos.x + width);
                positive = 1;
                break;
            default:
                doIt = false;
                break;
        }

        if (doIt) {
            switch (positive){
                case 1:
                    for (float yy = 0; yy <= height; yy = yy + 0.5f) {
                        for (z = -1; z <= 1; z++) {
                            cachedPos.x = x;
                            cachedPos.y = yy + pos.y;
                            cachedPos.z = fPos.z + z;

                            cachedBlock = detectBlock(floorPos(cachedPos));

                            byte rot =  detectRot(cachedPos);

                            if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                                collideXPositive(x, (int)(yy + pos.y), (int) fPos.z + z, rot, pos, inertia, oldPos, width, height, cachedBlock);
                            }
                        }
                    }
                    break;
                case 0:
                    for (float yy = 0; yy <= height; yy = yy + 0.5f) {
                        for (z = -1; z <= 1; z++) {
                            cachedPos.x = x;
                            cachedPos.y = yy + pos.y;
                            cachedPos.z = fPos.z + z;

                            cachedBlock = detectBlock(floorPos(cachedPos));

                            byte rot =  detectRot(cachedPos);

                            if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                                collideXNegative(x, (int)(yy + pos.y), (int) fPos.z + z, rot, pos, inertia, oldPos, width, height, cachedBlock);
                            }
                        }
                    }
                    break;
            }
        }


        //todo: Begin Z collision detection

        pos.z += inertia.z * gameSpeed;

        fPos = floorPos(new Vector3f(pos));

        doIt = true;

        positive = 0;

        switch (inertiaToDir(inertia.z)){
            case -1:
                z = (int)Math.floor(pos.z - width);
                break;
            case 1:
                z = (int)Math.floor(pos.z + width);
                positive = 1;
                break;
            default:
                doIt = false;
                break;
        }

        if (doIt) {
            switch (positive){
                case 1:
                    for (float yy = 0; yy <= height; yy = yy + 0.5f) {
                        for (x = -1; x <= 1; x++) {
                            cachedPos.x = fPos.x + x;
                            cachedPos.y = yy + pos.y;
                            cachedPos.z = z;
                            cachedBlock = detectBlock(floorPos(cachedPos));

                            byte rot =  detectRot(cachedPos);

                            if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                                collideZPositive((int)fPos.x + x, (int)(yy + pos.y), z, rot, pos, inertia, oldPos, width, height, cachedBlock);
                            }
                        }
                    }
                    break;
                case 0:
                    for (float yy = 0; yy <= height; yy = yy + 0.5f) {
                        for (x = -1; x <= 1; x++) {
                            cachedPos.x = fPos.x + x;
                            cachedPos.y = yy + pos.y;
                            cachedPos.z = z;
                            cachedBlock = detectBlock(floorPos(cachedPos));

                            byte rot =  detectRot(cachedPos);

                            if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                                collideZNegative((int)fPos.x + x, (int)(yy + pos.y), z, rot, pos, inertia, oldPos, width, height, cachedBlock);
                            }
                        }
                    }
                    break;
            }
        }
        return onGround;
    }

    private static boolean collideYNegative(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3f pos, Vector3f inertia, float width, float height, boolean onGround, int blockID){
        for (float[] thisBlockBox : getBlockShape(blockID, rot)) {
            setAABB(pos.x, pos.y, pos.z, width, height);
            setBlockBox(blockPosX,blockPosY,blockPosz,thisBlockBox);

            if (isWithin() && BlockBoxGetTop() > AABBGetBottom() && AABBGetBottom() - BlockBoxGetTop() > -0.1f) {
                pos.y = BlockBoxGetTop() + 0.0001f;
                inertia.y = 0;
                onGround = true;
            }
        }
        return onGround;
    }

    private static void collideYPositive(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3f pos, Vector3f inertia, float width, float height, int blockID){
        for (float[] thisBlockBox : getBlockShape(blockID, rot)) {
            setAABB(pos.x, pos.y, pos.z, width, height);
            setBlockBox(blockPosX, blockPosY, blockPosz,thisBlockBox);
            //head detection
            if (isWithin() && BlockBoxGetBottom() < AABBGetTop() && AABBGetTop() - BlockBoxGetBottom() < 0.1f) {
                pos.y = BlockBoxGetBottom() - height - 0.001f;
                inertia.y = 0;
            }
        }
    }

    private static void collideXPositive(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3f pos, Vector3f inertia, Vector3f oldPos, float width, float height, int blockID){

        for (float[] thisBlockBox : getBlockShape(blockID, rot)) {
            setAABB(pos.x, pos.y, pos.z, width, height);
            setBlockBox(blockPosX, blockPosY, blockPosz, thisBlockBox);
            if (isWithin() && BlockBoxGetLeft() < AABBGetRight() && BlockBoxGetLeft() - AABBGetRight() > -0.1f) {
                if (isSteppable(blockID) && AABBGetBottom() - BlockBoxGetTop() < -0.48f) {
                    pos.y = BlockBoxGetTop() + 0.0001f;
                } else {
                    pos.x = BlockBoxGetLeft() - width - 0.001f;
                    inertia.x = 0;
                }
            }
        }

        //correction for the sides of stairs
        if (pos.y - oldPos.y > 0.51) {
            pos.y = oldPos.y;
            for (float[] thisBlockBox : getBlockShape(blockID, rot)) {
                setAABB(pos.x, pos.y, pos.z, width, height);
                setBlockBox(blockPosX, blockPosY, blockPosz, thisBlockBox);
                if (isWithin() && BlockBoxGetLeft() < AABBGetRight() && BlockBoxGetLeft() - AABBGetRight() > -0.1f) {
                    pos.x = BlockBoxGetLeft() - width - 0.001f;
                    inertia.x = 0;
                }
            }
        }
    }

    private static void collideXNegative(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3f pos, Vector3f inertia, Vector3f oldPos, float width, float height, int blockID){
        for (float[] thisBlockBox : getBlockShape(blockID, rot)) {
            setAABB(pos.x, pos.y, pos.z, width, height);
            setBlockBox(blockPosX, blockPosY, blockPosz, thisBlockBox);
            if (isWithin() && BlockBoxGetRight() > AABBGetLeft() && BlockBoxGetRight() - AABBGetLeft() < 0.1f) {
                if (isSteppable(blockID) && AABBGetBottom() - BlockBoxGetTop() < -0.48f) {
                    pos.y = BlockBoxGetTop() + 0.0001f;
                }else {
                    pos.x = BlockBoxGetRight() + width + 0.001f;
                    inertia.x = 0;
                }
            }
        }

        //correction for the sides of stairs
        if (pos.y - oldPos.y > 0.51) {
            pos.y = oldPos.y;
            for (float[] thisBlockBox : getBlockShape(blockID, rot)) {
                setAABB(pos.x, pos.y, pos.z, width, height);
                setBlockBox(blockPosX, blockPosY, blockPosz, thisBlockBox);
                if (isWithin() && BlockBoxGetRight() > AABBGetLeft() && BlockBoxGetRight() - AABBGetLeft() < 0.1f) {
                    pos.x = BlockBoxGetRight() + width + 0.001f;
                    inertia.x = 0;
                }
            }
        }
    }


    private static void collideZPositive(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3f pos, Vector3f inertia, Vector3f oldPos, float width, float height, int blockID){
        for (float[] thisBlockBox : getBlockShape(blockID, rot)) {
            setAABB(pos.x, pos.y, pos.z, width, height);
            setBlockBox(blockPosX, blockPosY, blockPosz, thisBlockBox);
            if (isWithin() && BlockBoxGetFront() < AABBGetBack() && BlockBoxGetFront() - AABBGetBack() > -0.1f) {
                if (isSteppable(blockID) && AABBGetBottom() - BlockBoxGetTop() < -0.48f) {
                    pos.y = BlockBoxGetTop() + 0.0001f;
                }else {
                    pos.z = BlockBoxGetFront() - width - 0.001f;
                    inertia.z = 0;
                }
            }
        }

        //correction for the sides of stairs
        if (pos.y - oldPos.y > 0.51) {
            pos.y = oldPos.y;
            for (float[] thisBlockBox : getBlockShape(blockID, rot)) {
                setAABB(pos.x, pos.y, pos.z, width, height);
                setBlockBox(blockPosX, blockPosY, blockPosz, thisBlockBox);
                if (isWithin() && BlockBoxGetFront() < AABBGetBack() && BlockBoxGetFront() - AABBGetBack() > -0.1f) {
                    pos.z = BlockBoxGetFront() - width - 0.001f;
                    inertia.z = 0;
                }
            }
        }
    }

    private static void collideZNegative(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3f pos, Vector3f inertia, Vector3f oldPos, float width, float height, int blockID){
        for (float[] thisBlockBox : getBlockShape(blockID, rot)) {
            setAABB(pos.x, pos.y, pos.z, width, height);
            setBlockBox(blockPosX, blockPosY, blockPosz, thisBlockBox);
            if (isWithin() && BlockBoxGetBack() > AABBGetFront() && BlockBoxGetBack() - AABBGetFront() < 0.1f) {
                if (isSteppable(blockID) && AABBGetBottom() - BlockBoxGetTop() < -0.48f) {
                    pos.y = BlockBoxGetTop() + 0.0001f;
                }else {
                    pos.z = BlockBoxGetBack() + width + 0.001f;
                    inertia.z = 0;
                }
            }
        }

        //correction for the sides of stairs
        if (pos.y - oldPos.y > 0.51) {
            pos.y = oldPos.y;
            for (float[] thisBlockBox : getBlockShape(blockID, rot)) {
                setAABB(pos.x, pos.y, pos.z, width, height);
                setBlockBox(blockPosX, blockPosY, blockPosz, thisBlockBox);
                if (isWithin() && BlockBoxGetBack() > AABBGetFront() && BlockBoxGetBack() - AABBGetFront() < 0.1f) {
                    pos.z = BlockBoxGetBack() + width + 0.001f;
                    inertia.z = 0;
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

    private static int detectBlock(Vector3f flooredPos){
        return getBlock((int)flooredPos.x, (int)flooredPos.y, (int)flooredPos.z);
    }

    private static byte detectRot(Vector3f flooredPos){
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