package engine.graph;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static engine.Renderer.getWindowSize;
import static game.tnt.TNTEntity.getTNTPosition;
import static game.tnt.TNTEntity.getTNTScale;
import static engine.graph.Camera.getCameraPosition;
import static engine.graph.Camera.getCameraRotation;

public class Transformation {

    private static final Matrix4f projectionMatrix = new Matrix4f();
    private static final Matrix4f modelViewMatrix = new Matrix4f();
    private static final Matrix4f viewMatrix = new Matrix4f();
    private static final Matrix4f orthoMatrix = new Matrix4f();
    private static final Matrix4f orthoModelMatrix = new Matrix4f();

    public static final Matrix4f getViewMatrix(){
        Vector3f cameraPos = getCameraPosition();
        Vector3f rotation = getCameraRotation();
        viewMatrix.identity();
        //first do the rotation so the camera rotates over it's position
        viewMatrix.rotate((float)Math.toRadians(rotation.x), new Vector3f(1,0,0)).rotate((float)Math.toRadians(rotation.y), new Vector3f(0,1,0));
        //then do the translation
        viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        return viewMatrix;
    }

    public static Matrix4f getProjectionMatrix(float fov, float width, float height, float zNear, float zFar){
        float aspectRatio = width / height;
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);
        return projectionMatrix;
    }


    public static Matrix4f getTNTModelViewMatrix(int ID, Matrix4f viewMatrix){
        modelViewMatrix.identity().translate(getTNTPosition(ID)).
                rotateX((float)Math.toRadians(0)).
                rotateY((float)Math.toRadians(0)).
                rotateZ((float)Math.toRadians(0)).
                scale(getTNTScale(ID));
        Matrix4f viewCurr = new Matrix4f(viewMatrix);
        return viewCurr.mul(modelViewMatrix);
    }


    public static Matrix4f getModelViewMatrix(Matrix4f viewMatrix){
        Vector3f rotation = new Vector3f(0,0,0);
        modelViewMatrix.identity().translate(new Vector3f(0,0,0)).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(1f);
        Matrix4f viewCurr = new Matrix4f(viewMatrix);
        return viewCurr.mul(modelViewMatrix);
    }

    public static Matrix4f updateGenericViewMatrix(Vector3f position, Vector3f rotation, Matrix4f matrix) {
        // First do the rotation so camera rotates over its position
        return matrix.rotationX((float)Math.toRadians(rotation.x))
                .rotateY((float)Math.toRadians(rotation.y))
                .translate(-position.x, -position.y, -position.z);
    }

    public static  Matrix4f updateModelViewMatrix(Vector3f position, Vector3f rotation, Matrix4f matrix) {
        // First do the rotation so camera rotates over its position
        modelViewMatrix.identity().identity().translate(position).

                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                rotateX((float)Math.toRadians(-rotation.x)).
                scale(1f);
        return new Matrix4f(matrix).mul(modelViewMatrix);
    }

    public static  Matrix4f updateParticleViewMatrix(Vector3f position, Vector3f rotation, Matrix4f matrix) {
        // First do the rotation so camera rotates over its position
        modelViewMatrix.identity().identity().translate(position).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                rotateX((float)Math.toRadians(-rotation.x)).
                scale(1f);
        return new Matrix4f(matrix).mul(modelViewMatrix);
    }

    public static Matrix4f getGenericMatrixWithPosRotationScale(Vector3f position, Vector3f rotation,Vector3f scale, Matrix4f matrix){
        modelViewMatrix.identity().identity().translate(position.x, position.y, position.z).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).scale(scale);
        return new Matrix4f(matrix).mul(modelViewMatrix);
    }

    public static Matrix4f getMobMatrix(Vector3f basePos, Vector3f offsetPos, Vector3f bodyYaw,Vector3f bodyPartRotation,Vector3f scale, Matrix4f matrix){
        modelViewMatrix.identity().identity().
                //main rotation (positioning)
                translate(basePos.x, basePos.y, basePos.z).
                rotateX((float)Math.toRadians(-bodyYaw.x)).
                rotateY((float)Math.toRadians(-bodyYaw.y)).
                rotateZ((float)Math.toRadians(-bodyYaw.z)).scale(scale)
                //animation translation
                .translate(offsetPos).
                rotateY((float)Math.toRadians(-bodyPartRotation.y)).
                rotateX((float)Math.toRadians(-bodyPartRotation.x)).
                rotateZ((float)Math.toRadians(-bodyPartRotation.z));
        return new Matrix4f(matrix).mul(modelViewMatrix);
    }



    //TODO--begin ortho creation

    public static void resetOrthoProjectionMatrix() {
        orthoMatrix.identity();
        orthoMatrix.setOrtho(-(float)getWindowSize().x/2f, (float)getWindowSize().x/2f, -(float)getWindowSize().y/2f, (float)getWindowSize().y/2f, -1000f, 1000f);
    }

    public static Matrix4f buildOrthoProjModelMatrix(Vector3f position, Vector3f rotation, Vector3f scale) {
        modelViewMatrix.identity().translate(position.x, position.y, position.z).
                rotateX((float) Math.toRadians(rotation.x)).
                rotateY((float) Math.toRadians(rotation.y)).
                rotateZ((float) Math.toRadians(rotation.z)).
                scale(scale);

        orthoModelMatrix.set(orthoMatrix);

        orthoModelMatrix.mul(modelViewMatrix);

        return orthoModelMatrix;
    }

}
