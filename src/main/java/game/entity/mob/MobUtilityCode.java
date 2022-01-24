package game.entity.mob;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class MobUtilityCode {

    private final Vector3d headPos = new Vector3d();
    private final Vector3d headTurn = new Vector3d();
    private final Vector3d adjustedHeadPos = new Vector3d();

    public void doHeadCode(int thisMob){

        //this is a pointer object
        Vector3d thisMobPos = MobObject.getMobPos(thisMob);

        //yet another pointer object
        Vector3f[] thisMobBodyOffsets = getMobDefinitionBodyOffsets(MobObject.getMobID(thisMob));

        //look another pointer object
        Vector3f[] thisMobBodyRotations = MobObject.getMobBodyRotations(thisMob);

        float thisMobSmoothRotation = MobObject.getMobSmoothRotation(thisMob);

        float smoothToRad = Math.toRadians(thisMobSmoothRotation + 90f);

        //silly head turning
        headPos.set(thisMobPos.x, thisMobPos.y, thisMobPos.z);
        adjustedHeadPos.set(Math.cos(-smoothToRad), 0,Math.sin(smoothToRad));
        adjustedHeadPos.mul(thisMobBodyOffsets[0].z).add(0,thisMobBodyOffsets[0].y,0);
        headPos.add(adjustedHeadPos);

        //check if the mob can actual "see" the player
        if (!getLineOfSight(headPos, getPlayerPosWithEyeHeight())){
            return;
        }

        //this is debug code for creating a new mob
        //createParticle(headPos.x, headPos.y, headPos.z, 0.f,0.f,0.f, (byte) 7); //debug

        headTurn.set(getPlayerPosWithEyeHeight()).sub(headPos);
        //headTurn.normalize();

        float headYaw = (float) Math.toDegrees(Math.atan2(headTurn.z, headTurn.x)) + 90 - thisMobSmoothRotation;
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

        //weird OOP application
        thisMobBodyRotations[0].set(pitch,headYaw,0);
    }


    //todo: shortest distance
    public void mobSmoothRotation(int thisMob){
        double delta = getDelta();

        float thisMobRotation = MobObject.getMobRotation(thisMob);
        float thisMobSmoothRotation = MobObject.getMobSmoothRotation(thisMob);

        float diff = thisMobRotation - thisMobSmoothRotation;

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
            thisMobSmoothRotation = thisMobRotation;
        } else {
            if (Math.abs(diff) > 180) {
                if (diff < 0) {
                    thisMobSmoothRotation += delta * 500f;
                } else if (diff > 0) {
                    thisMobSmoothRotation -= delta * 500f;
                }

                //correction of degrees overflow (-piToDegrees to piToDegrees) so it is workable
                if (thisMobSmoothRotation < -180) {
                    thisMobSmoothRotation += 360;
                } else if (thisMobSmoothRotation > 180) {
                    thisMobSmoothRotation -= 360;
                }

            } else {
                if (diff < 0) {
                    thisMobSmoothRotation -= delta * 500f;
                } else if (diff > 0) {
                    thisMobSmoothRotation += delta * 500f;
                }
            }
        }

        MobObject.setMobSmoothRotation(thisMob, thisMobSmoothRotation);
    }
}
