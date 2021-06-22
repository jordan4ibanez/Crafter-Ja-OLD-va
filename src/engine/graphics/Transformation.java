package engine.graphics;

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

    private static final Matrix4d projectionMatrix = new Matrix4d();
    private static final Matrix4d modelViewMatrix = new Matrix4d();
    private static final Matrix4d viewMatrix = new Matrix4d();
    private static final Matrix4d orthoMatrix = new Matrix4d();
    private static final Matrix4d orthoModelMatrix = new Matrix4d();

    public static Matrix4d getViewMatrix(){
        Vector3d cameraPos = getCameraPosition();
        Vector3f rotation = getCameraRotation();
        viewMatrix.identity();
        //first do the rotation so the camera rotates over it's position
        //(includes z)
        //viewMatrix.rotate(Math.toRadians(rotation.z), new Vector3d(0,0,1)).rotate(Math.toRadians(rotation.x), new Vector3d(1,0,0)).rotate(Math.toRadians(rotation.y), new Vector3d(0,1,0));
        viewMatrix.rotate(Math.toRadians(rotation.x), new Vector3d(1,0,0)).rotate(Math.toRadians(rotation.y), new Vector3d(0,1,0));
        //then do the translation
        viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        return viewMatrix;
    }

    public static Matrix4d getProjectionMatrix(float fov, float width, float height, float zNear, float zFar){
        float aspectRatio = width / height;
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);
        return projectionMatrix;
    }


    public static Matrix4d getTNTModelViewMatrix(int ID, Matrix4d viewMatrix){

        modelViewMatrix.identity().translate(getTNTPosition(ID)).
                rotateX(Math.toRadians(0)).
                rotateY(Math.toRadians(0)).
                rotateZ(Math.toRadians(0)).
                scale(getTNTScale(ID));
        Matrix4d viewCurr = new Matrix4d(viewMatrix);
        return viewCurr.mul(modelViewMatrix);
    }


    public static Matrix4d updateSunMatrix(Matrix4d matrix) {

        Vector3d pos = new Vector3d();
        Vector3d basePos = new Vector3d(getCameraPosition());

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
        return new Matrix4d(matrix).mul(modelViewMatrix);
    }

    public static Matrix4d updateMoonMatrix(Matrix4d matrix) {

        Vector3d pos = new Vector3d();
        Vector3d basePos = new Vector3d(getCameraPosition());

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
        return new Matrix4d(matrix).mul(modelViewMatrix);
    }


    public static void updateGenericViewMatrix(Vector3d position, Vector3f rotation, Matrix4d matrix) {
        // First do the rotation so camera rotates over its position
        matrix.rotationX(Math.toRadians(rotation.x))
                .rotateY(Math.toRadians(rotation.y))
                .translate(-position.x, -position.y, -position.z);
    }



    public static  Matrix4d updateModelViewMatrix(Vector3d position, Vector3f rotation, Matrix4d matrix) {

        // First do the rotation so camera rotates over its position
        modelViewMatrix.identity().identity().translate(position).
                rotateY(Math.toRadians(-rotation.y)).
                rotateZ(Math.toRadians(-rotation.z)).
                rotateX(Math.toRadians(-rotation.x)).
                scale(1f);
        return new Matrix4d(matrix).mul(modelViewMatrix);
    }


    public static  Matrix4d updateParticleViewMatrix(Vector3d position, Vector3f rotation, Matrix4d matrix) {
        // First do the rotation so camera rotates over its position
        modelViewMatrix.identity().identity().translate(position).
                rotateY(Math.toRadians(-rotation.y)).
                rotateZ(Math.toRadians(-rotation.z)).
                rotateX(Math.toRadians(-rotation.x)).
                scale(1f);
        return new Matrix4d(matrix).mul(modelViewMatrix);
    }

    public static  Matrix4d updateTextIn3DSpaceViewMatrix(Vector3d position, Vector3f rotation, Vector3d scale, Matrix4d matrix) {
        // First do the rotation so camera rotates over its position
        modelViewMatrix.identity().identity().translate(position).
                rotateY(Math.toRadians(-rotation.y)).
                rotateZ(Math.toRadians(-rotation.z)).
                rotateX(Math.toRadians(-rotation.x)).
                scale(scale);
        return new Matrix4d(matrix).mul(modelViewMatrix);
    }


    public static Matrix4d getGenericMatrixWithPosRotationScale(Vector3d position, Vector3f rotation,Vector3d scale, Matrix4d matrix){
        modelViewMatrix.identity().identity().translate(position.x, position.y, position.z).
                rotateX(Math.toRadians(-rotation.x)).
                rotateY(Math.toRadians(-rotation.y)).
                rotateZ(Math.toRadians(-rotation.z)).scale(scale);
        return new Matrix4d(matrix).mul(modelViewMatrix);
    }


    public static Matrix4d getMobMatrix(Vector3d basePos, Vector3f offsetPos, Vector3f bodyYaw,Vector3f bodyPartRotation,Vector3d scale, Matrix4d matrix){
        modelViewMatrix.identity().identity().
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
        return new Matrix4d(matrix).mul(modelViewMatrix);
    }


    //TODO--begin ortho creation

    public static void resetOrthoProjectionMatrix() {
        orthoMatrix.identity();
        orthoMatrix.setOrtho(-getWindowSize().x/2f, getWindowSize().x/2f, -getWindowSize().y/2f, getWindowSize().y/2f, -1000f, 1000f);
    }

    public static Matrix4d buildOrthoProjModelMatrix(Vector3d position, Vector3f rotation, Vector3d scale) {
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
