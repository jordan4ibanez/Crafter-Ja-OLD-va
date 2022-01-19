package engine.graphics;

import org.joml.Math;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.graphics.Camera.*;
import static engine.render.GameRenderer.getWindowSizeX;
import static engine.render.GameRenderer.getWindowSizeY;
import static engine.time.TimeOfDay.getTimeOfDayLinear;
import static game.tnt.TNTEntity.getTNTPosition;
import static game.tnt.TNTEntity.getTNTScale;
import static org.joml.Math.toRadians;

//so much math
public class Transformation {

    //the master projection matrix, this is only modified by getProjectionMatrix
    private static final Matrix4d projectionMatrix = new Matrix4d();

    //another master matrix, this is only modified by getViewMatrix
    private static final Matrix4d modelViewMatrix = new Matrix4d();

    //another master matrix, only modified by resetOrthoProjectionMatrix
    private static final Matrix4d orthoMatrix = new Matrix4d();

    //these are worker matrices, they can be modified freely
    private static final Matrix4d modelMatrix = new Matrix4d();
    private static final Matrix4d viewMatrix = new Matrix4d();
    private static final Matrix4d orthoModelMatrix = new Matrix4d();

    //this is specifically used for openAL
    private static final Matrix4d openALMatrix = new Matrix4d();

    //these are workers for the sun and moon
    private static final Vector3d pos = new Vector3d();
    private static final Vector3d basePos = new Vector3d();

    public static Matrix4d getProjectionMatrix(){
        return projectionMatrix;
    }

    public static Matrix4d getModelMatrix(){
        return modelMatrix;
    }

    public static Matrix4d getOrthoModelMatrix(){
        return orthoModelMatrix;
    }

    public static void resetProjectionMatrix(float fov, float width, float height, float zNear, float zFar){
        projectionMatrix.setPerspective(fov, width / height, zNear, zFar);

        //right-handed coordinate system
        //first do the rotation so the camera rotates over it's position
        //then do the translation
        viewMatrix.rotation(toRadians(getCameraRotationX()), 1, 0 ,0)
                .rotate(toRadians(getCameraRotationY()), 0 , 1, 0)
                .translate(-getCameraPositionX(), -getCameraPositionY(), -getCameraPositionZ());
    }


    public static Matrix4d getTNTModelViewMatrix(int ID){
        modelViewMatrix.identity().translate(getTNTPosition(ID)).
                rotateX(toRadians(0)).
                rotateY(toRadians(0)).
                rotateZ(toRadians(0)).
                scale(getTNTScale(ID));
        modelMatrix.set(viewMatrix);
        modelMatrix.mul(modelViewMatrix);
        return modelMatrix;
    }


    public static void updateSunMatrix() {

        pos.set(0);
        basePos.set(getCameraPositionX(),getCameraPositionY(),getCameraPositionZ());

        double timeLinear = getTimeOfDayLinear() - 0.5d;

        pos.x = Math.sin(timeLinear * 2f * Math.PI);
        pos.y = Math.cos(timeLinear * 2f * Math.PI);
        pos.mul(5);
        pos.add(basePos);

        modelViewMatrix.identity().identity().translate(pos).
                rotateY(toRadians(90)).
                rotateZ(0).
                rotateX(toRadians((timeLinear + 0.25d) * 360)).
                scale(1f);

        modelMatrix.set(viewMatrix);
        modelMatrix.mul(modelViewMatrix);
    }

    public static void updateMoonMatrix() {

        pos.set(0);
        basePos.set(getCameraPositionX(),getCameraPositionY(),getCameraPositionZ());

        double timeLinear = getTimeOfDayLinear();

        pos.x = Math.sin(timeLinear * 2f * Math.PI);
        pos.y = Math.cos(timeLinear * 2f * Math.PI);
        pos.mul(5);
        pos.add(basePos);

        modelViewMatrix.identity().identity().translate(pos).
                rotateY(toRadians(90)).
                rotateZ(0).
                rotateX(toRadians((timeLinear + 0.25d) * 360)).
                scale(1f);

        modelMatrix.set(viewMatrix);
        modelMatrix.mul(modelViewMatrix);
    }


    //THIS IS USED BY OPENAL - SOUND MANAGER
    public static void updateOpenALSoundMatrix(double positionX, double positionY, double positionZ, float rotationX, float rotationY) {
        // First do the rotation so camera rotates over its position
        openALMatrix.rotationX(toRadians(rotationX))
                .rotateY(toRadians(rotationY))
                .translate(-positionX, -positionY, -positionZ);
    }

    public static Matrix4d getOpenALMatrix(){
        return openALMatrix;
    }



    public static void updateViewMatrix(double posX, double posY, double posZ, float rotX, float rotY, float rotZ) {
        // First do the rotation so camera rotates over its position
        modelMatrix.set(viewMatrix).translate(posX, posY, posZ).
                rotateY(toRadians(-rotY)).
                rotateZ(toRadians(-rotZ)).
                rotateX(toRadians(-rotX)).
                scale(1f);
    }

    //a silly addon method to modify the item size
    public static void updateItemViewMatrix(double posX, double posY, double posZ, float rotX, float rotY, float rotZ) {
        // First do the rotation so camera rotates over its position
        modelMatrix.set(viewMatrix).translate(posX, posY, posZ).
                rotateY(toRadians(-rotY)).
                rotateZ(toRadians(-rotZ)).
                rotateX(toRadians(-rotX)).
                scale(0.75);
    }


    public static void updateParticleViewMatrix(double positionX, double positionY, double positionZ, float rotationX, float rotationY, float rotationZ) {
        // First do the rotation so camera rotates over its position
        modelMatrix.set(viewMatrix).translate(positionX, positionY, positionZ).
                rotateY(toRadians(-rotationY)).
                rotateZ(toRadians(-rotationZ)).
                rotateX(toRadians(-rotationX)).
                scale(1f);
    }

    public static void updateTextIn3DSpaceViewMatrix(Vector3d position, Vector3f rotation, Vector3d scale) {
        // First do the rotation so camera rotates over its position
        modelMatrix.set(viewMatrix).translate(position).
                rotateY(toRadians(-rotation.y)).
                rotateZ(toRadians(-rotation.z)).
                rotateX(toRadians(-rotation.x)).
                scale(scale);
    }


    public static void updateViewMatrixWithPosRotationScale(double posX, double posY, double posZ, float rotX, float rotY, float rotZ,float scaleX, float scaleY, float scaleZ){
        modelMatrix.set(viewMatrix).translate(posX, posY, posZ).
                rotateX(toRadians(-rotX)).
                rotateY(toRadians(-rotY)).
                rotateZ(toRadians(-rotZ)).
                scale(scaleX, scaleY, scaleZ);
    }


    public static void updateMobMatrix(
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

    public static void setWieldHandMatrix(
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

    public static void resetOrthoProjectionMatrix() {
        orthoMatrix.setOrtho(-getWindowSizeX()/2f, getWindowSizeX()/2f, -getWindowSizeY()/2f, getWindowSizeY()/2f, -1000f, 1000f);
    }

    public static void updateOrthoModelMatrix(double posX, double posY, double posZ, float rotX, float rotY, float rotZ, double scaleX, double scaleY, double scaleZ) {
        orthoModelMatrix.set(orthoMatrix).translate(posX, posY, posZ).
                rotateX( toRadians(rotX)).
                rotateY( toRadians(rotY)).
                rotateZ( toRadians(rotZ)).
                scale(scaleX, scaleY, scaleZ);
    }

}
