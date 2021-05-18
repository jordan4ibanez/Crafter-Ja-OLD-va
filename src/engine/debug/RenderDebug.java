package engine.debug;

import engine.graph.Mesh;
import engine.graph.ShaderProgram;
import org.joml.Matrix4d;

import static engine.Window.getWindowHeight;
import static engine.Window.getWindowWidth;
import static engine.graph.Transformation.getProjectionMatrix;
import static engine.graph.Transformation.getViewMatrix;
import static engine.render.GameRenderer.*;
import static engine.render.GameRenderer.getzFar;

public class RenderDebug {
    public static void renderDebug() {

        ShaderProgram shaderProgram = getShaderProgram();

        ShaderProgram hudShaderProgram = getHudShaderProgram();

        Mesh workerMesh;
        clearScreen();

        rescaleWindow();

        shaderProgram.bind();

        //update projection matrix
        Matrix4d projectionMatrix = getProjectionMatrix(72f, getWindowWidth(), getWindowHeight(), getzNear(), getzFar());

        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        //update the view matrix
        Matrix4d viewMatrix = getViewMatrix();

        shaderProgram.setUniform("texture_sampler", 0);



        shaderProgram.unbind();

    }
}
