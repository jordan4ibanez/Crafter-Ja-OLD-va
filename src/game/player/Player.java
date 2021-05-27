package game.player;

import game.blocks.BlockDefinition;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static engine.Time.getDelta;
import static engine.disk.Disk.loadPlayerPos;
import static engine.graphics.Camera.*;
import static engine.gui.GUI.*;
import static engine.gui.GUILogic.calculateHealthBarElements;
import static engine.gui.GUILogic.makeHeartsJiggle;
import static engine.sound.SoundAPI.playSound;
import static game.blocks.BlockDefinition.getBlockDefinition;
import static game.blocks.BlockDefinition.isBlockLiquid;
import static game.chunk.Chunk.*;
import static game.collision.Collision.applyInertia;
import static game.crafting.Inventory.getItemInInventorySlot;
import static game.crafting.Inventory.updateWieldInventory;
import static game.particle.Particle.createParticle;
import static game.ray.Ray.playerRayCast;


public class Player {

    private static float runningFOVAdder = 0f;
    private static int health = 20;
    private static Vector3d pos                  = loadPlayerPos();
    private static final float eyeHeight               = 1.5f;
    private static final float collectionHeight        = 0.7f;
    private static final Vector3f inertia              = new Vector3f(0,0,0);
    private static final float height                  = 1.9f;
    private static final float width                   = 0.3f;
    private static boolean onGround              =  false;
    private static boolean wasOnGround           = false;
    private static boolean mining                = false;
    private static boolean placing               = false;
    private static float placeTimer              = 0;
    private static final float accelerationMultiplier  = 0.07f;
    private static final String name                   = "singleplayer";
    private static final Vector3f viewBobbing          = new Vector3f(0,0,0);
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
    private static Vector3i oldPos = new Vector3i(0,0,0);
    private static Vector3d oldRealPos = new Vector3d(0,0,0);

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
    private static float animationTimer = 0f;

