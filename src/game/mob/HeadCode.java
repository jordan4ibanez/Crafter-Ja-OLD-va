package game.mob;

import org.joml.Vector3d;
import org.joml.Vector3f;

import static game.player.Player.getPlayerPosWithEyeHeight;

public class HeadCode {

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

        //System.out.println(headYaw);

        //System.out.println((thisObject.rotation - 90) - yaw);

        thisObject.bodyRotations[0] = new Vector3f(pitch,headYaw,0);
    }
}
