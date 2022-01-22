package game.player;

import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class Player {

    //this is a final Vector3i which indicates that the player is not pointing at anything
    private final Vector3i notPointingAtAnything = new Vector3i(0,-555,0);

    private float runningFOVAdder = 0f;
    private int health = 20;
    private final float collectionHeight        = 0.7f;
    private final float eyeHeight               = 1.5f;
    private final Vector3d pos = new Vector3d(0,0,0);
    private final Vector3d posWithEyeHeight = new Vector3d().set(pos.x,pos.y + eyeHeight,pos.z);
    private final Vector3d posWithCollectionHeight = new Vector3d(pos.x, pos.y + collectionHeight, pos.z);
    private final Vector3d posWithEyeHeightViewBobbing = new Vector3d().set(posWithEyeHeight.x, posWithEyeHeight.y, posWithEyeHeight.z);
    private final Vector3i newFlooredPos = new Vector3i();
    private final Vector3f inertia              = new Vector3f(0,0,0);
    private final float height                  = 1.9f;
    private final float width                   = 0.3f;
    private boolean onGround              =  false;
    private boolean wasOnGround           = false;
    private boolean mining                = false;
    private boolean placing               = false;
    private float placeTimer              = 0;
    private final Vector2f animationInertiaWorker = new Vector2f();
    private final float accelerationMultiplier  = 0.07f;
    private String name                   = "";
    private int currentInventorySelection = 0;
    private int oldInventorySelection = 0;
    private final Vector3i oldWorldSelectionPos = new Vector3i();
    private final Vector3i worldSelectionPos    = new Vector3i();
    private float sneakOffset = 0;
    private boolean playerIsJumping = false;
    private final Vector3d particlePos = new Vector3d(worldSelectionPos);
    private final Vector3f particleInertia = new Vector3f();
    //this is like this because working with x and z is easier than x and y
    private final Vector3i currentChunk = new Vector3i((int)Math.floor(pos.x / 16f),0,(int)Math.floor(pos.z / 16f));
    public int oldY = 0;

    private final float reach = 3.575f;

    private boolean sneaking              = false;
    private boolean running               = false;

    private float lightCheckTimer = 0f;
    private byte lightLevel = 0;
    private final Vector3i oldFlooredPos = new Vector3i(0,0,0);
    private final Vector3d oldRealPos = new Vector3d(0,0,0);

    private float hurtCameraRotation = 0;
    private boolean doHurtRotation = false;
    private boolean hurtRotationUp = true;

    //block hardness cache
    private float stoneHardness = 0f;
    private float dirtHardness = 0f;
    private float woodHardness = 0f;
    private float leafHardness = 0f;

    //tool mining level cache
    private float stoneMiningLevel = 0.3f;
    private float dirtMiningLevel = 1f;
    private float woodMiningLevel = 1f;
    private float leafMiningLevel = 1f;


    public Vector3d getOldRealPos(){
        return oldRealPos;
    }


    public String getName(){
        return name;
    }

    public void setPlayerName(String newName){
        name = newName;
    }

    public void updatePlayerMiningLevelCache(float newStoneMiningLevel, float newDirtMiningLevel, float newWoodMiningLevel, float newLeafMiningLevel){
        //System.out.println("New levels: " + newStoneMiningLevel + " " + newDirtMiningLevel + " " + newWoodMiningLevel + " " + newLeafMiningLevel);
        stoneMiningLevel = newStoneMiningLevel;
        dirtMiningLevel = newDirtMiningLevel;
        woodMiningLevel = newWoodMiningLevel;
        leafMiningLevel = newLeafMiningLevel;
    }

    public void addPlayerInertia(float x, float y, float z){
        inertia.add(x,y,z);
    }



    public float getHurtCameraRotation(){
        return hurtCameraRotation;
    }

    public void resetPlayerInputs(){
        setPlayerForward(false);
        setPlayerBackward(false);
        setPlayerLeft(false);
        setPlayerRight(false);
        setPlayerSneaking(false);
        setPlayerJump(false);
        mining = false;
        placing = false;
    }


    public boolean getIfPlayerIsJumping(){
        return(playerIsJumping);
    }

    //todo make this 2D
    public Vector3i getPlayerCurrentChunk(){
        return currentChunk;
    }

    public int getPlayerCurrentChunkX(){
        return currentChunk.x;
    }

    public int getPlayerCurrentChunkZ(){
        return currentChunk.z;
    }



    public float getSneakOffset(){
        return sneakOffset / 900f;
    }

    public void setPlayerWorldSelectionPos(Vector3i thePos){

        oldWorldSelectionPos.set(worldSelectionPos.x,worldSelectionPos.y,worldSelectionPos.z);

        //pointing at a block
        if (thePos != null){
            byte block = getBlock(thePos.x, thePos.y,thePos.z);
            //add in block hardness levels
            if (block > 0){
                stoneHardness = getStoneHardness(block);
                dirtHardness = getDirtHardness(block);
                woodHardness = getWoodHardness(block);
                leafHardness = getLeafHardness(block);
            }
            //reset when not pointing at any block
            else {
                stoneHardness = -1;
                dirtHardness = -1;
                woodHardness = -1;
                leafHardness = -1;
            }

            worldSelectionPos.set(thePos.x, thePos.y,thePos.z);
        } else {
            worldSelectionPos.set(notPointingAtAnything.x, notPointingAtAnything.y, notPointingAtAnything.z);
        }
    }


    public byte getPlayerLightLevel(){
        return lightLevel;
    }

    public void setPlayerWorldSelectionPos(int x, int y, int z){
        oldWorldSelectionPos.set(worldSelectionPos.x,worldSelectionPos.y,worldSelectionPos.z);
        worldSelectionPos.set(x,y,z);

        //pointing at a block
        byte block = getBlock(x, y, z);
        //add in block hardness levels
        if (block > 0){
            stoneHardness = getStoneHardness(block);
            dirtHardness = getDirtHardness(block);
            woodHardness = getWoodHardness(block);
            leafHardness = getLeafHardness(block);
        }
        //reset when not pointing at any block
        else {
            stoneHardness = -1;
            dirtHardness = -1;
            woodHardness = -1;
            leafHardness = -1;
        }
    }

    //mutable - be careful with this
    public Vector3i getPlayerWorldSelectionPos(){
        return worldSelectionPos;
    }
    //immutable
    public int getPlayerWorldSelectionPosX(){
        return worldSelectionPos.x;
    }
    //immutable
    public int getPlayerWorldSelectionPosY(){
        return worldSelectionPos.y;
    }
    //immutable
    public int getPlayerWorldSelectionPosZ(){
        return worldSelectionPos.z;
    }


    public String getPlayerName(){
        return name;
    }

    public int getPlayerInventorySelection(){
        return currentInventorySelection;
    }

    public void setPlayerMining( boolean isMining){
        mining = isMining;
    }

    public boolean getPlayerMining(){
        return mining;
    }


    public void setPlayerPlacing( boolean isPlacing) {
        boolean wasPlacing = placing;
        placing = isPlacing;

        //allow players to spam click to place
        if (!wasPlacing && isPlacing){
            placeTimer = 0.f;
        }
    }

    public boolean isCameraSubmerged(){
        return cameraSubmerged;
    }

    private boolean cameraSubmerged = false;


    public float getPlayerHeight(){
        return height;
    }
    public float getPlayerWidth(){
        return width;
    }

    public boolean getPlayerPlacing() {
        return placing;
    }

    //this is mutable, be careful with this
    public Vector3d getPlayerPos() {
        return pos;
    }
    //immutable
    public double getPlayerPosX(){
        return pos.x;
    }
    //immutable
    public double getPlayerPosY(){
        return pos.y;
    }
    //immutable
    public double getPlayerPosZ(){
        return pos.z;
    }

    //this is mutable, be careful with this
    public Vector3d getPlayerPosWithEyeHeight(){
        return posWithEyeHeight;
    }
    //immutable
    public double getPlayerPosWithEyeHeightX(){
        return posWithEyeHeight.x;
    }
    //immutable
    public double getPlayerPosWithEyeHeightY(){
        return posWithEyeHeight.y;
    }
    //immutable
    public double getPlayerPosWithEyeHeightZ(){
        return posWithEyeHeight.z;
    }

    //this is mutable, be careful with this
    public Vector3d getPlayerPosWithViewBobbing(){
        return posWithEyeHeightViewBobbing;
    }
    //immutable
    public double getPlayerPosWithViewBobbingX(){
        return posWithEyeHeightViewBobbing.x;
    }
    //immutable
    public double getPlayerPosWithViewBobbingY(){
        return posWithEyeHeightViewBobbing.y;
    }
    //immutable
    public double getPlayerPosWithViewBobbingZ(){
        return posWithEyeHeightViewBobbing.z;
    }

    //this is mutable, be careful with this
    public Vector3d getPlayerPosWithCollectionHeight(){
        return posWithCollectionHeight;
    }
    //immutable
    public double getPlayerPosWithCollectionHeightX(){
        return posWithCollectionHeight.x;
    }
    //immutable
    public double getPlayerPosWithCollectionHeightY(){
        return posWithCollectionHeight.y;
    }
    //immutable
    public double getPlayerPosWithCollectionHeightZ(){
        return posWithCollectionHeight.z;
    }


    private void applyCameraViewBobbingOffset(){
        posWithEyeHeightViewBobbing.set(posWithEyeHeight.x, posWithEyeHeight.y,posWithEyeHeight.z);

        if (getCameraPerspective() == 0) {
            float cameraRotationY = getCameraRotationY();

            float viewBobbingZ = getPlayerViewBobbingZ();
            if (viewBobbingZ != 0) {
                //direct object modification
                posWithEyeHeightViewBobbing.x += (float) Math.sin(Math.toRadians(cameraRotationY)) * -1.0f * viewBobbingZ;
                posWithEyeHeightViewBobbing.z += (float) Math.cos(Math.toRadians(cameraRotationY)) * viewBobbingZ;
            }

            float viewBobbingX = getPlayerViewBobbingX();
            if (viewBobbingX != 0) {
                posWithEyeHeightViewBobbing.x += (float) Math.sin(Math.toRadians(cameraRotationY - 90f)) * -1.0f * viewBobbingX;
                posWithEyeHeightViewBobbing.z += (float) Math.cos(Math.toRadians(cameraRotationY - 90f)) * viewBobbingX;
            }

            float viewBobbingY = getPlayerViewBobbingY();
            if (viewBobbingY != 0) {
                posWithEyeHeightViewBobbing.y += viewBobbingY;
            }
        }
    }


    public void setPlayerPos(Vector3d newPos) {
        pos.set(newPos.x,newPos.y, newPos.z);
    }

    //mutable, be careful with this
    public Vector3f getPlayerInertia(){
        return inertia;
    }
    //immutable
    public float getPlayerInertiaX(){
        return inertia.x;
    }
    //immutable
    public float getPlayerInertiaY(){
        return inertia.y;
    }
    //immutable
    public float getPlayerInertiaZ(){
        return inertia.z;
    }

    public void setPlayerInertia(float x,float y,float z){
        inertia.x = x;
        inertia.y = y;
        inertia.z = z;
    }

    private final Vector3f inertiaBuffer = new Vector3f(0,0,0);

    private boolean forward = false;
    private boolean backward = false;
    private boolean left = false;
    private boolean right = false;
    private boolean jump = false;

    public boolean getPlayerForward(){
        return forward;
    }
    public boolean getPlayerBackward(){
        return backward;
    }
    public boolean getPlayerLeft(){
        return left;
    }
    public boolean getPlayerRight(){
        return right;
    }
    public boolean getPlayerJump(){
        return jump;
    }
    public boolean isPlayerSneaking(){
        return sneaking;
    }

    public void setPlayerForward(boolean isForward){
        forward = isForward;
    }
    public void setPlayerBackward(boolean isBackward){
        backward = isBackward;
    }
    public void setPlayerLeft(boolean isLeft){
        left = isLeft;
    }
    public void setPlayerRight(boolean isRight){
        right = isRight;
    }
    public void setPlayerJump(boolean isJump){
        jump = isJump;
    }
    public void setPlayerSneaking(boolean isSneaking){
        sneaking = isSneaking;
    }

    public void setPlayerRunning(boolean isRunning){
        if (!sneaking && isRunning) {
            running = isRunning;
        }
        else {
            running = false;
        }
    }

    private boolean playerIsMoving(){
        return forward || backward || left || right;
    }

    final private float movementAcceleration = 1000.f;

    final private float maxWalkSpeed = 4.f;

    final private float maxRunSpeed = 6.f;

    final private float maxSneakSpeed = 1.f;

    private boolean inWater = false;

    private boolean wasInWater = false;

    private float wasInWaterTimer = 0f;
    private boolean waterLockout = false;



    public void setPlayerInWater(boolean theTruth){
        inWater = theTruth;
        if (theTruth && playerIsJumping){
            playerIsJumping = false; //reset jumping mechanic
        }
    }

    public void setPlayerInertiaBuffer(){

        double delta = getDelta();

        if (forward){
            float yaw = (float)Math.toRadians(getCameraRotation().y) + (float)Math.PI;
            inertia.x += (float)(Math.sin(-yaw) * accelerationMultiplier) * movementAcceleration * delta;
            inertia.z += (float)(Math.cos(yaw)  * accelerationMultiplier) * movementAcceleration * delta;
        }
        if (backward){
            //no mod needed
            float yaw = (float)Math.toRadians(getCameraRotation().y);
            inertia.x += (float)(Math.sin(-yaw) * accelerationMultiplier) * movementAcceleration * delta;
            inertia.z += (float)(Math.cos(yaw)  * accelerationMultiplier) * movementAcceleration * delta;
        }

        if (right){
            float yaw = (float)Math.toRadians(getCameraRotation().y) - (float)(Math.PI /2);
            inertia.x += (float)(Math.sin(-yaw) * accelerationMultiplier) * movementAcceleration * delta;
            inertia.z += (float)(Math.cos(yaw)  * accelerationMultiplier) * movementAcceleration * delta;
        }

        if (left){
            float yaw = (float)Math.toRadians(getCameraRotation().y) + (float)(Math.PI /2);
            inertia.x += (float)(Math.sin(-yaw) * accelerationMultiplier) * movementAcceleration * delta;
            inertia.z += (float)(Math.cos(yaw)  * accelerationMultiplier) * movementAcceleration * delta;
        }

        if (!inWater && jump && isPlayerOnGround()){
            inertia.y += 8.75f; //do not get delta for this
            playerIsJumping = true;
        //the player comes to equilibrium with the water's surface
        // if this is not implemented like this
        } else if (jump && inWater && !waterLockout){
            wasInWater = true;
            if(inertia.y <= 4.f){
                inertia.y += 100.f * getDelta();
            }
        }
        if (wasInWater && !inWater){
            wasInWaterTimer = 0.2f;
        }
    }


    private final Vector2f inertiaWorker = new Vector2f();

    private void applyPlayerInertiaBuffer(){
        setPlayerInertiaBuffer();

        inertia.x += inertiaBuffer.x;
        inertia.y += inertiaBuffer.y;
        inertia.z += inertiaBuffer.z;

        //max speed todo: make this call from a player object's maxSpeed!
        inertiaWorker.set(inertia.x, inertia.z);

        float maxSpeed;

        if (sneaking) {
            maxSpeed = maxSneakSpeed;
        } else if (running) {
            maxSpeed = maxRunSpeed;
        } else {
            maxSpeed = maxWalkSpeed;
        }

        //speed limit the player's movement
        if(inertiaWorker.isFinite() && inertiaWorker.length() > maxSpeed){
            inertiaWorker.normalize().mul(maxSpeed);
            inertia.x = inertiaWorker.x;
            inertia.z = inertiaWorker.y;
        }

        //reset buffer
        inertiaBuffer.x = 0f;
        inertiaBuffer.y = 0f;
        inertiaBuffer.z = 0f;
    }



    public Boolean isPlayerOnGround(){
        return onGround;
    }

    private float diggingProgress = 0f;
    private byte diggingFrame = -1;
    private boolean hasDug = false;

    public boolean playerHasDug(){
        return hasDug;
    }

    public byte getDiggingFrame(){
        return diggingFrame;
    }

    private float particleBufferTimer = 0f;

    private float rainBuffer = 0f;

    private byte currentRotDir = 0;

    public byte getPlayerDir(){
        return currentRotDir;
    }

    public void playerOnTick() {

        double delta = getDelta();

        //camera underwater effect trigger
        byte cameraCheckBlock = getBlock((int)Math.floor(getCameraPositionX()),(int)Math.floor(getCameraPositionY() - 0.02d), (int)Math.floor(getCameraPositionZ()));

        cameraSubmerged = cameraCheckBlock > 0 && isBlockLiquid(cameraCheckBlock);

        //the player comes to equilibrium with the water's surface
        //if this is not implemented like this
        if (wasInWaterTimer > 0.f){
            //System.out.println(wasInWaterTimer);
            waterLockout = true;
            wasInWaterTimer -= delta;
            if (wasInWaterTimer <= 0){
                //System.out.println("turned off lockout");
                waterLockout = false;
            }
        }

        if (playerIsJumping && isPlayerOnGround()){
            playerIsJumping = false;
        }

        float camRot = getCameraRotation().y + 180f;

        if(camRot >= 315f || camRot < 45f){
//            System.out.println(2);
            currentRotDir = 2;
        } else if (camRot >= 45f && camRot < 135f){
//            System.out.println(3);
            currentRotDir = 3;
        } else if (camRot >= 135f && camRot < 225f){
//            System.out.println(0);
            currentRotDir = 0;
        } else if (camRot >= 225f){
//            System.out.println(1);
            currentRotDir = 1;
        }

        //this is a minor hack which allows it to "rain"
        /*
        rainBuffer += delta;
        if (rainBuffer >= 0.1f) {
            rainBuffer = 0f;

            double eyeHeightY = getPlayerPosWithEyeHeight().y;
            for (int x = -10; x <= 10; x++) {
                for (int z = -10; z <= 10; z++) {
                    int heightMap = getHeightMap((int) Math.floor(pos.x)+x, (int) Math.floor(pos.z)+z);
                    if (127 - heightMap > 0) {
                        for (int y = heightMap + 1; y < 127; y++) {
                            if (Math.abs(eyeHeightY - y) <= 8f) {
                                if (Math.random() > 0.8f) {
                                    createRainDrop(
                                            new Vector3f((int) Math.floor(pos.x) + x, y, (int) Math.floor(pos.z) + z)
                                                    .add((float) Math.random(), (float) Math.random(), (float) Math.random()),
                                            new Vector3f(0, -30f, 0)
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }
         */



        //mining timer
        hasDug = false;
        //reset mining timer
        if (mining && !worldSelectionPos.equals(oldWorldSelectionPos) || currentInventorySelection != oldInventorySelection){
            diggingFrame = -1;
            diggingProgress = 0f;
        }
        if (mining) {
            float progress = 0;
            //don't let players even attempt to dig undiggable blocks
            if (leafHardness > -1 && dirtHardness > -1 && woodHardness > -1 && stoneHardness > -1){
                //scan through max quickness for current tool
                if (leafHardness > 0 && leafMiningLevel / leafHardness > progress){
                    progress = leafMiningLevel / leafHardness;
                }
                if (dirtHardness > 0 && dirtMiningLevel / dirtHardness > progress){
                    progress = dirtMiningLevel / dirtHardness;
                }
                if (stoneHardness > 0 && stoneMiningLevel / stoneHardness > progress){
                    progress = stoneMiningLevel / stoneHardness;
                }
                if (woodHardness > 0 && woodMiningLevel / woodHardness > progress){
                    progress = woodMiningLevel / woodHardness;
                }
            }

            //System.out.println(progress);

            diggingProgress += delta * progress;
            if (diggingProgress >= 0.1f) {
                diggingFrame++;
                if (diggingFrame > 8) {
                    diggingFrame = 0;
                    hasDug = true;
                }
                diggingProgress = 0;
            }
        } else if (diggingFrame != -1){
            diggingFrame = -1;
        }

        //place timers
        if(placeTimer > 0){
            placeTimer -= delta;
            if (placeTimer < 0){
                placeTimer = 0;
            }
        }

        //values for application of inertia
        applyPlayerInertiaBuffer();


        //stop players from falling forever
        //this only applies their inertia if they are within a loaded chunk, IE
        //if the server doesn't load up something in time, they freeze in place
        if (getChunkKey(currentChunk.x, currentChunk.z) != null) {
            onGround = applyInertia(pos, inertia, true, width, height, true, sneaking, true, true, true);
        }

        //apply the eyeHeight offset to the eyeHeight position
        posWithEyeHeight.set(pos.x, pos.y + eyeHeight, pos.z);

        //apply the collection height offset to the collection position
        posWithCollectionHeight.set(pos.x, pos.y + collectionHeight, pos.z);


        //play sound when player lands on the ground
        if (onGround && !wasOnGround){
            playSound("dirt_" + (int)(Math.ceil(Math.random()*3)));
        }


        //fall damage
        if (onGround){

            int currentY = (int)Math.floor(pos.y);

            if (currentY < oldY){
                if (oldY - currentY > 6){
                    hurtPlayer(oldY - currentY - 6);
                }
            }
            oldY = currentY;
        }

        //apply player's body animation
        applyPlayerBodyAnimation();

        updatePlayerHandInertia();

        //this creates the view bobbing internal calculation offset in the ViewBobbing class
        //this is only creating the offsets that will be applied
        if(onGround && playerIsMoving() && !sneaking && !inWater){
            applyViewBobbing();
        } else {
            returnPlayerViewBobbing();
        }

        //apply view bobbing offset to camera position literal form (Vector3D)
        applyCameraViewBobbingOffset();


        //sneaking offset
        if (sneaking){
            if (sneakOffset > -100.f) {
                sneakOffset -= delta * 1000f;
                if (sneakOffset <= -100.f){
                    sneakOffset = -100.f;
                }
            }
        } else {
            if (sneakOffset < 0.f) {
                sneakOffset += delta * 1000f;
                if (sneakOffset > 0.f){
                    sneakOffset = 0.f;
                }
            }
        }


        if (mining){
            particleBufferTimer += delta;
            if (particleBufferTimer > 0.01f){
                int randomDir = (int)Math.floor(Math.random()*6f);
                int block;
                byte miningBlock = getBlock(worldSelectionPos.x, worldSelectionPos.y, worldSelectionPos.z);
                switch (randomDir) {
                    case 0 -> {
                        block = getBlock(worldSelectionPos.x + 1, worldSelectionPos.y, worldSelectionPos.z);
                        if (block == 0) {
                            particlePos.set(worldSelectionPos.x,worldSelectionPos.y,worldSelectionPos.z);
                            particlePos.x += 1.1f;
                            particlePos.z += Math.random();
                            particlePos.y += Math.random();

                            particleInertia.set(0,0,0);
                            particleInertia.x = (float) Math.random() * 2f;
                            particleInertia.y = (float) Math.random() * 2f;
                            particleInertia.z = (float) (Math.random() - 0.5f) * 2f;

                            createParticle(particlePos.x, particlePos.y ,particlePos.z, particleInertia.x, particleInertia.y, particleInertia.z, miningBlock);
                        }
                    }
                    case 1 -> {
                        block = getBlock(worldSelectionPos.x - 1, worldSelectionPos.y, worldSelectionPos.z);
                        if (block == 0) {
                            particlePos.set(worldSelectionPos.x,worldSelectionPos.y,worldSelectionPos.z);
                            particlePos.x -= 0.1f;
                            particlePos.z += Math.random();
                            particlePos.y += Math.random();

                            particleInertia.set(0,0,0);
                            particleInertia.x = (float) Math.random() * -2f;
                            particleInertia.y = (float) Math.random() * 2f;
                            particleInertia.z = (float) (Math.random() - 0.5f) * 2f;

                            createParticle(particlePos.x, particlePos.y ,particlePos.z, particleInertia.x, particleInertia.y, particleInertia.z, miningBlock);
                        }
                    }
                    case 2 -> {
                        block = getBlock(worldSelectionPos.x, worldSelectionPos.y + 1, worldSelectionPos.z);
                        if (block == 0) {
                            particlePos.set(worldSelectionPos.x,worldSelectionPos.y,worldSelectionPos.z);
                            particlePos.y += 1.1f;
                            particlePos.z += Math.random();
                            particlePos.x += Math.random();

                            particleInertia.set(0,0,0);
                            particleInertia.x = (float) (Math.random() - 0.5f) * 2f;
                            particleInertia.y = (float) Math.random() * 2f;
                            particleInertia.z = (float) (Math.random() - 0.5f) * 2f;

                            createParticle(particlePos.x, particlePos.y ,particlePos.z, particleInertia.x, particleInertia.y, particleInertia.z, miningBlock);
                        }
                    }
                    case 3 -> {
                        block = getBlock(worldSelectionPos.x, worldSelectionPos.y - 1, worldSelectionPos.z);
                        if (block == 0) {
                            particlePos.set(worldSelectionPos.x,worldSelectionPos.y,worldSelectionPos.z);
                            particlePos.y -= 0.1f;
                            particlePos.z += Math.random();
                            particlePos.x += Math.random();

                            particleInertia.set(0,0,0);
                            particleInertia.x = (float) (Math.random() - 0.5f) * 2f;
                            particleInertia.y = (float) Math.random() * -1f;
                            particleInertia.z = (float) (Math.random() - 0.5f) * 2f;

                            createParticle(particlePos.x, particlePos.y ,particlePos.z, particleInertia.x, particleInertia.y, particleInertia.z, miningBlock);
                        }
                    }
                    case 4 -> {
                        block = getBlock(worldSelectionPos.x, worldSelectionPos.y, worldSelectionPos.z + 1);
                        if (block == 0) {
                            particlePos.set(worldSelectionPos.x,worldSelectionPos.y,worldSelectionPos.z);
                            particlePos.z += 1.1f;
                            particlePos.x += Math.random();
                            particlePos.y += Math.random();

                            particleInertia.set(0,0,0);
                            particleInertia.z = (float) Math.random() * 2f;
                            particleInertia.y = (float) Math.random() * 2f;
                            particleInertia.x = (float) (Math.random() - 0.5f) * 2f;

                            createParticle(particlePos.x, particlePos.y ,particlePos.z, particleInertia.x, particleInertia.y, particleInertia.z, miningBlock);
                        }
                    }
                    case 5 -> {
                        block = getBlock(worldSelectionPos.x, worldSelectionPos.y, worldSelectionPos.z - 1);
                        if (block == 0) {
                            particlePos.set(worldSelectionPos.x,worldSelectionPos.y,worldSelectionPos.z);
                            particlePos.z -= 0.1f;
                            particlePos.x += Math.random();
                            particlePos.y += Math.random();

                            particleInertia.set(0,0,0);
                            particleInertia.z = (float) Math.random() * -2f;
                            particleInertia.y = (float) Math.random() * 2f;
                            particleInertia.x = (float) (Math.random() - 0.5f) * 2f;

                            createParticle(particlePos.x, particlePos.y ,particlePos.z, particleInertia.x, particleInertia.y, particleInertia.z, miningBlock);
                        }
                    }
                }
                particleBufferTimer = 0f;
            }
        }

        calculateRunningFOV();

        if (getCameraPerspective() < 2) {
            if (mining && hasDug) {
                playerRayCast(getPlayerPosWithViewBobbingX(),getPlayerPosWithViewBobbingY(),getPlayerPosWithViewBobbingZ(), getCameraRotationVectorX(),getCameraRotationVectorY(),getCameraRotationVectorZ(), reach, true, false, true);
            } else if (mining) {
                playerRayCast(getPlayerPosWithViewBobbingX(),getPlayerPosWithViewBobbingY(),getPlayerPosWithViewBobbingZ(), getCameraRotationVectorX(),getCameraRotationVectorY(),getCameraRotationVectorZ(), reach, true, false, false);
            } else if (placing && placeTimer <= 0) {
                playerRayCast(getPlayerPosWithViewBobbingX(),getPlayerPosWithViewBobbingY(),getPlayerPosWithViewBobbingZ(), getCameraRotationVectorX(),getCameraRotationVectorY(),getCameraRotationVectorZ(), reach, false, true, false);
                placeTimer = 0.25f; // every quarter second you can place
            } else {
                playerRayCast(getPlayerPosWithViewBobbingX(),getPlayerPosWithViewBobbingY(),getPlayerPosWithViewBobbingZ(), getCameraRotationVectorX(),getCameraRotationVectorY(),getCameraRotationVectorZ(), reach, false, false, false);
            }
        } else {
            if (mining && hasDug) {
                playerRayCast(getPlayerPosWithViewBobbingX(),getPlayerPosWithViewBobbingY(),getPlayerPosWithViewBobbingZ(), getCameraRotationVectorX()*-1f,getCameraRotationVectorY()*-1f,getCameraRotationVectorZ()*-1f, reach, true, false, true);
            } else if (mining) {
                playerRayCast(getPlayerPosWithViewBobbingX(),getPlayerPosWithViewBobbingY(),getPlayerPosWithViewBobbingZ(), getCameraRotationVectorX()*-1f,getCameraRotationVectorY()*-1f,getCameraRotationVectorZ()*-1f, reach, true, false, false);
            } else if (placing && placeTimer <= 0) {
                playerRayCast(getPlayerPosWithViewBobbingX(),getPlayerPosWithViewBobbingY(),getPlayerPosWithViewBobbingZ(), getCameraRotationVectorX()*-1f,getCameraRotationVectorY()*-1f,getCameraRotationVectorZ()*-1f, reach, false, true, false);
                placeTimer = 0.25f; // every quarter second you can place
            } else {
                playerRayCast(getPlayerPosWithViewBobbingX(),getPlayerPosWithViewBobbingY(),getPlayerPosWithViewBobbingZ(), getCameraRotationVectorX()*-1f,getCameraRotationVectorY()*-1f,getCameraRotationVectorZ()*-1f, reach, false, false, false);
            }
        }

        if (health <= 6) {
            makeHeartsJiggle();
        }

        //camera z axis hurt rotation thing

        /*
        if (doHurtRotation){
            if (hurtRotationUp) {
                hurtCameraRotation += delta * 150f;
                if (hurtCameraRotation >= 7f) {
                    hurtRotationUp = false;
                }
            } else {
                hurtCameraRotation -= delta * 150f;
                if (hurtCameraRotation <= 0f){
                    hurtCameraRotation = 0f;
                    hurtRotationUp = true;
                    doHurtRotation = false;
                }
            }
        }


        */


        //update light level for the wield item
        lightCheckTimer += delta;

        newFlooredPos.set((int)Math.floor(getCameraPositionX()), (int)Math.floor(getCameraPositionY()), (int)Math.floor(getCameraPositionZ()));

        //System.out.println(lightCheckTimer);
        if (lightCheckTimer >= 0.25f || !newFlooredPos.equals(oldFlooredPos)){
            lightCheckTimer = 0f;
            lightLevel = getLight(newFlooredPos.x, newFlooredPos.y, newFlooredPos.z);
        }

        //do the same for the literal wield inventory
        updateWieldInventory(lightLevel);

        oldFlooredPos.set(newFlooredPos);
        oldRealPos.set(pos);
        wasOnGround = onGround;
        oldInventorySelection = currentInventorySelection;
    }

    public void updateWorldChunkLoader(){
        int newChunkX = (int)Math.floor(pos.x / 16f);
        int newChunkZ = (int)Math.floor(pos.z / 16f);

        if (newChunkX != currentChunk.x || newChunkZ != currentChunk.z) {
            currentChunk.x = newChunkX;
            currentChunk.z = newChunkZ;
            generateNewChunks();
            setCloudPos(newChunkX,newChunkZ);
        }
    }

    public void updateMultiplayerWorldChunkLoader(){
        int newChunkX = (int)Math.floor(pos.x / 16f);
        int newChunkZ = (int)Math.floor(pos.z / 16f);

        if (newChunkX != currentChunk.x || newChunkZ != currentChunk.z) {
            currentChunk.x = newChunkX;
            currentChunk.z = newChunkZ;
            requestNewChunks();
            setCloudPos(newChunkX,newChunkZ);
        }
    }


    public void changeScrollSelection(int i){
        currentInventorySelection += i;
        if (currentInventorySelection < 0) {
            currentInventorySelection = 8;
        }
        if (currentInventorySelection > 8) {
            currentInventorySelection = 0;
        }

        //send out data if playing in multiplayer
        if (getIfMultiplayer()){
            sendInventorySlot(currentInventorySelection);
        }
    }


    public float getRunningFOVAdder(){
        return runningFOVAdder;
    }

    private void calculateRunningFOV(){
        double delta = getDelta();
        if (playerIsMoving() && running){
            if (runningFOVAdder < 0.3f){
                runningFOVAdder += delta;

                if (runningFOVAdder >= 0.3f){
                    runningFOVAdder = 0.3f;
                }
            }
        } else {
            if (runningFOVAdder > 0f){
                runningFOVAdder -= delta;

                if (runningFOVAdder <= 0f){
                    runningFOVAdder = 0f;
                }
            }
        }
    }

    public int getCurrentInventorySelection(){
        return currentInventorySelection;
    }

    public int getPlayerHealth(){
        return health;
    }

    private float healthTimer = 0;

    private void doHealthTest(){
        double delta = getDelta();

        healthTimer += delta;

        if (healthTimer >= 1f){
            healthTimer = 0;

            health -= 1;

            if (health < 0){
                health = 20;
            }

            //System.out.println("The player's health is: " + health);
            calculateHealthBarElements();
        }
    }

    public boolean isPlayerRunning(){
        return (running);
    }

    public void setPlayerHealth(int newHealth){
        health = newHealth;
        calculateHealthBarElements();
    }

    public void hurtPlayer(int hurt){
        health -= hurt;
        playSound("hurt", true);
        calculateHealthBarElements();
        //doHurtRotation = true;
    }
}
