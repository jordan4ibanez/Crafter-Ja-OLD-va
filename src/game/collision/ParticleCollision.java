package game.collision;

import org.joml.Vector3f;

import static game.chunk.Chunk.getBlock;
import static game.blocks.BlockDefinition.*;
import static game.chunk.Chunk.getBlockRotation;
import static game.collision.CollisionMath.floorPos;
import static game.collision.CustomAABB.*;
import static game.collision.CustomBlockBox.*;

public class ParticleCollision {
    final private static float gameSpeed = 0.001f;

    public static boolean applyParticleInertia(Vector3f pos, Vector3f inertia, boolean onGround, boolean gravity, boolean applyCollision){

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
            onGround = collisionDetect(pos, inertia);
        } else {
            pos.x += inertia.x * gameSpeed;
            pos.y += inertia.y * gameSpeed;
            pos.z += inertia.z * gameSpeed;
        }

        //apply friction
        if (onGround) {
            inertia.x += -inertia.x * gameSpeed * 10; // do (10 - 9.5f) for slippery!
            inertia.z += -inertia.z * gameSpeed * 10;
        }

        return onGround;
    }

    //these are class/method caches!! NOT FIELDS!
    private static Vector3f fPos;
    private static boolean onGround;
    private static int cachedBlock;
    private static Vector3f cachedPos = new Vector3f(0,0,0);


    //normal collision

    private static boolean collisionDetect(Vector3f pos, Vector3f inertia){
        onGround = false;

        Vector3f oldPos = new Vector3f();

        oldPos.x = pos.x;
        oldPos.y = pos.y;
        oldPos.z = pos.z;

        pos.y += inertia.y * gameSpeed;

        fPos = floorPos(new Vector3f(pos));

        int up = inertiaToDir(inertia.y);
        byte rot;

        switch (up){
            case -1:
                cachedBlock = detectBlock(fPos);
                rot =  detectRot(cachedPos);
                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    onGround = collideYNegative((int)fPos.x, (int)fPos.y, (int)fPos.z, rot, pos, inertia, onGround, cachedBlock);
                }
                break;
            case 1:
                cachedBlock = detectBlock(fPos);
                rot =  detectRot(cachedPos);
                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    collideYPositive((int) fPos.x, (int)fPos.y, (int) fPos.z, rot, pos, inertia, cachedBlock);
                }
                break;
        }


        //todo: begin X collision detection
        pos.x += inertia.x * gameSpeed;

        fPos = floorPos(new Vector3f(pos));


        int positive = inertiaToDir(inertia.x);

        switch (positive){
            case 1:
                cachedBlock = detectBlock(floorPos(fPos));
                rot =  detectRot(cachedPos);
                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    collideXPositive((int) pos.x, (int) fPos.y, (int) fPos.z, rot, pos, inertia, oldPos, cachedBlock);
                }
                break;
            case -1:
                cachedBlock = detectBlock(floorPos(fPos));
                rot =  detectRot(cachedPos);
                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    collideXNegative((int)fPos.x, (int)fPos.y, (int) fPos.z, rot, pos, inertia, oldPos, cachedBlock);
                }
                break;
        }



        //todo: Begin Z collision detection
        pos.z += inertia.z * gameSpeed;
        fPos = floorPos(new Vector3f(pos));

        positive = inertiaToDir(inertia.z);

        switch (positive){
            case 1:
                cachedBlock = detectBlock(floorPos(fPos));
                rot =  detectRot(cachedPos);
                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    collideZPositive((int)fPos.x , (int)fPos.y, (int)fPos.z, rot, pos, inertia, oldPos, cachedBlock);
                }
                break;
            case -1:
                cachedBlock = detectBlock(floorPos(fPos));
                rot =  detectRot(cachedPos);
                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    collideZNegative((int)fPos.x , (int)fPos.y, (int)fPos.z, rot, pos, inertia, oldPos, cachedBlock);
                }
                break;
        }

        return onGround;
    }

    private static boolean collideYNegative(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3f pos, Vector3f inertia, boolean onGround, int blockID){
        for (float[] thisBlockBox : getBlockShape(blockID, rot)) {
            setBlockBox(blockPosX,blockPosY,blockPosz,thisBlockBox);
            if (isWithin(pos)) {
                pos.y = BlockBoxGetTop() + 0.0001f;
                inertia.y = 0;
                onGround = true;
            }
        }
        return onGround;
    }

    private static void collideYPositive(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3f pos, Vector3f inertia, int blockID){
        for (float[] thisBlockBox : getBlockShape(blockID, rot)) {
            setBlockBox(blockPosX, blockPosY, blockPosz,thisBlockBox);
            //head detection
            if (isWithin(pos)) {
                pos.y = BlockBoxGetBottom() - 0.001f;
                inertia.y = 0;
            }
        }
    }

    private static void collideXPositive(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3f pos, Vector3f inertia, Vector3f oldPos, int blockID){
        for (float[] thisBlockBox : getBlockShape(blockID, rot)) {
            setBlockBox(blockPosX, blockPosY, blockPosz, thisBlockBox);
            if (isWithin(pos)) {
                pos.x = BlockBoxGetLeft() - 0.001f;
                inertia.x = 0;
            }
        }
    }

    private static void collideXNegative(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3f pos, Vector3f inertia, Vector3f oldPos, int blockID){
        for (float[] thisBlockBox : getBlockShape(blockID, rot)) {
            setBlockBox(blockPosX, blockPosY, blockPosz, thisBlockBox);
            if (isWithin(pos)) {
                pos.x = BlockBoxGetRight() + 0.001f;
                inertia.x = 0;
            }
        }
    }


    private static void collideZPositive(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3f pos, Vector3f inertia, Vector3f oldPos, int blockID){
        for (float[] thisBlockBox : getBlockShape(blockID, rot)) {
            setBlockBox(blockPosX, blockPosY, blockPosz, thisBlockBox);
            if (isWithin(pos)) {
                pos.z = BlockBoxGetFront() - 0.001f;
                inertia.z = 0;
            }
        }
    }

    private static void collideZNegative(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3f pos, Vector3f inertia, Vector3f oldPos, int blockID){
        for (float[] thisBlockBox : getBlockShape(blockID, rot)) {
            setBlockBox(blockPosX, blockPosY, blockPosz, thisBlockBox);
            if (isWithin(pos)) {
                pos.z = BlockBoxGetBack() + 0.001f;
                inertia.z = 0;
            }
        }
    }


    private static boolean isWithin(Vector3f pos){
        return !(pos.x > BlockBoxGetRight() ||
                pos.x < BlockBoxGetLeft() ||
                pos.y > BlockBoxGetTop() ||
                pos.y < BlockBoxGetBottom() ||
                pos.z > BlockBoxGetBack() ||
                pos.z < BlockBoxGetFront());
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
}