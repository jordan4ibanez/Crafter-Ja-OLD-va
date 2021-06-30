package game.collision;

import game.mob.MobObject;
import org.joml.Vector2d;
import org.joml.Vector3d;

import static game.mob.Mob.getAllMobs;
import static game.player.Player.*;

//basically cylindrical magnetic 2d collision detection class
public class MobCollision {
    public static void mobSoftCollisionDetect(MobObject thisMob){
        //get this mob's info
        Vector3d thisPos   = thisMob.pos;
        Vector2d this2dPos = new Vector2d(thisPos.x, thisPos.z);
        float thisWidth    = thisMob.width;
        float thisHeight   = thisMob.height;
        double thisBottom  = thisPos.y;
        double thisTop     = thisHeight + thisPos.y;

        MobObject[] mobs = getAllMobs();

        for (MobObject otherMob : mobs){

            //don't detect against self
            if (otherMob == thisMob || otherMob.health <= 0){
                //System.out.println("i collided with myself! ID: " + thisMob.globalID);
                //System.out.println("This guy's dead! ID: " + otherMob.globalID);
                continue;
            }

            //get other mob's info
            float otherWidth    = otherMob.width;
            Vector3d otherPos   = otherMob.pos;
            Vector2d other2DPos = new Vector2d(otherPos.x, otherPos.z);

            //only continue if within 2D radius
            if (this2dPos.distance(other2DPos) <= thisWidth + otherWidth) {

                float otherHeight  = otherMob.height;
                double otherBottom = otherPos.y;
                double otherTop = otherHeight + otherPos.y;

                //only continue if within - Y 1D collision detection
                if (!(thisTop < otherBottom) && !(thisBottom > otherTop)){

                    //success!

                    //normalize values and make it not shoot mobs out
                    Vector2d normalizedPos = new Vector2d(this2dPos).sub(other2DPos).normalize().mul(0.05f);

                    if (normalizedPos.isFinite()) {
                        thisMob.inertia.x += normalizedPos.x;
                        thisMob.inertia.z += normalizedPos.y;
                    }
                }
            }
        }
    }

    public static void mobSoftPlayerCollisionDetect(MobObject thisMob){
        //get this mob's info
        Vector3d thisPos   = thisMob.pos;
        Vector2d this2dPos = new Vector2d(thisPos.x, thisPos.z);
        float thisWidth    = thisMob.width;
        float thisHeight   = thisMob.height;
        double thisBottom  = thisPos.y;
        double thisTop     = thisHeight + thisPos.y;


        //get player's info
        float otherWidth    = getPlayerWidth();
        Vector3d otherPos   = getPlayerPos();
        Vector2d other2DPos = new Vector2d(otherPos.x, otherPos.z);

        //only continue if within 2D radius
        if (this2dPos.distance(other2DPos) <= thisWidth + otherWidth) {

            float otherHeight = getPlayerHeight();
            double otherBottom = otherPos.y;
            double otherTop = otherHeight + otherPos.y;

            //only continue if within - Y 1D collision detection
            if (!(thisTop < otherBottom) && !(thisBottom > otherTop)) {

                //success!

                //normalize values and make it not shoot mobs out
                Vector2d normalizedPos = new Vector2d(this2dPos).sub(other2DPos).normalize().mul(0.05f);

                if (normalizedPos.isFinite()) {
                    thisMob.inertia.x += normalizedPos.x;
                    thisMob.inertia.z += normalizedPos.y;
                    addPlayerInertia((float) -normalizedPos.x, 0, (float) -normalizedPos.y);
                }
            }
        }
    }
}
