package game.player;

import org.joml.*;

import java.lang.Math;

import static engine.Hud.rebuildMiningMesh;
import static engine.Hud.rebuildWieldHandMesh;
import static engine.Time.getDelta;
import static engine.disk.Disk.loadPlayerPos;
import static engine.graph.Camera.*;
import static engine.sound.SoundAPI.playSound;
import static game.blocks.BlockDefinition.getBlockDefinition;
import static game.blocks.BlockDefinition.isBlockLiquid;
import static game.chunk.Chunk.*;
import static game.collision.Collision.applyInertia;
import static game.particle.Particle.createParticle;
import static game.player.Inventory.getItemInInventorySlot;
import static game.player.Inventory.updateWieldInventory;
import static game.player.Ray.rayCast;


public class Player {

    private static Vector3d pos                  = loadPlayerPos();
    private static final float eyeHeight               = 1.5f;
    private static final float collectionHeight        = 0.7f;
    private static final Vector3f inertia              = new Vector3f(0,0,0);
    private static final float height                  = 1.9f;
    private static final float width                   = 0.3f;
    private static boolean onGround              =  false;
    private static boolean mining                = false;
    private static boolean placing               = false;
    private static float placeTimer              = 0;
    private static final float accelerationMultiplier  = 0.07f;
    private static final String name                   = "singleplayer";
    private static final Vector3f viewBobbing          = new Vector3f(0,0,0);
    private static int currentInventorySelection = 0;
    private static boolean inventoryOpen         = false;
    private static Vector3d worldSelectionPos    = null;
    private static int sneakOffset = 0;
    private static boolean playerIsJumping = false;
    private static final int[] currentChunk = {(int)Math.floor(pos.x / 16f),(int)Math.floor(pos.z / 16f)};

    private static float itemRotation = 0f;
    private static boolean itemRotationEnabled = false;

    private static final float reach = 3.575f;

    private static boolean sneaking              = false;
    private static boolean running               = false;

    private static float lightCheckTimer = 0f;
    private static byte lightLevel = 0;
    private static Vector3i oldPos = new Vector3i(0,0,0);

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


    //these two void are for enabling framerate consistant
    //item rotation in the hud menu
    public static void enableItemRotationInHud(){
        itemRotationEnabled = true;
    }
    public static void disableItemRotationInHud(){
        itemRotationEnabled = false;
    }

    public static float getItemRotationInHud(){
        return(itemRotation);
    }

    private static void checkIfHudItemRotates(){
        if (!itemRotationEnabled) {
            itemRotation = 0f;
        } else {
            itemRotation += 0.05f;
            //prevent buffer overflow
            if (itemRotation > 360f) {
                itemRotation -= 360f;
            }
        }
    }

    public static boolean getIfPlayerIsJumping(){
        return(playerIsJumping);
    }

    public static int[] getPlayerCurrentChunk(){
        return currentChunk;
    }

    public static float getSneakOffset(){
        return sneakOffset / 900f;
    }

    public static void setPlayerWorldSelectionPos(Vector3d thePos){
        worldSelectionPos = thePos;
    }

    public static void setPlayerWorldSelectionPos(){
        worldSelectionPos = null;
    }

    public static Vector3d getPlayerWorldSelectionPos(){
        return worldSelectionPos;
    }

    public static void togglePlayerInventory(){
        inventoryOpen = !inventoryOpen;
    }

    public static boolean isPlayerInventoryOpen(){
        return inventoryOpen;
    }

    public static String getPlayerName(){
        return name;
    }

    public static int getPlayerInventorySelection(){
        return currentInventorySelection;
    }

    public static Vector3f getPlayerViewBobbing(){
        return viewBobbing;
    }

    public static void setPlayerMining( boolean isMining){
        mining = isMining;
    }

    public static boolean getPlayerMining(){
        return mining;
    }


    private static boolean wasPlacing = false;

    public static void setPlayerPlacing( boolean isPlacing) {
        wasPlacing = placing;
        placing = isPlacing;

        //allow players to spam click to place
        if (!wasPlacing && isPlacing){
            placeTimer = 0.f;
        }
    }