    public static void updatePlayerMiningLevelCache(float newStoneMiningLevel, float newDirtMiningLevel, float newWoodMiningLevel, float newLeafMiningLevel){
        //System.out.println("New levels: " + newStoneMiningLevel + " " + newDirtMiningLevel + " " + newWoodMiningLevel + " " + newLeafMiningLevel);
        stoneMiningLevel = newStoneMiningLevel;
        dirtMiningLevel = newDirtMiningLevel;
        woodMiningLevel = newWoodMiningLevel;
        leafMiningLevel = newLeafMiningLevel;
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
            int block = getBlock(thePos.x, thePos.y,thePos.z);
            //add in block hardness levels
            if (block > 0){
                BlockDefinition thisDef = getBlockDefinition(block);
                stoneHardness = thisDef.stoneHardness;
                dirtHardness = thisDef.dirtHardness;
                woodHardness = thisDef.woodHardness;
                leafHardness = thisDef.leafHardness;
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
        if (!diggingAnimationGo && handSetUp && diggingAnimation == 0f){
            return;
        }

        if (worldSelectionPos != null && soundTrigger && mining){
            int block = getBlock(worldSelectionPos.x, worldSelectionPos.y, worldSelectionPos.z);
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
        }

        if(!handSetUp){
            handSetUp = true;
        }

        //hand
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
            //block
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
            //item/tool
        } else if (getItemInInventorySlot(getPlayerInventorySelection(),0).definition.isItem){

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

        doubledHandAnimationPos.x = wieldHandAnimationPos.x + handInertia.x - (viewBobbing.x * 10f);
        doubledHandAnimationPos.y = wieldHandAnimationPos.y + handInertia.y + (viewBobbing.y * 10f);
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
        return new Vector3d(pos);
    }

    public static Vector3d getPlayerPosWithEyeHeight(){
        return new Vector3d(pos.x, pos.y + eyeHeight, pos.z);
    }


    public static Vector3d getPlayerPosWithViewBobbing(){
        Vector3d position = getPlayerPosWithEyeHeight();
        if (getCameraPerspective() == 0) {
            Vector3f cameraRotation = getCameraRotation();
            if (viewBobbing.z != 0) {
                position.x += (float) Math.sin(Math.toRadians(cameraRotation.y)) * -1.0f * viewBobbing.z;
                position.z += (float) Math.cos(Math.toRadians(cameraRotation.y)) * viewBobbing.z;
            }

            if (viewBobbing.x != 0) {
                position.x += (float) Math.sin(Math.toRadians(cameraRotation.y - 90f)) * -1.0f * viewBobbing.x;
                position.z += (float) Math.cos(Math.toRadians(cameraRotation.y - 90f)) * viewBobbing.x;
            }

            if (viewBobbing.y != 0) {
                position.y += viewBobbing.y;
            }
        }
        return position;
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

    private static Vector3d handInertia = new Vector3d(0,0,0);

    private static float oldYaw = 0;

    private static final float doublePi = (float)Math.PI * 2f;

    private static void updatePlayerHandInertia(){

        float delta = getDelta();

        float yaw = (float)Math.toRadians(getCameraRotation().y) + (float)Math.PI;

        float diff = yaw - oldYaw;

        //correct for radians overflow
        if (diff > Math.PI){
            diff -= doublePi;
        } else if (diff < -Math.PI){
            diff += doublePi;
        }

        handInertia.x -= diff;

        //limit
        if (handInertia.x < -5f){
            handInertia.x = -5f;
        } else if (handInertia.x > 5f){
            handInertia.x = 5f;
        }

        if (Math.abs(diff) < 0.01f) {
            //rubberband hand back to center
            if (handInertia.x > 0) {
                handInertia.x -= 20 * delta;
            } else if (handInertia.x < 0) {
                handInertia.x += 20 * delta;
            }

            //reset hand x
            if (Math.abs(handInertia.x) < 20f * delta){
                handInertia.x = 0f;
            }
        }

        float yDiff = (float)(oldRealPos.y - pos.y) * 10f;

        handInertia.y += yDiff;


        //limit
        if (handInertia.y < -2.5f){
            handInertia.y = -2.5f;
        } else if (handInertia.y > 2.5f){
            handInertia.y = 2.5f;
        }

        //rubberband hand back to center
        if (handInertia.y > 0) {
            handInertia.y -= 20 * delta;
        } else if (handInertia.y < 0) {
            handInertia.y += 20 * delta;
        }

        //reset hand y
        if (Math.abs(handInertia.y) < 20f * delta){
            handInertia.y = 0f;
        }

        oldYaw = yaw;
    }

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

    private static float diggingProgress = 0f;
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
        int cameraCheckBlock = getBlock((int)Math.floor(camPos.x),(int)Math.floor(camPos.y), (int)Math.floor(camPos.z));

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
        } else if (camRot >= 225f && camRot < 315f){
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
            rebuildMiningMesh(diggingFrame);
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



        //play sound when player lands on the ground
        if (onGround && !wasOnGround){
            playSound("dirt_" + (int)(Math.ceil(Math.random()*3)));
        }


        //falldamage
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
                int miningBlock = getBlock(worldSelectionPos.x, worldSelectionPos.y, worldSelectionPos.z);
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
                            Vector3d particlePos = new Vector3d(worldSelectionPos);
                            particlePos.y += 1.1f;
                            particlePos.z += Math.random();
                            particlePos.x += Math.random();

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
                            Vector3d particlePos = new Vector3d(worldSelectionPos);
                            particlePos.y -= 0.1f;
                            particlePos.z += Math.random();
                            particlePos.x += Math.random();

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
                            Vector3d particlePos = new Vector3d(worldSelectionPos);
                            particlePos.z += 1.1f;
                            particlePos.x += Math.random();
                            particlePos.y += Math.random();

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
                            Vector3d particlePos = new Vector3d(worldSelectionPos);
                            particlePos.z -= 0.1f;
                            particlePos.x += Math.random();
                            particlePos.y += Math.random();

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
        oldRealPos = new Vector3d(pos);
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
        }
    }

    private static boolean xPositive = true;
    private static float xBobPos = 0;
    private static float yBobPos = 0;

    private static void applyViewBobbing() {

        float delta = getDelta();

        float viewBobbingAddition = delta  * 250f;

        //System.out.println(viewBobbingAddition);

        if (running){
            viewBobbingAddition = delta * 290f;
        }

        if (xPositive) {
            xBobPos += viewBobbingAddition;
            if (xBobPos >= 50f){
                xBobPos = 50f;
                xPositive = false;
                playSound("dirt_" + (int)(Math.ceil(Math.random()*3)));
            }
        } else {
            xBobPos -= viewBobbingAddition;
            if (xBobPos <= -50f){
                xBobPos = -50f;
                xPositive = true;
                playSound("dirt_"  + (int)(Math.ceil(Math.random()*3)));
            }
        }

        yBobPos = Math.abs(xBobPos);

        viewBobbing.x = xBobPos/700f;
        viewBobbing.y = yBobPos/800f;
    }

    private static void returnPlayerViewBobbing(){

        float delta = getDelta();

        if ((Math.abs(xBobPos)) <= 300 * delta){
            xBobPos = 0;
        }

        if (xBobPos > 0){
            xBobPos -= 300 * delta;
        } else if (xBobPos < 0){
            xBobPos += 300 * delta;
        }

        yBobPos = Math.abs(xBobPos);

        viewBobbing.x = xBobPos/700f;
        viewBobbing.y = yBobPos/800f;
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


    public static float getRunningFOVAdder(){
        return runningFOVAdder;
    }

    private static void calculateRunningFOV(){
        float delta = getDelta();
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
        float delta = getDelta();

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

    public static void hurtPlayer(int hurt){
        health -= hurt;
        playSound("hurt", true);
        calculateHealthBarElements();
        //doHurtRotation = true;
    }
}
