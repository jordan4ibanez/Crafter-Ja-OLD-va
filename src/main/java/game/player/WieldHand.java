package game.player;

import engine.graphics.Camera;
import engine.time.Delta;
import game.chunk.Chunk;
import game.crafting.Inventory;
import game.item.ItemDefinitionContainer;
import org.joml.*;
import org.joml.Math;

import static java.lang.Math.PI;

public class WieldHand {
    private ItemDefinitionContainer itemDefinitionContainer;
    private final Player player;
    private Chunk chunk;
    private final Delta delta;
    private final Inventory inventory;
    private final Camera camera;

    public WieldHand(Player player, Delta delta, Inventory inventory, Camera camera){
        this.player = player;
        this.delta = delta;
        this.inventory = inventory;
        this.camera = camera;
    }

    public void setChunk(Chunk chunk){
        if (this.chunk == null){
            this.chunk = chunk;
        }
    }

    public void setItemDefinitionContainer(ItemDefinitionContainer itemDefinitionContainer){
        if (this.itemDefinitionContainer == null){
            this.itemDefinitionContainer = itemDefinitionContainer;
        }
    }

    //z is distance from camera - negative is further
    //x - horizontal
    //y - vertical
    //These are the base positions of holding different types of items
    private final Vector3f wieldHandAnimationPosEmpty = new Vector3f(14, -20, -16f);
    private final Vector3f wieldHandAnimationPosBlock = new Vector3f(12, -16, -14f);
    private final Vector3f wieldHandAnimationPosItem = new Vector3f(9, -8, -7f);

    //this is the animation beginning and ending
    private final Vector3f wieldRotationEmptyBegin = radianVector3f(135, 75, 20); //empty
    private final Vector3f wieldRotationEmptyEnd = radianVector3f(110, 75, -20);

    private final Vector3f wieldRotationBlockBegin = radianVector3f(0f, 45f, 0f); //block
    private final Vector3f wieldRotationBlockEnd   = radianVector3f(-75f, 45f, 0f);

    private final Vector3f wieldRotationItemBegin = radianVector3f(-30f, -75, 0f); //item/tool
    private final Vector3f wieldRotationItemEnd   = radianVector3f(-70, -75, 0f);

    //These are the actual realtime values of where the hand is
    private final Vector3f wieldHandAnimationPos = new Vector3f(0, 0, 0);
    private final Vector3f wieldHandAnimationRot = new Vector3f(0, 0, 0);

    private final Vector3d doubledHandAnimationPos = new Vector3d();

    private float diggingAnimation = 0f;

    private final Vector3d handInertia = new Vector3d(0,0,0);


    private boolean diggingAnimationGo = false;
    private boolean diggingAnimationBuffer = false;
    private boolean handSetUp = false;

    private float oldYaw = 0;

    public void resetWieldHandSetupTrigger(){
        handSetUp = false;
    }
    private boolean soundTrigger = true;

