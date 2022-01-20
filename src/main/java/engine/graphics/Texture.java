package engine.graphics;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL44.*;
import static org.lwjgl.stb.STBImage.*;

final public class Texture {

    private final int ID;

    private final int width  ;
    private final int height ;

    public Texture(String fileName) {

        final ByteBuffer buf;

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

            this.width = w.get();
            this.height = h.get();
        }

        this.ID = createGLTexture(buf, this.width, this.height);

        //crash with assertion error instead of throwing exception
        assert buf != null;

        stbi_image_free(buf);
    }

    //create texture from image buffer
    public Texture(ByteBuffer imageBuffer) {
        final ByteBuffer buf;

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

            this.width = w.get();
            this.height = h.get();
        }

        this.ID = createGLTexture(buf, this.width,this.height);

        //crash with assertion error instead of throwing exception
        assert buf != null;

        stbi_image_free(buf);
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

    public int getID(){
        return this.ID;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void cleanUp() {
        glDeleteTextures(this.ID);
    }
}