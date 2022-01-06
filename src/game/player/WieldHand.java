package game.player;

import org.joml.*;
import org.joml.Math;

import static engine.graphics.Camera.getCameraRotation;
import static engine.sound.SoundAPI.playSound;
import static engine.time.Time.getDelta;
import static game.blocks.BlockDefinition.getDigSound;
import static game.chunk.Chunk.getBlock;
import static game.crafting.Inventory.getItemInInventorySlot;
import static game.item.ItemDefinition.getIfItem;
import static game.item.ItemDefinition.itemIsBlock;
import static game.player.Player.*;
import static game.player.ViewBobbing.getPlayerViewBobbing;

public class WieldHand {

    //z is distance from camera - negative is further
    //x - horizontal
    //y - vertical
    //These are the base positions of holding different types of items
    private static final Vector3f wieldHandAnimationPosEmpty = new Vector3f(14, -20, -16f);
    private static final Vector3f wieldHandAnimationPosBlock = new Vector3f(12, -16, -14f);
    private static final Vector3f wieldHandAnimationPosItem = new Vector3f(9, -8, -7f);

    //this is the animation beginning and ending
    private static final Vector3f wieldRotationEmptyBegin = radianVector3f(135, 75, 20); //empty
    private static final Vector3f wieldRotationEmptyEnd = radianVector3f(110, 75, -20);

    private static final Vector3f wieldRotationBlockBegin = radianVector3f(0f, 45f, 0f); //block
    private static final Vector3f wieldRotationBlockEnd   = radianVector3f(-75f, 45f, 0f);

    private static final Vector3f wieldRotationItemBegin = radianVector3f(-30f, -75, 0f); //item/tool
    private static final Vector3f wieldRotationItemEnd   = radianVector3f(-70, -75, 0f);

    //These are the actual realtime values of where the hand is
    private static final Vector3f wieldHandAnimationPos = new Vector3f(0, 0, 0);
    private static final Vector3f wieldHandAnimationRot = new Vector3f(0, 0, 0);

    private static final Vector3d doubledHandAnimationPos = new Vector3d();

    private static float diggingAnimation = 0f;

    private static final Vector3d handInertia = new Vector3d(0,0,0);


    private static boolean diggingAnimationGo = false;
    private static boolean diggingAnimationBuffer = false;
    private static boolean handSetUp = false;

    private static float oldYaw = 0;

    private static final float doublePi = (float)Math.PI * 2f;

    public static void resetWieldHandSetupTrigger(){
        handSetUp = false;
    }
    private static boolean soundTrigger = true;

    public static void testPlayerDiggingAnimation(){
        if (!diggingAnimationGo && handSetUp && diggingAnimation == 0f){
            return;
        }

        //this is the sound trigger that makes the mining noise
        if (getPlayerWorldSelectionPos() != null && soundTrigger && getPlayerMining()){
            byte block = getBlock(getPlayerWorldSelectionPos().x, getPlayerWorldSelectionPos().y, getPlayerWorldSelectionPos().z);
            if (block > 0){
                String digSound = getDigSound(block);
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
            //set position
            wieldHandAnimationPos.set((float) (-5f * Math.sin(java.lang.Math.pow(diggingAnimation, 0.8f) * Math.PI)) + wieldHandAnimationPosEmpty.x, (float) (7f * Math.sin(diggingAnimation * 2f * Math.PI)) + wieldHandAnimationPosEmpty.y, wieldHandAnimationPosEmpty.z);
            //set rotation
            wieldHandAnimationRot.set(wieldRotationEmptyBegin);
            //linear interpolate rotation in a circle
            wieldHandAnimationRot.lerp(wieldRotationEmptyEnd, (float) Math.sin(diggingAnimation * Math.PI));
        }
        //block
        else if (itemIsBlock(getItemInInventorySlot(getPlayerInventorySelection(),0))) {
            //set position
            wieldHandAnimationPos.set((float) (-5f * Math.sin(java.lang.Math.pow(diggingAnimation, 0.8f) * Math.PI)) + wieldHandAnimationPosBlock.x, (float) (2f * Math.sin(diggingAnimation * 2f * Math.PI)) + wieldHandAnimationPosBlock.y, wieldHandAnimationPosBlock.z);
            //set rotation
            wieldHandAnimationRot.set(wieldRotationBlockBegin);
            //linear interpolate
            wieldHandAnimationRot.lerp(wieldRotationBlockEnd, (float) Math.sin(diggingAnimation * Math.PI));

            //item/tool
        } else if (getIfItem(getItemInInventorySlot(getPlayerInventorySelection(),0))){
            //set position
            wieldHandAnimationPos.set((float) (-6f * Math.sin(java.lang.Math.pow(diggingAnimation, 0.8f) * Math.PI)) + wieldHandAnimationPosItem.x, (float) (4f * Math.sin(diggingAnimation * 2f * Math.PI)) + wieldHandAnimationPosItem.y, wieldHandAnimationPosItem.z);
            //set rotation
            wieldHandAnimationRot.set(wieldRotationItemBegin);
            //linear interpolate
            wieldHandAnimationRot.lerp(wieldRotationItemEnd, (float) Math.sin(diggingAnimation * Math.PI));
        }
    }


    //mutable - be careful with this
    public static Vector3d getWieldHandAnimationPos(){
        return doubledHandAnimationPos.set(wieldHandAnimationPos.x + handInertia.x - (getPlayerViewBobbing().x * 10f),wieldHandAnimationPos.y + handInertia.y + (getPlayerViewBobbing().y * 10f),wieldHandAnimationPos.z);
    }
    //immutable
    public static double getWieldHandAnimationPosX(){
        return wieldHandAnimationPos.x + handInertia.x - (getPlayerViewBobbing().x * 10f);
    }
    //immutable
    public static double getWieldHandAnimationPosY(){
        return wieldHandAnimationPos.y + handInertia.y + (getPlayerViewBobbing().y * 10f);
    }
    //immutable
    public static double getWieldHandAnimationPosZ(){
        return wieldHandAnimationPos.z;
    }

    //mutable - be careful with this
    public static Vector3f getWieldHandAnimationRot(){
        return wieldHandAnimationRot;
    }
    //immutable
    public static float getWieldHandAnimationRotX(){
        return wieldHandAnimationRot.x;
    }
    //immutable
    public static float getWieldHandAnimationRotY(){
        return wieldHandAnimationRot.y;
    }
    //immutable
    public static float getWieldHandAnimationRotZ(){
        return wieldHandAnimationRot.z;
    }



    public static void startDiggingAnimation(){
        diggingAnimationGo = true;
        diggingAnimationBuffer = true;
    }


    public static void updatePlayerHandInertia(){

        double delta = getDelta();

        float yaw = Math.toRadians(getCameraRotation().y) + (float)Math.PI;

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

        float yDiff = (float)(getOldRealPos().y - getPlayerPos().y) * 10f;

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
    private static Vector3f radianVector3f(float angleX, float angleY, float angleZ){
        return new Vector3f(Math.toRadians(angleX), Math.toRadians(angleY), Math.toRadians(angleZ));
    }
}
