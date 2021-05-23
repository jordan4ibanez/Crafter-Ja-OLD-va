package game.collision;

import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.Time.getDelta;
import static game.chunk.Chunk.getBlock;
import static game.blocks.BlockDefinition.*;
import static game.chunk.Chunk.getBlockRotation;
import static game.collision.CollisionMath.floorPos;
import static game.collision.CustomBlockBox.*;

public class ParticleCollision {

    private static float adjustedDelta;

    public static boolean applyParticleInertia(Vector3d pos, Vector3f inertia, boolean onGround, boolean gravity, boolean applyCollision){

        float delta = getDelta();

        int loops = 1;

        if (delta >  0.001f){
            loops = (int)Math.floor(delta / 0.001f);
            adjustedDelta = (delta/(float)loops);
        } else {
            adjustedDelta = delta;
        }


        for (int i = 0; i < loops; i++) {

            if(gravity) {
                inertia.y -= 30f * adjustedDelta; //gravity
            }

            //limit speed on x axis
            if (inertia.x <= -30f) {
                inertia.x = -30f;
            } else if (inertia.x > 30f) {
                inertia.x = 30f;
            }

            //limit speed on y axis
            if (inertia.y <= -50f) {
                inertia.y = -50f;
            } else if (inertia.y > 30f) {
                inertia.y = 30f;
            }

            //limit speed on z axis
            if (inertia.z <= -30f) {
                inertia.z = -30f;
            } else if (inertia.z > 30f) {
                inertia.z = 30f;
            }

            if (applyCollision) {
                onGround = collisionDetect(pos, inertia);
            } else {
                pos.x += inertia.x * adjustedDelta;
                pos.y += inertia.y * adjustedDelta;
                pos.z += inertia.z * adjustedDelta;
            }

            //apply friction
            if (onGround) {
                inertia.x += -inertia.x * adjustedDelta * 10; // do (10 - 9.5f) for slippery!
                inertia.z += -inertia.z * adjustedDelta * 10;
            }
        }

        return onGround;
    }

    private static final Vector3f cachedPos = new Vector3f(0,0,0);


    //normal collision

