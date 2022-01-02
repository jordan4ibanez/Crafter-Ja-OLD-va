package engine.graphics;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL44.*;
import static org.lwjgl.stb.STBImage.*;

final public class Texture {
    private static final Int2IntOpenHashMap width  = new Int2IntOpenHashMap();
    private static final Int2IntOpenHashMap height = new Int2IntOpenHashMap();


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

        int thisID = createTexture(buf, thisWidth, thisHeight);

        //crash with assertion error instead of throwing exception
        assert buf != null;

        width.put(thisID, thisWidth);
        height.put(thisID, thisHeight);

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

        int thisID = createTexture(buf, thisWidth,thisHeight);

        //crash with assertion error instead of throwing exception
        assert buf != null;

        width.put(thisID, thisWidth);
        height.put(thisID, thisHeight);

        stbi_image_free(buf);

        return thisID;
    }

    private static int createTexture(ByteBuffer buf, int thisWidth, int thisHeight) {
        // Create a new OpenGL texture
        int textureId = glGenTextures();
        // Bind the texture
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // Upload the texture data
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, thisWidth, thisHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
        // Generate Mip Map
//        glGenerateMipmap(GL_TEXTURE_2D);

        return textureId;
    }

    public static int getWidth(int gettingID) {
        return width.get(gettingID);
    }

    public static int getHeight(int gettingID) {
        return height.get(gettingID);
    }

    public static void cleanUpTexture(int gettingID) {
        glDeleteTextures(gettingID);

        width.remove(gettingID);
        height.remove(gettingID);
    }
}