    public void testPlayerDiggingAnimation(){
        if (!diggingAnimationGo && handSetUp && diggingAnimation == 0f){
            return;
        }

        //this is the sound trigger that makes the mining noise
        if (player.getPlayerWorldSelectionPos() != null && soundTrigger && player.getPlayerMining()){
            byte block = chunk.getBlock(player.getPlayerWorldSelectionPos());
            if (block > 0){
                /*
                String digSound = getDigSound(block);
                if (!digSound.equals("")) {
                    playSound(digSound);
                    soundTrigger = false;
                }
                 */
            }
        }

        if (handSetUp) {
            diggingAnimation += delta.getDelta() * 3.75f;
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
        if (inventory.getMain().getItem(player.getPlayerInventorySelection(),0) == null) {
            //set position
            wieldHandAnimationPos.set((float) (-5f * Math.sin(java.lang.Math.pow(diggingAnimation, 0.8f) * Math.PI)) + wieldHandAnimationPosEmpty.x, (float) (7f * Math.sin(diggingAnimation * 2f * Math.PI)) + wieldHandAnimationPosEmpty.y, wieldHandAnimationPosEmpty.z);
            //set rotation
            wieldHandAnimationRot.set(wieldRotationEmptyBegin);
            //linear interpolate rotation in a circle
            wieldHandAnimationRot.lerp(wieldRotationEmptyEnd, (float) Math.sin(diggingAnimation * Math.PI));
        }
        //block
        else if (itemDefinitionContainer.isBlock(inventory.getMain().getItem(player.getPlayerInventorySelection(),0))) {
            //set position
            wieldHandAnimationPos.set((float) (-5f * Math.sin(java.lang.Math.pow(diggingAnimation, 0.8f) * Math.PI)) + wieldHandAnimationPosBlock.x, (float) (2f * Math.sin(diggingAnimation * 2f * Math.PI)) + wieldHandAnimationPosBlock.y, wieldHandAnimationPosBlock.z);
            //set rotation
            wieldHandAnimationRot.set(wieldRotationBlockBegin);
            //linear interpolate
            wieldHandAnimationRot.lerp(wieldRotationBlockEnd, (float) Math.sin(diggingAnimation * Math.PI));

            //item/tool
        } else if (itemDefinitionContainer.isItem(inventory.getMain().getItem(player.getPlayerInventorySelection(),0))){
            //set position
            wieldHandAnimationPos.set((float) (-6f * Math.sin(java.lang.Math.pow(diggingAnimation, 0.8f) * Math.PI)) + wieldHandAnimationPosItem.x, (float) (4f * Math.sin(diggingAnimation * 2f * Math.PI)) + wieldHandAnimationPosItem.y, wieldHandAnimationPosItem.z);
            //set rotation
            wieldHandAnimationRot.set(wieldRotationItemBegin);
            //linear interpolate
            wieldHandAnimationRot.lerp(wieldRotationItemEnd, (float) Math.sin(diggingAnimation * Math.PI));
        }
    }


    //mutable - be careful with this
    public Vector3d getWieldHandAnimationPos(){
        return doubledHandAnimationPos.set(wieldHandAnimationPos.x + handInertia.x - (player.getPlayerViewBobbing().x * 10f),wieldHandAnimationPos.y + handInertia.y + (player.getPlayerViewBobbing().y * 10f),wieldHandAnimationPos.z);
    }
    //immutable
    public double getWieldHandAnimationPosX(){
        return wieldHandAnimationPos.x + handInertia.x - (player.getPlayerViewBobbing().x * 10f);
    }
    //immutable
    public double getWieldHandAnimationPosY(){
        return wieldHandAnimationPos.y + handInertia.y + (player.getPlayerViewBobbing().y * 10f);
    }
    //immutable
    public double getWieldHandAnimationPosZ(){
        return wieldHandAnimationPos.z;
    }

    public Vector3f getWieldHandAnimationRot(){
        return wieldHandAnimationRot;
    }


    public void startDiggingAnimation(){
        diggingAnimationGo = true;
        diggingAnimationBuffer = true;
    }


    public void onTick(){
        testPlayerDiggingAnimation();
        updatePlayerHandInertia();
    }

    public void updatePlayerHandInertia(){

        double delta = this.delta.getDelta();

        float yaw = Math.toRadians(camera.getCameraRotation().y) + (float)Math.PI;

        float diff = yaw - oldYaw;

        //correct for radians overflow
        float doublePi = (float) PI * 2f;
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

        float yDiff = (float)(player.getOldRealPos().y - player.getPlayerPos().y) * 10f;

        handInertia.y += yDiff;


        //limit
        if (handInertia.y < -2.5f){
            handInertia.y = -2.5f;
        } else if (handInertia.y > 2.5f){
            handInertia.y = 2.5f;
        }

        //rubber band hand back to center
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

    //a quick auto converter for laziness sake
    private Vector3f radianVector3f(float angleX, float angleY, float angleZ){
        return new Vector3f(Math.toRadians(angleX), Math.toRadians(angleY), Math.toRadians(angleZ));
    }
}
