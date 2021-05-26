package engine.debug;

import engine.Utils;
import engine.graphics.ShaderProgram;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.Time.getDelta;
import static engine.Window.getWindowHeight;
import static engine.Window.getWindowWidth;
import static engine.debug.DebugTerrainDrawTypes.getDebugMesh;
import static engine.graphics.Transformation.*;
import static engine.render.GameRenderer.*;
import static org.lwjgl.opengl.GL44.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL44C.*;

public class RenderDebug {

    private static float debugRotation = 0f;

    private static ShaderProgram debugShaderProgram;

    public static void initializeDebugRenderShader() throws Exception {
        debugShaderProgram = new ShaderProgram();
        debugShaderProgram.createVertexShader(Utils.loadResource("/resources/glasslike_vertex.vs"));
        debugShaderProgram.createFragmentShader(Utils.loadResource("/resources/glasslike_fragment.fs"));
        debugShaderProgram.link();

        //create uniforms for world and projection matrices
        debugShaderProgram.createUniform("projectionMatrix");
        //create uniforms for model view matrix
        debugShaderProgram.createUniform("modelViewMatrix");
        //create uniforms for texture sampler
        debugShaderProgram.createUniform("texture_sampler");
    }

    public static void renderDebug() {

        clearScreen();

        rescaleWindow();

        //glEnable(GL_ALPHA_TEST);

        //enable transparent textures

        glDisable(GL_BLEND);
        //glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        //set depth testing
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        //enable backface culling
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        glEnable(GL_ALPHA_TEST);

        //glDisable(GL_DEPTH_WRITEMASK);


        //glDepthMask(false);
        //glDepthFunc();

        debugShaderProgram.bind();

        //update projection matrix
        Matrix4d projectionMatrix = getProjectionMatrix(getFOV(), getWindowWidth(), getWindowHeight(), getzNear(), 1000);

        debugShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        //update the view matrix
        Matrix4d viewMatrix = getViewMatrix();

        debugShaderProgram.setUniform("texture_sampler", 0);

        debugRotation += getDelta() * 10;

        Matrix4d modelViewMatrix = updateModelViewMatrix(new Vector3d(0, 0, -25), new Vector3f(0, debugRotation, 0), viewMatrix);
        debugShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
        getDebugMesh().render();


        debugShaderProgram.unbind();

    }
}
