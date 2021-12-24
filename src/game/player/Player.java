package game.player;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static engine.disk.Disk.loadPlayerPos;
import static engine.graphics.Camera.*;
import static engine.gui.GUILogic.calculateHealthBarElements;
import static engine.gui.GUILogic.makeHeartsJiggle;
import static engine.network.Networking.getIfMultiplayer;
import static engine.network.Networking.sendInventorySlot;
import static engine.sound.SoundAPI.playSound;
import static engine.time.Time.getDelta;
import static game.blocks.BlockDefinition.*;
import static game.chunk.Chunk.*;
import static game.clouds.Cloud.setCloudPos;
import static game.collision.Collision.applyInertia;
import static game.crafting.Inventory.updateWieldInventory;
import static game.particle.Particle.createParticle;
import static game.player.ViewBobbing.*;
import static game.player.WieldHand.updatePlayerHandInertia;
import static game.ray.Ray.playerRayCast;


public class Player {

    private static float runningFOVAdder = 0f;
    private static int health = 20;
    private static final float collectionHeight        = 0.7f;
    private static final float eyeHeight               = 1.5f;
    private static Vector3d pos = loadPlayerPos();
    private static Vector3d posWithEyeHeight = new Vector3d().set(pos.x,pos.y + eyeHeight,pos.z);
    private static Vector3d posWithEyeHeightViewBobbing = new Vector3d().set(posWithEyeHeight.x, posWithEyeHeight.y, posWithEyeHeight.z);
    private static final Vector3f inertia              = new Vector3f(0,0,0);
    private static final float height                  = 1.9f;
    private static final float width                   = 0.3f;
    private static boolean onGround              =  false;
    private static boolean wasOnGround           = false;
    private static boolean mining                = false;
    private static boolean placing               = false;
    private static float placeTimer              = 0;
    private static final float accelerationMultiplier  = 0.07f;
    private static String name                   = "";
    private static int currentInventorySelection = 0;
    private static int oldInventorySelection = 0;
    private static Vector3i oldWorldSelectionPos = new Vector3i();
    private static Vector3i worldSelectionPos    = new Vector3i();
    private static float sneakOffset = 0;
    private static boolean playerIsJumping = false;
    //this is like this because working with x and z is easier than x and y
    private static final Vector3i currentChunk = new Vector3i((int)Math.floor(pos.x / 16f),0,(int)Math.floor(pos.z / 16f));
    public static int oldY = 0;

    private static final float reach = 3.575f;

    private static boolean sneaking              = false;
    private static boolean running               = false;

    private static float lightCheckTimer = 0f;
    private static byte lightLevel = 0;
    private static final Vector3i oldFlooredPos = new Vector3i(0,0,0);
    private static final Vector3d oldRealPos = new Vector3d(0,0,0);

    private static float hurtCameraRotation = 0;
    private static boolean doHurtRotation = false;
    private static boolean hurtRotationUp = true;

    //block hardness cache
    private static float stoneHardness = 0f;
    private static float dirtHardness = 0f;
    private static float woodHardness = 0f;
    private static float leafHardness = 0f;

    //tool mining level cache
    private static float stoneMiningLevel = 0.3f;
    private static float dirtMiningLevel = 1f;
    private static float woodMiningLevel = 1f;
    private static float leafMiningLevel = 1f;

    //animation data
    private static final Vector3f[] bodyRotations = new Vector3f[]{
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
            new Vector3f(0,0,0),
    };



    public static Vector3d getOldRealPos(){
        return oldRealPos;
    }


    public static String getName(){
        return name;
    }

    public static void setPlayerName(String newName){
        name = newName;
    }


    private static float animationTimer = 0f;

    public static void updatePlayerMiningLevelCache(float newStoneMiningLevel, float newDirtMiningLevel, float newWoodMiningLevel, float newLeafMiningLevel){
        //System.out.println("New levels: " + newStoneMiningLevel + " " + newDirtMiningLevel + " " + newWoodMiningLevel + " " + newLeafMiningLevel);
        stoneMiningLevel = newStoneMiningLevel;
        dirtMiningLevel = newDirtMiningLevel;
        woodMiningLevel = newWoodMiningLevel;
        leafMiningLevel = newLeafMiningLevel;
    }

    public static void addPlayerInertia(float x, float y, float z){
        inertia.add(x,y,z);
    }

    public static Vector3f[] getPlayerBodyRotations(){
        return bodyRotations;
    }

    public static float getHurtCameraRotation(){
        return hurtCameraRotation;
    }