    //TODO --- begin wield hand stuff!
    private static final Vector3f wieldHandAnimationPosBaseEmpty = new Vector3f(13, -15, -14f);
    private static final Vector3f wieldHandAnimationPosBaseItem = new Vector3f(13, -15, -14f);

    private static final Vector3f wieldRotationEmptyBegin = new Vector3f((float) Math.toRadians(30f), 0f, (float) Math.toRadians(-10f));
    private static final Vector3f wieldRotationEmptyEnd   = new Vector3f((float) Math.toRadians(40f), (float) Math.toRadians(20f), (float) Math.toRadians(20f));

    private static final Vector3f wieldRotationItemBegin = new Vector3f((float)Math.toRadians(0f), (float)Math.toRadians(45f), (float)Math.toRadians(0f));
    private static final Vector3f wieldRotationItemEnd   = new Vector3f((float) Math.toRadians(90f), (float) Math.toRadians(45f), (float) Math.toRadians(0f));


    private static Vector3f wieldHandAnimationPos = new Vector3f(0, 0, 0);
    private static Vector3f wieldHandAnimationRot = new Vector3f(0, 0, 0);

    private static float diggingAnimation = 0f;

    private static boolean cameraSubmerged = false;

    public static boolean isCameraSubmerged(){
        return cameraSubmerged;
    }


    public static void resetWieldHandSetupTrigger(){
        handSetUp = false;
    }

    private static boolean soundTrigger = true;

