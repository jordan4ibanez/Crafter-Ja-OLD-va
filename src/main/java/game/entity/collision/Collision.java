package game.entity.collision;

import engine.time.Delta;
import game.blocks.BlockDefinitionContainer;
import game.chunk.Chunk;
import game.player.Player;
import org.joml.Math;
import org.joml.*;

final public class Collision {

    private final BlockDefinitionContainer blockDefinitionContainer = new BlockDefinitionContainer();
    private final CollisionObject collisionObject = new CollisionObject();

    private Delta delta;
    private Chunk chunk;
    private Player player;

    public void setDelta(Delta delta){
        if (this.delta == null){
            this.delta = delta;
        }
    }
    public void setChunk(Chunk chunk){
        if (this.chunk == null){
            this.chunk = chunk;
        }
    }
    public void setPlayer(Player player){
        if (this.player == null){
            this.player = player;
        }
    }

    public Collision(){
    }



    private float inWater = 0;
    private final Vector3d clonedPos = new Vector3d();
    private final Vector3i cachedPos = new Vector3i();
    private final Vector3d oldPos = new Vector3d();
    private final Vector3i fPos = new Vector3i();
    private final Vector3d oldPosSneaking = new Vector3d();
    private final Vector3f clonedInertia = new Vector3f();

    //this probably definitely absolutely should not take isPlayer as a value
    public boolean applyInertia(Vector3d pos, Vector3f inertia, boolean onGround, float width, float height, boolean gravity, boolean sneaking, boolean applyCollision, boolean airFriction, boolean isPlayer){
        double delta = this.delta.getDelta();

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

                if (sneaking && !player.isJumping()) {
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
                        player.setInWater(true);
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
                        player.setInWater(false);
                    }
                    //regular gravity
                    inertia.y -= 30f * adjustedDelta;
                }
            }
       }

        return onGroundLock;
    }

    //sneaking stuff
    private byte sneakCollisionDetect(Vector3d pos, Vector3f inertia, float width, float height){

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

                byte cachedBlock = chunk.getBlock(cachedPos);
                byte rot = chunk.getBlockRotation(cachedPos);

                if (!onGround && cachedBlock > 0 && blockDefinitionContainer.getWalkable(cachedBlock)) {
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

                byte cachedBlock = chunk.getBlock(cachedPos);
                byte rot = chunk.getBlockRotation(cachedPos);

                if (!onGround && cachedBlock > 0 && blockDefinitionContainer.getWalkable(cachedBlock)) {
                    onGround = sneakCollideYNegative(cachedPos.x, cachedPos.y, cachedPos.z, rot, width, height, cachedBlock);
                }
            }
        }

        if (!onGround) {
            binaryReturn += 2;
        }

        return binaryReturn;
    }

    private boolean sneakCollideYNegative(int blockPosX, int blockPosY, int blockPosZ, byte rot, float width, float height, byte blockID){
        for (float[] blockBox : blockDefinitionContainer.getShape(blockID, rot)) {
            
            collisionObject.setAABBEntity(clonedPos.x, clonedPos.y, clonedPos.z, width, height);

            collisionObject.setAABBBlock(blockBox, blockPosX, blockPosY, blockPosZ);

            if (collisionObject.intersectsAABB()) {
                return true;
            }
        }
        return false;
    }

    //normal collision
    private boolean collisionDetect(Vector3d pos, Vector3f inertia, float width, float height, double delta){

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

                        byte cachedBlock = chunk.getBlock(cachedPos);

                        byte rot = chunk.getBlockRotation(cachedPos);

                        if (cachedBlock > 0 && blockDefinitionContainer.getWalkable(cachedBlock)) {
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

                        byte cachedBlock = chunk.getBlock(cachedPos);

                        byte rot = chunk.getBlockRotation(cachedPos);

                        if (cachedBlock > 0 && blockDefinitionContainer.getWalkable(cachedBlock)) {
                            collideYPositive(cachedPos.x, cachedPos.y, cachedPos.z, rot, pos, inertia, width, height, cachedBlock);
                        }
                    }
                }
                break;
        }




        //begin X collision detection -- XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

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
                    byte cachedBlock = chunk.getBlock(cachedPos);

                    //get rotation
                    byte rot = chunk.getBlockRotation(cachedPos);

                    //never collide with air (block ID 0)
                    if (cachedBlock > 0 && blockDefinitionContainer.getWalkable(cachedBlock)) {
                        collideX(cachedPos.x, cachedPos.y, cachedPos.z, rot, pos, inertia, width, height, cachedBlock);
                    }
                }
            }
        }


        //Begin Z collision detection -- ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ

        pos.z += inertia.z * delta;

        floorPos(pos);

        //this must start at -1f (loops through every position
        for (byte y =-1; y <= height + 1; y++) {
            for (byte x = -1; x <= 1; x++) {
                for (byte z = -1; z <= 1; z++) {
                    //update to polling position
                    cachedPos.set(fPos.x + x,fPos.y + y,fPos.z + z);

                    //get block ID
                    byte cachedBlock = chunk.getBlock(cachedPos);

                    //get rotation
                    byte rot = chunk.getBlockRotation(cachedPos);

                    //never collide with air (block ID 0)
                    if (cachedBlock > 0 && blockDefinitionContainer.getWalkable(cachedBlock)) {
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
                    byte cachedBlock = chunk.getBlock(cachedPos);

                    //get rotation
                    byte rot = chunk.getBlockRotation(cachedPos);

                    //never collide with air (block ID 0)
                    if (cachedBlock > 0 && blockDefinitionContainer.getIfLiquid(cachedBlock)){
                        detectIfInWater(cachedPos.x, cachedPos.y, cachedPos.z, rot, pos, width, height, cachedBlock);
                    }
                }
            }
        }

        return onGround;
    }

    //a simple way to check if an object is in the water, only done on x and z passes so you can't stand
    //next to water and get slowed down
    private void detectIfInWater(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, float width, float height, byte blockID){
        for (float[] blockBox : blockDefinitionContainer.getShape(blockID, rot)) {

            collisionObject.setAABBEntity(pos.x, pos.y, pos.z, width, height);

            collisionObject.setAABBBlock(blockBox, blockPosX, blockPosY, blockPosZ);
            
            if (collisionObject.intersectsAABB()) {
                float localViscosity = blockDefinitionContainer.getViscosity(blockID);
                if (localViscosity > inWater){
                    inWater = localViscosity;
                }
            }
        }
    }

    private boolean collideYNegative(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, float width, float height, boolean onGround, byte blockID){
        for (float[] blockBox : blockDefinitionContainer.getShape(blockID, rot)) {

            collisionObject.setAABBEntity(pos.x, pos.y, pos.z, width, height);

            collisionObject.setAABBBlock(blockBox, blockPosX, blockPosY, blockPosZ);
            
            if (collisionObject.intersectsAABB()) {
                pos.y = blockBox[4] + (double)blockPosY + 0.0000000001d;
                inertia.y = 0;
                onGround = true;
            }

        }
        return onGround;
    }

    private void collideYPositive(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, float width, float height, byte blockID){
        for (float[] blockBox : blockDefinitionContainer.getShape(blockID, rot)) {

            collisionObject.setAABBEntity(pos.x, pos.y, pos.z, width, height);

            collisionObject.setAABBBlock(blockBox, blockPosX, blockPosY, blockPosZ);

            if (collisionObject.intersectsAABB()) {
                pos.y = blockBox[1] + (double)blockPosY - height - 0.0000000001d;
                inertia.y = 0;
            }
        }
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

    private void collideX(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, float width, float height, byte blockID){
                
        
        //run through X collisions
        for (float[] blockBox : blockDefinitionContainer.getShape(blockID, rot)) {

            collisionObject.setAABBBlock(blockBox, blockPosX, blockPosY, blockPosZ);
            
            double blockXCenter = ((blockBox[0] + blockBox[3])/2d);
            
            //collide X negative
            if (blockPosX + blockXCenter <= pos.x) {

                collisionObject.setAABBEntity(pos.x, pos.y, pos.z, width, height);
                
                if (collisionObject.intersectsAABB()) {
                    if (blockDefinitionContainer.getSteppable(blockID) && inertia.y == 0) {
                        pos.y = blockBox[4] + (double)blockPosY;
                    } else {
                        pos.x = blockBox[3] + (double)blockPosX + width + 0.0000000001d;
                        inertia.x= 0f;
                    }
                }
            }
            
            //collide X positive
            if (blockPosX + blockXCenter >= pos.x) {

                collisionObject.setAABBEntity(pos.x, pos.y, pos.z, width, height);
                
                if (collisionObject.intersectsAABB()) {
                    if (blockDefinitionContainer.getSteppable(blockID) && inertia.y == 0) {
                        pos.y = blockBox[4] + (double)blockPosY;
                    } else {
                        pos.x = blockBox[0] + (double)blockPosX - width - 0.0000000001d;
                        inertia.x= 0f;
                    }
                }
            }
        }

        //correction for the sides of stairs
        if (blockDefinitionContainer.getSteppable(blockID) && pos.y - oldPos.y > 0.51d) {

            pos.y = oldPos.y;

            for (float[] blockBox : blockDefinitionContainer.getShape(blockID, rot)) {

                collisionObject.setAABBBlock(blockBox, blockPosX, blockPosY, blockPosZ);
                
                double blockXCenter = ((blockBox[0] + blockBox[3])/2d);

                //collide X negative
                if (blockPosX + blockXCenter <= pos.x) {

                    collisionObject.setAABBEntity(pos.x, pos.y, pos.z, width, height);
                    
                    if (collisionObject.intersectsAABB()) {
                        pos.x = blockBox[3] + (double)blockPosX + width + 0.0000000001d;
                        inertia.x = 0f;
                    }
                }

                //collide X positive
                if (blockPosX + blockXCenter >= pos.x) {

                    collisionObject.setAABBEntity(pos.x, pos.y, pos.z, width, height);
                    
                    if (collisionObject.intersectsAABB()) {
                        pos.x = blockBox[0] + (double)blockPosX - width - 0.0000000001d;
                        inertia.x = 0f;
                    }
                }
            }
        }
    }


    //ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ
    
    private void collideZ(int blockPosX, int blockPosY, int blockPosZ, byte rot, Vector3d pos, Vector3f inertia, float width, float height, byte blockID){

        //run through Z collisions
        for (float[] blockBox : blockDefinitionContainer.getShape(blockID, rot)) {

            collisionObject.setAABBBlock(blockBox, blockPosX, blockPosY, blockPosZ);

            double blockZCenter = ((blockBox[2] + blockBox[5])/2d);

            //collide Z negative
            if (blockPosZ + blockZCenter <= pos.z) {

                collisionObject.setAABBEntity(pos.x, pos.y, pos.z, width, height);
                
                if (collisionObject.intersectsAABB()) {
                    if (blockDefinitionContainer.getSteppable(blockID) && inertia.y == 0) {
                        pos.y = blockBox[4] + (double)blockPosY;
                    }else {
                        pos.z = blockBox[5] + (double)blockPosZ + width + 0.0000000001d;
                        inertia.z= 0f;
                    }
                }
            }

            //collide Z positive
            if (blockPosZ + blockZCenter >= pos.z) {

                collisionObject.setAABBEntity(pos.x, pos.y, pos.z, width, height);
                
                if (collisionObject.intersectsAABB()) {
                    if (blockDefinitionContainer.getSteppable(blockID) && inertia.y == 0) {
                        pos.y = blockBox[4] + (double)blockPosY;
                    } else {
                        pos.z = blockBox[2] + (double)blockPosZ - width - 0.0000000001d;
                        inertia.z= 0f;
                    }
                }
            }
        }

        //correction for the sides of stairs
        if (pos.y - oldPos.y > 0.51d) {

            pos.y = oldPos.y;


            for (float[] blockBox : blockDefinitionContainer.getShape(blockID, rot)) {

                collisionObject.setAABBBlock(blockBox, blockPosX, blockPosY, blockPosZ);
                
                double blockZCenter = ((blockBox[2] + blockBox[5])/2d);

                //collide Z negative
                if (blockPosZ + blockZCenter <= pos.z) {

                    collisionObject.setAABBEntity(pos.x, pos.y, pos.z, width, height);
                    
                    if (collisionObject.intersectsAABB()) {
                        pos.z = blockBox[5] + (double)blockPosZ + width + 0.0000000001d;
                        inertia.z = 0f;
                    }
                }

                //collide Z positive
                if (blockPosZ + blockZCenter >= pos.z) {

                    collisionObject.setAABBEntity(pos.x, pos.y, pos.z, width, height);
                    
                    if (collisionObject.intersectsAABB()) {
                        pos.z = blockBox[2] + (double)blockPosZ - width - 0.0000000001d;
                        inertia.z = 0f;
                    }
                }
            }
        }
    }



    private int inertiaToDir(float thisInertia){
        if (thisInertia > 0.0001f){
            return 1;
        } else if (thisInertia < -0.0001f){
            return -1;
        }

        return 0;
    }

    //precise collision prediction when placing
    public boolean wouldCollidePlacing(Vector3d pos, float width, float height, int blockPosX, int blockPosY, int blockPosZ, byte blockID, byte rotation){

        collisionObject.setAABBEntity(pos.x, pos.y, pos.z, width, height);
        
        for (float[] blockBox : blockDefinitionContainer.getShape(blockID, rotation)) {
            collisionObject.setAABBBlock(blockBox, blockPosX, blockPosY, blockPosZ);
            
            if (collisionObject.intersectsAABB()) {
                return true;
            }
        }

        return false;
    }

    //simple type cast bolt on to JOML
    //converts floored Vector3d to Vector3i
    //this needs to be like this because input.floor() will break whatever position is being put into it
    private void floorPos(Vector3d input){
        fPos.set((int) Math.floor(input.x), (int)Math.floor(input.y),(int)Math.floor(input.z));
    }
}