    public static void resetPlayerInputs(){
        setPlayerForward(false);
        setPlayerBackward(false);
        setPlayerLeft(false);
        setPlayerRight(false);
        setPlayerSneaking(false);
        setPlayerJump(false);
        mining = false;
        placing = false;
    }


    public static boolean getIfPlayerIsJumping(){
        return(playerIsJumping);
    }

    public static Vector3i getPlayerCurrentChunk(){
        return currentChunk;
    }

    public static float getSneakOffset(){
        return sneakOffset / 900f;
    }

    public static void setPlayerWorldSelectionPos(Vector3i thePos){
        if (worldSelectionPos != null) {
            oldWorldSelectionPos = new Vector3i(worldSelectionPos);
        }
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
        }
        worldSelectionPos = thePos;
    }


    public static byte getPlayerLightLevel(){
        return lightLevel;
    }

    public static void setPlayerWorldSelectionPos(){
        worldSelectionPos = null;
    }

    public static Vector3i getPlayerWorldSelectionPos(){
        return worldSelectionPos;
    }

    public static String getPlayerName(){
        return name;
    }

    public static int getPlayerInventorySelection(){
        return currentInventorySelection;
    }

    public static void setPlayerMining( boolean isMining){
        mining = isMining;
    }

    public static boolean getPlayerMining(){
        return mining;
    }


    public static void setPlayerPlacing( boolean isPlacing) {
        boolean wasPlacing = placing;
        placing = isPlacing;

        //allow players to spam click to place
        if (!wasPlacing && isPlacing){
            placeTimer = 0.f;
        }
    }

    public static boolean isCameraSubmerged(){
        return cameraSubmerged;
    }

    private static boolean cameraSubmerged = false;


    public static float getPlayerHeight(){
        return height;
    }
    public static float getPlayerWidth(){
        return width;
    }

    public static boolean getPlayerPlacing() {
        return placing;
    }

    //this is mutable, be careful with this
    public static Vector3d getPlayerPos() {
        return pos;
    }

    //immutable
    public static double getPlayerPosX(){
        return pos.x;
    }
    //immutable
    public static double getPlayerPosY(){
        return pos.y;
    }
    //immutable
    public static double getPlayerPosZ(){
        return pos.z;
    }

    //this is mutable, be careful with this
    public static Vector3d getPlayerPosWithEyeHeight(){
        return posWithEyeHeight;
    }
    //immutable
    public static double getPlayerPosWithEyeHeightX(){
        return posWithEyeHeight.x;
    }
    //immutable
    public static double getPlayerPosWithEyeHeightY(){
        return posWithEyeHeight.y;
    }
    //immutable
    public static double getPlayerPosWithEyeHeightZ(){
        return posWithEyeHeight.z;
    }


    public static Vector3d getPlayerPosWithViewBobbing(){

        // THIS CREATES A NEW OBJECT IN HEAP!
        Vector3d position = getPlayerPosWithEyeHeight();

        if (getCameraPerspective() == 0) {
            Vector3f cameraRotation = getCameraRotation();

            float viewBobbingZ = getPlayerViewBobbingZ();
            if (viewBobbingZ != 0) {
                position.x += (float) Math.sin(Math.toRadians(cameraRotation.y)) * -1.0f * viewBobbingZ;
                position.z += (float) Math.cos(Math.toRadians(cameraRotation.y)) * viewBobbingZ;
            }

            float viewBobbingX = getPlayerViewBobbingX();
            if (viewBobbingX != 0) {
                position.x += (float) Math.sin(Math.toRadians(cameraRotation.y - 90f)) * -1.0f * viewBobbingX;
                position.z += (float) Math.cos(Math.toRadians(cameraRotation.y - 90f)) * viewBobbingX;
            }

            float viewBobbingY = getPlayerViewBobbingY();
            if (viewBobbingY != 0) {
                position.y += viewBobbingY;
            }
        }
        return position;
    }

    public static Vector3d getPlayerPosWithCollectionHeight(){
        // THIS CREATES A NEW OBJECT IN HEAP!
        return new Vector3d(pos.x, pos.y + collectionHeight, pos.z);
    }

    public static void setPlayerPos(Vector3d newPos) {
        pos = newPos;
    }

    public static Vector3f getPlayerInertia(){
        return inertia;
    }

    public static void setPlayerInertia(float x,float y,float z){
        inertia.x = x;
        inertia.y = y;
        inertia.z = z;
    }

    private static Vector3f inertiaBuffer = new Vector3f(0,0,0);

    private static boolean forward = false;
    private static boolean backward = false;
    private static boolean left = false;
    private static boolean right = false;
    private static boolean jump = false;

    public static boolean getPlayerForward(){
        return forward;
    }
    public static boolean getPlayerBackward(){
        return backward;
    }
    public static boolean getPlayerLeft(){
        return left;
    }
    public static boolean getPlayerRight(){
        return right;
    }
    public static boolean getPlayerJump(){
        return jump;
    }
    public static boolean isPlayerSneaking(){
        return sneaking;
    }

    public static void setPlayerForward(boolean isForward){
        forward = isForward;
    }
    public static void setPlayerBackward(boolean isBackward){
        backward = isBackward;
    }
    public static void setPlayerLeft(boolean isLeft){
        left = isLeft;
    }
    public static void setPlayerRight(boolean isRight){
        right = isRight;
    }
    public static void setPlayerJump(boolean isJump){
        jump = isJump;
    }
    public static void setPlayerSneaking(boolean isSneaking){
        sneaking = isSneaking;
    }

    public static void setPlayerRunning(boolean isRunning){
        if (!sneaking && isRunning) {
            running = isRunning;
        }
        else {
            running = false;
        }
    }

    private static boolean playerIsMoving(){
        return forward || backward || left || right;
    }

    final private static float movementAcceleration = 1000.f;

    final private static float maxWalkSpeed = 4.f;

    final private static float maxRunSpeed = 6.f;

    final private static float maxSneakSpeed = 1.f;

    private static boolean inWater = false;

    private static boolean wasInWater = false;

    private static float wasInWaterTimer = 0f;
    private static boolean waterLockout = false;



    public static void setPlayerInWater(boolean theTruth){
        inWater = theTruth;
        if (theTruth && playerIsJumping){
            playerIsJumping = false; //reset jumping mechanic
        }
    }

    public static void setPlayerInertiaBuffer(){

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

    private static void applyPlayerInertiaBuffer(){
        setPlayerInertiaBuffer();

        inertia.x += inertiaBuffer.x;
        inertia.y += inertiaBuffer.y;
        inertia.z += inertiaBuffer.z;

        //max speed todo: make this call from a player object's maxSpeed!
        // THIS CREATES A NEW OBJECT IN HEAP!
        Vector3f inertia2D = new Vector3f(inertia.x, 0, inertia.z);

        float maxSpeed; //this should probably be cached

        if(sneaking){
            maxSpeed = maxSneakSpeed;
        } else if (running){
            maxSpeed = maxRunSpeed;
        } else {
            maxSpeed = maxWalkSpeed;
        }

        if(inertia2D.isFinite() && inertia2D.length() > maxSpeed){
            inertia2D = inertia2D.normalize().mul(maxSpeed);
            inertia.x = inertia2D.x;
            inertia.z = inertia2D.z;
        }
        
        inertiaBuffer.x = 0f;
        inertiaBuffer.y = 0f;
        inertiaBuffer.z = 0f;
    }



    public static Boolean isPlayerOnGround(){
        return onGround;
    }

    private static float diggingProgress = 0f;
    private static byte diggingFrame = -1;
    private static boolean hasDug = false;

    public static boolean playerHasDug(){
        return hasDug;
    }

    public static byte getDiggingFrame(){
        return diggingFrame;
    }

    private static float particleBufferTimer = 0f;

    private static float rainBuffer = 0f;

    private static byte currentRotDir = 0;

    public static byte getPlayerDir(){
        return currentRotDir;
    }

    public static void playerOnTick() {

        double delta = getDelta();

        //camera underwater effect trigger
        // THIS CREATES A NEW OBJECT IN HEAP!
        Vector3d camPos = new Vector3d(getCameraPosition());
        camPos.y -= 0.02f;
        byte cameraCheckBlock = getBlock((int)Math.floor(camPos.x),(int)Math.floor(camPos.y), (int)Math.floor(camPos.z));

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
        if ((mining && worldSelectionPos != null && !worldSelectionPos.equals(oldWorldSelectionPos)) || (currentInventorySelection != oldInventorySelection)){
            diggingFrame = -1;
            diggingProgress = 0f;
        }
        if (mining && worldSelectionPos != null) {
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

        //body animation scope
        {
            // THIS CREATES A NEW OBJECT IN HEAP!
            Vector3f inertia2D = new Vector3f(inertia.x, 0, inertia.z);

            animationTimer += delta * (inertia2D.length() / maxWalkSpeed) * 2f;

            if (animationTimer >= 1f) {
                animationTimer -= 1f;
            }

            bodyRotations[2] = new Vector3f((float) Math.toDegrees(Math.sin(animationTimer * Math.PI * 2f)), 0, 0);
            bodyRotations[3] = new Vector3f((float) Math.toDegrees(Math.sin(animationTimer * Math.PI * -2f)), 0, 0);

            bodyRotations[4] = new Vector3f((float) Math.toDegrees(Math.sin(animationTimer * Math.PI * -2f)), 0, 0);
            bodyRotations[5] = new Vector3f((float) Math.toDegrees(Math.sin(animationTimer * Math.PI * 2f)), 0, 0);
        }

        updatePlayerHandInertia();

        if(onGround && playerIsMoving() && !sneaking && !inWater){
            applyViewBobbing();
        } else {
            returnPlayerViewBobbing();
        }

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

        if (mining && worldSelectionPos != null){
            particleBufferTimer += delta;
            if (particleBufferTimer > 0.01f){
                int randomDir = (int)Math.floor(Math.random()*6f);
                int block;
                byte miningBlock = getBlock(worldSelectionPos.x, worldSelectionPos.y, worldSelectionPos.z);
                switch (randomDir) {
                    case 0 -> {
                        block = getBlock(worldSelectionPos.x + 1, worldSelectionPos.y, worldSelectionPos.z);
                        if (block == 0) {
                            Vector3d particlePos = new Vector3d(worldSelectionPos);
                            particlePos.x += 1.1f;
                            particlePos.z += Math.random();
                            particlePos.y += Math.random();

                            Vector3f particleInertia = new Vector3f();
                            particleInertia.x = (float) Math.random() * 2f;
                            particleInertia.y = (float) Math.random() * 2f;
                            particleInertia.z = (float) (Math.random() - 0.5f) * 2f;

                            createParticle(particlePos, particleInertia, miningBlock);
                        }
                    }
                    case 1 -> {
                        block = getBlock(worldSelectionPos.x - 1, worldSelectionPos.y, worldSelectionPos.z);
                        if (block == 0) {
                            Vector3d particlePos = new Vector3d(worldSelectionPos);
                            particlePos.x -= 0.1f;
                            particlePos.z += Math.random();
                            particlePos.y += Math.random();

                            Vector3f particleInertia = new Vector3f();
                            particleInertia.x = (float) Math.random() * -2f;
                            particleInertia.y = (float) Math.random() * 2f;
                            particleInertia.z = (float) (Math.random() - 0.5f) * 2f;

                            createParticle(particlePos, particleInertia, miningBlock);
                        }
                    }
                    case 2 -> {
                        block = getBlock(worldSelectionPos.x, worldSelectionPos.y + 1, worldSelectionPos.z);
                        if (block == 0) {
                            // THIS CREATES A NEW OBJECT IN HEAP!
                            Vector3d particlePos = new Vector3d(worldSelectionPos);
                            particlePos.y += 1.1f;
                            particlePos.z += Math.random();
                            particlePos.x += Math.random();
                            // THIS CREATES A NEW OBJECT IN HEAP!
                            Vector3f particleInertia = new Vector3f();
                            particleInertia.x = (float) (Math.random() - 0.5f) * 2f;
                            particleInertia.y = (float) Math.random() * 2f;
                            particleInertia.z = (float) (Math.random() - 0.5f) * 2f;

                            createParticle(particlePos, particleInertia, miningBlock);
                        }
                    }
                    case 3 -> {
                        block = getBlock(worldSelectionPos.x, worldSelectionPos.y - 1, worldSelectionPos.z);
                        if (block == 0) {
                            // THIS CREATES A NEW OBJECT IN HEAP!
                            Vector3d particlePos = new Vector3d(worldSelectionPos);
                            particlePos.y -= 0.1f;
                            particlePos.z += Math.random();
                            particlePos.x += Math.random();
                            // THIS CREATES A NEW OBJECT IN HEAP!
                            Vector3f particleInertia = new Vector3f();
                            particleInertia.x = (float) (Math.random() - 0.5f) * 2f;
                            particleInertia.y = (float) Math.random() * -1f;
                            particleInertia.z = (float) (Math.random() - 0.5f) * 2f;

                            createParticle(particlePos, particleInertia, miningBlock);
                        }
                    }
                    case 4 -> {
                        block = getBlock(worldSelectionPos.x, worldSelectionPos.y, worldSelectionPos.z + 1);
                        if (block == 0) {
                            // THIS CREATES A NEW OBJECT IN HEAP!
                            Vector3d particlePos = new Vector3d(worldSelectionPos);
                            particlePos.z += 1.1f;
                            particlePos.x += Math.random();
                            particlePos.y += Math.random();
                            // THIS CREATES A NEW OBJECT IN HEAP!
                            Vector3f particleInertia = new Vector3f();
                            particleInertia.z = (float) Math.random() * 2f;
                            particleInertia.y = (float) Math.random() * 2f;
                            particleInertia.x = (float) (Math.random() - 0.5f) * 2f;

                            createParticle(particlePos, particleInertia, miningBlock);
                        }
                    }
                    case 5 -> {
                        block = getBlock(worldSelectionPos.x, worldSelectionPos.y, worldSelectionPos.z - 1);
                        if (block == 0) {
                            // THIS CREATES A NEW OBJECT IN HEAP!
                            Vector3d particlePos = new Vector3d(worldSelectionPos);
                            particlePos.z -= 0.1f;
                            particlePos.x += Math.random();
                            particlePos.y += Math.random();
                            // THIS CREATES A NEW OBJECT IN HEAP!
                            Vector3f particleInertia = new Vector3f();
                            particleInertia.z = (float) Math.random() * -2f;
                            particleInertia.y = (float) Math.random() * 2f;
                            particleInertia.x = (float) (Math.random() - 0.5f) * 2f;

                            createParticle(particlePos, particleInertia, miningBlock);
                        }
                    }
                }
                particleBufferTimer = 0f;
            }
        }

        calculateRunningFOV();

        if (getCameraPerspective() < 2) {
            if (mining && hasDug) {
                playerRayCast(getPlayerPosWithViewBobbing(), getCameraRotationVector(), reach, true, false, true);
            } else if (mining) {
                playerRayCast(getPlayerPosWithViewBobbing(), getCameraRotationVector(), reach, true, false, false);
            } else if (placing && placeTimer <= 0) {
                playerRayCast(getPlayerPosWithViewBobbing(), getCameraRotationVector(), reach, false, true, false);
                placeTimer = 0.25f; // every quarter second you can place
            } else {
                playerRayCast(getPlayerPosWithViewBobbing(), getCameraRotationVector(), reach, false, false, false);
            }
        } else {
            if (mining && hasDug) {
                playerRayCast(getPlayerPosWithViewBobbing(), getCameraRotationVector().mul(-1), reach, true, false, true);
            } else if (mining) {
                playerRayCast(getPlayerPosWithViewBobbing(), getCameraRotationVector().mul(-1), reach, true, false, false);
            } else if (placing && placeTimer <= 0) {
                playerRayCast(getPlayerPosWithViewBobbing(), getCameraRotationVector().mul(-1), reach, false, true, false);
                placeTimer = 0.25f; // every quarter second you can place
            } else {
                playerRayCast(getPlayerPosWithViewBobbing(), getCameraRotationVector().mul(-1), reach, false, false, false);
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
        // THIS CREATES A NEW OBJECT IN HEAP!
        Vector3i newFlooredPos = new Vector3i((int)Math.floor(camPos.x), (int)Math.floor(camPos.y), (int)Math.floor(camPos.z));

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

    public static void updateWorldChunkLoader(){
        int newChunkX = (int)Math.floor(pos.x / 16f);
        int newChunkZ = (int)Math.floor(pos.z / 16f);

        if (newChunkX != currentChunk.x || newChunkZ != currentChunk.z) {
            currentChunk.x = newChunkX;
            currentChunk.z = newChunkZ;
            generateNewChunks();
            setCloudPos(newChunkX,newChunkZ);
        }
    }

    public static void updateMultiplayerWorldChunkLoader(){
        int newChunkX = (int)Math.floor(pos.x / 16f);
        int newChunkZ = (int)Math.floor(pos.z / 16f);

        if (newChunkX != currentChunk.x || newChunkZ != currentChunk.z) {
            currentChunk.x = newChunkX;
            currentChunk.z = newChunkZ;
            requestNewChunks();
            setCloudPos(newChunkX,newChunkZ);
        }
    }


    public static void changeScrollSelection(int i){
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


    public static float getRunningFOVAdder(){
        return runningFOVAdder;
    }

    private static void calculateRunningFOV(){
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

    public static int getCurrentInventorySelection(){
        return currentInventorySelection;
    }

    public static int getPlayerHealth(){
        return health;
    }

    private static float healthTimer = 0;

    private static void doHealthTest(){
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

    public static boolean isPlayerRunning(){
        return (running);
    }

    public static void hurtPlayer(int hurt){
        health -= hurt;
        playSound("hurt", true);
        calculateHealthBarElements();
        //doHurtRotation = true;
    }
}
