package game.player;

import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.graphics.Camera.getCameraRotation;
import static engine.sound.SoundAPI.playSound;
import static engine.time.Time.getDelta;
import static game.blocks.BlockDefinition.getDigSound;
import static game.chunk.Chunk.getBlock;
import static game.crafting.Inventory.getItemInInventorySlot;
import static game.player.Player.*;

public class WieldHand {

    //TODO --- begin wield hand stuff!
    private static final Vector3f wieldHandAnimationPosBaseEmpty = new Vector3f(13, -15, -14f);
    private static final Vector3f wieldHandAnimationPosBaseItem = new Vector3f(13, -15, -14f);

    private static final Vector3f wieldRotationEmptyBegin = new Vector3f(Math.toRadians(30f), 0f, Math.toRadians(-10f));
    private static final Vector3f wieldRotationEmptyEnd   = new Vector3f(Math.toRadians(40f), Math.toRadians(20f), Math.toRadians(20f));

    private static final Vector3f wieldRotationItemBegin = new Vector3f(Math.toRadians(0f), Math.toRadians(45f), Math.toRadians(0f));
    private static final Vector3f wieldRotationItemEnd   = new Vector3f(Math.toRadians(90f), Math.toRadians(45f), Math.toRadians(0f));

    private static final Vector3f wieldHandAnimationPos = new Vector3f(0, 0, 0);
    private static final Vector3f wieldHandAnimationRot = new Vector3f(0, 0, 0);

    private static final Vector3f wieldHandAnimationRotBegin = new Vector3f();
    private static final Vector3f wieldHandAnimationRotEnd = new Vector3f();

    private static final Vector3f wieldHandAnimationPosBaseTool = new Vector3f();

    private static final Quaternionf quatBegin = new Quaternionf();
    private static final Quaternionf quatEnd = new Quaternionf();

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
            wieldHandAnimationPos.set((float) (-5f * Math.sin(java.lang.Math.pow(diggingAnimation, 0.8f) * Math.PI)) + wieldHandAnimationPosBaseEmpty.x, (float) (5f * Math.sin(diggingAnimation * 2f * Math.PI)) + wieldHandAnimationPosBaseEmpty.y,wieldHandAnimationPosBaseEmpty.z);

            wieldHandAnimationRot.x = 180f;

            quatBegin.set(wieldRotationEmptyBegin.x, wieldRotationEmptyBegin.y, wieldRotationEmptyBegin.z,0);

            quatEnd.set(wieldRotationEmptyEnd.x, wieldRotationEmptyEnd.y, wieldRotationEmptyEnd.z, 0)
                    .set(quatBegin.slerp(quatEnd, (float) Math.sin(diggingAnimation * Math.PI)));

            wieldHandAnimationRot.set(quatEnd.getEulerAnglesXYZ(wieldHandAnimationRot))
                .add((float) Math.toDegrees(wieldHandAnimationRot.x) + 180f,(float) Math.toDegrees(wieldHandAnimationRot.y),(float) Math.toDegrees(wieldHandAnimationRot.z));

            //block
        } else if (getItemInInventorySlot(getPlayerInventorySelection(),0).definition.blockID > 0) {
            wieldHandAnimationPos.set(wieldHandAnimationPosBaseEmpty);

            wieldHandAnimationRot.x = 180f;

            quatBegin.set(wieldRotationItemBegin.x, wieldRotationItemBegin.y, wieldRotationItemBegin.z, 0);

            quatEnd.set(wieldRotationItemEnd.x, wieldRotationItemEnd.y, wieldRotationItemEnd.z,0)
                    .set(quatBegin.slerp(quatEnd, (float) Math.sin(diggingAnimation * Math.PI)));

            wieldHandAnimationRot.set(quatEnd.getEulerAnglesXYZ(wieldHandAnimationRot))
                    .set((float) Math.toDegrees(wieldHandAnimationRot.x),(float) Math.toDegrees(wieldHandAnimationRot.y),(float) Math.toDegrees(wieldHandAnimationRot.z));

            //item/tool
        } else if (getItemInInventorySlot(getPlayerInventorySelection(),0).definition.isItem){

            wieldHandAnimationPosBaseTool.set(10f,-6.5f,-8f);

            wieldHandAnimationRotBegin.set(Math.toRadians(0), Math.toRadians(65), Math.toRadians(-35));
            wieldHandAnimationRotEnd.set(Math.toRadians(50), Math.toRadians(75), Math.toRadians(-45));

            wieldHandAnimationPos.set((float) (-8f * Math.sin(java.lang.Math.pow(diggingAnimation, 0.6f) * Math.PI)) + wieldHandAnimationPosBaseTool.x,(float) (5f * Math.sin(diggingAnimation * Math.PI)) + wieldHandAnimationPosBaseTool.y,wieldHandAnimationPosBaseTool.z);

            quatBegin.set(wieldHandAnimationRotBegin.x, wieldHandAnimationRotBegin.y, wieldHandAnimationRotBegin.z,0);

            quatEnd.set(wieldHandAnimationRotEnd.x, wieldHandAnimationRotEnd.y, wieldHandAnimationRotEnd.z,0)
                    .set(quatBegin.slerp(quatEnd, (float) Math.sin(diggingAnimation * Math.PI)));

            wieldHandAnimationRot.set(quatEnd.getEulerAnglesXYZ(wieldHandAnimationRot))
                    .set((float) Math.toDegrees(wieldHandAnimationRot.x),(float) Math.toDegrees(wieldHandAnimationRot.y),(float) Math.toDegrees(wieldHandAnimationRot.z));
        }

    }



    public static Vector3d getWieldHandAnimationPos(){
        return doubledHandAnimationPos.set(wieldHandAnimationPos.x + handInertia.x - (getPlayerViewBobbing().x * 10f),wieldHandAnimationPos.y + handInertia.y + (getPlayerViewBobbing().y * 10f),wieldHandAnimationPos.z);
    }

    public static Vector3f getWieldHandAnimationRot(){
        return wieldHandAnimationRot;
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
}
