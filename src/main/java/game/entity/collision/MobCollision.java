package game.entity.collision;

import game.entity.Entity;
import game.entity.EntityContainer;
import game.entity.mob.Mob;
import game.player.Player;
import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.List;

//basically cylindrical magnetic 2d collision detection class
public class MobCollision {

    private EntityContainer entityContainer;

    public MobCollision(){

    }

    public void setEntityContainer(EntityContainer entityContainer){
        if (this.entityContainer == null){
            this.entityContainer = entityContainer;
        }
    }

    private final Vector2d workerVec2D = new Vector2d();
    private final Vector2d workerVec2D2 = new Vector2d();
    private final Vector2d normalizedPos = new Vector2d();

    /*
    public void mobSoftCollisionDetect(Entity thisEntity, Vector3d thisMobPos, float thisMobHeight, float thisMobWidth){
        //get this mob's info
        workerVec2D.set(thisMobPos.x, thisMobPos.z);
        double thisBottom  = thisMobPos.y;
        double thisTop     = thisMobHeight + thisMobPos.y;

        List<Entity> test = entityContainer.getAll();

        for (Mob otherEntity : test){

            if (!otherEntity.isMob() || otherEntity == thisEntity || otherEntity.getHealth() <= 0){
                continue;
            }

            //get other mob's info
            float otherWidth    = otherEntity.getWidth();
            Vector3d otherPos   = otherEntity.getPos();
            workerVec2D2.set(otherPos.x, otherPos.z);

            //only continue if within 2D radius
            if (workerVec2D.distance(workerVec2D2) <= thisMobWidth + otherWidth) {

                float otherHeight  = otherEntity.getHeight();
                double otherBottom = otherPos.y;
                double otherTop = otherHeight + otherPos.y;

                //only continue if within - Y 1D collision detection
                if (!(thisTop < otherBottom) && !(thisBottom > otherTop)){

                    //success!

                    //normalize values and make it not shoot mobs out
                    normalizedPos.set(workerVec2D2).sub(workerVec2D).normalize().mul(0.05f);

                    if (normalizedPos.isFinite()) {
                        thisEntity.getInertia().add((float)normalizedPos.x,0,(float)normalizedPos.y);
                    }
                }
            }
        }
    }
     */
    public void mobSoftPlayerCollisionDetect(Entity thisMob, Vector3d thisMobPos, float thisMobHeight, float thisMobWidth, Player player){
        //get this mob's info
        workerVec2D.set(thisMobPos.x, thisMobPos.z);
        double thisBottom  = thisMobPos.y;
        double thisTop     = thisMobHeight + thisMobPos.y;


        //get player's info
        float otherWidth    = player.getWidth();
        Vector3d otherPos   = player.getPlayerPos();
        workerVec2D2.set(otherPos.x, otherPos.z);

        //only continue if within 2D radius
        if (workerVec2D.distance(workerVec2D2) <= thisMobWidth + otherWidth) {

            float otherHeight = player.getHeight();
            double otherBottom = otherPos.y;
            double otherTop = otherHeight + otherPos.y;

            //only continue if within - Y 1D collision detection
            if (!(thisTop < otherBottom) && !(thisBottom > otherTop)) {

                //success!

                //normalize values and make it not shoot mobs out
                normalizedPos.set(workerVec2D).sub(workerVec2D2).normalize().mul(0.05f);

                if (normalizedPos.isFinite()) {
                    thisMob.getInertia().add((float)normalizedPos.x,0,(float)normalizedPos.y);
                    player.getInertia().add((float) -normalizedPos.x, 0, (float) -normalizedPos.y);
                }
            }
        }
    }
}
