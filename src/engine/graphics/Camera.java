package engine.graphics;

import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.MouseInput.getMouseDisplVec;
import static game.player.Player.*;
import static game.ray.Ray.genericWorldRaycast;

public class Camera {

    private static final Vector3d position = new Vector3d();

    private static final Vector3f rotation = new Vector3f();

    private static final float   MOUSE_SENSITIVITY   = 0.09f;

    private static byte cameraPerspective = 0;

    public static Vector3d getCameraPosition(){
        return position;
    }

    public static void setCameraPosition(double x, double y, double z){
        position.x = x;
        position.y = y;
        position.z = z;
    }

    public static void moveCameraPosition(float offsetX, float offsetY, float offsetZ){
        if ( offsetZ != 0){
            position.x += (float)Math.sin(Math.toRadians(rotation.y)) * -1.0f * offsetZ;
            position.z += (float)Math.cos(Math.toRadians(rotation.y)) * offsetZ;
        }

        if ( offsetX != 0) {
            position.x += (float)Math.sin(Math.toRadians(rotation.y - 90f)) * -1.0f * offsetX;
            position.z += (float)Math.cos(Math.toRadians(rotation.y - 90f)) * offsetX;
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

    public static Vector3f getCameraRotation(){
        return new Vector3f(rotation);
    }

    public static void setCameraRotation(float x, float y, float z){
        rotation.x = x;
        rotation.y = y;
        rotation.z = z;
    }

    public static void moveCameraRotation(float offsetX, float offsetY, float offsetZ){
        rotation.x += offsetX;
        rotation.y += offsetY;
        rotation.z += offsetZ;
    }

    public static Vector3f getCameraRotationVector(){
        Vector3f rotationVector = new Vector3f();
        float xzLen = (float)Math.cos(Math.toRadians(rotation.x + 180f));
        rotationVector.z = xzLen * (float)Math.cos(Math.toRadians(rotation.y));
        rotationVector.y = (float)Math.sin(Math.toRadians(rotation.x + 180f));
        rotationVector.x = xzLen * (float)Math.sin(Math.toRadians(-rotation.y));
        return rotationVector;
    }

    public static void updateCamera(){

        if (cameraPerspective == 0) {
            setCameraPosition(getPlayerPosWithEyeHeight().x, getPlayerPosWithEyeHeight().y + getSneakOffset(), getPlayerPosWithEyeHeight().z);
            moveCameraPosition(getPlayerViewBobbing().x, getPlayerViewBobbing().y, getPlayerViewBobbing().z);
        }

        //update camera based on mouse
        Vector2f rotVec = getMouseDisplVec();
        moveCameraRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);

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
            Vector3d newCameraPos = genericWorldRaycast(getPlayerPosWithEyeHeight(), getCameraRotationVector().mul(-1), 3);
            setCameraPosition(newCameraPos.x,newCameraPos.y, newCameraPos.z);
        }
    }
}
