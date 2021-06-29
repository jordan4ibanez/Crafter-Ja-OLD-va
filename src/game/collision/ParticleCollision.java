package game.collision;

import org.joml.AABBd;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static engine.time.Time.getDelta;
import static game.chunk.Chunk.getBlock;
import static game.blocks.BlockDefinition.*;
import static game.chunk.Chunk.getBlockRotation;

public class ParticleCollision {

    private static double adjustedDelta;

    public static boolean applyParticleInertia(Vector3d pos, Vector3f inertia, boolean gravity, boolean applyCollision){

        double delta = getDelta();

        int loops = 1;

        if (delta >  0.001f){
            loops = (int)Math.floor(delta / 0.001f);
            adjustedDelta = (delta/(double)loops);
        } else {
            adjustedDelta = delta;
        }

        boolean onGround = false;

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
                inertia.x -= inertia.x * adjustedDelta * 10; // do (10 - 9.5f) for slippery!
                inertia.z -= inertia.z * adjustedDelta * 10;
            }
        }
        return onGround;
    }

    //normal collision

    private static boolean collisionDetect(Vector3d pos, Vector3f inertia){
        boolean onGround = false;

        final Vector3d oldPos = new Vector3d();

        oldPos.x = pos.x;
        oldPos.y = pos.y;
        oldPos.z = pos.z;

        pos.y += inertia.y * adjustedDelta;

        final Vector3i fPos = floorPos(pos);

        switch (inertiaToDir(inertia.y)) {
            case -1 -> {
                byte cachedBlock = getBlock(fPos);
                byte rot = getBlockRotation(fPos);
                if (cachedBlock > 0 && isBlockWalkable(cachedBlock)) {
                    onGround = collideYNegative(fPos.x, fPos.y, fPos.z, rot, pos, inertia, cachedBlock);
                }
            }
            case 1 -> {
                byte cachedBlock = getBlock(fPos);
                byte rot = getBlockRotation(fPos);
                if (cachedBlock > 0 && isBlockWalkable(cachedBlock)) {
                    collideYPositive(fPos.x, fPos.y, fPos.z, rot, pos, inertia, cachedBlock);
                }
            }
        }


        //todo: begin X collision detection
        pos.x += inertia.x * adjustedDelta;

        fPos.set(floorPos(pos));

        switch (inertiaToDir(inertia.x)) {
            case 1 -> {
                byte cachedBlock = getBlock(fPos);
                byte rot = getBlockRotation(fPos);
                if (cachedBlock > 0 && isBlockWalkable(cachedBlock)) {
                    collideXPositive(fPos.x, fPos.y, fPos.z, rot, pos, inertia, cachedBlock);
                }
            }
            case -1 -> {
                byte cachedBlock = getBlock(fPos);
                byte rot = getBlockRotation(fPos);
                if (cachedBlock > 0 && isBlockWalkable(cachedBlock)) {
                    collideXNegative(fPos.x, fPos.y, fPos.z, rot, pos, inertia, cachedBlock);
                }
            }
        }



        //todo: Begin Z collision detection
        pos.z += inertia.z * adjustedDelta;
        fPos.set(floorPos(pos));

        switch (inertiaToDir(inertia.z)) {
            case 1 -> {
                byte cachedBlock = getBlock(fPos);
                byte rot = getBlockRotation(fPos);
                if (cachedBlock > 0 && isBlockWalkable(cachedBlock)) {
                    collideZPositive(fPos.x, fPos.y, fPos.z, rot, pos, inertia, cachedBlock);
                }
            }
            case -1 -> {
                byte cachedBlock = getBlock(fPos);
                byte rot = getBlockRotation(fPos);
                if (cachedBlock > 0 && isBlockWalkable(cachedBlock)) {
                    collideZNegative(fPos.x, fPos.y, fPos.z, rot, pos, inertia, cachedBlock);
                }
            }
        }

        return onGround;
    }

    private static boolean collideYNegative(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, byte blockID){
        boolean onGround = false;
        for (float[] blockBox : getBlockShape(blockID, rot)) {
            final AABBd block  = new AABBd(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ,blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);
            if (block.containsPoint(pos)) {
                onGround = true;
                pos.y = block.maxY;
                inertia.y = 0;
            }
        }
        return onGround;
    }

    private static void collideYPositive(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, byte blockID){
        for (float[] blockBox : getBlockShape(blockID, rot)) {
            final AABBd block  = new AABBd(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ,blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);
            //head detection
            if (block.containsPoint(pos)) {
                pos.y = block.minY;
                inertia.y = 0;
            }
        }
    }

    private static void collideXPositive(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, byte blockID){
        for (float[] blockBox : getBlockShape(blockID, rot)) {
            final AABBd block  = new AABBd(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ,blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);
            if (block.containsPoint(pos)) {
                pos.x = block.minX;
                inertia.x = 0;
            }
        }
    }

    private static void collideXNegative(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, byte blockID){
        for (float[] blockBox : getBlockShape(blockID, rot)) {
            final AABBd block  = new AABBd(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ,blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);
            if (block.containsPoint(pos)) {
                pos.x = block.maxX;
                inertia.x = 0;
            }
        }
    }


    private static void collideZPositive(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, byte blockID){
        for (float[] blockBox : getBlockShape(blockID, rot)) {
            final AABBd block  = new AABBd(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ,blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);
            if (block.containsPoint(pos)) {
                pos.z = block.minZ;
                inertia.z = 0;
            }
        }
    }

    private static void collideZNegative(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, byte blockID){
        for (float[] blockBox : getBlockShape(blockID, rot)) {
            final AABBd block  = new AABBd(blockBox[0]+blockPosX, blockBox[1]+blockPosY, blockBox[2]+blockPosZ,blockBox[3]+blockPosX,blockBox[4]+blockPosY,blockBox[5]+blockPosZ);
            if (block.containsPoint(pos)) {
                pos.z = block.maxZ;
                inertia.z = 0;
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

    //simple type cast bolt on to JOML
    //converts floored Vector3d to Vector3i
    private static Vector3i floorPos(Vector3d input){
        return new Vector3i((int)Math.floor(input.x), (int)Math.floor(input.y),(int)Math.floor(input.z));
    }
}