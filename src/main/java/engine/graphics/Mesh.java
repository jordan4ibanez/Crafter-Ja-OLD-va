package engine.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL44.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Mesh {

    //most of these acronyms can be learned as to what they're referring to on the Khronos wiki
    //https://www.khronos.org/opengl/wiki/Vertex_Specification

    private final int vaoId        ;

    //openGL VAO array attribution field IDs
    private final int posVboId     ;
    private final int colorVboId   ;
    private final int textureVboId ;
    private final int idxVboId     ;
    private final int vertexCount  ;
    //texture ID field
    private final Texture texture  ;


    public Mesh (float[] positions, float[] colors, int[] indices, float[] textCoords, Texture newTexture) {

        //the key for the mesh
        //Vertex Array Object (VAO)
        this.vaoId = glGenVertexArrays();

        glBindVertexArray(this.vaoId);

        //position VBO
        this.posVboId = glGenBuffers();

        final FloatBuffer posBuffer = memAllocFloat(positions.length);
        posBuffer.put(positions).flip();

        glBindBuffer(GL_ARRAY_BUFFER, this.posVboId);
        glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        // color VBO
        this.colorVboId = glGenBuffers();

        final FloatBuffer colorBuffer = memAllocFloat(colors.length);
        colorBuffer.put(colors).flip();

        glBindBuffer(GL_ARRAY_BUFFER, this.colorVboId);
        glBufferData(GL_ARRAY_BUFFER, colorBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

        //texture coordinates vbo
        this.textureVboId = glGenBuffers();

        FloatBuffer textCoordsBuffer = memAllocFloat(textCoords.length);
        textCoordsBuffer.put(textCoords).flip();

        glBindBuffer(GL_ARRAY_BUFFER, this.textureVboId);
        glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);


        //index vbo
        this.idxVboId = glGenBuffers();

        IntBuffer indicesBuffer = memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.idxVboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        //clean memory
        memFree(posBuffer);
        memFree(colorBuffer);
        memFree(textCoordsBuffer);
        memFree(indicesBuffer);

        //next add the vertex count to it's int
        this.vertexCount = indices.length;

        //finally, store the texture ID
        this.texture = newTexture;
    }

    public void render(){
        //activate first texture bank
        glActiveTexture(GL_TEXTURE0);

        //bind the texture
        glBindTexture(GL_TEXTURE_2D, this.texture.getID());

        //bind the mesh vertex array
        glBindVertexArray(this.vaoId);
        //glEnableVertexAttribArray(0);
        //glEnableVertexAttribArray(1);

        //draw the mesh
        glDrawElements(GL_TRIANGLES, this.vertexCount, GL_UNSIGNED_INT, 0);

        //restore data
        //glDisableVertexAttribArray(0);
        //glDisableVertexAttribArray(1);
        glBindVertexArray(0);
    }

    public void cleanUp(boolean deleteTexture){

        //this is directly talking to the C library

        //binding to buffer 0
        //what this means is, unbind any previously bound buffer
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //disable all previously enabled vertex attribution arrays
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(0);

        //clear the buffer data in memory - basically destroy all mesh variables in your GPU
        glDeleteBuffers(this.posVboId);
        glDeleteBuffers(this.colorVboId);
        glDeleteBuffers(this.textureVboId);
        glDeleteBuffers(this.idxVboId);



        //explicitly break the previous bindings
        //you can read more about this here https://www.khronos.org/registry/OpenGL-Refpages/gl4/html/glBindVertexArray.xhtml
        glBindVertexArray(0);
        //delete the whole object (VAO) - final OpenGL memory interaction for VAO
        glDeleteVertexArrays(this.vaoId);

        //simple OpenGL error check - if this is ever triggered, something has gone SERIOUSLY wrong
        int errorCheckValue = glGetError();
        if (glGetError() != GL_NO_ERROR) {
            System.out.println("Error could not destroy the mesh! Error value: " + errorCheckValue);
        }

        //delete the texture - if explicitly specified to
        if (deleteTexture) {
            texture.cleanUp();
        }
    }
}