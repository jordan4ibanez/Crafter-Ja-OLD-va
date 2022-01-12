package game.mob;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.time.Time.getDelta;
import static game.player.Player.getPlayerPosWithEyeHeight;
import static game.ray.LineOfSight.getLineOfSight;

public class MobUtilityCode {


    private final static Vector3d headPos = new Vector3d();
    private final static Vector3d headTurn = new Vector3d();
    private final static Vector3d adjustedHeadPos = new Vector3d();

    public static void doHeadCode(int thisMob){

        if (!getLineOfSight(thisMob.pos, getPlayerPosWithEyeHeight())){
            return;
        }

        //silly head turning
        headPos.set(thisMob.pos);

        float smoothToRad = Math.toRadians(thisMob.smoothRotation + 90f);

        headPos.add(adjustedHeadPos.set(Math.cos(-smoothToRad), 0,Math.sin(smoothToRad)).mul(thisMob.bodyOffsets[0].z).add(0,thisMob.bodyOffsets[0].y,0));

        //createParticle(new Vector3d(headPos), new Vector3f(0,0,0), 7); //debug

        headTurn.set(getPlayerPosWithEyeHeight()).sub(headPos);
        //headTurn.normalize();

        float headYaw = (float) Math.toDegrees(Math.atan2(headTurn.z, headTurn.x)) + 90 - thisMob.smoothRotation;
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

        thisMob.bodyRotations[0] = new Vector3f(pitch,headYaw,0);
    }


    //todo: shortest distance
    public static void mobSmoothRotation(int thisMob){
        double delta = getDelta();

        float diff = thisMob.rotation - thisMob.smoothRotation;

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
            thisMob.smoothRotation = thisMob.rotation;
        } else {
            if (Math.abs(diff) > 180) {
                if (diff < 0) {
                    thisMob.smoothRotation += delta * 500f;
                } else if (diff > 0) {
                    thisMob.smoothRotation -= delta * 500f;
                }

                //correction of degrees overflow (-piToDegrees to piToDegrees) so it is workable
                if (thisMob.smoothRotation < -180) {
                    thisMob.smoothRotation += 360;
                } else if (thisMob.smoothRotation > 180) {
                    thisMob.smoothRotation -= 360;
                }

            } else {
                if (diff < 0) {
                    thisMob.smoothRotation -= delta * 500f;
                } else if (diff > 0) {
                    thisMob.smoothRotation += delta * 500f;
                }
            }
        }
    }
}
