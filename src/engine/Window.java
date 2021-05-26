package engine;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL44.*;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private static String title;
    private static int width;
    private static int height;
    private static long windowHandle;
    private static boolean resized;
    private static boolean vSync;
    private static int dumpedKey = -1;

    public static void initWindow(String newTitle, int newWidth, int newHeight, boolean newVSync) {
        title   = newTitle;
        width   = newWidth;
        height  = newHeight;
        vSync   = newVSync;
        resized = false;

        // setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()){
            throw new IllegalStateException("Unable to initialize GLFW!");
        }

        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); //the window will be resizable

        //openGL version 4.4
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 4);

        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE); //allow auto driver optimizations

        // create the window
        windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (windowHandle == NULL){
            throw new RuntimeException("Failed to create the GLFW window!");
        }

        // setup resize callback
        glfwSetFramebufferSizeCallback(windowHandle, (thisWindow, thisWidth, thisHeight) -> {
            width = thisWidth;
            height = thisHeight;
            setWindowResized(true);
        });

        // setup a key callback. it will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            //data stream of key inputs
            if (action == GLFW_PRESS){
                dumpedKey = key;
            } else {
                dumpedKey = -1;
            }
        });

        // make the OpenGL context current
        glfwMakeContextCurrent(windowHandle);

        if (isvSync()){
            //enable v-sync
            glfwSwapInterval(1);
        }else {
            glfwSwapInterval(0);
        }

        //center window
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        glfwSetWindowPos(windowHandle, (d.width - width) / 2, (d.height - height) / 2);

        //make the window visible
        glfwShowWindow(windowHandle);

        GL.createCapabilities();

        //set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        //set depth testing
        glEnable(GL_DEPTH_TEST);
        //glDepthFunc(GL_ALWAYS);

        //enable backface culling
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        // Support for transparencies
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        //hide cursor
        glfwSetInputMode(getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        //load icon todo: needs to be it's own class
        MemoryStack stack = MemoryStack.stackPush();
        IntBuffer w = stack.mallocInt(1);
        IntBuffer h = stack.mallocInt(1);
        IntBuffer channels = stack.mallocInt(1);
        ByteBuffer buf = stbi_load("textures/icon.png", w, h, channels, 4);
        GLFWImage image = GLFWImage.malloc();
        image.set(32,32, buf);
        GLFWImage.Buffer images = GLFWImage.malloc(1);
        images.put(0, image);

        //set icon
        glfwSetWindowIcon(windowHandle, images);
    }


    public static int getDumpedKey(){
        return dumpedKey;
    }

    public static long getWindowHandle(){
        return windowHandle;
    }

    public static void setWindowClearColor(float r, float g, float b, float alpha){
        glClearColor(r, g, b, alpha);
    }

    public static boolean isKeyPressed(int keyCode){
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
    }

    public static boolean windowShouldClose(){
        return glfwWindowShouldClose(windowHandle);
    }

    public static String getTitle(){
        return title;
    }


    private static boolean fullScreen = false;

    public static boolean isFullScreen(){
        return fullScreen;
    }

    public static void toggleFullScreen(){
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        if (!fullScreen) {
            glfwSetWindowMonitor(windowHandle, glfwGetPrimaryMonitor(), d.width / 2, d.height / 2, d.width, d.height, glfwGetVideoMode(glfwGetPrimaryMonitor()).refreshRate());
            width = d.width;
            height = d.height;
            updateVSync();
        }
        else {
            glfwSetWindowMonitor(windowHandle, NULL, d.width / 4, d.height / 4,d.width / 2, d.height / 2, glfwGetVideoMode(glfwGetPrimaryMonitor()).refreshRate());
            width = d.width / 2;
            height = d.height / 2;
            updateVSync();
        }

        fullScreen = !fullScreen;
    }

    public static int getWindowWidth(){
        return width;
    }

    public static int getWindowHeight(){
        return height;
    }

    public static boolean isWindowResized(){
        return resized;
    }

    public static void setWindowResized(boolean newResized){
        resized = newResized;
    }

    public static boolean isvSync(){
        return vSync;
    }

    public static void setVSync(boolean newVSync){
        vSync = newVSync;
        if (vSync){
            glfwSwapInterval(1);
        } else {
            glfwSwapInterval(0);
        }
    }

    public static void updateVSync(){
        if (vSync){
            glfwSwapInterval(1);
        } else {
            glfwSwapInterval(0);
        }
    }

    public static void updateWindowTitle(String newTitle){
        glfwSetWindowTitle(windowHandle, newTitle);
    }

    public static void windowUpdate(){
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

}
