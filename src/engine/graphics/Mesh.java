package engine.graphics;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static engine.graphics.Texture.cleanUpTexture;
import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL44.*;
import static org.lwjgl.system.MemoryUtil.*;

final public class Mesh {

    //most of these acronyms can be learned as to what they're referring to on the Khronos wiki
    //https://www.khronos.org/opengl/wiki/Vertex_Specification

    //openGL VAO array attribution field IDs
    private static final Int2IntOpenHashMap posVboId  = new Int2IntOpenHashMap();
    private static final Int2IntOpenHashMap colorVboId  = new Int2IntOpenHashMap();
    private static final Int2IntOpenHashMap textureVboId  = new Int2IntOpenHashMap();
    private static final Int2IntOpenHashMap idxVboId  = new Int2IntOpenHashMap();
    private static final Int2IntOpenHashMap vertexCount  = new Int2IntOpenHashMap();

    //texture ID field
    private static final Int2IntOpenHashMap texture  = new Int2IntOpenHashMap();

    public static int createMesh(final float[] positions, final float[] colors, final int[] indices, final float[] textCoords, final int newTexture) {

        //the key for the mesh
        //Vertex Array Object (VAO)
        final int thisVaoId = glGenVertexArrays();

        //if you uncomment this, you can see the allocator working in real time :)
        //I just thought this was cool
        //System.out.println(thisVaoId);

        glBindVertexArray(thisVaoId);

        //position VBO
        final int thisPosVboId = glGenBuffers();
        posVboId.put(thisVaoId,thisPosVboId);

        final FloatBuffer posBuffer = memAllocFloat(positions.length);
        posBuffer.put(positions).flip();

        glBindBuffer(GL_ARRAY_BUFFER, thisPosVboId);
        glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        // color VBO
        final int thisColorVboId = glGenBuffers();
        colorVboId.put(thisVaoId,thisColorVboId);

        final FloatBuffer colorBuffer = memAllocFloat(colors.length);
        colorBuffer.put(colors).flip();

        glBindBuffer(GL_ARRAY_BUFFER, thisColorVboId);
        glBufferData(GL_ARRAY_BUFFER, colorBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

        //texture coordinates vbo
        final int thisTextureVboId = glGenBuffers();
        textureVboId.put(thisVaoId, thisTextureVboId);

        FloatBuffer textCoordsBuffer = memAllocFloat(textCoords.length);
        textCoordsBuffer.put(textCoords).flip();

        glBindBuffer(GL_ARRAY_BUFFER, thisTextureVboId);
        glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);


        //index vbo
        final int thisIdxVboId = glGenBuffers();
        idxVboId.put(thisVaoId, thisIdxVboId);

        IntBuffer indicesBuffer = memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, thisIdxVboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        //clean memory
        memFree(posBuffer);
        memFree(colorBuffer);
        memFree(textCoordsBuffer);
        memFree(indicesBuffer);

        //next add the vertex count to it's hashmap
        vertexCount.put(thisVaoId, indices.length);

        //finally, store the texture ID
        texture.put(thisVaoId, newTexture);

        return thisVaoId;
    }

    public static void renderMesh(final int meshID){
        //don't render null mesh
        if (meshID == 0){
            return;
        }
        
        //activate first texture bank
        glActiveTexture(GL_TEXTURE0);

        //bind the texture
        glBindTexture(GL_TEXTURE_2D, texture.get(meshID));

        //draw the mesh
        glBindVertexArray(meshID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, vertexCount.get(meshID), GL_UNSIGNED_INT, 0);

        //restore data
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
    }

    public static void cleanUpMesh(final int meshID, final boolean deleteTexture){

        //this is directly talking to the C library

        //binding to buffer 0
        //what this means is, unbind any previously bound buffer
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //disable all previously enabled vertex attribution arrays
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(0);

        //clear the buffer data in memory - basically destroy all mesh variables in your GPU
        glDeleteBuffers(posVboId.get(meshID));
        glDeleteBuffers(colorVboId.get(meshID));
        glDeleteBuffers(textureVboId.get(meshID));
        glDeleteBuffers(idxVboId.get(meshID));

        //explicitly break the previous bindings
        //you can read more about this here https://www.khronos.org/registry/OpenGL-Refpages/gl4/html/glBindVertexArray.xhtml
        glBindVertexArray(0);
        //delete the whole object (VAO) - final OpenGL memory interaction for VAO
        glDeleteVertexArrays(meshID);

        //simple OpenGL error check - if this is ever triggered, something has gone SERIOUSLY wrong
        int errorCheckValue = glGetError();
        if (glGetError() != GL_NO_ERROR) {
            System.out.println("Error could not destroy the mesh! Error value: " + errorCheckValue);
        }

        //delete the texture - if explicitly specified to
        if (deleteTexture) {
            cleanUpTexture(texture.get(meshID));
        }

        //finally, clear the data out of Java memory
        posVboId.remove(meshID);
        colorVboId.remove(meshID);
        textureVboId.remove(meshID);
        idxVboId.remove(meshID);
        vertexCount.remove(meshID);
        texture.remove(meshID);
    }
}