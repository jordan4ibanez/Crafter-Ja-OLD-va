package game.player;

import engine.Controls;
import engine.graphics.Camera;
import engine.gui.GUILogic;
import engine.time.Delta;
import game.blocks.BlockDefinitionContainer;
import game.chunk.Chunk;
import game.clouds.Cloud;
import game.crafting.Inventory;
import game.crafting.InventoryLogic;
import game.entity.collision.Collision;
import game.item.ItemDefinitionContainer;
import game.ray.Ray;
import org.joml.*;

import java.lang.Math;

import static org.joml.Math.floor;

public class Player {
    BlockDefinitionContainer blockDefinitionContainer = new BlockDefinitionContainer();
    private GUILogic guiLogic;
    private InventoryLogic inventoryLogic;
    private Collision collision;
    private Controls controls;
    private Camera camera;
    private Delta delta;
    private Chunk chunk;
    private Ray ray;
    private WieldHand wieldHand;
    private Movement movement;
    private ViewBobbing viewBobbing;
    private Cloud cloud;

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

    public void setRay(Ray ray) {
        if (this.ray == null) {
            this.ray = ray;
        }
    }
    public void setCamera(Camera camera){
        if (this.camera == null){
            this.camera = camera;
        }
    }
    public void setControls(Controls controls){
        if (this.controls == null){
            this.controls = controls;
        }
    }
    public void setCollision(Collision collision){
        if (this.collision == null){
            this.collision = collision;
        }
    }
    public void setInventoryLogic(InventoryLogic inventoryLogic){
        if (this.inventoryLogic == null){
            this.inventoryLogic = inventoryLogic;
        }
    }
    public void setCloud(Cloud cloud){
        if (this.cloud == null){
            this.cloud = cloud;
        }
    }
    public void setGuiLogic(GUILogic guiLogic){
        if (this.guiLogic == null){
            this.guiLogic = guiLogic;
        }
    }

    public void initialize(Inventory inventory, Camera camera, ItemDefinitionContainer itemDefinitionContainer) {
        this.wieldHand = new WieldHand(this, delta, inventory, camera);
        this.viewBobbing = new ViewBobbing(this, delta);
        this.wieldHand.setChunk(chunk);
        this.wieldHand.setItemDefinitionContainer(itemDefinitionContainer);
        this.movement = new Movement();
    }


    public WieldHand getWieldHand(){
        return wieldHand;
    }

    public Player(){

    }

    private final Vector3d posWithEyeHeightViewBobbing = new Vector3d();


    private boolean atCraftingBench;

    private boolean inventoryOpen;

    private float runningFOVAdder = 0f;

    private int health = 20;

    private final Vector3i newFlooredPos = new Vector3i();

    private boolean mining                = false;
    private boolean placing               = false;
    private float placeTimer              = 0;
    private String name                   = "";
    private int currentInventorySelection = 0;
    private int oldInventorySelection = 0;
    private final Vector3i oldWorldSelectionPos = new Vector3i();
    private final Vector3i worldSelectionPos    = new Vector3i();
    private float sneakOffset = 0;
    private final Vector3d particlePos = new Vector3d(worldSelectionPos);
    private final Vector3f particleInertia = new Vector3f();
    private boolean cameraSubmerged = false;

    //this is like this because working with x and z is easier than x and y
    private final Vector2i currentChunk = new Vector2i();

    private float lightCheckTimer = 0f;
    private byte lightLevel = 0;
    private final Vector3i oldFlooredPos = new Vector3i(0,0,0);

    //block hardness
    private float stoneHardness = 0f;
    private float dirtHardness = 0f;
    private float woodHardness = 0f;
    private float leafHardness = 0f;

    //tool mining level
    private float stoneMiningLevel = 0.3f;
    private float dirtMiningLevel = 1f;
    private float woodMiningLevel = 1f;
    private float leafMiningLevel = 1f;

    //digging stuff
    private float diggingProgress = 0f;
    private byte diggingFrame = -1;
    private boolean hasDug = false;
    private float particleBufferTimer = 0f;
    private byte currentRotDir = 0;