    public static void testPlayerDiggingAnimation(){
        if (!diggingAnimationGo && handSetUp){
            return;
        }

        if (worldSelectionPos != null && soundTrigger && mining){
            int block = getBlock((int)worldSelectionPos.x, (int)worldSelectionPos.y, (int)worldSelectionPos.z);
            if (block > 0){
                String digSound = getBlockDefinition(block).digSound;
                if (!digSound.equals("")) {
                    playSound(digSound);
                    soundTrigger = false;
                }
            }
        }

        if (handSetUp) {
            diggingAnimation += getDelta() * 3.75f;
        }

        if ((!diggingAnimationBuffer || diggingAnimation >= 1f) && handSetUp){
            diggingAnimationGo = false;
            diggingAnimation = 0f;
            soundTrigger = true;
            return;
        }

        if(!handSetUp){
            handSetUp = true;
        }

        if (getItemInInventorySlot(getPlayerInventorySelection(),0) == null) {
            wieldHandAnimationPos.x = (float) (-5f * Math.sin(Math.pow(diggingAnimation, 0.8f) * Math.PI)) + wieldHandAnimationPosBaseEmpty.x;
            wieldHandAnimationPos.y = (float) (5f * Math.sin(diggingAnimation * 2f * Math.PI)) + wieldHandAnimationPosBaseEmpty.y;
            wieldHandAnimationPos.z = wieldHandAnimationPosBaseEmpty.z;
            wieldHandAnimationRot.x = 180f;

            Quaternionf quatBegin = new Quaternionf().rotateXYZ(wieldRotationEmptyBegin.x, wieldRotationEmptyBegin.y, wieldRotationEmptyBegin.z);
            Quaternionf quatEnd = new Quaternionf().rotateXYZ(wieldRotationEmptyEnd.x, wieldRotationEmptyEnd.y, wieldRotationEmptyEnd.z);
            quatEnd = quatBegin.slerp(quatEnd, (float) Math.sin(diggingAnimation * Math.PI));

            wieldHandAnimationRot = quatEnd.getEulerAnglesXYZ(wieldHandAnimationRot);

            wieldHandAnimationRot.x = (float) Math.toDegrees(wieldHandAnimationRot.x);
            wieldHandAnimationRot.y = (float) Math.toDegrees(wieldHandAnimationRot.y);
            wieldHandAnimationRot.z = (float) Math.toDegrees(wieldHandAnimationRot.z);
            wieldHandAnimationRot.x += 180f;
        } else if (getItemInInventorySlot(getPlayerInventorySelection(),0).definition.blockID > 0) {
            wieldHandAnimationPos.x = wieldHandAnimationPosBaseEmpty.x;
            wieldHandAnimationPos.y = wieldHandAnimationPosBaseEmpty.y;
            wieldHandAnimationPos.z = wieldHandAnimationPosBaseEmpty.z;
            wieldHandAnimationRot.x = 180f;

            Quaternionf quatBegin = new Quaternionf().rotateXYZ(wieldRotationItemBegin.x, wieldRotationItemBegin.y, wieldRotationItemBegin.z);
            Quaternionf quatEnd = new Quaternionf().rotateXYZ(wieldRotationItemEnd.x, wieldRotationItemEnd.y, wieldRotationItemEnd.z);

            quatEnd = quatBegin.slerp(quatEnd, (float) Math.sin(diggingAnimation * Math.PI));

            wieldHandAnimationRot = quatEnd.getEulerAnglesXYZ(wieldHandAnimationRot);

            wieldHandAnimationRot.x = (float) Math.toDegrees(wieldHandAnimationRot.x);
            wieldHandAnimationRot.y = (float) Math.toDegrees(wieldHandAnimationRot.y);
            wieldHandAnimationRot.z = (float) Math.toDegrees(wieldHandAnimationRot.z);
        } else if (getItemInInventorySlot(getPlayerInventorySelection(),0).definition.isTool){

            Vector3f wieldHandAnimationPosBaseTool = new Vector3f(10f,-6.5f,-8f);

            Vector3f wieldHandAnimationRotBegin = new Vector3f((float)(Math.toRadians(0)),(float)(Math.toRadians(65)),(float)(Math.toRadians(-35)));
            Vector3f wieldHandAnimationRotEnd = new Vector3f((float)(Math.toRadians(50)),(float)(Math.toRadians(75)),(float)(Math.toRadians(-45)));

            wieldHandAnimationPos.x = (float) (-8f * Math.sin(Math.pow(diggingAnimation, 0.6f) * Math.PI)) + wieldHandAnimationPosBaseTool.x;
            wieldHandAnimationPos.y = (float) (5f * Math.sin(diggingAnimation * Math.PI)) + wieldHandAnimationPosBaseTool.y;
//            System.out.println(1f * Math.sin(diggingAnimation * 1f * Math.PI));
            wieldHandAnimationPos.z = wieldHandAnimationPosBaseTool.z;


            Quaternionf quatBegin = new Quaternionf().rotateXYZ(wieldHandAnimationRotBegin.x, wieldHandAnimationRotBegin.y, wieldHandAnimationRotBegin.z);
            Quaternionf quatEnd = new Quaternionf().rotateXYZ(wieldHandAnimationRotEnd.x, wieldHandAnimationRotEnd.y, wieldHandAnimationRotEnd.z);
            quatEnd = quatBegin.slerp(quatEnd, (float) Math.sin(diggingAnimation * Math.PI));

            wieldHandAnimationRot = quatEnd.getEulerAnglesXYZ(wieldHandAnimationRot);

            wieldHandAnimationRot.x = (float) Math.toDegrees(wieldHandAnimationRot.x);
            wieldHandAnimationRot.y = (float) Math.toDegrees(wieldHandAnimationRot.y);
            wieldHandAnimationRot.z = (float) Math.toDegrees(wieldHandAnimationRot.z);
        }

    }

    public static Vector3d getWieldHandAnimationPos(){
        Vector3d doubledHandAnimationPos = new Vector3d();

        doubledHandAnimationPos.x = wieldHandAnimationPos.x;
        doubledHandAnimationPos.y = wieldHandAnimationPos.y;
        doubledHandAnimationPos.z = wieldHandAnimationPos.z;

        return doubledHandAnimationPos;
    }

    public static Vector3f getWieldHandAnimationRot(){
        return wieldHandAnimationRot;
    }


    private static boolean diggingAnimationGo = false;
    private static boolean diggingAnimationBuffer = false;
    private static boolean handSetUp = false;
    public static void startDiggingAnimation(){
        diggingAnimationGo = true;
        diggingAnimationBuffer = true;
    }


    //TODO ----- end hand stuff!


    public static float getPlayerHeight(){
        return height;
    }
    public static float getPlayerWidth(){
        return width;
    }

    public static boolean getPlayerPlacing() {
        return placing;
    }

