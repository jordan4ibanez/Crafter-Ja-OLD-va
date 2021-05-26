package engine.graphics;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL44.*;

public class Mesh {
    private final int vaoId;

    private final int posVboId;
    private final int colorVboId;
    private final int textureVboId;
    private final int idxVboId;
    private final int vertexCount;

    private Texture texture;

    public Mesh(float[] positions, float[] colors, int[] indices, float[] textCoords, Texture texture) {

        FloatBuffer posBuffer = null;
        FloatBuffer colorBuffer = null;
        IntBuffer indicesBuffer = null;
        FloatBuffer textCoordsBuffer = null;

        try{

            //System.out.println("mesh created"+Math.random());

            this.texture = texture;
            this.vertexCount = indices.length;
            this.vaoId = glGenVertexArrays();
            glBindVertexArray(this.vaoId);

            //position VBO
            this.posVboId = glGenBuffers();

            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();

            glBindBuffer(GL_ARRAY_BUFFER, this.posVboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // color VBO
            this.colorVboId = glGenBuffers();

            colorBuffer = MemoryUtil.memAllocFloat(colors.length);
            colorBuffer.put(colors).flip();

            glBindBuffer(GL_ARRAY_BUFFER, colorVboId);
            glBufferData(GL_ARRAY_BUFFER, colorBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

            //texture coordinates vbo
            this.textureVboId = glGenBuffers();

            textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.length);
            textCoordsBuffer.put(textCoords).flip();

            glBindBuffer(GL_ARRAY_BUFFER, textureVboId);
            glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);


            //index vbo
            this.idxVboId = glGenBuffers();

            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

        } finally {
            
            if (posBuffer != null){
                MemoryUtil.memFree(posBuffer);
            }

            if (colorBuffer != null){
                MemoryUtil.memFree(colorBuffer);
            }

            if (textCoordsBuffer != null){
                MemoryUtil.memFree(textCoordsBuffer);
            }

            if (indicesBuffer != null){
                MemoryUtil.memFree(indicesBuffer);
            }
        }
    }

    public void render(){
        //activate first texture bank
        glActiveTexture(GL_TEXTURE0);

        //bind the texture
        glBindTexture(GL_TEXTURE_2D, this.texture.getId());

        //draw the mesh
        glBindVertexArray(this.vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, this.vertexCount, GL_UNSIGNED_INT, 0);

        //restore data
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
    }

    public void cleanUp(boolean deleteTexture){

        //https://openglbook.com/chapter-2-vertices-and-shapes.html

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(0);

        glDeleteBuffers(this.posVboId);
        glDeleteBuffers(this.colorVboId);
        glDeleteBuffers(this.textureVboId);
        glDeleteBuffers(this.idxVboId);

        glBindVertexArray(0);
        glDeleteVertexArrays(this.vaoId);

        int ErrorCheckValue = glGetError();

        if(ErrorCheckValue != GL_NO_ERROR){
            System.out.println("Error could not destroy the mesh!! Error: " + ErrorCheckValue);
        }


        //delete the texture
        if (deleteTexture) {
            this.texture.cleanup();
        }

        this.texture = null;
    }
}
