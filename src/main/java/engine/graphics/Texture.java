package engine.graphics;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL44.*;
import static org.lwjgl.stb.STBImage.*;

final public class Texture {

    //(4 bytes * currentBufferSize) = bytes of memory per array
    private static int currentBufferSize = 0;

    private static int[] width = new int[0];
    private static int[] height = new int[0];

    //this expands the arrays - keeps them in sync as well
    private static void expandMemory(){
        //+10 because memory is cheap on this scale
        currentBufferSize += 10;

        //debug info
        //System.out.println("EXPANDING TEXTURE ARRAY TO: " + currentBufferSize);

        int[] width2  = new int[currentBufferSize];
        int[] height2 = new int[currentBufferSize];

        System.arraycopy(width, 0, width2, 0, width.length);
        System.arraycopy(height, 0, height2, 0, height.length);

        width  = width2;
        height = height2;
    }

    public static int createTexture(String fileName) {

        final ByteBuffer buf;
        int thisWidth;
        int thisHeight;

        // Load Texture file
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer w = stack.mallocInt(1);
            final IntBuffer h = stack.mallocInt(1);
            final IntBuffer channels = stack.mallocInt(1);

            buf = stbi_load(fileName, w, h, channels, 4);
            if (buf == null) {
                System.out.println("Image file [" + fileName  + "] not loaded: " + stbi_failure_reason());
                //throw new Exception("Image file [" + fileName  + "] not loaded: " + stbi_failure_reason());
            }

            thisWidth = w.get();
            thisHeight = h.get();
        }

        int thisID = createGLTexture(buf, thisWidth, thisHeight);

        //this works on the assumption that the OpenGL allocator uses only free slots
        //IE: 1->2->3->(2 gets freed)->2->4
        if (thisID >= currentBufferSize){
            expandMemory();
        }

        //debug info
        //System.out.println("NEW TEXTURE: " + thisID);

        //crash with assertion error instead of throwing exception
        assert buf != null;

        width[thisID]  = thisWidth;
        height[thisID] = thisHeight;

        stbi_image_free(buf);

        return thisID;
    }

    //create texture from image buffer
    public static int createTexture(ByteBuffer imageBuffer) {
        final ByteBuffer buf;
        int thisWidth;
        int thisHeight;

        // Load Texture file
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer w = stack.mallocInt(1);
            final IntBuffer h = stack.mallocInt(1);
            final IntBuffer channels = stack.mallocInt(1);

            buf = stbi_load_from_memory(imageBuffer, w, h, channels, 4);
            if (buf == null) {
                System.out.println("Image file not loaded: " + stbi_failure_reason());
                //throw new Exception("Image file not loaded: " + stbi_failure_reason());
            }

            thisWidth = w.get();
            thisHeight = h.get();
        }

        int thisID = createGLTexture(buf, thisWidth,thisHeight);

        //this works on the assumption that the OpenGL allocator uses only free slots
        //IE: 1->2->3->(2 gets freed)->2->4
        if (thisID >= currentBufferSize){
            expandMemory();
        }

        //debug info
        //System.out.println("NEW TEXTURE: " + thisID);

        //crash with assertion error instead of throwing exception
        assert buf != null;

        width[thisID] = thisWidth;
        height[thisID] = thisHeight;

        stbi_image_free(buf);

        return thisID;
    }

    private static int createGLTexture(ByteBuffer buf, int thisWidth, int thisHeight) {
        // Create a new OpenGL texture
        int textureId = glGenTextures();
        // Bind the texture
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        //GL LINEAR gives it a smoothened look like an n64 game
        //GL NEAREST gives it a blocky look
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        //Do not repeat texture
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);


        // Generate Mip Map
        //glGenerateMipmap(GL_TEXTURE_2D);

        // Upload the texture data
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, thisWidth, thisHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);

        return textureId;
    }

    public static int getWidth(int gettingID) {
        return width[gettingID];
    }

    public static int getHeight(int gettingID) {
        return height[gettingID];
    }

    public static void cleanUpTexture(int gettingID) {
        glDeleteTextures(gettingID);

        width[gettingID] = 0;
        height[gettingID] = 0;
    }
}