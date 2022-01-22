package engine.graphics;

import org.joml.Math;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3f;

//so much math
public class Transformation {

    //the master projection matrix, this is only modified by getProjectionMatrix
    private final Matrix4d projectionMatrix = new Matrix4d();

    //another master matrix, only modified by resetOrthoProjectionMatrix
    private final Matrix4d orthoMatrix = new Matrix4d();

    //these are worker matrices, they can be modified freely
    private final Matrix4d modelMatrix = new Matrix4d();
    private final Matrix4d viewMatrix = new Matrix4d();
    private final Matrix4d orthoModelMatrix = new Matrix4d();

    //this is specifically used for openAL
    private final Matrix4d openALMatrix = new Matrix4d();

    public Matrix4d getProjectionMatrix(){
        return projectionMatrix;
    }

    public Matrix4d getModelMatrix(){
        return modelMatrix;
    }

    public Matrix4d getOrthoModelMatrix(){
        return orthoModelMatrix;
    }

    public void resetProjectionMatrix(float fov, float width, float height, float zNear, float zFar){
        projectionMatrix.setPerspective(fov, width / height, zNear, zFar);

        //right-handed coordinate system
        //first do the rotation so the camera rotates over it's position
        //then do the translation
        viewMatrix.rotation(toRadians(getCameraRotationX()), 1, 0 ,0)
                .rotate(toRadians(getCameraRotationY()), 0 , 1, 0)
                .translate(-getCameraPositionX(), -getCameraPositionY(), -getCameraPositionZ());
    }

    public void updateCelestialMatrix(double inputTime) {
        //keep these bois on the stack
        double x = (Math.sin(inputTime * 2f * Math.PI) * 5d) + getCameraPositionX();
        double y = (Math.cos(inputTime * 2f * Math.PI) * 5d) + getCameraPositionY();

        modelMatrix.set(viewMatrix)
                .translate(x, y, getCameraPositionZ()).
                rotateY(toRadians(90)).
                rotateZ(0).
                rotateX(toRadians((inputTime + 0.25d) * 360)).
                scale(1f);
    }

    //THIS IS USED BY OPENAL - SOUND MANAGER
    public void updateOpenALSoundMatrix(double positionX, double positionY, double positionZ, float rotationX, float rotationY) {
        // First do the rotation so camera rotates over its position
        openALMatrix.rotationX(toRadians(rotationX))
                .rotateY(toRadians(rotationY))
                .translate(-positionX, -positionY, -positionZ);
    }

    public Matrix4d getOpenALMatrix(){
        return openALMatrix;
    }



    public void updateViewMatrix(double posX, double posY, double posZ, float rotX, float rotY, float rotZ) {
        // First do the rotation so camera rotates over its position
        modelMatrix.set(viewMatrix).translate(posX, posY, posZ).
                rotateY(toRadians(-rotY)).
                rotateZ(toRadians(-rotZ)).
                rotateX(toRadians(-rotX)).
                scale(1f);
    }

    //a silly addon method to modify the item size
    public void updateItemViewMatrix(double posX, double posY, double posZ, float rotX, float rotY, float rotZ) {
        // First do the rotation so camera rotates over its position
        modelMatrix.set(viewMatrix).translate(posX, posY, posZ).
                rotateY(toRadians(-rotY)).
                rotateZ(toRadians(-rotZ)).
                rotateX(toRadians(-rotX)).
                scale(0.75);
    }


    public void updateParticleViewMatrix(double positionX, double positionY, double positionZ, float rotationX, float rotationY, float rotationZ) {
        // First do the rotation so camera rotates over its position
        modelMatrix.set(viewMatrix).translate(positionX, positionY, positionZ).
                rotateY(toRadians(-rotationY)).
                rotateZ(toRadians(-rotationZ)).
                rotateX(toRadians(-rotationX)).
                scale(1f);
    }

    public void updateTextIn3DSpaceViewMatrix(Vector3d position, Vector3f rotation, Vector3d scale) {
        // First do the rotation so camera rotates over its position
        modelMatrix.set(viewMatrix).translate(position).
                rotateY(toRadians(-rotation.y)).
                rotateZ(toRadians(-rotation.z)).
                rotateX(toRadians(-rotation.x)).
                scale(scale);
    }


    public void updateViewMatrixWithPosRotationScale(double posX, double posY, double posZ, float rotX, float rotY, float rotZ,float scaleX, float scaleY, float scaleZ){
        modelMatrix.set(viewMatrix).translate(posX, posY, posZ).
                rotateX(toRadians(-rotX)).
                rotateY(toRadians(-rotY)).
                rotateZ(toRadians(-rotZ)).
                scale(scaleX, scaleY, scaleZ);
    }


    public void updateMobMatrix(
            double basePosX, double basePosY, double basePosZ,
            float offsetPosX, float offsetPosY, float offsetPosZ,
            float bodyYawX, float bodyYawY, float bodyYawZ,
            float bodyPartRotationX, float bodyPartRotationY, float bodyPartRotationZ,
            double scaleX, double scaleY, double scaleZ){
        modelMatrix.set(viewMatrix)
                //main rotation (positioning)
                .translate(basePosX, basePosY, basePosZ).
                rotateX(toRadians(-bodyYawX)).
                rotateY(toRadians(-bodyYawY)).
                rotateZ(toRadians(-bodyYawZ))
                .scale(scaleX,scaleY,scaleZ)
                //animation translation
                .translate(offsetPosX, offsetPosY, offsetPosZ).
                rotateY(toRadians(-bodyPartRotationY)).
                rotateX(toRadians(-bodyPartRotationX)).
                rotateZ(toRadians(-bodyPartRotationZ));
    }

    public void setWieldHandMatrix(
            double basePosX, double basePosY, double basePosZ,
            double offsetPosX, double offsetPosY, double offsetPosZ,
            float cameraRotationX, float cameraRotationY, float cameraRotationZ,
            float handRotationX, float handRotationY, float handRotationZ,
            double scaleX, double scaleY, double scaleZ,
            double offsetScaleX, double offsetScaleY, double offsetScaleZ){
        modelMatrix.set(viewMatrix)
                //main positioning
                .translate(basePosX, basePosY, basePosZ)//.scale(scale);
                //main rotation
                .rotateY(toRadians(-cameraRotationY)) //y must be first - kind of like a tank aiming it's gun
                .rotateX(toRadians(-cameraRotationX))
                .rotateZ(toRadians(-cameraRotationZ))
                //do animation offsets
                .translate(offsetPosX * offsetScaleX, offsetPosY * offsetScaleY, offsetPosZ * offsetScaleZ)
                //finish off the animation rotations
                .rotateX(handRotationX)
                .rotateZ(handRotationZ)
                .rotateY(handRotationY)
                .scale(scaleX, scaleY, scaleZ);
    }


    //ortholinear

    public void resetOrthoProjectionMatrix() {
        orthoMatrix.setOrtho(-getWindowSizeX()/2f, getWindowSizeX()/2f, -getWindowSizeY()/2f, getWindowSizeY()/2f, -1000f, 1000f);
    }

    public void updateOrthoModelMatrix(double posX, double posY, double posZ, float rotX, float rotY, float rotZ, double scaleX, double scaleY, double scaleZ) {
        orthoModelMatrix.set(orthoMatrix).translate(posX, posY, posZ).
                rotateX( toRadians(rotX)).
                rotateY( toRadians(rotY)).
                rotateZ( toRadians(rotZ)).
                scale(scaleX, scaleY, scaleZ);
    }

}
