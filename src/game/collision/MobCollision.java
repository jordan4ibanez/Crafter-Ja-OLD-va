package game.collision;

import game.mob.MobObject;
import org.joml.Vector2d;
import org.joml.Vector3d;

import static game.mob.Mob.getAllMobs;
import static game.player.Player.*;

//basically cylindrical magnetic 2d collision detection class
public class MobCollision {

    private static final Vector2d workerVec2D = new Vector2d();
    private static final Vector2d workerVec2D2 = new Vector2d();
    private static final Vector2d normalizedPos = new Vector2d();
    private static final Vector3d thisPos = new Vector3d();

    public static void mobSoftCollisionDetect(MobObject thisMob){
        //get this mob's info
        thisPos.set(thisMob.pos);
        workerVec2D.set(thisPos.x, thisPos.z);
        float thisWidth    = thisMob.width;
        float thisHeight   = thisMob.height;
        double thisBottom  = thisPos.y;
        double thisTop     = thisHeight + thisPos.y;

        MobObject[] mobs = getAllMobs();

        for (MobObject otherMob : mobs){

            //don't detect against self or dead mobs
            if (otherMob == thisMob || otherMob.health <= 0){
                continue;
            }

            //get other mob's info
            float otherWidth    = otherMob.width;
            Vector3d otherPos   = otherMob.pos;
            workerVec2D2.set(otherPos.x, otherPos.z);

            //only continue if within 2D radius
            if (workerVec2D.distance(workerVec2D2) <= thisWidth + otherWidth) {

                float otherHeight  = otherMob.height;
                double otherBottom = otherPos.y;
                double otherTop = otherHeight + otherPos.y;

                //only continue if within - Y 1D collision detection
                if (!(thisTop < otherBottom) && !(thisBottom > otherTop)){

                    //success!

                    //normalize values and make it not shoot mobs out
                    normalizedPos.set(workerVec2D2).sub(workerVec2D).normalize().mul(0.05f);

                    if (normalizedPos.isFinite()) {
                        thisMob.inertia.add((float)normalizedPos.x,0,(float)normalizedPos.y);
                    }
                }
            }
        }
    }

    public static void mobSoftPlayerCollisionDetect(MobObject thisMob){
        //get this mob's info
        thisPos.set(thisMob.pos);
        workerVec2D.set(thisPos.x, thisPos.z);
        float thisWidth    = thisMob.width;
        float thisHeight   = thisMob.height;
        double thisBottom  = thisPos.y;
        double thisTop     = thisHeight + thisPos.y;


        //get player's info
        float otherWidth    = getPlayerWidth();
        Vector3d otherPos   = getPlayerPos();
        workerVec2D2.set(otherPos.x, otherPos.z);

        //only continue if within 2D radius
        if (workerVec2D.distance(workerVec2D2) <= thisWidth + otherWidth) {

            float otherHeight = getPlayerHeight();
            double otherBottom = otherPos.y;
            double otherTop = otherHeight + otherPos.y;

            //only continue if within - Y 1D collision detection
            if (!(thisTop < otherBottom) && !(thisBottom > otherTop)) {

                //success!

                //normalize values and make it not shoot mobs out
                normalizedPos.set(workerVec2D).sub(workerVec2D2).normalize().mul(0.05f);

                if (normalizedPos.isFinite()) {
                    thisMob.inertia.add((float)normalizedPos.x,0,(float)normalizedPos.y);
                    addPlayerInertia((float) -normalizedPos.x, 0, (float) -normalizedPos.y);
                }
            }
        }
    }
}