    public static Vector3d getPlayerPos() {
        return pos;
    }

    public static Vector3d getPlayerPosWithEyeHeight(){
        return new Vector3d(pos.x, pos.y + eyeHeight, pos.z);
    }


    public static Vector3d getPlayerPosWithViewBobbing(){
        return new Vector3d(pos.x, pos.y + eyeHeight, pos.z);
    }

    public static Vector3d getPlayerPosWithCollectionHeight(){
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

    private static Vector3f inertiaBuffer = new Vector3f();

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
        float delta = getDelta();

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
            inertia.y += 10.5f; //do not get delta for this
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
        Vector3f inertia2D = new Vector3f(inertia.x, 0, inertia.z);


        float maxSpeed; //this should probably be cached

        if(sneaking){
            maxSpeed = maxSneakSpeed;
        } else if (running){
            maxSpeed = maxRunSpeed;
        } else {
            maxSpeed = maxWalkSpeed;
        }
        if(inertia2D.length() > maxSpeed){
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

    private static float animationTest = 0f;
    private static int diggingFrame = -1;
    private static boolean hasDug = false;

    public static boolean playerHasDug(){
        return hasDug;
    }

    public static int getDiggingFrame(){
        return diggingFrame;
    }

    private static float particleBufferTimer = 0f;

    private static float rainBuffer = 0f;

    private static byte currentRotDir = 0;

    public static byte getPlayerDir(){
        return currentRotDir;
    }

    public static void playerOnTick() {

        float delta = getDelta();

        //camera underwater effect trigger
        Vector3d camPos = new Vector3d(getCameraPosition());
        camPos.y -= 0.02f;
        int cameraCheckBlock = getBlock((int)camPos.x,(int)camPos.y, (int)camPos.z);

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

        checkIfHudItemRotates();

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
        } else if (camRot >= 225f && camRot < 315f){
//            System.out.println(1);
            currentRotDir = 1;
        }

        //this is a minor hack which allows it to "rain"
//        rainBuffer += delta;
//        if (rainBuffer >= 0.1f) {
//            rainBuffer = 0f;
//
//            float eyeHeightY = getPlayerPosWithEyeHeight().y;
//            for (int x = -10; x <= 10; x++) {
//                for (int z = -10; z <= 10; z++) {
//                    int heightMap = getHeightMap((int) Math.floor(pos.x)+x, (int) Math.floor(pos.z)+z);
//                    if (127 - heightMap > 0) {
//                        for (int y = heightMap + 1; y < 127; y++) {
//                            if (Math.abs(eyeHeightY - y) <= 8f) {
//                                if (Math.random() > 0.8f) {
//                                    createRainDrop(
//                                            new Vector3f((int) Math.floor(pos.x) + x, y, (int) Math.floor(pos.z) + z)
//                                                    .add((float) Math.random(), (float) Math.random(), (float) Math.random()),
//                                            new Vector3f(0, -30f, 0)
//                                    );
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }



        //mining timer
        hasDug = false;
        if (mining && worldSelectionPos != null) {
            animationTest += delta;
            if (animationTest >= 0.1f) {
                diggingFrame++;
                if (diggingFrame > 8) {
                    diggingFrame = 0;
                    hasDug = true;
                }
                animationTest = 0;
                rebuildMiningMesh(diggingFrame);
            }
        } else if (diggingFrame != -1){
            diggingFrame = -1;
            rebuildMiningMesh(0);
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

        onGround = applyInertia(pos, inertia, true, width, height,true, sneaking, true, true, true);


        /* TODO: UNCOMMENT VIEW BOBBING
        if(playerIsMoving() && !sneaking && !inWater){
            applyViewBobbing();
        } else {
            returnPlayerViewBobbing();
        }
         */

        //sneaking offset
        if (sneaking){
            if (sneakOffset > -100) {
                sneakOffset -= 1;
            }
        } else {
            if (sneakOffset < 0) {
                sneakOffset += 1;
            }
        }

        if (mining && worldSelectionPos != null){
            particleBufferTimer += 0.01f;
            if (particleBufferTimer > 0.2f){
                int randomDir = (int)Math.floor(Math.random()*6f);
                int block;
                int miningBlock = getBlock((int)worldSelectionPos.x, (int)worldSelectionPos.y, (int)worldSelectionPos.z);
                switch (randomDir){
                    case 0:
                        block = getBlock((int)worldSelectionPos.x+1, (int)worldSelectionPos.y, (int)worldSelectionPos.z);
                        if (block == 0){
                            Vector3d particlePos = new Vector3d(worldSelectionPos);
                            particlePos.x += 1.1f;
                            particlePos.z += Math.random();
                            particlePos.y += Math.random();

                            Vector3f particleInertia = new Vector3f();
                            particleInertia.x = (float)Math.random()*2f;
                            particleInertia.y = (float)Math.random()*2f;
                            particleInertia.z = (float)(Math.random()-0.5f)*2f;

                            createParticle(particlePos, particleInertia, miningBlock);
                        }
                        break;
                    case 1:
                        block = getBlock((int)worldSelectionPos.x-1, (int)worldSelectionPos.y, (int)worldSelectionPos.z);
                        if (block == 0){
                            Vector3d particlePos = new Vector3d(worldSelectionPos);
                            particlePos.x -= 0.1f;
                            particlePos.z += Math.random();
                            particlePos.y += Math.random();

                            Vector3f particleInertia = new Vector3f();
                            particleInertia.x = (float)Math.random()*-2f;
                            particleInertia.y = (float)Math.random()*2f;
                            particleInertia.z = (float)(Math.random()-0.5f)*2f;

                            createParticle(particlePos, particleInertia, miningBlock);
                        }
                        break;
                    case 2:
                        block = getBlock((int)worldSelectionPos.x, (int)worldSelectionPos.y+1, (int)worldSelectionPos.z);
                        if (block == 0){
                            Vector3d particlePos = new Vector3d(worldSelectionPos);
                            particlePos.y += 1.1f;
                            particlePos.z += Math.random();
                            particlePos.x += Math.random();

                            Vector3f particleInertia = new Vector3f();
                            particleInertia.x = (float)(Math.random()-0.5f)*2f;
                            particleInertia.y = (float)Math.random()*2f;
                            particleInertia.z = (float)(Math.random()-0.5f)*2f;

                            createParticle(particlePos, particleInertia, miningBlock);
                        }
                        break;
                    case 3:
                        block = getBlock((int)worldSelectionPos.x, (int)worldSelectionPos.y-1, (int)worldSelectionPos.z);
                        if (block == 0){
                            Vector3d particlePos = new Vector3d(worldSelectionPos);
                            particlePos.y -= 0.1f;
                            particlePos.z += Math.random();
                            particlePos.x += Math.random();

                            Vector3f particleInertia = new Vector3f();
                            particleInertia.x = (float)(Math.random()-0.5f)*2f;
                            particleInertia.y = (float)Math.random()*-1f;
                            particleInertia.z = (float)(Math.random()-0.5f)*2f;

                            createParticle(particlePos, particleInertia, miningBlock);
                        }
                        break;
                    case 4:
                        block = getBlock((int)worldSelectionPos.x, (int)worldSelectionPos.y, (int)worldSelectionPos.z+1);
                        if (block == 0){
                            Vector3d particlePos = new Vector3d(worldSelectionPos);
                            particlePos.z += 1.1f;
                            particlePos.x += Math.random();
                            particlePos.y += Math.random();

                            Vector3f particleInertia = new Vector3f();
                            particleInertia.z = (float)Math.random()*2f;
                            particleInertia.y = (float)Math.random()*2f;
                            particleInertia.x = (float)(Math.random()-0.5f)*2f;

                            createParticle(particlePos, particleInertia, miningBlock);
                        }
                        break;
                    case 5:
                        block = getBlock((int)worldSelectionPos.x, (int)worldSelectionPos.y, (int)worldSelectionPos.z-1);
                        if (block == 0){
                            Vector3d particlePos = new Vector3d(worldSelectionPos);
                            particlePos.z -= 0.1f;
                            particlePos.x += Math.random();
                            particlePos.y += Math.random();

                            Vector3f particleInertia = new Vector3f();
                            particleInertia.z = (float)Math.random()*-2f;
                            particleInertia.y = (float)Math.random()*2f;
                            particleInertia.x = (float)(Math.random()-0.5f)*2f;

                            createParticle(particlePos, particleInertia, miningBlock);
                        }
                        break;
                }
                particleBufferTimer = 0f;
            }
        }

        if(mining && hasDug) {
            rayCast(getCameraPosition(), getCameraRotationVector(), reach,  true, false);
        } else if (placing && placeTimer <= 0){
            rayCast(getCameraPosition(), getCameraRotationVector(), reach,  false, true);
            placeTimer = 0.25f; // every quarter second you can place
        } else {
            rayCast(getCameraPosition(), getCameraRotationVector(), reach,  false, false);
        }


        //update light level for the wield item
        lightCheckTimer += delta;
        Vector3i newFlooredPos = new Vector3i((int)Math.floor(camPos.x), (int)Math.floor(camPos.y), (int)Math.floor(camPos.z));

        //System.out.println(lightCheckTimer);
        if (lightCheckTimer >= 0.5f || !newFlooredPos.equals(oldPos)){
            lightCheckTimer = 0f;

            byte newLightLevel = getLight(newFlooredPos.x, newFlooredPos.y, newFlooredPos.z);

            if (newLightLevel != lightLevel){
                lightLevel = newLightLevel;
                rebuildWieldHandMesh(lightLevel);

            }
        }

        //do the same for the literal wield inventory
        updateWieldInventory(lightLevel);

        oldPos = newFlooredPos;
    }

    public static void updateWorldChunkLoader(){
        int newChunkX = (int)Math.floor(pos.x / 16f);
        int newChunkZ = (int)Math.floor(pos.z / 16f);

        if (newChunkX != currentChunk[0] || newChunkZ != currentChunk[1]) {
            if (newChunkX < currentChunk[0]) {
                generateNewChunks(currentChunk[0], currentChunk[1], -1, 0);
            }
            if (newChunkX > currentChunk[0]) {
                generateNewChunks(currentChunk[0], currentChunk[1], 1, 0);
            }
            if (newChunkZ < currentChunk[1]) {
                generateNewChunks(currentChunk[0], currentChunk[1], 0, -1);
            }
            if (newChunkZ > currentChunk[1]) {
                generateNewChunks(currentChunk[0], currentChunk[1], 0, 1);
            }
            currentChunk[0] = newChunkX;
            currentChunk[1] = newChunkZ;
        }
    }

    private static boolean xPositive = true;
    private static boolean yPositive = true;
    private static int xBobPos = 0;
    private static int yBobPos = 0;

    private static void applyViewBobbing() {

        int viewBobbingAddition = 10;
        if (running){
            viewBobbingAddition = 16;
        }

        if (xPositive) {
            xBobPos += viewBobbingAddition;
            if (xBobPos >= 2000){
                xBobPos = 2000;
                xPositive = false;
                yPositive = false;
                playSound("dirt_" + (int)(Math.ceil(Math.random()*3)));
            }
        } else {
            xBobPos -= viewBobbingAddition;
            if (xBobPos <= -2000){
                xBobPos = -2000;
                xPositive = true;
                yPositive = false;
                playSound("dirt_"  + (int)(Math.ceil(Math.random()*3)));
            }
        }

        yBobPos = Math.abs(xBobPos);

        viewBobbing.x = xBobPos/18000f;
        viewBobbing.y = yBobPos/20000f;
    }

    private static void returnPlayerViewBobbing(){

        if (xBobPos > 0){
            xBobPos -= 10;
        } else if (xBobPos < 0){
            xBobPos += 10;
        }

        yBobPos = Math.abs(xBobPos);

        viewBobbing.x = xBobPos/18000f;
        viewBobbing.y = yBobPos/20000f;
    }

    public static void changeScrollSelection(int i){
        currentInventorySelection += i;
        if (currentInventorySelection < 0) {
            currentInventorySelection = 8;
        }
        if (currentInventorySelection > 8) {
            currentInventorySelection = 0;
        }
    }

    public static int getCurrentInventorySelection(){
        return currentInventorySelection;
    }
}
