package engine.graphics;

import org.joml.Math;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.render.GameRenderer.getWindowSize;
import static engine.time.TimeOfDay.getTimeOfDayLinear;
import static game.tnt.TNTEntity.getTNTPosition;
import static game.tnt.TNTEntity.getTNTScale;
import static engine.graphics.Camera.getCameraPosition;
import static engine.graphics.Camera.getCameraRotation;

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

    private static final Vector3d cameraPos = new Vector3d();
    private static final Vector3f cameraRotation = new Vector3f();
    private static final Vector3d constantLeft = new Vector3d(1,0,0);
    private static final Vector3d constantUp = new Vector3d(0,1,0);

    //these are workers for the sun and moon
    private static final Vector3d pos = new Vector3d();
    private static final Vector3d basePos = new Vector3d();

    public static Matrix4d getViewMatrix(){
        cameraPos.set(getCameraPosition());
        cameraRotation.set(getCameraRotation());
        viewMatrix.identity();
        //first do the rotation so the camera rotates over it's position
        viewMatrix.rotate(Math.toRadians(cameraRotation.x), constantLeft).rotate(Math.toRadians(cameraRotation.y),constantUp);
        //then do the translation
        viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        return viewMatrix;
    }

    public static Matrix4d getProjectionMatrix(float fov, float width, float height, float zNear, float zFar){
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, width / height, zNear, zFar);
        return projectionMatrix;
    }


    public static Matrix4d getTNTModelViewMatrix(int ID){
        modelViewMatrix.identity().translate(getTNTPosition(ID)).
                rotateX(Math.toRadians(0)).
                rotateY(Math.toRadians(0)).
                rotateZ(Math.toRadians(0)).
                scale(getTNTScale(ID));
        modelMatrix.set(viewMatrix);
        modelMatrix.mul(modelViewMatrix);
        return modelMatrix;
    }


    public static Matrix4d updateSunMatrix() {

        pos.set(0);
        basePos.set(getCameraPosition());

        double timeLinear = getTimeOfDayLinear() - 0.5d;

        pos.x = Math.sin(timeLinear * 2f * Math.PI);
        pos.y = Math.cos(timeLinear * 2f * Math.PI);
        pos.mul(5);
        pos.add(basePos);

        modelViewMatrix.identity().identity().translate(pos).
                rotateY(Math.toRadians(90)).
                rotateZ(0).
                rotateX(Math.toRadians((timeLinear + 0.25d) * 360)).
                scale(1f);

        modelMatrix.set(viewMatrix);
        modelMatrix.mul(modelViewMatrix);
        return modelMatrix;
    }

    public static Matrix4d updateMoonMatrix() {

        pos.set(0);
        basePos.set(getCameraPosition());

        double timeLinear = getTimeOfDayLinear();

        pos.x = Math.sin(timeLinear * 2f * Math.PI);
        pos.y = Math.cos(timeLinear * 2f * Math.PI);
        pos.mul(5);
        pos.add(basePos);

        modelViewMatrix.identity().identity().translate(pos).
                rotateY(Math.toRadians(90)).
                rotateZ(0).
                rotateX(Math.toRadians((timeLinear + 0.25d) * 360)).
                scale(1f);

        modelMatrix.set(viewMatrix);
        modelMatrix.mul(modelViewMatrix);
        return modelMatrix;
    }


    public static void updateGenericViewMatrix(Vector3d position, Vector3f rotation, Matrix4d matrix) {
        // First do the rotation so camera rotates over its position
        matrix.rotationX(Math.toRadians(rotation.x))
                .rotateY(Math.toRadians(rotation.y))
                .translate(-position.x, -position.y, -position.z);
    }



    public static  Matrix4d updateModelViewMatrix(Vector3d position, Vector3f rotation) {
        // First do the rotation so camera rotates over its position
        modelViewMatrix.identity().identity().translate(position).
                rotateY(Math.toRadians(-rotation.y)).
                rotateZ(Math.toRadians(-rotation.z)).
                rotateX(Math.toRadians(-rotation.x)).
                scale(1f);
        modelMatrix.set(viewMatrix);
        modelMatrix.mul(modelViewMatrix);
        return modelMatrix;
    }


    public static Matrix4d updateParticleViewMatrix(Vector3d position, Vector3f rotation) {
        // First do the rotation so camera rotates over its position
        modelViewMatrix.identity().identity().translate(position).
                rotateY(Math.toRadians(-rotation.y)).
                rotateZ(Math.toRadians(-rotation.z)).
                rotateX(Math.toRadians(-rotation.x)).
                scale(1f);
        modelMatrix.set(viewMatrix);
        modelMatrix.mul(modelViewMatrix);
        return modelMatrix;
    }

    public static  Matrix4d updateTextIn3DSpaceViewMatrix(Vector3d position, Vector3f rotation, Vector3d scale) {
        // First do the rotation so camera rotates over its position
        modelViewMatrix.identity().identity().translate(position).
                rotateY(Math.toRadians(-rotation.y)).
                rotateZ(Math.toRadians(-rotation.z)).
                rotateX(Math.toRadians(-rotation.x)).
                scale(scale);
        modelMatrix.set(viewMatrix);
        modelMatrix.mul(modelViewMatrix);
        return modelMatrix;
    }


    public static Matrix4d getGenericMatrixWithPosRotationScale(Vector3d position, Vector3f rotation,Vector3d scale){
        modelViewMatrix.identity().translate(position.x, position.y, position.z).
                rotateX(Math.toRadians(-rotation.x)).
                rotateY(Math.toRadians(-rotation.y)).
                rotateZ(Math.toRadians(-rotation.z)).scale(scale);
        modelMatrix.set(viewMatrix);
        modelMatrix.mul(modelViewMatrix);
        return modelMatrix;
    }


    public static Matrix4d getMobMatrix(Vector3d basePos, Vector3f offsetPos, Vector3f bodyYaw, Vector3f bodyPartRotation, Vector3d scale){
        modelViewMatrix.identity().
                //main rotation (positioning)
                translate(basePos.x, basePos.y, basePos.z).
                rotateX(Math.toRadians(-bodyYaw.x)).
                rotateY(Math.toRadians(-bodyYaw.y)).
                rotateZ(Math.toRadians(-bodyYaw.z)).scale(scale)
                //animation translation
                .translate(offsetPos).
                rotateY(Math.toRadians(-bodyPartRotation.y)).
                rotateX(Math.toRadians(-bodyPartRotation.x)).
                rotateZ(Math.toRadians(-bodyPartRotation.z));
        modelMatrix.set(viewMatrix);
        modelMatrix.mul(modelViewMatrix);
        return modelMatrix;
    }


    //TODO--begin ortho creation

    public static void updateOrthoProjectionMatrix() {
        orthoMatrix.identity();
        orthoMatrix.setOrtho(-getWindowSize().x/2f, getWindowSize().x/2f, -getWindowSize().y/2f, getWindowSize().y/2f, -1000f, 1000f);
    }

    public static Matrix4d updateOrthoModelMatrix(Vector3d position, Vector3f rotation, Vector3d scale) {
        modelViewMatrix.identity().translate(position.x, position.y, position.z).
                rotateX( Math.toRadians(rotation.x)).
                rotateY( Math.toRadians(rotation.y)).
                rotateZ( Math.toRadians(rotation.z)).
                scale(scale);
        orthoModelMatrix.set(orthoMatrix);
        orthoModelMatrix.mul(modelViewMatrix);
        return orthoModelMatrix;
    }

}
