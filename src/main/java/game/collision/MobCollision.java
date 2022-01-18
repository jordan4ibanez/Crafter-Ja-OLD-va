package game.collision;

import it.unimi.dsi.fastutil.ints.IntSet;
import org.joml.Vector2d;
import org.joml.Vector3d;

import static game.mob.MobDefinition.*;
import static game.mob.MobObject.*;
import static game.player.Player.*;

//basically cylindrical magnetic 2d collision detection class
public class MobCollision {

    private static final Vector2d workerVec2D = new Vector2d();
    private static final Vector2d workerVec2D2 = new Vector2d();
    private static final Vector2d normalizedPos = new Vector2d();

    public static void mobSoftCollisionDetect(int thisMob, Vector3d thisMobPos, float thisMobHeight, float thisMobWidth){
        //get this mob's info
        workerVec2D.set(thisMobPos.x, thisMobPos.z);
        double thisBottom  = thisMobPos.y;
        double thisTop     = thisMobHeight + thisMobPos.y;

        IntSet mobs = getMobKeys();

        for (int otherMob : mobs){

            //don't detect against self or dead mobs
            if (otherMob == thisMob || getMobHealth(otherMob) <= 0){
                continue;
            }

            //get other mob's info
            float otherWidth    = getMobDefinitionWidth(getMobID(otherMob));
            Vector3d otherPos   = getMobPos(otherMob);
            workerVec2D2.set(otherPos.x, otherPos.z);

            //only continue if within 2D radius
            if (workerVec2D.distance(workerVec2D2) <= thisMobWidth + otherWidth) {

                float otherHeight  = getMobDefinitionHeight(getMobID(otherMob));
                double otherBottom = otherPos.y;
                double otherTop = otherHeight + otherPos.y;

                //only continue if within - Y 1D collision detection
                if (!(thisTop < otherBottom) && !(thisBottom > otherTop)){

                    //success!

                    //normalize values and make it not shoot mobs out
                    normalizedPos.set(workerVec2D2).sub(workerVec2D).normalize().mul(0.05f);

                    if (normalizedPos.isFinite()) {
                        getMobInertia(thisMob).add((float)normalizedPos.x,0,(float)normalizedPos.y);
                    }
                }
            }
        }
    }

    public static void mobSoftPlayerCollisionDetect(int thisMob, Vector3d thisMobPos, float thisMobHeight, float thisMobWidth){
        //get this mob's info
        workerVec2D.set(thisMobPos.x, thisMobPos.z);
        double thisBottom  = thisMobPos.y;
        double thisTop     = thisMobHeight + thisMobPos.y;


        //get player's info
        float otherWidth    = getPlayerWidth();
        Vector3d otherPos   = getPlayerPos();
        workerVec2D2.set(otherPos.x, otherPos.z);

        //only continue if within 2D radius
        if (workerVec2D.distance(workerVec2D2) <= thisMobWidth + otherWidth) {

            float otherHeight = getPlayerHeight();
            double otherBottom = otherPos.y;
            double otherTop = otherHeight + otherPos.y;

            //only continue if within - Y 1D collision detection
            if (!(thisTop < otherBottom) && !(thisBottom > otherTop)) {

                //success!

                //normalize values and make it not shoot mobs out
                normalizedPos.set(workerVec2D).sub(workerVec2D2).normalize().mul(0.05f);

                if (normalizedPos.isFinite()) {
                    getMobInertia(thisMob).add((float)normalizedPos.x,0,(float)normalizedPos.y);
                    addPlayerInertia((float) -normalizedPos.x, 0, (float) -normalizedPos.y);
                }
            }
        }
    }
}
