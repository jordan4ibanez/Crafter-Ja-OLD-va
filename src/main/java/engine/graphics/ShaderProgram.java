package engine.graphics;

import org.joml.Matrix4d;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL44.*;

public class ShaderProgram {
    private final int programId;
    private final int vertexShaderId;
    private final int fragmentShaderId;
    private final Map<String, Integer> uniforms;

    public ShaderProgram(String vertexCode, String fragmentCode){
        programId = glCreateProgram();
        uniforms = new HashMap<>();
        if (programId == 0){
            try {
                throw new Exception("Could not create shader!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        vertexShaderId = createVertexShader(vertexCode);
        fragmentShaderId = createFragmentShader(fragmentCode);

        this.link();
    }

    public void createUniform(String uniformName){
        int uniformLocation = glGetUniformLocation(programId, uniformName);

        if (uniformLocation < 0) {
            try {
                throw new Exception("Could not find uniform: " + uniformName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        uniforms.put(uniformName, uniformLocation);
    }

    public void setUniform(String uniformName, Matrix4d value){
        // dump the matrix into a float buffer
        try(MemoryStack stack = MemoryStack.stackPush()){
            glUniformMatrix4fv(uniforms.get(uniformName), false, value.get(stack.mallocFloat(16)));
        }
    }

    public void setLightUniform(String uniformName, float value){
        // dump the byte into the new float buffer
        glUniform1f( uniforms.get(uniformName), value);

    }

    public void setUniform(String uniformName, int value) {
        glUniform1i(uniforms.get(uniformName), value);
    }

    private int createVertexShader(String shaderCode){
        return createShader(shaderCode, GL_VERTEX_SHADER);
    }

    private int createFragmentShader(String shaderCode){
        return createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    protected int createShader(String shaderCode, int shaderType){
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0){
            try {
                throw new Exception("Error creating shader. Type: " + shaderType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0){
            try {
                throw new Exception("Error compiling shader code: " + glGetShaderInfoLog(shaderId, 1024));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }

    private void link(){
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0){
            try {
                throw new Exception("Error linking shader code: " + glGetProgramInfoLog(programId, 1024));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(vertexShaderId != 0){
            glDetachShader(programId, vertexShaderId);
        }

        if (fragmentShaderId != 0){
            glDetachShader(programId, fragmentShaderId);
        }

        glValidateProgram(programId);

        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0){
            System.err.println("Warning validating shader code: " + glGetProgramInfoLog(programId, 1024));
        }
    }

    public void bind(){
        glUseProgram(programId);
    }

    public void unbind(){
        glUseProgram(0);
    }

    public void cleanup(){
        unbind();
        if (programId != 0){
            glDeleteProgram(programId);
        }
    }
}
