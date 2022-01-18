package game.player;

import org.joml.Vector3f;

import static engine.sound.SoundAPI.playSound;
import static engine.time.Time.getDelta;
import static game.player.Player.isPlayerRunning;

public class ViewBobbing {

    private static final Vector3f viewBobbing = new Vector3f(0,0,0);
    private static boolean xPositive = true;
    private static float xBobPos = 0;
    private static float yBobPos = 0;

    public static void applyViewBobbing() {

        double delta = getDelta();

        double viewBobbingAddition = delta  * 250f;

        //System.out.println(viewBobbingAddition);

        if (isPlayerRunning()){
            viewBobbingAddition = delta * 290f;
        }

        if (xPositive) {
            xBobPos += viewBobbingAddition;
            if (xBobPos >= 50f){
                xBobPos = 50f;
                xPositive = false;
                playSound("dirt_" + (int)(Math.ceil(Math.random()*3)));
            }
        } else {
            xBobPos -= viewBobbingAddition;
            if (xBobPos <= -50f){
                xBobPos = -50f;
                xPositive = true;
                playSound("dirt_"  + (int)(Math.ceil(Math.random()*3)));
            }
        }

        yBobPos = Math.abs(xBobPos);

        viewBobbing.x = xBobPos/700f;
        viewBobbing.y = yBobPos/800f;
    }

    public static void returnPlayerViewBobbing(){

        double delta = getDelta();

        if ((Math.abs(xBobPos)) <= 300 * delta){
            xBobPos = 0;
        }

        if (xBobPos > 0){
            xBobPos -= 300 * delta;
        } else if (xBobPos < 0){
            xBobPos += 300 * delta;
        }

        yBobPos = Math.abs(xBobPos);

        viewBobbing.x = xBobPos/700f;
        viewBobbing.y = yBobPos/800f;
    }

    //mutable, be careful with this
    public static Vector3f getPlayerViewBobbing(){
        return viewBobbing;
    }
    //immutable
    public static float getPlayerViewBobbingX(){
        return viewBobbing.x;
    }
    //immutable
    public static float getPlayerViewBobbingY(){
        return viewBobbing.y;
    }
    //immutable
    public static float getPlayerViewBobbingZ(){
        return viewBobbing.z;
    }
}
