package engine.graphics;

import org.joml.Matrix4d;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL44.*;

public class ShaderProgram {
    private final int programId;

    private int vertexShaderId;

    private int fragmentShaderId;

    private final Map<String, Integer> uniforms;

    public ShaderProgram() throws Exception{
        programId = glCreateProgram();
        uniforms = new HashMap<>();
        if (programId == 0){
            throw new Exception("Could not create shader!");
        }
    }

    public void createUniform(String uniformName) throws Exception{
        int uniformLocation = glGetUniformLocation(programId, uniformName);

        if (uniformLocation < 0) {
            throw new Exception("Could not find uniform: " + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    public void setUniform(String uniformName, Matrix4d value){
        // dump the matrix into a float buffer
        try(MemoryStack stack = MemoryStack.stackPush()){
            glUniformMatrix4fv(uniforms.get(uniformName), false,
                    value.get(stack.mallocFloat(16)));
        }
    }

    public void setUniform(String uniformName, int value) {
        glUniform1i(uniforms.get(uniformName), value);
    }

    public void createVertexShader(String shaderCode) throws Exception{
        vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) throws Exception{
        fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    protected int createShader(String shaderCode, int shaderType) throws Exception{
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0){
            throw new Exception("Error creating shader. Type: " + shaderType);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0){
            throw new Exception("Error compiling shader code: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }

    public void link() throws Exception{
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0){
            throw new Exception("Error linking shader code: " + glGetProgramInfoLog(programId, 1024));
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
