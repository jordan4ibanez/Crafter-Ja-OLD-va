package game.player;

import engine.time.Delta;
import org.joml.Vector3f;


public class ViewBobbing {

    private final Player player;
    private final Delta delta;

    public ViewBobbing(Player player, Delta delta){
        this.player = player;
        this.delta = delta;
    }

    private final Vector3f viewBobbing = new Vector3f(0,0,0);
    private boolean xPositive = true;
    private float xBobPos = 0;
    private float yBobPos = 0;

    public void applyViewBobbing() {

        double delta = this.delta.getDelta();

        double viewBobbingAddition = delta  * 250f;

        //System.out.println(viewBobbingAddition);

        if (player.isRunning()){
            viewBobbingAddition = delta * 290f;
        }

        if (xPositive) {
            xBobPos += viewBobbingAddition;
            if (xBobPos >= 50f){
                xBobPos = 50f;
                xPositive = false;
                //playSound("dirt_" + (int)(Math.ceil(Math.random()*3)));
            }
        } else {
            xBobPos -= viewBobbingAddition;
            if (xBobPos <= -50f){
                xBobPos = -50f;
                xPositive = true;
                //playSound("dirt_"  + (int)(Math.ceil(Math.random()*3)));
            }
        }

        yBobPos = Math.abs(xBobPos);

        viewBobbing.x = xBobPos/700f;
        viewBobbing.y = yBobPos/800f;
    }

    public void returnPlayerViewBobbing(){

        double delta = this.delta.getDelta();

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

    public Vector3f getPlayerViewBobbing(){
        return viewBobbing;
    }
}
