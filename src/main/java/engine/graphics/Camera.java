package engine.graphics;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class Camera {

    private final Vector3d position = new Vector3d();
    private final Vector3f rotation = new Vector3f();
    private final Vector3f rotationVector = new Vector3f();

    //make this adjustable 0.2 to 1.0 maybe - implement slider in menu system!
    private final float MOUSE_SENSITIVITY   = 0.09f;

    private byte cameraPerspective = 0;

    public Vector3d getCameraPosition(){
        return position;
    }

    public void setCameraPosition(Vector3d newPos){
        position.set(newPos);
    }

    public void moveCameraPosition(float offsetX, float offsetY, float offsetZ){
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

    public void toggleCameraPerspective(){
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

    public void setCameraPerspective(byte perspective){
        cameraPerspective = perspective;
    }

    public byte getCameraPerspective(){
        return cameraPerspective;
    }

    public Vector3f getCameraRotation(){
        return rotation;
    }

    public void setCameraRotation(Vector3f rotation){
        rotation.set(rotation);
    }

    public void moveCameraRotation(Vector3f offset){
        rotation.add(offset);
    }

    //mutable - be careful with this
    public Vector3f getCameraRotationVector(){
        float xzLen = Math.cos(Math.toRadians(rotation.x + 180f));
        rotationVector.z = xzLen * Math.cos(Math.toRadians(rotation.y));
        rotationVector.y = Math.sin(Math.toRadians(rotation.x + 180f));
        rotationVector.x = xzLen * Math.sin(Math.toRadians(-rotation.y));
        return rotationVector;
    }
    //immutable
    public float getCameraRotationVectorX(){
        float xzLen = Math.cos(Math.toRadians(rotation.x + 180f));
        return xzLen * Math.sin(Math.toRadians(-rotation.y));
    }
    //immutable
    public float getCameraRotationVectorY(){
        return Math.sin(Math.toRadians(rotation.x + 180f));
    }
    //immutable
    public float getCameraRotationVectorZ(){
        float xzLen = Math.cos(Math.toRadians(rotation.x + 180f));
        return xzLen * Math.cos(Math.toRadians(rotation.y));
    }





    public void updateCamera(){

        if (cameraPerspective == 0) {
            setCameraPosition(getPlayerPosWithEyeHeight());
            moveCameraPosition(getPlayerViewBobbing());
        }

        //update camera based on mouse
        moveCameraRotation(getMouseDisplVecX() * MOUSE_SENSITIVITY, getMouseDisplVecY() * MOUSE_SENSITIVITY, 0);

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
            setCameraPosition(cameraRayCast(getPlayerPosWithEyeHeightX(), getPlayerPosWithEyeHeightY(), getPlayerPosWithEyeHeightZ(), getCameraRotationVectorX() * -1f, getCameraRotationVectorY() * -1f, getCameraRotationVectorZ() * -1f, 3));
        }
    }
}
