package game.mob;

import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.Time.getDelta;
import static game.player.Player.getPlayerPosWithEyeHeight;

public class MobUtilityCode {

    public static void doHeadCode(MobObject thisObject){
        //silly head turning
        Vector3d headPos = new Vector3d(thisObject.pos);
        headPos.add(thisObject.bodyOffsets[0]);

        Vector3d headTurn = getPlayerPosWithEyeHeight().sub(headPos);
        //headTurn.normalize();

        float headYaw = (float)Math.toDegrees(Math.atan2(headTurn.z, headTurn.x)) + 90 - thisObject.smoothRotation;
        float pitch = (float)Math.toDegrees(Math.atan2(Math.sqrt(headTurn.z * headTurn.z + headTurn.x * headTurn.x), headTurn.y) + (Math.PI * 1.5));


        //correction of degrees overflow (-piToDegrees to piToDegrees) so it is workable
        if (headYaw < -180) {
            headYaw += 360;
        } else if (headYaw > 180){
            headYaw -= 360;
        }

        //a temporary reset, looks creepy
        if (headYaw > 90 || headYaw < -90){
            headYaw = 0;
            pitch = 0;
        }

        thisObject.bodyRotations[0] = new Vector3f(pitch,headYaw,0);
    }


    //todo: shortest distance
    public static void mobSmoothRotation(MobObject thisObject){
        float delta = getDelta();

        float diff = thisObject.rotation - thisObject.smoothRotation;

        //correction of degrees overflow (-piToDegrees to piToDegrees) so it is workable
        if (diff < -180) {
            diff += 360;
        } else if (diff > 180){
            diff -= 360;
        }

        /*
        this is basically brute force inversion to correct the yaw
        addition and make the mob move to the shortest rotation
        vector possible
         */

        if (Math.abs(diff) < delta * 500f){
            thisObject.smoothRotation = thisObject.rotation;
        } else {
            if (Math.abs(diff) > 180) {
                if (diff < 0) {
                    thisObject.smoothRotation += delta * 500f;
                } else if (diff > 0) {
                    thisObject.smoothRotation -= delta * 500f;
                }

                //correction of degrees overflow (-piToDegrees to piToDegrees) so it is workable
                if (thisObject.smoothRotation < -180) {
                    thisObject.smoothRotation += 360;
                } else if (thisObject.smoothRotation > 180) {
                    thisObject.smoothRotation -= 360;
                }

            } else {
                if (diff < 0) {
                    thisObject.smoothRotation -= delta * 500f;
                } else if (diff > 0) {
                    thisObject.smoothRotation += delta * 500f;
                }
            }
        }
    }
}
