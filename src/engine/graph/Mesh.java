package engine.graph;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

public class Mesh {
    private int vaoId;

    private int posVboId;

    private int colorVboId;

    private int textureVboId;

    private int idxVboId;

    private int vertexCount;

    private Texture texture;

    private static FloatBuffer posBuffer = null;
    private static FloatBuffer colorBuffer = null;
    private static IntBuffer indicesBuffer = null;
    private static FloatBuffer textCoordsBuffer = null;

    public Mesh(){

    }

    public Mesh(float[] positions, float[] colors, int[] indices, float[] textCoords, Texture texture) {

        try{
            this.texture = texture;

            vertexCount = indices.length;

            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            //position VBO
            posVboId = glGenBuffers();

            posBuffer = MemoryUtil.memAllocFloat(positions.length);

            posBuffer.put(positions).flip();

            glBindBuffer(GL_ARRAY_BUFFER, posVboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // color VBO
            colorVboId = glGenBuffers();

            colorBuffer = MemoryUtil.memAllocFloat(colors.length);


            colorBuffer.put(colors).flip();
            glBindBuffer(GL_ARRAY_BUFFER, colorVboId);
            glBufferData(GL_ARRAY_BUFFER, colorBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

            //texture coordinates vbo
            textureVboId = glGenBuffers();

            textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.length);



            textCoordsBuffer.put(textCoords).flip();
            glBindBuffer(GL_ARRAY_BUFFER, textureVboId);
            glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);


            //index vbo
            idxVboId = glGenBuffers();

            indicesBuffer = MemoryUtil.memAllocInt(indices.length);

            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

        } finally {

            if (posBuffer != null){
                MemoryUtil.memFree(posBuffer);
                posBuffer = null;
            }

            if (colorBuffer != null){
                MemoryUtil.memFree(colorBuffer);
                colorBuffer = null;
            }

            if (textCoordsBuffer != null){
                MemoryUtil.memFree(textCoordsBuffer);
                textCoordsBuffer = null;
            }

            if (indicesBuffer != null){
                MemoryUtil.memFree(indicesBuffer);
                indicesBuffer = null;
            }
        }
    }

    public int getVaoId(){
        return vaoId;
    }

    public int getVertexCount(){
        return vertexCount;
    }

    public void render(){
        //activate first texture bank
        glActiveTexture(GL_TEXTURE0);

        //bind the texture
        glBindTexture(GL_TEXTURE_2D, texture.getId());

        //draw the mesh
        glBindVertexArray(getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);

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

        glDeleteBuffers(posVboId);
        glDeleteBuffers(colorVboId);
        glDeleteBuffers(textureVboId);
        glDeleteBuffers(idxVboId);

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);

        int ErrorCheckValue = glGetError();

        if(ErrorCheckValue != GL_NO_ERROR){
            System.out.println("Error could not destroy the mesh!! Error: " + ErrorCheckValue);
        }


        //delete the texture
        if (deleteTexture) {
            texture.cleanup();
        }

        this.texture = null;
    }
}
