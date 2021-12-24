package engine.graphics;

import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.MouseInput.getMouseDisplVec;
import static game.player.Player.*;
import static game.player.ViewBobbing.*;
import static game.ray.Ray.genericWorldRaycast;

public class Camera {

    private static final Vector3d position = new Vector3d();
    private static final Vector3f rotation = new Vector3f();
    private static final Vector3f rotationVector = new Vector3f();
    private static final Vector2f mouseRotationVector = new Vector2f();

    private static final float MOUSE_SENSITIVITY   = 0.09f;

    private static byte cameraPerspective = 0;

    //this is mutable, be careful with this
    public static Vector3d getCameraPosition(){
        return position;
    }
    //immutable
    public static double getCameraPositionX(){
        return position.x;
    }
    //immutable
    public static double getCameraPositionY(){
        return position.y;
    }
    //immutable
    public static double getCameraPositionZ(){
        return position.z;
    }

    public static void setCameraPosition(double x, double y, double z){
        position.set(x,y,z);
    }

    public static void setCameraPosition(Vector3d newPos){
        position.set(newPos);
    }

    public static void moveCameraPosition(float offsetX, float offsetY, float offsetZ){
        if ( offsetZ != 0){
            position.x += Math.sin(Math.toRadians(rotation.y)) * -1.0f * offsetZ;
            position.z += Math.cos(Math.toRadians(rotation.y)) * offsetZ;
        }

        if ( offsetX != 0) {
            position.x += Math.sin(Math.toRadians(rotation.y - 90f)) * -1.0f * offsetX;
            position.z += Math.cos(Math.toRadians(rotation.y - 90f)) * offsetX;
        }

        if (offsetY != 0) {
            position.y += offsetY;
        }
    }

    public static void toggleCameraPerspective(){
        cameraPerspective++;

        //flip camera
        if (cameraPerspective == 2){
            rotation.x *= -1;
            rotation.y += 180;
        }
        //reset
        if (cameraPerspective > 2){
            cameraPerspective = 0;

            //flip camera
            rotation.x *= -1;
            rotation.y += 180;
        }
    }

    public static void setCameraPerspective(byte perspective){
        cameraPerspective = perspective;
    }

    public static byte getCameraPerspective(){
        return cameraPerspective;
    }

    //this is mutable, be careful with this
    public static Vector3f getCameraRotation(){
        return rotation;
    }
    //immutable
    public static float getCameraRotationX(){
        return rotation.x;
    }
    //immutable
    public static float getCameraRotationY(){
        return rotation.y;
    }
    //immutable
    public static float getCameraRotationZ(){
        return rotation.z;
    }


    public static void setCameraRotation(float x, float y, float z){
        rotation.set(x,y,z);
    }

    public static void moveCameraRotation(float offsetX, float offsetY, float offsetZ){
        rotation.x += offsetX;
        rotation.y += offsetY;
        rotation.z += offsetZ;
    }

    public static Vector3f getCameraRotationVector(){
        float xzLen = Math.cos(Math.toRadians(rotation.x + 180f));
        rotationVector.z = xzLen * Math.cos(Math.toRadians(rotation.y));
        rotationVector.y = Math.sin(Math.toRadians(rotation.x + 180f));
        rotationVector.x = xzLen * Math.sin(Math.toRadians(-rotation.y));
        return rotationVector;
    }

    public static void updateCamera(){

        if (cameraPerspective == 0) {
            setCameraPosition(getPlayerPosWithEyeHeight().x, getPlayerPosWithEyeHeight().y + getSneakOffset(), getPlayerPosWithEyeHeight().z);
            moveCameraPosition(getPlayerViewBobbingX(), getPlayerViewBobbingY(), getPlayerViewBobbingZ());
        }

        //update camera based on mouse
        mouseRotationVector.set(getMouseDisplVec());
        moveCameraRotation(mouseRotationVector.x * MOUSE_SENSITIVITY, mouseRotationVector.y * MOUSE_SENSITIVITY, 0);

        //limit camera pitch
        if (getCameraRotation().x < -90f) {
            moveCameraRotation((90f + getCameraRotation().x) * -1f, 0, 0);
        }
        if (getCameraRotation().x > 90f){
            moveCameraRotation((getCameraRotation().x - 90f) * -1f , 0, 0);
        }

        //loop camera yaw
        if (getCameraRotation().y < -180f){
            moveCameraRotation(0,360f, 0);
        }
        if (getCameraRotation().y > 180f){
            moveCameraRotation(0,-360f, 0);
        }

        //these must go after camera rotation
        //or weird inertia effect happens
        if (cameraPerspective > 0){
            setCameraPosition(genericWorldRaycast(getPlayerPosWithEyeHeight(), getCameraRotationVector().mul(-1), 3));
        }
    }
}