    private final Vector2f inertiaWorker = new Vector2f();
    private float healthTimer = 0;




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

    public void startDiggingAnimation(){
        wieldHand.startDiggingAnimation();
    }

    public float getHurtCameraRotation(){
        return (float) 0;
    }


    public Vector2i getPlayerCurrentChunk(){
        return currentChunk;
    }

    public float getSneakOffset(){
        return sneakOffset / 900f;
    }

    public void setPlayerWorldSelectionPos(Vector3i thePos){

        oldWorldSelectionPos.set(worldSelectionPos.x,worldSelectionPos.y,worldSelectionPos.z);

        //pointing at a block
        if (thePos != null){
            byte block = chunk.getBlock(new Vector3i(thePos.x, thePos.y,thePos.z));
            //add in block hardness levels
            if (block > 0){
                stoneHardness = blockDefinitionContainer.getStoneHardness(block);
                dirtHardness  = blockDefinitionContainer.getDirtHardness(block);
                woodHardness  = blockDefinitionContainer.getWoodHardness(block);
                leafHardness  = blockDefinitionContainer.getLeafHardness(block);
            }
            //reset when not pointing at any block
            else {
                stoneHardness = -1;
                dirtHardness  = -1;
                woodHardness  = -1;
                leafHardness  = -1;
            }

            worldSelectionPos.set(thePos.x, thePos.y,thePos.z);
        } else {
            worldSelectionPos.set(0,-1,0);
        }
    }


    public byte getPlayerLightLevel(){
        return lightLevel;
    }

    public void setPlayerWorldSelectionPos(int x, int y, int z){
        oldWorldSelectionPos.set(worldSelectionPos.x,worldSelectionPos.y,worldSelectionPos.z);
        worldSelectionPos.set(x,y,z);

        //pointing at a block
        byte block = chunk.getBlock(new Vector3i(x, y, z));
        //add in block hardness levels
        if (block > 0){
            stoneHardness = blockDefinitionContainer.getStoneHardness(block);
            dirtHardness  = blockDefinitionContainer.getDirtHardness(block);
            woodHardness  = blockDefinitionContainer.getWoodHardness(block);
            leafHardness  = blockDefinitionContainer.getLeafHardness(block);
        }
        //reset when not pointing at any block
        else {
            stoneHardness = -1;
            dirtHardness = -1;
            woodHardness = -1;
            leafHardness = -1;
        }
    }