    private static boolean collisionDetect(Vector3d pos, Vector3f inertia){
        boolean onGround = false;

        Vector3d oldPos = new Vector3d();

        oldPos.x = pos.x;
        oldPos.y = pos.y;
        oldPos.z = pos.z;

        pos.y += inertia.y * adjustedDelta;

        //these are class/method caches!! NOT FIELDS!
        Vector3d fPos = floorPos(new Vector3d(pos));

        int up = inertiaToDir(inertia.y);
        byte rot;

        int cachedBlock;
        switch (up) {
            case -1 -> {
                cachedBlock = detectBlock(fPos);
                rot = detectRot();
                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    onGround = collideYNegative((int) fPos.x, (int) fPos.y, (int) fPos.z, rot, pos, inertia, false, cachedBlock);
                }
            }
            case 1 -> {
                cachedBlock = detectBlock(fPos);
                rot = detectRot();
                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    collideYPositive((int) fPos.x, (int) fPos.y, (int) fPos.z, rot, pos, inertia, cachedBlock);
                }
            }
        }


        //todo: begin X collision detection
        pos.x += inertia.x * adjustedDelta;

        fPos = floorPos(new Vector3d(pos));


        int positive = inertiaToDir(inertia.x);

        switch (positive) {
            case 1 -> {
                cachedBlock = detectBlock(floorPos(fPos));
                rot = detectRot();
                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    collideXPositive((int) pos.x, (int) fPos.y, (int) fPos.z, rot, pos, inertia, cachedBlock);
                }
            }
            case -1 -> {
                cachedBlock = detectBlock(floorPos(fPos));
                rot = detectRot();
                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    collideXNegative((int) fPos.x, (int) fPos.y, (int) fPos.z, rot, pos, inertia, cachedBlock);
                }
            }
        }



        //todo: Begin Z collision detection
        pos.z += inertia.z * adjustedDelta;
        fPos = floorPos(new Vector3d(pos));

        positive = inertiaToDir(inertia.z);

        switch (positive) {
            case 1 -> {
                cachedBlock = detectBlock(floorPos(fPos));
                rot = detectRot();
                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    collideZPositive((int) fPos.x, (int) fPos.y, (int) fPos.z, rot, pos, inertia, cachedBlock);
                }
            }
            case -1 -> {
                cachedBlock = detectBlock(floorPos(fPos));
                rot = detectRot();
                if (cachedBlock > 0 && isWalkable(cachedBlock)) {
                    collideZNegative((int) fPos.x, (int) fPos.y, (int) fPos.z, rot, pos, inertia, cachedBlock);
                }
            }
        }

        return onGround;
    }

    private static boolean collideYNegative(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3d pos, Vector3f inertia, boolean onGround, int blockID){
        for (double[] thisBlockBox : getBlockShape(blockID, rot)) {
            setBlockBox(blockPosX,blockPosY,blockPosz,thisBlockBox);
            if (isWithin(pos)) {
                pos.y = BlockBoxGetTop() + 0.0001d;
                inertia.y = 0;
                onGround = true;
            }
        }
        return onGround;
    }

    private static void collideYPositive(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3d pos, Vector3f inertia, int blockID){
        for (double[] thisBlockBox : getBlockShape(blockID, rot)) {
            setBlockBox(blockPosX, blockPosY, blockPosz,thisBlockBox);
            //head detection
            if (isWithin(pos)) {
                pos.y = BlockBoxGetBottom() - 0.001d;
                inertia.y = 0;
            }
        }
    }

    private static void collideXPositive(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3d pos, Vector3f inertia, int blockID){
        for (double[] thisBlockBox : getBlockShape(blockID, rot)) {
            setBlockBox(blockPosX, blockPosY, blockPosz, thisBlockBox);
            if (isWithin(pos)) {
                pos.x = BlockBoxGetLeft() - 0.001d;
                inertia.x = 0;
            }
        }
    }

    private static void collideXNegative(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3d pos, Vector3f inertia, int blockID){
        for (double[] thisBlockBox : getBlockShape(blockID, rot)) {
            setBlockBox(blockPosX, blockPosY, blockPosz, thisBlockBox);
            if (isWithin(pos)) {
                pos.x = BlockBoxGetRight() + 0.001d;
                inertia.x = 0;
            }
        }
    }


    private static void collideZPositive(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3d pos, Vector3f inertia, int blockID){
        for (double[] thisBlockBox : getBlockShape(blockID, rot)) {
            setBlockBox(blockPosX, blockPosY, blockPosz, thisBlockBox);
            if (isWithin(pos)) {
                pos.z = BlockBoxGetFront() - 0.001d;
                inertia.z = 0;
            }
        }
    }

    private static void collideZNegative(int blockPosX, int blockPosY, int blockPosz, byte rot, Vector3d pos, Vector3f inertia, int blockID){
        for (double[] thisBlockBox : getBlockShape(blockID, rot)) {
            setBlockBox(blockPosX, blockPosY, blockPosz, thisBlockBox);
            if (isWithin(pos)) {
                pos.z = BlockBoxGetBack() + 0.001d;
                inertia.z = 0;
            }
        }
    }


    private static boolean isWithin(Vector3d pos){
        return !(pos.x > BlockBoxGetRight() ||
                pos.x < BlockBoxGetLeft() ||
                pos.y > BlockBoxGetTop() ||
                pos.y < BlockBoxGetBottom() ||
                pos.z > BlockBoxGetBack() ||
                pos.z < BlockBoxGetFront());
    }

    private static int detectBlock(Vector3d flooredPos){
        return getBlock((int)flooredPos.x, (int)flooredPos.y, (int)flooredPos.z);
    }

    private static byte detectRot(){
        return getBlockRotation((int) ParticleCollision.cachedPos.x, (int) ParticleCollision.cachedPos.y, (int) ParticleCollision.cachedPos.z);
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