    public Vector3i getPlayerWorldSelectionPos(){
        return worldSelectionPos;
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

    public boolean isAtCraftingBench(){
        return this.atCraftingBench;
    }

    public void setAtCraftingBench(boolean isAtCraft){
        this.atCraftingBench = isAtCraft;
    }

    public boolean isInventoryOpen(){
        return this.inventoryOpen;
    }

    public void setInventoryOpen(boolean open){
        this.inventoryOpen = open;
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

    public boolean getPlayerPlacing() {
        return placing;
    }

    public Vector3f getPlayerViewBobbing(){
        return viewBobbing.getPlayerViewBobbing();
    }

    private void applyCameraViewBobbingOffset(){
        posWithEyeHeightViewBobbing.set(movement.getPosEyeHeight());

        if (camera.getCameraPerspective() == 0) {
            float cameraRotationY = camera.getCameraRotation().y;

            float viewBobbingZ = viewBobbing.getPlayerViewBobbing().z;
            if (viewBobbingZ != 0) {
                //direct object modification
                posWithEyeHeightViewBobbing.x += (float) Math.sin(Math.toRadians(cameraRotationY)) * -1.0f * viewBobbingZ;
                posWithEyeHeightViewBobbing.z += (float) Math.cos(Math.toRadians(cameraRotationY)) * viewBobbingZ;
            }

            float viewBobbingX = viewBobbing.getPlayerViewBobbing().x;
            if (viewBobbingX != 0) {
                posWithEyeHeightViewBobbing.x += (float) Math.sin(Math.toRadians(cameraRotationY - 90f)) * -1.0f * viewBobbingX;
                posWithEyeHeightViewBobbing.z += (float) Math.cos(Math.toRadians(cameraRotationY - 90f)) * viewBobbingX;
            }

            float viewBobbingY = viewBobbing.getPlayerViewBobbing().y;
            if (viewBobbingY != 0) {
                posWithEyeHeightViewBobbing.y += viewBobbingY;
            }
        }
    }

    public void addInertia(Vector3f inertia){
        movement.addInertia(inertia);
    }

    public float getWidth(){
        return movement.getWidth();
    }

    public float getHeight(){
        return movement.getHeight();
    }

    public Vector3d getOldPos(){
        return movement.getOldPos();
    }

    public Vector3d getPos(){
        return movement.getPos();
    }

    public void setPos(Vector3d pos){
        movement.setPos(pos);
    }

    public Vector3d getPlayerPosWithEyeHeight(){
        return movement.getPosEyeHeight();
    }

    public Vector3d getPlayerPosWithViewBobbing(){
        return posWithEyeHeightViewBobbing;
    }


    public Boolean isOnGround(){
        return movement.getOnGround();
    }


    public boolean playerHasDug(){
        return hasDug;
    }

    public byte getDiggingFrame(){
        return diggingFrame;
    }



    public byte getPlayerDir(){
        return currentRotDir;
    }

    public void playerOnTick() {

        double delta = this.delta.getDelta();

        movement.onTick(chunk, camera, controls, this, this.delta, collision);

        wieldHand.onTick();


        updateWorldChunkLoader();

        //camera underwater effect trigger
        byte cameraCheckBlock = chunk.getBlock(new Vector3i((int)floor(camera.getCameraPosition().x),(int)floor(camera.getCameraPosition().y - 0.02d), (int)floor(camera.getCameraPosition().z)));

        cameraSubmerged = cameraCheckBlock > 0 && blockDefinitionContainer.isLiquid(cameraCheckBlock);

        //the player comes to equilibrium with the water's surface
        //if this is not implemented like this


        float camRot = camera.getCameraRotation().y + 180f;

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

        //apply player's body animation
        //applyPlayerBodyAnimation();

        //updatePlayerHandInertia();

        //this creates the view bobbing internal calculation offset in the ViewBobbing class
        //this is only creating the offsets that will be applied

        /*
        if(onGround && !sneaking && !inWater){
            applyViewBobbing();
        } else {
            returnPlayerViewBobbing();
        }
         */

        //apply view bobbing offset to camera position literal form (Vector3D)
        applyCameraViewBobbingOffset();


        //sneaking offset
        if (movement.getSneaking()){
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
                byte miningBlock = chunk.getBlock(worldSelectionPos);
                switch (randomDir) {
                    case 0 -> {
                        block = chunk.getBlock(new Vector3i(worldSelectionPos.x + 1, worldSelectionPos.y, worldSelectionPos.z));
                        if (block == 0) {
                            particlePos.set(worldSelectionPos.x,worldSelectionPos.y,worldSelectionPos.z);
                            particlePos.x += 1.1f;
                            particlePos.z += Math.random();
                            particlePos.y += Math.random();

                            particleInertia.set(0,0,0);
                            particleInertia.x = (float) Math.random() * 2f;
                            particleInertia.y = (float) Math.random() * 2f;
                            particleInertia.z = (float) (Math.random() - 0.5f) * 2f;

                            //createParticle(particlePos.x, particlePos.y ,particlePos.z, particleInertia.x, particleInertia.y, particleInertia.z, miningBlock);
                        }
                    }
                    case 1 -> {
                        block = chunk.getBlock(new Vector3i(worldSelectionPos.x - 1, worldSelectionPos.y, worldSelectionPos.z));
                        if (block == 0) {
                            particlePos.set(worldSelectionPos.x,worldSelectionPos.y,worldSelectionPos.z);
                            particlePos.x -= 0.1f;
                            particlePos.z += Math.random();
                            particlePos.y += Math.random();

                            particleInertia.set(0,0,0);
                            particleInertia.x = (float) Math.random() * -2f;
                            particleInertia.y = (float) Math.random() * 2f;
                            particleInertia.z = (float) (Math.random() - 0.5f) * 2f;

                            //createParticle(particlePos.x, particlePos.y ,particlePos.z, particleInertia.x, particleInertia.y, particleInertia.z, miningBlock);
                        }
                    }
                    case 2 -> {
                        block = chunk.getBlock(new Vector3i(worldSelectionPos.x, worldSelectionPos.y + 1, worldSelectionPos.z));
                        if (block == 0) {
                            particlePos.set(worldSelectionPos.x,worldSelectionPos.y,worldSelectionPos.z);
                            particlePos.y += 1.1f;
                            particlePos.z += Math.random();
                            particlePos.x += Math.random();

                            particleInertia.set(0,0,0);
                            particleInertia.x = (float) (Math.random() - 0.5f) * 2f;
                            particleInertia.y = (float) Math.random() * 2f;
                            particleInertia.z = (float) (Math.random() - 0.5f) * 2f;

                            //createParticle(particlePos.x, particlePos.y ,particlePos.z, particleInertia.x, particleInertia.y, particleInertia.z, miningBlock);
                        }
                    }
                    case 3 -> {
                        block = chunk.getBlock(new Vector3i(worldSelectionPos.x, worldSelectionPos.y - 1, worldSelectionPos.z));
                        if (block == 0) {
                            particlePos.set(worldSelectionPos.x,worldSelectionPos.y,worldSelectionPos.z);
                            particlePos.y -= 0.1f;
                            particlePos.z += Math.random();
                            particlePos.x += Math.random();

                            particleInertia.set(0,0,0);
                            particleInertia.x = (float) (Math.random() - 0.5f) * 2f;
                            particleInertia.y = (float) Math.random() * -1f;
                            particleInertia.z = (float) (Math.random() - 0.5f) * 2f;

                            //createParticle(particlePos.x, particlePos.y ,particlePos.z, particleInertia.x, particleInertia.y, particleInertia.z, miningBlock);
                        }
                    }
                    case 4 -> {
                        block = chunk.getBlock(new Vector3i(worldSelectionPos.x, worldSelectionPos.y, worldSelectionPos.z + 1));
                        if (block == 0) {
                            particlePos.set(worldSelectionPos.x,worldSelectionPos.y,worldSelectionPos.z);
                            particlePos.z += 1.1f;
                            particlePos.x += Math.random();
                            particlePos.y += Math.random();

                            particleInertia.set(0,0,0);
                            particleInertia.z = (float) Math.random() * 2f;
                            particleInertia.y = (float) Math.random() * 2f;
                            particleInertia.x = (float) (Math.random() - 0.5f) * 2f;

                            //createParticle(particlePos.x, particlePos.y ,particlePos.z, particleInertia.x, particleInertia.y, particleInertia.z, miningBlock);
                        }
                    }
                    case 5 -> {
                        block = chunk.getBlock(new Vector3i(worldSelectionPos.x, worldSelectionPos.y, worldSelectionPos.z - 1));
                        if (block == 0) {
                            particlePos.set(worldSelectionPos.x,worldSelectionPos.y,worldSelectionPos.z);
                            particlePos.z -= 0.1f;
                            particlePos.x += Math.random();
                            particlePos.y += Math.random();

                            particleInertia.set(0,0,0);
                            particleInertia.z = (float) Math.random() * -2f;
                            particleInertia.y = (float) Math.random() * 2f;
                            particleInertia.x = (float) (Math.random() - 0.5f) * 2f;

                            //createParticle(particlePos.x, particlePos.y ,particlePos.z, particleInertia.x, particleInertia.y, particleInertia.z, miningBlock);
                        }
                    }
                }
                particleBufferTimer = 0f;
            }
        }

        calculateRunningFOV();

        float reach = 3.575f;
        if (camera.getCameraPerspective() < 2) {
            if (mining && hasDug) {
                ray.playerRayCast(posWithEyeHeightViewBobbing, camera.getCameraRotationVector(), reach, true, false, true);
            } else if (mining) {
                ray.playerRayCast(posWithEyeHeightViewBobbing, camera.getCameraRotationVector(), reach, true, false, false);
            } else if (placing && placeTimer <= 0) {
                ray.playerRayCast(posWithEyeHeightViewBobbing, camera.getCameraRotationVector(), reach, false, true, false);
                placeTimer = 0.25f; // every quarter second you can place
            } else {
                ray.playerRayCast(posWithEyeHeightViewBobbing, camera.getCameraRotationVector(), reach, false, false, false);
            }
        } else {
            if (mining && hasDug) {
                ray.playerRayCast(posWithEyeHeightViewBobbing, camera.getInvertedCameraRotationVector(), reach, true, false, true);
            } else if (mining) {
                ray.playerRayCast(posWithEyeHeightViewBobbing, camera.getInvertedCameraRotationVector(), reach, true, false, false);
            } else if (placing && placeTimer <= 0) {
                ray.playerRayCast(posWithEyeHeightViewBobbing, camera.getInvertedCameraRotationVector(), reach, false, true, false);
                placeTimer = 0.25f; // every quarter second you can place
            } else {
                ray.playerRayCast(posWithEyeHeightViewBobbing, camera.getInvertedCameraRotationVector(), reach, false, false, false);
            }
        }

        /*
        if (health <= 6) {
            makeHeartsJiggle();
        }
         */

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

        newFlooredPos.set((int)floor(camera.getCameraPosition().x), (int)floor(camera.getCameraPosition().y), (int)floor(camera.getCameraPosition().z));

        //System.out.println(lightCheckTimer);
        if (lightCheckTimer >= 0.25f || !newFlooredPos.equals(oldFlooredPos)){
            lightCheckTimer = 0f;
            lightLevel = chunk.getLight(newFlooredPos.x, newFlooredPos.y, newFlooredPos.z);
        }

        //do the same for the literal wield inventory
        //this.inventoryLogic.getInventory().updateWieldInventory(lightLevel);

        oldFlooredPos.set(newFlooredPos);
        oldInventorySelection = currentInventorySelection;
    }

    private void updateWorldChunkLoader(){
        int newChunkX = (int)floor(movement.getPos().x / 16f);
        int newChunkZ = (int)floor(movement.getPos().z / 16f);

        if (newChunkX != currentChunk.x || newChunkZ != currentChunk.y) {
            currentChunk.x = newChunkX;
            currentChunk.y = newChunkZ;
            chunk.generateNewChunks();
            cloud.setCloudPos(newChunkX,newChunkZ);
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
    }


    public float getRunningFOVAdder(){
        return runningFOVAdder;
    }

    private void calculateRunningFOV(){
        double delta = this.delta.getDelta();

        if (playerIsMoving() && movement.getRunning()){
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

    private boolean playerIsMoving(){
        return controls.getBackward() || controls.getForward() || controls.getLeft() || controls.getRight();
    }


    private void doHealthTest(){
        double delta = this.delta.getDelta();

        healthTimer += delta;

        if (healthTimer >= 1f){
            healthTimer = 0;

            health -= 1;

            if (health < 0){
                health = 20;
            }

            //System.out.println("The player's health is: " + health);
            guiLogic.calculateHealthBarElements();
        }
    }

    public boolean isRunning(){
        return movement.getRunning();
    }

    public void setPlayerHealth(int newHealth){
        health = newHealth;
        //calculateHealthBarElements();
    }

    public void hurtPlayer(int hurt){
        health -= hurt;
        //playSound("hurt", true);
        //calculateHealthBarElements();
        //doHurtRotation = true;
    }

    public boolean isJumping() {
        return movement.getJumping();
    }

    public void setInWater(boolean inWater) {
        movement.setInWater(inWater);
    }
}
